package commons.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubscribeToBoardTest {
    @Test
    void serializes() throws JsonProcessingException {
        Assertions.assertEquals("""
                {"type":"subscribeToBoard","board":0}
                """.trim(), SubscribeToBoard.UNSUBSCRIBE.serialize());
        Assertions.assertEquals("""
                {"type":"subscribeToBoard","board":1}
                """.trim(), new SubscribeToBoard(1).serialize());
    }

    @Test
    void deserializes() throws JsonProcessingException {
        Assertions.assertEquals(SubscribeToBoard.UNSUBSCRIBE, ClientToServerEvent.deserialize("""
                {"type":"subscribeToBoard","board":0}
                """.trim()));
        Assertions.assertEquals(new SubscribeToBoard(1), ClientToServerEvent.deserialize("""
                {"type":"subscribeToBoard","board":1}
                """.trim()));
    }
}
