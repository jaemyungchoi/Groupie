package csci310.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class CanAttendTest {

    @Test
    public void testFromInt() {
        assertEquals(CanAttend.NONE, CanAttend.fromInt(CanAttend.NONE.getCode()));
        assertEquals(CanAttend.YES, CanAttend.fromInt(CanAttend.YES.getCode()));
        assertEquals(CanAttend.NO, CanAttend.fromInt(CanAttend.NO.getCode()));
        assertEquals(CanAttend.MAYBE, CanAttend.fromInt(CanAttend.MAYBE.getCode()));
        assertNull(CanAttend.fromInt(-1));
    }
}