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
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Embed the Virtuoso iSPARQL query tool
 */
//@SuppressWarnings("serial")
public class VirtuosoISPARQL extends CustomComponent
{

	// reference to the global internal state
	private LOD2DemoState state;

	public VirtuosoISPARQL(LOD2DemoState st) {

		// The internal state and 
		state = st;

        Embedded browser = new Embedded();
	try { 
		URL url;
		if (state.getHostName().equals("http://localhost:8080")) {
			url = new URL("http://localhost:8890/isparql");
		} else {
	  		url = new URL(state.getHostName(false) + "/virtuoso/isparql");
		};
		browser = new Embedded("", new ExternalResource(url));
		browser.setType(Embedded.TYPE_BROWSER);
		browser.setSizeFull();
	} catch (MalformedURLException e) {
                e.printStackTrace();
	};

		// The composition root MUST be set
		setCompositionRoot(browser);
	}

	// propagate the information of one tab to another.
	public void setDefaults() {
	};

};

