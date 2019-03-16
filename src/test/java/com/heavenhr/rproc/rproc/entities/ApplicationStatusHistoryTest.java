/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import com.heavenhr.rproc.rproc.storage.StorageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ApplicationStatusHistoryTest {
    @Autowired
    private TestEntityManager testEntityManager;

    @MockBean
    private StorageService storageService;

    private Offer offer;
    private Application application;
    private String email;

    private void createEntities(){
        offer = new Offer();
        offer.setJobTitle("job title");

        email = "email1@email.com";
        application = new Application();
        application.setOffer(offer);
        application.setEmail(email);
        application.setApplicationStatus(ApplicationStatus.APPLIED);
    }

    @Test
    public void testCascadePersistence(){
        createEntities();

        assertEquals(1, application.getApplicationStatusHistories().size());

        testEntityManager.persistAndFlush(offer);

        Integer offerId = testEntityManager.getId(offer, Integer.class);
        assertNotNull(offerId);
        assertTrue(offerId > 0);

        Integer applicationId = testEntityManager.getId(application, Integer.class);
        assertNotNull(applicationId);
        assertTrue(applicationId > 0);

        Application applicationTest = testEntityManager.find(Application.class, applicationId);
        Assert.assertEquals(email, applicationTest.getEmail());

        applicationTest.getApplicationStatusHistories().forEach(h -> System.out.println("history item# " + h));
        assertEquals(1, applicationTest.getApplicationStatusHistories().size());
    }
}
