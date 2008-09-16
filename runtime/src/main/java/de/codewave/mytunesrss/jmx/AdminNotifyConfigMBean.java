package de.codewave.mytunesrss.jmx;

public interface AdminNotifyConfigMBean {
    String getAdminEmail();

    void setAdminEmail(String adminEmail);

    boolean isNotifyOnPasswordChange();

    void setNotifyOnPasswordChange(boolean notify);

    boolean isNotifyOnEmailChange();

    void setNotifyOnEmailChange(boolean notify);

    boolean isNotifyOnQuotaExceeded();

    void setNotifyOnQuotaExceeded(boolean notify);

    boolean isNotifyOnLoginFailure();

    void setNotifyOnLoginFailure(boolean notify);

    boolean isNotifyOnWebUpload();

    void setNotifyOnWebUpload(boolean notify);

    boolean isNotifyOnTranscodingFailure();

    void setNotifyOnTranscodingFailure(boolean notify);

    boolean isNotifyOnInternalError();

    void setNotifyOnInternalError(boolean notify);

    boolean isNotifyOnDatabaseUpdate();

    void setNotifyOnDatabaseUpdate(boolean notify);
}
