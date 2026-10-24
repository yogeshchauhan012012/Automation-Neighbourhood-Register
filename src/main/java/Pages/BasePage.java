package Pages;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  BASE PAGE — elements shared by every screen of the resident portal          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  Header brand link      │  a.brand                                           ║
 * ║  Tabs                   │  #tab-register, #tab-status (role=tab)             ║
 * ║  Views                  │  #view-register, #view-status                      ║
 * ║  Stepper                │  ol.stepper li[data-step] (aria-current="step")    ║
 * ║  Toasts                 │  #toasts .toast.(success|error|warn|info) p        ║
 * ║  Confirm dialog         │  dialog#confirm-dialog, #confirm-title,            ║
 * ║                         │  #confirm-message, #confirm-ok, #confirm-cancel    ║
 * ║  Prototype banner       │  .prototype-bar                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    // ─────────────── Header / navigation ───────────────
    protected static final By BRAND = By.cssSelector("a.brand");
    protected static final By TAB_REGISTER = By.id("tab-register");
    protected static final By TAB_STATUS = By.id("tab-status");
    protected static final By VIEW_REGISTER = By.id("view-register");
    protected static final By VIEW_STATUS = By.id("view-status");
    protected static final By PROTOTYPE_BAR = By.cssSelector(".prototype-bar");
    protected static final By SKIP_LINK = By.cssSelector("a.skip-link");
    protected static final By FOOTER = By.cssSelector("footer.site-footer");

    // ─────────────── Stepper ───────────────
    protected static final By STEPPER_ITEMS = By.cssSelector("ol.stepper li");
    protected static final By CURRENT_STEP = By.cssSelector("ol.stepper li[aria-current='step']");

    // ─────────────── Toasts ───────────────
    protected static final By TOASTS = By.cssSelector("#toasts .toast");
    protected static final By TOAST_TEXT = By.cssSelector("#toasts .toast p");

    // ─────────────── Confirm dialog ───────────────
    protected static final By DIALOG = By.id("confirm-dialog");
    protected static final By DIALOG_TITLE = By.id("confirm-title");
    protected static final By DIALOG_MESSAGE = By.id("confirm-message");
    protected static final By DIALOG_OK = By.id("confirm-ok");
    protected static final By DIALOG_CANCEL = By.id("confirm-cancel");

    public BasePage(WebDriver driver, int explicitWaitSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
    }

    // ═══════════════════════ Generic helpers ═══════════════════════

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Scrolls the element to the middle of the viewport (clear of the sticky header) and clicks it.
     * If a toast is covering it, the toasts are dismissed and the click retried, as a user would.
     */
    protected void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        js("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", el);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            dismissAllToasts();
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        }
    }

    protected void type(By locator, String text) {
        WebElement el = visible(locator);
        el.clear();
        if (text != null && !text.isEmpty()) {
            el.sendKeys(text);
        }
    }

    protected String text(By locator) {
        return driver.findElement(locator).getText().trim();
    }

    protected String value(By locator) {
        return driver.findElement(locator).getDomProperty("value");
    }

    protected boolean isDisplayed(By locator) {
        List<WebElement> els = driver.findElements(locator);
        return !els.isEmpty() && els.get(0).isDisplayed();
    }

    /** True when the element carries the HTML {@code hidden} attribute. */
    protected boolean isHidden(By locator) {
        return Boolean.parseBoolean(driver.findElement(locator).getDomProperty("hidden"));
    }

    public Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /** Moves focus away from the current element so blur validation fires. */
    public void blurActiveElement() {
        js("if (document.activeElement) document.activeElement.blur();");
    }

    public String getActiveElementId() {
        Object id = js("return document.activeElement ? document.activeElement.id : '';");
        return id == null ? "" : id.toString();
    }

    // ═══════════════════════ Header / tabs ═══════════════════════

    public void clickRegisterTab() {
        click(TAB_REGISTER);
    }

    public void clickStatusTab() {
        click(TAB_STATUS);
    }

    public void clickBrand() {
        click(BRAND);
    }

    public boolean isRegisterViewDisplayed() {
        return isDisplayed(VIEW_REGISTER);
    }

    public boolean isStatusViewDisplayed() {
        return isDisplayed(VIEW_STATUS);
    }

    public String getRegisterTabAriaSelected() {
        return driver.findElement(TAB_REGISTER).getDomAttribute("aria-selected");
    }

    public String getStatusTabAriaSelected() {
        return driver.findElement(TAB_STATUS).getDomAttribute("aria-selected");
    }

    public String getRegisterTabText() {
        return text(TAB_REGISTER);
    }

    public String getStatusTabText() {
        return text(TAB_STATUS);
    }

    public void focusRegisterTab() {
        js("arguments[0].focus();", driver.findElement(TAB_REGISTER));
    }

    public void focusStatusTab() {
        js("arguments[0].focus();", driver.findElement(TAB_STATUS));
    }

    /** Sends a key to the currently focused element (e.g. arrow keys on tabs). */
    public void pressKeyOnActiveElement(CharSequence key) {
        driver.switchTo().activeElement().sendKeys(key);
    }

    public String getUrlHash() {
        Object h = js("return location.hash;");
        return h == null ? "" : h.toString();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getPrototypeBannerText() {
        return text(PROTOTYPE_BAR);
    }

    public String getFooterText() {
        return text(FOOTER);
    }

    public String getSkipLinkHref() {
        return driver.findElement(SKIP_LINK).getDomAttribute("href");
    }

    // ═══════════════════════ Stepper ═══════════════════════

    public int getStepperCount() {
        return driver.findElements(STEPPER_ITEMS).size();
    }

    public List<String> getStepperLabels() {
        return driver.findElements(By.cssSelector("ol.stepper li .step-label")).stream()
                .map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    /** The data-step number of the step marked aria-current="step". */
    public int getCurrentStepNumber() {
        return Integer.parseInt(driver.findElement(CURRENT_STEP).getDomAttribute("data-step"));
    }

    public boolean isStepMarkedDone(int step) {
        WebElement li = driver.findElement(By.cssSelector("ol.stepper li[data-step='" + step + "']"));
        String cls = li.getDomAttribute("class");
        return cls != null && cls.contains("done");
    }

    // ═══════════════════════ Toasts ═══════════════════════

    /** Waits for a toast whose text contains the given fragment and returns its full text. */
    public String waitForToastContaining(String fragment) {
        return wait.until(d -> d.findElements(TOAST_TEXT).stream()
                .map(e -> e.getText().trim())
                .filter(t -> t.contains(fragment))
                .findFirst().orElse(null));
    }

    /** Waits for any toast (visible, with text) and returns the text of the newest one. */
    public String waitForAnyToast() {
        return wait.until(d -> {
            List<WebElement> toasts = d.findElements(TOAST_TEXT);
            if (toasts.isEmpty()) return null;
            String t = toasts.get(toasts.size() - 1).getText().trim();
            return t.isEmpty() ? null : t;
        });
    }

    /** Clicks the close button on every toast and waits for them to leave. */
    public void dismissAllToasts() {
        for (WebElement close : driver.findElements(By.cssSelector("#toasts .toast .icon-btn"))) {
            try {
                close.click();
            } catch (RuntimeException ignored) {
                // toast already leaving
            }
        }
        waitForNoToasts();
    }

    /** CSS classes of the newest toast, e.g. "toast success". */
    public String getLatestToastClass() {
        List<WebElement> toasts = driver.findElements(TOASTS);
        return toasts.isEmpty() ? "" : toasts.get(toasts.size() - 1).getDomAttribute("class");
    }

    public void dismissLatestToast() {
        List<WebElement> toasts = driver.findElements(By.cssSelector("#toasts .toast .icon-btn"));
        if (!toasts.isEmpty()) {
            toasts.get(toasts.size() - 1).click();
        }
    }

    public int getToastCount() {
        return driver.findElements(TOASTS).size();
    }

    public void waitForNoToasts() {
        wait.until(d -> d.findElements(TOASTS).isEmpty());
    }

    // ═══════════════════════ Confirm dialog ═══════════════════════

    public boolean isConfirmDialogOpen() {
        Object open = js("return document.getElementById('confirm-dialog').open;");
        return Boolean.TRUE.equals(open);
    }

    public void waitForConfirmDialog() {
        wait.until(d -> isConfirmDialogOpen());
        // The dialog fades in; wait until its text is actually visible before reading it.
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG_TITLE));
    }

    public void waitForConfirmDialogClosed() {
        wait.until(d -> !isConfirmDialogOpen());
    }

    public String getConfirmTitle() {
        return text(DIALOG_TITLE);
    }

    public String getConfirmMessage() {
        return text(DIALOG_MESSAGE);
    }

    public String getConfirmOkText() {
        return text(DIALOG_OK);
    }

    public String getConfirmOkClass() {
        return driver.findElement(DIALOG_OK).getDomAttribute("class");
    }

    public void clickConfirmOk() {
        click(DIALOG_OK);
        waitForConfirmDialogClosed();
    }

    public void clickConfirmCancel() {
        click(DIALOG_CANCEL);
        waitForConfirmDialogClosed();
    }

    public void pressEscapeOnDialog() {
        driver.findElement(DIALOG).sendKeys(org.openqa.selenium.Keys.ESCAPE);
        waitForConfirmDialogClosed();
    }
}
