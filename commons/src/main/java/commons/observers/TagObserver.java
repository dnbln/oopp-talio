package commons.observers;

import commons.Tag;

public interface TagObserver {

    /**
     * Bind the observer to a specific tag.
     *
     * @param tag The tag.
     */
    void setTag(Tag tag);


    /**
     * Announces to the observer that a new name was set.
     *
     * @param newName the new list.
     */
    void nameSet(String newName);

    /**
     * The background color was set to a new value.
     *
     * @param backgroundColor The new background color.
     */
    void backgroundColorSet(String backgroundColor);

    /**
     * The font color was set to a new value.
     *
     * @param fontColor The new font color.
     */
    void fontColorSet(String fontColor);
}
