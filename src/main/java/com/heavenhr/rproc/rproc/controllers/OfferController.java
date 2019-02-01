/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.exceptions.OfferAlreadySubmittedException;
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import com.heavenhr.rproc.rproc.messaging.RabbitNotificationService;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.recourseassemblers.OfferResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(path = "/offers", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class OfferController {
    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final OfferResourceAssembler offerResourceAssembler;
    private final ApplicationResourceAssembler applicationResourceAssembler;

    @Autowired
    public OfferController(
            OfferRepository offerRepository,
            ApplicationRepository applicationRepository,
            RabbitNotificationService rabbitNotificationService,
            OfferResourceAssembler offerResourceAssembler,
            ApplicationResourceAssembler applicationResourceAssembler){

        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.offerResourceAssembler = offerResourceAssembler;
        this.applicationResourceAssembler = applicationResourceAssembler;
    }

    /**
     * get list of all offers.  GET /offers
     * @return
     */
    @GetMapping
    public Resources<Resource<Offer>> allOffers(){
        List<Resource<Offer>> resources = StreamSupport.stream(offerRepository.findAll().spliterator(), false)
                .map(offerResourceAssembler::toResource)
                .collect(Collectors.toList());

        return new Resources<>(
                resources,
                linkTo(methodOn(OfferController.class).allOffers()).withSelfRel());
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

        return ResponseEntity.ok(offerResourceAssembler.toResource(offer));
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
            Errors errors) throws URISyntaxException {

        if (errors.hasErrors()){
            return ResponseEntity.badRequest().body(
                    ErrorResponse.buildFromErrors(errors)
            );
        }

        try{
            offer = offerRepository.save(offer);
        }catch (org.springframework.dao.DataIntegrityViolationException e){
            throw new OfferAlreadySubmittedException();
        }

        Resource<Offer> resource = offerResourceAssembler.toResource(offer);

        return ResponseEntity
                .created(new URI(resource.getId().getHref()))
                .body(resource);
    }
}
