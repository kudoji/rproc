/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.recourseassemblers;

import com.heavenhr.rproc.rproc.controllers.ApplicationController;
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
                linkTo(methodOn(ApplicationController.class).getApplication(application.getId())).withSelfRel()
        );
    }
}
