package eu.lod2.lod2testsuite.testcases;

import eu.lod2.lod2testsuite.configuration.TestCase;
import eu.lod2.lod2testsuite.pages.OntoWikiPage;
import org.openqa.selenium.By;
import org.testng.Assert;

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
public class Authoring extends TestCase {
    
   
    /**
     * TC 001.
     * pre: ontoWiki is accessible.
     * post: user is logged into ontoWiki
     */
    @Test(groups = { "ontowiki" })
    @Parameters({"ontowiki.user","ontowiki.pw"})
    public void logIntoOntoWiki(String user, String pw)  {
        driver.get(url.resolve("/ontowiki").toString());
        bf.waitUntilElementIsVisible("Ontowiki could not be loaded in time.",
                By.id("application"));
        OntoWikiPage ontoWiki = new OntoWikiPage();
        ontoWiki.logIntoOntoWiki(user, pw);
    }
    
    /**
     * TC 002.
     * pre: Knowledge Base with the same URI does not exist.
     * post: New knowledge base exists.
     */
    @Test(groups = { "ontowiki" }, dependsOnMethods= {"logIntoOntoWiki"})
    @Parameters({"ontowiki.user","ontowiki.pw","knowledgeBaseTitle","knowledgeBaseUri"})
    public void createNewKnowledgeBase(String user, String pw,String knowledgeBaseTitle, String knowledgeBaseUri)  {
        driver.get(url.resolve("/ontowiki").toString());
        bf.waitUntilElementIsVisible("Ontowiki could not be loaded in time.",
                By.id("application"));
        OntoWikiPage ontoWiki = new OntoWikiPage();
        ontoWiki.logIntoOntoWiki(user, pw);
        ontoWiki.createNewKnowledgeBase(knowledgeBaseTitle, knowledgeBaseUri, "");
    }
    
    /**
     * TC 003.
     * pre: Knowledge base is accessible; Web resource is available.
     * post: Knowledge base has data added.
     */
    @Test(groups = { "ontowiki" }, dependsOnMethods= {"logIntoOntoWiki","createNewKnowledgeBase"})
    @Parameters({"ontowiki.user","ontowiki.pw","knowledgeBaseTitle","knowledgeBaseUri"})
    public void addDataToKnowledgeBaseViaRDFFromWeb(String user, String pw, String knowledgeBaseUri, String importUri)  {
        driver.get(url.resolve("/ontowiki").toString());
        bf.waitUntilElementIsVisible("Ontowiki could not be loaded in time.",
                By.id("application"));
        OntoWikiPage ontoWiki = new OntoWikiPage();
        ontoWiki.logIntoOntoWiki(user, pw);
        ontoWiki.addDataToKnowledgeBaseViaRDFFromWeb(knowledgeBaseUri, importUri);
    }
    
    /**
     * TC 004.
     * pre: Knowledge base is accessible; Resource with same title does not exist already.
     * post: New Resource exists in knowledge base.
     */
    @Test(groups = { "ontowiki" }, dependsOnMethods= {"logIntoOntoWiki","createNewKnowledgeBase"})
    @Parameters({"ontowiki.user","ontowiki.pw","knowledgeBaseUri","resourceTitle"})
    public void addResource(String user, String pw, String knowledgeBaseUri, String resourceTitle)  {
        driver.get(url.resolve("/ontowiki").toString());
        bf.waitUntilElementIsVisible("Ontowiki could not be loaded in time.",
                By.id("application"));
        OntoWikiPage ontoWiki = new OntoWikiPage();
        ontoWiki.logIntoOntoWiki(user, pw);
        ontoWiki.addResource(knowledgeBaseUri, resourceTitle);
    }
   
