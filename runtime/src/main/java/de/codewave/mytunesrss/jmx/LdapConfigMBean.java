/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.jmx;

public interface LdapConfigMBean {
    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    String getAuthMethod();

    void setAuthMethod(String authMethod);

    String getAuthPrincipal();

    void setAuthPrincipal(String authPrincipal);

    String getSearchRoot();

    void setSearchRoot(String searchRoot);

    String getSearchExpression();

    void setSearchExpression(String searchExpression);

    String getMailAttributeName();

    void setMailAttributeName(String mailAttributeName);

    int getSearchTimeout();

    void setSearchTimeout(int searchTimeout);

    String getTemplateUser();

    void setTemplateUser(String templateUser);
}
