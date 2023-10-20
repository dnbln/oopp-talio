package commons.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ListCreatedEvent.class, name = "listCreated"),
        @JsonSubTypes.Type(value = ListRemovedEvent.class, name = "listRemoved"),
        @JsonSubTypes.Type(value = ListsReorderedEvent.class, name = "listsReordered"),
        @JsonSubTypes.Type(value = CardRemovedEvent.class, name = "cardRemoved"),
        @JsonSubTypes.Type(value = CardCreatedEvent.class, name = "cardCreated"),
        @JsonSubTypes.Type(value = CardMovedEvent.class, name = "cardMoved"),
        @JsonSubTypes.Type(value = XListCardMoveEvent.class, name = "xListCardMoved"),
        @JsonSubTypes.Type(value = CardTitleSetEvent.class, name = "cardTitleSet"),
        @JsonSubTypes.Type(value = CardTextSetEvent.class, name = "cardTextSet"),
        @JsonSubTypes.Type(value = CardCategorySetEvent.class, name = "cardCategorySet"),
        @JsonSubTypes.Type(value = CardDueDateSetEvent.class, name = "cardDueDateSet"),
        @JsonSubTypes.Type(value = CardListTitleSetEvent.class, name = "cardListTitleSet"),
        @JsonSubTypes.Type(value = BoardTitleSetEvent.class, name = "boardTitleSet"),
        @JsonSubTypes.Type(value = TagCreatedEvent.class, name = "tagCreated"),
        @JsonSubTypes.Type(value = TagDeletedEvent.class, name = "tagDeleted"),
        @JsonSubTypes.Type(value = BoardFontColorSetEvent.class, name = "boardFontColorSet"),
        @JsonSubTypes.Type(value = BoardBackgroundColorSetEvent.class, name = "boardBackgroundColorSet"),
        @JsonSubTypes.Type(value = ColorPresetCreatedEvent.class, name = "colorPresetCreated"),
        @JsonSubTypes.Type(value = ColorPresetRemovedEvent.class, name = "colorPresetRemoved"),
        @JsonSubTypes.Type(value = DefaultCardColorPresetSetEvent.class, name = "defaultColorPresetSet"),
        @JsonSubTypes.Type(value = ColorPresetNameSetEvent.class, name = "colorPresetNameSet"),
        @JsonSubTypes.Type(value = ColorPresetFontColorSetEvent.class, name = "colorPresetFontColorSet"),
        @JsonSubTypes.Type(value = ColorPresetBackgroundColorSetEvent.class, name = "colorPresetBackgroundColorSet"),
        @JsonSubTypes.Type(value = ListFontColorSetEvent.class, name = "listFontColorSet"),
        @JsonSubTypes.Type(value = ListBackgroundColorSetEvent.class, name = "listBackgroundColorSet"),
        @JsonSubTypes.Type(value = CardPresetSetEvent.class, name = "cardPresetSet"),
        @JsonSubTypes.Type(value = CardTagAddedEvent.class, name = "cardTagAdded"),
        @JsonSubTypes.Type(value = CardTagRemovedEvent.class, name = "cardTagRemoved"),
        @JsonSubTypes.Type(value = MessageProcessedEvent.class, name = "messageProcessed"),
        @JsonSubTypes.Type(value = BoardRemovedEvent.class, name = "boardRemoved"),
        @JsonSubTypes.Type(value = TagNameSetEvent.class, name = "tagNameSet"),
        @JsonSubTypes.Type(value = TagFontColorSetEvent.class, name = "tagFontColorSet"),
        @JsonSubTypes.Type(value = TagBackgroundColorSetEvent.class, name = "tagBackgroundSet"),
        @JsonSubTypes.Type(value = CardSubtaskCreatedEvent.class, name = "cardSubtaskCreated"),
        @JsonSubTypes.Type(value = CardSubtaskRemovedEvent.class, name = "cardSubtaskRemoved"),
        @JsonSubTypes.Type(value = CardSubtaskNameSetEvent.class, name = "cardSubtaskNameSet"),
        @JsonSubTypes.Type(value = CardSubtaskCompletenessSetEvent.class, name = "cardSubtaskCompletenessSet"),
})
public interface ServerToClientEvent {
    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    /**
     * Serializes the event into a JSON string.
     *
     * @return The json string.
     * @throws JsonProcessingException Any exception while processing the JSON.
     */
    default String serialize() throws JsonProcessingException {
        return objectMapper()
                .writerFor(ServerToClientEvent.class)
                .writeValueAsString(this);
    }

    /**
     * Deserializes the JSON into a ServerToClientEvent.
     *
     * @param json The JSON to deserialize.
     * @return The deserialized ServerToClientEvent.
     * @throws JsonProcessingException Any exception while processing the JSON.
     */
    static ServerToClientEvent deserialize(final String json) throws JsonProcessingException {
        return objectMapper()
                .readerFor(ServerToClientEvent.class)
                .readValue(json);
    }
}
