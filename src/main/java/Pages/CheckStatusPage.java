package Pages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 * ║  PAGE OBJECT — Check enrollment status                                           ║
 * ║  URL: /neighbourhood-register/public/#status                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════════╣
 * ║  Reference input      │ #statusRef     error #err-statusRef                     ║
 * ║  Mobile input         │ #statusMobile  error #err-statusMobile                  ║
 * ║  Check button         │ #btn-check                                              ║
 * ║  Demo record links    │ button[data-demo-ref] (pending / approved / rejected)   ║
 * ║  Result card          │ #status-result article.result-card                      ║
 * ║     name / ref        │ .result-head h2, .result-ref                            ║
 * ║     badge             │ .result-head .badge                                     ║
 * ║     details           │ .detail-list > div > dt / dd                            ║
 * ║     message           │ .result-message h3, p                                   ║
 * ║  Not-found error      │ #status-result .result-error                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 */
public class CheckStatusPage extends BasePage {

    private static final By HEADING = By.cssSelector("#view-status .page-head h1");
    private static final By REF = By.id("statusRef");
    private static final By MOBILE = By.id("statusMobile");
    private static final By REF_ERROR = By.id("err-statusRef");
    private static final By MOBILE_ERROR = By.id("err-statusMobile");
    private static final By CHECK_BUTTON = By.id("btn-check");
    private static final By RESULT = By.id("status-result");
    private static final By RESULT_CARD = By.cssSelector("#status-result article.result-card");
    private static final By RESULT_NAME = By.cssSelector("#status-result .result-head h2");
    private static final By RESULT_REF = By.cssSelector("#status-result .result-ref");
    private static final By RESULT_BADGE = By.cssSelector("#status-result .result-head .badge");
    private static final By RESULT_ROWS = By.cssSelector("#status-result .detail-list > div");
    private static final By RESULT_MESSAGE = By.cssSelector("#status-result .result-message");
    private static final By RESULT_MESSAGE_HEADING = By.cssSelector("#status-result .result-message h3");
    private static final By RESULT_ERROR = By.cssSelector("#status-result .result-error");

    public CheckStatusPage(WebDriver driver, int explicitWaitSeconds) {
        super(driver, explicitWaitSeconds);
    }

    /** Loads the status page fresh (full reload). */
    public void open(String baseUrl) {
        driver.get("about:blank");
        driver.get(baseUrl + "#status");
        wait.until(ExpectedConditions.visibilityOfElementLocated(REF));
    }

    public String getHeading() { return text(HEADING); }

    // ═══════════════════════ Input ═══════════════════════

    public void enterReference(String v) { type(REF, v); }
    public void enterMobile(String v) { type(MOBILE, v); }

    public void clickCheckStatus() { click(CHECK_BUTTON); }

    public void checkStatus(String ref, String mobile) {
        enterReference(ref);
        enterMobile(mobile);
        clickCheckStatus();
    }

    /** Clicks a demo link: "pending", "approved" or "rejected". */
    public void clickDemoLink(String linkText) {
        click(By.xpath("//p[@class='demo-hint']/button[normalize-space()='" + linkText + "']"));
    }

    public String getReferenceValue() { return value(REF); }
    public String getMobileValue() { return value(MOBILE); }
    public String getReferencePlaceholder() { return driver.findElement(REF).getDomAttribute("placeholder"); }
    public String getCheckButtonText() { return text(CHECK_BUTTON); }

    // ═══════════════════════ Validation ═══════════════════════

    public String getReferenceError() { return text(REF_ERROR); }
    public String getMobileError() { return text(MOBILE_ERROR); }

    public String getReferenceAriaInvalid() { return driver.findElement(REF).getDomAttribute("aria-invalid"); }
    public String getMobileAriaInvalid() { return driver.findElement(MOBILE).getDomAttribute("aria-invalid"); }

    // ═══════════════════════ Result ═══════════════════════

    public void waitForResultCard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_CARD));
    }

    /**
     * Waits for the card to show a specific reference (avoids reading a stale previous result).
     *
     * @throws RateLimitedException if the server answers with its status-check rate limit
     */
    public void waitForResultFor(String ref) {
        wait.until(d -> {
            if (isDisplayed(RESULT_ERROR)) {
                throwIfRateLimited();
                throw new IllegalStateException("Expected " + ref + " but got: " + getResultErrorText());
            }
            return isDisplayed(RESULT_REF) && text(RESULT_REF).contains(ref);
        });
    }

    /** @throws RateLimitedException if the error shown is the rate-limit message */
    public void waitForResultError() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_ERROR));
        throwIfRateLimited();
    }

    private void throwIfRateLimited() {
        String msg = getResultErrorText();
        if (RateLimitedException.isRateLimitMessage(msg)) {
            throw new RateLimitedException(msg);
        }
    }

    public boolean isResultCardDisplayed() { return isDisplayed(RESULT_CARD); }
    public boolean isResultErrorDisplayed() { return isDisplayed(RESULT_ERROR); }
    public String getResultAreaText() { return text(RESULT); }

    public String getResultName() { return text(RESULT_NAME); }
    public String getResultRef() { return text(RESULT_REF); }
    public String getResultBadgeText() { return text(RESULT_BADGE); }
    public String getResultBadgeClass() { return driver.findElement(RESULT_BADGE).getDomAttribute("class"); }
    public String getResultMessageHeading() { return text(RESULT_MESSAGE_HEADING); }
    public String getResultMessageText() { return text(RESULT_MESSAGE); }
    public String getResultErrorText() { return text(RESULT_ERROR); }

    public String getResultMessageClass() {
        return driver.findElement(RESULT_MESSAGE).getDomAttribute("class");
    }

    public Map<String, String> getResultDetails() {
        Map<String, String> map = new LinkedHashMap<>();
        List<WebElement> rows = driver.findElements(RESULT_ROWS);
        for (WebElement row : rows) {
            map.put(row.findElement(By.tagName("dt")).getText().trim(),
                    row.findElement(By.tagName("dd")).getText().trim());
        }
        return map;
    }
}
