package commons.patchExceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchCardTest {

    @Test
    void testConstructor() {
        NoSuchCard nsc = new NoSuchCard(1L);
        Assertions.assertNotNull(nsc);
    }

    @Test
    void testGetCardId() {
        NoSuchCard nsc = new NoSuchCard(1L);
        Assertions.assertEquals(1L, nsc.getCardId());
    }
}
