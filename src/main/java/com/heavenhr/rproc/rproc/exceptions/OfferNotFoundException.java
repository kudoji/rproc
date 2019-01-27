/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.exceptions;

public class OfferNotFoundException extends RuntimeException{
    public OfferNotFoundException(int id){
        super(String.format("Error: offer with #%d not found", id));
    }
}
