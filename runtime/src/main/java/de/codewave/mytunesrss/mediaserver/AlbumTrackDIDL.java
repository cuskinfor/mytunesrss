package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class AlbumTrackDIDL extends MusicTrackDIDL {

    @Override
    protected Item createTrackItem(Track track, User user) {
        return createMusicTrackItem(track, user);
    }

    @Override
    protected String getParentId(Track track) {
        return ObjectID.Album.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist());
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.AlbumTrack.getValue() + ";" + encode(track.getId());
    }

}
