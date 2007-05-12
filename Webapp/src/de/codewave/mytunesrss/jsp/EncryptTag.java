package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.*;
import org.apache.commons.logging.*;

import javax.crypto.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;
import java.security.*;

/**
 * de.codewave.mytunesrss.jsp.FlipFlopTag
 */
public class EncryptTag extends BodyTagSupport {
    private static final Log LOG = LogFactory.getLog(EncryptTag.class);

    @Override
    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().write(encryptPathInfo(getBodyContent().getString()));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not encrypt path info.", e);
            }
        }
        return Tag.EVAL_PAGE;
    }

    private String encryptPathInfo(String pathInfo) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, UnsupportedEncodingException {
        if ("true".equalsIgnoreCase(System.getProperty("encryptPathInfo")) && MyTunesRss.CONFIG.getPathInfoKey() != null) {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
            return MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8")));
        }
        return pathInfo;
    }
}