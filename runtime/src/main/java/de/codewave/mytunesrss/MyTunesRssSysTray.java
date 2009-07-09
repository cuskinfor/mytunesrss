/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;

import de.codewave.mytunesrss.settings.Settings;

/**
 * de.codewave.mytunesrss.SysTray
 */
public class MyTunesRssSysTray {

	public static MyTunesRssSysTray newInstance(Settings settingsForm)
			throws AWTException {
		if (SystemTray.isSupported()) {
			return new MyTunesRssSysTray(settingsForm);
		}
		return null;
	}

	private TrayIcon myTrayIcon;
	private PopupMenu myMenu;
	private MenuItem myQuit;
	private MenuItem myShow;
	private MenuItem myUpdate;
	private MenuItem myStartServer;
	private MenuItem myStopServer;

	public MyTunesRssSysTray(Settings settingsForm) throws AWTException {
		Image menuIcon = Toolkit.getDefaultToolkit().createImage(
				getClass().getResource("SysTray.gif"));
		ActionListener menuListener = new Listener(settingsForm);

		myMenu = new PopupMenu(MyTunesRssUtils
				.getBundleString("systray.menuLabel"));
		myQuit = new MenuItem(MyTunesRssUtils.getBundleString("systray.quit"));
		myQuit.setActionCommand("quit");
		myQuit.addActionListener(menuListener);
		myMenu.add(myQuit);
		myMenu.addSeparator();
		myShow = new MenuItem(MyTunesRssUtils.getBundleString("systray.show"));
		myShow.setActionCommand("show");
		myShow.addActionListener(menuListener);
		myMenu.add(myShow);
		myMenu.addSeparator();
		myUpdate = new MenuItem(MyTunesRssUtils
				.getBundleString("systray.updateDatabase"));
		myUpdate.setActionCommand("update");
		myUpdate.addActionListener(menuListener);
		myMenu.add(myUpdate);
		myMenu.addSeparator();
		myStopServer = new MenuItem(MyTunesRssUtils
				.getBundleString("systray.stopServer"));
		myStopServer.setActionCommand("stop_server");
		myStopServer.addActionListener(menuListener);
		myMenu.add(myStopServer);
		myStartServer = new MenuItem(MyTunesRssUtils
				.getBundleString("systray.startServer"));
		myStartServer.setActionCommand("start_server");
		myStartServer.addActionListener(menuListener);
		myMenu.add(myStartServer);
		myTrayIcon = new TrayIcon(menuIcon, MyTunesRssUtils
				.getBundleString("systray.menuLabel"), myMenu);
		SystemTray.getSystemTray().add(myTrayIcon);
		setServerStopped();
	}

	public void disableAll() {
		myQuit.setEnabled(false);
		myShow.setEnabled(false);
		myUpdate.setEnabled(false);
		myStartServer.setEnabled(false);
		myStopServer.setEnabled(false);
	}

	public void setServerRunning() {
		myQuit.setEnabled(true);
		myShow.setEnabled(true);
		myUpdate.setEnabled(true);
		myStartServer.setEnabled(false);
		myStopServer.setEnabled(true);
	}

	public void setServerStopped() {
		myQuit.setEnabled(true);
		myShow.setEnabled(true);
		myUpdate.setEnabled(true);
		myStartServer.setEnabled(true);
		myStopServer.setEnabled(false);
	}

	public void remove() {
		if (SystemTray.isSupported()) {
			SystemTray.getSystemTray().remove(myTrayIcon);
		}
	}

	public static class Listener implements ActionListener {
		private Settings mySettingsForm;

		public Listener(Settings settingsForm) {
			mySettingsForm = settingsForm;
		}

		public void actionPerformed(ActionEvent e) {
			if (StringUtils.equals(e.getActionCommand(), "quit")) {
				mySettingsForm.doQuitApplication();
			} else if (StringUtils.equals(e.getActionCommand(), "show")) {
				showFrame();
			} else if (StringUtils.equals(e.getActionCommand(), "update")) {
				updateDatabase();
			} else if (StringUtils.equals(e.getActionCommand(), "start_server")) {
				mySettingsForm.doStartServer();
			} else if (StringUtils.equals(e.getActionCommand(), "stop_server")) {
				mySettingsForm.doStopServer();
			}
		}

		private void showFrame() {
			MyTunesRss.ROOT_FRAME.setVisible(true);
			MyTunesRss.ROOT_FRAME.setExtendedState(JFrame.NORMAL);
		}

		private void updateDatabase() {
			MyTunesRssUtils.executeDatabaseUpdate();
		}
	}
}
