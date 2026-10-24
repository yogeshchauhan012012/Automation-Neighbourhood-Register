package EndToEndFlow;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
 * ║    END-TO-END FLOW — resident registers a household and tracks it                 ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  E2E-01  Fill form → hit validation → fix → Review → Edit → Review → Submit       ║
 * ║  E2E-02  Use the issued reference on a fresh page load to check status (Pending) ║
 * ║  E2E-03  The same reference with a different mobile is not found                 ║
 * ║  E2E-04  A second application with the same mobile is blocked while pending      ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  Steps depend on each other (dependsOnMethods) and share the reference/mobile.   ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class NeighbourhoodRegisterEndToEndTest extends BaseTest {

    private RegisterHouseholdPage form;
    private ReviewApplicationPage review;
    private SubmissionSuccessPage success;
    private CheckStatusPage status;

    private final String name = "Meera Kapoor";
    private final String mobile = TestDataFactory.uniqueMobile();
    private String reference;

    @BeforeClass
    public void initPages() {
        form = new RegisterHouseholdPage(driver, explicitWait);
        review = new ReviewApplicationPage(driver, explicitWait);
        success = new SubmissionSuccessPage(driver, explicitWait);
        status = new CheckStatusPage(driver, explicitWait);
    }

    @Test(priority = 1, description = "NR-E2E-01: Register a household end to end (validation, review, edit, submit)")
    public void E2E_01_registerHousehold() throws IOException {
        form.open(baseUrl);
        logReportStep("Mobile used for this run: " + mobile);

        // 1. Enter data with two mistakes
        form.enterFullName(name);
        form.selectGender("Female");
        form.enterMobile("12345");
        form.enterFamilyMembers("25");
        form.enterAddress("Flat D-1102, Palm Grove Society, Sector 50, Noida 201301");
        form.selectIdType("Aadhaar");
        form.enterDocRef("DEMO-7788");
        form.uploadPhoto(TestDataFactory.pngImage(800, 600));
        form.waitForPhotoPreview();
        form.enterNotes("Owner-occupied. Moving in next month.");
        form.clickReviewApplication();
        Assert.assertEquals(form.getErrorSummaryItems().size(), 2, "Mobile and family members should fail");
        logReportStep("Validation blocked 2 invalid fields");

        // 2. Fix the mistakes and review
        form.enterMobile(mobile);
        form.enterFamilyMembers("4");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertEquals(review.getReviewValue("Full name"), name);
        Assert.assertEquals(review.getReviewValue("Document reference"),
                "••••7788 (only this masked form is saved)");
        Assert.assertTrue(review.isPhotoShown());

        // 3. Go back, change family size, review again
        review.clickEditDetails();
        form.enterFamilyMembers("5");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertEquals(review.getReviewValue("Family members"), "5");

        // 4. Confirm and submit
        review.tickDeclaration();
        Assert.assertEquals(review.submitAndWaitForOutcome(), SubmitOutcome.SUCCESS);
        success.waitForSuccessPanel();
        reference = success.getReference();
        logReportStep("Application submitted with reference " + reference);
        Assert.assertTrue(reference.matches(SubmissionSuccessPage.REF_REGEX), reference);
        Assert.assertEquals(success.getResidentName(), name);
        Assert.assertEquals(success.getStatusBadgeText(), "Pending");
    }

    @Test(priority = 2, dependsOnMethods = "E2E_01_registerHousehold",
            description = "NR-E2E-02: The new reference shows Pending on the Check status page (fresh load)")
    public void E2E_02_checkStatusOfNewApplication() {
        status.open(baseUrl);
        status.checkStatus(reference, mobile);
        status.waitForResultFor(reference);
        Assert.assertEquals(status.getResultName(), name);
        Assert.assertEquals(status.getResultBadgeText(), "Pending");
        Assert.assertEquals(status.getResultMessageHeading(), "Waiting for review");
    }

    @Test(priority = 3, dependsOnMethods = "E2E_01_registerHousehold",
            description = "NR-E2E-03: The new reference with a different mobile is not found")
    public void E2E_03_referenceWithOtherMobileNotFound() {
        status.open(baseUrl);
        String other = mobile.equals("9000000002") ? "9000000003" : "9000000002";
        status.checkStatus(reference, other);
        status.waitForResultError();
        Assert.assertTrue(status.getResultErrorText().startsWith("No application matches"));
    }

    @Test(priority = 4, dependsOnMethods = "E2E_01_registerHousehold",
            description = "NR-E2E-04: Re-applying with the same mobile while pending is blocked")
    public void E2E_04_duplicateWhilePendingBlocked() {
        form.open(baseUrl);
        form.fillRequiredFields(name, "Female", mobile, "4",
                "Flat D-1102, Palm Grove Society, Sector 50, Noida 201301", "Other");
        form.clickReviewApplication();
        review.waitForReviewPanel();
        review.tickDeclaration();
        Assert.assertEquals(review.submitAndWaitForOutcome(), SubmitOutcome.RETURNED_TO_FORM);
        Assert.assertTrue(form.waitForFieldError("mobile").startsWith("A pending application already uses this mobile number"));
    }
}
