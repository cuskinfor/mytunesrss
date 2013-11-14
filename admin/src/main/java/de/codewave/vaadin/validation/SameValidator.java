/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.AbstractField;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

public class SameValidator extends AbstractValidator {

    private AbstractField myOtherField;

    public SameValidator(AbstractField otherField, String errorMessage) {
        super(errorMessage);
        myOtherField = otherField;
    }

    public boolean isValid(Object o) {
        Object otherValue = myOtherField.getValue();
        if (otherValue == o) {
            return true;
        }
        if (o != null) {
            if (o.equals(otherValue)) {
                return true;
            }
            if (o instanceof byte[] && otherValue instanceof byte[]) {
                return Arrays.equals((byte[])o, (byte[])otherValue);
            }
        } 
        return false;
    }
}
