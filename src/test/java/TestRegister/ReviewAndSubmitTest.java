package TestRegister;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Base.BaseTest;
import Pages.CheckStatusPage;
import Pages.RegisterHouseholdPage;
import Pages.ReviewApplicationPage;
import Pages.ReviewApplicationPage.SubmitOutcome;
import Pages.SubmissionSuccessPage;
import Utils.TestDataFactory;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════╗
 * ║    REVIEW & SUBMIT TEST SUITE — Register household (Steps 2 and 3)                ║
 * ║    Page Objects: RegisterHouseholdPage, ReviewApplicationPage,                    ║
 * ║                  SubmissionSuccessPage, CheckStatusPage   Total Test Cases: 26    ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  A. REVIEW STEP CONTENT                             │  1 – 9   │   9  │           ║
 * ║  B. DECLARATION & EDIT DETAILS                      │ 10 – 15  │   6  │           ║
 * ║  C. SUCCESSFUL SUBMISSION                           │ 16 – 22  │   7  │           ║
 * ║  D. POST-SUBMISSION ACTIONS & SERVER RULES          │ 23 – 26  │   4  │           ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  NOTE: section C/D tests create real applications in the database. Each one uses ║
 * ║  a fresh random mobile number because the server blocks a second PENDING         ║
 * ║  application for the same mobile.                                                ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class ReviewAndSubmitTest extends BaseTest {

    private RegisterHouseholdPage form;
    private ReviewApplicationPage review;
    private SubmissionSuccessPage success;
    private CheckStatusPage status;

    @BeforeClass
    public void initPages() {
        form = new RegisterHouseholdPage(driver, explicitWait);
        review = new ReviewApplicationPage(driver, explicitWait);
        success = new SubmissionSuccessPage(driver, explicitWait);
        status = new CheckStatusPage(driver, explicitWait);
    }

    @BeforeMethod
    public void reloadPage() {
        form.open(baseUrl);
    }

    /** Fills a valid form with the given mobile and opens the Review step. */
    private void goToReview(String mobile) {
        form.fillRequiredFields(TestDataFactory.VALID_NAME, TestDataFactory.VALID_GENDER, mobile,
                TestDataFactory.VALID_FAMILY, TestDataFactory.VALID_ADDRESS, TestDataFactory.VALID_ID_TYPE);
        form.clickReviewApplication();
        review.waitForReviewPanel();
    }

    /** Ticks the declaration and submits; asserts the success step opened (skips if rate limited). */
    private void submitFromReview() {
        review.tickDeclaration();
        Assert.assertEquals(review.submitAndWaitForOutcome(), SubmitOutcome.SUCCESS, "Submission should succeed");
        success.waitForSuccessPanel();
    }

    // ====================================================================
    // A. REVIEW STEP CONTENT  (Priority 1 – 9)
    // ====================================================================

    @Test(priority = 1, description = "NR-SUB-TC001: Valid form opens Review step titled 'Check your details' (step 2 current, step 1 done)")
    public void SUB_TC001_reviewStepOpens() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertEquals(review.getTitle(), "Check your details");
        Assert.assertEquals(review.getCurrentStepNumber(), 2);
        Assert.assertTrue(review.isStepMarkedDone(1), "Step 1 should be marked done");
        Assert.assertFalse(form.isFormPanelDisplayed(), "Form panel should be hidden");
        Assert.assertTrue(review.isTitleFocused(), "Focus should move to the review title");
    }

    @Test(priority = 2, description = "NR-SUB-TC002: Review lists all 8 detail rows in order")
    public void SUB_TC002_reviewRowLabels() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertEquals(List.copyOf(review.getReviewDetails().keySet()), List.of(
                "Full name", "Gender", "Mobile number", "Residential address",
                "Family members", "Identity proof type", "Document reference", "Additional notes"));
    }

    @Test(priority = 3, description = "NR-SUB-TC003: Review shows exactly what was entered")
    public void SUB_TC003_reviewShowsEnteredValues() {
        form.fillRequiredFields("Kavya Iyer", "Other", "9123456789", "6",
                "Flat C-12, Lake View Society, Sector 5, Gurugram 122001", "Voter ID");
        form.enterDocRef("DEMO-5678");
        form.enterNotes("Tenant, moved in 2025");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Map<String, String> d = review.getReviewDetails();
        Assert.assertEquals(d.get("Full name"), "Kavya Iyer");
        Assert.assertEquals(d.get("Gender"), "Other");
        Assert.assertEquals(d.get("Mobile number"), "+91 91234 56789");
        Assert.assertEquals(d.get("Residential address"), "Flat C-12, Lake View Society, Sector 5, Gurugram 122001");
        Assert.assertEquals(d.get("Family members"), "6");
        Assert.assertEquals(d.get("Identity proof type"), "Voter ID");
        Assert.assertEquals(d.get("Document reference"), "••••5678 (only this masked form is saved)");
        Assert.assertEquals(d.get("Additional notes"), "Tenant, moved in 2025");
    }

    @Test(priority = 4, description = "NR-SUB-TC004: Optional fields left blank show 'Not provided' and 'None'")
    public void SUB_TC004_optionalBlankPlaceholders() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertEquals(review.getReviewValue("Document reference"), "Not provided");
        Assert.assertEquals(review.getReviewValue("Additional notes"), "None");
    }

    @Test(priority = 5, description = "NR-SUB-TC005: Extra spaces in the name are collapsed on Review")
    public void SUB_TC005_nameWhitespaceCollapsed() {
        form.enterFullName("  Priya    Sharma  ");
        form.selectGender("Female");
        form.enterMobile(TestDataFactory.uniqueMobile());
        form.enterFamilyMembers("2");
        form.enterAddress(TestDataFactory.VALID_ADDRESS);
        form.selectIdType("PAN");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertEquals(review.getReviewValue("Full name"), "Priya Sharma");
    }

    @Test(priority = 6, description = "NR-SUB-TC006: Without a photo the Review shows the resident's initials")
    public void SUB_TC006_initialsAvatarWithoutPhoto() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertFalse(review.isPhotoShown());
        Assert.assertEquals(review.getAvatarInitials(), "PS");
    }

    @Test(priority = 7, description = "NR-SUB-TC007: With a photo the Review shows the photo instead of initials")
    public void SUB_TC007_photoShownOnReview() throws IOException {
        form.uploadPhoto(TestDataFactory.pngImage(500, 500));
        form.waitForPhotoPreview();
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertTrue(review.isPhotoShown());
        Assert.assertEquals(review.getAvatarInitials(), "");
    }

    @Test(priority = 8, description = "NR-SUB-TC008: Declaration is unticked by default and its text mentions demo information")
    public void SUB_TC008_declarationDefault() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertFalse(review.isDeclarationChecked());
        Assert.assertEquals(review.getDeclarationText(),
                "I confirm these details are correct and contain only demo information.");
    }

    @Test(priority = 9, description = "NR-SUB-TC009: Review buttons read 'Edit details' and 'Submit application'")
    public void SUB_TC009_reviewButtons() {
        goToReview(TestDataFactory.uniqueMobile());
        Assert.assertEquals(review.getEditButtonText(), "Edit details");
        Assert.assertEquals(review.getSubmitButtonText(), "Submit application");
    }

    // ====================================================================
    // B. DECLARATION & EDIT DETAILS  (Priority 10 – 15)
    // ====================================================================

    @Test(priority = 10, description = "NR-SUB-TC010: Submitting without the declaration shows an error and focuses the checkbox")
    public void SUB_TC010_submitWithoutDeclaration() {
        goToReview(TestDataFactory.uniqueMobile());
        review.clickSubmitApplication();
        Assert.assertEquals(review.getDeclarationError(), "Tick the box to confirm your details before submitting.");
        Assert.assertTrue(review.isDeclarationFocused());
        Assert.assertTrue(review.isReviewPanelDisplayed(), "Must stay on Review");
        Assert.assertFalse(success.isSuccessPanelDisplayed());
    }

    @Test(priority = 11, description = "NR-SUB-TC011: Ticking the declaration clears its error")
    public void SUB_TC011_tickingClearsDeclarationError() {
        goToReview(TestDataFactory.uniqueMobile());
        review.clickSubmitApplication();
        Assert.assertFalse(review.getDeclarationError().isEmpty());
        review.tickDeclaration();
        Assert.assertEquals(review.getDeclarationError(), "");
    }

    @Test(priority = 12, description = "NR-SUB-TC012: 'Edit details' returns to step 1 with all values kept")
    public void SUB_TC012_editKeepsValues() {
        String mobile = TestDataFactory.uniqueMobile();
        form.enterNotes("Keep me");
        goToReview(mobile);
        review.clickEditDetails();
        Assert.assertTrue(form.isFormPanelDisplayed());
        Assert.assertEquals(form.getCurrentStepNumber(), 1);
        Assert.assertEquals(form.getFullNameValue(), TestDataFactory.VALID_NAME);
        Assert.assertEquals(form.getMobileValue(), mobile);
        Assert.assertEquals(form.getSelectedGender(), TestDataFactory.VALID_GENDER);
        Assert.assertEquals(form.getSelectedIdType(), TestDataFactory.VALID_ID_TYPE);
        Assert.assertEquals(form.getNotesValue(), "Keep me");
        Assert.assertEquals(form.getActiveElementId(), "fullName", "Focus should return to Full name");
    }

    @Test(priority = 13, description = "NR-SUB-TC013: Changes made after 'Edit details' appear on the next Review")
    public void SUB_TC013_editedValuesReflected() {
        goToReview(TestDataFactory.uniqueMobile());
        review.clickEditDetails();
        form.enterFullName("Arjun Nair");
        form.enterFamilyMembers("7");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertEquals(review.getReviewValue("Full name"), "Arjun Nair");
        Assert.assertEquals(review.getReviewValue("Family members"), "7");
        Assert.assertEquals(review.getAvatarInitials(), "AN");
    }

    @Test(priority = 14, description = "NR-SUB-TC014: Declaration resets to unticked when returning to Review after editing")
    public void SUB_TC014_declarationResetsAfterEdit() {
        goToReview(TestDataFactory.uniqueMobile());
        review.tickDeclaration();
        review.clickEditDetails();
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertFalse(review.isDeclarationChecked(), "Declaration must be confirmed again after edits");
    }

    @Test(priority = 15, description = "NR-SUB-TC015: Introducing an error after 'Edit details' blocks Review again")
    public void SUB_TC015_editIntroducingErrorBlocks() {
        goToReview(TestDataFactory.uniqueMobile());
        review.clickEditDetails();
        form.enterMobile("123");
        form.clickReviewApplication();
        Assert.assertEquals(form.waitForFieldError("mobile"), "Enter a 10-digit mobile number.");
        Assert.assertFalse(review.isReviewPanelDisplayed());
    }

    // ====================================================================
    // C. SUCCESSFUL SUBMISSION  (Priority 16 – 22)
    // ====================================================================

    @Test(priority = 16, description = "NR-SUB-TC016: Submitting shows 'Application submitted' (step 3 current, steps 1-2 done)")
    public void SUB_TC016_submitShowsSuccess() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        Assert.assertEquals(success.getTitle(), "Application submitted");
        Assert.assertEquals(success.getCurrentStepNumber(), 3);
        Assert.assertTrue(success.isStepMarkedDone(1));
        Assert.assertTrue(success.isStepMarkedDone(2));
        Assert.assertTrue(success.isTitleFocused());
    }

    @Test(priority = 17, description = "NR-SUB-TC017: Reference number follows the NR-XX-XXXX format")
    public void SUB_TC017_referenceFormat() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        String ref = success.getReference();
        logReportStep("Generated reference: " + ref);
        Assert.assertTrue(ref.matches(SubmissionSuccessPage.REF_REGEX), "Unexpected reference format: " + ref);
    }

    @Test(priority = 18, description = "NR-SUB-TC018: Success slip shows resident name, submitted date and Pending badge")
    public void SUB_TC018_successSlipDetails() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        Assert.assertEquals(success.getResidentName(), TestDataFactory.VALID_NAME);
        Assert.assertFalse(success.getSubmittedDate().isEmpty() || success.getSubmittedDate().equals("—"),
                "Submitted date should be filled");
        Assert.assertEquals(success.getStatusBadgeText(), "Pending");
        Assert.assertTrue(success.getStatusBadgeClass().contains("badge-pending"));
    }

    @Test(priority = 19, description = "NR-SUB-TC019: Success toast quotes the reference number")
    public void SUB_TC019_successToast() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        String ref = success.getReference();
        Assert.assertEquals(success.waitForToastContaining(ref),
                "Application submitted. Your reference number is " + ref + ".");
    }

    @Test(priority = 20, description = "NR-SUB-TC020: Each submission gets a different reference number")
    public void SUB_TC020_uniqueReferences() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        String first = success.getReference();
        success.clickRegisterAnother();
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        Assert.assertNotEquals(success.getReference(), first);
    }

    @Test(priority = 21, description = "NR-SUB-TC021: Submission with photo, document reference and notes succeeds")
    public void SUB_TC021_submitWithAllOptionalFields() throws IOException {
        form.uploadPhoto(TestDataFactory.jpgImage());
        form.waitForPhotoPreview();
        form.enterDocRef("DEMO/4321");
        form.enterNotes("Owner. Two-wheeler parking needed.");
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        Assert.assertTrue(success.getReference().matches(SubmissionSuccessPage.REF_REGEX));
    }

    @Test(priority = 22, description = "NR-SUB-TC022: Name with Unicode characters is stored and shown correctly")
    public void SUB_TC022_unicodeNameSubmitted() {
        String name = "Zoë D'Souza-राव";
        form.fillRequiredFields(name, "Female", TestDataFactory.uniqueMobile(), "2",
                TestDataFactory.VALID_ADDRESS, "Aadhaar");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        submitFromReview();
        Assert.assertEquals(success.getResidentName(), name);
    }

    // ====================================================================
    // D. POST-SUBMISSION ACTIONS & SERVER RULES  (Priority 23 – 26)
    // ====================================================================

    @Test(priority = 23, description = "NR-SUB-TC023: 'Register another household' returns to an empty step 1")
    public void SUB_TC023_registerAnother() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        success.clickRegisterAnother();
        Assert.assertTrue(form.isFormPanelDisplayed());
        Assert.assertEquals(form.getCurrentStepNumber(), 1);
        Assert.assertEquals(form.getFullNameValue(), "");
        Assert.assertEquals(form.getMobileValue(), "");
        Assert.assertEquals(form.getSelectedGender(), "");
        Assert.assertEquals(form.getSelectedIdType(), "Choose one");
    }

    @Test(priority = 24, description = "NR-SUB-TC024: 'Check status' opens the status tab pre-filled and shows Pending")
    public void SUB_TC024_checkStatusFromSuccess() {
        String mobile = TestDataFactory.uniqueMobile();
        goToReview(mobile);
        submitFromReview();
        String ref = success.getReference();
        success.clickCheckStatus();
        status.waitForResultFor(ref);
        Assert.assertTrue(status.isStatusViewDisplayed());
        Assert.assertEquals(status.getUrlHash(), "#status");
        Assert.assertEquals(status.getReferenceValue(), ref);
        Assert.assertEquals(status.getMobileValue(), mobile);
        Assert.assertEquals(status.getResultName(), TestDataFactory.VALID_NAME);
        Assert.assertEquals(status.getResultBadgeText(), "Pending");
    }

    @Test(priority = 25, description = "NR-SUB-TC025: 'Copy reference' confirms the copy with a toast")
    public void SUB_TC025_copyReference() {
        goToReview(TestDataFactory.uniqueMobile());
        submitFromReview();
        String ref = success.getReference();
        success.waitForNoToasts(); // let the submit toast go so we read the copy toast
        success.clickCopyReference();
        String toast = success.waitForAnyToast();
        Assert.assertTrue(toast.equals("Reference " + ref + " copied.") || toast.startsWith("Couldn’t copy automatically"),
                "Unexpected copy toast: " + toast);
    }

    @Test(priority = 26, description = "NR-SUB-TC026: A second pending application for the same mobile is rejected by the server")
    public void SUB_TC026_duplicatePendingMobileRejected() {
        String mobile = TestDataFactory.uniqueMobile();
        goToReview(mobile);
        submitFromReview();
        success.clickRegisterAnother();

        goToReview(mobile);
        review.tickDeclaration();
        Assert.assertEquals(review.submitAndWaitForOutcome(), SubmitOutcome.RETURNED_TO_FORM,
                "User should be sent back to step 1");
        String expected = "A pending application already uses this mobile number. Check its status before applying again.";
        Assert.assertEquals(form.waitForFieldError("mobile"), expected);
        Assert.assertTrue(form.getErrorSummaryItems().contains("Mobile number: " + expected));
    }
}
