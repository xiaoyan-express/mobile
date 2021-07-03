Mobile Automation Testing
===

Appium + TestNg

# Android Driver
Android Driver with UiAutomator2 (or Espresso).

## Choose driver
```java
    DesiredCapabilities capabilities = new DesiredCapabilities();
    ...
    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
```

## Choose Context
By default, ***NATIVE_APP*** is the context.

Switch the context as below:
```java
    String originalCtx = driver.getContext();
    driver.context(newCtx);
    ...
    driver.context(originalCtx);
```

To operate UI elements in the Chrome page, we need to switch to the ***WEBVIEW_chrome*** context.

## BDD
TBD
