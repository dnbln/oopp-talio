package commons.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.ColorPreset;
import commons.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"LineLength", "LongLine"})
public class ServerToClientEventSerDeTests {
    record TestSpec(ServerToClientEvent value, String json) {
    }

    static CardList buildList(final Consumer<CardList> patcher) {
        var list = new CardList();
        patcher.accept(list);
        return list;
    }

    static Card buildCard(final Consumer<Card> patcher) {
        var card = new Card();
        patcher.accept(card);
        return card;
    }

    private static final ArrayList<TestSpec> TESTS = new ArrayList<>(List.of(
            new TestSpec(
                    new CardTextSetEvent(1234, "abcd"),
                    """
                            {"type":"cardTextSet","cardId":1234,"newText":"abcd"}
                            """.trim()),
            new TestSpec(
                    new CardTitleSetEvent(1234, "abcd"),
                    """
                            {"type":"cardTitleSet","cardId":1234,"newTitle":"abcd"}
                            """.trim()
            ),
            new TestSpec(
                    new ListCreatedEvent(new CardList()),
                    """
                            {"type":"listCreated","list":{"id":0,"cards":[],"title":"","fontColor":"","backgroundColor":""}}
                            """.trim()
            ),
            new TestSpec(
                    new ListCreatedEvent(buildList((list) -> {
                        var card = new Card();

                        card.patch(p -> {
                            p.setTitle("Aa");
                            p.setText("Bb");
                        });

                        list.patch(p -> p.addCard(card));
                    })),
                    """
                            {"type":"listCreated","list":{"id":0,"cards":[{"id":0,"title":"Aa","text":"Bb","category":"","dueDate":null,"cardList":0,"tags":[],"colorPresetKey":0,"subtasks":[]}],"title":"","fontColor":"","backgroundColor":""}}
                            """.trim()
            ),
            new TestSpec(
                    new CardCategorySetEvent(1, "newCategory"),
                    """
                            {"type":"cardCategorySet","cardId":1,"newCategory":"newCategory"}
                            """.trim()
            ),
            new TestSpec(
                    new CardCreatedEvent(2, new Card()),
                    """
                            {"type":"cardCreated","cardListId":2,"card":{"id":0,"title":"","text":"","category":"","dueDate":null,"cardList":0,"tags":[],"colorPresetKey":0,"subtasks":[]}}
                            """.trim()
            ),
            new TestSpec(
                    new CardDueDateSetEvent(2, ZonedDateTime.of(2023, 3, 10, 3, 4, 5, 6, ZoneId.of("UTC"))),
                    """
                            {"type":"cardDueDateSet","cardId":2,"dueDate":"2023-03-10T03:04:05.000000006Z"}
                            """.trim()
            ),
            new TestSpec(
                    new CardMovedEvent(1, 2, 0),
                    """
                            {"type":"cardMoved","cardListId":1,"card":2,"placedAfter":0}
                            """.trim()
            ),
            new TestSpec(
                    new CardMovedEvent(1, 2, 3),
                    """
                            {"type":"cardMoved","cardListId":1,"card":2,"placedAfter":3}
                            """.trim()
            ),
            new TestSpec(
                    new CardRemovedEvent(1, buildCard(c -> {
                        c.patch(p -> p.setTitle("ab"));
                        c.patch(p -> p.setText("cd"));
                    })),
                    """
                            {"type":"cardRemoved","cardListId":1,"card":{"id":0,"title":"ab","text":"cd","category":"","dueDate":null,"cardList":0,"tags":[],"colorPresetKey":0,"subtasks":[]}}
                            """.trim()
            ),
            new TestSpec(
                    new ListRemovedEvent(new CardList()),
                    """
                            {"type":"listRemoved","cardList":{"id":0,"cards":[],"title":"","fontColor":"","backgroundColor":""}}
                            """.trim()
            ),
            new TestSpec(
                    new ListRemovedEvent(buildList(l -> {
                        var c = buildCard(card -> card.patch(p -> {
                            p.setTitle("ab");
                            p.setText("cd");
                        }));

                        l.patch(p -> p.addCard(c));
                    })),
                    """
                            {"type":"listRemoved","cardList":{"id":0,"cards":[{"id":0,"title":"ab","text":"cd","category":"","dueDate":null,"cardList":0,"tags":[],"colorPresetKey":0,"subtasks":[]}],"title":"","fontColor":"","backgroundColor":""}}
                            """.trim()
            ),
            new TestSpec(
                    new ListsReorderedEvent(
                            1,
                            0),
                    """
                            {"type":"listsReordered","cardList":1,"placedAfter":0}
                            """.trim()
            ),
            new TestSpec(
                    new ListsReorderedEvent(
                            1,
                            2),
                    """
                            {"type":"listsReordered","cardList":1,"placedAfter":2}
                            """.trim()
            ),
            new TestSpec(
                    new XListCardMoveEvent(1, 2, 3, 0),
                    """
                            {"type":"xListCardMoved","srcCardList":1,"card":2,"destCardList":3,"hook":0}
                            """.trim()
            ),
            new TestSpec(
                    new XListCardMoveEvent(1, 2, 3, 4),
                    """
                            {"type":"xListCardMoved","srcCardList":1,"card":2,"destCardList":3,"hook":4}
                            """.trim()
            ),
            new TestSpec(
                    new CardListTitleSetEvent(1234, "newTitle"),
                    """
                            {"type":"cardListTitleSet","cardListId":1234,"newTitle":"newTitle"}
                            """.trim()
            ),
            new TestSpec(
                    new

                            BoardTitleSetEvent(1234, "newTitle"),
                    """
                            {"type":"boardTitleSet","boardId":1234,"newTitle":"newTitle"}
                            """.

                            trim()
            ),
            new TestSpec(
                    new TagCreatedEvent(new Tag()),
                    """
                            {"type":"tagCreated","tag":{"id":0,"name":"","fontColor":"","backgroundColor":null}}
                            """.trim()
            ),
            new TestSpec(
                    new TagDeletedEvent(new Tag()),
                    """
                            {"type":"tagDeleted","tag":{"id":0,"name":"","fontColor":"","backgroundColor":null}}
                            """.trim()
            ),
            new TestSpec(
                    new BoardFontColorSetEvent("a"),
                    """
                            {"type":"boardFontColorSet","fontColor":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new BoardBackgroundColorSetEvent("a"),
                    """
                            {"type":"boardBackgroundColorSet","background":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new ColorPresetFontColorSetEvent(0, "a"),
                    """
                            {"type":"colorPresetFontColorSet","presetKey":0,"fontColor":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new ColorPresetBackgroundColorSetEvent(0, "a"),
                    """
                            {"type":"colorPresetBackgroundColorSet","presetKey":0,"background":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new ColorPresetNameSetEvent(0, "a"),
                    """
                            {"type":"colorPresetNameSet","presetKey":0,"name":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new ColorPresetCreatedEvent(new ColorPreset()),
                    """
                            {"type":"colorPresetCreated","preset":{"id":0,"name":null,"background":null,"foreground":null,"board":0}}
                            """.trim()
            ),
            new TestSpec(
                    new ColorPresetRemovedEvent(new ColorPreset()),
                    """
                            {"type":"colorPresetRemoved","preset":{"id":0,"name":null,"background":null,"foreground":null,"board":0}}
                            """.trim()
            ),
            new TestSpec(
                    new ListFontColorSetEvent(0, "a"),
                    """
                            {"type":"listFontColorSet","listId":0,"newFont":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new ListBackgroundColorSetEvent(0, "a"),
                    """
                            {"type":"listBackgroundColorSet","listId":0,"newBackground":"a"}
                            """.trim()
            ),
            new TestSpec(
                    new CardTagAddedEvent(1, 2),
                    """
                            {"type":"cardTagAdded","cardId":1,"tagId":2}
                            """.trim()
            ),
            new TestSpec(
                    new CardTagRemovedEvent(1, 2),
                    """
                            {"type":"cardTagRemoved","cardId":1,"tagId":2}
                            """.trim()
            ),
            new TestSpec(
                    new CardSubtaskCreatedEvent(1, new CardSubtask()),
                    """
                            {"type":"cardSubtaskCreated","cardId":1,"subtask":{"id":0,"name":"","completed":false,"card":0}}
                            """.trim()
            ),
            new TestSpec(
                    new CardSubtaskRemovedEvent(1, 2),
                    """
                            {"type":"cardSubtaskRemoved","cardId":1,"subtaskId":2}
                            """.trim()
            ),
            new TestSpec(
                    new CardSubtaskNameSetEvent(2, "abc"),
                    """
                            {"type":"cardSubtaskNameSet","subtaskId":2,"newName":"abc"}
                            """.trim()
            ),
            new TestSpec(
                    new CardSubtaskCompletenessSetEvent(2, true),
                    """
                            {"type":"cardSubtaskCompletenessSet","subtaskId":2,"newCompleteness":true}
                            """.trim()
            )
    ));

    @Test
    void allSerialize() throws JsonProcessingException {
        for (var testSpec : TESTS) {
            assertEquals(testSpec.json, testSpec.value.serialize());
        }
    }

    @Test
    void allDeserialize() throws JsonProcessingException {
        for (var testSpec : TESTS) {
            assertEquals(testSpec.value, ServerToClientEvent.deserialize(testSpec.json));
        }
    }
}
