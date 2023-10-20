package commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public final class ColorPreset implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @Column
    private String background;

    @Column
    private String foreground;

    private long board;

    /**
     * package-private setter for the board.
     *
     * @param board The new board.
     */
    void setBoard(final long board) {
        if (this.board != 0 && this.board != board) {
            throw new IllegalStateException();
        }

        this.board = board;
    }

    /**
     * Used on the backend to set the board before saving the preset.
     *
     * @param board The board the preset belongs to.
     */
    public void presaveForBoard(final Board board) {
        setBoard(board.getId());
    }


    /**
     * Constructor.
     */
    public ColorPreset() {
        this.id = 0;
        this.board = 0;
        this.name = null;
        this.foreground = null;
        this.background = null;
    }

    /**
     * Getter for color preset ID.
     *
     * @return Color preset ID.
     */
    @JsonGetter("id")
    public long getId() {
        return id;
    }

    /**
     * Getter for the color preset name.
     *
     * @return The name.
     */
    @JsonGetter("name")
    public String getName() {
        return name;
    }

    /**
     * Getter for the board id.
     *
     * @return Board id.
     */
    @JsonGetter("board")
    public long getBoard() {
        return board;
    }

    /**
     * Getter for background.
     *
     * @return Background.
     */
    @JsonGetter("background")
    public String getBackground() {
        return background;
    }

    /**
     * Getter for foreground.
     *
     * @return Foreground.
     */
    @JsonGetter("foreground")
    public String getForeground() {
        return foreground;
    }

    void setName(final String name) {
        this.name = name;
    }

    void setBackground(final String background) {
        this.background = background;
    }

    void setForeground(final String foreground) {
        this.foreground = foreground;
    }

    /**
     * Clone impl.
     *
     * @return Cloned value.
     */
    @Override
    public ColorPreset clone() {
        try {
            ColorPreset clone = (ColorPreset) super.clone();
            clone.id = this.id;
            clone.board = this.board;
            clone.name = this.name;
            clone.foreground = this.foreground;
            clone.background = this.background;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColorPreset that = (ColorPreset) o;

        if (id != that.id) {
            return false;
        }
        if (board != that.board) {
            return false;
        }
        if (!Objects.equals(name, that.name)) {
            return false;
        }
        if (!Objects.equals(background, that.background)) {
            return false;
        }
        return Objects.equals(foreground, that.foreground);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (background != null ? background.hashCode() : 0);
        result = 31 * result + (foreground != null ? foreground.hashCode() : 0);
        result = 31 * result + (int) (board ^ (board >>> 32));
        return result;
    }
}
