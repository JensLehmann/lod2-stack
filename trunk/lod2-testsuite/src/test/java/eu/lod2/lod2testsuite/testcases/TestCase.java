package eu.lod2.lod2testsuite.testcases;

import com.thoughtworks.selenium.Selenium;
import eu.lod2.lod2testsuite.configuration.BasicFunctions;
import eu.lod2.lod2testsuite.configuration.FirefoxProfileConfig;
import eu.lod2.lod2testsuite.configuration.MyWebDriverEventListener;
import eu.lod2.lod2testsuite.configuration.Navigator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;


/**
 * Represents a base class for all test cases.
 *
 * @author Stefan Schurischuster
 */
public abstract class TestCase {
    private String url;

    public static WebDriver driver; 
    public static Selenium selenium;
    public static Actions driverActions;
    public static Navigator navigator;
    public static BasicFunctions bf;
    public static WebDriverEventListener eventListener;
    private static final Logger logger = Logger.getLogger(TestCase.class);
    
    /**
     * Initialises browser and opens the web page.
     * 
     * @param context 
     *          Contains the necessary meta information from the testng.xml
     */
    @BeforeSuite(alwaysRun=true)
    public void setUp(ITestContext context) {
        logger.info("STARTING");
        // Get parameters from testng.xml
        url = context.getCurrentXmlTest().getParameter("selenium.url");
        
        logger.info("Projects root directory is "+System.getProperty("user.dir"));
        
        String filesDir = System.getProperty("user.dir") + File.separator + "files";
        String firebugPath = filesDir + File.separator + "firefox"
                + File.separator + "firebug-1.9.2.xpi";
        String firefinderPath = filesDir + File.separator + "firefox"
                + File.separator + "firefinder_for_firebug-1.2.2.xpi";
        // Create new FirefoxProfile:
        FirefoxProfileConfig config = new FirefoxProfileConfig(filesDir);
        try {
            // Add firebug extension
            config.addFireBugExtension(firebugPath);
            // Add firefinder extension
            config.addExtension(firefinderPath);
        } catch (FileNotFoundException ex) {
            Assert.fail("Could not find firefox-plugin: " + ex.getMessage());
        } catch (IOException ex) {
            Assert.fail("Something went wrong trying to register "
                    + "plugins at firefox profile.: " + ex.getMessage());
        }
        // Create WebDriver instance.
        eventListener = new MyWebDriverEventListener();
        driver = new EventFiringWebDriver(
                new FirefoxDriver(config.getConfiguredProfile()))
                .register(eventListener);
        // Set implicit waitingtime when a field is not available
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        // Create Selenium instance (to be able to use selenium 1 api).
        selenium = new WebDriverBackedSelenium(driver, url);
        driverActions = new Actions(driver);
        navigator = new Navigator(driver);
        bf = new BasicFunctions(driver);
        // Open Website
        selenium.open(url);
        // Wait for page to be completely displayed.
        WebElement elem = bf.waitUntilElementIsVisible(
                By.xpath("//img[contains(@src,'lifecycle')]"));
    }
    
    /**
     * Returns an absolute path using this projects root folder
     * as source for the parameters relativePath.
     * 
     * @param relativePath
     *          A relative path starting from root directory of this project.
     * @return 
     *          An absolute path matching the relative path.
     */
    protected String getAbsolutePath(String relativePath)  {
        return System.getProperty("user.dir") + File.separator + relativePath;
    }
    
    /**
     * This method is run before every test method and puts the window focus
     * back to its original position. 
     * This is necessary when switching iframes.
     */
    @BeforeMethod(alwaysRun=true)
    public void prepareTestCase()  {
        driver.switchTo().defaultContent();
        logger.debug("Switching to default frame.");
                
        WebElement toMove = bf.getVisibleElement(By.xpath(
                "//img[@src='/lod2demo/VAADIN/themes/lod2/app_images/logo-lod2-small.png']"));
        //driverActions.moveToElement(toMove).build().perform();
        // Reposition the browser view to be at the top.
        bf.scrollIntoView(toMove);
    }
    
    /**
     * Closes opened error messages.
     * Error messages from earlier test cases can interfere with current 
     * test case. Therefore if an error message is present it has to be closed.
     */
    @AfterMethod(alwaysRun=true)
    public void afterTestCase()  {
        // Error message is visible.
        if(bf.isElementVisible(bf.getErrorPopupLocator()))  {
            WebElement message =  bf.getVisibleElement(bf.getErrorPopupLocator());
            logger.fatal("Error message is visible with text: " + message.getText());
            message.click();
            bf.waitUntilElementDisappears(By.xpath("//div[@class='gwt-HTML']"));   
        }
    }
    
    /**
     * Stops browser.
     */
    @AfterSuite(alwaysRun=true)
    public void tearDown()  {
        logger.info("STOPPING");
        //Insteat of driver.quit();
        //driver.quit();
        selenium.stop();
    }   
    
    /**
     * @return 
     *      The WebDriver instance.
     */
    public WebDriver getDriver()  {
        return this.driver;
    }
}