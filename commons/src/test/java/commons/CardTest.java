package commons;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardTest {

    // Creating new instances of Cards to use later in the tests
    private final ZonedDateTime newDate = ZonedDateTime.now();
    private final Card emptyCard = new Card();
    private final Card onlyTitleCard = new Card("To Do");
    private final Card fullCard = new Card("Doing", "Description", "Assignment", newDate, new HashSet<>());


    @Test
    void testFirstConstructor() {
        assertEquals("", emptyCard.getTitle());
        assertEquals("", emptyCard.getText());
        assertEquals("", emptyCard.getCategory());
        assertNull(emptyCard.getDueDate());
    }

    @Test
    void testSecondConstructor() {
        assertEquals("To Do", onlyTitleCard.getTitle());
        assertEquals("", onlyTitleCard.getText());
        assertEquals("", onlyTitleCard.getCategory());
        assertNull(onlyTitleCard.getDueDate());
    }

    @Test
    void testThirdConstructor() {
        assertEquals("Doing", fullCard.getTitle());
        assertEquals("Description", fullCard.getText());
        assertEquals("Assignment", fullCard.getCategory());
        assertNotNull(fullCard.getDueDate());
    }

    @Test
    void testGetId() {
        assertEquals(0, fullCard.getId());
    }

    @Test
    void testGetTitle() {
        assertEquals("Doing", fullCard.getTitle());
    }

    @Test
    void testSetTitle() {
        fullCard.patch(patcher -> patcher.setTitle("Done"));
        assertEquals("Done", fullCard.getTitle());
    }

    @Test
    void testGetText() {
        assertEquals("Description", fullCard.getText());
    }

    @Test
    void testSetText() {
        fullCard.patch((patcher) -> patcher.setText("new description"));
        assertEquals("new description", fullCard.getText());
    }

    @Test
    void testGetCategory() {
        assertEquals("Assignment", fullCard.getCategory());
    }

    @Test
    void testSetCategory() {
        fullCard.patch(patcher -> patcher.setCategory("Homework"));
        assertEquals("Homework", fullCard.getCategory());
    }

    @Test
    void testGetDueDate() {
        assertNotNull(fullCard.getDueDate());
    }

    @Test
    void testSetDueDate() {
        ZonedDateTime newDueDate = ZonedDateTime.now();
        fullCard.patch(patcher -> patcher.setDueDate(newDueDate));
        assertEquals(newDueDate, fullCard.getDueDate());
    }


    @Test
    void testEquals() {
        Card sameCard = new Card("Doing", "Description", "Assignment", fullCard.getDueDate(), new HashSet<>());
        assertEquals(fullCard, sameCard);
    }

    @Test
    void testNotEquals() {
        Card differentCard = new Card("To Do", "Description", "Assignment", fullCard.getDueDate(), new HashSet<>());
        assertNotEquals(fullCard, differentCard);
    }

    @Test
    void testHashCode() {
        Card sameCard = new Card("Doing", "Description", "Assignment",
                fullCard.getDueDate(), new HashSet<>());
        assertEquals(fullCard.hashCode(), sameCard.hashCode());
    }

    @Test
    void testToString() {
        String expected = "Card{id=0, title='Doing', text='Description', category='Assignment', dueDate="
                + fullCard.getDueDate() + ", tags=[]}";
        assertEquals(expected, fullCard.toString());
    }
}

