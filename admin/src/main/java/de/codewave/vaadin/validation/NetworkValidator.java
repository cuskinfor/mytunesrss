package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractValidator;
import de.codewave.mytunesrss.mediaserver.Network;
import org.apache.commons.lang3.StringUtils;

public class NetworkValidator extends AbstractValidator {
    public NetworkValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(Object value) {
        try {
            new Network(value.toString());
        } catch (IllegalArgumentException ignored) {
            return false;
        }
        return true;
    }
}
