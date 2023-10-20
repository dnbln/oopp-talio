package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TagTest {

    private final Tag tag = new Tag("urgent", "red");


    @Test
    void testGetId() {
        assertEquals(0, tag.getId());
    }

    @Test
    void testGetName() {
        assertEquals("urgent", tag.getName());
    }

    @Test
    void testGetColor() {
        assertEquals("red", tag.getFontColor());
    }

    @Test
    void testEquals() {
        Tag sameTag = new Tag("urgent", "red");
        assertEquals(tag, sameTag);
    }

    @Test
    void testNotEquals() {
        Tag differentTag = new Tag("important", "red");
        assertNotEquals(tag, differentTag);
    }

    @Test
    void testHashCode() {
        Tag sameTag = new Tag("urgent", "red");
        assertEquals(tag.hashCode(), sameTag.hashCode());
    }

    @Test
    void testToString() {
        assertEquals("Tag{id=0, name='urgent', fontColor='red', backgroundColor='null', boardId=0}", tag.toString());
    }
}
