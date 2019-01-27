/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.exceptions;

/**
 * thrown then application is already submitted to an offer
 */
public class ApplicationAlreadySubmittedException extends RuntimeException{
    public ApplicationAlreadySubmittedException(){
        super("candidate is already submitted resume for the offer");
    }
}
