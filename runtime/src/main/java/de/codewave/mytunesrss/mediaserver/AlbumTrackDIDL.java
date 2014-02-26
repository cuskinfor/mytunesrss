package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class AlbumTrackDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user, String oidParams) {
        return createMusicTrack(
                user,
                track,
                ObjectID.AlbumTrack.getValue() + ";" + encode(track.getId()),
                ObjectID.Album.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist())
        );
    }

}
