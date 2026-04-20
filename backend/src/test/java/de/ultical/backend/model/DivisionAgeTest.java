package de.ultical.backend.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DivisionAgeTest {

    @Test
    public void regularAcceptsAnyAge() {
        assertTrue(DivisionAge.REGULAR.isAgeEligible(0, Gender.MALE));
        assertTrue(DivisionAge.REGULAR.isAgeEligible(10, Gender.FEMALE));
        assertTrue(DivisionAge.REGULAR.isAgeEligible(75, Gender.DIVERSE));
        assertTrue(DivisionAge.REGULAR.isAgeEligible(120, Gender.NA));
    }

    @Test
    public void uDivisionsCapAtTheirThresholdRegardlessOfGender() {
        // U17 allows calendar age 0..16
        assertTrue(DivisionAge.U17.isAgeEligible(16, Gender.MALE));
        assertTrue(DivisionAge.U17.isAgeEligible(16, Gender.FEMALE));
        assertTrue(DivisionAge.U17.isAgeEligible(16, Gender.DIVERSE));
        assertFalse(DivisionAge.U17.isAgeEligible(17, Gender.MALE));
        assertFalse(DivisionAge.U17.isAgeEligible(17, Gender.FEMALE));
        assertFalse(DivisionAge.U17.isAgeEligible(17, Gender.DIVERSE));
    }

    @Test
    public void mastersThresholdIs33ForMaleAndNa() {
        assertFalse(DivisionAge.MASTERS.isAgeEligible(32, Gender.MALE));
        assertFalse(DivisionAge.MASTERS.isAgeEligible(32, Gender.NA));
        assertTrue(DivisionAge.MASTERS.isAgeEligible(33, Gender.MALE));
        assertTrue(DivisionAge.MASTERS.isAgeEligible(33, Gender.NA));
    }

    @Test
    public void mastersThresholdDropsBy3ForFemaleAndDiverse() {
        assertFalse(DivisionAge.MASTERS.isAgeEligible(29, Gender.FEMALE));
        assertFalse(DivisionAge.MASTERS.isAgeEligible(29, Gender.DIVERSE));
        assertTrue(DivisionAge.MASTERS.isAgeEligible(30, Gender.FEMALE));
        assertTrue(DivisionAge.MASTERS.isAgeEligible(30, Gender.DIVERSE));
    }

    @Test
    public void grandmastersBonusAppliesToFemaleAndDiverse() {
        assertFalse(DivisionAge.GRANDMASTERS.isAgeEligible(39, Gender.MALE));
        assertTrue(DivisionAge.GRANDMASTERS.isAgeEligible(40, Gender.MALE));
        assertFalse(DivisionAge.GRANDMASTERS.isAgeEligible(36, Gender.FEMALE));
        assertTrue(DivisionAge.GRANDMASTERS.isAgeEligible(37, Gender.FEMALE));
        assertTrue(DivisionAge.GRANDMASTERS.isAgeEligible(37, Gender.DIVERSE));
        assertFalse(DivisionAge.GRANDMASTERS.isAgeEligible(37, Gender.NA));
    }

    @Test
    public void greatgrandBonusAppliesToFemaleAndDiverse() {
        assertFalse(DivisionAge.GREATGRAND.isAgeEligible(47, Gender.MALE));
        assertTrue(DivisionAge.GREATGRAND.isAgeElim gible(48, Gender.MALE));
        assertFalse(DivisionAge.GREATGRAND.isAgeEligible(44, Gender.FEMALE));
        assertTrue(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.FEMALE));
        assertTrue(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.DIVERSE));
        assertFalse(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.NA));
    }
}
