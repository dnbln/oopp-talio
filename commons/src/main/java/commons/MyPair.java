package commons;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Utility pair class.
 * @param <L> the left object
 * @param <R> the right object
 */
public class MyPair<L, R> {
    private L first;
    private R second;

    /**
     * Jackson-compliant constructor.
     * @param first the first object of the pair
     * @param second the second object of the pair
     */
    public MyPair(@JsonProperty("first") final L first, @JsonProperty("second") final R second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Getter for the first object.
     * @return the first object
     */
    public L getFirst() {
        return first;
    }

    /**
     * Getter for the second object.
     * @return the second object
     */
    public R getSecond() {
        return second;
    }
}
