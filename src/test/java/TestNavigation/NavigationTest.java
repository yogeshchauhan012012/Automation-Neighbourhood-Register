package TestNavigation;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
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
 * ║    NAVIGATION & LAYOUT TEST SUITE                                                 ║
 * ║    Page Objects: RegisterHouseholdPage, ReviewApplicationPage  Total: 16          ║
 * ╠═══════════════════════════════════════════════════════════════════════════════════╣
 * ║  A. PAGE SHELL (title, banner, footer, skip link)   │  1 – 4   │   4  │           ║
 * ║  B. TABS & URL HASH                                 │  5 – 11  │   7  │           ║
 * ║  C. KEYBOARD TAB NAVIGATION                         │ 12 – 14  │   3  │           ║
 * ║  D. STATE & RESPONSIVE                              │ 15 – 16  │   2  │           ║
 * ╚═══════════════════════════════════════════════════════════════════════════════════╝
 */
public class NavigationTest extends BaseTest {

    private RegisterHouseholdPage page;
    private ReviewApplicationPage review;

    @BeforeClass
    public void initPages() {
        page = new RegisterHouseholdPage(driver, explicitWait);
        review = new ReviewApplicationPage(driver, explicitWait);
    }

    @BeforeMethod
    public void reloadPage() {
        driver.manage().window().maximize();
        page.open(baseUrl);
    }

    // ====================================================================
    // A. PAGE SHELL  (Priority 1 – 4)
    // ====================================================================

    @Test(priority = 1, description = "NR-NAV-TC001: Browser title is 'Neighbourhood Register – Society Household Enrollment Portal'")
    public void NAV_TC001_pageTitle() {
        Assert.assertEquals(page.getPageTitle(), "Neighbourhood Register – Society Household Enrollment Portal");
    }

    @Test(priority = 2, description = "NR-NAV-TC002: Demo banner warns not to enter real ID numbers")
    public void NAV_TC002_demoBanner() {
        String banner = page.getPrototypeBannerText();
        Assert.assertTrue(banner.startsWith("Demo environment."), banner);
        Assert.assertTrue(banner.contains("Don't enter real Aadhaar, PAN, Voter ID"), banner);
    }

    @Test(priority = 3, description = "NR-NAV-TC003: Footer explains that only a masked document reference is kept")
    public void NAV_TC003_footer() {
        Assert.assertTrue(page.getFooterText().contains("Only a masked form of any document reference"));
    }

    @Test(priority = 4, description = "NR-NAV-TC004: Skip link points to the main content")
    public void NAV_TC004_skipLink() {
        Assert.assertEquals(page.getSkipLinkHref(), "#main");
    }

    // ====================================================================
    // B. TABS & URL HASH  (Priority 5 – 11)
    // ====================================================================

    @Test(priority = 5, description = "NR-NAV-TC005: Register tab is selected by default")
    public void NAV_TC005_registerDefault() {
        Assert.assertTrue(page.isRegisterViewDisplayed());
        Assert.assertFalse(page.isStatusViewDisplayed());
        Assert.assertEquals(page.getRegisterTabAriaSelected(), "true");
        Assert.assertEquals(page.getStatusTabAriaSelected(), "false");
        Assert.assertEquals(page.getRegisterTabText(), "Register household");
        Assert.assertEquals(page.getStatusTabText(), "Check status");
    }

    @Test(priority = 6, description = "NR-NAV-TC006: Clicking 'Check status' shows the status view and sets #status")
    public void NAV_TC006_clickStatusTab() {
        page.clickStatusTab();
        Assert.assertTrue(page.isStatusViewDisplayed());
        Assert.assertFalse(page.isRegisterViewDisplayed());
        Assert.assertEquals(page.getStatusTabAriaSelected(), "true");
        Assert.assertEquals(page.getUrlHash(), "#status");
    }

    @Test(priority = 7, description = "NR-NAV-TC007: Clicking 'Register household' returns to the register view")
    public void NAV_TC007_clickRegisterTab() {
        page.clickStatusTab();
        page.clickRegisterTab();
        Assert.assertTrue(page.isRegisterViewDisplayed());
        Assert.assertEquals(page.getUrlHash(), "#register");
    }

