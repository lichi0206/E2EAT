package com.chi.automationtest.utils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @auther lichi
 * @create 2020-08-18 11:28
 */
@Slf4j
public class AppiumUtils {

    ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    private volatile static AppiumUtils appiumUtils;

    private AppiumUtils() {
    }

    /**
     * DCL (Double-checked locking):
     * Lazy initialization and Multi-thread safe
     *
     * @return AppiumUtils
     */
    public static AppiumUtils getAppiumUtils() {
        if (appiumUtils == null) {
            synchronized (AppiumUtils.class) {
                if (appiumUtils == null) {
                    appiumUtils = new AppiumUtils();
                }
            }
        }

        return appiumUtils;
    }

    /**
     * Switch context to webview
     *
     * @param driver: Appium Driver
     */
    public void switch2WebViewContext(AppiumDriver driver) {
        Set<String> contextNames = driver.getContextHandles();
        log.info("Current context: " + driver.getContext());

        String webViewContext = contextNames.stream()
                .filter((s) -> !s.equalsIgnoreCase("NATIVE_APP"))
                .findFirst()
                .get();

        driver.context(webViewContext);

        log.info("Switch context successfully: " + webViewContext);
    }

    /**
     * Switch context to native
     *
     * @param driver: Appium Driver
     */
    public void switch2NativeContext(AppiumDriver driver) {
        log.info("Current context: " + driver.getContext());
        driver.context("NATIVE_APP");
        log.info("Switch context successfully: " + driver.getContext());
    }

