/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.LdapAuthMethod;
import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;

public class LdapConfig extends MyTunesRssMBean implements LdapConfigMBean {
    public LdapConfig() throws NotCompliantMBeanException {
        super(LdapConfigMBean.class);
    }

    public String getHost() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getHost());
    }

    public void setHost(String host) {
        MyTunesRss.CONFIG.getLdapConfig().setHost(StringUtils.trimToNull(host));
        onChange();
    }

    public int getPort() {
        return MyTunesRss.CONFIG.getLdapConfig().getPort();
    }

    public void setPort(int port) {
        if (port >= 1 && port <= 65535) {
            MyTunesRss.CONFIG.getLdapConfig().setPort(port);
            onChange();
        }
    }

    public String getAuthMethod() {
        LdapAuthMethod ldapAuthMethod = MyTunesRss.CONFIG.getLdapConfig().getAuthMethod();
        return ldapAuthMethod != null ? ldapAuthMethod.name() : null;
    }

    public void setAuthMethod(String authMethod) {
        MyTunesRss.CONFIG.getLdapConfig().setAuthMethod(LdapAuthMethod.valueOf(StringUtils.trimToNull(StringUtils.upperCase(authMethod))));
        onChange();
    }

    public String getAuthPrincipal() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getAuthPrincipal());
    }

    public void setAuthPrincipal(String authPrincipal) {
        MyTunesRss.CONFIG.getLdapConfig().setAuthPrincipal(StringUtils.trimToNull(authPrincipal));
        onChange();
    }

    public String getSearchRoot() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getSearchRoot());
    }

    public void setSearchRoot(String searchRoot) {
        MyTunesRss.CONFIG.getLdapConfig().setSearchRoot(StringUtils.trimToNull(searchRoot));
        onChange();
    }

    public String getSearchExpression() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getSearchExpression());
    }

    public void setSearchExpression(String searchExpression) {
        MyTunesRss.CONFIG.getLdapConfig().setSearchExpression(StringUtils.trimToNull(searchExpression));
        onChange();
    }

    public String getMailAttributeName() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getMailAttributeName());
    }

    public void setMailAttributeName(String mailAttributeName) {
        MyTunesRss.CONFIG.getLdapConfig().setMailAttributeName(StringUtils.trimToNull(mailAttributeName));
        onChange();
    }

    public int getSearchTimeout() {
        return MyTunesRss.CONFIG.getLdapConfig().getSearchTimeout();
    }

    public void setSearchTimeout(int searchTimeout) {
        if (searchTimeout >= 0 && searchTimeout <= 10000) {
            MyTunesRss.CONFIG.getLdapConfig().setSearchTimeout(searchTimeout);
            onChange();
        }
    }

    public String getTemplateUser() {
        return StringUtils.defaultString(MyTunesRss.CONFIG.getLdapConfig().getTemplateUser());
    }

    public void setTemplateUser(String templateUser) {
        if (MyTunesRss.CONFIG.getUser(StringUtils.trimToNull(templateUser)) != null) {
            MyTunesRss.CONFIG.getLdapConfig().setTemplateUser(StringUtils.trimToNull(templateUser));
            onChange();
        }
    }
}
