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

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * The GeoSpatial allows to visualize geospatial data.
 */
//@SuppressWarnings("serial")
public class GeoSpatial extends CustomComponent
{

	// reference to the global internal state
	private LOD2DemoState state;

	// fields
	private String service  = "http://localhost/ssb";
	private String username = "" ;
	private String password = "";
	private ExportSelector3 exportGraph;
	private Embedded geobrowser;

	public GeoSpatial(LOD2DemoState st) {

		// The internal state and 
		state = st;

	    initLogin();

		HorizontalLayout geospatiallayout = new HorizontalLayout();


		// Configuration form start
		// Set all properties at once for the moment.
		Form t2f = new Form();
        t2f.setDebugId(this.getClass().getSimpleName()+"_t2f");
		t2f.setCaption("Configuration");


		exportGraph = new ExportSelector3(state, true, "Select graph with geo data:");
        exportGraph.setDebugId(this.getClass().getSimpleName()+"_exportGraph");
		t2f.getLayout().addComponent(exportGraph);

		// initialize the footer area of the form
		HorizontalLayout t2ffooterlayout = new HorizontalLayout();
		t2f.setFooter(t2ffooterlayout);

		Button commitButton = new Button("Set configuration", new ClickListener() {
				public void buttonClick(ClickEvent event) {
				storeConfiguration(event);
				}
				});
        commitButton.setDebugId(this.getClass().getSimpleName()+"_commitButton");
		commitButton.setDescription("Commit the new configuration settings.");
		t2f.getFooter().addComponent(commitButton);

		geospatiallayout.addComponent(t2f);


		// Configuration form end
		geobrowser = new Embedded();
		try { 

			URL url = new URL(service);
			geobrowser = new Embedded("", new ExternalResource(url));
			geobrowser.setType(Embedded.TYPE_BROWSER);
			geospatiallayout.addComponent(geobrowser);
			geobrowser.setHeight(-1, Sizeable.UNITS_PERCENTAGE);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		};


		// The composition root MUST be set
		setCompositionRoot(geospatiallayout);
	}

	private void storeConfiguration(ClickEvent event) {
		if (exportGraph.getExportGraph() == null || exportGraph.getExportGraph().equals("")) {
		} else {
			try {
				String encodedGraph = URLEncoder.encode(exportGraph.getExportGraph(), "UTF-8");
				URL url = new URL(service + "?default-graph-uri=" + encodedGraph);
				ExternalResource res = new ExternalResource(url);
				geobrowser.setSource(res);
				//			geobrowser.setParameter("default-graph-uri", encodedGraph);
			} catch (UnsupportedEncodingException e) { 
				e.printStackTrace();
			} catch (MalformedURLException e) { 
				e.printStackTrace();
			};
		}


	};

	// propagate the information of one tab to another.
	public void setDefaults() {
	};

	private void initLogin() {
		try {

			RepositoryConnection con = state.getRdfStore().getConnection();

			// initialize the hostname and portnumber
            String query = "select ?u ?p ?s from <" + state.getConfigurationRDFgraph() + "> where {<" + state.getConfigurationRDFgraph() + "> <http://lod2.eu/lod2demo/configures> <http://localhost/ssb>. <http://localhost/ssb> <http://lod2.eu/lod2demo/password> ?p. <http://localhost/ssb> <http://lod2.eu/lod2demo/username> ?u. <http://localhost/ssb> <http://lod2.eu/lod2demo/service> ?s.} LIMIT 100";
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();

				Value valueOfH = bindingSet.getValue("u");
				if (valueOfH instanceof LiteralImpl) {
					LiteralImpl literalH = (LiteralImpl) valueOfH;
					username = literalH.getLabel();
				};	
				Value valueOfP = bindingSet.getValue("p");
				if (valueOfP instanceof LiteralImpl) {
					LiteralImpl literalP = (LiteralImpl) valueOfP;
					password = literalP.getLabel();
				};	
				Value valueOfS = bindingSet.getValue("s");
				if (valueOfS instanceof LiteralImpl) {
					LiteralImpl literalS = (LiteralImpl) valueOfS;
					String service0 = literalS.getLabel();
					state.processService(service0,"ssb");
				};	
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


};

