package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.crypto.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.jsp.FlipFlopTag
 */
public class EncryptTag extends BodyTagSupport {
    private static final Log LOG = LogFactory.getLog(EncryptTag.class);

    private SecretKey myKey;

    public void setKey(SecretKey key) {
        myKey = key;
    }

    @Override
    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(MyTunesRssWebUtils.encryptPathInfo(getBodyContent().getString()));
        } catch (IOException e) {
            throw new JspException(e);
        }
        return Tag.EVAL_PAGE;
    }
}