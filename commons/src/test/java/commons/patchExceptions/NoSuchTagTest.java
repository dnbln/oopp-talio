package commons.patchExceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchTagTest {

    @Test
    void testConstructor() {
        NoSuchTag nst = new NoSuchTag(1L);
        Assertions.assertNotNull(nst);
    }

    @Test
    void testGetTagId() {
        NoSuchTag nst = new NoSuchTag(1L);
        Assertions.assertEquals(1L, nst.getTagId());
    }
}
