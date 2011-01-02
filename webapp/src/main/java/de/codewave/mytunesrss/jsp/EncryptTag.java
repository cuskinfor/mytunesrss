package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.MyTunesRssWebUtils;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.jsp.FlipFlopTag
 */
public class EncryptTag extends BodyTagSupport {
    private SecretKey myKey;

    public void setKey(SecretKey key) {
        myKey = key;
    }

    @Override
    public int doStartTag() {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(MyTunesRssWebUtils.encryptPathInfo((HttpServletRequest) pageContext.getRequest(),
                    getBodyContent().getString()));
        } catch (IOException e) {
            throw new JspException(e);
        }
        return Tag.EVAL_PAGE;
    }
}