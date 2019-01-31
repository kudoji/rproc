/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.repositories;

import com.heavenhr.rproc.rproc.entities.Application;
import com.heavenhr.rproc.rproc.entities.Offer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApplicationRepository extends CrudRepository<Application, Integer> {
    Iterable<Application> findAllByOffer(Offer offer);
}
