/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class PreferencesDIDL extends MyTunesRssDIDL {


    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        addContainer(createSimpleContainer(ObjectID.PrefsPhotoSize.getValue(), ObjectID.Preferences.getValue(), "Photos Size", PrefsPhotoSizeDIDL.PHOTO_SIZES.length));
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        addContainer(createSimpleContainer(ObjectID.PrefsPhotoSize.getValue(), ObjectID.Preferences.getValue(), "Photos Size", PrefsPhotoSizeDIDL.PHOTO_SIZES.length));
    }

    @Override
    long getTotalMatches() {
        return 1;
    }

}
