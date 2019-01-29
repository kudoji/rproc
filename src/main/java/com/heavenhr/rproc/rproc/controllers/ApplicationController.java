/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping(path = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationController(
            ApplicationRepository applicationRepository
    ){
        this.applicationRepository = applicationRepository;

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

}
