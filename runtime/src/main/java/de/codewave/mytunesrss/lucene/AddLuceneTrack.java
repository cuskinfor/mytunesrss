package de.codewave.mytunesrss.lucene;

public class AddLuceneTrack extends LuceneTrack {
    @Override
    boolean isAdd() {
        return true;
    }

    @Override
    boolean isUpdate() {
        return false;
    }
}
