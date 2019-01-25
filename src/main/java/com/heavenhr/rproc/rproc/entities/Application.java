/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heavenhr.rproc.rproc.controllers.ApplicationStatusHistory;
import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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

//    @NotNull(message = "Application status cannot be empty")
    @Enumerated
    private ApplicationStatus applicationStatus;

    @NotNull(message = "Offer must be selected")
    @ManyToOne(fetch = FetchType.LAZY)
    private Offer offer;

    @JsonIgnore
    @OneToMany(mappedBy = "application", cascade = CascadeType.PERSIST)
    @Setter(AccessLevel.NONE)
    private List<ApplicationStatusHistory> applicationStatusHistories = new ArrayList<>();

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

    /**
     * Possible flows are:
     *
     *  - null -> APPLIED;
     *  - APPLIED -> INVITED;
     *  - APPLIED -> REJECTED;
     *  - INVITED -> REJECTED;
     *  - INVITED -> HIRED
     *
     * @param applicationStatus
     * @throws IllegalArgumentException if status is incorrect based on flow
     */
    public void setApplicationStatus(ApplicationStatus applicationStatus){
        if (this.applicationStatus == applicationStatus){
            String error = String.format("status is already set to '%s'", applicationStatus.toString());
            log.warn(error);
            throw new IllegalArgumentException(error);
        };

        if (this.applicationStatus == null){
            if (applicationStatus != ApplicationStatus.APPLIED){
                log.warn("only '{}' is acceptable at this point", ApplicationStatus.APPLIED);
                throw new IllegalArgumentException("Application status is incorrect");
            }
        }else if (this.applicationStatus == ApplicationStatus.APPLIED) {
            if ((applicationStatus != ApplicationStatus.INVITED) &&
                    (applicationStatus != ApplicationStatus.REJECTED)) {
                log.warn(
                        "only '{}' or '{}' are acceptable at this point",
                        ApplicationStatus.APPLIED,
                        ApplicationStatus.REJECTED);
                throw new IllegalArgumentException("Application status is incorrect");
            }
        }else if (this.applicationStatus == ApplicationStatus.INVITED){
            if ((applicationStatus != ApplicationStatus.HIRED) &&
                    (applicationStatus != ApplicationStatus.REJECTED)) {
                log.warn(
                        "only '{}' or '{}' are acceptable at this point",
                        ApplicationStatus.HIRED,
                        ApplicationStatus.REJECTED);
                throw new IllegalArgumentException("Application status is incorrect");
            }
        }else{
            throw new IllegalArgumentException("Application status is incorrect");
        }

        log.info("application status changed from '{}' to '{}'", this.applicationStatus, applicationStatus);
        this.applicationStatus = applicationStatus;

        log.info("history items for application status: {}", this.getApplicationStatusHistories().size());
        //  status changed, thus, history needs to be created
        ApplicationStatusHistory applicationStatusHistory = new ApplicationStatusHistory();
        applicationStatusHistory.setApplicationStatus(this.applicationStatus);
        applicationStatusHistory.setApplication(this);

        log.info("history items for application status: {}", this.getApplicationStatusHistories().size());
        this.getApplicationStatusHistories().forEach(h -> log.info("history item '{}'", h));
    }

    @PreRemove
    private void preRemove(){
        //  destroy bi-directional link to the offer
        log.info("preRemove() is called for application '{}'", this);

        setOffer(null);
    }

    @PrePersist
    private void prePersist(){
        //  status is not set, thus, it needs do be set to applied by default
        if (this.applicationStatus == null){
            setApplicationStatus(ApplicationStatus.APPLIED);
        }
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

    @Override
    public int hashCode(){
        return this.id;
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Application)) return false;

        Application application = (Application)obj;

        return (this.id > 0) & (this.id == application.id);
    }
}
