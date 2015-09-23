/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.swing.components;

import org.apache.commons.lang3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;

/**
 * de.codewave.mytunesrss.settings.PasswordHashField
 */
public class PasswordHashField extends JPasswordField {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordHashField.class);

    private byte[] myPasswordHash;
    private String myPasswordSetText;
    private MessageDigest myDigest;

    public PasswordHashField(String passwordSetText, MessageDigest digest) {
        init(passwordSetText, digest);
    }

    public PasswordHashField(int columns, String passwordSetText, MessageDigest digest) {
        super(columns);
        init(passwordSetText, digest);
    }

    private void init(String passwordSetText, MessageDigest digest) {
        myPasswordSetText = passwordSetText;
        myDigest = digest;
        addFocusListener(new PasswordInputListener());
    }

    private void setShowMode() {
        setText(myPasswordSetText);
        setEchoChar((char)0);
        Font font = getFont();
        setPasswordVisibleStyle(font);
    }

    protected void setPasswordVisibleStyle(Font font) {
        setFont(new Font(font.getName(), font.getStyle() | Font.ITALIC, font.getSize()));
        setForeground(Color.LIGHT_GRAY);
    }

    private void setEditMode() {
        setText("");
        setEchoChar('*');
        Font font = getFont();
        setPasswordHiddenStyle(font);
    }

    protected void setPasswordHiddenStyle(Font font) {
        setFont(new Font(font.getName(), font.getStyle() & (Integer.MAX_VALUE - Font.ITALIC), font.getSize()));
        setForeground(Color.BLACK);
    }

    public byte[] getPasswordHash() {
        if (hasFocus()) {
            createPasswordHash();
        }
        return myPasswordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myPasswordHash = passwordHash;
        if (passwordHash != null && passwordHash.length > 0) {
            setShowMode();
        } else {
            setEditMode();
        }
    }

    private void createPasswordHash() {
        String password = new String(getPassword()).trim();
        if (password != null && password.trim().length() > 0) {
            try {
                myPasswordHash = myDigest.digest(StringUtils.trim(password).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not found!");
            }
        }
    }

    private class PasswordInputListener implements FocusListener {
        private boolean myPreviousPasswordSet;

        public void focusGained(FocusEvent focusEvent) {
            myPreviousPasswordSet = getPassword().length > 0;
            setEditMode();
        }

        public void focusLost(FocusEvent focusEvent) {
            String password = new String(getPassword()).trim();
            if (password != null && password.trim().length() > 0) {
                createPasswordHash();
                setShowMode();
            } else if (myPreviousPasswordSet) {
                setShowMode();
            }
        }
    }

}
