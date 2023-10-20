package commons.patchExceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NoSuchColorPresetTest {

    @Test
    void testConstructor() {
        NoSuchColorPreset nscp = new NoSuchColorPreset(1L);
        Assertions.assertNotNull(nscp);
    }

    @Test
    void testGetPresetId() {
        NoSuchColorPreset nscp = new NoSuchColorPreset(1L);
        Assertions.assertEquals(1L, nscp.getPresetId());
    }
}
