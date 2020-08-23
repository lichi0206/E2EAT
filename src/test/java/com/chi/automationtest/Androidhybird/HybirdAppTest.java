package com.chi.automationtest.Androidhybird;

import com.chi.automationtest.TestCommonSuite;
import com.chi.automationtest.utils.AppiumUtils;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * @auther lichi
 * @create 2020-08-20 0:01
 */
@Test
public class HybirdAppTest extends TestCommonSuite {

    AppiumUtils appiumUtils = AppiumUtils.getAppiumUtils();

    @Parameters({"searchKey"})
    public void baiduSearchTest(@Optional("Appium") String searchKey) throws InterruptedException {
        Thread.sleep(1000);
        if (driver.findElementById("com.android.chrome:id/terms_accept") != null) {
            driver.findElementById("com.android.chrome:id/terms_accept").click();
            driver.findElementById("com.android.chrome:id/negative_button").click();
        }

        WebElement googleSearchBox = driver.findElementById("com.android.chrome:id/search_box_text");
        googleSearchBox.clear();
        googleSearchBox.sendKeys("https://baidu.com");
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER));
        driver.findElementById("android:id/button2").click();
        System.out.println(driver.getContextHandles());
        appiumUtils.switch2WebViewContext(driver);
        appiumUtils.wait4ElementVisible(driver, By.id("index-kw"));
        WebElement searchBox = driver.findElementById("index-kw");
        searchBox.clear();
        searchBox.sendKeys(searchKey);
        appiumUtils.wait4ElementVisible(driver, By.id("index-bn"));
        driver.findElementById("index-bn").click();
        Thread.sleep(2000);
    }
}
