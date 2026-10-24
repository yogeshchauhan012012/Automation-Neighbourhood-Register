package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  PAGE OBJECT — Register household, Step 3 (Application submitted)            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  Panel               │ #panel-success                                       ║
 * ║  Title               │ #success-title ("Application submitted")             ║
 * ║  Reference number    │ #success-ref   (format NR-XX-XXXX…)                  ║
 * ║  Resident / date     │ #success-name, #success-date                         ║
 * ║  Status badge        │ #panel-success .badge-pending                        ║
 * ║  Buttons             │ #btn-copy-ref, #btn-new, #btn-goto-status            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SubmissionSuccessPage extends BasePage {

    /** Same pattern the portal uses to validate reference numbers. */
    public static final String REF_REGEX = "^NR-[A-Z0-9]{2}-[A-Z0-9]{4,12}$";

    private static final By PANEL = By.id("panel-success");
    private static final By TITLE = By.id("success-title");
    private static final By REF = By.id("success-ref");
    private static final By NAME = By.id("success-name");
    private static final By DATE = By.id("success-date");
    private static final By STATUS_BADGE = By.cssSelector("#panel-success .slip-meta .badge");
    private static final By COPY_BUTTON = By.id("btn-copy-ref");
    private static final By NEW_BUTTON = By.id("btn-new");
    private static final By GOTO_STATUS_BUTTON = By.id("btn-goto-status");

    public SubmissionSuccessPage(WebDriver driver, int explicitWaitSeconds) {
        super(driver, explicitWaitSeconds);
    }

    public void waitForSuccessPanel() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PANEL));
    }

    public boolean isSuccessPanelDisplayed() {
        return isDisplayed(PANEL);
    }

    public String getTitle() { return text(TITLE); }
    public String getReference() { return text(REF); }
    public String getResidentName() { return text(NAME); }
    public String getSubmittedDate() { return text(DATE); }
    public String getStatusBadgeText() { return text(STATUS_BADGE); }

    public String getStatusBadgeClass() {
        return driver.findElement(STATUS_BADGE).getDomAttribute("class");
    }

    public boolean isTitleFocused() {
        return "success-title".equals(getActiveElementId());
    }

    public void clickCopyReference() { click(COPY_BUTTON); }
    public void clickRegisterAnother() { click(NEW_BUTTON); }
    public void clickCheckStatus() { click(GOTO_STATUS_BUTTON); }
}