    @Test(priority = 8, description = "NR-NAV-TC008: Clicking the brand logo returns to the register view")
    public void NAV_TC008_brandGoesToRegister() {
        page.clickStatusTab();
        page.clickBrand();
        Assert.assertTrue(page.isRegisterViewDisplayed());
        Assert.assertEquals(page.getRegisterTabAriaSelected(), "true");
    }

    @Test(priority = 9, description = "NR-NAV-TC009: Opening the URL with #status opens the status view directly")
    public void NAV_TC009_deepLinkStatus() {
        driver.get("about:blank");
        driver.get(baseUrl + "#status");
        Assert.assertTrue(page.isStatusViewDisplayed());
        Assert.assertEquals(page.getStatusTabAriaSelected(), "true");
    }

    @Test(priority = 10, description = "NR-NAV-TC010: An unknown hash falls back to the register view")
    public void NAV_TC010_unknownHashFallsBack() {
        driver.get("about:blank");
        driver.get(baseUrl + "#unknown-page");
        Assert.assertTrue(page.isRegisterViewDisplayed());
        Assert.assertFalse(page.isStatusViewDisplayed());
    }

    @Test(priority = 11, description = "NR-NAV-TC011: Changing the hash in the address bar switches the view")
    public void NAV_TC011_hashChangeSwitchesView() {
        page.js("location.hash = '#status';");
        Assert.assertTrue(page.isStatusViewDisplayed());
        page.js("location.hash = '#register';");
        Assert.assertTrue(page.isRegisterViewDisplayed());
    }

    // ====================================================================
    // C. KEYBOARD TAB NAVIGATION  (Priority 12 – 14)
    // ====================================================================

    @Test(priority = 12, description = "NR-NAV-TC012: ArrowRight on the Register tab moves to Check status")
    public void NAV_TC012_arrowRight() {
        page.focusRegisterTab();
        page.pressKeyOnActiveElement(Keys.ARROW_RIGHT);
        Assert.assertTrue(page.isStatusViewDisplayed());
        Assert.assertEquals(page.getActiveElementId(), "tab-status");
    }

    @Test(priority = 13, description = "NR-NAV-TC013: ArrowLeft wraps from the first tab to the last")
    public void NAV_TC013_arrowLeftWraps() {
        page.focusRegisterTab();
        page.pressKeyOnActiveElement(Keys.ARROW_LEFT);
        Assert.assertTrue(page.isStatusViewDisplayed());
    }

    @Test(priority = 14, description = "NR-NAV-TC014: Home / End jump to the first / last tab")
    public void NAV_TC014_homeEnd() {
        page.focusRegisterTab();
        page.pressKeyOnActiveElement(Keys.END);
        Assert.assertTrue(page.isStatusViewDisplayed());
        page.pressKeyOnActiveElement(Keys.HOME);
        Assert.assertTrue(page.isRegisterViewDisplayed());
        Assert.assertEquals(page.getActiveElementId(), "tab-register");
    }

    // ====================================================================
    // D. STATE & RESPONSIVE  (Priority 15 – 16)
    // ====================================================================

    @Test(priority = 15, description = "NR-NAV-TC015: Form data is kept when switching to Check status and back")
    public void NAV_TC015_formDataKeptAcrossTabs() {
        page.enterFullName("Tab Switch Test");
        page.enterNotes("kept");
        page.clickStatusTab();
        page.clickRegisterTab();
        Assert.assertEquals(page.getFullNameValue(), "Tab Switch Test");
        Assert.assertEquals(page.getNotesValue(), "kept");
    }

    @Test(priority = 16, description = "NR-NAV-TC016: On a mobile-size window (390x844) the form can be filled and reviewed")
    public void NAV_TC016_mobileViewport() {
        driver.manage().window().setSize(new Dimension(390, 844));
        page.open(baseUrl);
        page.fillRequiredFields(TestDataFactory.VALID_NAME, "Male", TestDataFactory.uniqueMobile(), "3",
                TestDataFactory.VALID_ADDRESS, "Other");
        page.clickReviewApplication();
        review.waitForReviewPanel();
        Assert.assertTrue(review.isReviewPanelDisplayed());
    }
}
