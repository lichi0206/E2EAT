## 移动应用自动化测试框架

### 简介

日常工作中，经常需要给不同的移动应用开发自动化代码。为了方便自己和其他有自动化需求的小伙伴，这里将平常我经常使用的一套自动化框架整合并开源，希望可以帮助大家。

框架中集成了以下工具：

1. Appium

   使用Appium作为自动化引擎，支持Android，iOS和PC Bowser，代码中也写了两个例子供大家参考。

2. TestNG

   集成TestNG来管理测试用例。

3. ReportNG 和 ExtentReports

   这里集成了两个Report工具，相比TestNG自带的测试报告，这两个工具均可以提供非常简单美观的测试报告。

   为什么要集成两个，也是为了不同的需求，ReportNG已经停止维护了，但是我们之前的很多项目中还是使用ReportNG，而且已经和Jenkins进行了深度整合，所以为了保证兼容性，还是集成了ReportNG。

   ExtentReports是一个比较好的测试报告的代替品，但是需要自己写监听器来初始化和自定义报表，框架中已经定义好了，可以直接使用。

   两个报表工具均已经配置好，开箱可用，可以自由选择哪个报表工具作为最终展现。

4. Log4j2 + SLF4j

   集成Log4j2和SLF4j作为日志处理工具。

5. Lombok

   集成Lombok简化代码。

6. Maven

   集成Maven和Maven Wrapper。已经在POM文件中配置好了，可以在命令行直接运行`mvn test`来跑TestNG定义的测试用例。当然了，你依旧可以通过直接运行TestNG的XML文件来跑。集成Maven的优点是方便大家做持续集成。

### 功能

#### Appium 初始化

项目中已经集成了Appium最新的客户端，初始化Appium客户端的代码也已经完成，只需要提供必要的Capabilities即可。

##### 初始化Appium client driver

初始化代码请参考：InitialTestSuite.java

```java
public void initialDriver(@Optional("http://127.0.0.1:4723/wd/hub") String appiumRemoteURL,
                              String deviceName,
                              @Optional("Android") String platformName,
                              String platformVersion,
                              @Optional("UIAutomator2") String automationName,
                              String appName,
                              String appPackage,
                              String appActivity) throws MalformedURLException {

        DesiredCapabilities desiredCapabilities = null;

        if (platformName.equalsIgnoreCase("Android")) {
            desiredCapabilities = initialAndroidDriver(deviceName, platformName, platformVersion, automationName, appName, appPackage, appActivity);
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
```

##### 如何使用

Appium client driver定义在TestCommonSuite.java的抽象类中，在自动化代码运行之前，会用上述代码进行初始化，如果想要写自己的测试用例，就需要集成该抽象类，这样就可以你自己的类中直接使用`driver`了。

抽象类代码如下：

```java
public abstract class TestCommonSuite {
    public static AppiumDriver driver = null;
}
```

继承该抽象类即可使用driver：

```java
public class TestClass extends TestCommonSuite {
    public void testMethod() throws InterruptedException {
        driver.findElementById("testID").click();
    }
}
```

