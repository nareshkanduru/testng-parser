package com.tupilabs.testng.parser;

import junit.framework.TestCase;

public class TestStatuses extends TestCase {

    // --- getValue() tests ---

    public void testPassGetValue() {
        // PASS enum should store the lowercase string "pass"
        assertEquals("pass", Statuses.PASS.getValue());
    }

    public void testFailGetValue() {
        // FAIL enum should store the lowercase string "fail"
        assertEquals("fail", Statuses.FAIL.getValue());
    }

    public void testSkipGetValue() {
        // SKIP enum should store the lowercase string "skip"
        assertEquals("skip", Statuses.SKIP.getValue());
    }

    // --- toString() tests ---

    public void testPassToString() {
        // toString() delegates to getValue(), so should return "pass"
        assertEquals("pass", Statuses.PASS.toString());
    }

    public void testFailToString() {
        // toString() delegates to getValue(), so should return "fail"
        assertEquals("fail", Statuses.FAIL.toString());
    }

    public void testSkipToString() {
        // toString() delegates to getValue(), so should return "skip"
        assertEquals("skip", Statuses.SKIP.toString());
    }

    public void testToStringMatchesGetValue() {
        // toString() and getValue() must always return the same value for all enum constants
        for (Statuses status : Statuses.values()) {
            assertEquals(status.getValue(), status.toString());
        }
    }

    // --- get(String) happy path tests ---

    public void testGetPassLowercase() {
        // Exact lowercase "pass" should resolve to PASS
        assertEquals(Statuses.PASS, Statuses.get("pass"));
    }

    public void testGetFailLowercase() {
        // Exact lowercase "fail" should resolve to FAIL
        assertEquals(Statuses.FAIL, Statuses.get("fail"));
    }

    public void testGetSkipLowercase() {
        // Exact lowercase "skip" should resolve to SKIP
        assertEquals(Statuses.SKIP, Statuses.get("skip"));
    }

    // --- get(String) case-insensitive tests ---

    public void testGetPassUppercase() {
        // "PASS" in uppercase should resolve to PASS (case-insensitive)
        assertEquals(Statuses.PASS, Statuses.get("PASS"));
    }

    public void testGetFailUppercase() {
        // "FAIL" in uppercase should resolve to FAIL (case-insensitive)
        assertEquals(Statuses.FAIL, Statuses.get("FAIL"));
    }

    public void testGetSkipUppercase() {
        // "SKIP" in uppercase should resolve to SKIP (case-insensitive)
        assertEquals(Statuses.SKIP, Statuses.get("SKIP"));
    }

    public void testGetPassMixedCase() {
        // Mixed case "PaSs" should resolve to PASS (equalsIgnoreCase)
        assertEquals(Statuses.PASS, Statuses.get("PaSs"));
    }

    public void testGetFailMixedCase() {
        // Mixed case "fAiL" should resolve to FAIL (equalsIgnoreCase)
        assertEquals(Statuses.FAIL, Statuses.get("fAiL"));
    }

    public void testGetSkipMixedCase() {
        // Mixed case "sKiP" should resolve to SKIP (equalsIgnoreCase)
        assertEquals(Statuses.SKIP, Statuses.get("sKiP"));
    }

    // --- get(String) returns null for unrecognized values ---

    public void testGetNullInput() {
        // null input should return null without throwing NullPointerException
        // because "pass".equalsIgnoreCase(null) returns false safely
        assertNull(Statuses.get(null));
    }

    public void testGetEmptyString() {
        // Empty string does not match any status, should return null
        assertNull(Statuses.get(""));
    }

    public void testGetUnknownValue() {
        // An unrecognized status string should return null
        assertNull(Statuses.get("error"));
    }

    public void testGetWhitespaceOnly() {
        // Whitespace-only string does not match any status (no trimming), should return null
        assertNull(Statuses.get("   "));
    }

    public void testGetPassWithLeadingSpace() {
        // Leading space means it won't match "pass" exactly, should return null
        assertNull(Statuses.get(" pass"));
    }

    public void testGetPassWithTrailingSpace() {
        // Trailing space means it won't match "pass" exactly, should return null
        assertNull(Statuses.get("pass "));
    }

    public void testGetPassWithSurroundingSpaces() {
        // Surrounding spaces mean it won't match "pass" exactly, should return null
        assertNull(Statuses.get(" pass "));
    }

    // --- Enum structural tests ---

    public void testEnumValuesCount() {
        // There should be exactly 3 enum constants
        assertEquals(3, Statuses.values().length);
    }

    public void testEnumValuesOrder() {
        // Enum constants should be declared in order: PASS, FAIL, SKIP
        Statuses[] values = Statuses.values();
        assertEquals(Statuses.PASS, values[0]);
        assertEquals(Statuses.FAIL, values[1]);
        assertEquals(Statuses.SKIP, values[2]);
    }

    public void testValueOfPass() {
        // Enum.valueOf should work with the enum constant name "PASS"
        assertEquals(Statuses.PASS, Statuses.valueOf("PASS"));
    }

    public void testValueOfFail() {
        // Enum.valueOf should work with the enum constant name "FAIL"
        assertEquals(Statuses.FAIL, Statuses.valueOf("FAIL"));
    }

    public void testValueOfSkip() {
        // Enum.valueOf should work with the enum constant name "SKIP"
        assertEquals(Statuses.SKIP, Statuses.valueOf("SKIP"));
    }

    public void testValueOfInvalidThrowsException() {
        // Enum.valueOf with a non-existent constant name should throw IllegalArgumentException
        try {
            Statuses.valueOf("UNKNOWN");
            fail("Expected IllegalArgumentException for invalid enum constant name");
        } catch (IllegalArgumentException e) {
            // Verify the exception message contains the invalid name
            assertTrue(e.getMessage().contains("UNKNOWN"));
        }
    }

    public void testValueOfLowercaseThrowsException() {
        // Enum.valueOf is case-sensitive; "pass" is not a valid constant name
        try {
            Statuses.valueOf("pass");
            fail("Expected IllegalArgumentException for lowercase enum constant name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("pass"));
        }
    }

    // --- Adversarial inputs to get() ---

    public void testGetSpecialCharacters() {
        // Special characters should not match any status
        assertNull(Statuses.get("p@ss"));
    }

    public void testGetUnicodeInput() {
        // Unicode input should not match any status
        assertNull(Statuses.get("p\u00E4ss"));
    }

    public void testGetSubstringOfValidStatus() {
        // Partial match "pas" should not resolve to PASS
        assertNull(Statuses.get("pas"));
    }

    public void testGetSupersetOfValidStatus() {
        // "passing" contains "pass" but is not equal, should return null
        assertNull(Statuses.get("passing"));
    }

    public void testGetNewlineInValue() {
        // Newline embedded in value should not match
        assertNull(Statuses.get("pass\n"));
    }

    public void testGetTabInValue() {
        // Tab embedded in value should not match
        assertNull(Statuses.get("\tpass"));
    }

    // --- Identity and equality ---

    public void testEnumIdentity() {
        // Enum constants retrieved via get() should be the same instance as the constant
        assertSame(Statuses.PASS, Statuses.get("pass"));
        assertSame(Statuses.FAIL, Statuses.get("fail"));
        assertSame(Statuses.SKIP, Statuses.get("skip"));
    }

    public void testEnumNameVsGetValue() {
        // Enum name() returns "PASS" while getValue() returns "pass" - they differ in case
        assertEquals("PASS", Statuses.PASS.name());
        assertEquals("pass", Statuses.PASS.getValue());
        assertFalse(Statuses.PASS.name().equals(Statuses.PASS.getValue()));
    }
}
