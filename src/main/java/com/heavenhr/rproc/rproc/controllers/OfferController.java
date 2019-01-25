/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(path = "/offers", produces = "application/json")
@CrossOrigin(origins = "*")
public class OfferController {
    private final OfferRepository offerRepository;

    @Autowired
    public OfferController(OfferRepository offerRepository){
        this.offerRepository = offerRepository;
    }

    @GetMapping(path = "/all")
    public Iterable<Offer> allOffers(){
        return offerRepository.findAll();
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
