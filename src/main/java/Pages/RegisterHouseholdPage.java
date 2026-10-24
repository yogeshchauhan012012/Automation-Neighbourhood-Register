package Pages;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════╗
 * ║  PAGE OBJECT — Register household, Step 1 (Household details form)               ║
 * ║  URL: /neighbourhood-register/public/#register                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════════╣
 * ║  Full name          │ #fullName       (required, 2–80, letters . ' - space)     ║
 * ║  Gender (radios)    │ #gender-female / -male / -other / -na  (required)         ║
 * ║  Mobile number      │ #mobile         (required, 10 digits, starts 6–9)         ║
 * ║  Family members     │ #familyMembers  (required, whole number 1–20)             ║
 * ║  Address            │ #address        (required, 15–250, no < >)                ║
 * ║  ID proof type      │ #idType         (required: Aadhaar|Voter ID|PAN|Other)    ║
 * ║  Document reference │ #docRef         (optional, 4–30, blocks real ID formats)  ║
 * ║  Profile photo      │ #photo          (optional, JPG/PNG/WebP/GIF ≤ 5 MB)       ║
 * ║  Notes              │ #notes          (optional, ≤ 300, live counter)           ║
 * ║  Field errors       │ #err-{field}                                              ║
 * ║  Error summary      │ #error-summary, #error-summary-list li a                  ║
 * ║  Buttons            │ #form-reset ("Clear form"), submit ("Review application") ║
 * ╚══════════════════════════════════════════════════════════════════════════════════╝
 */
public class RegisterHouseholdPage extends BasePage {

    // ─────────────── Locators ───────────────
    private static final By PANEL_FORM = By.id("panel-form");
    private static final By PAGE_HEADING = By.cssSelector("#view-register .page-head h1");
    private static final By FULL_NAME = By.id("fullName");
    private static final By MOBILE = By.id("mobile");
    private static final By FAMILY = By.id("familyMembers");
    private static final By ADDRESS = By.id("address");
    private static final By ID_TYPE = By.id("idType");
    private static final By DOC_REF = By.id("docRef");
    private static final By PHOTO_INPUT = By.id("photo");
    private static final By PHOTO_BUTTON = By.id("photo-btn");
    private static final By PHOTO_REMOVE = By.id("photo-remove");
    private static final By PHOTO_PREVIEW_IMG = By.cssSelector("#photo-preview img");
    private static final By PHOTO_PREVIEW = By.id("photo-preview");
    private static final By NOTES = By.id("notes");
    private static final By NOTES_COUNT = By.id("notes-count");
    private static final By ERROR_SUMMARY = By.id("error-summary");
    private static final By ERROR_SUMMARY_HEADING = By.cssSelector("#error-summary h2");
    private static final By ERROR_SUMMARY_ITEMS = By.cssSelector("#error-summary-list li a");
    private static final By CLEAR_BUTTON = By.id("form-reset");
    private static final By REVIEW_BUTTON = By.cssSelector("#enroll-form button[type='submit']");
    private static final By REQUIRED_MARKERS = By.cssSelector("#enroll-form label .req, #enroll-form legend .req");

    /** Gender radio ids keyed by their visible value. */
    public static String genderRadioId(String gender) {
        switch (gender) {
            case "Female": return "gender-female";
            case "Male": return "gender-male";
            case "Other": return "gender-other";
            case "Prefer not to say": return "gender-na";
            default: throw new IllegalArgumentException("Unknown gender: " + gender);
        }
    }

    public RegisterHouseholdPage(WebDriver driver, int explicitWaitSeconds) {
        super(driver, explicitWaitSeconds);
    }

    // ═══════════════════════ Navigation ═══════════════════════

