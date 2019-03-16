/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Analogue of Application class but with fewer properties
 * This is not an entity
 * Class is needed for OfferController for creating Application with less json data and validation capabilities
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Component
public class ApplicationPartial {
    @NotBlank(message = "Candidate email cannot be empty")
    @Email(message = "Candidate email is invalid")
    private String email;

    @Min(value = 1, message = "Offer id is invalid")
    private int offerId;

    /**
     * Creates instance from Application
     *
     * @param application
     */
    public ApplicationPartial(Application application){
        this.email = application.getEmail();
        this.offerId = application.getOffer().getId();
    }
}
