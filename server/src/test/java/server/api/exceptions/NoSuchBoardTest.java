package server.api.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchBoardTest {

    @Test
    void testConstructor() {
        NoSuchBoard nsb = new NoSuchBoard(1);
        Assertions.assertNotNull(nsb);
    }

    @Test
    void getId() {
        NoSuchBoard nsb = new NoSuchBoard(1);
        Assertions.assertEquals(1, nsb.getId());
    }
}
