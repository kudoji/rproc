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
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping(path = "/offers", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class OfferController {
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    private RabbitNotificationService rabbitNotificationService;

    @Autowired
    public OfferController(
            OfferRepository offerRepository,
            ApplicationRepository applicationRepository){

        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * get list of all offers.  GET /offers/all
     * @return
     */
    @GetMapping(path = "/all")
    public Iterable<Offer> allOffers(){
        return offerRepository.findAll();
    }

    /**
     * get the total number of applications. GET /offers/apps_total
     *
     * @return
     */
    @GetMapping(path = "/apps_total")
    public Map<String, Long> getNumberOfApplicationsTotal(){
        long total = StreamSupport.stream(applicationRepository.findAll().spliterator(), false).count();

        return new HashMap<String, Long>(){{put("apps_total", total);}};
    }

    /**
     * get the total number of applications for an offer. GET /offers/[offerId]/apps_total
     *
     * @param offerId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}/apps_total")
    public ResponseEntity<?> getNumberOfApplicationsPerOffer(@PathVariable(value = "offerId") int offerId){
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        long total = StreamSupport.stream(
                    applicationRepository.findAllByOffer(offer).spliterator(),
                    false)
                .count();

        return ResponseEntity.ok(new HashMap<String, Long>(){{put("apps_total", total);}});
    }

    /**
     * read a single offer. GET /offers/[id]
     *
     * @param offerId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}")
    public ResponseEntity<?> getOfferById(@PathVariable(value = "offerId") int offerId){
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        return ResponseEntity.ok(offer);
    }

    /**
     * get list of all applications for an offer. GET /offers/[offerId]/all
     *
     * @param offerId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}/all")
    public ResponseEntity<?> allApplicationsPerOffers(@PathVariable(value = "offerId") int offerId){
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        return ResponseEntity.ok(applicationRepository.findAllByOffer(offer));
    }

    /**
     * read a single application for an offer. GET /offers/[offerId]/[appId]
     *
     * @param offerId
     * @param appId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}/{appId:[\\d]+}")
    public ResponseEntity<?> getApplicationForOffer(
            @PathVariable(value = "offerId") int offerId,
            @PathVariable(value = "appId") int appId){
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        Application application = applicationRepository
                .findByIdAndOffer(appId, offer)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        return ResponseEntity.ok(application);
    }

    /**
     * creates an offer. POST /offers
     *
     * @param offer
     * @param errors
     * @return
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitOffer(
            @Valid @RequestBody Offer offer,
            Errors errors){

        if (errors.hasErrors()){
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrors(errors)
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(offerRepository.save(offer));
    }

    /**
     * create an application. POST /offers/{offerId}
     *
     * @param offerId
     * @param applicationPartial
     * @param errors
     * @return
     */
    @PostMapping(path = "/{offerId:[\\d]+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> submitApplication(
            @PathVariable(value = "offerId") int offerId,
            @Valid @RequestBody ApplicationPartial applicationPartial,
            Errors errors
    ) {
        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrors(errors)
            );
        }

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
     * progress application status. PATCH /offers/app/[appId]
     *
     * @param appId
     * @param applicationPatch
     * @return
     */
    @PatchMapping(path = "/app/{appId:[\\d]+}", consumes = MediaType.APPLICATION_JSON_VALUE)
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
        if (applicationStatus == null) throw new IllegalArgumentException("Invali application status");

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
