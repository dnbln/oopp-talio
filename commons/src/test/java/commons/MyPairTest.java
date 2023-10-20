package commons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyPairTest {
    private MyPair<Integer, String> mp;

    @BeforeEach
    void setup() {
        mp = new MyPair<>(3, "a");
    }

    @Test
    void testConstructor() {
        Assertions.assertNotNull(mp);
    }

    @Test
    void testGetFirst() {
        Assertions.assertEquals(3, mp.getFirst());
    }

    @Test
    void testGetSecond() {
        Assertions.assertEquals("a", mp.getSecond());
    }
}
