/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.ApplicationPartial;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping(path = "/offers", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class OfferController {
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;

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
        Optional<Offer> optionalOffer = offerRepository.findById(offerId);
        if (optionalOffer.isPresent()){
            long total = StreamSupport.stream(
                        applicationRepository.findAllByOffer(optionalOffer.get()).spliterator(),
                        false)
                    .count();

            return ResponseEntity.ok(new HashMap<String, Long>(){{put("apps_total", total);}});
        }

        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(
                        "Error: offer with #%d not found",
                        offerId));
    }

    /**
     * read a single offer. GET /offers/[id]
     *
     * @param offerId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}")
    public ResponseEntity<?> getOfferById(@PathVariable(value = "offerId") int offerId){
        Optional<Offer> optionalOffer = offerRepository.findById(offerId);
        if (optionalOffer.isPresent()){
            return ResponseEntity.ok(optionalOffer.get());
        }

        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(
                        "Error: offer with #%d not found",
                        offerId));
    }

    /**
     * get list of all applications for an offer. GET /offers/[offerId]/all
     *
     * @param offerId
     * @return
     */
    @GetMapping(path = "/{offerId:[\\d]+}/all")
    public ResponseEntity<?> allApplicationsPerOffers(@PathVariable(value = "offerId") int offerId){
        Optional<Offer> optionalOffer = offerRepository.findById(offerId);
        if (optionalOffer.isPresent()) {
            return ResponseEntity.ok(applicationRepository.findAllByOffer(optionalOffer.get()));
        }

        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(
                        "Error: offer with #%d not found",
                        offerId));
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
        Optional<Offer> optionalOffer = offerRepository.findById(offerId);
        if (optionalOffer.isPresent()){
            Optional<Application> optionalApplication = applicationRepository.findByIdAndOffer(
                                                                                appId,
                                                                                optionalOffer.get());
            if (optionalApplication.isPresent()){
                return ResponseEntity.ok(optionalApplication.get());
            }

            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrorMessage(
                            "Error: application with #%d not found",
                            appId));
        }

        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(
                        "Error: offer with #%d not found",
                        offerId));
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
        Optional<Offer> optionalOffer = offerRepository.findById(offerId);
        if (!optionalOffer.isPresent()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrorMessage(
                            "Error: offer with #%d not found",
                            offerId));
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrors(errors)
            );
        }

        Application application = new Application(applicationPartial);
        application.setOffer(optionalOffer.get());
        boolean constrainError = false;
        try{
            application = applicationRepository.save(application);
        }catch (org.springframework.dao.DataIntegrityViolationException e){
            constrainError = true;
        }
        if (constrainError){
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrorMessage("candidate is already submitted resume for the offer")
            );
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
        Optional<Application> optionalApplication = applicationRepository.findById(appId);
        if (!optionalApplication.isPresent()) {
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrorMessage(
                            "Error: application with #%d not found",
                            appId));
        }

        Application application = optionalApplication.get();

        boolean doSave = false;
        String applicationStatus = applicationPatch.getOrDefault("applicationStatus", null);
        log.info(
                "requested applicationStatus patch from '{}' to '{}'",
                application.getApplicationStatus().toString(),
                applicationStatus);
        if (applicationStatus != null){
            try{
                application.setApplicationStatus(ApplicationStatus.valueOf(applicationStatus));

                doSave = true;
            }catch (IllegalArgumentException e){
                return ResponseEntity.badRequest().body(
                        ErrorResponse.buildFromErrorMessage(e.getMessage())
                );
            }
        }

        if (!doSave){
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrorMessage("nothing to patch")
            );
        }

        offerRepository.save(application.getOffer());
        log.info(
                "saving application '{}' with {} history items",
                application,
                application.getApplicationStatusHistories().size());
        applicationRepository.save(application);

        applicationPatch.put("status", "updated");
        return ResponseEntity.ok(applicationPatch);
    }
}
