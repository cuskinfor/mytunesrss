package de.codewave.mytunesrss.lucene;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.mytunesrss.datastore.statement.SmartInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.sandbox.queries.SlowFuzzyQuery;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * de.codewave.mytunesrss.lucene.LuceneTrackService
 */
public class LuceneTrackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTrackService.class);
    private static final int MAX_TRACK_BUFFER = 5000;

    public static class ScoredTrack {
        private String myId;
        private float myScore;

        public ScoredTrack(String id, float score) {
            myId = id;
            myScore = score;
        }

        public String getId() {
            return myId;
        }

        public float getScore() {
            return myScore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScoredTrack that = (ScoredTrack) o;

            return myId.equals(that.myId);

        }

        @Override
        public int hashCode() {
            return myId.hashCode();
        }
    }

    private Deque<LuceneTrack> myTrackBuffer = new ArrayDeque<>();
    private FSDirectory myDirectory;
    private Analyzer myAnalyzer;
    private IndexWriter myIndexWriter;

    private Directory getDirectory() throws IOException {
        return FSDirectory.open(new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track").toPath());
    }

    private synchronized IndexWriter getIndexWriter() throws IOException {
        if (myDirectory == null) {
            myDirectory = FSDirectory.open(new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track").toPath());
        }
        if (myAnalyzer == null) {
            WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
            whitespaceAnalyzer.setVersion(Version.LUCENE_5_3_1);
            myAnalyzer = new LimitTokenCountAnalyzer(whitespaceAnalyzer, 300);
        }
        try {
            if (myIndexWriter == null) {
                IndexWriterConfig indexWriterConfig = new IndexWriterConfig(myAnalyzer);
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
                myIndexWriter = new IndexWriter(myDirectory, indexWriterConfig);
            }
        } catch (IndexNotFoundException e) {
            LOGGER.warn("No lucene index found, creating a new one.", e);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(myAnalyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            myIndexWriter = new IndexWriter(myDirectory, indexWriterConfig);
        }
        return myIndexWriter;
    }

    public synchronized boolean exists() {
        return new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track").isDirectory();
    }

    public synchronized void shutdown() {
        try {
            if (myIndexWriter != null) {
                try {
                    myIndexWriter.close();
                } catch (CorruptIndexException e) {
                    LOGGER.error("Could not close index writer.", e);
                    try {
                        deleteLuceneIndex();
                    } catch (IOException e1) {
                        LOGGER.error("Could not delete corrupted lucene index.", e1);
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not close index writer.",e );
                }
            }
            if (myDirectory != null) {
                myDirectory.close();
            }
        } finally {
            myIndexWriter = null;
            myDirectory = null;
        }
    }

    public synchronized void deleteLuceneIndex() throws IOException {
        myTrackBuffer.clear();
        getIndexWriter().deleteAll();
        getIndexWriter().commit();
    }

    /**
     * Create a document for a track.
     *
     * @param track A track to create a document from.
     * @return Document for the track.
     */
    private Document createTrackDocument(LuceneTrack track) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating lucene document for track \"" + track + "\".");
        }
        Document document = new Document();
        document.add(new StoredField("id", track.getId()));
        document.add(new StoredField("source_id", track.getSourceId()));
        document.add(new StringField("filename", StringUtils.lowerCase(track.getFilename()), Field.Store.NO));
        if (StringUtils.isNotBlank(track.getName())) {
            document.add(new TextField("name", StringUtils.lowerCase(track.getName()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getAlbum())) {
            document.add(new TextField("album", StringUtils.lowerCase(track.getAlbum()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getArtist())) {
            document.add(new TextField("artist", StringUtils.lowerCase(track.getArtist()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getSeries())) {
            document.add(new TextField("series", StringUtils.lowerCase(track.getSeries()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getComment())) {
            document.add(new TextField("comment", StringUtils.lowerCase(track.getComment()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getAlbumArtist())) {
            document.add(new TextField("album_artist", StringUtils.lowerCase(track.getAlbumArtist()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getGenre())) {
            document.add(new TextField("genre", StringUtils.lowerCase(track.getGenre()), Field.Store.NO));
        }
        if (StringUtils.isNotBlank(track.getComposer())) {
            document.add(new TextField("composer", StringUtils.lowerCase(track.getComposer()), Field.Store.NO));
        }
        return document;
    }

    public synchronized void updateTrack(LuceneTrack track) {
        myTrackBuffer.add(track);
        if (myTrackBuffer.size() >= MAX_TRACK_BUFFER) {
            flushTrackBuffer();
        }
    }

    public synchronized void flushTrackBuffer() {
        StopWatch.start("Indexing " + myTrackBuffer.size() + " tracks");
        try {
            for (LuceneTrack track = myTrackBuffer.peek(); track != null; track = myTrackBuffer.peek()) {
                Document document = createTrackDocument(track);
                if (track.isAdd()) {
                    getIndexWriter().addDocument(document);
                } else if (track.isUpdate()) {
                    getIndexWriter().updateDocument(new Term("id", track.getId()), document);
                } else {
                    LOGGER.warn("Lucence track type \"" + track.getClass().getName() + "\" is neither add nor update.");
                }
                myTrackBuffer.pop(); // element done
            }
            getIndexWriter().commit();
        } catch (CorruptIndexException e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            try {
                deleteLuceneIndex();
            } catch (IOException e1) {
                LOGGER.error("Could not delete corrupted lucene index.", e1);
            }
            shutdown();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
        } finally {
            StopWatch.stop();
        }
    }

    public synchronized void deleteTracksForSourceIds(Collection<String> sourceIds) {
        flushTrackBuffer();
        StopWatch.start("Removing tracks for " + sourceIds.size() + " data sources from index");
        try {
            for (String sourceId : sourceIds) {
                getIndexWriter().deleteDocuments(new Term("source_id", sourceId));
                getIndexWriter().commit();
            }
        } catch (CorruptIndexException e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            try {
                deleteLuceneIndex();
            } catch (IOException e1) {
                LOGGER.error("Could not delete corrupted lucene index.", e1);
            }
            shutdown();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
        } finally {
            StopWatch.stop();
        }
    }

    public synchronized void deleteTracksForIds(Collection<String> trackIds) throws IOException {
        flushTrackBuffer();
        try {
            LOGGER.info("Removing " + trackIds.size() + " tracks from index.");
            long start = System.currentTimeMillis();
            for (String trackId : trackIds) {
                getIndexWriter().deleteDocuments(new Term("id", trackId));
                getIndexWriter().commit();
            }
            LOGGER.info("Finished removing " + trackIds.size() + " tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
        } catch (RuntimeException e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
        }
    }

    public List<ScoredTrack> searchTracks(String[] searchTerms, int fuzziness, int maxResults) throws IOException {
        Directory directory = null;
        IndexReader indexReader = null;
        IndexSearcher indexSearcher = null;
        Collection<ScoredTrack> trackIds;
        try {
            directory = getDirectory();
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            Query luceneQuery = createQuery(searchTerms, fuzziness);
            TopDocs topDocs = indexSearcher.search(luceneQuery, null, maxResults, Sort.RELEVANCE, true, true);
            trackIds = new LinkedHashSet<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(new ScoredTrack(indexSearcher.doc(scoreDoc.doc).get("id"), scoreDoc.score));
            }
            LOGGER.debug("Lucene query returned " + trackIds.size() + " tracks.");
            return new ArrayList<>(trackIds);
        } finally {
            close(directory, indexReader);
        }
    }

    public List<ScoredTrack> searchTracks(String searchExpression, int maxResults) throws IOException, LuceneQueryParserException {
        Directory directory = null;
        IndexReader indexReader = null;
        IndexSearcher indexSearcher = null;
        try {
            directory = getDirectory();
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            Query luceneQuery = null;
            try {
                WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
                whitespaceAnalyzer.setVersion(Version.LUCENE_5_3_1);
                QueryParser parser = new QueryParser("name", whitespaceAnalyzer);
                parser.setAllowLeadingWildcard(true);
                luceneQuery = parser.parse(searchExpression);
            } catch (ParseException | RuntimeException e) {
                throw new LuceneQueryParserException("Could not parse query string.", e);
            }
            TopDocs topDocs = indexSearcher.search(luceneQuery, null, maxResults, Sort.RELEVANCE, true, true);
            Collection<ScoredTrack> trackIds = new LinkedHashSet<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(new ScoredTrack(indexSearcher.doc(scoreDoc.doc).get("id"), scoreDoc.score));
            }
            return new ArrayList<>(trackIds);
        } finally {
            close(directory, indexReader);
        }
    }

    private Query createQuery(String[] searchTerms, int fuzziness) {
        String[] fields = {"name", "album", "artist", "series", "comment", "album_artist", "composer"};
        BooleanQuery finalQuery = new BooleanQuery();
        BooleanQuery searchTermAndQuery = new BooleanQuery();
        for (String searchTerm : searchTerms) {
            String escapedSearchTerm = QueryParser.escape(searchTerm);
            BooleanQuery termOrQuery = new BooleanQuery();
            for (String field : fields) {
                Query query = new TermQuery(new Term(field, escapedSearchTerm));
                query.setBoost(5000f);
                termOrQuery.add(query, BooleanClause.Occur.SHOULD);
                query = new WildcardQuery(new Term(field, "*" + escapedSearchTerm + "*"));
                query.setBoost(1000f);
                termOrQuery.add(query, BooleanClause.Occur.SHOULD);
                if (fuzziness > 0) {
                    query = new SlowFuzzyQuery(new Term(field, escapedSearchTerm), ((float) (100 - fuzziness)) / 100f);
                    termOrQuery.add(query, BooleanClause.Occur.SHOULD);
                }
            }
            searchTermAndQuery.add(termOrQuery, BooleanClause.Occur.MUST);
        }
        finalQuery.add(searchTermAndQuery, BooleanClause.Occur.SHOULD);
        if (searchTerms.length > 1) {
            for (String field : fields) {
                PhraseQuery phraseQuery = new PhraseQuery();
                for (String searchTerm : searchTerms) {
                    String escapedSearchTerm = QueryParser.escape(searchTerm);
                    phraseQuery.add(new Term(field, escapedSearchTerm));
                }
                phraseQuery.setBoost(10000f);
                finalQuery.add(phraseQuery, BooleanClause.Occur.SHOULD);
            }
        }
        LOGGER.debug("QUERY for \"" + StringUtils.join(searchTerms, " ") + "\" (fuzziness=" + fuzziness + "): " + finalQuery);
        return finalQuery;
    }

    public Collection<ScoredTrack> searchTracks(Collection<SmartInfo> smartInfos, int fuzziness, int maxResults) throws IOException {
        Directory directory = null;
        IndexReader indexReader = null;
        IndexSearcher indexSearcher = null;
        Collection<ScoredTrack> trackIds;
        try {
            directory = getDirectory();
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
            Query luceneQuery = createQuery(smartInfos, fuzziness);
            TopDocs topDocs = indexSearcher.search(luceneQuery, maxResults);
            trackIds = new LinkedHashSet<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(new ScoredTrack(indexSearcher.doc(scoreDoc.doc).get("id"), scoreDoc.score));
            }
            return trackIds;
        } finally {
            close(directory, indexReader);
        }
    }

    private void close(Directory directory, IndexReader indexReader) {
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (IOException e) {
                LOGGER.warn("Could not close index reader.", e);
            }
        }
        if (directory != null) {
            try {
                directory.close();
            } catch (IOException e) {
                LOGGER.warn("Could not close directory.", e);
            }
        }
    }

    private Query createQuery(Collection<SmartInfo> smartInfos, int fuzziness) {
        BooleanQuery andQuery = new BooleanQuery();
        boolean invertedOnly = true;
        for (SmartInfo smartInfo : smartInfos) {
            switch (smartInfo.getFieldType()) {
                case album:
                    BooleanQuery orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "album", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly &= smartInfo.isInvert();
                    break;
                case artist:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        for (String term : StringUtils.split(orTerm)) {
                            BooleanQuery innerOrQuery = new BooleanQuery();
                            addToOrQuery(innerOrQuery, "artist", StringUtils.lowerCase(term), fuzziness);
                            addToOrQuery(innerOrQuery, "album_artist", StringUtils.lowerCase(term), fuzziness);
                            innerAndQuery.add(innerOrQuery, BooleanClause.Occur.MUST);
                        }
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly &= smartInfo.isInvert();
                    break;
                case comment:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "comment", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                case composer:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "composer", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                case file:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "filename", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                case genre:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "genre", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                case title:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "name", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                case tvshow:
                    orQuery = new BooleanQuery();
                    for (String orTerm : StringUtils.split(smartInfo.getPattern(), "|")) {
                        BooleanQuery innerAndQuery = new BooleanQuery();
                        addToAndQuery(innerAndQuery, "series", false, StringUtils.lowerCase(orTerm), fuzziness);
                        orQuery.add(innerAndQuery, BooleanClause.Occur.SHOULD);
                    }
                    andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    invertedOnly |= smartInfo.isInvert();
                    break;
                default:
                    // nothing to add to query in other cases
            }
        }
        if (invertedOnly) {
            // add a dummy query
            andQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        }
        LOGGER.debug("QUERY for \"" + StringUtils.join(smartInfos, " ") + "\" (fuzziness=" + fuzziness + "): " + andQuery);
        return andQuery;
    }

    private void addToAndQuery(BooleanQuery query, String field, boolean not, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            for (String term : StringUtils.split(pattern)) {
                if (fuzziness > 0) {
                    query.add(new SlowFuzzyQuery(new Term(field, QueryParser.escape(term)), ((float) (100 - fuzziness)) / 100f), not ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                } else {
                    query.add(new WildcardQuery(new Term(field, "*" + QueryParser.escape(term) + "*")), not ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                }
            }
        }
    }

    private void addToOrQuery(BooleanQuery query, String field, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            if (fuzziness > 0) {
                query.add(new SlowFuzzyQuery(new Term(field, QueryParser.escape(pattern)), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.SHOULD);
            } else {
                query.add(new WildcardQuery(new Term(field, "*" + QueryParser.escape(pattern) + "*")), BooleanClause.Occur.SHOULD);
            }
        }
    }
}
