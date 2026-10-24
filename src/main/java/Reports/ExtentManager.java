package Reports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Thread-safe singleton that owns the one and only {@link ExtentReports} instance
 * for an entire test execution, so every test class writes into the same
 * consolidated HTML report.
 */
public final class ExtentManager {

    /** Folder (under the project root) where the HTML report is written. */
    private static final String REPORT_DIR =
            System.getProperty("user.dir") + File.separator + "test-output"
                    + File.separator + "ExtentReports";

    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            extent = createInstance();
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        new File(REPORT_DIR).mkdirs();

        String timestamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());
        String reportPath = REPORT_DIR + File.separator + "ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Neighbourhood Register - Automation Report");
        spark.config().setReportName("Web Automation Test Results");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(spark);
        return reports;
    }
}
