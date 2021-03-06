package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.FlashPlayerConfig;
import de.codewave.mytunesrss.config.PlaylistFileType;
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
import java.util.concurrent.TimeUnit;

public class FlashPlayerEditPanel extends MyTunesRssConfigPanel implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver, Upload.StartedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlashPlayerEditPanel.class);
    private List<TimeUnitWrapper> myTimeUnitWrappers;

    public static class TimeUnitWrapper {
        private TimeUnit myTimeUnit;
        private String myText;

        public TimeUnitWrapper(TimeUnit timeUnit, String text) {
            myTimeUnit = timeUnit;
            myText = text;
        }

        public TimeUnit getTimeUnit() {
            return myTimeUnit;
        }

        @Override
        public String toString() {
            return myText;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof TimeUnitWrapper && myTimeUnit.equals(((TimeUnitWrapper) o).getTimeUnit());
        }

        @Override
        public int hashCode() {
            return myTimeUnit.hashCode();
        }
    }

    private AddonsConfigPanel myAddonsConfigPanel;
    private FlashPlayerConfig myFlashPlayerConfig;
    private Form myForm;
    private SmartTextField myName;
    private Select myFileType;
    private Select myTimeUnit;
    private SmartTextField myWidth;
    private SmartTextField myHeight;
    private SmartTextField myImageSize;
    private Upload myUpload;
    private List<String> myIllegalNames;

    public FlashPlayerEditPanel(AddonsConfigPanel addonsConfigPanel, FlashPlayerConfig flashPlayerConfig) {
        myAddonsConfigPanel = addonsConfigPanel;
        myFlashPlayerConfig = flashPlayerConfig;
        myIllegalNames = new ArrayList<>();
        for (FlashPlayerConfig config : MyTunesRss.CONFIG.getFlashPlayers()) {
            myIllegalNames.add(config.getName().toLowerCase(Locale.ENGLISH));
        }
        myIllegalNames.remove(flashPlayerConfig.getName().toLowerCase(Locale.ENGLISH));
    }

    @Override
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
        myFileType = getComponentFactory().createSelect("flashPlayerEditPanel.filetype", Arrays.asList(PlaylistFileType.Xspf, PlaylistFileType.M3u, PlaylistFileType.Json, PlaylistFileType.JwMediaRss));
        myForm.addField("filetype", myFileType);
        myTimeUnitWrappers = Arrays.asList(new TimeUnitWrapper(TimeUnit.SECONDS, getBundleString("flashPlayerEditPanel.timeunit.seconds")), new TimeUnitWrapper(TimeUnit.MILLISECONDS, getBundleString("flashPlayerEditPanel.timeunit.milliseconds")));
        myTimeUnit = getComponentFactory().createSelect("flashPlayerEditPanel.timeunit", myTimeUnitWrappers);
        myForm.addField("timeunit", myTimeUnit);
        myWidth = getComponentFactory().createTextField("flashPlayerEditPanel.width", new MinMaxIntegerValidator(getBundleString("flashPlayerEditPanel.error.width", 1, 4096), 1, 4096));
        setRequired(myWidth);
        myForm.addField("width", myWidth);
        myHeight = getComponentFactory().createTextField("flashPlayerEditPanel.height", new MinMaxIntegerValidator(getBundleString("flashPlayerEditPanel.error.height", 1, 4096), 1, 4096));
        setRequired(myHeight);
        myForm.addField("height", myHeight);
        myImageSize = getComponentFactory().createTextField("flashPlayerEditPanel.imageSize", new MinMaxIntegerValidator(getBundleString("flashPlayerEditPanel.error.imageSize", 0, 4096), 0, 4096));
        myForm.addField("imageSize", myImageSize);
        myUpload = new Upload(null, this);
        myUpload.setButtonCaption(getBundleString("flashPlayerEditPanel.upload"));
        myUpload.setImmediate(true);
        myUpload.addListener((Upload.StartedListener) this);
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
        myFlashPlayerConfig.setPlaylistFileType((PlaylistFileType) myFileType.getValue());
        myFlashPlayerConfig.setTimeUnit(((TimeUnitWrapper)myTimeUnit.getValue()).getTimeUnit());
        myFlashPlayerConfig.setWidth(myWidth.getIntegerValue(640));
        myFlashPlayerConfig.setHeight(myHeight.getIntegerValue(480));
        myFlashPlayerConfig.setImageSize(myImageSize.getIntegerValue(0));
        myAddonsConfigPanel.saveFlashPlayer(myFlashPlayerConfig);
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected void initFromConfig() {
        myName.setValue(myFlashPlayerConfig.getName());
        myFileType.setValue(myFlashPlayerConfig.getPlaylistFileType());
        for (TimeUnitWrapper wrapper : myTimeUnitWrappers) {
            if (wrapper.getTimeUnit().equals(myFlashPlayerConfig.getTimeUnit())) {
                myTimeUnit.setValue(wrapper);
                break;
            }
        }
        myWidth.setValue(myFlashPlayerConfig.getWidth());
        myHeight.setValue(myFlashPlayerConfig.getHeight());
        myImageSize.setValue(myFlashPlayerConfig.getImageSize());
    }

    @Override
    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myForm)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else {
            writeToConfig();
            closeWindow();
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

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.uploadFailed");
        FileUtils.deleteQuietly(new File(getUploadDir(), event.getFilename()));
    }

    private File getUploadDir() {
        return MyTunesRss.TEMP_CACHE.getBaseDir();
    }

    @Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
        try {
            return new FileOutputStream(new File(getUploadDir(), filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            File uploadFile = new File(getUploadDir(), event.getFilename());
            File targetDir = myFlashPlayerConfig.getBaseDir();
            targetDir = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/flashplayer", myFlashPlayerConfig.getId());
            if (!targetDir.mkdirs()) {
                LOGGER.warn("Could not create folder for flash player.");
            }
            if (event.getFilename().toLowerCase().endsWith(".zip")) {
                if (!ZipUtils.unzip(uploadFile, targetDir)) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.extractFailed");
                } else {
                    if (!new File(targetDir, "index.html").isFile()) {
                        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.invalidArchive");
                    }
                }
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("flashPlayerEditPanel.error.invalidArchive");
            }
        } finally {
            FileUtils.deleteQuietly(new File(getUploadDir(), event.getFilename()));
        }
    }
}
