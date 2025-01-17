/*
 * Copyright 2011 LOD2 consortium
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

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * The extraction tab which collects information about 
 * ways and components to extract information.
 */
//@SuppressWarnings("serial")
public class QueryingTab extends CustomComponent
	implements TextChangeListener 
{

	private Panel sparqlResult = new Panel("Query Results");

	// reference to the global internal state
	private LOD2DemoState state;

	// reference to the resource of the ontowiki query;
	private Link ontowikiquerylink;
	//
	// queryform
	private String querygraph = "";
	private TextField graphname;

	public QueryingTab(LOD2DemoState st) {

		// The internal state and 
		state = st;

		VerticalLayout queryingTab = new VerticalLayout();

		Form t2f = new Form();
        t2f.setDebugId(this.getClass().getSimpleName()+"_t2f");
		t2f.setCaption("Information Source Querying");

		graphname = new TextField("repository graph name:");
        graphname.setDebugId(this.getClass().getSimpleName()+"_graphname");
/*		if (state == null | state.getCurrentGraph() == null | state.getCurrentGraph().equals("")) {
			graphname.setValue("");
		} else {
			graphname.setValue(state.getCurrentGraph());
		};
		*/

		// configure & add to layout
		graphname.setImmediate(true);
		graphname.addListener(this);
		graphname.setColumns(30);
		graphname.setRequired(true);
		graphname.setRequiredError("Name of the graph is missing. No query will be issued.");
		t2f.getLayout().addComponent(graphname);

		// initialize the footer area of the form
		HorizontalLayout t2ffooterlayout = new HorizontalLayout();
		t2f.setFooter(t2ffooterlayout);

		Button okbutton = new Button("List graph content", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				extractionQuery(event);
			}
		});
        okbutton.setDebugId(this.getClass().getSimpleName()+"_okbutton");
		okbutton.setDescription("View the result from the SPARQL query: 'select * from <graphname> where {?s ?p ?o.} LIMIT 100'");
		//								okbutton.addListener(this); // react to tclicks
		
		ExternalResource ontowikiquery = new ExternalResource(state.getHostName() + "/ontowiki/queries/editor/?query=&m=http://mytest.com");

		ontowikiquerylink = new Link("Query via Ontowiki", ontowikiquery);
		ontowikiquerylink.setTargetName("_blank");
		ontowikiquerylink.setTargetBorder(Link.TARGET_BORDER_NONE);
		ontowikiquerylink.setEnabled(false);
		ThemeResource ontoWikiIcon = new ThemeResource("app_images/OntoWiki.logo.png");
		ontowikiquerylink.setIcon(ontoWikiIcon);

		t2f.getFooter().addComponent(okbutton);
		t2ffooterlayout.setComponentAlignment(okbutton, Alignment.TOP_RIGHT);
		t2f.getFooter().addComponent(ontowikiquerylink);
		t2ffooterlayout.setComponentAlignment(ontowikiquerylink, Alignment.TOP_RIGHT);

		queryingTab.addComponent(t2f);
		queryingTab.addComponent(sparqlResult);

		final Panel t2components = new Panel("LOD2 components interfaces");

		VerticalLayout t2ComponentsContent = new VerticalLayout();

		// dummy request
		ExternalResource ontowikiquery2 = new ExternalResource(state.getHostName() + "/ontowiki/queries/editor/?query=&m=");
		Link ontowikiquerylink2 = new Link("Ontowiki", ontowikiquery2);
		ontowikiquerylink2.setTargetName("_blank");
		ontowikiquerylink2.setTargetBorder(Link.TARGET_BORDER_NONE);
		ThemeResource ontoWikiIcon2 = new ThemeResource("app_images/OntoWiki.logo.png");
		ontowikiquerylink2.setIcon(ontoWikiIcon2);
		t2ComponentsContent.addComponent(ontowikiquerylink2);

		t2components.setContent(t2ComponentsContent);
		queryingTab.addComponent(t2components);

		// The composition root MUST be set
		setCompositionRoot(queryingTab);
	}

	private void extractionQuery(ClickEvent event) {

		try {
			RepositoryConnection con = state.getRdfStore().getConnection();

			if (querygraph.equals("")) {

				sparqlResult.removeAllComponents();
				getWindow().showNotification("No query issued.");

			} else {
				//Initialize the result page
				sparqlResult.removeAllComponents();

				String query = "select * from <" + querygraph + "> where {?s ?p ?o} LIMIT 100";
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
				TupleQueryResult result = tupleQuery.evaluate();
			
				String statements = "";
				while (result.hasNext()) {
					BindingSet bindingSet = result.next();
					Value valueOfS = bindingSet.getValue("s");
					Value valueOfP = bindingSet.getValue("p");
					Value valueOfO = bindingSet.getValue("o");

					String objectType = "";
					String objectString = "";
				        if (valueOfO instanceof LiteralImpl) {
						objectType = "literal";
						LiteralImpl literalO = (LiteralImpl) valueOfO;
						objectString = "\"" + literalO.getLabel() + "\" ^^ <" + literalO.getDatatype() + ">";

					};	
				        if (valueOfO instanceof URIImpl) {
						objectType = "resource";
						objectString = "<" + valueOfO.stringValue() + ">";
					};	

					String triple = "<" + valueOfS.stringValue() + ">  <" + valueOfP.stringValue() + "> " + 
							objectString; 

					statements = statements + "\n" + triple;


					// do something interesting with the values here...
				}
				TextArea resultArea = new TextArea("", statements);
                resultArea.setDebugId(this.getClass().getSimpleName()+"_resultArea");
				resultArea.setReadOnly(true);
				resultArea.setColumns(0);
				resultArea.setRows(30);
				sparqlResult.addComponent(resultArea);
			}

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	};

	public void textChange(TextChangeEvent event) {
		
	querygraph = event.getText();	
		/*
		final String Query = "SELECT * where {?s ?p ?o.} LIMIT 20";
		String Encoded = "";
		try {
			Encoded = URLEncoder.encode(Query, "UTF-8");
		} catch (UnsupportedEncodingException e) { 
			Encoded = "error";
			e.printStackTrace();
		};
		ExternalResource ontowikiquery = new ExternalResource("http://localhost/ontowiki/queries/editor/?query=" + Encoded + "&m=" + event.getText() );
		ontowikiquerylink.setResource(ontowikiquery);
		*/

		activateOntoWikiQuery();
	}

	// propagate the information of one tab to another.
	public void setDefaults() {
		if (querygraph.equals("")) {    
			// on empty set the default value
			querygraph = state.getCurrentGraph();
			graphname.setValue(querygraph);
		};
		activateOntoWikiQuery();
	};

	private void activateOntoWikiQuery() {
		if (querygraph.equals("")) {
		    ontowikiquerylink.setEnabled(false);
		} else {    
		    final String query = "SELECT * where {?s ?p ?o.} LIMIT 20";
		    String encoded = "";
		    try {
			encoded = URLEncoder.encode(query, "UTF-8");
			String encodedGraph = URLEncoder.encode(querygraph, "UTF-8");
			ExternalResource o = new ExternalResource(
			    state.getHostName(false) + "/ontowiki/queries/editor/?query=" + encoded + "&m=" + encodedGraph);
			ontowikiquerylink.setResource(o);
			ontowikiquerylink.setEnabled(true);
		    } catch (UnsupportedEncodingException e) { 
			ontowikiquerylink.setEnabled(false);
			e.printStackTrace();
		    };
		};
	};
};

