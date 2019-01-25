/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.bytebuddy.matcher.ElementMatchers.isArray;
import static org.assertj.core.api.Assertions.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private ObjectMapper objectMapper;

    private Offer offer;

    private List<Offer> getOffers(){
        Offer offer1 = new Offer();
        offer1.setJobTitle("job title 1");

        Offer offer2 = new Offer();
        offer2.setJobTitle("job title 2");

        List<Offer> offers = Arrays.asList(
            offer1,
            offer2
        );

        return offers;
    }

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
    }

    @Test
    public void testAllOffers() throws Exception{
        when(offerRepository.findAll()).thenReturn(Arrays.asList(offer));

        mockMvc.perform(
                get("/offers/all")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].jobTitle", is(offer.getJobTitle())));
    }

    @Test
    public void testGetOfferByIdWithValidId() throws Exception{
        when(offerRepository.findById(1)).thenReturn(Optional.of(offer));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", containsString("Error: offer with #")));
    }

    @Test
    public void testSubmitOfferValid() throws Exception{
        when(offerRepository.save(offer)).thenReturn(offer);

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
}
