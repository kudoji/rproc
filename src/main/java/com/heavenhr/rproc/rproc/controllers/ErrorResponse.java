/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorResponse {
    private final String errorMessage;

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private final List<String> errors = new ArrayList<>();

    public void addError(String error){
        this.errors.add(error);
    }

    public static ErrorResponse buildFromErrorMessage(String errorMessage){
        return new ErrorResponse(errorMessage);
    }

    public static ErrorResponse buildFromErrorMessage(String errorMessage, Object... args){
        return new ErrorResponse(String.format(errorMessage, args));
    }

    public static ErrorResponse buildFromErrors(Errors errors){
        ErrorResponse errorResponse = new ErrorResponse(
                String.format("Validation failed due to %d errors found", errors.getErrorCount()));

        errors.getAllErrors().forEach(e -> errorResponse.addError(e.getDefaultMessage()));

        return errorResponse;
    }
}
