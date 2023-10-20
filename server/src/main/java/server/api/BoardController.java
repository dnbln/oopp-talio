package server.api;

import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.ColorPreset;
import commons.Tag;
import commons.events.BoardRemovedEvent;
import commons.events.BoardTitleSetEvent;
import commons.events.ServerToClientEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RestController
public final class BoardController {
    private static final long LONG_POLLING_TIMEOUT_VALUE = 10000L;
    private final BoardService boardService;

    /**
     * Constructor.
     *
     * @param boardService The board service.
     */
    public BoardController(final BoardService boardService) {
        this.boardService = boardService;
    }

    private Map<Object, Consumer<ServerToClientEvent>> listeners = new ConcurrentHashMap<>();

    private static <T> ResponseEntity<T> queryWithBuilder(final Supplier<ResponseEntity<T>> builder) {
        try {
            return builder.get();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().header("Message", e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private static <T> ResponseEntity<T> query(final Supplier<T> supplier) {
        return queryWithBuilder(() -> ResponseEntity.ok(supplier.get()));
    }

    @FunctionalInterface
    interface SideEffect {
        void produceSideEffect();
    }

    private static ResponseEntity<Void> queryVoid(final SideEffect effect) {
        return queryWithBuilder(() -> {
            effect.produceSideEffect();
            return ResponseEntity.noContent().build();
        });
    }

    @GetMapping("/boards/all")
    private List<Board> allSortedBoards() {
        return boardService.allSortedBoards();
    }

    @GetMapping("/boards")
    private List<Board> specificBoards(@RequestParam("id") final List<Long> boardIds) {
        return boardService.specificBoards(boardIds);
    }

    @GetMapping("/boards/{id}")
    ResponseEntity<Board> getBoard(@PathVariable final long id) {
        return query(() -> boardService.getBoard(id));
    }

    @PostMapping("/boards")
    @ResponseBody
    private ResponseEntity<Board> newBoard() {
        return ResponseEntity.ok(boardService.newBoard());
    }

    @PutMapping("/boards/{boardId}/board_title")
    @ResponseBody
    private ResponseEntity<Void> setBoardTitle(@PathVariable final long boardId, @RequestBody final String boardTitle) {
        listeners.forEach((k, l) -> l.accept(new BoardTitleSetEvent(boardId, boardTitle)));
        return queryVoid(() -> boardService.setBoardTitle(boardId, boardTitle));
    }

    @DeleteMapping("/boards/{boardId}")
    @ResponseBody
    private ResponseEntity<Void> deleteBoard(@PathVariable final long boardId) {
        listeners.forEach((k, l) -> l.accept(new BoardRemovedEvent()));
        return queryVoid(() -> boardService.deleteBoard(boardId));
    }
    @GetMapping("/updates")
    private DeferredResult<ResponseEntity<ServerToClientEvent>> longPollingUpdates() {
        var noEvent = ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        var res = new DeferredResult<ResponseEntity<ServerToClientEvent>>(LONG_POLLING_TIMEOUT_VALUE, noEvent);
        var key = new Object();
        listeners.put(key, event -> res.setResult(ResponseEntity.ok(event)));
        res.onCompletion(() -> listeners.remove(key));
        return res;
    }

    @PostMapping("/boards/{boardId}/lists")
    @ResponseBody
    private ResponseEntity<CardList> newList(@PathVariable final long boardId) {
        return query(() -> boardService.newList(boardId));
    }

    @GetMapping("/boards/{boardId}/lists/{listId}")
    @ResponseBody
    private ResponseEntity<CardList> getCardList(@PathVariable final long boardId, @PathVariable final long listId) {
        return query(() -> boardService.getCardList(boardId, listId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/list_title")
    @ResponseBody
    private ResponseEntity<Void> setCardListTitle(@PathVariable final long boardId, @PathVariable final long listId,
                                                  @RequestBody final String cardListTitle) {
        return queryVoid(() -> boardService.setCardListTitle(boardId, listId, cardListTitle));
    }

    @DeleteMapping("/boards/{boardId}/lists/{listId}")
    @ResponseBody
    private ResponseEntity<Void> deleteCardList(@PathVariable final long boardId, @PathVariable final long listId) {
        return queryVoid(() -> boardService.deleteCardList(boardId, listId));
    }

    @DeleteMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}")
    private ResponseEntity<Void> deleteCard(@PathVariable final long boardId, @PathVariable final long listId,
                                            @PathVariable final long cardId) {
        return queryVoid(() -> boardService.deleteCard(boardId, listId, cardId));
    }

    @PostMapping("/boards/{boardId}/lists/{listId}/cards")
    @ResponseBody
    private ResponseEntity<Card> newCard(@PathVariable final long boardId, @PathVariable final long listId,
                                         @RequestBody final Card card) {
        return query(() -> boardService.newCard(boardId, listId, card));
    }

    @GetMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}")
    @ResponseBody
    private ResponseEntity<Card> getCard(@PathVariable final long boardId, @PathVariable final long listId,
                                         @PathVariable final long cardId) {
        return query(() -> boardService.getCard(boardId, listId, cardId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/card_title")
    @ResponseBody
    private ResponseEntity<Void> setCardTitle(@PathVariable final long boardId, @PathVariable final long listId,
                                              @PathVariable final long cardId, @RequestBody final String cardTitle) {
        return queryVoid(() -> boardService.setCardTitle(boardId, listId, cardId, cardTitle));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/card_text")
    @ResponseBody
    private ResponseEntity<Void> setCardText(@PathVariable final long boardId, @PathVariable final long listId,
                                             @PathVariable final long cardId,
                                             @RequestBody(required = false) final String cardText) {
        return queryVoid(
                () -> boardService.setCardText(boardId, listId, cardId, Objects.requireNonNullElse(cardText, "")));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/due_date")
    @ResponseBody
    private ResponseEntity<Void> setCardDueDate(@PathVariable final long boardId, @PathVariable final long listId,
                                                @PathVariable final long cardId,
                                                @RequestBody final String dueDateString) {
        ZonedDateTime dueDate = ZonedDateTime.parse(dueDateString);

        return queryVoid(() -> boardService.setCardDueDate(boardId, listId, cardId, dueDate));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/tags/+{tagId}")
    @ResponseBody
    private ResponseEntity<Void> addTag(@PathVariable final long boardId, @PathVariable final long listId,
                                        @PathVariable final long cardId, @PathVariable final long tagId) {
        return queryVoid(() -> boardService.addTagToCard(boardId, listId, cardId, tagId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/tags/-{tagId}")
    @ResponseBody
    private ResponseEntity<Void> removeTag(@PathVariable final long boardId, @PathVariable final long listId,
                                           @PathVariable final long cardId, @PathVariable final long tagId) {
        return queryVoid(() -> boardService.removeTagFromCard(boardId, listId, cardId, tagId));
    }

    @PutMapping("/boards/{boardId}/tags/{tagId}/tag_name")
    private ResponseEntity<Void> setTagName(@PathVariable final long boardId, @PathVariable final long tagId,
                                            @RequestBody final String tagName) {
        return queryVoid(() -> boardService.setTagName(boardId, tagId, tagName));
    }

    private static String nullableStr(final String s) {
        return s.equals("null") ? null : s;
    }

    @PutMapping("/boards/{boardId}/tags/{tagId}/tag_color")
    @ResponseBody
    private ResponseEntity<Void> setTagFontColor(@PathVariable final long boardId, @PathVariable final long tagId,
                                                 @RequestBody final String tagColor) {
        String actualFontColor = nullableStr(tagColor);

        return queryVoid(() -> boardService.setTagFontColor(boardId, tagId, actualFontColor));
    }

    @PutMapping("/boards/{boardId}/tags/{tagId}/tag_background_color")
    @ResponseBody
    private ResponseEntity<Void> setTagBackgroundColor(@PathVariable final long boardId, @PathVariable final long tagId,
                                                       @RequestBody final String tagBackgroundColor) {
        String actualBackgroundColor = nullableStr(tagBackgroundColor);

        return queryVoid(() -> boardService.setTagBackgroundColor(boardId, tagId, actualBackgroundColor));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/moveAfter/{otherCardId}")
    @ResponseBody
    private ResponseEntity<Void> sameListCardMove(@PathVariable final long boardId, @PathVariable final long listId,
                                                  @PathVariable final long cardId,
                                                  @PathVariable final long otherCardId) {
        return queryVoid(() -> boardService.sameListCardMove(boardId, listId, cardId, otherCardId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/xListMoveAfter/{otherListId}/{otherCardId}")
    @ResponseBody
    private ResponseEntity<Void> xListCardMove(@PathVariable final long boardId, @PathVariable final long listId,
                                               @PathVariable final long cardId, @PathVariable final long otherListId,
                                               @PathVariable final long otherCardId) {
        return queryVoid(() -> boardService.xListCardMove(boardId, listId, cardId, otherListId, otherCardId));
    }

    /**
     * Moves a card list to be placed after another list in a board.
     *
     * @param boardId     the ID of the board containing the lists
     * @param listId      the ID of the list that will be moved
     * @param otherListId the ID of the list after which the moved list will be placed
     * @return void.
     */
    @PutMapping("/boards/{boardId}/lists/{listId}/moveAfter/{otherListId}")
    @ResponseBody
    private ResponseEntity<Void> listMove(@PathVariable final long boardId, @PathVariable final long listId,
                                          @PathVariable final long otherListId) {
        return queryVoid(() -> boardService.listMove(boardId, listId, otherListId));
    }

    @PostMapping("/boards/{boardId}/tags")
    @ResponseBody
    private ResponseEntity<Tag> newTag(@PathVariable final long boardId, @RequestBody final Tag tag) {
        return query(() -> boardService.newTag(boardId, tag));
    }

    @GetMapping("/boards/{boardId}/tags/{tagId}")
    @ResponseBody
    private ResponseEntity<Tag> getTag(@PathVariable final long boardId, @PathVariable final long tagId) {
        return query(() -> boardService.getTag(boardId, tagId));
    }

    @DeleteMapping("/boards/{boardId}/tags/{tagId}")
    @ResponseBody
    private ResponseEntity<Void> deleteTag(@PathVariable final long boardId, @PathVariable final long tagId) {
        return queryVoid(() -> boardService.deleteTag(boardId, tagId));
    }

    @PutMapping("/boards/{boardId}/fontColor")
    @ResponseBody
    private ResponseEntity<Void> setBoardFont(@PathVariable final long boardId, @RequestBody final String fontColor) {
        String actualFontColor = nullableStr(fontColor);

        return queryVoid(() -> boardService.setBoardFont(boardId, actualFontColor));
    }

    @PutMapping("/boards/{boardId}/backgroundColor")
    @ResponseBody
    private ResponseEntity<Void> setBoardBackground(@PathVariable final long boardId,
                                                    @RequestBody final String backgroundColor) {
        String actualBackgroundColor = nullableStr(backgroundColor);

        return queryVoid(() -> boardService.setBoardBackground(boardId, actualBackgroundColor));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/fontColor")
    @ResponseBody
    private ResponseEntity<Void> setCardListFont(@PathVariable final long boardId, @PathVariable final long listId,
                                                 @RequestBody final String fontColor) {
        String actualFontColor = nullableStr(fontColor);

        return queryVoid(() -> boardService.setCardListFont(boardId, listId, actualFontColor));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/backgroundColor")
    @ResponseBody
    private ResponseEntity<Void> setCardListBackground(@PathVariable final long boardId,
                                                       @PathVariable final long listId,
                                                       @RequestBody final String backgroundColor) {
        String actualBackgroundColor = nullableStr(backgroundColor);

        return queryVoid(() -> boardService.setCardListBackground(boardId, listId, actualBackgroundColor));
    }

    @PostMapping("/boards/{boardId}/presets")
    @ResponseBody
    private ResponseEntity<ColorPreset> newPreset(@PathVariable final long boardId,
                                                  @RequestBody final ColorPreset preset) {
        return query(() -> boardService.newPreset(boardId, preset));
    }

    @GetMapping("/boards/{boardId}/presets/{presetKey}")
    @ResponseBody
    private ResponseEntity<ColorPreset> getPreset(@PathVariable final long boardId,
                                                  @PathVariable final long presetKey) {
        return query(() -> boardService.getPreset(boardId, presetKey));
    }

    @DeleteMapping("/boards/{boardId}/presets/{presetKey}")
    @ResponseBody
    private ResponseEntity<Void> deletePreset(@PathVariable final long boardId, @PathVariable final long presetKey) {
        return queryVoid(() -> boardService.deletePreset(boardId, presetKey));
    }

    @PutMapping("/boards/{boardId}/presets/{presetKey}/name")
    @ResponseBody
    private ResponseEntity<Void> setPresetName(@PathVariable final long boardId, @PathVariable final long presetKey,
                                               @RequestBody final String name) {
        return queryVoid(() -> boardService.setPresetName(boardId, presetKey, name));
    }

    @GetMapping("/boards/{board_id}/tags")
    @ResponseBody
    private ResponseEntity<List<Tag>> getAllTags(@PathVariable("board_id") final long boardId) {
        return query(() -> boardService.getAllTags(boardId));
    }

    @PutMapping("/boards/{boardId}/presets/{presetKey}/fontColor")
    @ResponseBody
    private ResponseEntity<Void> setPresetFontColor(@PathVariable final long boardId,
                                                    @PathVariable final long presetKey,
                                                    @RequestBody final String fontColor) {
        String actualFontColor = nullableStr(fontColor);
        return queryVoid(() -> boardService.setPresetFontColor(boardId, presetKey, actualFontColor));
    }

    @PutMapping("/boards/{boardId}/presets/{presetKey}/backgroundColor")
    @ResponseBody
    private ResponseEntity<Void> setPresetBackgroundColor(@PathVariable final long boardId,
                                                          @PathVariable final long presetKey,
                                                          @RequestBody final String backgroundColor) {
        String actualBackgroundColor = nullableStr(backgroundColor);
        return queryVoid(() -> boardService.setPresetBackgroundColor(boardId, presetKey, actualBackgroundColor));
    }

    @PutMapping("/boards/{boardId}/defaultPreset")
    @ResponseBody
    private ResponseEntity<Void> setDefaultPreset(@PathVariable final long boardId, @RequestBody final long presetKey) {
        return queryVoid(() -> boardService.setDefaultPreset(boardId, presetKey));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/preset")
    @ResponseBody
    private ResponseEntity<Void> setCardPreset(@PathVariable final long boardId, @PathVariable final long listId,
                                               @PathVariable final long cardId, @RequestBody final long presetKey) {
        return queryVoid(() -> boardService.setCardPreset(boardId, listId, cardId, presetKey));
    }

    @PostMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks")
    @ResponseBody
    private ResponseEntity<CardSubtask> newSubtask(@PathVariable final long boardId, @PathVariable final long listId,
                                                   @PathVariable final long cardId,
                                                   @RequestBody final CardSubtask subtask) {
        return query(() -> boardService.newSubtask(boardId, listId, cardId, subtask));
    }

    @GetMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks/{subtaskId}")
    @ResponseBody
    private ResponseEntity<CardSubtask> getSubtask(@PathVariable final long boardId, @PathVariable final long listId,
                                                   @PathVariable final long cardId,
                                                   @PathVariable final long subtaskId) {
        return query(() -> boardService.getSubtask(boardId, listId, cardId, subtaskId));
    }

    @DeleteMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks/{subtaskId}")
    @ResponseBody
    private ResponseEntity<Void> deleteSubtask(@PathVariable final long boardId, @PathVariable final long listId,
                                               @PathVariable final long cardId, @PathVariable final long subtaskId) {
        return queryVoid(() -> boardService.deleteSubtask(boardId, listId, cardId, subtaskId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks/{subtaskId}/moveAfter/{otherSubtaskId}")
    @ResponseBody
    private ResponseEntity<Void> subtaskMove(@PathVariable final long boardId, @PathVariable final long listId,
                                                  @PathVariable final long cardId,
                                                  @PathVariable final long subtaskId,
                                                  @PathVariable final long otherSubtaskId) {
        return queryVoid(() -> boardService.subtaskMove(boardId, listId, cardId, subtaskId, otherSubtaskId));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks/{subtaskId}/name")
    @ResponseBody
    private ResponseEntity<Void> setSubtaskName(@PathVariable final long boardId, @PathVariable final long listId,
                                                @PathVariable final long cardId, @PathVariable final long subtaskId,
                                                @RequestBody final String newName) {
        return queryVoid(() -> boardService.setSubtaskName(boardId, listId, cardId, subtaskId, newName));
    }

    @PutMapping("/boards/{boardId}/lists/{listId}/cards/{cardId}/subtasks/{subtaskId}/completeness")
    @ResponseBody
    private ResponseEntity<Void> setSubtaskCompleteness(@PathVariable final long boardId,
                                                        @PathVariable final long listId,
                                                        @PathVariable final long cardId,
                                                        @PathVariable final long subtaskId,
                                                        @RequestBody final boolean newCompleteness) {
        return queryVoid(
                () -> boardService.setSubtaskCompleteness(boardId, listId, cardId, subtaskId, newCompleteness));
    }
}
