package de.codewave.mytunesrss.datastore.statement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VirtualGenre extends Genre {

    private Set<String> myRealGenreNames = new HashSet<String>();

    public VirtualGenre(String name, Genre genre) {
        setName(name);
        setHidden(genre.isHidden());
        addRealGenre(genre);
    }

    public Collection<String> getRealGenreNames() {
        return new HashSet<String>(myRealGenreNames);
    }

    public void addRealGenre(Genre genre) {
        if (myRealGenreNames.add(genre.getName())) {
            setAlbumCount(getAlbumCount() + genre.getAlbumCount());
            setArtistCount(getArtistCount() + genre.getArtistCount());
            setTrackCount(getTrackCount() + genre.getTrackCount());
            setHidden(isHidden() && genre.isHidden());
        }
    }

}
