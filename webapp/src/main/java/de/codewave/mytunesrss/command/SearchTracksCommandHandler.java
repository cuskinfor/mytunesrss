package de.codewave.mytunesrss.command;

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
        getRequest().getSession().setAttribute("lastSearchFuzzy", getBooleanRequestParameter("fuzzy", false));
        super.executeAuthorized();
    }
}