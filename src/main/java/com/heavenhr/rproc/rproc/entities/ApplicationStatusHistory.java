/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;

import com.heavenhr.rproc.rproc.enums.ApplicationStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Slf4j
@Data
@Entity
@Table(name = "app_status_history")
public class ApplicationStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull(message = "Event date cannot be null")
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @NotNull(message = "Application status cannot be empty")
    @Enumerated
    private ApplicationStatus applicationStatus;

    @NotNull(message = "Application must be selected")
    @ManyToOne(fetch = FetchType.LAZY)
    private Application application;

    /**
     *
     * @param application if null removes previous link
     */
    public void setApplication(Application application){
        if (application == null){
            if (this.application != null){
                //  remove previous link
                log.info("remove application history item '{}' from the application '{}'", this, this.application);

                log.info("before {}", this.application.getApplicationStatusHistories().size());
                this.application.getApplicationStatusHistories().remove(this);
                log.info("after {}", this.application.getApplicationStatusHistories().size());
            }

            this.application = null;
        }else{
            //  in this case nothing to do
            if (this.application == application) return;

            if (this.application != null){
                //  remove link to this account from previous currency object
                this.application.getApplicationStatusHistories().remove(this);
            }

            this.application = application;
            this.application.getApplicationStatusHistories().add(this);
        }
    }

    @PrePersist
    private void prePersist(){
        log.info("prePersist() is called");
        if (dateTime == null){
            log.info("prePersist(): setting date");
            dateTime = LocalDateTime.now();
        }
    }

    @PreRemove
    private void preRemove(){
        //  destroy bi-directional link to the application
        log.info("preRemove() is called for application history item '{}'", this);

        setApplication(null);
    }

    /**
     * avoid StackOverflowError due to lombok behaviour
     *
     * @return
     */
    @Override
    public String toString(){
        return "" + this.dateTime + ", '" + this.application + "' #" + this.id + " [" + this.applicationStatus + "]";
    }

}
