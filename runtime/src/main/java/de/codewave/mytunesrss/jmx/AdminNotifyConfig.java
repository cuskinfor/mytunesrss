package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;

import javax.management.NotCompliantMBeanException;

import org.apache.commons.lang.StringUtils;

public class AdminNotifyConfig extends MyTunesRssMBean implements AdminNotifyConfigMBean {
    public AdminNotifyConfig() throws NotCompliantMBeanException {
        super(AdminNotifyConfigMBean.class);
    }

    public String getAdminEmail() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getAdminEmail());
    }

    public void setAdminEmail(String adminEmail) {
        MyTunesRss.CONFIG.setAdminEmail(StringUtils.trimToNull(adminEmail));
        onChange();
    }

    public boolean isNotifyOnDatabaseUpdate() {
        return MyTunesRss.CONFIG.isNotifyOnDatabaseUpdate();
    }

    public boolean isNotifyOnEmailChange() {
        return MyTunesRss.CONFIG.isNotifyOnEmailChange();
    }

    public boolean isNotifyOnInternalError() {
        return MyTunesRss.CONFIG.isNotifyOnInternalError();
    }

    public boolean isNotifyOnLoginFailure() {
        return MyTunesRss.CONFIG.isNotifyOnLoginFailure();
    }

    public boolean isNotifyOnPasswordChange() {
        return MyTunesRss.CONFIG.isNotifyOnPasswordChange();
    }

    public boolean isNotifyOnQuotaExceeded() {
        return MyTunesRss.CONFIG.isNotifyOnQuotaExceeded();
    }

    public boolean isNotifyOnTranscodingFailure() {
        return MyTunesRss.CONFIG.isNotifyOnTranscodingFailure();
    }

    public boolean isNotifyOnWebUpload() {
        return MyTunesRss.CONFIG.isNotifyOnWebUpload();
    }

    public void setNotifyOnDatabaseUpdate(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnDatabaseUpdate(notify);
        onChange();
    }

    public void setNotifyOnEmailChange(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnEmailChange(notify);
        onChange();
    }

    public void setNotifyOnInternalError(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnInternalError(notify);
        onChange();
    }

    public void setNotifyOnLoginFailure(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnLoginFailure(notify);
        onChange();
    }

    public void setNotifyOnPasswordChange(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnPasswordChange(notify);
        onChange();
    }

    public void setNotifyOnQuotaExceeded(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnQuotaExceeded(notify);
        onChange();
    }

    public void setNotifyOnTranscodingFailure(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnTranscodingFailure(notify);
        onChange();
    }

    public void setNotifyOnWebUpload(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnWebUpload(notify);
        onChange();
    }

    public boolean isNotifyOnMissingFile() {
        return MyTunesRss.CONFIG.isNotifyOnMissingFile();
    }

    public void setNotifyOnMissingFile(boolean notify) {
        MyTunesRss.CONFIG.setNotifyOnMissingFile(notify);
        onChange();
    }
}