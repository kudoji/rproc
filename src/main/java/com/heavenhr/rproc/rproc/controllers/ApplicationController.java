/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.exceptions.ApplicationNotFoundException;
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    public ApplicationController(
            ApplicationRepository applicationRepository,
            ApplicationResourceAssembler applicationResourceAssembler
    ){
        this.applicationRepository = applicationRepository;
        this.applicationResourceAssembler = applicationResourceAssembler;
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
}