更多Appium知识请参考[官网](http://appium.io/)。

#### TestNG 测试用例管理

集成TestNG来管理测试用例，xml文件放置在resources目录下。

##### 创建新的TestNG文件方法

1. 创建空XML，加入以下代码：

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
   <suite name="All Test Suite">
       <test verbose="2" preserve-order="true" name="E:/6CodeRepository/AutomationTools/E2EAT/src/main/resources">
       </test>
   </suite>
   ```

   注意：`<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">`这一行代码一定要加上，声明该文件为TestNG文件，并提供了代码提示和自动完成功能。

2. 使用`Create TestNG XML`插件

   Ctrl+ALT+S打开设置，选择Plugins，搜索`Create TestNG XML`插件并安装，之后就可以在右键菜单上找到`Create TestNG XML`选项，使用该选项可以直接在选定目录下生成`testng.xml`

**注意：所有的自动化代码请放置在Maven工程的Test目录下**。

##### 初始化Appium Driver

初始化Appium Driver所需要的参数都放在testng.xml文件里，示例：

```xml
<parameter name="appiumRemoteURL" value="http://127.0.0.1:4723/wd/hub"/>

<!-- Modify parameters according to your needs -->
<parameter name="deviceName" value="Android"/>
<parameter name="platformName" value="Android"/>
<parameter name="platformVersion" value="10.0"/>
<parameter name="automationName" value="UIAutomator2"/>

<!-- App name, not path, put your apk file under “resources/apks” folder. -->
<parameter name="appName" value=""/>
<!--
        Android Only
        If there is "appName", will ignore the "appPackage" and "appActivity",
          otherwise, will use "appPackage" and "appActivity"
    -->
<parameter name="appPackage" value="com.google.android.calculator"/>
<parameter name="appActivity" value="com.android.calculator2.Calculator"/>
```

需要注意，如果要在Android设备上运行自动化代码，那么`app`和`appPackage`，`appActivity`二者取其一即可，摘抄Appium官网说明：

>The absolute local path *or* remote http URL to a `.ipa` file (IOS), `.app` folder (IOS Simulator), `.apk` file (Android) or `.apks` file (Android App Bundle), or a `.zip` file containing one of these. Appium will attempt to install this app binary on the appropriate device first. **Note that this capability is not required for Android if you specify `appPackage` and `appActivity` capabilities (see below).** `UiAutomator2` and `XCUITest` allow to start the session without `app` or `appPackage`. Incompatible with `browserName`. See [here](http://appium.io/docs/en/writing-running-appium/android/android-appbundle/index.html) about `.apks` file.

更多TestNG知识请参考[官网](https://testng.org/doc/)。

#### 日志功能

使用SLF4j + Log4j2作为日志管理工具，Log4j2的配置文件放在resources文件下，摘抄如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- Status:  TRACE < DEBUG < INFO < WARN < ERROR < FATAL
    Log level defined in <logger> will override this configuration.
    monitorInterval: Read the configuration file every 300 s.
-->
<Configuration status="WARN" monitorInterval="300">
    <properties>
        <property name="LOG_HOME">./logs</property>
        <property name="FILE_NAME">E2EATLog</property>
    </properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <RollingFile name="RollingFileCommonMSG"
                     fileName="${LOG_HOME}/${FILE_NAME}.log"
                     filePattern="${LOG_HOME}/$${date:yyyy-MM}/${FILE_NAME}-%d{yyyy-MM-dd HH-mm}-%i.log">
            <PatternLayout
                    pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="6"/>
                <SizeBasedTriggeringPolicy size="10 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="20"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Logger name="com.automation" level="DEBUG" additivity="false" includeLocation="true">
            <AppenderRef ref="RollingFileCommonMSG"/>
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="org.testng" level="DEBUG" additivity="false" includeLocation="true">
            <AppenderRef ref="RollingFileCommonMSG"/>
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="com.github.appium" level="DEBUG" additivity="false" includeLocation="true">
            <AppenderRef ref="RollingFileCommonMSG"/>
            <AppenderRef ref="Console"/>
        </Logger>
        <Root level="DEBUG">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

在`properties`中可以定义log文件存放目录和文件名称，这里我使用的是项目根目录下的logs目录，可以根据时间和大小进行分卷并归类，如下图：

![](.\document\logFolder.png)

你可以自定义该文件来满足你的需求。

#### 测试报告

如上文所说，集成了ReportNG和Extent Reports两个报表工具。

##### ReportNG

ReportNG依赖导入后不需要自定义，在testng.xml文件中加入一下代码后即可直接使用：

```xml
<listeners>
    <listener class-name="org.uncommons.reportng.HTMLReporter"/>
    <listener class-name="org.uncommons.reportng.JUnitXMLReporter"/>
</listeners>
```

生成的报表下图：

![](.\document\ReportNG.png)

##### Extent Reports

Extent Reports 依赖导入以后，可以使用监听器来自定义报表属性。项目中自定义代码参考：ExtentReportListener.java，可以参考该代码来自定义你的Extent Reports。

同样的，我们也需要在testng.xml文件中加入以下代码才可使用：

```xml
<listeners>
    <listener class-name="com.automation.e2e.ExtentReportListener"/>
</listeners>
```

生成的报表如下图：

![](.\document\ExtentReport.png)

#### Maven

我们有两种方式来运行testng.xml，一种是直接右键该文件，选择`run...`，另一种是使用Maven来运行测试。

##### 运行testng.xml

右键testng.xml文件，选择`Run ...`来运行

##### Maven

使用`surefire`来将TestNG测试文件绑定到Maven Test。

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>2.20.1</version>
    <configuration>
        <testFailureIgnore>true</testFailureIgnore>
        <suiteXmlFiles>
            <!-- Location of TestNG xml -->
            <suiteXmlFile>${testngFileName}</suiteXmlFile>
        </suiteXmlFiles>
        <properties>
            <property>
                <name>suitethreadpoolsize</name>
                <value>2</value>
            </property>
            <!-- Use ReportNG to instead of the default report of TestNG -->
            <property>
                <name>usedefaultlisteners</name>
                <value>false</value>
            </property>
            <!-- listener -->
            <property>
                <name>listener</name>
                <value>org.uncommons.reportng.HTMLReporter, org.uncommons.reportng.JUnitXMLReporter,
                    com.automation.e2e.AroundLoggerListener,
                    com.automation.e2e.ExtentReportListener
                </value>
            </property>
        </properties>
        <argLine>-Dfile.encoding=UTF-8</argLine>
        <argLine>-Dtestng.dtd.http=true</argLine>
    </configuration>
</plugin>
```

这样就可以直接在命令行使用`mvn test`来运行自动化测试，如GIF所示：

![](.\document\MVN_Test.gif)

#### 自定义监听器

正如前面所说，我们在使用Extent Reports的过程中是需要使用自定义监听器的。

TestNG的自定义监听器非常简单，写好了你的监听器文件之后，在XML文件的`<listeners></listeners>`中加入你的自定义监听器即可。

项目中加入了两个自定义监听器，均可以在项目src\main目录下找到，一个是Extent Reports的自定义监听器，另一个是Log的自定义监听器。

#### 常用工具箱

项目中还加入了常用的工具来方便大家开发，如：

1. **切换到WebView context**

2. **切换到Native context**

3. 向上/向下滚动屏幕

4. 滚动屏幕到指定元素（WebView context）

5. WebView下模拟Native Tap方法（Android & iOS）

6. 截图

7. **等待元素**（WebView context）

   这个是非常常用的方法，在写自动化代码的时候经常用到，为显示等待，实现了三种显示等待：

   1. wait for element present
   2. wait for element visible
   3. wait for element clickable

所有的这些实用方法均放在`AppiumUtils.java`类中。该类为线程安全的懒加载的单例模式，使用方法为：

```java
AppiumUtils appiumUtils = AppiumUtils.getAppiumUtils();
```

### 目录结构

> -.mvn                     // 集成Maven Wrapper，可以在根目录命令行下使用`mvnw`来代替`mvn`以避免Maven版本带来的问题
>
> -document            // 文档
>
> -logs                       // 日志文件
>
> -src                         // 代码
>
> ​	-main
>
> ​		-java               // Java代码
>
> ​		-resources     // 资源文件，配置文件等
>
> ​	-test                    // 自动化代码
>
> -test-output           // ReportNG
>
> -test-ouotput2       // ExtentReports
>
> mvnw                      // mvnw for linux
>
> mvnw.cmd             // mvnw for windows
>
> pom.xml
>
> README.md

### 实例

项目中集成了两个例子，一个是Android Native App的自动化，一个是Hybrid App的自动化。

#### Android Native App: Calculator

这里使用安卓自带的计算器来作为Native的示例

##### 用例描述

1. 打开安卓自带的计算器
2. 输入：1 + 2
3. 输出：3

##### Capabilities

Android 10原生系统中不带计算器，需要去Google Play下载。

```xml
<parameter name="appiumRemoteURL" value="http://127.0.0.1:4723/wd/hub"/>

<!-- Modify parameters according to your needs -->
<parameter name="deviceName" value="Android"/>
<parameter name="platformName" value="Android"/>
<parameter name="platformVersion" value="10.0"/>
<parameter name="automationName" value="UIAutomator2"/>

<!-- App name, not path, put your apk file under “resources/apks” folder. -->
<parameter name="appName" value=""/>
<!--
        Android Only
        If there is "appName", will ignore the "appPackage" and "appActivity",
          otherwise, will use "appPackage" and "appActivity"
    -->
<parameter name="appPackage" value="com.google.android.calculator"/>
<parameter name="appActivity" value="com.android.calculator2.Calculator"/>
```

##### 运行效果

![](.\document\TestAndroidNative.gif)

#### Hybrid App: Chrome

这里我们选用Chrome来作为Hybrid App的示例。

##### 用例描述

1. 打开Chrome浏览器
2. 跳过配置
3. 访问Baidu.com
4. 输入Appium到搜索框并搜索
5. 展示搜索结果

其中1，2，3步骤在native context中完成，之后需要切换到WebView context中才能继续操作，切换context的代码使用的正是上文所提到的`AppiumUtils.java`中的切换代码。

##### capabilities

```xml
<parameter name="appiumRemoteURL" value="http://127.0.0.1:4723/wd/hub"/>

<!-- Modify parameters according to your needs -->
<parameter name="deviceName" value="Android"/>
<parameter name="platformName" value="Android"/>
<parameter name="platformVersion" value="10.0"/>
<parameter name="automationName" value="UIAutomator2"/>

<!-- App name, not path, put your apk file under “resources/apks” folder. -->
<parameter name="appName" value=""/>
<!--
        Android Only
        If there is "appName", will ignore the "appPackage" and "appActivity",
          otherwise, will use "appPackage" and "appActivity"
    -->
<parameter name="appPackage" value="com.android.chrome"/>
<parameter name="appActivity" value="com.google.android.apps.chrome.Main"/>
```

##### 运行效果

![](.\document\TestWebViewGif.gif)

### 总结

这套框架目前我也在使用，依旧在不断完善，这里开源出来，欢迎大家使用并提意见。