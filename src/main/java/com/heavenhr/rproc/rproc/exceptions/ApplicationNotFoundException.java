/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.exceptions;

public class ApplicationNotFoundException extends RuntimeException{
    public ApplicationNotFoundException(int id){
        super(String.format("Error: application with #%d not found", id));
    }
}
