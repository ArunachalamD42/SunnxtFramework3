package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import config.ConfigReader;

/**
 * RetryAnalyser - Automatically retries failed tests up to a configured limit.
 */
public class RetryAnalyser implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = ConfigReader.getInt("retry.count", 2);

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            System.out.println("Retrying test: " + result.getMethod().getMethodName()
                    + " | Attempt " + (retryCount + 1) + " of " + MAX_RETRY_COUNT);
            retryCount++;
            return true;
        }
        return false;
    }
}
