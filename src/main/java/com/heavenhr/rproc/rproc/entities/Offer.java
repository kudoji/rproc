/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.entities;


import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "offers")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotBlank(message = "Job title cannot be empty")
    @Size(min = 5, max = 35, message = "Job title must be from 5 to 35 characters long")
    @Column(unique = true, nullable = false)
    private String jobTitle;

    @NotNull(message = "Job start date cannot be null")
    @FutureOrPresent(message = "Job start date be in the past")
    @Column(nullable = false)
    private LocalDate startDate;

    @Transient
    private int numberOfApplications = 0;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.PERSIST)
    @Setter(AccessLevel.NONE)
    private List<Application> applications;

    @PrePersist
    private void prePersist(){
        startDate = LocalDate.now();
    }
}
