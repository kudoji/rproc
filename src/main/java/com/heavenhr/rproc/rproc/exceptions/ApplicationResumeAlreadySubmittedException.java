/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.exceptions;

/**
 * thrown then application is already submitted to an offer
 */
public class ApplicationResumeAlreadySubmittedException extends RuntimeException{
    public ApplicationResumeAlreadySubmittedException(int id){
        super(String.format("resume file is already uploaded for the application #%d", id));
    }
}
