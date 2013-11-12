package de.codewave.mytunesrss.lucene;

public class UpdateLuceneTrack extends LuceneTrack {
    @Override
    boolean isAdd() {
        return false;
    }

    @Override
    boolean isUpdate() {
        return true;
    }
}
