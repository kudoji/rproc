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
import com.heavenhr.rproc.rproc.recourseassemblers.OfferResourceAssembler;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.junit.Before;
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
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(OfferController.class)
public class OfferControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferRepository offerRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private RabbitNotificationService rabbitNotificationService;

    @MockBean
    private OfferResourceAssembler offerResourceAssembler;

    @MockBean
    private ApplicationResourceAssembler applicationResourceAssembler;

    @Autowired
    private ObjectMapper objectMapper;

    private Offer offer, offer2;
    private Application application, application2;

    @Before
    public void beforeTest(){
        offer = new Offer();
        offer.setId(1);
        //  make it in the future
        offer.setStartDate(LocalDate.of(
                2019,
                LocalDate.now().getMonth().getValue() + 1,
                23));
        offer.setJobTitle("job title");

        application = new Application();
        application.setId(1);
        application.setOffer(offer);
        application.setApplicationStatus(ApplicationStatus.APPLIED);
        application.setEmail("email@email.com");
        application.setResume("resume");

        offer2 = new Offer();
        offer2.setId(2);

        application2 = new Application();
        application2.setId(2);
        application2.setOffer(offer2);
    }

    @Test
    public void allOffers_withNoOffers() throws Exception{
        mockMvc.perform(
                get("/offers")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    public void allOffers_withSomeOffers() throws Exception{
        when(offerRepository.findAll()).thenReturn(Arrays.asList(offer));
        when(offerResourceAssembler.toResource(offer)).thenReturn(new Resource<>(offer));

        mockMvc.perform(
                get("/offers")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.offerList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.offerList[0].jobTitle", is(offer.getJobTitle())));
    }

    @Test
    public void testGetNumberOfApplicationsTotal() throws Exception{
        when(applicationRepository.findAll()).thenReturn(Arrays.asList(application, application2));

        mockMvc.perform(
                get("/offers/apps_total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apps_total", is(2)));
    }

    @Test
    public void testGetNumberOfApplicationsPerOffer() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(Arrays.asList(application, application2));

        mockMvc.perform(
                get("/offers/1/apps_total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apps_total", is(2)));
    }

    @Test
    public void testGetNumberOfApplicationsPerOfferWithNoApplications() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(Arrays.asList());

        mockMvc.perform(
                get("/offers/1/apps_total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apps_total", is(0)));
    }

    @Test
    public void testGetNumberOfApplicationsPerOfferInvalidOfferId() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/offers/1/apps_total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: offer with #")));
    }

    @Test
    public void testGetOfferByIdWithValidId() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(offerResourceAssembler.toResource(offer)).thenReturn(new Resource<>(offer));

        mockMvc.perform(
                get("/offers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(offer.getId())))
                .andExpect(jsonPath("$.jobTitle", is(offer.getJobTitle())))
                .andExpect(jsonPath("$.startDate", is(offer.getStartDate().toString())));
    }

    @Test
    public void testGetOfferByIdWithInvalidId() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/offers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: offer with #")));
    }

    @Test
    public void testGetApplicationForOfferValid() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.findByIdAndOffer(1, offer)).thenReturn(Optional.of(application));
        when(applicationResourceAssembler.toResource(application)).thenReturn(new Resource<>(application));

        mockMvc.perform(
                get("/offers/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(application.getId())))
                .andExpect(jsonPath("$.email", is(application.getEmail())))
                .andExpect(jsonPath("$.resume", is(application.getResume())))
                .andExpect(jsonPath("$.applicationStatus", is(application.getApplicationStatus().toString())));

    }

    @Test
    public void testGetApplicationForOfferInvalidApplication() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/offers/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: application with #")));

    }

    @Test
    public void testGetApplicationForOfferValidApplicationFromOtherOffer() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(offerRepository.findById(2)).thenReturn(Optional.of(offer2));

        when(applicationRepository.findById(1)).thenReturn(Optional.of(application));
        when(applicationRepository.findById(2)).thenReturn(Optional.of(application2));

        when(applicationRepository.findByIdAndOffer(2, offer)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/offers/1/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: application with #")));

    }

    @Test
    public void testAllApplicationsPerOffersValid() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.findAllByOffer(offer)).thenReturn(Arrays.asList(application));
        when(applicationResourceAssembler.toResource(application)).thenReturn(new Resource<>(application));

        mockMvc.perform(
                get("/offers/1/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.applicationList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.applicationList[0].email", is(application.getEmail())));
    }

    @Test
    public void testAllApplicationsPerOffersInvalidOffer() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(
                get("/offers/1/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: offer with #")));
    }

    @Test
    public void testSubmitOfferValid() throws Exception{
        when(offerRepository.save(offer)).thenReturn(offer);
        when(offerResourceAssembler.toResource(offer)).thenReturn(new Resource<>(offer));

        mockMvc.perform(
                post("/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offer))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(offer.getId())))
                .andExpect(jsonPath("$.jobTitle", is(offer.getJobTitle())))
                .andExpect(jsonPath("$.startDate", is(offer.getStartDate().toString())));
    }

    @Test
    public void testSubmitOfferInvalid() throws Exception{
        Offer offerInvalid = new Offer();
        when(offerRepository.save(offerInvalid)).thenReturn(offerInvalid);

        mockMvc.perform(
                post("/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerInvalid))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", containsString("Validation failed due to")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testSubmitApplicationValid() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));
        when(applicationRepository.save(application)).thenReturn(application);

        ApplicationPartial applicationPartial = new ApplicationPartial(application);

        mockMvc.perform(
                post("/offers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationPartial))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(application.getEmail())))
                .andExpect(jsonPath("$.resume", is(application.getResume())));
    }

    @Test
    public void testSubmitApplicationInvalid() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));

        ApplicationPartial applicationInvalid = new ApplicationPartial();

        mockMvc.perform(
                post("/offers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationInvalid))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", containsString("Validation failed due to")))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    public void testPatchApplicationValid() throws Exception{
        when(applicationRepository.findById(1)).thenReturn(Optional.of(application));

        Map<String, String> patch = new HashMap<>();
        patch.put("applicationStatus", ApplicationStatus.INVITED.toString());

        mockMvc.perform(
                patch("/offers/app/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("updated")));
    }
}
