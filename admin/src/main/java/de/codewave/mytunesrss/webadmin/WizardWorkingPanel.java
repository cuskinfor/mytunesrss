package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang.StringUtils;
import org.vaadin.henrik.refresher.Refresher;

public class WizardWorkingPanel extends Panel implements Refresher.RefreshListener {

    public void attach() {
        super.attach();
        setCaption(getApplication().getBundleString("wizardPanel.caption"));
        setContent(getApplication().getComponentFactory().createVerticalLayout(true, true));
        Refresher refresher = new Refresher();
        refresher.setRefreshInterval(2000); // refresh every 2 seconds
        refresher.addListener(this);
        addComponent(refresher);
        addComponent(new Label(getApplication().getBundleString("wizardWorkingPanel.message")));
    }

    public MyTunesRssWebAdmin getApplication() {
        return (MyTunesRssWebAdmin) super.getApplication();
    }

    public void refresh(Refresher refresher) {
        if (!MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning()) {
            VaadinUtils.getApplicationWindow(this).open(new ExternalResource("http://" + getApplication().getURL().getHost() + ":" + MyTunesRss.CONFIG.getPort() + StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext())));
        }
    }
}
