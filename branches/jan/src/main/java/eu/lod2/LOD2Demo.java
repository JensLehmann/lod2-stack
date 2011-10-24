/*
 * Copyright 2011 LOD2.eu consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.lod2;

import java.net.*;
import java.util.Iterator;

import com.vaadin.Application;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.Alignment.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field.ValueChangeEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.Layout.*;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractOrderedLayout.*;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.data.Property.*;
import com.vaadin.ui.MenuBar.*;


import eu.lod2.LOD2DemoState;
import eu.lod2.LOD2Exception;
import java.lang.RuntimeException;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class LOD2Demo extends Application 
   implements TabSheet.SelectedTabChangeListener
{

    private LOD2DemoState state;



    private TabSheet tabsheet = new TabSheet();
    private QueryingTab queryingTab;
    private AuthoringTab authoringTab;
    private ExtractionTab extractionTab;
    private LinkingTab linkingTab;
    private EnrichmentTab enrichmentTab;
    private OnlineToolsTab onlineToolsTab;
    private ConfigurationTab configurationTab;

    private TextField currentgraph;
    private Label     currentgraphlabel;
    private VerticalLayout workspace;

    @Override
        public void init() {

            state = new LOD2DemoState();

            final Window mainWindow = new Window("LOD2 Prototype");
            setTheme("lod2");


            final AbsoluteLayout welcomeSlagzin = new AbsoluteLayout();
            welcomeSlagzin.setWidth("370px");
            welcomeSlagzin.setHeight("75px");
            Embedded lod2logo = new Embedded("", new ThemeResource("app_images/logo-lod2-small.png"));
            lod2logo.setMimeType("image/png");
//            lod2logo.addStyleName("lod2logo");
//            Label slagzin = new Label("Creating Knowledge out of Interlinked Data");
            //slagzin.setContentMode(Label.CONTENT_XHTML);
            welcomeSlagzin.addComponent(lod2logo, "top:0px; left:5px");
//            welcomeSlagzin.addComponent(slagzin, "top:5px; left:80px");
            //	welcome.setComponentAlignment(lod2logo, Alignment.TOP_CENTER);
//            slagzin.addStyleName("slagzin");

	   // the current graph as label
           currentgraphlabel = new Label("no current graph selected");
           currentgraphlabel.addStyleName("currentgraphlabel");
	    // Create an horizontal container
	    HorizontalLayout welcomeContainer = new HorizontalLayout();

            //menubarContainer.addComponent(lod2logo);
            welcomeContainer.addComponent(welcomeSlagzin);
	    welcomeContainer.setComponentAlignment(welcomeSlagzin, Alignment.TOP_LEFT);
	    welcomeContainer.addComponent(currentgraphlabel);
	    welcomeContainer.setComponentAlignment(currentgraphlabel, Alignment.TOP_RIGHT);
	    welcomeContainer.setWidth("100%");
	
	    final VerticalLayout welcome = new VerticalLayout();
	    welcome.addComponent(welcomeContainer);

            mainWindow.addComponent(welcome);

	    
            //************************************************************************
	    //  menu bar style
	    //
	    MenuBar menubar = new MenuBar();

	    // First define all menu commands

	    MenuBar.Command me1c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
//			mainWindow.getContent().setSizeFull();
			ELoadRDFFile me1c_content = new ELoadRDFFile(state);
			workspace.addComponent(me1c_content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			float wh = mainWindow.getHeight() -90;
//			workspace.addComponent(new Label(Float.toString(wh)));
//			me1c_content.getContent().setHeight("100%");
			me1c_content.setWidth("100%");
//			me1c_content.setHeight("100%");
			me1c_content.setHeight(wh, mainWindow.getHeightUnits());
			workspace.setWidth("100%");
			workspace.setHeight("100%");
		    }  
		};

	    MenuBar.Command me3c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			EXML me3c_content = new EXML(extractionTab,state);
			workspace.addComponent(me3c_content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			me3c_content.setSizeFull();
		    }  
		};
	    MenuBar.Command me3cbis = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			EXMLExtended me3c_content = new EXMLExtended(extractionTab,state);
			workspace.addComponent(me3c_content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			me3c_content.setSizeFull();
		    }  
		};


	    MenuBar.Command me4c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			ESpotlight me4c_content = new ESpotlight(extractionTab);
			workspace.addComponent(me4c_content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			me4c_content.setHeight("500px");
		    }  
		};

	    MenuBar.Command me5c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			EPoolPartyExtractor me5c_content = new EPoolPartyExtractor(state);
			workspace.addComponent(me5c_content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			me5c_content.setHeight("90%");
		    }  
		};

	    MenuBar.Command me6c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			D2RCordis content = new D2RCordis(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command me7c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			DebCKAN content = new DebCKAN(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("90%");
		    }  
		};

	    MenuBar.Command silk = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			LinkingTab lsilk = new LinkingTab(state);
			workspace.addComponent(lsilk);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			lsilk.setSizeFull();
//			workspace.setSizeFull();
//			mainWindow.getContent().setSizeFull();
			workspace.setHeight("500px");
		    }  
		};

	    MenuBar.Command ore = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			ORE content = new ORE(state);
			workspace.addComponent(content);
			welcome.setHeight("110px");
			content.setSizeFull();
			workspace.setHeight("500px");
		    }  
		};

	    MenuBar.Command mconfiguration = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			ConfigurationTab content = new ConfigurationTab(state, currentgraphlabel);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mau = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			OntoWiki content = new OntoWiki(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mq1c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			SesameSPARQL content = new SesameSPARQL(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mq2c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			OntoWikiQuery content = new OntoWikiQuery(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mq3c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			VirtuosoSPARQL content = new VirtuosoSPARQL(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mq4c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			VirtuosoISPARQL content = new VirtuosoISPARQL(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo1c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			SameAs content = new SameAs(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo2c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			Sigma content = new Sigma(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo3c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			LODCloud content = new LODCloud(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo4c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			DBpedia content = new DBpedia(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo5c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			SPARQLPoolParty content = new SPARQLPoolParty(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};


	    MenuBar.Command mo6c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			OnlinePoolParty content = new OnlinePoolParty(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo7c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			MondecaSPARQLList content = new MondecaSPARQLList(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo8c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			CKAN content = new CKAN(state);
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    MenuBar.Command mo9c = new MenuBar.Command() {
		public void menuSelected(MenuItem selectedItem) {
			workspace.removeAllComponents();
			IframedUrl content = new IframedUrl(state, "http://publicdata.eu");
			workspace.addComponent(content);
			// stretch the content to the full workspace area
			welcome.setHeight("110px");
			content.setHeight("500px");
		    }  
		};

	    // Secondly define menu layout
	    // root menu's
	    MenuBar.MenuItem extraction    = menubar.addItem("Extraction & Loading", null, null);
	    MenuBar.MenuItem querying      = menubar.addItem("Querying", null, null);
	    MenuBar.MenuItem authoring     = menubar.addItem("Authoring", null, null);
	    MenuBar.MenuItem linking       = menubar.addItem("Linking", null, null);
	    MenuBar.MenuItem enrichment    = menubar.addItem("Enrichment", null, null);
	    MenuBar.MenuItem onlinetools   = menubar.addItem("Online Tools and Services", null, null);
	    MenuBar.MenuItem configuration = menubar.addItem("Configuration", null, mconfiguration);

	    // sub menu's 
	    MenuBar.MenuItem me1 = extraction.addItem("Upload RDF file", null, me1c);
	    MenuBar.MenuItem me2 = extraction.addItem("Load RDF data from CKAN", null, me7c);
	    MenuBar.MenuItem me3 = extraction.addItem("Extract RDF from XML", null, null);
	    MenuBar.MenuItem exml = me3.addItem("Basic extraction", null, me3c);
	    MenuBar.MenuItem extended = me3.addItem("Extended extraction", null, me3cbis);
	    MenuBar.MenuItem me6 = extraction.addItem("Extract RDF from SQL", null, me6c);
	    MenuBar.MenuItem me4 = extraction.addItem("Extract RDF from text w.r.t. DBpedia",null, me4c);
	    MenuBar.MenuItem me5 = extraction.addItem("Extract RDF from text w.r.t. a controlled vocabulary", null, me5c);

	    MenuBar.MenuItem mq1 = querying.addItem("Direct via Sesame", null, mq1c);
	    MenuBar.MenuItem mq2 = querying.addItem("OntoWiki SPARQL endpoint", null, mq2c);
	    MenuBar.MenuItem mq3 = querying.addItem("Virtuoso SPARQL endpoint", null, mq3c);
	    MenuBar.MenuItem mq4 = querying.addItem("Virtuoso interactive SPARQL endpoint", null, mq4c);

	    MenuBar.MenuItem ma = authoring.addItem("OntoWiki", null, mau);

	    MenuBar.MenuItem linking1 = linking.addItem("Silk", null, silk);

	    MenuBar.MenuItem enrichment1 = enrichment.addItem("ORE", null, ore);

	    MenuBar.MenuItem sameAs    = onlinetools.addItem("SameAs", null, mo1c);
	    MenuBar.MenuItem sigma     = onlinetools.addItem("Sigma EE", null, mo2c);
	    MenuBar.MenuItem ckan      = onlinetools.addItem("CKAN", null, mo8c);
	    MenuBar.MenuItem publicdata = onlinetools.addItem("Europe's Public Data", null, mo9c);
	    MenuBar.MenuItem poolparty = onlinetools.addItem("PoolParty", null, mo6c);
	    MenuBar.MenuItem sparqlonline = onlinetools.addItem("Online SPARQL endpoints", null, null);
	    MenuBar.MenuItem lodcloud        = sparqlonline.addItem("LOD cloud", null, mo3c);
	    MenuBar.MenuItem dbpedia         = sparqlonline.addItem("DBpedia", null, mo4c);
	    MenuBar.MenuItem sparqlpoolparty = sparqlonline.addItem("PoolParty SPARQL endpoint", null, mo5c);
	    MenuBar.MenuItem mondecalist     = sparqlonline.addItem("Mondeca SPARQL endpoint Collection", null, mo7c);


/*
	    // the current graph selection widget
            currentgraph = new TextField("", state.getCurrentGraph());
	    currentgraph.setNullRepresentation("no current graph selected");
            currentgraph.setImmediate(false);
            currentgraph.addListener(new TextChangeListener() {
    		public void textChange(TextChangeEvent event) {
        		currentGraphChange(event);
		}
	    });
            currentgraph.setColumns(50);
	    */
	   

	    HorizontalLayout menubarContainer = new HorizontalLayout();
            menubarContainer.addComponent(menubar);
            menubarContainer.addStyleName("menubarContainer");
	    menubarContainer.setWidth("100%");
            welcome.addComponent(menubarContainer);
	    welcome.setHeight("110px");
