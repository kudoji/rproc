/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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

    @MockBean
    private ApplicationResourceAssembler applicationResourceAssembler;

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
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setResume("resume2");
        application.setEmail("email2@email.com");
        application.setOffer(offer);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setResume("resume2");
        application.setEmail("email2@email.com");
        application.setOffer(offer2);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);
    }

    @Test
    public void allApplications_withNoApplications() throws Exception{
        mockMvc.perform(
                get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    public void allApplications_withApplications() throws Exception{
        when(applicationRepository.findAll()).thenReturn(applications);
        applications.forEach(a ->
                when(applicationResourceAssembler.toResource(a)).thenReturn(new Resource<>(a)));

        mockMvc.perform(
                get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.applicationList", hasSize(applications.size())))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    public void allApplications_withInvalidIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications?offerId=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    public void allApplications_withInvalidNotIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications?offerId=saw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    public void allApplications_withValidOfferIdAndApplications() throws Exception{
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(offer.getApplications());
        offer.getApplications().forEach(a ->
                when(applicationResourceAssembler.toResource(a)).thenReturn(new Resource<>(a)));

        mockMvc.perform(
                get("/applications?offerId=" + offer.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.applicationList", hasSize(offer.getApplications().size())))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    public void allApplications_withValidOfferIdAndNoApplications() throws Exception{
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/applications?offerId=" + offer.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links").exists());
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

    @Test
    public void getApplication_withInvalidIntAppId() throws Exception{
        mockMvc.perform(
                get("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: application with #")));
    }

    @Test
    public void getApplication_withInvalidNotIntAppId() throws Exception{
        mockMvc.perform(
                get("/applications/ad1w")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getApplication_withValidAppId() throws Exception{
        Application application = applications.get(0);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationResourceAssembler.toResource(application)).thenReturn(new Resource<>(application));

        mockMvc.perform(
                get("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(application.getId())))
                .andExpect(jsonPath("$.email", is(application.getEmail())))
                .andExpect(jsonPath("$.resume", is(application.getResume())))
                .andExpect(jsonPath("$.applicationStatus", is(application.getApplicationStatus().toString())));

    }

}
