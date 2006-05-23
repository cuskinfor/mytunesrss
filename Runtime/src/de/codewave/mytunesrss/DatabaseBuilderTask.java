/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.DatabaseBuilderTask
 */
public class DatabaseBuilderTask extends PleaseWait.Task {
    private static final Log LOG = LogFactory.getLog(DatabaseBuilderTask.class);

    public static boolean needsCreation() {
        try {
            return MyTunesRss.STORE.executeQuery(new DataStoreQuery<Boolean>() {
                public Collection<Boolean> execute(Connection connection) throws SQLException {
                    connection.createStatement().executeQuery("SELECT COUNT(*) FROM track");
                    return Collections.singletonList(Boolean.FALSE);
                }
            }).iterator().next();
        } catch (SQLException e) {
            return true;
        }
    }

    public static boolean needsUpdate(URL libraryXmlUrl) throws SQLException {
        if (libraryXmlUrl != null) {
            if (needsCreation()) {
                return true;
            }
            Collection<Long> lastUpdate = MyTunesRss.STORE.executeQuery(new DataStoreQuery<Long>() {
                public Collection<Long> execute(Connection connection) throws SQLException {
                    ResultSet resultSet = connection.createStatement().executeQuery("SELECT lastupdate FROM itunes");
                    if (resultSet.next()) {
                        return Collections.singletonList(resultSet.getLong(1));
                    }
                    return null;
                }
            });
            if (lastUpdate == null || lastUpdate.isEmpty() || new File(libraryXmlUrl.getPath()).lastModified() > lastUpdate.iterator().next()) {
                return true;
            }
        }
        return false;
    }

    private URL myLibraryXmlUrl;

    public DatabaseBuilderTask(URL libraryXmlUrl) {
        myLibraryXmlUrl = libraryXmlUrl;
    }

    public void execute() {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            if (needsCreation()) {
                createDatabase();
            } else {
                storeSession.executeStatement(new PrepareForReloadStatement());
            }
            final long updateTime = System.currentTimeMillis();
            ITunesUtils.loadFromITunes(myLibraryXmlUrl, storeSession);
            storeSession.executeStatement(new UpdateHelpTablesStatement());
            storeSession.executeStatement(new CleanupAfterReloadStatement());
            createPagers(storeSession);
            storeSession.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    connection.createStatement().execute("UPDATE itunes SET lastupdate = " + updateTime);
                }
            });
            storeSession.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load iTunes library.", e);
            }
            try {
                storeSession.rollback();
            } catch (SQLException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not rollback transaction.", e1);
                }
            }
        }
    }

    private void createPagers(DataStoreSession storeSession) throws SQLException {
        List<FindIndexesQuery.Index> indexes = (List<FindIndexesQuery.Index>)storeSession.executeQuery(new FindAlbumIndexesQuery());
        Pager pager = createPager(indexes);
        List<Pager.Page> pages = null;
        if (pager != null) {
            pages = pager.getCurrentPages();
            for (int i = 0; i < pages.size(); i++) {
                Pager.Page page = pages.get(i);
                storeSession.executeStatement(new InsertAlbumPageStatement(i, page.getKey(), page.getValue()));
            }
        }
        indexes = (List<FindIndexesQuery.Index>)storeSession.executeQuery(new FindArtistIndexesQuery());
        pager = createPager(indexes);
        if (pager != null) {
            pages = pager.getCurrentPages();
            for (int i = 0; i < pages.size(); i++) {
                Pager.Page page = pages.get(i);
                storeSession.executeStatement(new InsertArtistPageStatement(i, page.getKey(), page.getValue()));
            }
        }
    }

    private static Pager createPager(List<FindIndexesQuery.Index> indexes) {
        if (indexes != null && indexes.size() > 1) {
            List<Pager.Page> pages = new ArrayList<Pager.Page>();
            if (indexes.size() < 10) {
                for (FindIndexesQuery.Index index : indexes) {
                    pages.add(new Pager.Page(index.getLetter(), index.getLetter()));
                }
            } else {
                float indexesPerPage = indexes.size() / 9;
                for (int page = 0; page < 9; page++) {
                    int startIndex = (int)(page * indexesPerPage);
                    int endIndex = page == 8 ? indexes.size() - 1 : (int)(((page + 1) * indexesPerPage) - 1);
                    String value;
                    if (startIndex != endIndex) {
                        value = indexes.get(startIndex).getLetter() + " - " + indexes.get(endIndex).getLetter();
                    } else {
                        value = indexes.get(startIndex).getLetter();
                    }
                    StringBuffer key = new StringBuffer(page == 0 ? "_!" : "");
                    for (int i = startIndex; i <= endIndex; i++) {
                        key.append("_").append(indexes.get(i).getLetter());
                    }
                    pages.add(new Pager.Page(key.substring(1), value));
                }
            }
            pages.add(new Pager.Page("", "all"));// todo: i18n word "all"
            Pager pager = new Pager(pages, pages.size());
            return pager;
        }
        return null;
    }

    public void createDatabase() throws SQLException {
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        storeSession.begin();
        try {
            storeSession.executeStatement(new CreateAllTablesStatement());
            storeSession.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create tables.", e);
            }
            storeSession.rollback();
            throw e;
        }
    }

    protected void cancel() {
        // intentionally left blank
    }

}