package TestRegister;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Base.BaseTest;
import Pages.RegisterHouseholdPage;
import Utils.TestDataFactory;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════╗
 * ║    UI & BEHAVIOUR TEST SUITE — Register household (Step 1 form)                   ║
 * ║    Page Object: RegisterHouseholdPage                    Total Test Cases: 30     ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  A. PAGE CONTENT & FIELD ATTRIBUTES                 │  1 – 8   │   8  │           ║
 * ║  B. INLINE (BLUR) & LIVE RE-VALIDATION              │  9 – 14  │   6  │           ║
 * ║  C. CLEAR FORM + CONFIRM DIALOG                     │ 15 – 22  │   8  │           ║
 * ║  D. PROFILE PHOTO UPLOAD                            │ 23 – 30  │   8  │           ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class RegisterFormBehaviourTest extends BaseTest {

    private RegisterHouseholdPage form;

    @BeforeClass
    public void initPages() {
        form = new RegisterHouseholdPage(driver, explicitWait);
    }

    @BeforeMethod
    public void reloadPage() {
        form.open(baseUrl);
    }

    private void fillValidForm() {
        form.fillRequiredFields(TestDataFactory.VALID_NAME, TestDataFactory.VALID_GENDER,
                TestDataFactory.uniqueMobile(), TestDataFactory.VALID_FAMILY,
                TestDataFactory.VALID_ADDRESS, TestDataFactory.VALID_ID_TYPE);
    }

    // ====================================================================
    // A. PAGE CONTENT & FIELD ATTRIBUTES  (Priority 1 – 8)
    // ====================================================================

    @Test(priority = 1, description = "NR-UI-TC001: Register page shows heading 'Enroll your household' and step 1 is current")
    public void UI_TC001_headingAndStepOne() {
        Assert.assertEquals(form.getPageHeading(), "Enroll your household");
        Assert.assertEquals(form.getCurrentStepNumber(), 1);
        Assert.assertTrue(form.isFormPanelDisplayed());
    }

    @Test(priority = 2, description = "NR-UI-TC002: Stepper shows 3 steps: Household details, Review, Submitted")
    public void UI_TC002_stepperLabels() {
        Assert.assertEquals(form.getStepperCount(), 3);
        Assert.assertEquals(form.getStepperLabels(), List.of("Household details", "Review", "Submitted"));
    }

    @Test(priority = 3, description = "NR-UI-TC003: Six fields are marked required with an asterisk")
    public void UI_TC003_requiredMarkers() {
        Assert.assertEquals(form.getRequiredMarkerCount(), 6);
    }

    @Test(priority = 4, description = "NR-UI-TC004: Gender offers Female, Male, Other, Prefer not to say; none pre-selected")
    public void UI_TC004_genderOptions() {
        Assert.assertEquals(form.getGenderOptions(), List.of("Female", "Male", "Other", "Prefer not to say"));
        Assert.assertEquals(form.getSelectedGender(), "", "No gender should be selected by default");
    }

    @Test(priority = 5, description = "NR-UI-TC005: ID type offers Choose one, Aadhaar, Voter ID, PAN, Other")
    public void UI_TC005_idTypeOptions() {
        Assert.assertEquals(form.getIdTypeOptions(), List.of("Choose one", "Aadhaar", "Voter ID", "PAN", "Other"));
        Assert.assertEquals(form.getSelectedIdType(), "Choose one");
    }

    @Test(priority = 6, description = "NR-UI-TC006: Mobile is a tel input; family members is a number input with min 1 / max 20")
    public void UI_TC006_inputTypes() {
        Assert.assertEquals(form.getAttribute("mobile", "type"), "tel");
        Assert.assertEquals(form.getAttribute("familyMembers", "type"), "number");
        Assert.assertEquals(form.getAttribute("familyMembers", "min"), "1");
        Assert.assertEquals(form.getAttribute("familyMembers", "max"), "20");
    }

    @Test(priority = 7, description = "NR-UI-TC007: Document reference placeholder is DEMO-1234 and address has an example placeholder")
    public void UI_TC007_placeholders() {
        Assert.assertEquals(form.getAttribute("docRef", "placeholder"), "DEMO-1234");
        Assert.assertTrue(form.getAttribute("address", "placeholder").contains("PIN code"));
    }

    @Test(priority = 8, description = "NR-UI-TC008: Buttons read 'Clear form' and 'Review application'")
    public void UI_TC008_buttonLabels() {
        Assert.assertEquals(form.getClearButtonText(), "Clear form");
        Assert.assertEquals(form.getReviewButtonText(), "Review application");
    }

    // ====================================================================
    // B. INLINE (BLUR) & LIVE RE-VALIDATION  (Priority 9 – 14)
    // ====================================================================

    @Test(priority = 9, description = "NR-UI-TC009: Invalid mobile shows an inline error when the field loses focus")
    public void UI_TC009_blurShowsMobileError() {
        form.enterMobile("12345");
        form.blurActiveElement();
        Assert.assertEquals(form.getFieldError("mobile"), "Enter a 10-digit mobile number.");
        Assert.assertEquals(form.getAriaInvalid("mobile"), "true");
    }

    @Test(priority = 10, description = "NR-UI-TC010: Leaving an empty field does not show an error (no premature errors)")
    public void UI_TC010_blurOnEmptyFieldNoError() {
        form.enterFullName("");
        form.blurActiveElement();
        Assert.assertEquals(form.getFieldError("fullName"), "");
    }

    @Test(priority = 11, description = "NR-UI-TC011: Correcting an invalid field clears its error while typing")
    public void UI_TC011_liveRevalidationClearsError() {
        form.enterMobile("12345");
        form.blurActiveElement();
        Assert.assertFalse(form.getFieldError("mobile").isEmpty());
        form.enterMobile("9876543210");
        Assert.assertEquals(form.getFieldError("mobile"), "", "Error should clear once value becomes valid");
        Assert.assertNull(form.getAriaInvalid("mobile"));
    }

    @Test(priority = 12, description = "NR-UI-TC012: Error message is re-evaluated on blur when the value changes to a different invalid state")
    public void UI_TC012_revalidationChangesMessage() {
        form.enterFamilyMembers("0");
        form.blurActiveElement();
        Assert.assertEquals(form.getFieldError("familyMembers"), "There must be at least 1 family member.");
        // Typing "2" is valid and clears the error; "25" is then checked again when the field loses focus.
        form.enterFamilyMembers("25");
        form.blurActiveElement();
        Assert.assertEquals(form.getFieldError("familyMembers"),
                "Enter 20 or fewer. Mention larger households in the notes.");
    }

    @Test(priority = 13, description = "NR-UI-TC013: Changing ID type after an error clears the error")
    public void UI_TC013_idTypeChangeClearsError() {
        form.clickReviewApplication();
        Assert.assertEquals(form.getFieldError("idType"), "Choose an identity proof type.");
        form.selectIdType("PAN");
        Assert.assertEquals(form.getFieldError("idType"), "");
    }

    @Test(priority = 14, description = "NR-UI-TC014: Blur validation on document reference flags a real-looking PAN")
    public void UI_TC014_blurValidatesDocRef() {
        form.enterDocRef("ABCDE1234F");
        form.blurActiveElement();
        Assert.assertEquals(form.getFieldError("docRef"),
                "This looks like a real PAN. Enter a made-up value such as DEMO-1234.");
    }

    // ====================================================================
    // C. CLEAR FORM + CONFIRM DIALOG  (Priority 15 – 22)
    // ====================================================================

    @Test(priority = 15, description = "NR-UI-TC015: 'Clear form' on an empty form clears without asking")
    public void UI_TC015_clearEmptyFormNoDialog() {
        form.clickClearForm();
        Assert.assertFalse(form.isConfirmDialogOpen(), "No confirmation needed for an empty form");
    }

    @Test(priority = 16, description = "NR-UI-TC016: 'Clear form' with data opens a confirmation dialog with correct text")
    public void UI_TC016_clearWithDataOpensDialog() {
        form.enterFullName("Test Person");
        form.clickClearForm();
        form.waitForConfirmDialog();
        Assert.assertEquals(form.getConfirmTitle(), "Clear the form?");
        Assert.assertEquals(form.getConfirmMessage(),
                "Everything you’ve entered, including the photo, will be removed.");
        Assert.assertEquals(form.getConfirmOkText(), "Clear form");
        Assert.assertTrue(form.getConfirmOkClass().contains("btn-danger"), "Destructive action should be styled as danger");
    }

    @Test(priority = 17, description = "NR-UI-TC017: Confirm dialog focuses Cancel by default (safe choice)")
    public void UI_TC017_dialogFocusesCancel() {
        form.enterNotes("x");
        form.clickClearForm();
        form.waitForConfirmDialog();
        Assert.assertEquals(form.getActiveElementId(), "confirm-cancel");
    }

    @Test(priority = 18, description = "NR-UI-TC018: Cancel keeps every entered value")
    public void UI_TC018_cancelKeepsData() {
        fillValidForm();
        form.enterNotes("Owner");
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.clickConfirmCancel();
        Assert.assertEquals(form.getFullNameValue(), TestDataFactory.VALID_NAME);
        Assert.assertEquals(form.getSelectedGender(), TestDataFactory.VALID_GENDER);
        Assert.assertEquals(form.getSelectedIdType(), TestDataFactory.VALID_ID_TYPE);
        Assert.assertEquals(form.getNotesValue(), "Owner");
    }

    @Test(priority = 19, description = "NR-UI-TC019: Pressing Escape closes the dialog and keeps the data")
    public void UI_TC019_escapeKeepsData() {
        form.enterFullName("Escape Test");
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.pressEscapeOnDialog();
        Assert.assertEquals(form.getFullNameValue(), "Escape Test");
    }

    @Test(priority = 20, description = "NR-UI-TC020: Confirming clears every field and resets the notes counter")
    public void UI_TC020_confirmClearsAllFields() {
        fillValidForm();
        form.enterDocRef("DEMO-9999");
        form.enterNotes("Some notes");
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.clickConfirmOk();
        Assert.assertEquals(form.getFullNameValue(), "");
        Assert.assertEquals(form.getSelectedGender(), "");
        Assert.assertEquals(form.getMobileValue(), "");
        Assert.assertEquals(form.getFamilyMembersValue(), "");
        Assert.assertEquals(form.getAddressValue(), "");
        Assert.assertEquals(form.getSelectedIdType(), "Choose one");
        Assert.assertEquals(form.getDocRefValue(), "");
        Assert.assertEquals(form.getNotesValue(), "");
        Assert.assertEquals(form.getNotesCounterText(), "0 / 300");
    }

    @Test(priority = 21, description = "NR-UI-TC021: Confirming clear shows 'Form cleared.' toast and focuses Full name")
    public void UI_TC021_confirmShowsToastAndFocus() {
        form.enterFullName("Toast Test");
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.clickConfirmOk();
        Assert.assertEquals(form.waitForToastContaining("Form cleared."), "Form cleared.");
        Assert.assertEquals(form.getActiveElementId(), "fullName");
    }

    @Test(priority = 22, description = "NR-UI-TC022: Clearing the form also removes validation errors and the summary")
    public void UI_TC022_clearRemovesErrors() {
        form.enterFullName("1");
        form.clickReviewApplication();
        Assert.assertTrue(form.isErrorSummaryDisplayed());
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.clickConfirmOk();
        Assert.assertTrue(form.hasNoErrors(), "All errors should be cleared");
    }

    // ====================================================================
    // D. PROFILE PHOTO UPLOAD  (Priority 23 – 30)
    // ====================================================================

    @Test(priority = 23, description = "NR-UI-TC023: Photo input accepts only JPG, PNG, WebP and GIF")
    public void UI_TC023_photoAcceptAttribute() {
        Assert.assertEquals(form.getPhotoInputAccept(), "image/jpeg,image/png,image/webp,image/gif");
        Assert.assertEquals(form.getPhotoButtonText(), "Choose photo");
        Assert.assertFalse(form.isRemovePhotoButtonVisible());
    }

    @Test(priority = 24, description = "NR-UI-TC024: Uploading a PNG shows a preview, 'Change photo' and 'Remove photo'")
    public void UI_TC024_uploadPngShowsPreview() throws IOException {
        form.uploadPhoto(TestDataFactory.pngImage(600, 400));
        form.waitForPhotoPreview();
        Assert.assertTrue(form.isPhotoPreviewMarkedHasPhoto());
        Assert.assertEquals(form.getPhotoButtonText(), "Change photo");
        Assert.assertTrue(form.isRemovePhotoButtonVisible());
        Assert.assertEquals(form.waitForToastContaining("Photo added."), "Photo added.");
    }

    @Test(priority = 25, description = "NR-UI-TC025: Uploading a JPG is accepted")
    public void UI_TC025_uploadJpgAccepted() throws IOException {
        form.uploadPhoto(TestDataFactory.jpgImage());
        form.waitForPhotoPreview();
        Assert.assertEquals(form.getFieldError("photo"), "");
    }

    @Test(priority = 26, description = "NR-UI-TC026: 'Remove photo' restores the placeholder")
    public void UI_TC026_removePhoto() throws IOException {
        form.uploadPhoto(TestDataFactory.pngImage(300, 300));
        form.waitForPhotoPreview();
        form.clickRemovePhoto();
        Assert.assertFalse(form.isPhotoPreviewImageShown());
        Assert.assertEquals(form.getPhotoButtonText(), "Choose photo");
        Assert.assertFalse(form.isRemovePhotoButtonVisible());
    }

    @Test(priority = 27, description = "NR-UI-TC027: A non-image file (.txt) is rejected")
    public void UI_TC027_textFileRejected() throws IOException {
        form.uploadPhoto(TestDataFactory.textFile());
        Assert.assertEquals(form.waitForFieldError("photo"), "Choose a JPG, PNG, WebP or GIF image.");
        Assert.assertFalse(form.isPhotoPreviewImageShown());
    }

    @Test(priority = 28, description = "NR-UI-TC028: An image larger than 5 MB is rejected")
    public void UI_TC028_oversizedImageRejected() throws IOException {
        form.uploadPhoto(TestDataFactory.oversizedPng());
        Assert.assertEquals(form.waitForFieldError("photo"), "This image is larger than 5 MB. Choose a smaller file.");
        Assert.assertFalse(form.isPhotoPreviewImageShown());
    }

    @Test(priority = 29, description = "NR-UI-TC029: A corrupt file with an image extension is rejected")
    public void UI_TC029_corruptImageRejected() throws IOException {
        form.uploadPhoto(TestDataFactory.corruptPng());
        Assert.assertEquals(form.waitForFieldError("photo"), "This image couldn’t be read. Try a different file.");
        Assert.assertFalse(form.isPhotoPreviewImageShown());
    }

    @Test(priority = 30, description = "NR-UI-TC030: Clearing the form also removes an uploaded photo")
    public void UI_TC030_clearRemovesPhoto() throws IOException {
        form.uploadPhoto(TestDataFactory.pngImage(300, 300));
        form.waitForPhotoPreview();
        form.clickClearForm();
        form.waitForConfirmDialog();
        form.clickConfirmOk();
        Assert.assertFalse(form.isPhotoPreviewImageShown());
        Assert.assertEquals(form.getPhotoButtonText(), "Choose photo");
    }
}
