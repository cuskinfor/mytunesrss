/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.addons.AddonsUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.addons.ThemeDefinition;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * de.codewave.mytunesrss.command.ShowSettingsCommandHandler
 */
public class ShowSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isEditWebSettings()) {
            Collection<ThemeDefinition> themes = AddonsUtils.getThemes(true);
            // remove default user interface theme from list (if any is set as default)
            for (Iterator<ThemeDefinition> iter = themes.iterator(); iter.hasNext(); ) {
                if (StringUtils.equals(iter.next().getName(), MyTunesRss.CONFIG.getDefaultUserInterfaceTheme())) {
                    iter.remove();
                }
            }
            getRequest().setAttribute("themes", themes);
            forward(MyTunesRssResource.Settings);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
