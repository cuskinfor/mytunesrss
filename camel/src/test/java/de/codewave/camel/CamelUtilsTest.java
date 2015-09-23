package de.codewave.camel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CamelUtilsTest {

    @Test
    public void testGetSetLong() {
        byte[] bytes = new byte[] {0x12, 0x34, 0x56, 0x78};
        assertEquals(0x12345678L, CamelUtils.getLongValue(bytes, 0, 4, false, Endianness.Big));
        CamelUtils.setLongValue(bytes, 0, 4, Endianness.Big, 0xAABCFFEDL);
        assertEquals(0xAABCFFEDL, CamelUtils.getLongValue(bytes, 0, 4, false, Endianness.Big));
    }

}
