package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.PlaylistFileType;
import de.codewave.utils.io.ZipUtils;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FlashPlayerEditPanel extends MyTunesRssConfigPanel implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashPlayerEditPanel.class);

    private AddonsConfigPanel myAddonsConfigPanel;
    private FlashPlayerConfig myFlashPlayerConfig;
    private Form myForm;
    private SmartTextField myName;
    private SmartTextField myHtml;
    private Select myFileType;
    private SmartTextField myWidth;
    private SmartTextField myHeight;
    private Upload myUpload;
    private List<String> myIllegalNames;

    public FlashPlayerEditPanel(AddonsConfigPanel addonsConfigPanel, FlashPlayerConfig flashPlayerConfig) {
        myAddonsConfigPanel = addonsConfigPanel;
        myFlashPlayerConfig = flashPlayerConfig;
        myIllegalNames = new ArrayList<String>();
        for (FlashPlayerConfig config : MyTunesRss.CONFIG.getFlashPlayers()) {
            myIllegalNames.add(config.getName().toLowerCase(Locale.ENGLISH));
        }
        myIllegalNames.remove(flashPlayerConfig.getName().toLowerCase(Locale.ENGLISH));
    }

    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 3, true, true));

        myForm = getComponentFactory().createForm(null, true);
        myName = getComponentFactory().createTextField("flashPlayerEditPanel.name", new AbstractStringValidator(getBundleString("flashPlayerEditPanel.error.name")) {
            @Override
            protected boolean isValidString(String value) {
                return value != null && !myIllegalNames.contains(value.toLowerCase(Locale.ENGLISH));
            }
        });
        setRequired(myName);
        myForm.addField("name", myName);
        myHtml = getComponentFactory().createTextField("flashPlayerEditPanel.html");
        setRequired(myHtml);
        myHtml.setRows(10);
        myForm.addField("html", myHtml);
        myFileType = getComponentFactory().createSelect("flashPlayerEditPanel.filetype", Arrays.asList(PlaylistFileType.Xspf, PlaylistFileType.M3u)); // todo add json
        myForm.addField("filetype", myFileType);
        myWidth = getComponentFactory().createTextField("flashPlayerEditPanel.width", new MinMaxIntegerValidator(getBundleString("flashPlayerEditPanel.error.width", 1, 4096), 1, 4096));
        setRequired(myWidth);
        myForm.addField("width", myWidth);
        myHeight = getComponentFactory().createTextField("flashPlayerEditPanel.height", new MinMaxIntegerValidator(getBundleString("flashPlayerEditPanel.error.height", 1, 4096), 1, 4096));
        setRequired(myHeight);
        myForm.addField("height", myHeight);
        myUpload = new Upload(null, this);
        myUpload.setButtonCaption(getBundleString("flashPlayerEditPanel.upload"));
        myUpload.setImmediate(true);
        myUpload.addListener((Upload.SucceededListener) this);
        myUpload.addListener((Upload.FailedListener) this);

        addComponent(getComponentFactory().surroundWithPanel(myForm, FORM_PANEL_MARGIN_INFO, getBundleString("flashPlayerEditPanel.caption.form")));

        Panel panel = getComponentFactory().surroundWithPanel(myUpload, new Layout.MarginInfo(false, false, false, false), null);
        panel.addStyleName("light");
        addComponent(panel);

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myFlashPlayerConfig.setName(myName.getStringValue("Unknown player"));
        myFlashPlayerConfig.setHtml(myHtml.getStringValue("<!-- missing flash player html -->"));
        myFlashPlayerConfig.setPlaylistFileType((PlaylistFileType) myFileType.getValue());
        myFlashPlayerConfig.setWidth(myWidth.getIntegerValue(640));
        myFlashPlayerConfig.setHeight(myHeight.getIntegerValue(480));
        myAddonsConfigPanel.saveFlashPlayer(myFlashPlayerConfig);
    }

    @Override
    protected void initFromConfig() {
        myName.setValue(myFlashPlayerConfig.getName());
        myHtml.setValue(myFlashPlayerConfig.getHtml());
        myFileType.setValue(myFlashPlayerConfig.getPlaylistFileType());
        myWidth.setValue(myFlashPlayerConfig.getWidth());
        myHeight.setValue(myFlashPlayerConfig.getHeight());
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myForm)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else try {
            if (!myFlashPlayerConfig.getBaseDir().isDirectory()) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.missingFiles");
            } else {
                writeToConfig();
                closeWindow();
            }
        } catch (IOException e) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.missingFiles");
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        closeWindow();
        return false; // make sure the default operation is not used
    }

    private void closeWindow() {
        getWindow().getParent().removeWindow(getWindow());
    }

    public void uploadFailed(Upload.FailedEvent event) {
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.uploadFailed");
        FileUtils.deleteQuietly(new File(getUploadDir(), event.getFilename()));
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
            return new FileOutputStream(new File(getUploadDir(), filename));
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
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.extractFailed");
                }
            } else {
                FileUtils.copyFileToDirectory(uploadFile, targetDir);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not upload data.", e);
            }
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.uploadFailed");
        } finally {
            FileUtils.deleteQuietly(new File(getUploadDir(), event.getFilename()));
        }
    }
}
