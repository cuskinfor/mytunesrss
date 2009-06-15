package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.LuceneTrackService
 */
public class LuceneTrackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTrackService.class);

    private Directory getDirectory() throws IOException {
        return FSDirectory.getDirectory(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/lucene/track");
    }

    public void indexAllTracks() throws IOException, SQLException {
        LOGGER.debug("Indexing all tracks.");
        long start = System.currentTimeMillis();
        Directory directory = getDirectory();
        IndexWriter iwriter = new IndexWriter(directory, new StandardAnalyzer(Collections.emptySet()), true, new IndexWriter.MaxFieldLength(300));
        FindPlaylistTracksQuery query = new FindPlaylistTracksQuery(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, SortOrder.KeepOrder);
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            Document document = new Document();
            document.add(new Field("id", track.getId(), Field.Store.YES, Field.Index.NO));
            document.add(new Field("name", track.getName(), Field.Store.NO, Field.Index.ANALYZED));
            document.add(new Field("album", track.getAlbum(), Field.Store.NO, Field.Index.ANALYZED));
            document.add(new Field("artist", track.getArtist(), Field.Store.NO, Field.Index.ANALYZED));
            iwriter.addDocument(document);
        }
        iwriter.optimize();
        iwriter.close();
        directory.close();
        session.commit();
        LOGGER.debug("Finished indexing all tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
    }

/*
    public void indexTrack(String id, String name, String album, String artist) throws IOException {
        LOGGER.debug("Index track.");
        Directory directory = getDirectory();
        IndexWriter iwriter = new IndexWriter(directory, new StandardAnalyzer(), new IndexWriter.MaxFieldLength(2000));
        Document document = new Document();
        document.add(new Field("id", id, Field.Store.YES, Field.Index.NO));
        document.add(new Field("name", name, Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("album", album, Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("artist", artist, Field.Store.NO, Field.Index.ANALYZED));
        iwriter.addDocument(document);
        iwriter.optimize();
        iwriter.close();
        directory.close();
    }

    public void removeTracksFromIndex(String... ids) throws IOException {
        LOGGER.debug("Removed tracks from index.");
        Directory directory = getDirectory();
        IndexWriter iwriter = new IndexWriter(directory, new StandardAnalyzer(), new IndexWriter.MaxFieldLength(2000));
        for (String id : ids) {
            iwriter.deleteDocuments(new Term("id", id));
        }
        iwriter.optimize();
        iwriter.close();
        directory.close();
    }
*/

    public List<String> searchTrackIds(String[] searchTerms, boolean fuzzy) throws IOException, ParseException {
        Directory directory = getDirectory();
        final IndexSearcher isearcher = new IndexSearcher(directory);
        QueryParser parser = new QueryParser("name", new StandardAnalyzer(Collections.emptySet()));
        Query luceneQuery = parser.parse(createQueryString(searchTerms, fuzzy));
        final BitSet bits = new BitSet();
        isearcher.search(luceneQuery, new HitCollector() {
            @Override
            public void collect(int i, float v) {
                    bits.set(i);
            }
        });
        final List<String> trackIds = new ArrayList<String>();
        for (int i = bits.nextSetBit(0); i > 0; i = bits.nextSetBit(i + 1)) {
            trackIds.add(isearcher.doc(i).get("id"));
        }
        isearcher.close();
        directory.close();
        return trackIds;
    }

    private String createQueryString(String[] searchTerms, boolean fuzzy) {
        StringBuilder queryString = new StringBuilder();
        for (String field : new String[]{"name", "album", "artist"}) {
            queryString.append("(");
            for (int i = 0; i < searchTerms.length; i++) {
                queryString.append(field).append(":").append(QueryParser.escape(searchTerms[i])).append(fuzzy ? "~" : "");
                if (i + 1 < searchTerms.length) {
                    queryString.append(" AND ");
                }
            }
            queryString.append(") OR ");
        }
        String result = queryString.substring(0, queryString.length() - 4);
        LOGGER.debug("Created query string \"" + result + "\".");
        return result;
    }
}