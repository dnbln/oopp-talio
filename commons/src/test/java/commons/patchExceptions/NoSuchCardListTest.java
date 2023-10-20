package commons.patchExceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchCardListTest {

    @Test
    void testConstructor() {
        NoSuchCardList nscl = new NoSuchCardList(1L);
        Assertions.assertNotNull(nscl);
    }

    @Test
    void testGetCardListId() {
        NoSuchCardList nscl = new NoSuchCardList(1L);
        Assertions.assertEquals(1L, nscl.getCardListId());
    }
}
