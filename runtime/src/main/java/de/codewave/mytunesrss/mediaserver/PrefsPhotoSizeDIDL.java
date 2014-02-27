/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.SortCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class PrefsPhotoSizeDIDL extends PreferencesDIDL {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrefsPhotoSizeDIDL.class);
    private static final int DEFAULT_PHOTO_SIZE = 1024;
    static final long[] PHOTO_SIZES = new long[]{256, 512, 1024, 2048, 4096, 0};
    static AtomicInteger selectedPhotoSize = new AtomicInteger(DEFAULT_PHOTO_SIZE);

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        if (StringUtils.isNotBlank(oidParams)) {
            try {
                selectedPhotoSize.set(Integer.parseInt(oidParams));
            } catch (NumberFormatException e) {
                LOGGER.warn("Could not parse photo size from oid params.", e);
                selectedPhotoSize.set(DEFAULT_PHOTO_SIZE);
            }
        }
        createPhotoSizesMenu();
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        createPhotoSizesMenu();
    }

    @Override
    long getTotalMatches() {
        return PHOTO_SIZES.length;
    }

    private void createPhotoSizesMenu() {
        for (long photoSize : PHOTO_SIZES) {
            String title = photoSize != 0 ? photoSize + " pixels" : "Original";
            addContainer(
                    createSimpleContainer(
                            ObjectID.PrefsPhotoSize.getValue() + ";" + photoSize,
                            ObjectID.PrefsPhotoSize.getValue(),
                            selectedPhotoSize.get() == photoSize ? "[X] " + title : title,
                            1
                    )
            );
        }
    }


}
