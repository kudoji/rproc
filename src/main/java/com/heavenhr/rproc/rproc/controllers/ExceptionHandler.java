/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception e, WebRequest webRequest){
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                body(ErrorResponse.buildFromErrorMessage(e.getMessage()));
    }
}
