package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Form;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.io.ZipUtils;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FlashPlayerEditPanel extends MyTunesRssConfigPanel implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {

    private static final String PREFIX = "upload_flashplayer_";

    private AddonsConfigPanel myAddonsConfigPanel;
    private FlashPlayerConfig myFlashPlayerConfig;
    private Form myForm;
    private SmartTextField myName;
    private SmartTextField myHtml;
    private Upload myUpload;

    public FlashPlayerEditPanel(AddonsConfigPanel addonsConfigPanel, FlashPlayerConfig flashPlayerConfig) {
        myAddonsConfigPanel = addonsConfigPanel;
        myFlashPlayerConfig = flashPlayerConfig;
    }

    public void attach() {
        init(null, getComponentFactory().createGridLayout(1, 3, true, true));

        myForm = getComponentFactory().createForm(null, true);
        myName = getComponentFactory().createTextField("flashPlayerEditPanel.name");
        setRequired(myName);
        myForm.addField("name", myName);
        myHtml = getComponentFactory().createTextField("flashPlayerEditPanel.html");
        setRequired(myHtml);
        myHtml.setRows(10);
        myForm.addField("html", myHtml);
        myUpload = new Upload(null, this);
        myUpload.setButtonCaption(getBundleString("flashPlayerEditPanel.upload"));
        myUpload.setImmediate(true);
        myUpload.addListener((Upload.SucceededListener) this);
        myUpload.addListener((Upload.FailedListener) this);

        addComponent(getComponentFactory().surroundWithPanel(myForm, FORM_PANEL_MARGIN_INFO, getBundleString("flashPlayerEditPanel.caption.form")));

        Panel panel = getComponentFactory().surroundWithPanel(myUpload, new Layout.MarginInfo(false, false, false, false), null);
        panel.setStyleName("light");
        addComponent(panel);

        attach(0, 2, 0, 2);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myFlashPlayerConfig.setName(myName.getStringValue("Unknown player"));
        myFlashPlayerConfig.setHtml(myHtml.getStringValue("<!-- missing flash player html -->"));
        myAddonsConfigPanel.addOrUpdatePlayer(myFlashPlayerConfig);
    }

    @Override
    protected void initFromConfig() {
        myName.setValue(myFlashPlayerConfig.getName());
        myHtml.setValue(myFlashPlayerConfig.getHtml());
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myForm)) {
            getApplication().showError("error.formInvalid");
        } else try {
            if (!myFlashPlayerConfig.getBaseDir().isDirectory()) {
                getApplication().showError("flashPlayerEditPanel.error.missingFiles");
            } else {
                writeToConfig();
                closeWindow();
            }
        } catch (IOException e) {
            getApplication().showError("flashPlayerEditPanel.error.missingFiles");
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        closeWindow();
        return false; // make sure the default operation is not used
    }

    private void closeWindow() {
        getWindow().getParent().getWindow().removeWindow(getWindow());
    }

    public void uploadFailed(Upload.FailedEvent event) {
        getApplication().showError("flashPlayerEditPanel.error.uploadFailed");
        FileUtils.deleteQuietly(new File(getUploadDir(), PREFIX + event.getFilename()));
    }

    private String getUploadDir() {
        try {
            return MyTunesRssUtils.getCacheDataPath() + "/" + MyTunesRss.CACHEDIR_TEMP;
        } catch (IOException e) {
            throw new RuntimeException("Could not get cache path.");
        }
    }

    public OutputStream receiveUpload(String filename, String MIMEType) {
        try {
            return new FileOutputStream(new File(getUploadDir(), PREFIX + filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            File uploadFile = new File(getUploadDir(), event.getFilename());
            File targetDir = myFlashPlayerConfig.getBaseDir();
            targetDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/flashplayer", myFlashPlayerConfig.getId());
            targetDir.mkdirs();
            if (event.getFilename().toLowerCase().endsWith(".zip")) {
                if (!ZipUtils.unzip(uploadFile, targetDir)) {
                    getApplication().showError("flashPlayerEditPanel.error.extractFailed");
                }
            } else {
                FileUtils.copyFileToDirectory(uploadFile, targetDir);
            }
        } catch (IOException e) {
            getApplication().showError("flashPlayerEditPanel.error.uploadFailed");
        } finally {
            FileUtils.deleteQuietly(new File(getUploadDir(), PREFIX + event.getFilename()));
        }
    }
}
