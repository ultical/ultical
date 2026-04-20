package de.ultical.backend.model;

public enum Gender {
    MALE, FEMALE, DIVERSE, NA;

    public static Gender robustValueOf(String gender) {
        if ("m".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender) || "männlich".equalsIgnoreCase(gender)) {
            return MALE;
        }
        if ("w".equalsIgnoreCase(gender) || "female".equals(gender) || "weiblich".equalsIgnoreCase(gender)) {
            return FEMALE;
        }
        if ("d".equalsIgnoreCase(gender) || "diverse".equalsIgnoreCase(gender) || "divers".equalsIgnoreCase(gender)) {
            return DIVERSE;
        }
        return NA;
    }
}
