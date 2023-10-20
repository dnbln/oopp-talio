package commons.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents a client to server event that can be serialized into and deserialized from a JSON string.
 * It uses Jackson annotations for JSON handling.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(@JsonSubTypes.Type(value = SubscribeToBoard.class, name = "subscribeToBoard"))
public abstract class ClientToServerEvent {
    /**
     * Serializes the event into a JSON string.
     *
     * @return The json string.
     * @throws JsonProcessingException Any exception while processing the JSON.
     */
    public final String serialize() throws JsonProcessingException {
        return new ObjectMapper().writerFor(ClientToServerEvent.class).writeValueAsString(this);
    }

    /**
     * Deserializes the JSON into a ClientToServerEvent.
     *
     * @param json The JSON to deserialize.
     * @return The deserialized ClientToServerEvent.
     * @throws JsonProcessingException Any exception while processing the JSON.
     */
    public static ClientToServerEvent deserialize(final String json) throws JsonProcessingException {
        return new ObjectMapper().readerFor(ClientToServerEvent.class).readValue(json);
    }
}
