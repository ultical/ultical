package de.ultical.backend.schedule;

import lombok.Data;

@Data
public class SpiritVersion {

    private boolean own;
    private int maxScore;
    private int minScore;

    public boolean hasOwn() {
        return this.isOwn();
    }
}
