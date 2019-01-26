/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.messaging;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class Notification implements Serializable {
    private final String email;
    private final String body;
    /**
     * do not serialize this field
     */
    @Getter(AccessLevel.NONE)
    private final Application application;
    /**
     * do not serialize this field
     */
    @Getter(AccessLevel.NONE)
    private final Offer offer;

    public Notification(Application application){
        this.offer = application.getOffer();
        this.application = application;

        this.email = application.getEmail();
        this.body =
                "Dear applicant,\r\n" +
                "\r\n" +
                "your application #'" + application.getId() + "' to the position: '" + offer.getJobTitle() + "' " +
                "has changed the status to '" + application.getApplicationStatus() + "'";
    }

    @Override
    public String toString(){
        return String.format(
                "notification for the application: '%s' to <%s> body: '%s'",
                this.application,
                this.email,
                this.body);
    }
}
