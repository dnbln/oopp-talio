package commons.patchExceptions;

import java.util.NoSuchElementException;

public class NoSuchColorPreset extends NoSuchElementException {
    private final long presetId;

    /**
     * Constructor.
     *
     * @param presetId Preset id.
     */
    public NoSuchColorPreset(final long presetId) {
        super("No preset with id " + presetId);
        this.presetId = presetId;
    }

    /**
     * Getter for preset id.
     *
     * @return Preset id.
     */
    public long getPresetId() {
        return presetId;
    }
}
