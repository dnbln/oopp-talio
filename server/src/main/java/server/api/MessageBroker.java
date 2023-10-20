package server.api;

import commons.Board;
import commons.CardList;
import commons.observers.BoardObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class MessageBroker {
    private final HashMap<Long, ArrayList<BoardObserver>> boardObservers;

    /**
     * Constructor.
     */
    public MessageBroker() {
        this.boardObservers = new HashMap<>();
    }

    /**
     * Gets a BoardWrapper from the given Board.
     *
     * @param b The Board.
     * @return The BoardWrapper.
     */
    public BoardWrapper getWrapper(final Board b) {
        return new BoardWrapper(b);
    }

    private static <T> void addObserver(final HashMap<Long, ArrayList<T>> observers, final long id, final T observer) {
        observers.putIfAbsent(id, new ArrayList<>());

        observers.get(id).add(observer);
    }

    private static <T> void removeObserver(
            final HashMap<Long, ArrayList<T>> observers,
            final long id,
            final T observer) {
        var o = observers.get(id);
        if (o != null) {
            o.remove(observer);
            if (o.isEmpty()) {
                observers.remove(id);
            }
        }
    }

    /**
     * Adds a board observer to the list of observers tracked by the MessageBroker.
     *
     * @param boardId  The id of the board that the observer should track.
     * @param observer The observer.
     */
    public void addBoardObserver(final long boardId, final BoardObserver observer) {
        addObserver(this.boardObservers, boardId, observer);
    }

    /**
     * Removes an observer from the list of observers tracked by the MessageBroker.
     *
     * @param boardId  The id of the board that the observer tracked.
     * @param observer The observer.
     */
    public void removeBoardObserver(final long boardId, final BoardObserver observer) {
        removeObserver(this.boardObservers, boardId, observer);
    }

    /**
     * A wrapper over the board, that adds the MessageBroker observers before patching the board.
     */
    public final class BoardWrapper {
        private final Board b;
        private boolean prepared = false;

        private BoardWrapper(final Board b) {
            this.b = b;
        }

        private void prepareForPatch() {
            if (prepared) {
                return;
            }

            prepared = true;
            var boardObservers = MessageBroker.this.boardObservers.get(this.b.getId());
            if (boardObservers != null) {
                for (var observer : boardObservers) {
                    b.notify(observer);
                }
            }
        }

        /**
         * Patches the board.
         *
         * @param patcher The patcher.
         */
        public void patch(final Consumer<Board.BoardPatcher> patcher) {
            this.prepareForPatch();

            b.patch(patcher);
        }

        /**
         * Getter for the board id.
         *
         * @return The board id.
         */
        public long getId() {
            return b.getId();
        }

        /**
         * Getter for the CardLists.
         *
         * @return The card lists.
         */
        public List<CardList> getCardLists() {
            return b.getCardLists();
        }
    }
}
