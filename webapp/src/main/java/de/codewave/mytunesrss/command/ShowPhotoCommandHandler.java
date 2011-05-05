/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class ShowPhotoCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        final String id = getRequestParameter("photo", null);
        if (StringUtils.isNotBlank(id)) {
            String filename = getTransaction().executeQuery(new DataStoreQuery<DataStoreQuery.QueryResult<String>>() {
                @Override
                public QueryResult<String> execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getPhoto");
                    statement.setString("id", id);
                    return execute(statement, new ResultBuilder<String>() {
                        public String create(ResultSet resultSet) throws SQLException {
                            return resultSet.getString("file");
                        }
                    });
                }
            }).getResult(0);
            File photoFile = new File(filename);
            if (StringUtils.isNotBlank(filename) && photoFile.isFile()) {
                FileSender sender = new FileSender(photoFile, "image/" + StringUtils.lowerCase(FilenameUtils.getExtension(filename), Locale.ENGLISH), photoFile.length());
                sender.setCounter((StreamSender.ByteSentCounter) SessionManager.getSessionInfo(getRequest()));
                sender.sendGetResponse(getRequest(), getResponse(), false);
            } else {
                getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
