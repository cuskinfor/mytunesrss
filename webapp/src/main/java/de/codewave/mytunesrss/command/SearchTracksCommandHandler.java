package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.servlet.WebConfig;

/**
 * de.codewave.mytunesrss.command.SearchTracksCommandHandler
 */
public class SearchTracksCommandHandler extends BrowseTrackCommandHandler {

    /**
     * Save last search term and fuzzy search indicator and then call the
     * super method.
     *
     * @throws Exception Any exception thrown in the super method.
     */
    @Override
    public void executeAuthorized() throws Exception {
        getRequest().getSession().setAttribute("lastSearchTerm", getRequestParameter("searchTerm", null));
        WebConfig webConfig = getWebConfig();
        webConfig.setSearchFuzziness(getIntegerRequestParameter("searchFuzziness", 0));
        MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getAuthUser(), webConfig);
        super.executeAuthorized();
    }
}