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
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import com.heavenhr.rproc.rproc.messaging.RabbitNotificationService;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @Autowired
    public ApplicationController(
            ApplicationRepository applicationRepository,
            ApplicationResourceAssembler applicationResourceAssembler,
            OfferRepository offerRepository,
            RabbitNotificationService rabbitNotificationService
    ){
        this.applicationRepository = applicationRepository;
        this.applicationResourceAssembler = applicationResourceAssembler;
        this.offerRepository = offerRepository;
        this.rabbitNotificationService = rabbitNotificationService;
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
            Errors errors
    ) {
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
            applicationRepository.save(application);
        }catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new ApplicationAlreadySubmittedException();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(applicationPartial);
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
