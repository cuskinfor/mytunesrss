/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.StreamSender;

import java.io.IOException;

public class DownloadPhotoCommandHandler extends ShowPhotoCommandHandler {

    @Override
    protected void sendResponse(StreamSender sender, String filename) throws IOException {
        getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }
}
