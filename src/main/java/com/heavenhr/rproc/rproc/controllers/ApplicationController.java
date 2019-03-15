/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.ApplicationPartial;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.exceptions.ApplicationAlreadySubmittedException;
import com.heavenhr.rproc.rproc.exceptions.ApplicationNotFoundException;
import com.heavenhr.rproc.rproc.exceptions.ApplicationResumeAlreadySubmittedException;
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import com.heavenhr.rproc.rproc.messaging.RabbitNotificationService;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import com.heavenhr.rproc.rproc.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(path = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final ApplicationResourceAssembler applicationResourceAssembler;
    private final OfferRepository offerRepository;
    private final RabbitNotificationService rabbitNotificationService;
    private final StorageService storageService;

    @Autowired
    public ApplicationController(
            ApplicationRepository applicationRepository,
            ApplicationResourceAssembler applicationResourceAssembler,
            OfferRepository offerRepository,
            RabbitNotificationService rabbitNotificationService,
            StorageService storageService
    ){
        this.applicationRepository = applicationRepository;
        this.applicationResourceAssembler = applicationResourceAssembler;
        this.offerRepository = offerRepository;
        this.rabbitNotificationService = rabbitNotificationService;
        this.storageService = storageService;
    }

    /**
     * get list of all applications.  GET /applications[?offerId=[offerId]]
     *
     * with offerId specified applications' list for the offerId returned
     *
     * @return
     */
    @GetMapping
    public Resources<Resource<Application>> allApplications(
            @RequestParam(value = "offerId", required = false) Offer offer){

        Spliterator<Application> applicationSpliterator;

        if (offer == null){
            applicationSpliterator = applicationRepository.findAll().spliterator();
        }else{
            applicationSpliterator = applicationRepository.findAllByOffer(offer).spliterator();
        }

        List<Resource<Application>> resources = StreamSupport.stream(
                    applicationSpliterator,
                    false)
                .map(applicationResourceAssembler::toResource)
                .collect(Collectors.toList());

        return new Resources<>(
                resources,
                linkTo(methodOn(ApplicationController.class).allApplications(null)).withSelfRel());
    }

    /**
     * get the total number of applications. GET /applications/total[?offerId=[offerId]]
     *
     * if request param offerId is not set returns total number of applications,
     * or number of applications for the [offerId]
     *
     * @return
     */
    @GetMapping(path = "/total")
    public Map<String, Long> getNumberOfApplicationsTotal(
            @RequestParam(value = "offerId", required = false) Offer offer){

        Spliterator<Application> applicationSpliterator;
        if (offer == null){
            applicationSpliterator = applicationRepository.findAll().spliterator();
        }else{
            applicationSpliterator = applicationRepository.findAllByOffer(offer).spliterator();
        }

        long total = StreamSupport.stream(applicationSpliterator, false).count();

        return new HashMap<String, Long>(){{put("total", total);}};
    }

    /**
     * read a single application. GET /applications/[appId]
     *
     * @param appId
     * @return
     */
    @GetMapping(path = "/{appId:[\\d]+}")
    public ResponseEntity<Resource<Application>> getApplication(
            @PathVariable(value = "appId") int appId){
        Application application = applicationRepository
                .findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        return ResponseEntity.ok(applicationResourceAssembler.toResource(application));
    }

    /**
     * create an application. POST /applications
     *
     * @param applicationPartial
     * @param errors
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitApplication(
            @Valid @RequestBody ApplicationPartial applicationPartial,
            Errors errors) throws URISyntaxException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrors(errors)
            );
        }

        int offerId = applicationPartial.getOfferId();
        Offer offer = offerRepository
                .findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId));

        Application application = new Application(applicationPartial);
        application.setOffer(offer);
        try{
            application = applicationRepository.save(application);
        }catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new ApplicationAlreadySubmittedException();
        }

        Resource<Application> resource = applicationResourceAssembler.toResource(application);
        resource.add(linkTo(methodOn(ApplicationController.class).getApplication(application.getId()))
                .slash(application.getUploadHash())
                .withRel("upload"));

        return ResponseEntity
                .created(new URI(resource.getId().getHref()))
                .body(resource);
    }

    @PostMapping(path = "/{appId:[\\d]+}/{hashCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitResumeFile(
            @PathVariable(value = "appId") int appId,
            @PathVariable(value = "hashCode") String hashCode,
            @RequestParam(value = "resume") MultipartFile resumeFile){

        Application application = applicationRepository
                .findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        if (application.getResumeFile() != null && !application.getResumeFile().isEmpty()){
            //  file is already uploaded, reject any further uploads
            throw new ApplicationResumeAlreadySubmittedException(appId);
        }

        if (application.getUploadHash() == null){
            //  should not be like that
            throw new ApplicationNotFoundException(appId);
        }

        if (!application.getUploadHash().toString().equals(hashCode)){
            throw new ApplicationNotFoundException(appId);
        }

        storageService.store(resumeFile);

        return ResponseEntity.ok(new HashMap<String, String>(){{put("status", "uploaded");}});
    }

    /**
     * progress application status. PATCH /application/[appId]/status
     *
     * @param appId
     * @param applicationPatch
     * @return
     */
    @PatchMapping(path = "/{appId:[\\d]+}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> patchApplication(
            @PathVariable(value = "appId") int appId,
            @RequestBody Map<String, String> applicationPatch
    ){
        Application application = applicationRepository
                .findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        String applicationStatus = applicationPatch.getOrDefault("applicationStatus", null);
        log.info(
                "requested applicationStatus patch from '{}' to '{}'",
                application.getApplicationStatus().toString(),
                applicationStatus);
        if (applicationStatus == null) throw new IllegalArgumentException("Invalid application status");

        application.setApplicationStatus(ApplicationStatus.valueOf(applicationStatus.toUpperCase()));

        offerRepository.save(application.getOffer());
        log.info(
                "saving application '{}' with {} history items",
                application,
                application.getApplicationStatusHistories().size());
        applicationRepository.save(application);

        try{
            log.debug("trying to send notification to the application: '{}'", application);
            rabbitNotificationService.sendNotification(application);
        }catch (AmqpException e){
            log.error(
                    "couldn't sent notification to the application '{}' due to exception: '{}'",
                    application,
                    e.getMessage());
            //  TODO do proper error handling in case of problem with rabbintmq server
            //  sending notification the same as using rabbitmq is auxiliary functionality at this point
            //  this is why all exceptions from AMQP just suppressed
        }

        applicationPatch.put("status", "updated");
        return ResponseEntity.ok(applicationPatch);
    }
}
