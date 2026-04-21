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
        assertTrue(DivisionAge.GREATGRAND.isAgeEligible(48, Gender.MALE));
        assertFalse(DivisionAge.GREATGRAND.isAgeEligible(44, Gender.FEMALE));
        assertTrue(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.FEMALE));
        assertTrue(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.DIVERSE));
        assertFalse(DivisionAge.GREATGRAND.isAgeEligible(45, Gender.NA));
    }

    @Test
    public void underAgeToleranceExistsOnlyForMastersTierDivisions() {
        assertFalse(DivisionAge.REGULAR.isWithinUnderAgeTolerance(10, Gender.MALE));
        assertFalse(DivisionAge.U17.isWithinUnderAgeTolerance(17, Gender.MALE));
        assertFalse(DivisionAge.U23.isWithinUnderAgeTolerance(24, Gender.MALE));
    }

    @Test
    public void mastersUnderAgeToleranceCoversOneYearBelowThreshold() {
        // male: threshold 33, tolerance window [32..32]
        assertTrue(DivisionAge.MASTERS.isWithinUnderAgeTolerance(32, Gender.MALE));
        assertTrue(DivisionAge.MASTERS.isWithinUnderAgeTolerance(32, Gender.NA));
        assertFalse(DivisionAge.MASTERS.isWithinUnderAgeTolerance(31, Gender.MALE));
        // already eligible players are not "within tolerance"
        assertFalse(DivisionAge.MASTERS.isWithinUnderAgeTolerance(33, Gender.MALE));
    }

    @Test
    public void mastersUnderAgeToleranceStacksWithFemaleAndDiverseBonus() {
        // FEMALE/DIVERSE effective age = calendar+3, threshold 33 → window [29..29]
        assertTrue(DivisionAge.MASTERS.isWithinUnderAgeTolerance(29, Gender.FEMALE));
        assertTrue(DivisionAge.MASTERS.isWithinUnderAgeTolerance(29, Gender.DIVERSE));
        assertFalse(DivisionAge.MASTERS.isWithinUnderAgeTolerance(28, Gender.FEMALE));
        assertFalse(DivisionAge.MASTERS.isWithinUnderAgeTolerance(30, Gender.FEMALE));
    }

    @Test
    public void grandmastersAndGreatgrandHaveSameOneYearTolerance() {
        assertTrue(DivisionAge.GRANDMASTERS.isWithinUnderAgeTolerance(39, Gender.MALE));
        assertFalse(DivisionAge.GRANDMASTERS.isWithinUnderAgeTolerance(38, Gender.MALE));
        assertTrue(DivisionAge.GREATGRAND.isWithinUnderAgeTolerance(47, Gender.MALE));
        assertFalse(DivisionAge.GREATGRAND.isWithinUnderAgeTolerance(46, Gender.MALE));
    }
}
