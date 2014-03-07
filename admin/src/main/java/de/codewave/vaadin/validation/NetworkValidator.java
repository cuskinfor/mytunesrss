package de.codewave.vaadin.validation;

import com.vaadin.data.validator.AbstractValidator;
import org.apache.commons.lang3.StringUtils;

public class NetworkValidator extends AbstractValidator {
    public NetworkValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(Object value) {
        String s = StringUtils.trimToNull(value.toString());
        if (s == null) {
            return true;
        }
        int i = s.indexOf('/');
        if (i == 0 || i == s.length() - 1) {
            return false;
        }
        String ip = i != -1 ? s.substring(0, i) : s;
        String bits = i != -1 ? s.substring(i + 1) : "";
        try {
            int bitsValue = Integer.parseInt(bits);
            if (bitsValue < 0 || bitsValue > 32) {
                return false;
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        String[] ipParts = StringUtils.split(ip, ".");
        if (ipParts.length != 4) {
            return false;
        }
        for (String ipPart : ipParts) {
            try {
                int ipPartValue = Integer.parseInt(ipPart);
                if (ipPartValue < 0 || ipPartValue > 255) {
                    return  false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return true;
    }
}
