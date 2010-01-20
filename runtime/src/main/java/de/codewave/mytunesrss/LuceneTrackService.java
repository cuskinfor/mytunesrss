package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.LuceneTrackService
 */
public class LuceneTrackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTrackService.class);

    private static final Set<String> STOP_WORDS = new HashSet<String>();

    {
        STOP_WORDS.addAll(StopFilter.makeStopSet(StopAnalyzer.ENGLISH_STOP_WORDS));
    }

    private Directory getDirectory() throws IOException {
        return FSDirectory.getDirectory(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/lucene/track");
    }

    public void indexAllTracks() throws IOException, SQLException {
        LOGGER.debug("Indexing all tracks.");
        long start = System.currentTimeMillis();
        Directory directory = getDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer(STOP_WORDS);
        IndexWriter iwriter = new IndexWriter(directory, analyzer, true, new IndexWriter.MaxFieldLength(300));
        FindPlaylistTracksQuery query = new FindPlaylistTracksQuery(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, SortOrder.KeepOrder);
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        final Map<String, List<String>> trackTagMap = new HashMap<String, List<String>>();
        session.executeQuery(new DataStoreQuery<Object>() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                execute(MyTunesRssUtils.createStatement(connection, "getTrackTagMap"), new ResultBuilder<Object>() {
                    public Object create(ResultSet resultSet) throws SQLException {
                        List<String> tags = trackTagMap.get(resultSet.getString(1));
                        if (tags == null) {
                            tags = new ArrayList<String>();
                            trackTagMap.put(resultSet.getString(1), tags);
                        }
                        tags.add(resultSet.getString(2));
                        return null;
                    }
                }).getResults(); // call #getResults() just to make result builder run
                return null;
            }
        });
        DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            Document document = createTrackDocument(track, trackTagMap);
            iwriter.addDocument(document);
        }
        iwriter.optimize();
        iwriter.close();
        directory.close();
        session.commit();
        LOGGER.debug("Finished indexing all tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
    }

    /**
     * Create a document for a track.
     *
     * @param track A track to create a document from.
     * @param trackTagMap Track to tag mapping.
     *
     * @return Document for the track.
     */
    private Document createTrackDocument(Track track, Map<String, List<String>> trackTagMap) {
        Document document = new Document();
        document.add(new Field("id", track.getId(), Field.Store.YES, Field.Index.NO));
        document.add(new Field("name", track.getName(), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("album", track.getAlbum(), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("artist", track.getArtist(), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("filename", track.getFilename(), Field.Store.NO, Field.Index.ANALYZED));
        if (StringUtils.isNotBlank(track.getComment())) {
            document.add(new Field("comment", track.getComment(), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getGenre())) {
            document.add(new Field("genre", track.getGenre(), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (trackTagMap.get(track.getId()) != null) {
            document.add(new Field("tags", StringUtils.join(trackTagMap.get(track.getId()), " "), Field.Store.NO, Field.Index.ANALYZED));
        }
        return document;
    }

    public void updateTracks(String[] trackIds) throws IOException, SQLException {
        LOGGER.debug("Indexing " + trackIds.length + " tracks.");
        long start = System.currentTimeMillis();
        Directory directory = getDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer(STOP_WORDS);
        IndexWriter iwriter = new IndexWriter(directory, analyzer, false, new IndexWriter.MaxFieldLength(300));
        FindTrackQuery query = FindTrackQuery.getForIds(trackIds);
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        final Map<String, List<String>> trackTagMap = new HashMap<String, List<String>>();
        session.executeQuery(new DataStoreQuery<Object>() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                execute(MyTunesRssUtils.createStatement(connection, "getTrackTagMap"), new ResultBuilder<Object>() {
                    public Object create(ResultSet resultSet) throws SQLException {
                        List<String> tags = trackTagMap.get(resultSet.getString(1));
                        if (tags == null) {
                            tags = new ArrayList<String>();
                            trackTagMap.put(resultSet.getString(1), tags);
                        }
                        tags.add(resultSet.getString(2));
                        return null;
                    }
                }).getResults(); // call #getResults() just to make result builder run
                return null;
            }
        });
        DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
        Set<String> deletedTracks = new HashSet<String>(Arrays.asList(trackIds));
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            Document document = createTrackDocument(track, trackTagMap);
            iwriter.updateDocument(new Term("id", track.getId()), document);
            deletedTracks.remove(track.getId());
        }
        for (String deletedTrack : deletedTracks) {
            iwriter.deleteDocuments(new Term("id", deletedTrack));
        }
        iwriter.optimize();
        iwriter.close();
        directory.close();
        session.commit();
        LOGGER.debug("Finished indexing " + trackIds.length + " tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
    }

    public Collection<String> searchTrackIds(String[] searchTerms, int fuzziness) throws IOException, ParseException {
        Directory directory = getDirectory();
        final IndexSearcher isearcher = new IndexSearcher(directory);
        Query luceneQuery = createQuery(searchTerms, fuzziness);
        final BitSet bits = new BitSet();
        isearcher.search(luceneQuery, new HitCollector() {
            @Override
            public void collect(int i, float v) {
                bits.set(i);
            }
        });
        final Set<String> trackIds = new HashSet<String>();
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            trackIds.add(isearcher.doc(i).get("id"));
        }
        isearcher.close();
        directory.close();
        return trackIds;
    }

    private Query createQuery(String[] searchTerms, int fuzziness) {
        BooleanQuery andQuery = new BooleanQuery();
        for (String searchTerm : searchTerms) {
            if (!STOP_WORDS.contains(searchTerm)) {
                BooleanQuery orQuery = new BooleanQuery();
                for (String field : new String[]{"name", "album", "artist", "comment", "tags"}) {
                    String escapedSearchTerm = QueryParser.escape(searchTerm);
                    orQuery.add(new WildcardQuery(new Term(field, "*" + escapedSearchTerm + "*")), BooleanClause.Occur.SHOULD);
                    if (fuzziness > 0) {
                        orQuery.add(new FuzzyQuery(new Term(field, escapedSearchTerm), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.SHOULD);
                    }
                }
                andQuery.add(orQuery, BooleanClause.Occur.MUST);
            }
        }
        return andQuery;
    }

    public List<String> searchTrackIds(SmartInfo smartInfo, int fuzziness) throws IOException, ParseException {
        Directory directory = getDirectory();
        final IndexSearcher isearcher = new IndexSearcher(directory);
        Query luceneQuery = createQuery(smartInfo, fuzziness);
        final BitSet bits = new BitSet();
        isearcher.search(luceneQuery, new HitCollector() {
            @Override
            public void collect(int i, float v) {
                bits.set(i);
            }
        });
        final List<String> trackIds = new ArrayList<String>();
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            trackIds.add(isearcher.doc(i).get("id"));
        }
        isearcher.close();
        directory.close();
        return trackIds;
    }

    private Query createQuery(SmartInfo smartInfo, int fuzziness) {
        BooleanQuery andQuery = new BooleanQuery();
        addToAndQuery(andQuery, "album", smartInfo.getAlbumPattern(), fuzziness);
        addToAndQuery(andQuery, "artist", smartInfo.getArtistPattern(), fuzziness);
        addToAndQuery(andQuery, "genre", smartInfo.getGenrePattern(), fuzziness);
        addToAndQuery(andQuery, "tags", smartInfo.getTagPattern(), fuzziness);
        addToAndQuery(andQuery, "name", smartInfo.getTitlePattern(), fuzziness);
        addToAndQuery(andQuery, "comment", smartInfo.getCommentPattern(), fuzziness);
        addToAndQuery(andQuery, "filename", smartInfo.getFilePattern(), fuzziness);
        return andQuery;
    }

    private void addToAndQuery(BooleanQuery andQuery, String field, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            if (!STOP_WORDS.contains(pattern)) {
                BooleanQuery orQuery = new BooleanQuery();
                String escapedPattern = QueryParser.escape(pattern);
                orQuery.add(new WildcardQuery(new Term(field, "*" + escapedPattern + "*")), BooleanClause.Occur.SHOULD);
                if (fuzziness > 0) {
                    orQuery.add(new FuzzyQuery(new Term(field, escapedPattern), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.SHOULD);
                }
                andQuery.add(orQuery, BooleanClause.Occur.MUST);
            }
        }
    }
}