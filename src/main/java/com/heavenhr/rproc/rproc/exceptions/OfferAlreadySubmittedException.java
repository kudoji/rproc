/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.exceptions;

/**
 * thrown then application is already submitted to an offer
 */
public class OfferAlreadySubmittedException extends RuntimeException{
    public OfferAlreadySubmittedException(){
        super("offer with this title is already existed");
    }
}
