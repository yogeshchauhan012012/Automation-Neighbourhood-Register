import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Builds TestCases_NeighbourhoodRegister.csv from the @Test annotations in src/test/java.
 * If target/surefire-reports/testng-results.xml exists, Actual Result and Status are filled
 * from the latest run.
 *
 * Run from the project root (JDK 11+):   java tools/ExportTestCases.java
 */
public class ExportTestCases {

    private static final Pattern TEST = Pattern.compile(
            "@Test\\(([^)]*?)description\\s*=\\s*\"([^\"]+)\"\\)\\s*public void (\\w+)", Pattern.DOTALL);
    private static final Pattern PRIORITY = Pattern.compile("priority\\s*=\\s*(\\d+)");
    private static final Pattern RESULT = Pattern.compile(
            "<test-method\\s([^>]*?)(?:/>|>(.*?)</test-method>)", Pattern.DOTALL);
    private static final Pattern MESSAGE = Pattern.compile("<message>\\s*<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);

    /** Module, precondition and testing type per test class. */
    private static final Map<String, String[]> CLASS_INFO = Map.of(
            "RegisterFieldValidationTest", new String[] {"Register Household - Field Validation",
                    "Register page (#register) is freshly loaded; step 1 form is empty", "Functional - Validation"},
            "RegisterFormBehaviourTest", new String[] {"Register Household - UI & Behaviour",
                    "Register page (#register) is freshly loaded; step 1 form is empty", "Functional - UI"},
            "ReviewAndSubmitTest", new String[] {"Register Household - Review & Submit",
                    "Register page is freshly loaded; a unique mobile number is generated per submission", "Functional - Workflow"},
            "CheckStatusTest", new String[] {"Check Status",
                    "Status page (#status) is freshly loaded; demo records NR-26-DEMO01/02/04 exist", "Functional"},
            "NavigationTest", new String[] {"Navigation & Layout",
                    "Portal is freshly loaded on the Register tab", "UI / Accessibility"},
            "NeighbourhoodRegisterEndToEndTest", new String[] {"End-to-End Flow",
                    "Portal is reachable; steps run in order and share one reference number", "End-to-End"});

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(System.getProperty("user.dir"));
        Map<String, String[]> results = readResults(root.resolve("target/surefire-reports/testng-results.xml"));

        List<String> rows = new ArrayList<>();
        rows.add("Test Case ID,Module,Test Scenario (In Details),Precondition,Automated Test,Priority Number,Testing Type,Actual Result,Status");

        List<Path> files;
        try (Stream<Path> s = Files.walk(root.resolve("src/test/java"))) {
            files = s.filter(p -> p.toString().endsWith("Test.java")).sorted().toList();
        }
        int count = 0;
        for (Path file : files) {
            String cls = file.getFileName().toString().replace(".java", "");
            String[] info = CLASS_INFO.getOrDefault(cls, new String[] {cls, "", "Functional"});
            Matcher m = TEST.matcher(Files.readString(file, StandardCharsets.UTF_8));
            while (m.find()) {
                String desc = m.group(2);
                int colon = desc.indexOf(':');
                String id = colon > 0 ? desc.substring(0, colon).trim() : m.group(3);
                String scenario = colon > 0 ? desc.substring(colon + 1).trim() : desc;
                Matcher pm = PRIORITY.matcher(m.group(1));
                String priority = pm.find() ? pm.group(1) : "";
                String[] r = results.getOrDefault(m.group(3), new String[] {"", "Not Run"});
                rows.add(String.join(",", csv(id), csv(info[0]), csv(scenario), csv(info[1]),
                        csv(cls + "#" + m.group(3)), priority, csv(info[2]), csv(r[0]), csv(r[1])));
                count++;
            }
        }
        Path out = root.resolve("TestCases_NeighbourhoodRegister.csv");
        // BOM so Excel opens the UTF-8 file (• and – characters) correctly
        Files.writeString(out, "\uFEFF" + String.join("\r\n", rows) + "\r\n", StandardCharsets.UTF_8);
        System.out.println("Wrote " + count + " test cases to " + out);
    }

    private static Map<String, String[]> readResults(Path xml) throws IOException {
        Map<String, String[]> map = new HashMap<>();
        if (!Files.exists(xml)) {
            return map;
        }
        Matcher m = RESULT.matcher(Files.readString(xml, StandardCharsets.UTF_8));
        while (m.find()) {
            String attrs = m.group(1);
            if (attrs.contains("is-config=\"true\"")) continue;
            String status = attr(attrs, "status");
            String name = attr(attrs, "name");
            String body = m.group(2) == null ? "" : m.group(2);
            String actual;
            switch (status) {
                case "PASS": actual = "As expected"; break;
                case "SKIP": {
                    Matcher msg = MESSAGE.matcher(body);
                    actual = msg.find() ? "Skipped: " + msg.group(1).trim() : "Skipped";
                    break;
                }
                default: {
                    Matcher msg = MESSAGE.matcher(body);
                    actual = msg.find() ? msg.group(1).trim().split("\\R")[0] : "Failed";
                }
            }
            String label = status.equals("PASS") ? "Pass" : status.equals("FAIL") ? "Fail" : "Skipped";
            map.put(name, new String[] {actual, label});
        }
        return map;
    }

    private static String attr(String attrs, String key) {
        Matcher a = Pattern.compile("\\b" + key + "=\"([^\"]*)\"").matcher(attrs);
        return a.find() ? a.group(1) : "";
    }

    private static String csv(String v) {
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
