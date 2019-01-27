/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.heavenhr.rproc.rproc.exceptions.ApplicationAlreadySubmittedException;
import com.heavenhr.rproc.rproc.exceptions.ApplicationNotFoundException;
import com.heavenhr.rproc.rproc.exceptions.OfferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ExceptionHandler {
    /**
     * for other exceptions return "500 Internal Server Error"
     *
     * @param e
     * @param webRequest
     * @return
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, WebRequest webRequest){
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(ErrorResponse.buildFromErrorMessage(e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = OfferNotFoundException.class)
    public ResponseEntity<ErrorResponse> offerNotFoundHandler(OfferNotFoundException e){
        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(e.getMessage())
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> applicationNotFoundHandler(ApplicationNotFoundException e){
        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(e.getMessage())
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = ApplicationAlreadySubmittedException.class)
    public ResponseEntity<ErrorResponse> applicationAlreadySubmittedHandler(ApplicationAlreadySubmittedException e){
        return ResponseEntity.badRequest().body(
                ErrorResponse.buildFromErrorMessage(e.getMessage())
        );
    }

}
