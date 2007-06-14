package de.codewave.mytunesrss.servlet;

import org.apache.commons.io.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.regex.*;
import java.util.*;

import de.codewave.utils.servlet.*;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Cologne Systems GmbH<br>
 * <b>Creation Date:</b> 14.06.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
public class ScaledStyleServlet extends HttpServlet {
    private Pattern myPattern = Pattern.compile("(\\D|^)([\\d]+\\.[\\d]+|[\\d]+)(em|ex|px|in|cm|mm|pt|pc)(;|\\s|\\})");
    private Map<String, String> myStyleCache = new HashMap<String, String>();

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String styleFile = ServletUtils.getServletMapping(getServletContext(), getClass()).replace("/*", "") + httpServletRequest.getPathInfo();
        String styleFilePath = getServletContext().getRealPath(styleFile);
        String factorParameter = httpServletRequest.getParameter("factor");
        if (StringUtils.isEmpty(factorParameter)) {
            factorParameter = "1";
        }
        String styleCacheKey = styleFilePath + ":" + factorParameter;
        String styleSheet = myStyleCache.get(styleCacheKey);
        if (styleSheet == null) {
            styleSheet = FileUtils.readFileToString(new File(styleFilePath), "UTF-8");
            Matcher matcher = myPattern.matcher(styleSheet);
            Map<String, String> replacements = new HashMap<String, String>();
            double factor;
            try {
                factor = Double.parseDouble(factorParameter);
            } catch (NumberFormatException e) {
                factor = 1.0;
            }
            while (matcher.find()) {
                replacements.put(matcher.group(), matcher.group(1) + (Double.parseDouble(matcher.group(2)) * factor) + matcher.group(3) + matcher.group(4));
            }
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                styleSheet = styleSheet.replace(entry.getKey(), entry.getValue());
            }
            myStyleCache.put(styleCacheKey, styleSheet);
        }
        httpServletResponse.setContentType("text/css");
        httpServletResponse.setContentLength(styleSheet.length());
        IOUtils.copy(new StringReader(styleSheet), httpServletResponse.getWriter());
    }
}



