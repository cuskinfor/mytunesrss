package de.codewave.mytunesrss;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import de.codewave.utils.xml.JXPathUtils;
import de.codewave.utils.xml.DOMUtils;

/**
 * Configuration for LDAP server.
 */
public class LdapConfig {
    private String myHost;
    private int myPort;
    private LdapAuthMethod myAuthMethod;
    private String myAuthPrincipal;
    private String mySearchRoot;
    private String mySearchExpression;
    private String myMailAttributeName;
    private int mySearchTimeout;
    private String myTemplateUser;

    public LdapConfig(JXPathContext settings) {
        myHost = JXPathUtils.getStringValue(settings, "ldap/host", null);
        myPort = JXPathUtils.getIntValue(settings, "ldap/port", -1);
        myAuthMethod = LdapAuthMethod.valueOf(JXPathUtils.getStringValue(settings, "ldap/auth-method", LdapAuthMethod.SIMPLE.name()));
        myAuthPrincipal = JXPathUtils.getStringValue(settings, "ldap/auth-principal", null);
        mySearchRoot = JXPathUtils.getStringValue(settings, "ldap/search-root", null);
        mySearchExpression = JXPathUtils.getStringValue(settings, "ldap/search-expression", null);
        myMailAttributeName = JXPathUtils.getStringValue(settings, "ldap/search-mail-name", null);
        mySearchTimeout = JXPathUtils.getIntValue(settings, "ldap/search-timeout", 0);
        myTemplateUser = JXPathUtils.getStringValue(settings, "ldap/template-user", null);
    }

    public Element createSettingsElement(Document settings) {
        Element element = settings.createElement("ldap");
        element.appendChild(DOMUtils.createTextElement(settings, "host", myHost));
        element.appendChild(DOMUtils.createIntElement(settings, "port", myPort));
        element.appendChild(DOMUtils.createTextElement(settings, "auth-method", myAuthMethod.name()));
        element.appendChild(DOMUtils.createTextElement(settings, "auth-principal", myAuthPrincipal));
        element.appendChild(DOMUtils.createTextElement(settings, "search-root", mySearchRoot));
        element.appendChild(DOMUtils.createTextElement(settings, "search-expression", mySearchExpression));
        element.appendChild(DOMUtils.createTextElement(settings, "search-mail-name", myMailAttributeName));
        element.appendChild(DOMUtils.createIntElement(settings, "search-timeout", mySearchTimeout));
        element.appendChild(DOMUtils.createTextElement(settings, "template-user", myTemplateUser));
        return element;
    }

    public String getHost() {
        return myHost;
    }

    public void setHost(String host) {
        myHost = host;
    }

    public int getPort() {
        return myPort;
    }

    public void setPort(int port) {
        myPort = port;
    }

    public LdapAuthMethod getAuthMethod() {
        return myAuthMethod;
    }

    public void setAuthMethod(LdapAuthMethod authMethod) {
        myAuthMethod = authMethod;
    }

    public String getAuthPrincipal() {
        return myAuthPrincipal;
    }

    public void setAuthPrincipal(String authPrincipal) {
        myAuthPrincipal = authPrincipal;
    }

    public String getSearchRoot() {
        return mySearchRoot;
    }

    public void setSearchRoot(String searchRoot) {
        mySearchRoot = searchRoot;
    }

    public String getSearchExpression() {
        return mySearchExpression;
    }

    public void setSearchExpression(String searchExpression) {
        mySearchExpression = searchExpression;
    }

    public String getMailAttributeName() {
        return myMailAttributeName;
    }

    public void setMailAttributeName(String mailAttributeName) {
        myMailAttributeName = mailAttributeName;
    }

    public int getSearchTimeout() {
        return mySearchTimeout;
    }

    public void setSearchTimeout(int searchTimeout) {
        mySearchTimeout = searchTimeout;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(myHost) && myAuthMethod != null && StringUtils.isNotBlank(myAuthPrincipal) && myPort > 0 && myPort < 65536 && StringUtils.isNotBlank(myTemplateUser);
    }

    public boolean isFetchEmail() {
        return StringUtils.isNotBlank(mySearchExpression) && StringUtils.isNotBlank(myMailAttributeName);
    }

    public String getTemplateUser() {
        return myTemplateUser;
    }

    public void setTemplateUser(String templateUser) {
        myTemplateUser = templateUser;
    }
}
