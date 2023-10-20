package commons.patchExceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchSubtaskTest {

    @Test
    void testConstructor() {
        NoSuchSubtask nss = new NoSuchSubtask(1L);
        Assertions.assertNotNull(nss);
    }

    @Test
    void testGetSubtaskId() {
        NoSuchSubtask nss = new NoSuchSubtask(1L);
        Assertions.assertEquals(1L, nss.getSubtaskId());
    }
}