    /**
     * TC 005.
     * pre: Resource is accessible; Instance with same title does not exist already.
     * post: New Instance is added to existing Resource.
     */
    @Test(groups = { "ontowiki" }, dependsOnMethods= {"logIntoOntoWiki","createNewKnowledgeBase","addResource"})
    @Parameters({"ontowiki.user","ontowiki.pw","knowledgeBaseUri","resourceTitle","instanceTitle"})
    public void addInstanceToResource(String user, String pw, String knowledgeBaseUri, String resourceTitle, String instanceTitle)  {
        driver.get(url.resolve("/ontowiki").toString());
        bf.waitUntilElementIsVisible("Ontowiki could not be loaded in time.",
                By.id("application"));
        OntoWikiPage ontoWiki = new OntoWikiPage();
        ontoWiki.logIntoOntoWiki(user, pw);
        ontoWiki.addInstanceToResource(knowledgeBaseUri, resourceTitle, instanceTitle);
    }
    
    
    /**
     * TC 001 - 006.
     */
    @Test
    @Parameters({ "ontowiki.user", "knowledgeBaseTitle", "knowledgeBaseUri", "importUri", "resourceTitle", "instanceTitle"})
    public void addAndEditKnowledgeBase(String username, String knowledgeBaseTitle, String knowledgeBaseUri, String importUri, String resourceTitle, String instanceTitle)  {
        
        navigator.navigateTo(new String[] {
            "Authoring", 
            "OntoWiki"});  
        By frameIdentifier = By.xpath("//iframe[contains(@src,'ontowiki')]");
        
        bf.checkIFrame(
                frameIdentifier, 
                By.id("application"));
        
        OntoWikiPage ontoWiki = new OntoWikiPage(frameIdentifier);   
        
        // Login into onto wiki
        ontoWiki.logIntoOntoWiki(username,"");
        
        // Create new Knowledge base
        ontoWiki.createNewKnowledgeBase(knowledgeBaseTitle,knowledgeBaseUri,"");
        
        // Add data
        ontoWiki.addDataToKnowledgeBaseViaRDFFromWeb(knowledgeBaseUri, importUri);
        
        //TODO: drop off sparql query?
        // Add resource
        ontoWiki.addResource(knowledgeBaseUri, resourceTitle);
        
        //Add instance
        ontoWiki.addInstanceToResource(knowledgeBaseUri, resourceTitle, instanceTitle);
    }
    
    /**
     * TC 007.
     * pre: Knowledge base to delete exists
     * post: Knowledge base is deleted.
     */
    @Test
    @Parameters({ "username","knowledgeBaseUri" })
    public void deleteKnowledgeBase(String username, String knowledgeBaseUri)  {
        By frameIdentifier = By.xpath("//iframe[contains(@src,'ontowiki')]");
        if(bf.isElementVisible(frameIdentifier))  {
            logger.info("Already on correct page. Skipping navigation");
        } else {
            navigator.navigateTo(new String[] {
            "Authoring", 
            "OntoWiki"});  
        }
        bf.checkIFrame(frameIdentifier, By.id("application"));
        
        OntoWikiPage ontoWiki = new OntoWikiPage(frameIdentifier);   
        // Perform login if necessary
        ontoWiki.logIntoOntoWiki(username, "");
        // Delete KB
        ontoWiki.navigateToContextMenuEntry(knowledgeBaseUri,"Delete Knowledge");
        // Check for delted
        By element = By.xpath("//div[@class='section-sidewindows']//a[" +bf.xpathEndsWith("@about", knowledgeBaseUri) +"]");
        bf.waitUntilElementDisappears("Knowledgebase was not correctly deleted. It is still"
                + "visible after delete.", element);        
    }
    
    /**
     * TC 002.
     */
    @Test
    public void publishToCkan()  {
        bf.checkAndChooseDefaultGraph();
        navigator.navigateTo(new String[] {
            "Authoring", 
            "Publish to CKAN"});  

        bf.waitUntilElementIsVisible("Could not find CKAN input fields.", 
                By.cssSelector("input.v-textfield"));
        //TODO: further testing
    }
}