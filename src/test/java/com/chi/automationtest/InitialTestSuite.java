package com.chi.automationtest;

import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * @auther lichi
 * @create 2020-08-18 11:26
 */
@Test
@Slf4j
public class InitialTestSuite {

    ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    @BeforeSuite
    @Parameters({"appiumRemoteURL",
            "chromedriverExecutableDir",
            "deviceName",
            "platformName",
            "platformVersion",
            "automationName",
            "appName",
            "appPackage",
            "appActivity"})
    public void initialDriver(@Optional("http://127.0.0.1:4723/wd/hub") String appiumRemoteURL,
                              @Optional("chromedrivers") String chromedriverExecutableDir,
                              String deviceName,
                              String platformName,
                              String platformVersion,
                              String automationName,
                              String appName,
                              String appPackage,
                              String appActivity) throws MalformedURLException {

        DesiredCapabilities desiredCapabilities = null;

        if (platformName.equalsIgnoreCase("Android")) {
            desiredCapabilities = initialAndroidDriver(deviceName, chromedriverExecutableDir, platformName, platformVersion, automationName, appName, appPackage, appActivity);
        } else if (platformName.equalsIgnoreCase("iOS")) {
            desiredCapabilities = initialIOSDriver(deviceName, platformName, platformVersion, automationName, appName);
        } else {
            log.error("For \"platform\" parameter, only support \"Android\" or \"iOS\"");
            throw new IllegalArgumentException("For \"platform\" parameter, only support \"Android\" or \"iOS\"");
        }

        TestCommonSuite.driver = new AndroidDriver(new URL(appiumRemoteURL), desiredCapabilities);

        TestCommonSuite.driver.manage().timeouts().implicitlyWait(
                Integer.parseInt(resourceBundle.getString("appium.driver.implicitlyWait")),
                TimeUnit.SECONDS);
    }

    private DesiredCapabilities initialAndroidDriver(String deviceName,
                                                     String chromedriverExecutableDir,
                                                     @Optional("Android") String platformName,
                                                     String platformVersion,
                                                     @Optional("UIAutomator2") String automationName,
                                                     String appName,
                                                     String appPackage,
                                                     String appActivity) throws MalformedURLException {

        String rootPath = System.getProperty("user.dir");

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("deviceName", deviceName);

        String chromedriverPath = rootPath + "\\target\\classes\\" + chromedriverExecutableDir;
        File directory = new File(rootPath);
        if (directory.isDirectory()) {
            if (directory.list().length <= 0) {
                log.error("No chromedriver executable file found in: " + chromedriverPath);
                throw new IllegalArgumentException("No chromedriver executable file found in: " + chromedriverPath);
            }
        } else {
            log.error("You need to create following folder: " + chromedriverExecutableDir);
            throw new IllegalArgumentException("You need to create following folder: " + chromedriverExecutableDir);
        }

        desiredCapabilities.setCapability("chromedriverExecutableDir", chromedriverPath);

        desiredCapabilities.setCapability("platformName", platformName);
        desiredCapabilities.setCapability("platformVersion", platformVersion);
        desiredCapabilities.setCapability("automationName", automationName);
        if (!appName.equals("")) {
            desiredCapabilities.setCapability("app",
                    System.getProperty("user.dir") + "\\target\\classes\\apks\\" + appName);
        } else {
            desiredCapabilities.setCapability("appPackage", appPackage);
            desiredCapabilities.setCapability("appActivity", appActivity);
        }

        return desiredCapabilities;
    }

    private DesiredCapabilities initialIOSDriver(String deviceName,
                                                 @Optional("iOS") String platformName,
                                                 String platformVersion,
                                                 @Optional("XCUITest") String automationName,
                                                 String appName) throws MalformedURLException {

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("deviceName", deviceName);
        desiredCapabilities.setCapability("platformName", platformName);
        desiredCapabilities.setCapability("platformVersion", platformVersion);
        desiredCapabilities.setCapability("automationName", automationName);
        desiredCapabilities.setCapability("app",
                System.getProperty("user.dir") + "\\target\\classes\\apks\\" + appName);

        return desiredCapabilities;
    }

    @AfterSuite
    public void destroyDriver() {
        if (TestCommonSuite.driver != null) {
            TestCommonSuite.driver.quit();
        }
    }
}
