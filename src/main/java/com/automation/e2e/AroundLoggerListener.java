package com.automation.e2e;

import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * @auther lichi
 * @create 2020-08-18 11:20
 */
@Slf4j
public class AroundLoggerListener extends TestListenerAdapter {

    @Override
    public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        log.info("Test Success: " + tr.getName());
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        super.onTestFailure(tr);
        log.error("Test failure: " + tr.getName());
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        super.onTestSkipped(tr);
        log.warn("Test Skipped: " + tr.getName());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult tr) {
        super.onTestFailedWithTimeout(tr);
        log.error("Test failed with timeout: " + tr.getName());
    }

    @Override
    public void onStart(ITestContext testContext) {
        super.onStart(testContext);
    }

    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext);
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);
    }
}
