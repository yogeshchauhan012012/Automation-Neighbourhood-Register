package Base;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import Reports.ExtentManager;
import Utils.ConfigReader;

/**
 * Base class for every test class.
 * <ul>
 *   <li>@BeforeClass — starts one browser per test class</li>
 *   <li>@BeforeMethod — creates the ExtentReports entry for the test</li>
 *   <li>@AfterMethod — logs the result; on failure saves a screenshot to /screenshot and embeds it in the report</li>
 *   <li>@AfterClass — quits the browser</li>
 * </ul>
 * The browser comes from config.properties (browser.name) and can be overridden with -Dbrowser.name=Edge.
 * Selenium Manager (built into Selenium 4) downloads the matching driver automatically.
 */
@Listeners(RateLimitListener.class)
public class BaseTest {

    public WebDriver driver;

    protected final String baseUrl = ConfigReader.get("app.url");
    protected final int explicitWait = ConfigReader.getInt("browser.explicitWait");

    // ═══════════════════════════ Browser lifecycle ═══════════════════════════

    @BeforeClass(alwaysRun = true)
    public void launchBrowser() {
        driver = createDriver(ConfigReader.get("browser.name"), ConfigReader.getBoolean("browser.headless"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("browser.implicitWait")));
        // In headless mode maximize() shrinks to ~800x600, so the size is set via --window-size instead.
        if (!ConfigReader.getBoolean("browser.headless")) {
            driver.manage().window().maximize();
        }
    }

    @AfterClass(alwaysRun = true)
    public void quitBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private WebDriver createDriver(String browserName, boolean headless) {
        String name = browserName.toLowerCase();
        if (name.contains("edge")) {
            EdgeOptions options = new EdgeOptions();
            if (headless) options.addArguments("--headless=new", "--window-size=1920,1080");
            return new EdgeDriver(options);
        }
        if (name.contains("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            if (headless) options.addArguments("-headless");
            return new FirefoxDriver(options);
        }
        if (!name.contains("chrome")) {
            System.out.println("Browser '" + browserName + "' is not configured. Falling back to Chrome.");
        }
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless=new", "--window-size=1920,1080");
        // Avoid the password-manager / "save address" bubbles covering the form.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "autofill.profile_enabled", false));
        return new ChromeDriver(options);
    }

    // ═══════════════════════════ Utilities ═══════════════════════════

    public String screenshot(String testcasename) throws IOException {
        String stamp = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(new Date());
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File file = new File(System.getProperty("user.dir") + File.separator + "screenshot"
                + File.separator + testcasename + "-" + stamp + ".png");
        FileUtils.copyFile(source, file);
        return file.getAbsolutePath();
    }

    // ═══════════════════════════ ExtentReports lifecycle ═══════════════════════════

    protected static ExtentReports extent;

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    @BeforeSuite(alwaysRun = true)
    public void initReport() {
        extent = ExtentManager.getInstance();
        extent.setSystemInfo("Application URL", ConfigReader.get("app.url"));
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", ConfigReader.get("browser.name"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Tester", ConfigReader.get("tester.name"));
    }

    @BeforeMethod(alwaysRun = true)
    public void startReport(ITestResult result) {
        if (extent == null) {
            initReport();
        }
        String description = result.getMethod().getDescription();
        ExtentTest test = extent.createTest(result.getMethod().getMethodName(), description == null ? "" : description);
        test.assignCategory(getClass().getSimpleName());
        CURRENT_TEST.set(test);
    }

    @AfterMethod(alwaysRun = true)
    public void logResult(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test == null) {
            return;
        }
        long durationMs = System.currentTimeMillis() - result.getStartMillis();
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                test.log(Status.PASS, "Test passed in " + durationMs + " ms");
                break;
            case ITestResult.FAILURE:
                test.log(Status.FAIL, "Test failed in " + durationMs + " ms");
                if (result.getThrowable() != null) {
                    test.fail(result.getThrowable());
                }
                attachFailureScreenshot(test, result.getName());
                break;
            case ITestResult.SKIP:
                test.log(Status.SKIP, "Test skipped");
                if (result.getThrowable() != null) {
                    test.skip(result.getThrowable());
                }
                break;
            default:
                test.log(Status.WARNING, "Test ended with an unknown status");
        }
        CURRENT_TEST.remove();
    }

    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    /** Log a custom step to the current test's report entry. */
    protected void logReportStep(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.INFO, message);
        }
    }

    private void attachFailureScreenshot(ExtentTest test, String testName) {
        if (driver == null) {
            return;
        }
        try {
            String path = screenshot(testName);
            System.out.println("Screenshot captured: " + path);
            String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            test.addScreenCaptureFromBase64String(base64, "Failure Screenshot");
        } catch (Exception e) {
            test.warning("Could not capture screenshot: " + e.getMessage());
        }
    }
}
