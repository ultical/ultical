package de.ultical.backend.model;

import java.util.EnumSet;
import java.util.Set;

public enum DivisionAge {
    U14(13),
    U17(16),
    U20(19),
    U23(23),
    REGULAR(0, true, false),
    MASTERS(33, true, true),
    GRANDMASTERS(40, true, true),
    GREATGRAND(48, true, true);

    /**
     * In masters-tier divisions (MASTERS, GRANDMASTERS, GREATGRAND),
     * FEMALE and DIVERSE players qualify {@value #AGE_BONUS_YEARS} years
     * earlier than MALE or NA players.
     */
    private static final Set<Gender> AGE_BONUS_GENDERS = EnumSet.of(Gender.FEMALE, Gender.DIVERSE);
    private static final int AGE_BONUS_YEARS = 3;

    private final int ageThreshold;
    private final boolean hasToBeOlder;
    private final boolean mastersTierBonus;

    private DivisionAge(final int ageThreshold) {
        this(ageThreshold, false, false);
    }

    private DivisionAge(final int ageThreshold, final boolean hasToBeOlder, final boolean mastersTierBonus) {
        this.ageThreshold = ageThreshold;
        this.hasToBeOlder = hasToBeOlder;
        this.mastersTierBonus = mastersTierBonus;
    }

    /**
     * Tells whether a player with the given calendar-year age
     * ({@code season.year - birthDate.year}) and gender is eligible to play
     * in this age division.
     */
    public boolean isAgeEligible(final int calendarAge, final Gender gender) {
        final int effectiveAge = effectiveAge(calendarAge, gender);
        return this.hasToBeOlder ? effectiveAge >= this.ageThreshold : effectiveAge <= this.ageThreshold;
    }

    private int effectiveAge(final int calendarAge, final Gender gender) {
        if (this.mastersTierBonus && AGE_BONUS_GENDERS.contains(gender)) {
            return calendarAge + AGE_BONUS_YEARS;
        }
        return calendarAge;
    }
}
