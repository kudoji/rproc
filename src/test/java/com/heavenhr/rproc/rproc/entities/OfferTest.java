/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import com.heavenhr.rproc.rproc.testutils.AssertValidation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.time.LocalDate;

import static com.heavenhr.rproc.rproc.testutils.AssertValidation.getString;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OfferTest {
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private OfferRepository offerRepository;

    private static AssertValidation<Offer> assertValidation;

    @BeforeClass
    public static void initialization(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        assertValidation = new AssertValidation<>(validator);
    }

    @Test
    public void testJobTitleValidation(){
        Offer offer = new Offer();
        assertValidation.assertErrorValidation(offer, "jobTitle", "Job title cannot be empty");

        offer.setJobTitle("");
        assertValidation.assertErrorValidation(offer, "jobTitle", "Job title must be from 5 to 35 characters long");

        offer.setJobTitle(getString(4));
        assertValidation.assertErrorValidation(offer, "jobTitle", "Job title must be from 5 to 35 characters long");

        offer.setJobTitle(getString(5));
        assertValidation.assertNoErrorValidation(offer, "jobTitle");

        offer.setJobTitle(getString(35));
        assertValidation.assertNoErrorValidation(offer, "jobTitle");

        offer.setJobTitle(getString(36));
        assertValidation.assertErrorValidation(offer, "jobTitle", "Job title must be from 5 to 35 characters long");
    }

    @Test
    public void testStartDateValidation(){
        Offer offer = new Offer();
        assertValidation.assertErrorValidation(offer, "startDate", "Job start date cannot be null");

        offer.setStartDate(LocalDate.of(1990, 9, 1));
        assertValidation.assertErrorValidation(offer, "startDate", "Job start date cannot be in the past");

        offer.setStartDate(LocalDate.now().plusMonths(1));
        assertValidation.assertNoErrorValidation(offer, "startDate");
    }

    @Test
    public void testPersistence(){
        String jobTitle = getString(6);
        LocalDate startDate = LocalDate.now().plusMonths(1);
        Offer offer = new Offer();
        offer.setJobTitle(jobTitle);
        offer.setStartDate(startDate);

        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        Offer offerTest = testEntityManager.find(Offer.class, offerId);
        assertNotNull(offerTest);
        assertEquals(jobTitle, offerTest.getJobTitle());
        assertEquals(startDate, offerTest.getStartDate());
    }

    @Test
    public void testPrePersist(){
        Offer offer = new Offer();
        String jobTitle = getString(6);
        offer.setJobTitle(jobTitle);

        LocalDate startDate = LocalDate.now();
        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        Offer offerTest = testEntityManager.find(Offer.class, offerId);
        assertNotNull(offerTest);
        assertEquals(jobTitle, offerTest.getJobTitle());
        //  actually, offer might be persisted on the next day
        //  if it is the case assertion will fail
        assertEquals(startDate, offerTest.getStartDate());
    }

    @Test(expected = PersistenceException.class)
    public void testJobTitleUniqueness(){
        String jobTitle = getString(6);

        Offer offer = new Offer();
        offer.setJobTitle(jobTitle);

        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        offer = new Offer();
        offer.setJobTitle(jobTitle);

        testEntityManager.persistAndFlush(offer);
    }
}
