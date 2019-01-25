/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/offers", produces = "application/json")
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

    @GetMapping(path = "/all")
    public Iterable<Offer> allOffers(){
        return offerRepository.findAll();
    }

    @GetMapping(path = "/apps_total")
    public Map<String, Long> getNumberOfApplicationsTotal(){
        long total = StreamSupport.stream(applicationRepository.findAll().spliterator(), false).count();

        return new HashMap<String, Long>(){{put("apps_total", total);}};
    }

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

    @PostMapping(consumes = "application/json")
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
}
