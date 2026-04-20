package de.ultical.backend.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GenderTest {

    @Test
    public void robustValueOfMapsMaleAliases() {
        assertEquals(Gender.MALE, Gender.robustValueOf("m"));
        assertEquals(Gender.MALE, Gender.robustValueOf("M"));
        assertEquals(Gender.MALE, Gender.robustValueOf("male"));
        assertEquals(Gender.MALE, Gender.robustValueOf("Male"));
        assertEquals(Gender.MALE, Gender.robustValueOf("männlich"));
        assertEquals(Gender.MALE, Gender.robustValueOf("Männlich"));
    }

    @Test
    public void robustValueOfMapsFemaleAliases() {
        assertEquals(Gender.FEMALE, Gender.robustValueOf("w"));
        assertEquals(Gender.FEMALE, Gender.robustValueOf("W"));
        assertEquals(Gender.FEMALE, Gender.robustValueOf("female"));
        assertEquals(Gender.FEMALE, Gender.robustValueOf("weiblich"));
        assertEquals(Gender.FEMALE, Gender.robustValueOf("Weiblich"));
    }

    @Test
    public void robustValueOfMapsDiverseAliases() {
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("d"));
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("D"));
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("diverse"));
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("Diverse"));
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("divers"));
        assertEquals(Gender.DIVERSE, Gender.robustValueOf("Divers"));
    }

    @Test
    public void robustValueOfFallsBackToNaForUnknownInput() {
        assertEquals(Gender.NA, Gender.robustValueOf(null));
        assertEquals(Gender.NA, Gender.robustValueOf(""));
        assertEquals(Gender.NA, Gender.robustValueOf("x"));
        assertEquals(Gender.NA, Gender.robustValueOf("unknown"));
    }
}
