/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.converters;

import com.heavenhr.rproc.rproc.entities.Offer;
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import com.heavenhr.rproc.rproc.repositories.OfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class OfferByIdConverter implements Converter<String, Offer> {
    private final OfferRepository offerRepository;

    @Autowired
    public OfferByIdConverter(OfferRepository offerRepository){
        this.offerRepository = offerRepository;
    }

    @Override
    public Offer convert(String id){
        if (id == null) return null;

        int offerId;
        try{
            offerId = Integer.parseInt(id);
        }catch (NumberFormatException e){
            throw new IllegalArgumentException(String.format("provided offer id parameter is invalid: %s", id));
        }

        if (offerId <= 0) throw new OfferNotFoundException(offerId);

        Offer offer = offerRepository.findById(offerId).orElseThrow(() -> new OfferNotFoundException(offerId));

        log.info("Converting id#{}->Offer '{}'", offerId, offer);

        return offer;
    }
}
