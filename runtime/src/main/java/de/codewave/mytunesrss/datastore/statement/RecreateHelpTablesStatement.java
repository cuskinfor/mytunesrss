package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class RecreateHelpTablesStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(RecreateHelpTablesStatement.class);

    public void execute(Connection connection) throws SQLException {
        List<Genre> genres = new FindGenreQuery(null, true, -1).execute(connection).getResults();
        List<String> hiddenGenres = new ArrayList<String>();
        List<String> genreNames = new ArrayList<String>();
        for (Genre genre : genres) {
            genreNames.add(genre.getName());
            if (genre.isHidden()) {
                hiddenGenres.add(genre.getName());
            }
        }
        SmartStatement statementAlbum = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesAlbum");
        long startTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recreating albums help table.");
        }
        statementAlbum.execute();
        connection.commit();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recreating artists help table.");
        }
        SmartStatement statementArtist = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesArtist");
        statementArtist.execute();
        connection.commit();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recreating genres help table.");
        }
        SmartStatement statementGenre = MyTunesRssUtils.createStatement(connection, "recreateHelpTablesGenre");
        statementGenre.setObject("hidden_genres", hiddenGenres);
        statementGenre.setObject("genres", genreNames);
        statementGenre.execute();
        connection.commit();
        long endTime = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time for building help tables: " + (endTime - startTime));
        }
    }
}