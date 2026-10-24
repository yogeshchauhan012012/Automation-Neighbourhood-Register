package TestStatus;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Base.BaseTest;
import Pages.CheckStatusPage;
import Utils.ConfigReader;

/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════╗
 * ║    CHECK STATUS TEST SUITE                                                        ║
 * ║    Page Object: CheckStatusPage                          Total Test Cases: 26     ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  A. PAGE CONTENT                                    │  1 – 3   │   3  │           ║
 * ║  B. INPUT VALIDATION                                │  4 – 11  │   8  │           ║
 * ║  C. PENDING / APPROVED / REJECTED RESULTS           │ 12 – 20  │   9  │           ║
 * ║  D. INPUT NORMALISATION                             │ 21 – 23  │   3  │           ║
 * ║  E. NO MATCH / RESULT REFRESH                       │ 24 – 26  │   3  │           ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  Uses the demo records seeded by the application (see config.properties).        ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class CheckStatusTest extends BaseTest {

    private CheckStatusPage status;

    private final String pendingRef = ConfigReader.get("demo.pending.ref");
    private final String pendingMobile = ConfigReader.get("demo.pending.mobile");
    private final String pendingName = ConfigReader.get("demo.pending.name");
    private final String approvedRef = ConfigReader.get("demo.approved.ref");
    private final String approvedMobile = ConfigReader.get("demo.approved.mobile");
    private final String approvedName = ConfigReader.get("demo.approved.name");
    private final String rejectedRef = ConfigReader.get("demo.rejected.ref");
    private final String rejectedName = ConfigReader.get("demo.rejected.name");
    private final String rejectedReason = ConfigReader.get("demo.rejected.reason");

    private static final String MSG_REF_EMPTY = "Enter your enrollment reference number.";
    private static final String MSG_REF_FORMAT = "Reference numbers look like NR-26-AB12CD.";
    private static final String MSG_MOBILE_EMPTY = "Enter a mobile number.";
    private static final String MSG_NO_MATCH = "No application matches that reference number and mobile number. Check both and try again.";

    @BeforeClass
    public void initPages() {
        status = new CheckStatusPage(driver, explicitWait);
    }

    @BeforeMethod
    public void reloadPage() {
        status.open(baseUrl);
    }

    // ====================================================================
    // A. PAGE CONTENT  (Priority 1 – 3)
    // ====================================================================

    @Test(priority = 1, description = "NR-STS-TC001: Status page shows heading 'Check enrollment status'")
    public void STS_TC001_heading() {
        Assert.assertTrue(status.isStatusViewDisplayed());
        Assert.assertEquals(status.getHeading(), "Check enrollment status");
    }

    @Test(priority = 2, description = "NR-STS-TC002: Reference placeholder is NR-26-XXXXXX and button reads 'Check status'")
    public void STS_TC002_placeholderAndButton() {
        Assert.assertEquals(status.getReferencePlaceholder(), "NR-26-XXXXXX");
        Assert.assertEquals(status.getCheckButtonText(), "Check status");
    }

    @Test(priority = 3, description = "NR-STS-TC003: No result is shown before a search")
    public void STS_TC003_noResultInitially() {
        Assert.assertFalse(status.isResultCardDisplayed());
        Assert.assertFalse(status.isResultErrorDisplayed());
    }

    // ====================================================================
    // B. INPUT VALIDATION  (Priority 4 – 11)
    // ====================================================================

    @Test(priority = 4, description = "NR-STS-TC004: Empty search shows errors for both fields")
    public void STS_TC004_emptySearch() {
        status.clickCheckStatus();
        Assert.assertEquals(status.getReferenceError(), MSG_REF_EMPTY);
        Assert.assertEquals(status.getMobileError(), MSG_MOBILE_EMPTY);
        Assert.assertEquals(status.getReferenceAriaInvalid(), "true");
        Assert.assertEquals(status.getMobileAriaInvalid(), "true");
    }

    @Test(priority = 5, description = "NR-STS-TC005: Empty search focuses the reference field first")
    public void STS_TC005_emptySearchFocusesReference() {
        status.clickCheckStatus();
        Assert.assertEquals(status.getActiveElementId(), "statusRef");
    }

    @Test(priority = 6, description = "NR-STS-TC006: Badly formatted reference is rejected")
    public void STS_TC006_badReferenceFormat() {
        status.checkStatus("12345", pendingMobile);
        Assert.assertEquals(status.getReferenceError(), MSG_REF_FORMAT);
        Assert.assertEquals(status.getMobileError(), "");
    }

    @Test(priority = 7, description = "NR-STS-TC007: Reference with wrong prefix (AB-26-DEMO02) is rejected")
    public void STS_TC007_wrongPrefix() {
        status.checkStatus("AB-26-DEMO02", pendingMobile);
        Assert.assertEquals(status.getReferenceError(), MSG_REF_FORMAT);
    }

    @Test(priority = 8, description = "NR-STS-TC008: Reference with too-short suffix (NR-26-AB1) is rejected")
    public void STS_TC008_shortSuffix() {
        status.checkStatus("NR-26-AB1", pendingMobile);
        Assert.assertEquals(status.getReferenceError(), MSG_REF_FORMAT);
    }

    @Test(priority = 9, description = "NR-STS-TC009: Valid reference with empty mobile focuses the mobile field")
    public void STS_TC009_emptyMobile() {
        status.checkStatus(pendingRef, "");
        Assert.assertEquals(status.getReferenceError(), "");
        Assert.assertEquals(status.getMobileError(), MSG_MOBILE_EMPTY);
        Assert.assertEquals(status.getActiveElementId(), "statusMobile");
    }

    @Test(priority = 10, description = "NR-STS-TC010: Mobile with fewer than 10 digits is rejected")
    public void STS_TC010_shortMobile() {
        status.checkStatus(pendingRef, "98765");
        Assert.assertEquals(status.getMobileError(), "Enter a 10-digit mobile number.");
    }

    @Test(priority = 11, description = "NR-STS-TC011: Mobile starting with 1-5 is rejected")
    public void STS_TC011_mobileBadStart() {
        status.checkStatus(pendingRef, "1234567890");
        Assert.assertEquals(status.getMobileError(), "Indian mobile numbers start with 6, 7, 8 or 9.");
    }

    // ====================================================================
    // C. PENDING / APPROVED / REJECTED RESULTS  (Priority 12 – 20)
    // ====================================================================

    @Test(priority = 12, description = "NR-STS-TC012: Pending record shows name, reference and Pending badge")
    public void STS_TC012_pendingCard() {
        status.checkStatus(pendingRef, pendingMobile);
        status.waitForResultFor(pendingRef);
        Assert.assertEquals(status.getResultName(), pendingName);
        Assert.assertEquals(status.getResultRef(), pendingRef);
        Assert.assertEquals(status.getResultBadgeText(), "Pending");
        Assert.assertTrue(status.getResultBadgeClass().contains("badge-pending"));
    }

    @Test(priority = 13, description = "NR-STS-TC013: Pending record shows 'Waiting for review' and no decision date")
    public void STS_TC013_pendingMessage() {
        status.checkStatus(pendingRef, pendingMobile);
        status.waitForResultFor(pendingRef);
        Assert.assertEquals(status.getResultMessageHeading(), "Waiting for review");
        Assert.assertTrue(status.getResultMessageClass().contains("msg-pending"));
        Assert.assertFalse(status.getResultDetails().containsKey("Decision date"));
    }

    @Test(priority = 14, description = "NR-STS-TC014: Result details list name, reference, submitted date and status")
    public void STS_TC014_pendingDetails() {
        status.checkStatus(pendingRef, pendingMobile);
        status.waitForResultFor(pendingRef);
        Map<String, String> d = status.getResultDetails();
        Assert.assertEquals(d.get("Resident name"), pendingName);
        Assert.assertEquals(d.get("Reference number"), pendingRef);
        Assert.assertEquals(d.get("Current status"), "Pending");
        Assert.assertNotNull(d.get("Submitted"));
        Assert.assertNotEquals(d.get("Submitted"), "—");
    }

    @Test(priority = 15, description = "NR-STS-TC015: Approved record shows Approved badge and 'Your household is enrolled'")
    public void STS_TC015_approvedCard() {
        status.checkStatus(approvedRef, approvedMobile);
        status.waitForResultFor(approvedRef);
        Assert.assertEquals(status.getResultName(), approvedName);
        Assert.assertEquals(status.getResultBadgeText(), "Approved");
        Assert.assertTrue(status.getResultBadgeClass().contains("badge-approved"));
        Assert.assertEquals(status.getResultMessageHeading(), "Your household is enrolled");
        Assert.assertTrue(status.getResultMessageText().contains("Approved on"));
    }

    @Test(priority = 16, description = "NR-STS-TC016: Approved record shows a decision date")
    public void STS_TC016_approvedDecisionDate() {
        status.checkStatus(approvedRef, approvedMobile);
        status.waitForResultFor(approvedRef);
        Assert.assertTrue(status.getResultDetails().containsKey("Decision date"));
    }

    @Test(priority = 17, description = "NR-STS-TC017: Rejected record shows Rejected badge and the rejection reason")
    public void STS_TC017_rejectedCard() {
        status.clickDemoLink("rejected");
        status.waitForResultFor(rejectedRef);
        Assert.assertEquals(status.getResultName(), rejectedName);
        Assert.assertEquals(status.getResultBadgeText(), "Rejected");
        Assert.assertEquals(status.getResultMessageHeading(), "Reason for rejection");
        Assert.assertTrue(status.getResultMessageText().contains(rejectedReason));
    }

    @Test(priority = 18, description = "NR-STS-TC018: Rejected record tells the resident they can re-apply")
    public void STS_TC018_rejectedReapplyHint() {
        status.clickDemoLink("rejected");
        status.waitForResultFor(rejectedRef);
        Assert.assertTrue(status.getResultMessageText()
                .contains("You can submit a new application with corrected details."));
        Assert.assertTrue(status.getResultDetails().containsKey("Decision date"));
    }

    @Test(priority = 19, description = "NR-STS-TC019: 'pending' demo link fills both fields and shows the result")
    public void STS_TC019_pendingDemoLink() {
        status.clickDemoLink("pending");
        status.waitForResultFor(pendingRef);
        Assert.assertEquals(status.getReferenceValue(), pendingRef);
        Assert.assertEquals(status.getMobileValue(), pendingMobile);
        Assert.assertEquals(status.getResultBadgeText(), "Pending");
    }

    @Test(priority = 20, description = "NR-STS-TC020: 'approved' demo link fills both fields and shows the result")
    public void STS_TC020_approvedDemoLink() {
        status.clickDemoLink("approved");
        status.waitForResultFor(approvedRef);
        Assert.assertEquals(status.getReferenceValue(), approvedRef);
        Assert.assertEquals(status.getMobileValue(), approvedMobile);
        Assert.assertEquals(status.getResultBadgeText(), "Approved");
    }

    // ====================================================================
    // D. INPUT NORMALISATION  (Priority 21 – 23)
    // ====================================================================

    @Test(priority = 21, description = "NR-STS-TC021: Lower-case reference with surrounding spaces is accepted")
    public void STS_TC021_lowercaseReference() {
        status.checkStatus("  " + approvedRef.toLowerCase() + "  ", approvedMobile);
        status.waitForResultFor(approvedRef);
        Assert.assertEquals(status.getResultBadgeText(), "Approved");
    }

    @Test(priority = 22, description = "NR-STS-TC022: Mobile with +91 and spaces is accepted")
    public void STS_TC022_mobileWithCountryCode() {
        status.checkStatus(pendingRef, "+91 " + pendingMobile.substring(0, 5) + " " + pendingMobile.substring(5));
        status.waitForResultFor(pendingRef);
        Assert.assertEquals(status.getResultName(), pendingName);
    }

    @Test(priority = 23, description = "NR-STS-TC023: Letters typed into the mobile field are stripped")
    public void STS_TC023_mobileLettersStripped() {
        status.enterMobile("98abc765");
        Assert.assertEquals(status.getMobileValue(), "98765");
    }

    // ====================================================================
    // E. NO MATCH / RESULT REFRESH  (Priority 24 – 26)
    // ====================================================================

    @Test(priority = 24, description = "NR-STS-TC024: Correct reference with the wrong mobile shows 'No application matches'")
    public void STS_TC024_wrongMobileNoMatch() {
        status.checkStatus(pendingRef, "9000000001");
        status.waitForResultError();
        Assert.assertEquals(status.getResultErrorText(), MSG_NO_MATCH);
        Assert.assertFalse(status.isResultCardDisplayed(), "No record details should leak");
    }

    @Test(priority = 25, description = "NR-STS-TC025: Unknown reference shows 'No application matches'")
    public void STS_TC025_unknownReference() {
        status.checkStatus("NR-26-ZZZZ99", pendingMobile);
        status.waitForResultError();
        Assert.assertEquals(status.getResultErrorText(), MSG_NO_MATCH);
    }

    @Test(priority = 26, description = "NR-STS-TC026: A validation error clears the previous result card")
    public void STS_TC026_validationClearsPreviousResult() {
        status.clickDemoLink("approved");
        status.waitForResultFor(approvedRef);
        status.enterReference("bad");
        status.clickCheckStatus();
        Assert.assertEquals(status.getReferenceError(), MSG_REF_FORMAT);
        Assert.assertFalse(status.isResultCardDisplayed(), "Old result should be cleared");
    }
}
