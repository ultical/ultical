package de.ultical.backend.model;

public enum DivisionAge {
    U14(13), U17(16), U20(19), U23(23), REGULAR(0, true), MASTERS(33, true), GRANDMASTERS(40, true), GREATGRAND(48, true);

    private final int ageDifference;
    private final boolean hasToBeOlder;

    private DivisionAge(final int ageDiff) {
        this(ageDiff, false);
    }

    private DivisionAge(final int ageDiff, final boolean older) {
        this.ageDifference = ageDiff;
        this.hasToBeOlder = older;
    }

    public int getAgeDifference() {
        return this.ageDifference;
    }

    public boolean isHasToBeOlder() {
        return this.hasToBeOlder;
    }
}
