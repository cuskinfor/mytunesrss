package de.codewave.utils.xml;

import org.apache.commons.codec.binary.*;

import org.apache.commons.lang3.*;
import org.w3c.dom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import java.io.*;

/**
 * de.codewave.utils.xml.DOMUtils
 */
public class DOMUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DOMUtils.class);

    public static Element createTextElement(Document document, String tagName, String content) {
        Element element = document.createElement(tagName);
        if (StringUtils.isNotEmpty(content)) {
            element.appendChild(document.createTextNode(content));
        }
        return element;
    }

    public static Element createByteArrayElement(Document document, String tagName, byte[] content) {
        try {
            return createTextElement(document, tagName, new String(Base64.encodeBase64(content), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found!");
        }
    }

    public static Element createLongElement(Document document, String tagName, long content) {
        return createTextElement(document, tagName, Long.toString(content));
    }

    public static Element createIntElement(Document document, String tagName, int content) {
        return createTextElement(document, tagName, Integer.toString(content));
    }

    public static Element createBooleanElement(Document document, String tagName, boolean content) {
        return createTextElement(document, tagName, Boolean.toString(content));
    }

    public static Element createClassElement(Document document, String tagName, Class content) {
        return createTextElement(document, tagName, content.getName());
    }

    public static void prettyPrint(Document document, OutputStream outputStream) {
        try {
            Source source = new DOMSource(document);
            Result target = new StreamResult(outputStream);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, target);
        } catch (TransformerException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not pretty print xml.", e);
            }
        }
    }
}
