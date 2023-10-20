package commons.observers;

import commons.Board;
import commons.Card;
import commons.CardList;
import commons.ColorPreset;
import commons.Tag;

public interface BoardObserver {
    /**
     * Binds the observer to a specific board.
     *
     * @param board The board
     */
    void setBoard(Board board);

    /**
     * Announces to the observer that a new title was set.
     *
     * @param newTitle the new list.
     */
    void titleSet(String newTitle);

    /**
     * Announces to the observer that a new list was created.
     *
     * @param list The list.
     */
    void listCreated(CardList list);

    /**
     * A list was removed (deleted).
     *
     * @param list The list id.
     */
    void listRemoved(CardList list);

    /**
     * Lists were reordered.
     *
     * @param list        The list that was moved.
     * @param placedAfter The list right before the new position
     *                    of the moved list, or null if the list
     *                    was moved before all the others.
     */
    void listsReordered(CardList list, CardList placedAfter);

    /**
     * A card was moved between 2 different lists.
     *
     * @param srcList  The source card list.
     * @param card     The card that was moved.
     * @param destList The destination card list.
     * @param hook     The "hook" card that the moved card was placed after, or
     *                 null if the card was moved to the very beginning of the list.
     */
    void xListCardMoved(CardList srcList, Card card, CardList destList, Card hook);

    /**
     * A tag was added to the board.
     *
     * @param tag The new tag.
     */
    void tagAdded(Tag tag);

    /**
     * A tag was deleted from the board.
     *
     * @param tag The deleted tag.
     */
    void tagRemoved(Tag tag);

    /**
     * The font color was set to a new value.
     *
     * @param fontColor The new font color.
     */
    void fontColorSet(String fontColor);

    /**
     * The background color was set to a new value.
     *
     * @param backgroundColor The new background color.
     */
    void backgroundColorSet(String backgroundColor);

    /**
     * A new color preset was created.
     *
     * @param preset The new color preset.
     */
    void colorPresetCreated(ColorPreset preset);

    /**
     * A color preset was deleted.
     *
     * @param preset The deleted color preset.
     */
    void colorPresetRemoved(ColorPreset preset);

    /**
     * A new color preset default was set.
     *
     * @param colorPresetKey The key of the new defeault color preset.
     */
    void defaultCardColorPresetSet(long colorPresetKey);

    /**
     * The name for a color preset was changed.
     *
     * @param presetKey The key of the preset whose name was changed.
     * @param newName   The new name for the preset.
     */
    void colorPresetNameSet(long presetKey, String newName);

    /**
     * The font color of a preset has been changed.
     *
     * @param presetKey    The key of the preset whose font color was changed.
     * @param newFontColor The new font color.
     */
    void colorPresetFontColorSet(long presetKey, String newFontColor);

    /**
     * The background color of a preset has been changed.
     *
     * @param presetKey          The key of the preset whose background color was changed.
     * @param newBackgroundColor The new background color.
     */
    void colorPresetBackgroundColorSet(long presetKey, String newBackgroundColor);

    /**
     * A board has been removed.
     */
    void boardRemoved();
}
