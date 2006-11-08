package de.codewave.mytunesrss;

import de.codewave.utils.servlet.ServletUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2006<br>
 * <b>Company:</b>       daGama Business Travel GmbH<br>
 * <b>Creation Date:</b> 08.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssWebUtils {
  public static String getServletUrl(HttpServletRequest request) {
    return ServletUtils.getApplicationUrl(request) + "/mytunesrss";
  }
}



