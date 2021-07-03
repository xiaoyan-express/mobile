package com.xyz.android;

import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public class ChromeTest {
    private static AndroidDriver<MobileElement> driver;
    private static WebDriverWait waiter;

    private static final String ANDROID = "ANDROID";
    private static final String ESPRESSO = "Espresso";
    private static final String UIAUTOMATOR2 = "UiAutomator2";
    private static final String CTX_NATIVE_APP = "NATIVE_APP";
    private static final String CTX_WEBVIEW_CHROME = "WEBVIEW_chrome";

    private static final String APPIUM_URL = "http://0.0.0.0:4723/wd/hub";
    private String deviceName = "emulator-5554";

    private By noThanksId = By.id("com.android.chrome:id/negative_button");
    private By acceptId = By.id("com.android.chrome:id/terms_accept");
    private By searchBoxId = By.id("com.android.chrome:id/search_box_text");

    // content-desc: Close
    private By closeBtnId = By.id("com.android.chrome:id/infobar_close_button");

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException, InterruptedException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, ANDROID);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, UIAUTOMATOR2);

        capabilities.setCapability(
                AndroidMobileCapabilityType.APP_PACKAGE,
                "com.android.chrome");

        capabilities.setCapability(
                AndroidMobileCapabilityType.APP_ACTIVITY,
                "org.chromium.chrome.browser.ChromeTabbedActivity");


        driver = new AndroidDriver<MobileElement>(new URL(APPIUM_URL), capabilities);
        driver.startRecordingScreen(AndroidStartScreenRecordingOptions.startScreenRecordingOptions());
        waiter = new WebDriverWait(driver, 10);

        clickIfFound(acceptId);
        waitForPage(1);
        clickIfFound(noThanksId);
        waitForPage(1);
    }

    @AfterMethod
    public void afterMethod() {
        String videoStr = driver.stopRecordingScreen();
        byte[] videoBytes = Base64.getDecoder().decode(videoStr.getBytes());
        try {
            Path path = Paths.get("/Users/xyz/Desktop/screen.mp4");
            Files.write(path, videoBytes);
        } catch (IOException ex) {
            System.out.println("Filed to write video to a file");
        }
        driver.closeApp();
    }

    @Test void scrollTest() {
        String searchItem = "iPhone 12";
        sendKeys(searchBoxId, searchItem);
        keyboardClick(AndroidKey.ENTER);
        waitForPage(1);

        scroll(-100);
        waitForPage(1);
        scroll(-400);
        waitForPage(1);
    }

    @Test
    public void googleSearchTest() throws InterruptedException {
        String searchItem = "iPhone 12";
        sendKeys(searchBoxId, searchItem);
        keyboardClick(AndroidKey.ENTER);
        waitForPage(1);

        clickIfFound(closeBtnId);

        verifyTextResult(searchItem);
    }

    @Test
    public void refreshChromePageTest() {
        String searchItem = "iPhone 12";
        sendKeys(searchBoxId, searchItem);
        keyboardClick(AndroidKey.ENTER);
        waitForPage(1);

        refreshChromePage();
    }

    private void clickIfFound(By by) {
        try {
            driver.findElement(by).click();
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void verifyTextResult(String text) {
        String curCtx = driver.getContext();
        try {
            driver.context(CTX_WEBVIEW_CHROME);

            String xpathTextSearch = String.format("//*[contains(text(),'%s')]", text);
            By textSearchBy = By.xpath(xpathTextSearch);

            List<MobileElement> elems = driver.findElements(textSearchBy);
            System.out.println(String.format("Found %d elements for [%s]", elems.size(), xpathTextSearch));
            Assert.assertTrue(elems.size() > 0);
        } finally {
            driver.context(curCtx);
        }
    }

    private void waitForPage(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException ex) {

        }
    }

    private void sendKeys(By by, String keys) {
        driver.findElement(by).sendKeys(keys);
    }

    private void keyboardClick(AndroidKey key) {
        driver.pressKey(new KeyEvent(key));
    }

    private void scroll(int offset) {
        int startXOffset = 20;
        int startYOffset = 200;
        PointOption startPnt = PointOption.point(startXOffset, startYOffset);
        PointOption endPnt = PointOption.point(startXOffset, startYOffset + offset);
        if (offset < 0) {
            startPnt = PointOption.point(startXOffset, startYOffset - offset);
            endPnt = PointOption.point(startXOffset, startYOffset);
        }
        new TouchAction<>(driver)
                .press(startPnt)
                .waitAction(WaitOptions.waitOptions(Duration.ofSeconds(1)))
                .moveTo(endPnt)
                .release()
                .perform();
    }

    private void screenshot(String path) {
        File file  = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File(path));
        } catch (IOException ex) {
            System.out.println("Failed to write screenshot image to file");
        }
    }

    private void refreshChromePage() {
        String curCtx = driver.getContext();
        try {
            driver.context(CTX_WEBVIEW_CHROME);
            driver.get(driver.getCurrentUrl());
        } finally {
            driver.context(curCtx);
        }
    }
}
