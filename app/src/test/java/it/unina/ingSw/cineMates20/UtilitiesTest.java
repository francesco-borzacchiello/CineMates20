package it.unina.ingSw.cineMates20;

import org.junit.jupiter.api.Test;

import it.unina.ingSw.cineMates20.view.util.Utilities;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UtilitiesTest {

    /**
       Testing Black-Box:
        isUserNameValid(String)
    */
    @Test
    void isUserNameValidEmptyUsername() {
        assertFalse(Utilities.isUserNameValid(""));
    }

    @Test
    void isUserNameValidNullUsername() {
        assertFalse(Utilities.isUserNameValid(null));
    }

    @Test
    void isUserNameValidStartsWithBlankSpace() {
        assertTrue(Utilities.isUserNameValid(" Username"));
    }

    @Test
    void isUserNameValidStartsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isUserNameValid("     Username"));
    }

    @Test
    void isUserNameValidEndsWithBlankSpace() {
        assertTrue(Utilities.isUserNameValid("Username "));
    }

    @Test
    void isUserNameValidEndsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isUserNameValid("Username    "));
    }

    @Test
    void isUserNameValidStartsAndEndsWithBlankSpace() {
        assertTrue(Utilities.isUserNameValid(" Username "));
    }

    @Test
    void isUserNameValidStartsAndEndsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isUserNameValid("    Username         "));
    }

    @Test
    void isUserNameValidContainsSingleBlankSpace() {
        assertFalse(Utilities.isUserNameValid("User name"));
    }

    @Test
    void isUserNameValidContainsMultipleInnerBlankSpaces() {
        assertFalse(Utilities.isUserNameValid("U   ser na  me"));
    }

    @Test
    void isUserNameValidContainsMultipleBlankSpaces() {
        assertFalse(Utilities.isUserNameValid("   U   ser na  me       "));
    }

    @Test
    void isUserNameValidStartsWithAtChar() {
        assertFalse(Utilities.isUserNameValid("@Username"));
    }

    @Test
    void isUserNameValidContainsOnlyAtChar() {
        assertFalse(Utilities.isUserNameValid("@@@"));
    }

    @Test
    void isUserNameValidStartsWithMultipleAtChar() {
        assertFalse(Utilities.isUserNameValid("@@@@@@@Username"));
    }

    @Test
    void isUserNameValidEndsWithAtChar() {
        assertFalse(Utilities.isUserNameValid("Username@"));
    }

    @Test
    void isUserNameValidEndsWithMultipleAtChar() {
        assertFalse(Utilities.isUserNameValid("Username@@@@@@@@"));
    }

    @Test
    void isUserNameValidContainsAtChar() {
        assertFalse(Utilities.isUserNameValid("Usern@ame"));
    }

    @Test
    void isUserNameValidContainsMultipleAtChar() {
        assertFalse(Utilities.isUserNameValid("Us@@@ern@am@@e"));
    }

    @Test
    void isUserNameValidWithTwoCharactersLength() {
        assertFalse(Utilities.isUserNameValid("us"));
    }

    @Test
    void isUserNameValidWithThreeCharactersLength() {
        assertTrue(Utilities.isUserNameValid("use"));
    }

    @Test
    void isUserNameValidWithFourCharactersLength() {
        assertTrue(Utilities.isUserNameValid("user"));
    }

    /**
     Testing White-Box:
     isUserNameValid(String)
     */
    @Test
    void isUserNameValid_1b_7b() {
        assertFalse(Utilities.isUserNameValid(null));
    }

    @Test
    void isUserNameValid_1b_2b_3b() {
        assertFalse(Utilities.isUserNameValid("u"));
    }

    @Test
    void isUserNameValid_1b_2b_5b() {
        assertTrue(Utilities.isUserNameValid("User"));
    }

    /**
     Testing Black-Box:
     isPasswordValid(String)
     */

    @Test
    void isPasswordValidNullPassword() {
        assertFalse(Utilities.isPasswordValid(null));
    }

    @Test
    void isPasswordValidEmptyPassword() {
        assertFalse(Utilities.isPasswordValid(""));
    }

    @Test
    void isPasswordValidBlankPassword() {
        assertFalse(Utilities.isPasswordValid("    "));
    }

    @Test
    void isPasswordValidWithSevenCharactersLength() {
        assertFalse(Utilities.isPasswordValid("P@ssw0r"));
    }

    @Test
    void isPasswordValidWithEightCharactersLength() {
        assertTrue(Utilities.isPasswordValid("P@ssw0rd"));
    }

    @Test
    void isPasswordValidWithNineCharactersLength() {
        assertTrue(Utilities.isPasswordValid("P@ssw0rd_"));
    }

    @Test
    void isPasswordValidWithoutUppercase() {
        assertFalse(Utilities.isPasswordValid("p@ssw0rd"));
    }

    @Test
    void isPasswordValidWithoutLowercase() {
        assertFalse(Utilities.isPasswordValid("P@SSW0RD"));
    }

    @Test
    void isPasswordValidWithoutNumber() {
        assertFalse(Utilities.isPasswordValid("P@ssword"));
    }

    @Test
    void isPasswordValidWithoutSpecialCharacter() {
        assertFalse(Utilities.isPasswordValid("Passw0rd"));
    }

    @Test
    void isPasswordValidStartsWithBlankSpace() {
        assertTrue(Utilities.isPasswordValid(" P@ssw0rd"));
    }

    @Test
    void isPasswordValidStartsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isPasswordValid("     P@ssw0rd"));
    }

    @Test
    void isPasswordValidEndsWithBlankSpace() {
        assertTrue(Utilities.isPasswordValid("P@ssw0rd "));
    }

    @Test
    void isPasswordValidEndsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isPasswordValid("P@ssw0rd    "));
    }

    @Test
    void isPasswordValidStartsAndEndsWithBlankSpace() {
        assertTrue(Utilities.isPasswordValid(" P@ssw0rd "));
    }

    @Test
    void isPasswordValidStartsAndEndsWithMultipleBlankSpaces() {
        assertTrue(Utilities.isPasswordValid("    P@ssw0rd         "));
    }

    @Test
    void isPasswordValidContainsSingleBlankSpace() {
        assertFalse(Utilities.isPasswordValid("P@ss  w0rd"));
    }

    @Test
    void isPasswordValidContainsMultipleInnerBlankSpaces() {
        assertFalse(Utilities.isPasswordValid("P@ s  sw  0r  d"));
    }

    @Test
    void isPasswordValidContainsMultipleBlankSpaces() {
        assertFalse(Utilities.isPasswordValid("   P   @ s  sw      0r  d       "));
    }

    @Test
    void isPasswordValidLongPassword() {
        assertTrue(Utilities.isPasswordValid("Nu0VAP@SSsSssSSSssSSSSSsssssssssssSSSSSSSSSSSSSSSs"));
    }

    /**
     Testing White-Box:
     isPasswordValid(String)
     */

    @Test
    void isPasswordValid_1b_2b() {
        assertFalse(Utilities.isPasswordValid(null));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_11b() {
        assertFalse(Utilities.isPasswordValid("string"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_14b() {
        assertFalse(Utilities.isPasswordValid("str _ing"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_16b_17b() {
        assertFalse(Utilities.isPasswordValid("str123ing"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_16b_19b_20b() {
        assertFalse(Utilities.isPasswordValid("nu0vap@ss"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_16b_19b_22b_23b() {
        assertFalse(Utilities.isPasswordValid("NU0VAP@SS"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_16b_19b_22b_25b_invalid() {
        assertFalse(Utilities.isPasswordValid("NuVAP@SS"));
    }

    @Test
    void isPasswordValid_1b_4b_5b_6b_7b_8b_10b_13b_16b_19b_22b_25b_valid() {
        assertTrue(Utilities.isPasswordValid("Nu0VAP@SS"));
    }
}