/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferRepository offerRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    private static Offer offer;
    private static List<Application> applications = new ArrayList<>();

    @BeforeClass
    public static void beforeClass(){
        offer = new Offer();
        offer.setId(1);
        //  future date
        offer.setStartDate(LocalDate.of(
                2019,
                LocalDate.now().getMonth().getValue() + 1,
                23));
        offer.setJobTitle("job title1");

        Offer offer2 = new Offer();
        offer2.setId(2);
        //  future date
        offer2.setStartDate(LocalDate.of(
                2019,
                LocalDate.now().getMonth().getValue() + 2,
                23));
        offer2.setJobTitle("job title2");

        Application application = new Application();
        application.setId(1);
        application.setResume("resume1");
        application.setEmail("email1@email.com");
        application.setOffer(offer);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setResume("resume2");
        application.setEmail("email2@email.com");
        application.setOffer(offer);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setResume("resume2");
        application.setEmail("email2@email.com");
        application.setOffer(offer2);

        applications.add(application);
    }

    @Test
    public void getNumberOfApplicationsTotal_withNoApplications() throws Exception{
        mockMvc.perform(
                get("/applications/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    public void getNumberOfApplicationsTotal_withApplications() throws Exception{
        when(applicationRepository.findAll()).thenReturn(applications);

        assertEquals(3, applications.size());

        mockMvc.perform(
                get("/applications/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(applications.size())));
    }

    @Test
    public void getNumberOfApplicationsTotal_withInvalidIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications/total?offerId=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    public void getNumberOfApplicationsTotal_withInvalidNotIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications/total?offerId=e0w")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    public void getNumberOfApplicationsTotal_withValidOfferId() throws Exception{
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(offer.getApplications());

        mockMvc.perform(
                get("/applications/total?offerId=" + offer.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(offer.getApplications().size())));
    }

    @Test
    public void getNumberOfApplicationsTotal_withValidOfferIdAndZeroApplications() throws Exception{
        Offer offer0 = new Offer();
        offer0.setId(1);
        when(offerRepository.findById(offer0.getId())).thenReturn(Optional.of(offer0));
        when(applicationRepository.findAllByOffer(offer0)).thenReturn(offer0.getApplications());

        mockMvc.perform(
                get("/applications/total?offerId=" +  offer0.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(offer0.getApplications().size())));
    }

}
