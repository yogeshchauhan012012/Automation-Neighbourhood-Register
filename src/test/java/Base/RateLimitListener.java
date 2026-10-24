package Base;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import Pages.RateLimitedException;

/**
 * Turns failures caused by the portal's rate limit into SKIPs, so a run that
 * exceeds the server's hourly quota is not reported as an application defect.
 */
public class RateLimitListener implements IInvokedMethodListener {

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod() && result.getStatus() == ITestResult.FAILURE
                && result.getThrowable() instanceof RateLimitedException) {
            result.setStatus(ITestResult.SKIP);
        }
    }
}
