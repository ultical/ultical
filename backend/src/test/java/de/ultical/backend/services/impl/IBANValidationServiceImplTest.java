package de.ultical.backend.services.impl;

import de.ultical.backend.services.IBANValidationService;
import de.ultical.backend.exception.IBANValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;

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
	expected.expect(IBANValidationException.class);
	expected.expectMessage("IBAN-length does not match");
	this.service.validateIBAN("DEtooShort");
    }

    @Test
    public void testIBANtooShortUnknownCountry() throws Exception {
	expected.expect(IBANValidationException.class);
	expected.expectMessage("IBAN-length does not match");
	this.service.validateIBAN("XAtooShort");
    }

    @Test
    public void testIBANtooLongUnknown() throws Exception {
	expected.expect(IBANValidationException.class);
	expected.expectMessage("IBAN-length does not match");
	this.service.validateIBAN("XAcompletelyUselessAndMuchTooLongIBANButForTestingItsMoreThanOK");
    }
    
    @Test
    public void testNullValue() throws Exception {
	expected.expect(IBANValidationException.class);
	expected.expectMessage("NULL");
	this.service.validateIBAN(null);
    }

}
