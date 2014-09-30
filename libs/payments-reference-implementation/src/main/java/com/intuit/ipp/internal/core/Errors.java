package com.intuit.ipp.internal.core;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.*;
public class Errors {
  
    private List<Error> errors = new ArrayList<Error>();
    /**
     * List of Error Object
     *
     * @return List of Error Object
     */
    public List<Error> getErrors() {
        return errors;
    }

    /**
     * List of Error Object
     *
     * @param errors List of Error Object
     */
    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

