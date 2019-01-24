/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.repositories.ApplicationRepository;
import com.heavenhr.rproc.rproc.testutils.AssertValidation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ApplicationTest {
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ApplicationRepository applicationRepository;

    private static AssertValidation<Application> assertValidation;
    private Offer offer;
    private Application application1, application2;
    private String email1, email2;

    private void createEntities(){
        offer = new Offer();
        offer.setJobTitle("job title");

        email1 = "email1@email.com";
        application1 = new Application();
        application1.setOffer(offer);
        application1.setEmail(email1);
        application1.setApplicationStatus(ApplicationStatus.APPLIED);
        application1.setResume("resume1");

        email2 = "email2@email.com";
        application2 = new Application();
        application2.setOffer(offer);
        application2.setEmail(email2);
        application2.setApplicationStatus(ApplicationStatus.APPLIED);
        application2.setResume("resume2");
    }

    @BeforeClass
    public static void initialization(){
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        assertValidation = new AssertValidation<>(validator);
    }

    @Test
    public void testEmail(){
        Application application = new Application();
        assertValidation.assertErrorValidation(application, "email", "Candidate email cannot be empty");

        application.setEmail("email");
        assertValidation.assertErrorValidation(application, "email", "Candidate email is invalid");

        application.setEmail("email@email.com");
        assertValidation.assertNoErrorValidation(application, "email");
    }

    @Test
    public void testApplicationStatus(){
        Application application = new Application();
        assertValidation.assertErrorValidation(application, "applicationStatus", "Application status cannot be empty");

        application.setApplicationStatus(ApplicationStatus.APPLIED);
        assertValidation.assertNoErrorValidation(application, "applicationStatus");
    }

    @Test
    public void testOffer(){
        Application application = new Application();
        assertValidation.assertErrorValidation(application, "offer", "Offer must be selected");

        application.setOffer(null);
        assertValidation.assertErrorValidation(application, "offer", "Offer must be selected");

        application.setOffer(new Offer());
        assertValidation.assertNoErrorValidation(application, "offer");
    }

    @Test
    public void testSetOffer(){
        Offer offer = new Offer();
        assertEquals(0, offer.getApplications().size());

        Application application1 = new Application();
        application1.setOffer(offer);
        assertEquals(1, offer.getApplications().size());

        Application application2 = new Application();
        application2.setOffer(offer);
        assertEquals(2, offer.getApplications().size());
        assertTrue(offer.getApplications().contains(application1));
        assertTrue(offer.getApplications().contains(application2));
        assertEquals(offer, application1.getOffer());
        assertEquals(offer, application2.getOffer());

        application1.setOffer(null);
        assertEquals(1, offer.getApplications().size());
        assertTrue(!offer.getApplications().contains(application1));
        assertTrue(offer.getApplications().contains(application2));
        assertEquals(null, application1.getOffer());
        assertEquals(offer, application2.getOffer());

        application2.setOffer(null);
        assertEquals(0, offer.getApplications().size());
        assertEquals(null, application1.getOffer());
        assertEquals(null, application2.getOffer());
    }

    @Test
    public void testCascadePersistence(){
        createEntities();

        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        Integer applicationId1 = testEntityManager.getId(application1, Integer.class);
        assertNotNull(applicationId1);
        assertTrue(applicationId1 > 0);

        Application applicationTest = testEntityManager.find(Application.class, applicationId1);
        assertEquals(email1, applicationTest.getEmail());

        Integer applicationId2 = testEntityManager.getId(application2, Integer.class);
        assertNotNull(applicationId2);
        assertTrue(applicationId2 > 0);

        applicationTest = testEntityManager.find(Application.class, applicationId2);
        assertEquals(email2, applicationTest.getEmail());
    }

    @Test
    public void testPreRemove(){
        createEntities();
        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        Integer applicationId1 = testEntityManager.getId(application1, Integer.class);
        assertNotNull(applicationId1);
        assertTrue(applicationId1 > 0);

        Integer applicationId2 = testEntityManager.getId(application2, Integer.class);
        assertNotNull(applicationId2);
        assertTrue(applicationId2 > 0);

        assertEquals(2, offer.getApplications().size());

        testEntityManager.remove(application1);

        assertEquals(null, application1.getOffer());
        assertEquals(offer, application2.getOffer());
        assertEquals(1, offer.getApplications().size());
        assertTrue(!offer.getApplications().contains(application1));
        assertTrue(offer.getApplications().contains(application2));
    }
}
