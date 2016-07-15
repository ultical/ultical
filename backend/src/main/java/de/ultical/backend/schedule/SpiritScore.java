package de.ultical.backend.schedule;

import lombok.Data;

@Data
public class SpiritScore {
    private SpiritSheet spiritSheet;
    private String category;
    private int scoreOpponent;
    private int scoreSelf;
}
