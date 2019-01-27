/**
 * @author kudoji
 */
package com.heavenhr.rproc.rproc.testutils;

import lombok.Data;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@Data
public class AssertValidation<T> {
    private final Validator validator;

    public void assertErrorValidation(T t, String property, String errorMessage){
        Set<ConstraintViolation<T>> constraintViolations =
                validator.validateProperty(t, property);

//        constraintViolations.forEach(System.out::print);
        assertEquals(1, constraintViolations.size());
        assertEquals(errorMessage, constraintViolations.iterator().next().getMessage());
    }

    public void assertNoErrorValidation(T t, String property){
        Set<ConstraintViolation<T>> constraintViolations =
                validator.validateProperty(t, property);

        assertEquals(0, constraintViolations.size());
    }

    /**
     * Returns string with particular lenght
     * @param lenght
     * @return
     */
    public static String getString(int lenght){
        return Stream.generate(() -> 1).
                limit(lenght).
                map((integer) -> integer.toString()).
                collect(Collectors.joining(""));
    }

}
