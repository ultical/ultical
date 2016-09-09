package de.ultical.backend.model;

public enum Gender {
    MALE, FEMALE, NA;

    public static Gender robustValueOf(String gender) {
        if ("m".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender) || "männlich".equalsIgnoreCase(gender)) {
            return MALE;
        }
        if ("w".equalsIgnoreCase(gender) || "female".equals(gender) || "w".equalsIgnoreCase(gender)
                || "weiblich".equalsIgnoreCase(gender)) {
            return FEMALE;
        }
        return NA;
    }
}
