/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.recourseassemblers;

import com.heavenhr.rproc.rproc.controllers.OfferController;
import com.heavenhr.rproc.rproc.entities.Application;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ApplicationResourceAssembler implements ResourceAssembler<Application, Resource<Application>> {
    @Override
    public Resource<Application> toResource(Application application){
        return new Resource<>(
                application,
                linkTo(methodOn(OfferController.class).getApplicationForOffer(
                        application.getOffer().getId(),
                        application.getId())).withSelfRel()
        );
    }
}
