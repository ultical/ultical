package de.ultical.backend.model;

import java.util.EnumSet;
import java.util.Set;

public enum DivisionAge {
    U12(11),
    U14(13),
    U17(16),
    U20(19),
    U23(23),
    REGULAR(0, true, false, 0),
    MASTERS(33, true, true, 1),
    GRANDMASTERS(40, true, true, 1),
    GREATGRAND(48, true, true, 1);

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
    /**
     * Years of tolerance below {@link #ageThreshold} granted on a per-roster
     * quota basis (see RosterResource). Zero means no under-age exception.
     */
    private final int underAgeToleranceYears;

    private DivisionAge(final int ageThreshold) {
        this(ageThreshold, false, false, 0);
    }

    private DivisionAge(final int ageThreshold, final boolean hasToBeOlder, final boolean mastersTierBonus,
            final int underAgeToleranceYears) {
        this.ageThreshold = ageThreshold;
        this.hasToBeOlder = hasToBeOlder;
        this.mastersTierBonus = mastersTierBonus;
        this.underAgeToleranceYears = underAgeToleranceYears;
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

    /**
     * Tells whether the player is not eligible per {@link #isAgeEligible} but
     * is within {@link #underAgeToleranceYears} of the threshold. Callers must
     * still enforce the per-roster quota.
     */
    public boolean isWithinUnderAgeTolerance(final int calendarAge, final Gender gender) {
        if (!this.hasToBeOlder || this.underAgeToleranceYears == 0) {
            return false;
        }
        final int effectiveAge = effectiveAge(calendarAge, gender);
        return effectiveAge < this.ageThreshold
                && effectiveAge >= this.ageThreshold - this.underAgeToleranceYears;
    }

    private int effectiveAge(final int calendarAge, final Gender gender) {
        if (this.mastersTierBonus && AGE_BONUS_GENDERS.contains(gender)) {
            return calendarAge + AGE_BONUS_YEARS;
        }
        return calendarAge;
    }
}