//	    mainWindow.getContent().setSizeFull();


	    //mainWindow.addComponent(currentgraph);
            //************************************************************************
	    // add workspace
	    workspace = new VerticalLayout();

	    mainWindow.addComponent(workspace);
	    workspace.setHeight("80%");

	    HorizontalLayout introH = new HorizontalLayout();
            Embedded lod2cycle = new Embedded("", new ThemeResource("app_images/lod-lifecycle-small.png"));
            lod2cycle.setMimeType("image/png");
	    introH.addComponent(lod2cycle);
	    introH.setComponentAlignment(lod2cycle, Alignment.MIDDLE_LEFT);

	    VerticalLayout introV =  new VerticalLayout();
	    introH.addComponent(introV);

	    Label introtextl =  new Label(introtext, Label.CONTENT_XHTML);
	    introV.addComponent(introtextl);
	    introtextl.setWidth("400px");

	    HorizontalLayout introVH =  new HorizontalLayout();
	    introV.addComponent(introVH);

            Embedded euflag = new Embedded("", new ThemeResource("app_images/eu-flag.gif"));
            euflag.setMimeType("image/gif");
	    introVH.addComponent(euflag);
            euflag.addStyleName("eugif");
            euflag.setHeight("50px");
            Embedded fp7 = new Embedded("", new ThemeResource("app_images/fp7-gen-rgb_small.gif"));
            fp7.setMimeType("image/gif");
            fp7.addStyleName("eugif");
            fp7.setHeight("50px");
	    introVH.addComponent(fp7);

	    workspace.addComponent(introH);
             
            

	    mainWindow.setSizeFull();
