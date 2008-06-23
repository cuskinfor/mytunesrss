package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.settings.ServerInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.NotCompliantMBeanException;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class ServerConfig extends MyTunesRssMBean implements ServerConfigMBean {
    private static final Log LOG = LogFactory.getLog(ServerConfig.class);
    
    ServerConfig() throws NotCompliantMBeanException {
        super(ServerConfigMBean.class);
    }

    public int getPort() {
        return MyTunesRss.CONFIG.getPort();
    }

    public void setPort(int port) {
        MyTunesRss.CONFIG.setPort(port);
        onChange();
    }

    public boolean isShowOnLocalNetwork() {
        return MyTunesRss.CONFIG.isAvailableOnLocalNet();
    }

    public void setShowOnLocalNetwork(boolean showOnLocalNetwork) {
        MyTunesRss.CONFIG.setAvailableOnLocalNet(showOnLocalNetwork);
        onChange();
    }

    public String getServerName() {
        return MyTunesRss.CONFIG.getServerName();
    }

    public void setServerName(String name) {
        MyTunesRss.CONFIG.setServerName(name);
        onChange();
    }

    public boolean isRunning() {
        return MyTunesRss.WEBSERVER.isRunning();
    }

    public String startServer() {
        MyTunesRss.ERROR_QUEUE.clear();
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.startWebserver();
            onChange();
        }
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }

    public String stopServer() {
        MyTunesRss.ERROR_QUEUE.clear();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
            onChange();
        }
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }

    public boolean isAutostart() {
        return MyTunesRss.CONFIG.isAutoStartServer();
    }

    public void setAutostart(boolean autostart) {
        MyTunesRss.CONFIG.setAutoStartServer(autostart);
        MyTunesRssEventManager.getInstance().fireEvent(
                autostart ? MyTunesRssEvent.ENABLE_AUTO_START_SERVER : MyTunesRssEvent.DISABLE_AUTO_START_SERVER);
        onChange();
    }

    public boolean isCreateTempArchives() {
        return MyTunesRss.CONFIG.isLocalTempArchive();
    }

    public void setCreateTempArchives(boolean createTempArchives) {
        MyTunesRss.CONFIG.setLocalTempArchive(createTempArchives);
        onChange();
    }

    public String getExternalAddress() {
        return ServerInfo.getExternalAddress(Integer.toString(getPort()));
    }

    public String[] getLocalAddresses() {
        return ServerInfo.getLocalAddresses(Integer.toString(getPort()));
    }

    public int getAjpPort() {
        return MyTunesRss.CONFIG.getTomcatAjpPort();
    }

    public void setAjpPort(int port) {
        MyTunesRss.CONFIG.setTomcatAjpPort(port);
        onChange();
    }

    public String getSslKeystoreFile() {
        return MyTunesRss.CONFIG.getSslKeystoreFile();
    }

    public String getSslKeystoreKeyAlias() {
        return MyTunesRss.CONFIG.getSslKeystoreKeyAlias();
    }

    public String getSslKeystorePassphrase() {
        return MyTunesRss.CONFIG.getSslKeystorePass();
    }

    public int getSslPort() {
        return MyTunesRss.CONFIG.getSslPort();
    }

    public void setSslKeystoreFile(String file) {
        MyTunesRss.CONFIG.setSslKeystoreFile(file);
        onChange();
    }

    public void setSslKeystoreKeyAlias(String alias) {
        MyTunesRss.CONFIG.setSslKeystoreKeyAlias(alias);
        onChange();
    }

    public void setSslKeystorePassphrase(String passphrase) {
        MyTunesRss.CONFIG.setSslKeystorePass(passphrase);
        onChange();
    }

    public void setSslPort(int port) {
        MyTunesRss.CONFIG.setSslPort(port);
        onChange();
    }

    public String getTomcatProxyHost() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getTomcatProxyHost());
    }

    public int getTomcatProxyPort() {
        return MyTunesRss.CONFIG.getTomcatProxyPort();
    }

    public String getTomcatSslProxyHost() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getTomcatSslProxyHost());
    }

    public int getTomcatSslProxyPort() {
        return MyTunesRss.CONFIG.getTomcatSslProxyPort();
    }

    public void setTomcatProxyHost(String host) {
        MyTunesRss.CONFIG.setTomcatProxyHost(StringUtils.trimToNull(host));
    }

    public void setTomcatProxyPort(int port) {
        MyTunesRss.CONFIG.setTomcatProxyPort(port);
    }

    public void setTomcatSslProxyHost(String host) {
        MyTunesRss.CONFIG.setTomcatSslProxyHost(StringUtils.trimToNull(host));
    }

    public void setTomcatSslProxyPort(int port) {
        MyTunesRss.CONFIG.setTomcatSslProxyPort(port);
    }

    public String addAdditionalContext(String context, String docbase) {
        if (StringUtils.isEmpty(context) || StringUtils.isEmpty(docbase)) {
            return MyTunesRssUtils.getBundleString("jmx.illegalContextArguments");
        }
        context = StringUtils.trimToEmpty(context);
        if (!context.startsWith("/")) {
            context = "/" + context;
        }
        docbase = StringUtils.trimToEmpty(docbase);
        if (!new File(docbase).exists()) {
            return MyTunesRssUtils.getBundleString("jmx.contextDocBaseDoesNotExist");
        }
        for (Iterator<String> iter = MyTunesRss.CONFIG.getAdditionalContexts().iterator(); iter.hasNext();) {
            if (iter.next().startsWith(context + ":")) {

                return MyTunesRssUtils.getBundleString("jmx.contextAlreadyExists");
            }
        }
        MyTunesRss.CONFIG.getAdditionalContexts().add(context + ":" + docbase);
        return MyTunesRssUtils.getBundleString("ok");
    }

    public List<String> getAdditionalContexts() {
        List<String> display = new ArrayList<String>();
        for (String context : MyTunesRss.CONFIG.getAdditionalContexts()) {
            try {
                display.add(context.split(":", 2)[0] + " ---> " + context.split(":", 2)[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
            }
        }
        return display;
    }

    public String getWebappContext() {
        return MyTunesRss.CONFIG.getWebappContext();
    }

    public String removeAdditionalContext(String context) {
        context = StringUtils.trimToEmpty(context);
        if (!context.startsWith("/")) {
            context = "/" + context;
        }
        for (Iterator<String> iter = MyTunesRss.CONFIG.getAdditionalContexts().iterator(); iter.hasNext();) {
            if (iter.next().startsWith(context + ":")) {
                iter.remove();
                return MyTunesRssUtils.getBundleString("ok");
            }
        }
        return MyTunesRssUtils.getBundleString("jmx.noSuchContext");
    }

    public void setWebappContext(String context) {
        MyTunesRss.CONFIG.setWebappContext(context);
    }
}
