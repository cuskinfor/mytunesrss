/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.utils.io.IOUtils;
import de.codewave.vaadin.VaadinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

public abstract class ServerSideFileChooser extends CustomComponent implements Button.ClickListener, ItemClickEvent.ItemClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSideFileChooser.class);

    public static Pattern PATTERN_ALL = Pattern.compile("^.*$");

    private static File theLastSelectedDir = new File(System.getProperty("user.home"));

    private Table myChooser;
    private File myCurrentDir;
    private Button myOk;
    private Button myCancel;
    private Button myCreateDir;
    private Pattern myAllowedDirPattern;
    private Pattern myAllowedFilePattern;
    private Label myCurrentDirLabel;
    private Select myRootsInput;
    private File[] myRootFiles;
    private Property.ValueChangeListener myRootSelectionListener;

    public ServerSideFileChooser(File currentDir, Pattern allowedDirPattern, Pattern allowedFilePattern, boolean allowCreateDir, String rootsLabel) {
        myAllowedDirPattern = allowedDirPattern;
        myAllowedFilePattern = allowedFilePattern;
        if (currentDir != null && currentDir.exists()) {
            myCurrentDir = currentDir.isDirectory() ? currentDir : currentDir.getParentFile();
        } else {
            myCurrentDir = theLastSelectedDir;
        }
        Panel panel = new Panel();
        ((Layout) panel.getContent()).setMargin(true);
        ((Layout.SpacingHandler) panel.getContent()).setSpacing(true);
        myRootFiles = File.listRoots();
        //myRootFiles = new File[] {new File("/Applications"), new File("/Users"), new File("/Volumes")};
        if (myRootFiles != null && myRootFiles.length > 1) {
            myRootsInput = new Select(rootsLabel, Arrays.asList(myRootFiles));
            myRootsInput.setNullSelectionAllowed(false);
            myRootSelectionListener = new Property.ValueChangeListener() {
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    myCurrentDir = (File) myRootsInput.getValue();
                    setFiles();
                }
            };
            myRootsInput.addListener(myRootSelectionListener);
            panel.addComponent(myRootsInput);
        }
        myCurrentDirLabel = new Label();
        panel.addComponent(myCurrentDirLabel);
        myChooser = new Table();
        myChooser.setWidth(100f, Sizeable.UNITS_PERCENTAGE);
        myChooser.setSelectable(true);
        myChooser.addContainerProperty("File", String.class, "");
        myChooser.addContainerProperty("Date", String.class, null);
        myChooser.setColumnExpandRatio("File", 1f);
        myChooser.addListener(this);
        myOk = new Button("Ok", this); // TODO i18n
        myOk.setEnabled(false);
        myCancel = new Button("Cancel", this); // TODO i18n
        myCreateDir = new Button("Create folder", this); // TODO i18n
        panel.addComponent(myChooser);
        Panel buttonPanel = new Panel(new HorizontalLayout());
        ((Layout.SpacingHandler) buttonPanel.getContent()).setSpacing(true);
        buttonPanel.addStyleName("light");
        if (allowCreateDir) {
            buttonPanel.addComponent(myCreateDir);
        }
        buttonPanel.addComponent(myOk);
        buttonPanel.addComponent(myCancel);
        panel.addComponent(buttonPanel);
        setCompositionRoot(panel);
        setFiles();
    }

    private void setFiles() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Setting files.");
        }

        myCurrentDirLabel.setValue(myCurrentDir.getAbsolutePath());
        if (myRootsInput != null) {
            for (File root : myRootFiles) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking root \"" + root.getAbsolutePath() + "\".");
                }
                try {
                    if (IOUtils.isContainedOrSame(root, myCurrentDir)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Selecting root \"" + root.getAbsolutePath() + "\".");
                        }
                        // make sure changing the root does not trigger the listener
                        myRootsInput.removeListener(myRootSelectionListener);
                        myRootsInput.setValue(root);
                        myRootsInput.addListener(myRootSelectionListener);
                        break;
                    }
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not check if file is root.", e);
                    }

                }
            }
        }
        myOk.setEnabled(false);
        myChooser.removeAllItems();
        if (myCurrentDir.getParentFile() != null) {
            myChooser.addItem(new Object[]{"[..]", null}, myCurrentDir.getParentFile());
        }
        File[] files = myCurrentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                myChooser.addItem(new Object[]{file.getName(), new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(file.lastModified()))}, file);
            }
        }
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myOk) {
            File file = (File) myChooser.getValue();
            if (isAllowedSelect(file)) {
                theLastSelectedDir = myCurrentDir;
                onFileSelected(file);
            }
        } else if (clickEvent.getButton() == myCancel) {
            theLastSelectedDir = myCurrentDir;
            onCancel();
        } else if (clickEvent.getButton() == myCreateDir) {
            new TextFieldWindow(30, Sizeable.UNITS_EM, null, null, "Create folder", "Please enter the name of the new folder.", "Create", "Cancel") {
                @Override
                protected void onOk(String text) {
                    getParent().removeWindow(this);
                    new File(myCurrentDir, text).mkdir();
                    setFiles();
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        }
    }

    private boolean isAllowedSelect(File file) {
        if (file != null && file.exists()) {
            return (file.isFile() && myAllowedFilePattern != null && myAllowedFilePattern.matcher(file.getName()).matches()) || (file.isDirectory() && myAllowedDirPattern != null && myAllowedDirPattern.matcher(file.getName()).matches());
        }
        return false;
    }

    public void itemClick(ItemClickEvent itemClickEvent) {
        File file = (File) itemClickEvent.getItemId();
        myOk.setEnabled(isAllowedSelect(file));
        if (itemClickEvent.isDoubleClick()) {
            if (file.isDirectory()) {
                myCurrentDir = file;
                setFiles();
            }
        }
    }

    protected abstract void onCancel();

    protected abstract void onFileSelected(File file);
}
