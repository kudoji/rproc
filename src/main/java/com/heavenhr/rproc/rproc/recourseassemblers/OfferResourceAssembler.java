/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.recourseassemblers;

import com.heavenhr.rproc.rproc.controllers.OfferController;
import com.heavenhr.rproc.rproc.entities.Offer;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class OfferResourceAssembler implements ResourceAssembler<Offer, Resource<Offer>> {
    @Override
    public Resource<Offer> toResource(Offer offer){
        return new Resource<>(
                offer,
                linkTo(methodOn(OfferController.class).getOfferById(offer.getId())).withSelfRel()
        );
    }
}
