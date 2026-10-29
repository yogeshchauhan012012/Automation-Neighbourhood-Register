# Neighbourhood Register: Web Automation

Selenium 4 + TestNG + ExtentReports automation for the **Neighbourhood Register** resident portal
(`http://localhost:8081/neighbourhood-register/public/#register`).
It follows the same structure as the `AlumniApplication` project: Page Object Model, one browser per test class, and one combined HTML report.

## Project layout

```
NeighbourhoodRegister/
├── pom.xml
├── testng.xml                     Full regression (154 tests)
├── testng-smoke.xml               Quick smoke (E2E + demo status lookups)
├── TestCases_NeighbourhoodRegister.csv   Test case sheet (generated, see below)
├── tools/ExportTestCases.java     Regenerates the CSV from the code + last run results
└── src/
    ├── main/java/
    │   ├── Pages/                 Page objects
    │   │   ├── BasePage.java              header tabs, stepper, toasts, confirm dialog
    │   │   ├── RegisterHouseholdPage.java step 1 form
    │   │   ├── ReviewApplicationPage.java step 2 review + submit
    │   │   ├── SubmissionSuccessPage.java step 3 reference slip
    │   │   ├── CheckStatusPage.java       status lookup
    │   │   └── RateLimitedException.java
    │   ├── Reports/ExtentManager.java
    │   └── Utils/ConfigReader.java, TestDataFactory.java
    └── test/
        ├── java/
        │   ├── Base/BaseTest.java, RateLimitListener.java
        │   ├── TestRegister/      RegisterFieldValidationTest (52), RegisterFormBehaviourTest (30), ReviewAndSubmitTest (26)
        │   ├── TestStatus/        CheckStatusTest (26)
        │   ├── TestNavigation/    NavigationTest (16)
        │   └── EndToEndFlow/      NeighbourhoodRegisterEndToEndTest (4)
        └── resources/config.properties
```

## Scenarios covered (154)

| Suite | What it checks |
|---|---|
| Field validation (52) | Empty submit and the error summary (content, order, focus, links). Name: length, characters, Unicode, maxlength. Gender. Mobile: length, first digit, +91 / 0 / 91 prefixes, letter stripping. Family members: 0, 21, decimals, negatives, 1 and 20 boundaries. Address: 15-char minimum, `< >` blocked, maxlength. ID type. Document reference: length, characters, real-looking Aadhaar / PAN / Voter ID blocked, masking. Notes: counter and maxlength. |
| UI & behaviour (30) | Labels, options, input types. Blur and live re-validation. Clear-form dialog (Cancel, Escape, Confirm, toast, focus). Photo upload: PNG/JPG preview, remove, wrong type, larger than 5 MB, corrupt file. |
| Review & submit (26) | Review rows and formatting, initials or photo avatar, declaration required, Edit details keeps values and the declaration resets. Submitting: reference format, slip, toast, unique references. Register another, Check status hand-off, Copy reference, server rejects a duplicate pending mobile. |
| Check status (26) | Validation messages, reference format, Pending / Approved / Rejected cards (badge, message, decision date, rejection reason), demo links, case and `+91` normalisation, no-match message, stale result cleared. |
| Navigation (16) | Title, demo banner, footer, skip link, tabs and `#hash` deep links, keyboard tab navigation (arrows, Home, End), form data kept across tabs, 390×844 mobile viewport. |
| End-to-end (4) | Register (fix errors, edit, upload photo), then check the new reference, a wrong mobile returns no match, and a re-apply while pending is blocked. |

## Running

Prerequisites: JDK 17+, Maven, Chrome (Edge and Firefox also supported), and the portal running on XAMPP.

```bash
mvn test                                    # full regression, visible Chrome
mvn test -Dbrowser.headless=true            # headless
mvn test -Dbrowser.name=Edge                # another browser
mvn test -Dsuite=testng-smoke.xml           # smoke suite
mvn test -Dtest=CheckStatusTest             # a single class
mvn test -Dapp.url=http://host/neighbourhood-register/public/   # another environment
```

Any key in `config.properties` can be overridden with `-Dkey=value`.

**Outputs**

- HTML report: `test-output/ExtentReports/ExtentReport_<timestamp>.html`
- Failure screenshots: `screenshot/`
- Test case sheet with Pass/Fail: run `java tools/ExportTestCases.java` after a run. It rewrites `TestCases_NeighbourhoodRegister.csv`.

## Server rate limits

The portal limits requests per IP. The limits are set in `C:\xampp\htdocs\neighbourhood-register\app\config.php`:

| Limit | Default | Used by one full run |
|---|---|---|
| `submissions_per_hour` (successful submissions only) | 20 (raised to 100 on this machine) | 13 |
| `status_checks_per_10_min` (every lookup, including "not found") | 30 | ~18 |

With the defaults, the full suite can run **once per hour**. With submissions raised to 100 it can run about 7 times per hour, but status checks still limit you to **one run every 10 minutes**. When a limit is hit, the affected tests are reported as **Skipped** with the server's message instead of failing.
To run the suite more often on a local test environment, raise those two values in `config.php`, or wait for the window to reset.

## Test data notes

- Every submission uses a random mobile number (`TestDataFactory.uniqueMobile()`), because the server blocks a second **pending** application for the same mobile.
- Each submitting test creates a real Pending application in the local `neighbourhood_register` database.
- Photo test files are generated at run time in `target/test-files/`.
- Status tests use the seeded demo records `NR-26-DEMO01` (Approved), `NR-26-DEMO02` (Pending) and `NR-26-DEMO04` (Rejected). If the database is re-seeded with different data, update `config.properties`.