    /**
     * For webview element, scroll to the specify element
     *
     * @param driver  appium driver
     * @param element
     */
    public void scroll2WebElement(AppiumDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Scroll down
     *
     * @param driver appium driver
     */
    public void scrollDown(AppiumDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", "down");
        js.executeScript("mobile: scroll", scrollObject);
    }

    /**
     * Scroll up
     *
     * @param driver appium driver
     */
    public void scrollUp(AppiumDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", "up");
        js.executeScript("mobile: scroll", scrollObject);
    }

    /**
     * Scroll Down to the element.
     *
     * @param driver  Appium driver
     * @param element Scroll to element
     */
    public void scrollDown2Element(AppiumDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("direction", "down");
        scrollObject.put("element", ((RemoteWebElement) element).getId());
        js.executeScript("mobile: scroll", scrollObject);
    }

    /**
     * Because the actual resolution of the device and the resolution of the webview are different,
     * so we need to convert the point of webview to the point of device.
     * <p>
     * For the function "(native) driver.tap()" has been deprecated, there is a new way to do tap in latest Appium.
     * TouchAction: https://appium.github.io/java-client/io/appium/java_client/TouchAction.html
     *
     * @param driver  Appium driver
     * @param element Element you want to tap
     */
    public void tapElement4iOS(AppiumDriver driver, WebElement element) {

        // Fetch DOM document full size
        JavascriptExecutor js = (JavascriptExecutor) driver;
        double documentWidth = Double.parseDouble(js.executeScript("return document.width").toString());
        double documentHeight = Double.parseDouble(js.executeScript("return document.height").toString());

        // Fetch element center position from the DOM
        int elementLeftCenter = element.getLocation().getX() + (element.getSize().getWidth() / 2);
        int elementTopCenter = element.getLocation().getY() + (element.getSize().getHeight() / 2);

        // Switch to native context
        switch2NativeContext(driver);

        // Fetch the cordova WebView element
        WebElement iOSWebView = driver.findElementByXPath("//UIAApplication[1]/UIAWindow[1]/UIAScrollView[1]/UIAWebView[1]");
        double screenXMultiplier = iOSWebView.getSize().getWidth() / documentWidth;
        double screenYMultiplier = iOSWebView.getSize().getHeight() / documentHeight;

        // Get the tap position: X & Y
        int tapX = (int) Math.round(elementLeftCenter * screenXMultiplier);
        int tapY = (int) Math.round(elementTopCenter * screenYMultiplier) + iOSWebView.getLocation().getY();

        // Send a tap event on the specific button position
        // Deprecated function: driver.tap(1, tapX, tapY, 300);
        TouchAction action = new TouchAction(driver);
        action.tap(PointOption.point(tapX, tapY)).perform();

        switch2WebViewContext(driver);
    }

    /**
     * Because the actual resolution of the device and the resolution of the webview are different,
     * so we need to convert the point of webview to the point of device.
     * <p>
     * For the function "(native) driver.tap()" has been deprecated, there is a new way to do tap in latest Appium.
     * TouchAction: https://appium.github.io/java-client/io/appium/java_client/TouchAction.html
     * <p>
     * The function of get the Document width and height is different between iOS and Android.
     *
     * @param driver  Appium driver
     * @param element Element you want to tap
     */
    public void tapElement4Android(AppiumDriver driver, WebElement element) {

        // Fetch DOM document full size
        JavascriptExecutor js = (JavascriptExecutor) driver;
        double documentWidth = Double.parseDouble(js.executeScript("return window.innerWidth").toString());
        double documentHeight = Double.parseDouble(js.executeScript("return window.innerHeight").toString());

        // Fetch element center position from the DOM
        int elementLeftCenter = element.getLocation().getX() + (element.getSize().getWidth() / 2);
        int elementTopCenter = element.getLocation().getY() + (element.getSize().getHeight() / 2);

        switch2NativeContext(driver);

        // Fetch the cordova WebView element
        List<WebElement> androidWebViewList = driver.findElements(By.className("android.webkit.WebView"));
        double screenXMultiplier = androidWebViewList.get(0).getSize().getWidth() / documentWidth;
        double screenYMultiplier = androidWebViewList.get(0).getSize().getHeight() / documentHeight;

        // Get the tap position: X & Y
        int tapX = (int) Math.round(elementLeftCenter * screenXMultiplier);
        int tapY = (int) Math.round(elementTopCenter * screenYMultiplier) + androidWebViewList.get(0).getLocation().getY();

        // Send a tap event on the specific button position
        // Deprecated function: driver.tap(1, tapX, tapY, 300);
        TouchAction action = new TouchAction(driver);
        action.tap(PointOption.point(tapX, tapY)).perform();

        switch2WebViewContext(driver);
    }

    /**
     * Get screenshot
     *
     * @param driver Appium driver
     */
    public void screenshot(AppiumDriver driver) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH.mm.ss");
        String currentPath = resourceBundle.getString("common.screenshot.path")
                + dateTimeFormatter.format(LocalDateTime.now())
                + " "
                + UUID.randomUUID().toString().substring(0, 10)
                + ".jpg";
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(srcFile, new File(currentPath));
            log.info("Save screenshot success: " + currentPath);
        } catch (Exception e) {
            log.error("Save screenshot failed.");
            e.printStackTrace();
        }
    }

    /**
     * Explicit wait until element visible.
     * <p>
     * NOTE: Exist/Present/Visible/Enable
     *
     * @param driver Appium driver
     * @param by     Element by id/name...
     */
    public void wait4ElementVisible(AppiumDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(
                driver,
                Integer.parseInt(resourceBundle.getString("appium.driver.explicitWait")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * Explicit wait until element present.
     * <p>
     * NOTE: Exist/Present/Visible/Enable
     *
     * @param driver Appium driver
     * @param by     Element by id/name...
     */
    public void wait4ElementPresent(AppiumDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(
                driver,
                Integer.parseInt(resourceBundle.getString("appium.driver.explicitWait")));
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * Explicit wait until element clickable.
     * <p>
     * NOTE: Exist/Present/Visible/Enable
     *
     * @param driver Appium driver
     * @param by     Element by id/name...
     */
    public void wait4ElementClickable(AppiumDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(
                driver,
                Integer.parseInt(resourceBundle.getString("appium.driver.explicitWait")));
        wait.until(ExpectedConditions.elementToBeClickable(by));
    }
}