//	    mainWindow.getContent().setSizeFull();
//	    Iterator<Component> iterator = mainWindow.getContent().getComponentIterator();
//	    Component second = iterator.next();
//	    second.setSizeFull();

	    /* Obselete => should be cleaned.
	     *
            //************************************************************************
            // Extraction Tab
            extractionTab = new ExtractionTab(state);	

            tabsheet.addTab(extractionTab);
            tabsheet.getTab(extractionTab).setCaption("Loading & Extraction");


            //************************************************************************
            // Querying Tab
            queryingTab = new QueryingTab(state);	

            tabsheet.addTab(queryingTab);
            tabsheet.getTab(queryingTab).setCaption("Querying");

            //************************************************************************
            // Authoring Tab
            authoringTab = new AuthoringTab(state);	

            tabsheet.addTab(authoringTab);
            tabsheet.getTab(authoringTab).setCaption("Authoring");

            //************************************************************************
            // Enrichment Tab
            linkingTab = new LinkingTab(state);	

            tabsheet.addTab(linkingTab);
            tabsheet.getTab(linkingTab).setCaption("Linking");

            //************************************************************************
            // Enrichment Tab
            enrichmentTab = new EnrichmentTab(state);	

            tabsheet.addTab(enrichmentTab);
            tabsheet.getTab(enrichmentTab).setCaption("Enrichment");

            //************************************************************************
            // Online Tools Tab

            onlineToolsTab = new OnlineToolsTab("", state);	

            tabsheet.addTab(onlineToolsTab);
            tabsheet.getTab(onlineToolsTab).setCaption("Online Tools and Services");
            //************************************************************************
            // Configuration Tab
            configurationTab = new ConfigurationTab(state, currentgraphlabel);	

            tabsheet.addTab(configurationTab);
            tabsheet.getTab(configurationTab).setCaption("Configuration");

            tabsheet.addListener(this);
//            mainWindow.addComponent(tabsheet);


	*/

            setMainWindow(mainWindow);

        }

     public void selectedTabChange(SelectedTabChangeEvent event) {

            final TabSheet source = (TabSheet) event.getSource();

            if (source == tabsheet) {
		    propagateData(tabsheet);
            };
	    currentgraph.setValue(state.getCurrentGraph());
     };

    public void currentGraphChange(TextChangeEvent event) {

	if (event != null && event.getText() != null) {
        	state.setCurrentGraph(event.getText());
	};
	propagateData(tabsheet);

    };

    public void propagateData(TabSheet source) {
	if (source.getSelectedTab() == queryingTab) {
	    queryingTab.setDefaults();
	} else if (source.getSelectedTab() == authoringTab) {
	    authoringTab.setDefaults();
	} else if (source.getSelectedTab() == extractionTab) {
	    extractionTab.setDefaults();
	} else if (source.getSelectedTab() == linkingTab) {
	    linkingTab.setDefaults();
	} else if (source.getSelectedTab() == enrichmentTab) {
	    enrichmentTab.setDefaults();
	} else if (source.getSelectedTab() == onlineToolsTab) {
	    onlineToolsTab.setDefaults();
	} else if (source.getSelectedTab() == configurationTab) {
	    configurationTab.setDefaults();
	} else {
	    
	}
    }

    public void setDefaults() {
	    currentgraph.setValue(state.getCurrentGraph());
    };

    private String introtext = 
"<p>This is Version 1.0 of the LOD2 Stack, which comprises a number of tools " + 
"for managing the life-cycle of Linked Data. The life-cycle comprises in " + 
"particular the stages" + 
"</p><p>" + 
"<ul>" + 
"<li>Extraction of RDF from text, XML and SQL</li>" + 
"<li>Querying and Exploration using SPARQL</li>" + 
"<li>Authoring of Linked Data using a Semantic Wiki</li>" + 
"<li>Semi-automatic link discovery between Linked Data sources</li>" + 
"<li>Knowledge-base Enrichment and Repair</li>" + 
"</ul>" + 
"</p><p>" + 
"You can access tools for each of these stages using the menu on top." + 
"</p><p>" + 
"The LOD2 Stack is developed by the LOD2 project consortium comprising 15" + 
"research groups and companies. The LOD2 project is co-funded by the" + 
"European Commission within the 7th Framework Programme (GA no. 257934)." + 
"</p><p>" + 
"You can find further information about the LOD2 Stack and the LOD2" + 
"project at <a href=\"http://lod2.eu\">http://lod2.eu</a>." + 
"<p>";

}

