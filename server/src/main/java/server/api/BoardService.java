package server.api;

import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.ColorPreset;
import commons.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.Config;
import server.api.exceptions.NoSuchBoard;
import server.database.BoardRepository;
import server.database.CardListRepository;
import server.database.CardRepository;
import server.database.ColorPresetRepository;
import server.database.SubtaskRepository;
import server.database.TagRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Consumer;

@Service
public final class BoardService {
    private final BoardRepository boardRepository;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;
    private final TagRepository tagRepository;
    private final ColorPresetRepository colorPresetRepository;
    private final SubtaskRepository subtaskRepository;

    private final MessageBroker messageBroker;

    /**
     * Constructor.
     *
     * @param config        Config.
     * @param messageBroker The message broker.
     */
    public BoardService(final Config config, final MessageBroker messageBroker) {
        this.boardRepository = config.getBoardRepository();
        this.cardListRepository = config.getCardListRepository();
        this.cardRepository = config.getCardRepository();
        this.tagRepository = config.getTagRepository();
        this.colorPresetRepository = config.getColorPresetRepository();
        this.subtaskRepository = config.getSubtaskRepository();

        this.messageBroker = messageBroker;
    }

    Boolean hasAnyBoard() {
        return boardRepository.count() > 0;
    }

    /**
     * Retrieves a list of all boards sorted by ID in ascending order.
     *
     * @return a List of Board objects.
     */
    List<Board> allSortedBoards() {
        return this.boardRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Retrieves a list of specific boards.
     *
     * @param boardIds the IDs of the boards to get.
     * @return a List of Board objects.
     */
    List<Board> specificBoards(final Iterable<Long> boardIds) {
        return this.boardRepository.findAllById(boardIds);
    }

    Board getBoard(final long id) {
        return boardRepository.findById(id).orElseThrow(() -> new NoSuchBoard(id));
    }

    Board newBoard() {
        return boardRepository.save(new Board());
    }

    void setBoardTitle(
            final long boardId,
            final String boardTitle
    ) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> {
            bp.setTitle(boardTitle);
            boardRepository.save(bp.getBaseBoard());
        });
    }

    void deleteBoard(final long boardId) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> {
            bp.removeBoard();
            this.boardRepository.deleteById(boardId);
        });
    }

    CardList newList(final long boardId) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        var cardList = new CardList();

        cardList.presaveForBoard(b);

        var saved = cardListRepository.save(cardList);

        bw.patch(p -> p.addCardList(saved));

        boardRepository.save(b);

        return saved;
    }


    CardList getCardList(final long boardId,
                         final long listId) {
        Board b = getBoard(boardId);

        return b.cardListById(listId);
    }

    void setCardListTitle(final long boardId,
                          final long listId,
                          final String cardListTitle) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> bp.patchCardList(listId, lp -> {
            lp.setTitle(cardListTitle);
            cardListRepository.save(lp.getBaseCardList());
        }));
    }

    void deleteCardList(final long boardId,
                        final long listId) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(p -> {
            p.removeCardList(listId);
            cardListRepository.deleteById(listId);
            boardRepository.save(p.getBaseBoard());
        });
    }

    void deleteCard(final long boardId,
                    final long listId,
                    final long cardId) {
        Board board = getBoard(boardId);
        var boardWrapper = messageBroker.getWrapper(board);

        boardWrapper.patch(boardPatcher -> boardPatcher.patchCardList(listId, cardListPatcher -> {
            cardListPatcher.removeCard(cardId);
            this.cardRepository.deleteById(cardId);
            this.cardListRepository.save(cardListPatcher.getBaseCardList());
            this.boardRepository.save(boardPatcher.getBaseBoard());
        }));
    }

    /**
     * Add a new card to a list in a board.
     *
     * @param boardId the ID of the board that contains the list in which the card will be added.
     * @param listId  the ID of the list in which the card will be added.
     * @param card    the card that will be added.
     * @return a response entity which contains the card that was added.
     */
    Card newCard(final long boardId,
                 final long listId,
                 final Card card) {
        if (card.getId() != 0) {
            throw new IllegalArgumentException("card id is not 0");
        }

        if (card.getCardList() != 0) {
            throw new IllegalArgumentException("card list of card should be 0");
        }


        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        Card[] saved = new Card[1];

        bw.patch(bp -> bp.patchCardList(listId, lp -> {
            card.presaveForList(lp.getBaseCardList());

            saved[0] = cardRepository.save(card);

            lp.addCard(saved[0]);

            cardListRepository.save(lp.getBaseCardList());
        }));

        return saved[0];
    }

    Card getCard(final long boardId,
                 final long listId,
                 final long cardId) {
        Board b = getBoard(boardId);

        var list = b.cardListById(listId);

        return list.cardById(cardId);
    }

    void setCardTitle(final long boardId,
                      final long listId,
                      final long cardId,
                      final String cardTitle) {
        patchCard(boardId, listId, cardId, cp -> cp.setTitle(cardTitle));
    }

    void setCardText(final long boardId,
                     final long listId,
                     final long cardId,
                     final String cardText) {
        patchCard(boardId, listId, cardId, cp -> cp.setText(cardText));
    }

    void setCardDueDate(final long boardId,
                        final long listId,
                        final long cardId,
                        final ZonedDateTime dueDate) {
        patchCard(boardId, listId, cardId, cp -> cp.setDueDate(dueDate));
    }

    void addTagToCard(final long boardId,
                      final long listId,
                      final long cardId,
                      final long tagId) {
        patchCard(boardId, listId, cardId, cp -> cp.addTag(getTag(tagId)));
    }

    List<Tag> getAllTags(
            final long boardId
    ) {
        Board board = getBoard(boardId);

        return board.getTags();
    }

    void removeTagFromCard(final long boardId,
                           final long listId,
                           final long cardId,
                           final long tagId) {
        patchCard(boardId, listId, cardId, cp -> cp.removeTag(getTag(tagId)));
    }

    void setTagName(final long boardId,
                    final long tagId,
                    final String tagName) {
        patchTag(boardId, tagId, tp -> tp.setName(tagName));
    }

    void setTagFontColor(final long boardId,
                         final long tagId,
                         final String tagColor) {
        patchTag(boardId, tagId, tp -> tp.setFontColor(tagColor));
    }

    void setTagBackgroundColor(final long boardId,
                               final long tagId,
                               final String tagBackgroundColor) {
        patchTag(boardId, tagId, tp -> tp.setBackgroundColor(tagBackgroundColor));
    }

    void sameListCardMove(final long boardId,
                          final long listId,
                          final long cardId,
                          final long otherCardId) {
        patchCardList(boardId, listId, lp -> lp.moveCard(cardId, otherCardId));
    }

    void xListCardMove(final long boardId,
                       final long listId,
                       final long cardId,
                       final long otherListId,
                       final long otherCardId) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> bp.xListCardMove(listId, cardId, otherListId, otherCardId,
                cardRepository::save, cardListRepository::save));
    }

    /**
     * Moves a card list to be placed after another list in a board.
     *
     * @param boardId     the ID of the board containing the lists
     * @param listId      the ID of the list that will be moved
     * @param otherListId the ID of the list after which the moved list will be placed
     */
    void listMove(final long boardId,
                  final long listId,
                  final long otherListId) {
        patchBoard(boardId, bp -> bp.moveCardList(listId, otherListId));
    }

    Tag newTag(final long boardId, final Tag tag) {
        if (tag.getId() != 0) {
            throw new IllegalArgumentException("tag id must be 0");
        }

        var saved = tagRepository.save(tag);

        patchBoard(boardId, bp -> bp.addTag(saved));

        return saved;
    }

    Tag getTag(final long boardId, final long tagId) {
        Board board = getBoard(boardId);

        return board.tagById(tagId);
    }

    Tag getTag(final long tagId) {
        return tagRepository.findById(tagId).orElseThrow();
    }

    void deleteTag(final long boardId, final long tagId) {
        patchBoard(boardId, bp -> {
            var tag = bp.getBaseBoard().tagById(tagId);
            bp.removeTag(tagId);

            for (var cl : bp.getBaseBoard().getCardLists()) {
                for (var c : cl.getCards()) {
                    if (c.getTags().contains(tag)) {
                        c.patch(cp -> {
                            cp.removeTag(tag);
                            cardRepository.save(cp.getBaseCard());
                        });
                    }
                }
            }

            tagRepository.deleteById(tagId);
        });
    }

    void setBoardFont(final long boardId, final String fontColor) {
        patchBoard(boardId, bp -> bp.setFontColor(fontColor));
    }

    void setBoardBackground(final long boardId, final String backgroundColor) {
        patchBoard(boardId, bp -> bp.setBackgroundColor(backgroundColor));
    }

    void setCardListFont(final long boardId, final long listId, final String fontColor) {
        patchCardList(boardId, listId, lp -> lp.setFontColor(fontColor));
    }

    void setCardListBackground(final long boardId,
                               final long listId,
                               final String backgroundColor) {
        patchCardList(boardId, listId, lp -> lp.setBackgroundColor(backgroundColor));
    }

    ColorPreset newPreset(final long boardId, final ColorPreset preset) {
        if (preset.getId() != 0) {
            throw new IllegalArgumentException("preset id must be 0");
        }

        var saved = new ColorPreset[1];

        patchBoard(boardId, bp -> {
            preset.presaveForBoard(bp.getBaseBoard());
            saved[0] = colorPresetRepository.save(preset);
            bp.addCardColorPreset(saved[0]);
        });

        return saved[0];
    }

    ColorPreset getPreset(final long boardId, final long presetKey) {
        Board board = getBoard(boardId);

        return board.colorPresetById(presetKey);
    }

    void deletePreset(final long boardId, final long presetKey) {
        Board board = getBoard(boardId);
        var bw = messageBroker.getWrapper(board);

        bw.patch(bp -> {
            bp.removeColorPreset(presetKey);

            for (var cl : bp.getBaseBoard().getCardLists()) {
                for (var card : cl.getCards()) {
                    if (card.getColorPresetKey() == presetKey) {
                        card.patch(cp -> {
                            cp.setColorPreset(0);
                            cardRepository.save(cp.getBaseCard());
                        });
                    }
                }
            }

            colorPresetRepository.deleteById(presetKey);
            boardRepository.save(bp.getBaseBoard());
        });
    }

    void setPresetName(final long boardId,
                       final long presetKey,
                       final String name) {
        patchBoard(boardId, bp -> bp.setPresetName(presetKey, name));
    }

    void setPresetFontColor(final long boardId,
                            final long presetKey,
                            final String fontColor) {
        patchBoard(boardId, bp -> bp.setPresetFontColor(presetKey, fontColor));
    }

    void setPresetBackgroundColor(final long boardId,
                                  final long presetKey,
                                  final String backgroundColor) {
        patchBoard(boardId, bp -> bp.setPresetBackgroundColor(presetKey, backgroundColor));
    }

    void setDefaultPreset(final long boardId,
                          final long presetKey) {
        patchBoard(boardId, bp -> bp.setDefaultCardColorPreset(presetKey));
    }

    void setCardPreset(final long boardId,
                       final long listId,
                       final long cardId,
                       final long presetKey) {
        patchCard(boardId, listId, cardId, cp -> cp.setColorPreset(presetKey));
    }

    CardSubtask newSubtask(final long boardId,
                           final long listId,
                           final long cardId,
                           final CardSubtask subtask) {
        if (subtask.getId() != 0) {
            throw new IllegalArgumentException("subtask id must be 0");
        }

        if (subtask.getCard() != 0) {
            throw new IllegalArgumentException("subtask card must be 0");
        }

        var saved = new CardSubtask[1];

        patchCard(boardId, listId, cardId, cp -> {
            subtask.presaveForCard(cp.getBaseCard());
            saved[0] = subtaskRepository.save(subtask);
            cp.addSubtask(saved[0]);
        });

        return saved[0];
    }

    CardSubtask getSubtask(final long boardId,
                           final long listId,
                           final long cardId,
                           final long subtaskId) {
        Board board = getBoard(boardId);
        var cl = board.cardListById(listId);
        var card = cl.cardById(cardId);
        return card.subtaskById(subtaskId);
    }

    void deleteSubtask(final long boardId,
                       final long listId,
                       final long cardId,
                       final long subtaskId) {
        patchCard(boardId, listId, cardId, cp -> {
            cp.deleteSubtask(subtaskId);
            subtaskRepository.deleteById(subtaskId);
        });
    }

    void subtaskMove(final long boardId,
                     final long listId,
                     final long cardId,
                     final long subtaskId,
                     final long otherSubtaskId) {
        patchCard(boardId, listId, cardId, cp -> cp.moveSubtask(subtaskId, otherSubtaskId));
    }

    void setSubtaskName(final long boardId,
                        final long listId,
                        final long cardId,
                        final long subtaskId,
                        final String newName) {
        patchSubtask(boardId, listId, cardId, subtaskId, sp -> sp.setName(newName));
    }

    void setSubtaskCompleteness(final long boardId,
                                final long listId,
                                final long cardId,
                                final long subtaskId,
                                final boolean newCompleteness) {
        patchSubtask(boardId, listId, cardId, subtaskId, sp -> sp.setCompleteness(newCompleteness));
    }

    private void patchBoard(final long boardId,
                            final Consumer<Board.BoardPatcher> patcherConsumer) {
        Board board = getBoard(boardId);
        var bw = messageBroker.getWrapper(board);

        bw.patch(bp -> {
            patcherConsumer.accept(bp);
            boardRepository.save(bp.getBaseBoard());
        });
    }

    private void patchCardList(final long boardId,
                               final long listId,
                               final Consumer<CardList.CardListPatcher> patcherConsumer) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> bp.patchCardList(listId, lp -> {
            patcherConsumer.accept(lp);
            cardListRepository.save(lp.getBaseCardList());
        }));
    }


    private void patchCard(final long board,
                           final long list,
                           final long card,
                           final Consumer<Card.CardPatcher> patcher) {
        Board b = getBoard(board);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> bp.patchCardList(list, lp -> lp.patchCard(card, cp -> {
            patcher.accept(cp);
            cardRepository.save(cp.getBaseCard());
        })));
    }

    private void patchSubtask(final long board,
                              final long list,
                              final long card,
                              final long subtask,
                              final Consumer<CardSubtask.SubtaskPatcher> patcherConsumer) {
        Board b = getBoard(board);
        var bw = messageBroker.getWrapper(b);

        bw.patch(bp -> bp.patchCardList(list, lp -> lp.patchCard(card, cp -> cp.patchSubtask(subtask, sp -> {
            patcherConsumer.accept(sp);
            subtaskRepository.save(sp.getBaseSubtask());
        }))));
    }

    private void patchTag(final long boardId,
                          final long tagId,
                          final Consumer<Tag.TagPatcher> patcherConsumer) {
        Board b = getBoard(boardId);
        var bw = messageBroker.getWrapper(b);

        bw.patch(boardPatcher -> boardPatcher.patchTag(tagId, tagPatcher -> {
            patcherConsumer.accept(tagPatcher);
            tagRepository.save(tagPatcher.getBaseTag());
        }));
    }
}
