package de.codewave.mytunesrss;

import de.codewave.utils.*;
import de.codewave.utils.registration.*;
import de.codewave.utils.xml.*;
import org.apache.commons.jxpath.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.MyTunesRssRegistration
 */
public class MyTunesRssRegistration {
    private static final Log LOG = LogFactory.getLog(MyTunesRssRegistration.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private String myName;
    private long myExpiration;
    private boolean myRegistered;

    public void init() throws IOException {
        String path = ProgramUtils.getPreferencesDataPath("MyTunesRSS");
        URL publicKey = getClass().getResource("/MyTunesRSS.public");
        String registration = RegistrationUtils.getRegistrationData(new File(path + "/MyTunesRSS.key").toURL(), publicKey);
        if (registration != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Using registration data from preferences.");
            }
            handleRegistration(registration);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Using default registration data.");
            }
            handleRegistration(RegistrationUtils.getRegistrationData(getClass().getResource("/MyTunesRSS.key"), publicKey));
        }
    }

    private void handleRegistration(String registration) {
        if (StringUtils.isNotEmpty(registration)) {
            JXPathContext registrationContext = JXPathUtils.getContext(registration);
            myRegistered = JXPathUtils.getBooleanValue(registrationContext, "/registration/registered", false);
            myName = JXPathUtils.getStringValue(registrationContext, "/registration/name", "unregistered");
            String expirationDate = JXPathUtils.getStringValue(registrationContext, "/registration/expiration", null);
            if (expirationDate != null) {
                try {
                    myExpiration = DATE_FORMAT.parse(expirationDate).getTime();
                } catch (ParseException e) {
                    // intentionally left blank
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registration data:");
                LOG.debug("name=" + getName());
                LOG.debug("registered=" + isRegistered());
                LOG.debug("expiration=" + getExpiration(MyTunesRss.BUNDLE.getString("registrationDateFormat")));
            }
        }
    }

    public long getExpiration() {
        return myExpiration;
    }

    public String getExpiration(String dateFormat) {
        if (getExpiration() > 0) {
            return new SimpleDateFormat(dateFormat).format(new Date(getExpiration()));
        }
        return "";
    }

    public boolean isExpired() {
        return myExpiration > 0 && myExpiration <= System.currentTimeMillis();
    }

    public String getName() {
        return myName;
    }

    public boolean isRegistered() {
        return myRegistered;
    }
}