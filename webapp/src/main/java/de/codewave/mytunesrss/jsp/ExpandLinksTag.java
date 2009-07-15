package de.codewave.mytunesrss.jsp;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * de.codewave.mytunesrss.jsp.ExpandLinksTag
 */
public class ExpandLinksTag extends BodyTagSupport {

    private static final Pattern TAG_BEGIN = Pattern.compile("\\[(a .+?)\\]", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE);

    private static final Pattern TAG_END = Pattern.compile("\\[(/a)\\]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @Override
    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            String bodyText = getBodyContent().getString();
            bodyText = replace(bodyText);
            pageContext.getOut().write(bodyText);
        } catch (IOException e) {
            throw new JspException(e);
        }
        return Tag.EVAL_PAGE;
    }

    static String replace(String bodyText) {
        Matcher matcher = TAG_BEGIN.matcher(bodyText);
        bodyText = matcher.replaceAll("<$1>");
        matcher = TAG_END.matcher(bodyText);
        bodyText = matcher.replaceAll("<$1>");
        return StringEscapeUtils.unescapeXml(bodyText);
    }
}
