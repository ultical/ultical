package de.ultical.backend.services.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ultical.backend.exception.IBANValidationException;
import de.ultical.backend.services.IBANValidationService;

public class IBANValidationServiceImplTest {

    private IBANValidationService service;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        this.service = new IBANValidationServiceImpl();
    }

    @Test
    public void testValidIBAN() throws Exception {
        this.service.validateIBAN("DE02120300000000202051");
        this.service.validateIBAN("AT026000000001349870");
        this.service.validateIBAN("CH0209000000100013997");
        this.service.validateIBAN("AA4800000000001");
    }

    @Test
    public void testIBANtooShort() throws Exception {
        this.expected.expect(IBANValidationException.class);
        this.expected.expectMessage("IBAN-length does not match");
        this.service.validateIBAN("DEtooShort");
    }

    @Test
    public void testIBANtooShortUnknownCountry() throws Exception {
        this.expected.expect(IBANValidationException.class);
        this.expected.expectMessage("IBAN-length does not match");
        this.service.validateIBAN("XAtooShort");
    }

    @Test
    public void testIBANtooLongUnknown() throws Exception {
        this.expected.expect(IBANValidationException.class);
        this.expected.expectMessage("IBAN-length does not match");
        this.service.validateIBAN("XAcompletelyUselessAndMuchTooLongIBANButForTestingItsMoreThanOK");
    }

    @Test
    public void testNullValue() throws Exception {
        this.expected.expect(IBANValidationException.class);
        this.expected.expectMessage("NULL");
        this.service.validateIBAN(null);
    }

    @Test
    public void testInvalidCharacters() throws Exception {
        this.expected.expect(IBANValidationException.class);
        this.expected.expectMessage("invalid character € in iban");
        this.service.validateIBAN("CH020900€000100013997");
    }
}
