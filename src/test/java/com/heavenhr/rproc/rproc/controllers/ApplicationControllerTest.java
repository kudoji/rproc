/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.ApplicationPartial;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.messaging.RabbitNotificationService;
import com.heavenhr.rproc.rproc.recourseassemblers.ApplicationResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @MockBean
    private RabbitNotificationService rabbitNotificationService;

    @Autowired
    private ObjectMapper objectMapper;

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
        application.setEmail("email1@email.com");
        application.setOffer(offer);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setEmail("email2@email.com");
        application.setOffer(offer);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);

        application = new Application();
        application.setId(2);
        application.setEmail("email2@email.com");
        application.setOffer(offer2);
        application.setApplicationStatus(ApplicationStatus.APPLIED);

        applications.add(application);
    }

    @Test
    public void allApplications_withUnauthorizedUser() throws Exception{
        mockMvc.perform(
                get("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @WithMockUser(username = "hr")
    @Test
    public void allApplications_withNoApplications() throws Exception{
        mockMvc.perform(
                get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links").exists());
    }

    @WithMockUser(username = "hr")
    @Test
    public void allApplications_withApplications() throws Exception{
        when(applicationRepository.findAll()).thenReturn(applications);
        applications.forEach(a ->
                when(applicationResourceAssembler.toResource(a)).thenReturn(new Resource<>(a)));

        mockMvc.perform(
                get("/applications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.applications", hasSize(applications.size())))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    public void allApplications_withUnauthorizedUserAndOfferId() throws Exception{
        mockMvc.perform(
                get("/applications?offerId=0")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @WithMockUser(username = "hr")
    @Test
    public void allApplications_withInvalidIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications?offerId=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @WithMockUser(username = "hr")
    @Test
    public void allApplications_withInvalidNotIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications?offerId=saw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @WithMockUser(username = "hr")
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
                .andExpect(jsonPath("$._embedded.applications", hasSize(offer.getApplications().size())))
                .andExpect(jsonPath("$._links").exists());
    }

    @WithMockUser(username = "hr")
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

    @WithMockUser(username = "hr")
    @Test
    public void getNumberOfApplicationsTotal_withNoApplications() throws Exception{
        mockMvc.perform(
                get("/applications/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(0)));
    }

    @WithMockUser(username = "hr")
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

    @WithMockUser(username = "hr")
    @Test
    public void getNumberOfApplicationsTotal_withInvalidIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications/total?offerId=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @WithMockUser(username = "hr")
    @Test
    public void getNumberOfApplicationsTotal_withInvalidNotIntOfferId() throws Exception{
        mockMvc.perform(
                get("/applications/total?offerId=e0w")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @WithMockUser(username = "hr")
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

    @WithMockUser(username = "hr")
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

    @WithMockUser(username = "hr")
    @Test
    public void getApplication_withInvalidIntAppId() throws Exception{
        mockMvc.perform(
                get("/applications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: application with #")));
    }

    @WithMockUser(username = "hr")
    @Test
    public void getApplication_withInvalidNotIntAppId() throws Exception{
        mockMvc.perform(
                get("/applications/ad1w")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "hr")
    @Test
    public void getApplication_withValidAppId() throws Exception{
        Application application = applications.get(0);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationResourceAssembler.toResource(application)).thenReturn(new Resource<>(application));

        mockMvc.perform(
                get("/applications/" + application.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(application.getId())))
                .andExpect(jsonPath("$.email", is(application.getEmail())))
                .andExpect(jsonPath("$.applicationStatus", is(application.getApplicationStatus().toString())));

    }

    @Test
    public void submitApplication_withUnauthorizedUser() throws Exception{
        mockMvc.perform(
                post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applications.get(0)))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @WithMockUser(username = "hr")
    @Test
    public void submitApplication_withInvalidApplication() throws Exception{
        ApplicationPartial applicationInvalid = new ApplicationPartial();

        mockMvc.perform(
                post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationInvalid))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", containsString("Validation failed due to")))
                .andExpect(jsonPath("$.errors").exists());

    }

    @WithMockUser(username = "hr")
    @Test
    public void submitApplication_withValidApplicationAndNoOfferExists() throws Exception{
        Application application = applications.get(0);
        when(offerRepository.findById(application.getOffer().getId())).thenReturn(Optional.empty());

        ApplicationPartial applicationInvalid = new ApplicationPartial(application);

        mockMvc.perform(
                post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationInvalid))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: offer with #")));

    }

    @WithMockUser(username = "hr")
    @Test
    public void submitApplication_withValidApplicationAndOffer() throws Exception{
        String createdLink = "link_to_created_application";
        Application application = applications.get(0);
        when(offerRepository.findById(application.getOffer().getId())).thenReturn(Optional.of(application.getOffer()));
        when(applicationRepository.save(ArgumentMatchers.any(Application.class))).thenReturn(application);

        when(applicationResourceAssembler.toResource(application))
            .thenReturn(new Resource<>(
                    application,
                    new Link(createdLink)
            ));

        ApplicationPartial applicationValid = new ApplicationPartial(application);

        mockMvc.perform(
                post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationValid))
        )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(createdLink)))
                .andExpect(jsonPath("$.email", is(application.getEmail())))
                .andExpect(jsonPath("$._links.self.href", is(createdLink)));
    }

    @Test
    public void patchApplication_withGetMethod() throws Exception{
        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.INVITED.toString());

        mockMvc.perform(
                get("/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login")));
    }

    @Test
    public void patchApplication_withInvalidIntAppId() throws Exception{
        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.INVITED.toString());

        mockMvc.perform(
                patch("/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: application with #")));
    }

    @Test
    public void patchApplication_withInvalidNotIntAppId() throws Exception{
        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.INVITED.toString());

        mockMvc.perform(
                patch("/applications/dsd1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchApplication_withValidAppIdAndNullStatus() throws Exception{
        Application application = applications.get(0);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        Map<String, String> patch = new HashMap<>();

        mockMvc.perform(
                patch("/applications/" + application.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage", containsString("Invalid application status")));
    }

    @Test
    public void patchApplication_withValidAppIdAndEmptyStatus() throws Exception{
        Application application = applications.get(0);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", "");

        mockMvc.perform(
                patch("/applications/" + application.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    public void patchApplication_withValidAppIdAndInvalidStatus() throws Exception{
        Application application = new Application();
        application.setApplicationStatus(ApplicationStatus.APPLIED);
        application.setId(1);
        assertEquals(ApplicationStatus.APPLIED, application.getApplicationStatus());

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.HIRED.toString());

        mockMvc.perform(
                patch("/applications/" + application.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.errorMessage", containsString("Application status is incorrect")));
    }

    @Test
    public void patchApplication_withValidAppIdAndStatus() throws Exception{
        Application application = applications.get(0);
        assertEquals(ApplicationStatus.APPLIED, application.getApplicationStatus());

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.INVITED.toString());

        mockMvc.perform(
                patch("/applications/" + application.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("updated")));
    }
}
