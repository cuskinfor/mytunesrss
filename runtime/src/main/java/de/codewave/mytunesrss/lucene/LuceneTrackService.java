package de.codewave.mytunesrss.lucene;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.SmartInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * de.codewave.mytunesrss.lucene.LuceneTrackService
 */
public class LuceneTrackService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTrackService.class);
    private static final int MAX_TRACK_BUFFER = 5000;

    private Deque<LuceneTrack> myTrackBuffer = new ArrayDeque<LuceneTrack>();
    private FSDirectory myDirectory;
    private Analyzer myAnalyzer;
    private IndexWriter myIndexWriter;
    
    private Directory getDirectory() throws IOException {
        return FSDirectory.open(new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track"));
    }
    
    private synchronized IndexWriter getIndexWriter() throws IOException {
        if (myDirectory == null) {
            myDirectory = FSDirectory.open(new File(MyTunesRss.CACHE_DATA_PATH + "/lucene/track"));
        }
        if (myAnalyzer == null) {
            myAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
        }
        try {
            if (myIndexWriter == null) {
                myIndexWriter = new IndexWriter(myDirectory, myAnalyzer, false, new IndexWriter.MaxFieldLength(300));
            }
        } catch (IndexNotFoundException e) {
            LOGGER.warn("No lucene index found, creating a new one.", e);
            myIndexWriter = new IndexWriter(myDirectory, myAnalyzer, true, new IndexWriter.MaxFieldLength(300));
        }
        return myIndexWriter;
    }
    
    public synchronized void shutdown() {
        try {
            if (myIndexWriter != null) {
                try {
                    myIndexWriter.close();
                } catch (IOException e) {
                    LOGGER.error("Could not close index writer.");
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
        document.add(new Field("id", track.getId(), Field.Store.YES, Field.Index.NO));
        document.add(new Field("source_id", track.getSourceId(), Field.Store.YES, Field.Index.NO));
        document.add(new Field("name", StringUtils.lowerCase(track.getName()), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("album", StringUtils.lowerCase(track.getAlbum()), Field.Store.NO, Field.Index.ANALYZED));
        document.add(new Field("artist", StringUtils.lowerCase(track.getArtist()), Field.Store.NO, Field.Index.ANALYZED));
        if (StringUtils.isNotBlank(track.getSeries())) {
            document.add(new Field("series", StringUtils.lowerCase(track.getSeries()), Field.Store.NO, Field.Index.ANALYZED));
        }
        document.add(new Field("filename", StringUtils.lowerCase(track.getFilename()), Field.Store.NO, Field.Index.NOT_ANALYZED));
        if (StringUtils.isNotBlank(track.getComment())) {
            document.add(new Field("comment", StringUtils.lowerCase(track.getComment()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getAlbumArtist())) {
            document.add(new Field("album_artist", StringUtils.lowerCase(track.getAlbumArtist()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getGenre())) {
            document.add(new Field("genre", StringUtils.lowerCase(track.getGenre()), Field.Store.NO, Field.Index.ANALYZED));
        }
        if (StringUtils.isNotBlank(track.getComposer())) {
            document.add(new Field("composer", StringUtils.lowerCase(track.getComposer()), Field.Store.NO, Field.Index.ANALYZED));
        }
        return document;
    }

    public synchronized void updateTrack(LuceneTrack track) throws IOException {
        myTrackBuffer.add(track);
        if (myTrackBuffer.size() >= MAX_TRACK_BUFFER) {
            flushTrackBuffer();
        }
    }

    public synchronized void updateTracks(Collection<LuceneTrack> tracks) throws IOException {
        myTrackBuffer.addAll(tracks);
        if (myTrackBuffer.size() >= MAX_TRACK_BUFFER) {
            flushTrackBuffer();
        }
    }

    public synchronized void flushTrackBuffer() throws IOException {
        try {
            LOGGER.info("Indexing " + myTrackBuffer.size() + " tracks.");
            long start = System.currentTimeMillis();
            long doneCount = 0;
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
                doneCount++;
            }
            getIndexWriter().commit();
            LOGGER.info("Finished indexing " + doneCount + " tracks (duration: " + (System.currentTimeMillis() - start) + " ms).");
        } catch (Exception e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
        }
    }

    public synchronized void deleteTracksForSourceIds(Collection<String> sourceIds) throws IOException {
        flushTrackBuffer();
        try {
            LOGGER.info("Removing tracks for " + sourceIds.size() + " data sources from index.");
            long start = System.currentTimeMillis();
            for (String sourceId : sourceIds) {
                getIndexWriter().deleteDocuments(new Term("source_id", sourceId));
                getIndexWriter().commit();
            }
            LOGGER.info("Finished removing tracks for " + sourceIds.size() + " data sources (duration: " + (System.currentTimeMillis() - start) + " ms).");
        } catch (Exception e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
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
        } catch (Exception e) {
            LOGGER.error("Could not flush track buffer to lucene index.", e);
            shutdown();
        }
    }

    public List<String> searchTrackIds(String[] searchTerms, int fuzziness, int maxResults) throws IOException, ParseException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        List<String> trackIds;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            isearcher.setDefaultFieldSortScoring(true, true);
            Query luceneQuery = createQuery(searchTerms, fuzziness);
            TopDocs topDocs = isearcher.search(luceneQuery, maxResults);
            trackIds = new ArrayList<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    public List<String> searchTrackIds(String searchExpression, int maxResults) throws IOException, LuceneQueryParserException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            isearcher.setDefaultFieldSortScoring(true, true);
            Query luceneQuery = null;
            try {
                QueryParser parser = new QueryParser(Version.LUCENE_35, "name", new WhitespaceAnalyzer(Version.LUCENE_35));
                parser.setAllowLeadingWildcard(true);
                luceneQuery = parser.parse(searchExpression);
            } catch (ParseException e) {
                throw new LuceneQueryParserException("Could not parse query string.", e);
            } catch (Exception e) {
                throw new LuceneQueryParserException("Could not parse query string.", e);
            }
            TopDocs topDocs = isearcher.search(luceneQuery, maxResults);
            List<String> trackIds = new ArrayList<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    private Query createQuery(String[] searchTerms, int fuzziness) {
        String[] fields = {"name", "album", "artist", "series", "comment", "tags", "album_artist", "composer"};
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
                    query = new FuzzyQuery(new Term(field, escapedSearchTerm), ((float) (100 - fuzziness)) / 100f);
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

    public List<String> searchTrackIds(Collection<SmartInfo> smartInfos, int fuzziness, int maxResults) throws IOException, ParseException {
        Directory directory = null;
        IndexSearcher isearcher = null;
        List<String> trackIds;
        try {
            directory = getDirectory();
            isearcher = new IndexSearcher(IndexReader.open(directory));
            Query luceneQuery = createQuery(smartInfos, fuzziness);
            TopDocs topDocs = isearcher.search(luceneQuery, maxResults);
            trackIds = new ArrayList<String>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                trackIds.add(isearcher.doc(scoreDoc.doc).get("id"));
            }
            return trackIds;
        } finally {
            if (isearcher != null) {
                isearcher.close();
            }
            if (directory != null) {
                directory.close();
            }
        }
    }

    private Query createQuery(Collection<SmartInfo> smartInfos, int fuzziness) {
        BooleanQuery andQuery = new BooleanQuery();
        for (SmartInfo smartInfo : smartInfos) {
            switch (smartInfo.getFieldType()) {
                case album:
                    addToAndQuery(andQuery, "album", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case artist:
                    for (String term : StringUtils.split(smartInfo.getPattern())) {
                        BooleanQuery orQuery = new BooleanQuery();
                        addToOrQuery(orQuery, "artist", StringUtils.lowerCase(term), fuzziness);
                        addToOrQuery(orQuery, "album_artist", StringUtils.lowerCase(term), fuzziness);
                        andQuery.add(orQuery, smartInfo.isInvert() ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                    }
                    break;
                case comment:
                    addToAndQuery(andQuery, "comment", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case composer:
                    addToAndQuery(andQuery, "composer", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case file:
                    addToAndQuery(andQuery, "filename", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case genre:
                    addToAndQuery(andQuery, "genre", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case tag:
                    addToAndQuery(andQuery, "tags", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case title:
                    addToAndQuery(andQuery, "name", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
                case tvshow:
                    addToAndQuery(andQuery, "series", smartInfo.isInvert(), StringUtils.lowerCase(smartInfo.getPattern()), fuzziness);
                    break;
            }
        }
        return andQuery;
    }

    private void addToAndQuery(BooleanQuery query, String field, boolean not, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            for (String term : StringUtils.split(pattern)) {
                if (fuzziness > 0) {
                    query.add(new FuzzyQuery(new Term(field, QueryParser.escape(term)), ((float) (100 - fuzziness)) / 100f), not ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                } else {
                    query.add(new WildcardQuery(new Term(field, "*" + QueryParser.escape(term) + "*")), not ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
                }
            }
        }
    }

    private void addToOrQuery(BooleanQuery query, String field, String pattern, int fuzziness) {
        if (StringUtils.isNotEmpty(pattern)) {
            if (fuzziness > 0) {
                query.add(new FuzzyQuery(new Term(field, QueryParser.escape(pattern)), ((float) (100 - fuzziness)) / 100f), BooleanClause.Occur.SHOULD);
            } else {
                query.add(new WildcardQuery(new Term(field, "*" + QueryParser.escape(pattern) + "*")), BooleanClause.Occur.SHOULD);
            }
        }
    }
}