    /** Loads the register page fresh (full reload so all JS state is reset). */
    public void open(String baseUrl) {
        driver.get("about:blank");
        driver.get(baseUrl + "#register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(FULL_NAME));
    }

    public boolean isFormPanelDisplayed() {
        return isDisplayed(PANEL_FORM);
    }

    public String getPageHeading() {
        return text(PAGE_HEADING);
    }

    // ═══════════════════════ Field entry ═══════════════════════

    public void enterFullName(String v) { type(FULL_NAME, v); }
    public void enterMobile(String v) { type(MOBILE, v); }
    public void enterFamilyMembers(String v) { type(FAMILY, v); }
    public void enterAddress(String v) { type(ADDRESS, v); }
    public void enterDocRef(String v) { type(DOC_REF, v); }
    public void enterNotes(String v) { type(NOTES, v); }

    public void selectGender(String gender) {
        // The radio sits inside a styled label; clicking the label is what a user does.
        WebElement radio = driver.findElement(By.id(genderRadioId(gender)));
        radio.findElement(By.xpath("./ancestor::label")).click();
    }

    public void selectIdType(String visibleText) {
        new Select(visible(ID_TYPE)).selectByVisibleText(visibleText);
    }

    public void uploadPhoto(File file) {
        driver.findElement(PHOTO_INPUT).sendKeys(file.getAbsolutePath());
    }

    public void clickRemovePhoto() {
        click(PHOTO_REMOVE);
    }

    /** Fills every required field (optional ones left blank). */
    public void fillRequiredFields(String name, String gender, String mobile, String family,
                                   String address, String idType) {
        enterFullName(name);
        selectGender(gender);
        enterMobile(mobile);
        enterFamilyMembers(family);
        enterAddress(address);
        selectIdType(idType);
    }

    public void clickReviewApplication() {
        click(REVIEW_BUTTON);
    }

    public void clickClearForm() {
        click(CLEAR_BUTTON);
    }

    public String getReviewButtonText() {
        return text(REVIEW_BUTTON);
    }

    public String getClearButtonText() {
        return text(CLEAR_BUTTON);
    }

    // ═══════════════════════ Field state ═══════════════════════

    public String getFullNameValue() { return value(FULL_NAME); }
    public String getMobileValue() { return value(MOBILE); }
    public String getFamilyMembersValue() { return value(FAMILY); }
    public String getAddressValue() { return value(ADDRESS); }
    public String getDocRefValue() { return value(DOC_REF); }
    public String getNotesValue() { return value(NOTES); }

    public String getSelectedIdType() {
        return new Select(driver.findElement(ID_TYPE)).getFirstSelectedOption().getText().trim();
    }

    public List<String> getIdTypeOptions() {
        return new Select(driver.findElement(ID_TYPE)).getOptions().stream()
                .map(o -> o.getText().trim()).collect(Collectors.toList());
    }

    /** Value of the checked gender radio, or "" when none is selected. */
    public String getSelectedGender() {
        List<WebElement> checked = driver.findElements(By.cssSelector("input[name='gender']:checked"));
        return checked.isEmpty() ? "" : checked.get(0).getDomProperty("value");
    }

    public List<String> getGenderOptions() {
        return driver.findElements(By.cssSelector("input[name='gender']")).stream()
                .map(e -> e.getDomProperty("value")).collect(Collectors.toList());
    }

    public String getMaxLength(String fieldId) {
        return driver.findElement(By.id(fieldId)).getDomAttribute("maxlength");
    }

    public String getAttribute(String fieldId, String attr) {
        return driver.findElement(By.id(fieldId)).getDomAttribute(attr);
    }

    public String getNotesCounterText() {
        return text(NOTES_COUNT);
    }

    public int getRequiredMarkerCount() {
        return driver.findElements(REQUIRED_MARKERS).size();
    }

    // ═══════════════════════ Photo state ═══════════════════════

    public boolean isPhotoPreviewImageShown() {
        return !driver.findElements(PHOTO_PREVIEW_IMG).isEmpty();
    }

    public void waitForPhotoPreview() {
        wait.until(ExpectedConditions.presenceOfElementLocated(PHOTO_PREVIEW_IMG));
    }

    public boolean isPhotoPreviewMarkedHasPhoto() {
        String cls = driver.findElement(PHOTO_PREVIEW).getDomAttribute("class");
        return cls != null && cls.contains("has-photo");
    }

    public boolean isRemovePhotoButtonVisible() {
        return isDisplayed(PHOTO_REMOVE);
    }

    public String getPhotoButtonText() {
        return text(PHOTO_BUTTON);
    }

    public String getPhotoInputAccept() {
        return driver.findElement(PHOTO_INPUT).getDomAttribute("accept");
    }

    // ═══════════════════════ Validation messages ═══════════════════════

    /** Inline error text under a field ("" when none). Field keys match the app: fullName, gender, … */
    public String getFieldError(String field) {
        return text(By.id("err-" + field));
    }

    /** Waits for an inline error to contain text and returns it. */
    public String waitForFieldError(String field) {
        By err = By.id("err-" + field);
        return wait.until(d -> {
            String t = d.findElement(err).getText().trim();
            return t.isEmpty() ? null : t;
        });
    }

    /** aria-invalid of a field (gender uses the fieldset). Null when not set. */
    public String getAriaInvalid(String field) {
        String id = "gender".equals(field) ? "gender-group" : field;
        return driver.findElement(By.id(id)).getDomAttribute("aria-invalid");
    }

    public boolean isErrorSummaryDisplayed() {
        return isDisplayed(ERROR_SUMMARY);
    }

    public String getErrorSummaryHeading() {
        return text(ERROR_SUMMARY_HEADING);
    }

    public List<String> getErrorSummaryItems() {
        return driver.findElements(ERROR_SUMMARY_ITEMS).stream()
                .map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    /** Clicks the summary link whose text starts with the given label (e.g. "Mobile number"). */
    public void clickErrorSummaryLink(String labelPrefix) {
        driver.findElements(ERROR_SUMMARY_ITEMS).stream()
                .filter(e -> e.getText().trim().startsWith(labelPrefix))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No summary link starting with " + labelPrefix))
                .click();
    }

    public boolean isErrorSummaryFocused() {
        return "error-summary".equals(getActiveElementId());
    }

    /** True when every field error and the summary are empty/hidden. */
    public boolean hasNoErrors() {
        boolean inlineEmpty = driver.findElements(By.cssSelector("#enroll-form .field-error")).stream()
                .allMatch(e -> e.getText().trim().isEmpty());
        return inlineEmpty && !isErrorSummaryDisplayed();
    }
}
