package client.scenes.helperclasses;

import client.scenes.MainCtrl;
import client.utils.ServerUtilsInterface;
import commons.Card;
import commons.CardList;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The custom list view that creates a list of cards.
 */
public class CardListListView extends ListView<Card> {

    private long dragFromIndex = -1;
    private long dragToIndex = -1;
    private static final DataFormat CARD_LIST = new DataFormat("CardList");
    private final ServerUtilsInterface server;
    private final CardList cardList;
    private int dragTurn = 0;

    /**
     * Creates a list view for displaying a list of cards.
     * The card list is associated with a MainCtrl object.
     * The list view allows drag-and-drop operations for moving cards within the list.
     *
     * @param mainCtrl The connection to the main controller.
     * @param server   The connection to the server.
     * @param cardList The list this will represent.
     */
    public CardListListView(final MainCtrl mainCtrl, final ServerUtilsInterface server, final CardList cardList) {
        this.server = server;
        this.cardList = cardList;
        this.setCellFactory(list -> {
            final CardCell cell = new CardCell(mainCtrl);
            cell.setOnDragOver(event -> this.dragToIndex = cell.getCardId());
            cell.setOnMouseEntered(event -> {
                if (!cell.isEmpty()) {
                    this.requestFocus();
                    this.getSelectionModel().select(cell.getIndex());
                }
            });
            return cell;
        });
        this.setOnDragDetected(this::onDragDetected);
        this.setOnDragOver(this::onDragOver);
        this.setOnDragDropped(this::onDragDropped);

        final int someHighNumber = 42069;
        this.setPrefHeight(someHighNumber);
        this.setMaxHeight(Region.USE_PREF_SIZE);
        this.setBackground(null);

        final int minWidth = 250;
        this.setMinWidth(minWidth);
    }

    /**
     * If a drag event is detected in a cell, the {@code dragFromIndex} is set to the index of the cell.
     *
     * @param event the mouse event.
     */
    private void onDragDetected(final MouseEvent event) {
        if (this.getSelectionModel().getSelectedItem() == null ||
            this.getSelectionModel().getSelectedItem().getId() < 0) {
            event.consume();
            return;
        }
        this.dragFromIndex = this.getSelectionModel().getSelectedItem().getId();

        Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
        Map<DataFormat, Object> content = new ClipboardContent();
        content.put(CARD_LIST, new ArrayList<>(this.getSelectionModel().getSelectedItems()));
        dragboard.setContent(content);
        event.consume();
    }

    /**
     * When dragged over the list, set the transfer mode to move. This allows only moving cards.
     *
     * @param event the drag event.
     */
    private void onDragOver(final DragEvent event) {
        if (event.getGestureSource() == this && event.getDragboard().hasContent(CARD_LIST)) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        } else {
            if (event.getGestureSource() != this && event.getDragboard().hasContent(CARD_LIST)) {
                this.dragTurn = 1;
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        }
    }

    /**
     * If the drag event is dropped, then the card gets placed on the index of the cell the mouse was over most
     * recently.
     *
     * @param event the drag event.
     */
    @SuppressWarnings("unchecked")
    private void onDragDropped(final DragEvent event) {
        if (this.dragTurn == 0) {
            this.server.sameListCardMove(this.cardList.getId(), this.dragFromIndex, this.dragToIndex);
        } else {
            final long dragToListIndex = ((List<Card>) event.getDragboard().getContent(CARD_LIST)).get(0).getCardList();
            this.dragFromIndex = (((List<Card>) event.getDragboard().getContent(CARD_LIST)).get(0).getId());
            this.server.xListCardMove(dragToListIndex, this.dragFromIndex, this.cardList.getId(), this.dragToIndex);
        }
        this.dragFromIndex = -1;
        this.dragToIndex = -1;
        event.consume();
    }

    /**
     * Returns the id of the list this list view represents.
     *
     * @return the id of the list this list view represents.
     */
    public long getListId() {
        return this.cardList.getId();
    }

    /**
     * Sets the focus on the card with the given id.
     *
     * @param cardId the id of the card to focus on.
     */
    public void setFocus(final long cardId) {
        for (final Card card : this.getItems()) {
            if (card.getId() == cardId) {
                this.requestFocus();
                this.getSelectionModel().select(card);
                this.scrollTo(card);
                return;
            }
        }
    }
}
