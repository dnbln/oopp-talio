package commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.observers.TagObserver;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Tag is a label that can be applied to a card.
 */
@Entity
public final class Tag implements Serializable {

    @Serial
    private static final long serialVersionUID = 763157006175263256L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @Column
    private String fontColor;

    @Column
    private String backgroundColor;

    @Column
    private long boardId;

    @Transient
    @JsonIgnore
    private ArrayList<TagObserver> observers;

    void setBoardId(final long boardId) {
        if (this.boardId != 0 && this.boardId != boardId) {
            throw new IllegalStateException();
        }

        this.boardId = boardId;
    }

    /**
     * The constructor of this class.
     *
     * @param name  The name of the tag.
     * @param fontColor The color of the tag.
     */
    public Tag(final String name, final String fontColor) {
        this.name = name;
        this.fontColor = fontColor;
    }

    /**
     * No-arg constructor.
     */
    public Tag() {
        this("", "");
    }

    /**
     * One-arg constructor, only name.
     *
     * @param name the name of the Tag.
     */
    public Tag(final String name) {
        this(name, "");
    }

    /**
     * Get id.
     *
     * @return id The value to get.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Get name.
     *
     * @return name The value to get.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get color.
     *
     * @return color The value to get.
     */
    public String getFontColor() {
        return this.fontColor;
    }

    /**
     * Get backgroundColor.
     *
     * @return backgroundColor The value to get.
     */
    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    /**
     * Adds the given observer to the list of observers to notify on changes to the tag.
     *
     * @param observer The observer.
     */
    public void notify(final TagObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        this.observers.add(observer);
        observer.setTag(this);
    }

    /**
     * Equals method for this class.
     *
     * @param o The other object to check the equality with.
     * @return {@code true} if equal, {@code false} if not equal.
     */
    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * Tha hashcode for this class.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getName(), this.getFontColor());
    }

    private void forEachObserver(final Consumer<TagObserver> observerConsumer) {
        if (this.observers != null) {
            this.observers.forEach(observerConsumer);
        }
    }

    public final class TagPatcher {

        /**
         * Returns the tag we are patching.
         *
         * @return The tag.
         */
        public Tag getBaseTag() {
            return Tag.this;
        }

        /**
         * setter for the name.
         *
         * @param newName the title to be set.
         */
        public void setName(final String newName) {
            Tag.this.name = newName;

            Tag.this.forEachObserver(observer -> observer.nameSet(newName));
        }

        /**
         * Sets the font color for the board.
         *
         * @param color The new font color.
         */
        public void setFontColor(final String color) {
            Tag.this.fontColor = color;

            Tag.this.forEachObserver(o -> o.fontColorSet(color));
        }

        /**
         * Sets the font color for the board.
         *
         * @param color The new font color.
         */
        public void setBackgroundColor(final String color) {
            Tag.this.backgroundColor = color;

            Tag.this.forEachObserver(o -> o.backgroundColorSet(color));
        }
    }

    /**
     * Patches the tag, with the given patcher.
     *
     * @param patcher The patcher that will modify the tag.
     */
    public void patch(final Consumer<? super Tag.TagPatcher> patcher) {
        var p = new Tag.TagPatcher();

        patcher.accept(p);
    }

    /**
     * Creates a string of the instance.
     *
     * @return A string representing the instance.
     */
    @Override
    public String toString() {
        return "Tag{id=%d, name='%s', fontColor='%s', backgroundColor='%s', boardId=%d}".formatted(this.id,
                this.name,
                this.fontColor, this.backgroundColor, this.boardId);
    }
}
