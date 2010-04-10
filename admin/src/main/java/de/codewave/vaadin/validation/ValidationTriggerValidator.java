/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.validation;

import com.vaadin.data.Validatable;
import com.vaadin.data.validator.AbstractValidator;

public class ValidationTriggerValidator extends AbstractValidator {

    private Validatable myValidatable;

    public ValidationTriggerValidator(Validatable validatable) {
        super(null);
        myValidatable = validatable;
    }

    public boolean isValid(Object o) {
        myValidatable.validate();
        return true;
    }
}
