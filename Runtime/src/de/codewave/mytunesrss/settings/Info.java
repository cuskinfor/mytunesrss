/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.utils.swing.components.*;
import de.codewave.utils.swing.SwingUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;

/**
 * de.codewave.mytunesrss.settings.Info
 */
public class Info {
    private JButton mySupportContactButton;
    private JPanel myRootPanel;
    private JTextField myUsernameInput;
    private PasswordHashField myPasswordInput;
  private JTextField myProxyHostInput;
  private JTextField myProxyPortInput;
  private JCheckBox myUseProxyInput;

  public void init() {
        mySupportContactButton.addActionListener(new SupportContactActionListener());
        myUsernameInput.setText(MyTunesRss.CONFIG.getMyTunesRssComUser());
        myPasswordInput.setPasswordHash(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash());
    myUseProxyInput.setSelected(MyTunesRss.CONFIG.isProxyServer());
    myUseProxyInput.addActionListener(new UseProxyActionListener());
    SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
    SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
    myProxyHostInput.setText(MyTunesRss.CONFIG.getProxyHost());
    int port = MyTunesRss.CONFIG.getProxyPort();
    if (port > 0 && port < 65536) {
        myProxyPortInput.setText(Integer.toString(port));
    } else {
        myProxyPortInput.setText("");
    }
    }

    private void createUIComponents() {
        myPasswordInput = new PasswordHashField(MyTunesRss. BUNDLE.getString("passwordHasBeenSet"), MyTunesRss.MESSAGE_DIGEST);
    }

    public void updateConfigFromGui() {
        MyTunesRss.CONFIG.setMyTunesRssComUser(myUsernameInput.getText());
        if (myPasswordInput.getPasswordHash() != null) {
            MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(myPasswordInput.getPasswordHash());
        }
      MyTunesRss.CONFIG.setProxyServer(myUseProxyInput.isSelected());
      MyTunesRss.CONFIG.setProxyHost(myProxyHostInput.getText());
      try {
          MyTunesRss.CONFIG.setProxyPort(Integer.parseInt(myProxyPortInput.getText()));
      } catch (NumberFormatException e1) {
          MyTunesRss.CONFIG.setProxyPort(-1);
      }
    }

    public class SupportContactActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            updateConfigFromGui();
            new SupportContact().display(MyTunesRss.ROOT_FRAME, MyTunesRss.BUNDLE.getString("dialog.supportRequest"), MyTunesRss.BUNDLE.getString(
                    "settings.supportInfo"));
        }
    }

  public class UseProxyActionListener implements ActionListener {
      public void actionPerformed(ActionEvent actionEvent) {
          SwingUtils.enableElementAndLabel(myProxyHostInput, myUseProxyInput.isSelected());
          SwingUtils.enableElementAndLabel(myProxyPortInput, myUseProxyInput.isSelected());
      }
  }
}
