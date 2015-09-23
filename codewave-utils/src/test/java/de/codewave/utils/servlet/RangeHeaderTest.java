package de.codewave.utils.servlet;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class RangeHeaderTest {
    @Test
    public void testRangeFromAndTo() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://127.0.0.1:8080/mytunesrss/playTrackCommandHandler");
        request.addHeader("Range", "bytes=100-200");
        RangeHeader rangeHeader = new RangeHeader(request);
        assertEquals(100L, rangeHeader.getRangeFrom());
        assertEquals(200L, rangeHeader.getRangeTo());
        assertEquals(49L, rangeHeader.getFirstByte(50L));
        assertEquals(100L, rangeHeader.getFirstByte(150L));
        assertEquals(200L, rangeHeader.getLastByte(300L));
        assertEquals(169L, rangeHeader.getLastByte(170L));
    }

    @Test
    public void testRangeFrom() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://127.0.0.1:8080/mytunesrss/playTrackCommandHandler");
        request.addHeader("Range", "bytes=100-");
        RangeHeader rangeHeader = new RangeHeader(request);
        assertEquals(100L, rangeHeader.getRangeFrom());
        assertEquals(-1L, rangeHeader.getRangeTo());
        assertEquals(49L, rangeHeader.getFirstByte(50L));
        assertEquals(100L, rangeHeader.getFirstByte(150L));
        assertEquals(299L, rangeHeader.getLastByte(300L));
    }

    @Test
    public void testRangeTo() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://127.0.0.1:8080/mytunesrss/playTrackCommandHandler");
        request.addHeader("Range", "bytes=-200");
        RangeHeader rangeHeader = new RangeHeader(request);
        assertEquals(-1L, rangeHeader.getRangeFrom());
        assertEquals(200L, rangeHeader.getRangeTo());
        assertEquals(0L, rangeHeader.getFirstByte(50L));
        assertEquals(50L, rangeHeader.getFirstByte(250L));
    }
}
