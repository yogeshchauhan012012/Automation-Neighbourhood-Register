package TestRegister;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Base.BaseTest;
import Pages.RegisterHouseholdPage;
import Pages.ReviewApplicationPage;
import Utils.TestDataFactory;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════╗
 * ║    FIELD VALIDATION TEST SUITE — Register household (Step 1 form)                 ║
 * ║    Page Object: RegisterHouseholdPage                    Total Test Cases: 52     ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  A. EMPTY SUBMISSION / ERROR SUMMARY                │  1 – 8   │   8  │           ║
 * ║  B. FULL NAME                                       │  9 – 17  │   9  │           ║
 * ║  C. GENDER                                          │ 18 – 19  │   2  │           ║
 * ║  D. MOBILE NUMBER                                   │ 20 – 28  │   9  │           ║
 * ║  E. FAMILY MEMBERS                                  │ 29 – 35  │   7  │           ║
 * ║  F. RESIDENTIAL ADDRESS                             │ 36 – 41  │   6  │           ║
 * ║  G. IDENTITY PROOF TYPE                             │ 42 – 43  │   2  │           ║
 * ║  H. DOCUMENT REFERENCE (demo-data guard)            │ 44 – 50  │   7  │           ║
 * ║  I. ADDITIONAL NOTES                                │ 51 – 52  │   2  │           ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  DESIGN: every test reloads the page in @BeforeMethod, so tests are independent.  ║
 * ║  Negative tests fill every other field with valid data and change only the field  ║
 * ║  under test, then assert the exact message the portal shows.                      ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class RegisterFieldValidationTest extends BaseTest {

    private RegisterHouseholdPage form;
    private ReviewApplicationPage review;

    // Messages exactly as shown by the portal
    private static final String MSG_NAME_EMPTY = "Enter the full name of the household head.";
    private static final String MSG_NAME_SHORT = "Name must be at least 2 characters.";
    private static final String MSG_NAME_CHARS = "Name can contain letters, spaces, dots, apostrophes and hyphens only.";
    private static final String MSG_GENDER = "Select a gender option.";
    private static final String MSG_MOBILE_EMPTY = "Enter a mobile number.";
    private static final String MSG_MOBILE_DIGITS = "Mobile number can contain digits only.";
    private static final String MSG_MOBILE_LENGTH = "Enter a 10-digit mobile number.";
    private static final String MSG_MOBILE_START = "Indian mobile numbers start with 6, 7, 8 or 9.";
    private static final String MSG_FAMILY_EMPTY = "Enter how many family members live in the house.";
    private static final String MSG_FAMILY_WHOLE = "Enter a whole number, like 4.";
    private static final String MSG_FAMILY_MIN = "There must be at least 1 family member.";
    private static final String MSG_FAMILY_MAX = "Enter 20 or fewer. Mention larger households in the notes.";
    private static final String MSG_ADDRESS_EMPTY = "Enter your complete residential address.";
    private static final String MSG_ADDRESS_SHORT = "Address looks too short. Include flat number, society name and city.";
    private static final String MSG_ADDRESS_TAGS = "Address can’t contain < or > characters.";
    private static final String MSG_ID_TYPE = "Choose an identity proof type.";
    private static final String MSG_DOC_LENGTH = "Use 4 to 30 characters, or leave this blank.";
    private static final String MSG_DOC_CHARS = "Use letters, numbers, spaces, hyphens or slashes only.";
    private static final String MSG_DOC_AADHAAR = "This looks like a real Aadhaar number. Enter a made-up value such as DEMO-1234.";
    private static final String MSG_DOC_PAN = "This looks like a real PAN. Enter a made-up value such as DEMO-1234.";
    private static final String MSG_DOC_VOTER = "This looks like a real Voter ID. Enter a made-up value such as DEMO-1234.";

    @BeforeClass
    public void initPages() {
        form = new RegisterHouseholdPage(driver, explicitWait);
        review = new ReviewApplicationPage(driver, explicitWait);
    }

    @BeforeMethod
    public void reloadPage() {
        form.open(baseUrl);
    }

    /** Fills every required field with valid data. Tests then overwrite one field. */
    private void fillValidForm() {
        form.fillRequiredFields(TestDataFactory.VALID_NAME, TestDataFactory.VALID_GENDER,
                TestDataFactory.uniqueMobile(), TestDataFactory.VALID_FAMILY,
                TestDataFactory.VALID_ADDRESS, TestDataFactory.VALID_ID_TYPE);
    }

    private void assertSubmitShowsError(String field, String expected) {
        form.clickReviewApplication();
        Assert.assertEquals(form.waitForFieldError(field), expected, "Inline error for " + field);
        Assert.assertEquals(form.getAriaInvalid(field), "true", field + " should be marked aria-invalid");
        Assert.assertTrue(form.isFormPanelDisplayed(), "User should stay on the form step");
        Assert.assertFalse(review.isReviewPanelDisplayed(), "Review step must not open with invalid data");
    }

    private void assertSubmitAccepted() {
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertTrue(review.isReviewPanelDisplayed(), "Valid data should open the Review step");
    }

    // ====================================================================
    // A. EMPTY SUBMISSION / ERROR SUMMARY  (Priority 1 – 8)
    // ====================================================================

    @Test(priority = 1, description = "NR-VAL-TC001: Submitting an empty form shows the error summary")
    public void VAL_TC001_emptySubmitShowsErrorSummary() {
        form.clickReviewApplication();
        Assert.assertTrue(form.isErrorSummaryDisplayed(), "Error summary should be visible");
        Assert.assertEquals(form.getErrorSummaryHeading(), "Fix these before continuing");
    }

    @Test(priority = 2, description = "NR-VAL-TC002: Empty submit lists exactly the 6 required fields in form order")
    public void VAL_TC002_emptySubmitListsSixRequiredFields() {
        form.clickReviewApplication();
        List<String> items = form.getErrorSummaryItems();
        Assert.assertEquals(items, List.of(
                "Full name: " + MSG_NAME_EMPTY,
                "Gender: " + MSG_GENDER,
                "Mobile number: " + MSG_MOBILE_EMPTY,
                "Residential address: " + MSG_ADDRESS_EMPTY,
                "Family members: " + MSG_FAMILY_EMPTY,
                "Identity proof type: " + MSG_ID_TYPE));
    }

    @Test(priority = 3, description = "NR-VAL-TC003: Empty submit shows inline errors under every required field")
    public void VAL_TC003_emptySubmitShowsInlineErrors() {
        form.clickReviewApplication();
        Assert.assertEquals(form.getFieldError("fullName"), MSG_NAME_EMPTY);
        Assert.assertEquals(form.getFieldError("gender"), MSG_GENDER);
        Assert.assertEquals(form.getFieldError("mobile"), MSG_MOBILE_EMPTY);
        Assert.assertEquals(form.getFieldError("address"), MSG_ADDRESS_EMPTY);
        Assert.assertEquals(form.getFieldError("familyMembers"), MSG_FAMILY_EMPTY);
        Assert.assertEquals(form.getFieldError("idType"), MSG_ID_TYPE);
    }

    @Test(priority = 4, description = "NR-VAL-TC004: Empty submit does not flag optional fields")
    public void VAL_TC004_optionalFieldsNotFlagged() {
        form.clickReviewApplication();
        Assert.assertEquals(form.getFieldError("docRef"), "");
        Assert.assertEquals(form.getFieldError("photo"), "");
        Assert.assertEquals(form.getFieldError("notes"), "");
    }

    @Test(priority = 5, description = "NR-VAL-TC005: Keyboard focus moves to the error summary after a failed submit")
    public void VAL_TC005_focusMovesToErrorSummary() {
        form.clickReviewApplication();
        Assert.assertTrue(form.isErrorSummaryFocused(), "Error summary should receive focus");
    }

    @Test(priority = 6, description = "NR-VAL-TC006: Clicking a summary link focuses the matching field")
    public void VAL_TC006_summaryLinkFocusesField() {
        form.clickReviewApplication();
        form.clickErrorSummaryLink("Mobile number");
        Assert.assertEquals(form.getActiveElementId(), "mobile");
        form.clickErrorSummaryLink("Identity proof type");
        Assert.assertEquals(form.getActiveElementId(), "idType");
    }

    @Test(priority = 7, description = "NR-VAL-TC007: Gender summary link focuses the first gender radio")
    public void VAL_TC007_genderSummaryLinkFocusesFirstRadio() {
        form.clickReviewApplication();
        form.clickErrorSummaryLink("Gender");
        Assert.assertEquals(form.getActiveElementId(), "gender-female");
    }

    @Test(priority = 8, description = "NR-VAL-TC008: Fixing all errors and resubmitting hides the summary and opens Review")
    public void VAL_TC008_fixingErrorsOpensReview() {
        form.clickReviewApplication();
        Assert.assertTrue(form.isErrorSummaryDisplayed());
        fillValidForm();
        assertSubmitAccepted();
        Assert.assertFalse(form.isErrorSummaryDisplayed(), "Summary should be hidden after a valid submit");
    }

    // ====================================================================
    // B. FULL NAME  (Priority 9 – 17)
    // ====================================================================

    @Test(priority = 9, description = "NR-VAL-TC009: Whitespace-only name is treated as empty")
    public void VAL_TC009_whitespaceOnlyName() {
        fillValidForm();
        form.enterFullName("     ");
        assertSubmitShowsError("fullName", MSG_NAME_EMPTY);
    }

    @Test(priority = 10, description = "NR-VAL-TC010: Single-character name is rejected (min 2)")
    public void VAL_TC010_singleCharacterName() {
        fillValidForm();
        form.enterFullName("A");
        assertSubmitShowsError("fullName", MSG_NAME_SHORT);
    }

    @Test(priority = 11, description = "NR-VAL-TC011: Name with digits is rejected")
    public void VAL_TC011_nameWithDigits() {
        fillValidForm();
        form.enterFullName("Rahul 2");
        assertSubmitShowsError("fullName", MSG_NAME_CHARS);
    }

    @Test(priority = 12, description = "NR-VAL-TC012: Name with special characters (@, #) is rejected")
    public void VAL_TC012_nameWithSpecialCharacters() {
        fillValidForm();
        form.enterFullName("Rahul@Sharma#");
        assertSubmitShowsError("fullName", MSG_NAME_CHARS);
    }

    @Test(priority = 13, description = "NR-VAL-TC013: Name starting with a dot is rejected (must start with a letter)")
    public void VAL_TC013_nameStartingWithDot() {
        fillValidForm();
        form.enterFullName(".Rahul");
        assertSubmitShowsError("fullName", MSG_NAME_CHARS);
    }

    @Test(priority = 14, description = "NR-VAL-TC014: Name with dot, apostrophe and hyphen is accepted")
    public void VAL_TC014_nameWithAllowedPunctuation() {
        fillValidForm();
        form.enterFullName("Mary O'Brien-Dsouza Jr.");
        assertSubmitAccepted();
    }

    @Test(priority = 15, description = "NR-VAL-TC015: Name with accented / Devanagari letters is accepted")
    public void VAL_TC015_unicodeNameAccepted() {
        fillValidForm();
        form.enterFullName("José राहुल");
        assertSubmitAccepted();
    }

    @Test(priority = 16, description = "NR-VAL-TC016: Name field stops input at 80 characters (maxlength)")
    public void VAL_TC016_nameMaxLength() {
        Assert.assertEquals(form.getMaxLength("fullName"), "80");
        form.enterFullName(TestDataFactory.repeat('a', 85));
        Assert.assertEquals(form.getFullNameValue().length(), 80, "Input should be capped at 80 characters");
    }

    @Test(priority = 17, description = "NR-VAL-TC017: Two-character name is accepted (boundary)")
    public void VAL_TC017_twoCharacterNameAccepted() {
        fillValidForm();
        form.enterFullName("Al");
        assertSubmitAccepted();
    }

    // ====================================================================
    // C. GENDER  (Priority 18 – 19)
    // ====================================================================

    @Test(priority = 18, description = "NR-VAL-TC018: Missing gender is rejected when all else is valid")
    public void VAL_TC018_missingGender() {
        form.enterFullName(TestDataFactory.VALID_NAME);
        form.enterMobile(TestDataFactory.uniqueMobile());
        form.enterFamilyMembers(TestDataFactory.VALID_FAMILY);
        form.enterAddress(TestDataFactory.VALID_ADDRESS);
        form.selectIdType(TestDataFactory.VALID_ID_TYPE);
        assertSubmitShowsError("gender", MSG_GENDER);
    }

    @Test(priority = 19, description = "NR-VAL-TC019: Selecting a gender clears the gender error immediately")
    public void VAL_TC019_selectingGenderClearsError() {
        form.clickReviewApplication();
        Assert.assertEquals(form.getFieldError("gender"), MSG_GENDER);
        form.selectGender("Prefer not to say");
        Assert.assertEquals(form.getFieldError("gender"), "", "Gender error should clear on selection");
        Assert.assertNull(form.getAriaInvalid("gender"), "aria-invalid should be removed");
    }

    // ====================================================================
    // D. MOBILE NUMBER  (Priority 20 – 28)
    // ====================================================================

    @Test(priority = 20, description = "NR-VAL-TC020: Mobile with fewer than 10 digits is rejected")
    public void VAL_TC020_mobileTooShort() {
        fillValidForm();
        form.enterMobile("98765");
        assertSubmitShowsError("mobile", MSG_MOBILE_LENGTH);
    }

    @Test(priority = 21, description = "NR-VAL-TC021: Mobile with more than 10 digits is rejected")
    public void VAL_TC021_mobileTooLong() {
        fillValidForm();
        form.enterMobile("98765432101");
        assertSubmitShowsError("mobile", MSG_MOBILE_LENGTH);
    }

    @Test(priority = 22, description = "NR-VAL-TC022: Mobile starting with 0-5 (after normalising) is rejected")
    public void VAL_TC022_mobileInvalidStartDigit() {
        fillValidForm();
        form.enterMobile("5876543210");
        assertSubmitShowsError("mobile", MSG_MOBILE_START);
    }

    @Test(priority = 23, description = "NR-VAL-TC023: Letters typed into mobile are stripped as you type")
    public void VAL_TC023_mobileLettersStripped() {
        form.enterMobile("98ab76cd54");
        Assert.assertEquals(form.getMobileValue(), "987654", "Only digits, +, space and hyphen should remain");
    }

    @Test(priority = 24, description = "NR-VAL-TC024: A '+' in the middle of the number is rejected")
    public void VAL_TC024_mobilePlusInMiddle() {
        fillValidForm();
        form.enterMobile("98765+43210");
        assertSubmitShowsError("mobile", MSG_MOBILE_DIGITS);
    }

    @Test(priority = 25, description = "NR-VAL-TC025: Mobile with +91 prefix and spaces is accepted and normalised")
    public void VAL_TC025_mobileWithCountryCode() {
        fillValidForm();
        form.enterMobile("+91 98765 43210");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Mobile number"), "+91 98765 43210");
    }

    @Test(priority = 26, description = "NR-VAL-TC026: Mobile with leading 0 (11 digits) is accepted")
    public void VAL_TC026_mobileWithLeadingZero() {
        fillValidForm();
        form.enterMobile("09876543210");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Mobile number"), "+91 98765 43210");
    }

    @Test(priority = 27, description = "NR-VAL-TC027: Mobile with 91 prefix (12 digits) and hyphen is accepted")
    public void VAL_TC027_mobileWith91PrefixAndHyphen() {
        fillValidForm();
        form.enterMobile("91-98765-43210");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Mobile number"), "+91 98765 43210");
    }

    @Test(priority = 28, description = "NR-VAL-TC028: Mobile starting with 6 is accepted (boundary)")
    public void VAL_TC028_mobileStartingWithSix() {
        fillValidForm();
        form.enterMobile("6000000000");
        assertSubmitAccepted();
    }

    // ====================================================================
    // E. FAMILY MEMBERS  (Priority 29 – 35)
    // ====================================================================

    @Test(priority = 29, description = "NR-VAL-TC029: Family members = 0 is rejected")
    public void VAL_TC029_familyZero() {
        fillValidForm();
        form.enterFamilyMembers("0");
        assertSubmitShowsError("familyMembers", MSG_FAMILY_MIN);
    }

    @Test(priority = 30, description = "NR-VAL-TC030: Family members = 21 is rejected")
    public void VAL_TC030_familyTwentyOne() {
        fillValidForm();
        form.enterFamilyMembers("21");
        assertSubmitShowsError("familyMembers", MSG_FAMILY_MAX);
    }

    @Test(priority = 31, description = "NR-VAL-TC031: Decimal family members (2.5) is rejected")
    public void VAL_TC031_familyDecimal() {
        fillValidForm();
        form.enterFamilyMembers("2.5");
        assertSubmitShowsError("familyMembers", MSG_FAMILY_WHOLE);
    }

    @Test(priority = 32, description = "NR-VAL-TC032: Negative family members (-3) is rejected")
    public void VAL_TC032_familyNegative() {
        fillValidForm();
        form.enterFamilyMembers("-3");
        assertSubmitShowsError("familyMembers", MSG_FAMILY_WHOLE);
    }

    @Test(priority = 33, description = "NR-VAL-TC033: Letters cannot be entered in family members (number input)")
    public void VAL_TC033_familyLettersIgnored() {
        fillValidForm();
        form.enterFamilyMembers("abc");
        Assert.assertEquals(form.getFamilyMembersValue(), "", "Number input should hold no value for letters");
        assertSubmitShowsError("familyMembers", MSG_FAMILY_EMPTY);
    }

    @Test(priority = 34, description = "NR-VAL-TC034: Family members = 1 is accepted (lower boundary)")
    public void VAL_TC034_familyOneAccepted() {
        fillValidForm();
        form.enterFamilyMembers("1");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Family members"), "1");
    }

    @Test(priority = 35, description = "NR-VAL-TC035: Family members = 20 is accepted (upper boundary)")
    public void VAL_TC035_familyTwentyAccepted() {
        fillValidForm();
        form.enterFamilyMembers("20");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Family members"), "20");
    }

    // ====================================================================
    // F. RESIDENTIAL ADDRESS  (Priority 36 – 41)
    // ====================================================================

    @Test(priority = 36, description = "NR-VAL-TC036: Address shorter than 15 characters is rejected")
    public void VAL_TC036_addressTooShort() {
        fillValidForm();
        form.enterAddress("Flat 1, Delhi");
        assertSubmitShowsError("address", MSG_ADDRESS_SHORT);
    }

    @Test(priority = 37, description = "NR-VAL-TC037: Address of exactly 15 characters is accepted (boundary)")
    public void VAL_TC037_addressFifteenChars() {
        fillValidForm();
        form.enterAddress("Flat 12, Noida.");
        assertSubmitAccepted();
    }

    @Test(priority = 38, description = "NR-VAL-TC038: Address containing < or > is rejected")
    public void VAL_TC038_addressWithAngleBrackets() {
        fillValidForm();
        form.enterAddress("Flat 1 <b>Green Meadows</b> Society, Delhi");
        assertSubmitShowsError("address", MSG_ADDRESS_TAGS);
    }

    @Test(priority = 39, description = "NR-VAL-TC039: Whitespace-only address is treated as empty")
    public void VAL_TC039_addressWhitespaceOnly() {
        fillValidForm();
        form.enterAddress("                    ");
        assertSubmitShowsError("address", MSG_ADDRESS_EMPTY);
    }

    @Test(priority = 40, description = "NR-VAL-TC040: Address input stops at 250 characters (maxlength)")
    public void VAL_TC040_addressMaxLength() {
        Assert.assertEquals(form.getMaxLength("address"), "250");
        form.enterAddress(TestDataFactory.repeat('x', 260));
        Assert.assertEquals(form.getAddressValue().length(), 250);
    }

    @Test(priority = 41, description = "NR-VAL-TC041: Multi-line address with commas and slashes is accepted")
    public void VAL_TC041_multilineAddressAccepted() {
        fillValidForm();
        form.enterAddress("Flat B-204/2, Tower C\nGreen Meadows Society\nSector 12, Dwarka, New Delhi 110075");
        assertSubmitAccepted();
    }

    // ====================================================================
    // G. IDENTITY PROOF TYPE  (Priority 42 – 43)
    // ====================================================================

    @Test(priority = 42, description = "NR-VAL-TC042: Leaving ID type on 'Choose one' is rejected")
    public void VAL_TC042_idTypeNotChosen() {
        form.enterFullName(TestDataFactory.VALID_NAME);
        form.selectGender("Male");
        form.enterMobile(TestDataFactory.uniqueMobile());
        form.enterFamilyMembers("3");
        form.enterAddress(TestDataFactory.VALID_ADDRESS);
        assertSubmitShowsError("idType", MSG_ID_TYPE);
    }

    @Test(priority = 43, description = "NR-VAL-TC043: Each ID type option (Aadhaar, Voter ID, PAN, Other) is accepted")
    public void VAL_TC043_eachIdTypeAccepted() {
        for (String type : List.of("Aadhaar", "Voter ID", "PAN", "Other")) {
            form.open(baseUrl);
            fillValidForm();
            form.selectIdType(type);
            assertSubmitAccepted();
            Assert.assertEquals(review.getReviewValue("Identity proof type"), type);
        }
    }

    // ====================================================================
    // H. DOCUMENT REFERENCE  (Priority 44 – 50)
    // ====================================================================

    @Test(priority = 44, description = "NR-VAL-TC044: Document reference shorter than 4 characters is rejected")
    public void VAL_TC044_docRefTooShort() {
        fillValidForm();
        form.enterDocRef("AB1");
        assertSubmitShowsError("docRef", MSG_DOC_LENGTH);
    }

    @Test(priority = 45, description = "NR-VAL-TC045: Document reference with disallowed characters is rejected")
    public void VAL_TC045_docRefInvalidCharacters() {
        fillValidForm();
        form.enterDocRef("DEMO@1234");
        assertSubmitShowsError("docRef", MSG_DOC_CHARS);
    }

    @Test(priority = 46, description = "NR-VAL-TC046: A real-looking Aadhaar number (12 digits) is blocked")
    public void VAL_TC046_docRefAadhaarBlocked() {
        fillValidForm();
        form.enterDocRef("1234 5678 9012");
        assertSubmitShowsError("docRef", MSG_DOC_AADHAAR);
    }

    @Test(priority = 47, description = "NR-VAL-TC047: A real-looking PAN (ABCDE1234F) is blocked")
    public void VAL_TC047_docRefPanBlocked() {
        fillValidForm();
        form.enterDocRef("abcde1234f");
        assertSubmitShowsError("docRef", MSG_DOC_PAN);
    }

    @Test(priority = 48, description = "NR-VAL-TC048: A real-looking Voter ID (ABC1234567) is blocked")
    public void VAL_TC048_docRefVoterIdBlocked() {
        fillValidForm();
        form.enterDocRef("ABC1234567");
        assertSubmitShowsError("docRef", MSG_DOC_VOTER);
    }

    @Test(priority = 49, description = "NR-VAL-TC049: Demo document reference is accepted and masked on Review")
    public void VAL_TC049_docRefDemoValueMasked() {
        fillValidForm();
        form.enterDocRef("demo-ab12");
        assertSubmitAccepted();
        Assert.assertEquals(review.getReviewValue("Document reference"),
                "••••AB12 (only this masked form is saved)");
    }

    @Test(priority = 50, description = "NR-VAL-TC050: Document reference input stops at 30 characters")
    public void VAL_TC050_docRefMaxLength() {
        Assert.assertEquals(form.getMaxLength("docRef"), "30");
        form.enterDocRef(TestDataFactory.repeat('D', 35));
        Assert.assertEquals(form.getDocRefValue().length(), 30);
    }

    // ====================================================================
    // I. ADDITIONAL NOTES  (Priority 51 – 52)
    // ====================================================================

    @Test(priority = 51, description = "NR-VAL-TC051: Notes input stops at 300 characters and counter shows 300 / 300")
    public void VAL_TC051_notesMaxLength() {
        Assert.assertEquals(form.getMaxLength("notes"), "300");
        form.enterNotes(TestDataFactory.repeat('n', 305));
        Assert.assertEquals(form.getNotesValue().length(), 300);
        Assert.assertEquals(form.getNotesCounterText(), "300 / 300");
    }

    @Test(priority = 52, description = "NR-VAL-TC052: Notes counter updates live as the user types")
    public void VAL_TC052_notesCounterUpdates() {
        Assert.assertEquals(form.getNotesCounterText(), "0 / 300");
        form.enterNotes("Tenant since 2024");
        Assert.assertEquals(form.getNotesCounterText(), "17 / 300");
    }
}
