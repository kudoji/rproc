/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Slf4j
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"offer_id", "email"})
)
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull(message = "Candidate email cannot be empty")
    @Email(message = "Candidate email is invalid")
    @Column(nullable = false)
    private String email;

    //TODO make it as a file link
    @NotBlank(message = "Resume cannot be empty")
    private String resume;

    @NotNull(message = "Application status cannot be empty")
    @Enumerated
    private ApplicationStatus applicationStatus;

    @NotNull(message = "Offer must be selected")
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;

    /**
     *
     * @param offer if null removes previous link
     */
    public void setOffer(Offer offer){
        if (offer == null){
            if (this.offer != null){
                //  remove previous link
                log.info("remove application '{}' from the offer '{}'", this, this.offer);

                log.info("before {}", this.offer.getApplications().size());
                this.offer.getApplications().remove(this);
                log.info("after {}", this.offer.getApplications().size());
            }

            this.offer = null;
        }else{
            //  in this case nothing to do
            if (this.offer == offer) return;

            if (this.offer != null){
                //  remove link to this account from previous currency object
                this.offer.getApplications().remove(this);
            }

            this.offer = offer;
            this.offer.getApplications().add(this);
        }
    }

    @PreRemove
    private void preRemove(){
        //  destroy bi-directional link to the offer
        log.info("preRemove() is called for application '{}'", this);

        setOffer(null);
    }

    /**
     * avoid StackOverflowError due to lombok behaviour
     *
     * @return
     */
    @Override
    public String toString(){
        return "" + this.email + " #" + this.id;
    }
}