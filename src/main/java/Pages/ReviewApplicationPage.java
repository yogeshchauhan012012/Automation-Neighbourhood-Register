package Pages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  PAGE OBJECT — Register household, Step 2 (Review)                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  Panel             │ #panel-review                                          ║
 * ║  Title             │ #review-title ("Check your details")                   ║
 * ║  Detail rows       │ #review-list > div > dt / dd                           ║
 * ║  Photo / avatar    │ #review-photo img.avatar  |  #review-photo span.avatar ║
 * ║  Declaration       │ #declaration, error #err-declaration                   ║
 * ║  Buttons           │ #btn-edit ("Edit details"), #btn-submit                ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class ReviewApplicationPage extends BasePage {

    private static final By PANEL = By.id("panel-review");
    private static final By TITLE = By.id("review-title");
    private static final By ROWS = By.cssSelector("#review-list > div");
    private static final By PHOTO_IMG = By.cssSelector("#review-photo img.avatar");
    private static final By AVATAR_INITIALS = By.cssSelector("#review-photo span.avatar");
    private static final By DECLARATION = By.id("declaration");
    private static final By DECLARATION_LABEL = By.cssSelector("label.confirm-check");
    private static final By DECLARATION_ERROR = By.id("err-declaration");
    private static final By EDIT_BUTTON = By.id("btn-edit");
    private static final By SUBMIT_BUTTON = By.id("btn-submit");
    private static final By SUCCESS_PANEL = By.id("panel-success");
    private static final By FORM_PANEL = By.id("panel-form");
    private static final By ERROR_TOAST_TEXT = By.cssSelector("#toasts .toast.error p");

    public ReviewApplicationPage(WebDriver driver, int explicitWaitSeconds) {
        super(driver, explicitWaitSeconds);
    }

    public void waitForReviewPanel() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PANEL));
    }

    public boolean isReviewPanelDisplayed() {
        return isDisplayed(PANEL);
    }

    public String getTitle() {
        return text(TITLE);
    }

    public boolean isTitleFocused() {
        return "review-title".equals(getActiveElementId());
    }

    /** All review rows as label → value, in display order. */
    public Map<String, String> getReviewDetails() {
        Map<String, String> map = new LinkedHashMap<>();
        List<WebElement> rows = driver.findElements(ROWS);
        for (WebElement row : rows) {
            map.put(row.findElement(By.tagName("dt")).getText().trim(),
                    row.findElement(By.tagName("dd")).getText().trim());
        }
        return map;
    }

    public String getReviewValue(String label) {
        return getReviewDetails().get(label);
    }

    public boolean isPhotoShown() {
        return !driver.findElements(PHOTO_IMG).isEmpty();
    }

    /** Initials avatar text shown when no photo was chosen ("" if a photo is shown). */
    public String getAvatarInitials() {
        List<WebElement> a = driver.findElements(AVATAR_INITIALS);
        return a.isEmpty() ? "" : a.get(0).getText().trim();
    }

    public boolean isDeclarationChecked() {
        return driver.findElement(DECLARATION).isSelected();
    }

    public void tickDeclaration() {
        if (!isDeclarationChecked()) {
            click(DECLARATION_LABEL);
        }
    }

    public void untickDeclaration() {
        if (isDeclarationChecked()) {
            click(DECLARATION_LABEL);
        }
    }

    public String getDeclarationText() {
        return text(DECLARATION_LABEL);
    }

    public String getDeclarationError() {
        return text(DECLARATION_ERROR);
    }

    public boolean isDeclarationFocused() {
        return "declaration".equals(getActiveElementId());
    }

    public void clickEditDetails() {
        click(EDIT_BUTTON);
    }

    public void clickSubmitApplication() {
        click(SUBMIT_BUTTON);
    }

    /** Outcome of a submission: the success step opened, or the server sent the user back to the form. */
    public enum SubmitOutcome { SUCCESS, RETURNED_TO_FORM }

    /**
     * Clicks "Submit application" and waits for the server's answer.
     *
     * @throws RateLimitedException when the server refuses because of its hourly submission limit
     * @throws IllegalStateException for any other server error toast
     */
    public SubmitOutcome submitAndWaitForOutcome() {
        clickSubmitApplication();
        Object outcome = wait.until(d -> {
            if (isDisplayed(SUCCESS_PANEL)) return SubmitOutcome.SUCCESS;
            if (isDisplayed(FORM_PANEL)) return SubmitOutcome.RETURNED_TO_FORM;
            for (WebElement t : d.findElements(ERROR_TOAST_TEXT)) {
                String text = t.getText().trim();
                if (!text.isEmpty()) return text;
            }
            return null;
        });
        if (outcome instanceof SubmitOutcome) {
            return (SubmitOutcome) outcome;
        }
        String message = outcome.toString();
        if (RateLimitedException.isRateLimitMessage(message)) {
            throw new RateLimitedException(message);
        }
        throw new IllegalStateException("Submission failed with server message: " + message);
    }

    public String getSubmitButtonText() {
        return text(SUBMIT_BUTTON);
    }

    public String getEditButtonText() {
        return text(EDIT_BUTTON);
    }
}
