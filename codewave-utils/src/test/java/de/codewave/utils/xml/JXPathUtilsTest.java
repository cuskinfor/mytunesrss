package de.codewave.utils.xml;

import junit.framework.*;
import org.apache.commons.jxpath.*;

/**
 * de.codewave.utils.xml.JXPathUtilsTest
 */
public class JXPathUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(JXPathUtilsTest.class);
    }

    private JXPathContext myTestContext;

    public JXPathUtilsTest(String name) {
        super(name);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myTestContext = JXPathUtils.getContext(getClass().getResource("test.xml"));
    }

    public void testGetStringValue() {
        assertEquals("Hello World", JXPathUtils.getStringValue(myTestContext, "/test/string", null));
        assertEquals("Hello World", JXPathUtils.getStringValue(myTestContext, "/test/illegal", "Hello World"));
    }

    public void testGetIntValue() {
        assertEquals(24021972, JXPathUtils.getIntValue(myTestContext, "/test/int", 0));
        assertEquals(24021972, JXPathUtils.getIntValue(myTestContext, "/test/illegal", 24021972));
    }

    public void testGetDoubleValue() {
        assertEquals(3.141593, JXPathUtils.getDoubleValue(myTestContext, "/test/double", 0));
        assertEquals(3.141593, JXPathUtils.getDoubleValue(myTestContext, "/test/illegal", 3.141593));
    }

    public void testGetClassValue() throws ClassNotFoundException {
        assertEquals(JXPathUtils.class, JXPathUtils.getClassValue(myTestContext, "/test/class", null));
        assertEquals(JXPathUtils.class, JXPathUtils.getClassValue(myTestContext, "/test/illegal", JXPathUtils.class));
    }

    public void testGetByteArray() throws ClassNotFoundException {
        assertEquals("hello world", new String(JXPathUtils.getByteArray(myTestContext, "/test/base64", null)));
        assertEquals("hello world", new String(JXPathUtils.getByteArray(myTestContext, "/test/illegal", "hello world".getBytes())));
    }

    public void testGetEncoding() {
        assertEquals("ISO-8859-1", JXPathUtils.getXmlEncoding("<?xml version =\"1.0\" encoding=\"ISO-8859-1\"?>\n<root>\n<something test=\"dummy\"/>\n</root>"));
        assertEquals("UTF-16", JXPathUtils.getXmlEncoding("<?xml version =\"1.0\" encoding=\"UTF-16\"?>\n<root>\n<something test=\"dummy\"/>\n</root>"));
        assertEquals(System.getProperty("file.encoding"), JXPathUtils.getXmlEncoding("<?xml version =\"1.0\"?>\n<root>\n<something test=\"dummy\"/>\n</root>"));
    }
}