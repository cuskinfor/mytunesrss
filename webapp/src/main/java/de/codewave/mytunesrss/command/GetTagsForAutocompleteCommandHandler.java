package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindAllTagsQuery;

/**
 * de.codewave.mytunesrss.command.GetTagsForAutocompleteCommandHandler
 */
public class GetTagsForAutocompleteCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getResponse().setContentType("text/plain");
        getResponse().setCharacterEncoding("UTF-8");
        String query = getRequestParameter("q", null);
        for (String result : getTransaction().executeQuery(new FindAllTagsQuery(query)).getResults()) {
            getResponse().getWriter().println(result);
        }
    }
}
