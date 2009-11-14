package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.MinMaxValueTextFieldValidation;
import de.codewave.utils.swing.NotEmptyTextFieldValidation;
import de.codewave.utils.swing.SwingUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ldap implements SettingsForm {
    private JPanel myRootPanel;
    private JTextField myHostInput;
    private JTextField myPortInput;
    private JTextField myAuthPrincipalInput;
    private JTextField mySearchRootInput;
    private JTextField mySearchExpressionInput;
    private JTextField mySearchTimeoutInput;
    private JTextField myMailAttributeNameInput;
    private JComboBox myTemplateUserInput;
    private JComboBox myAuthMethodInput;

    public Ldap() {
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myPortInput, 1, 65535, true, MyTunesRssUtils.getBundleString("error.illegalLdapPort")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myHostInput, MyTunesRssUtils.getBundleString("error.emptyLdapHost")));
        JTextFieldValidation.setValidation(new NotEmptyTextFieldValidation(myAuthPrincipalInput, MyTunesRssUtils.getBundleString("error.emptyAuthPrincipal")));
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(mySearchTimeoutInput, 1, 10000, true, MyTunesRssUtils.getBundleString("error.illegalLdapSearchTimeout")));
        myAuthMethodInput.addItem("SIMPLE");
        List<String> userNames = new ArrayList<String>();
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            userNames.add(user.getName());
        }
        Collections.sort(userNames);
        for (String userName : userNames) {
            myTemplateUserInput.addItem(userName);
        }
    }

    public void initValues() {
        myAuthMethodInput.setSelectedItem(MyTunesRss.CONFIG.getLdapConfig().getAuthMethod());
        myAuthPrincipalInput.setText(MyTunesRss.CONFIG.getLdapConfig().getAuthPrincipal());
        myHostInput.setText(MyTunesRss.CONFIG.getLdapConfig().getHost());
        myMailAttributeNameInput.setText(MyTunesRss.CONFIG.getLdapConfig().getMailAttributeName());
        myPortInput.setText(Integer.toString(MyTunesRss.CONFIG.getLdapConfig().getPort()));
        mySearchExpressionInput.setText(MyTunesRss.CONFIG.getLdapConfig().getSearchExpression());
        mySearchRootInput.setText(MyTunesRss.CONFIG.getLdapConfig().getSearchRoot());
        mySearchTimeoutInput.setText(Integer.toString(MyTunesRss.CONFIG.getLdapConfig().getSearchTimeout()));
        myTemplateUserInput.setSelectedItem(MyTunesRss.CONFIG.getLdapConfig().getTemplateUser());

    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(myRootPanel);
        if (messages == null) {
            MyTunesRss.CONFIG.getLdapConfig().setAuthMethod(myAuthMethodInput.getSelectedItem().toString());
            MyTunesRss.CONFIG.getLdapConfig().setAuthPrincipal(myAuthPrincipalInput.getText());
            MyTunesRss.CONFIG.getLdapConfig().setHost(myHostInput.getText());
            MyTunesRss.CONFIG.getLdapConfig().setMailAttributeName(myMailAttributeNameInput.getText());
            MyTunesRss.CONFIG.getLdapConfig().setPort(MyTunesRssUtils.getTextFieldInteger(myPortInput, -1));
            MyTunesRss.CONFIG.getLdapConfig().setSearchExpression(mySearchExpressionInput.getText());
            MyTunesRss.CONFIG.getLdapConfig().setSearchRoot(mySearchRootInput.getText());
            MyTunesRss.CONFIG.getLdapConfig().setSearchTimeout(MyTunesRssUtils.getTextFieldInteger(mySearchTimeoutInput, 0));
            MyTunesRss.CONFIG.getLdapConfig().setTemplateUser(myTemplateUserInput.getSelectedItem().toString());
        }
        return messages;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.ldap.title");
    }
}
