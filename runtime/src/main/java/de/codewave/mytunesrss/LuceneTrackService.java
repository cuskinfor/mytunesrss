package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.FindAllTagsForTrackQuery;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultBuilder;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
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
                });
                return null;
            }
        });
        DataStoreQuery.QueryResult<Track> queryResult = session.executeQuery(query);
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            Document document = new Document();
            document.add(new Field("id", track.getId(), Field.Store.YES, Field.Index.NO));
            document.add(new Field("name", track.getName(), Field.Store.NO, Field.Index.ANALYZED));
            document.add(new Field("album", track.getAlbum(), Field.Store.NO, Field.Index.ANALYZED));
            document.add(new Field("artist", track.getArtist(), Field.Store.NO, Field.Index.ANALYZED));
            if (StringUtils.isNotBlank(track.getComment())) {
                document.add(new Field("comment", track.getComment(), Field.Store.NO, Field.Index.ANALYZED));
            }
            if (trackTagMap.get(track.getId()) != null) {
                document.add(new Field("tags", StringUtils.join(trackTagMap.get(track.getId()), " "), Field.Store.NO, Field.Index.ANALYZED));
            }
            iwriter.addDocument(document);
        }
        iwriter.optimize();
        iwriter.close();
        directory.close();
        session.commit();
        LOGGER.debug("Finished indexing all tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
    }

    public List<String> searchTrackIds(String[] searchTerms, int fuzziness) throws IOException, ParseException {
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
        final List<String> trackIds = new ArrayList<String>();
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
                    Term term = new Term(field, searchTerm);
                    Query termQuery = fuzziness > 0 ? new FuzzyQuery(term, ((float)(100 - fuzziness)) / 100f) : new TermQuery(term);
                    orQuery.add(termQuery, BooleanClause.Occur.SHOULD);
                }
                andQuery.add(orQuery, BooleanClause.Occur.MUST);
            }
        }
        return andQuery;
    }
}