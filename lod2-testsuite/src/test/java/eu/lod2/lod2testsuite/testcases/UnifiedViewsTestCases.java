package eu.lod2.lod2testsuite.testcases;

import eu.lod2.lod2testsuite.configuration.TestCase;
import eu.lod2.lod2testsuite.pages.UnifiedViewsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class contains functional tests concerning authoring of
 * the lod2 - stack.
 * All TestCases are documented here:
 * https://grips.semantic-web.at/display/LOD2/LOD2Stack+Test+Cases+-+Authoring
 * 
 * @author Stefan Schurischuster
 * @email s.schurischuster@semantic-web.at
 */
public class UnifiedViewsTestCases extends TestCase {
    
    @BeforeMethod(alwaysRun=true)
    @Override
    public void prepareTestCase()  {
        logger.info("Preparing for onto wiki test...");
        logger.info("Current url: " +driver.getCurrentUrl());
        
        if(!driver.getCurrentUrl().equals(unifiedviewsUrl.toString()) &&
                !driver.getCurrentUrl().equals(unifiedviewsUrl.toString()+"/"))  {
            driver.get(unifiedviewsUrl.toString());
            logger.info("Navigating to: " +unifiedviewsUrl.toString());
        }
        // Reposition the browser view to be at the top.
        WebElement home = bf.waitUntilElementIsVisible(
                By.xpath("//div[contains(@class,'v-slot v-slot-loginPanel')]/descendant::span[1]"));
        bf.scrollIntoView(home);
        home.click();
        bf.waitUntilElementIsVisible("Could not find unified views home screen.", 
                By.xpath("//div[contains(@class,'v-slot v-slot-viewLayout')]"
                + "/descendant::a[contains(@href,'Introduction')] | "
                + "//div[contains(@class,'v-slot v-slot-viewLayout')]//h1[.='Login']"));
    }

    @BeforeMethod(alwaysRun=true)
    @Override
    public void afterTestCase() {}


    /**
     * TC 001.
     * pre: Unifiedviews is accessible.
     * post: User is logged into unifiedviews
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw"})
    public void logIntoUnifiedViews(String user, String pw)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);
    }
    
    /**
     * TC 002.
     * pre: Logged into unifiedviews
     * post: New pipeline is created
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name","pipeline.description","pipeline.visibility"})    
    public void createPipeline(String user, String pw, String name, String description, String visibility)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.createPipeline(name, description, visibility);
    }
    
    /**
     * TC 003.
     * pre: Logged into unifiedviews; Pipeline to create exists.
     * post: Pipeline is deleted
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name"})    
    public void deletePipeline(String user, String pw, String name)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.deletePipeline(name);
    }
    
    /**
     * TC 004.
     * pre: Logged into unifiedviews; Pipeline to copy exists
     * post: Pipeline is doubled
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name"})    
    public void copyPipeline(String user, String pw, String name)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.copyPipeline(name);
    }
    
    /**
     * TC 007.
     * pre: Pipeline to run exists.
     * post: Pipeline is executed
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name"})    
    public void runPipelineManually(String user, String pw, String name)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.runPipelineManually(name);
    }    
    
    /**
     * TC 005-01.
     * pre: At least two pipelines exist
     * post: Pipeline is scheduled to run after another pipeline has run.
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name","schedule.finishedPipeline"})    
    public void schedulePipelineAfterAnotherPipeline(String user, String pw, String name, String nameOfFinishedPipeline)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.schedulePipelineAfterAnotherPipeline(name, nameOfFinishedPipeline);
    }    
    
    /**
     * TC 005-02.
     * pre: Pipeline to run exists.
     * post: Pipeline is scheduled to run once, and is run once.
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name"})    
    public void schedulePipelineToRunOnce(String user, String pw, String name)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.schedulePipelineToRunOnce(name, null);
    }
    
    /**
     * TC 005-03.
     * pre: Pipeline to run exists.
     * post: Pipeline is scheduled in an interval.
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name"})    
    public void schedulePipelineToRunInInterval(String user, String pw, String name)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.schedulePipelineToRunInInterval(name, 1, "Minutes", 1);
    }    
    
    /**
     * TC 006.
     * pre: At least two pipelines exist
     * post: Pipeline is doubled
     */
    @Test(groups={"unifiedviews"})
    @Parameters({"unifiedviews.user","unifiedviews.pw","pipeline.name","schedule.ruleName"})    
    public void deleteScheduleRule(String user, String pw, String name, String ruleName)  {
        UnifiedViewsPage unifiedviews = new UnifiedViewsPage();
        unifiedviews.login(user, pw);   
        unifiedviews.deleteScheduleRule(name, ruleName);
    }  
}