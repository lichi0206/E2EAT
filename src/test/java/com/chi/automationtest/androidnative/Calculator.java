package com.chi.automationtest.androidnative;

import com.chi.automationtest.TestCommonSuite;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @auther lichi
 * @create 2020-08-19 22:20
 */
@Test
@Slf4j
public class Calculator extends TestCommonSuite {

    public void addMethod() throws InterruptedException {
        log.info("Add Method");
        Thread.sleep(2000);
        driver.findElementById("com.google.android.calculator:id/digit_1").click();
        driver.findElementById("com.google.android.calculator:id/op_add").click();
        driver.findElementById("com.google.android.calculator:id/digit_2").click();
        driver.findElementById("com.google.android.calculator:id/eq").click();
        int result = Integer.parseInt(driver.findElementById("com.google.android.calculator:id/result_final").getText());
        Assert.assertEquals(result, 3);
        Thread.sleep(2000);
    }
}
