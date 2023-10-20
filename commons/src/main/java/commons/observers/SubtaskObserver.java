package commons.observers;

import commons.CardSubtask;

public interface SubtaskObserver {
    /**
     * Sets the subtask the observer tracks.
     *
     * @param subtask The subtask.
     */
    void setSubtask(CardSubtask subtask);

    /**
     * Announces to the observer that a new name was set.
     *
     * @param newName The new name.
     */
    void nameSet(String newName);

    /**
     * Announces to the observer that a new completeness status was set.
     *
     * @param newIsComplete The new completeness status.
     */
    void completenessUpdated(boolean newIsComplete);
}
