package eu.lod2;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;

public class SparqledManager extends CustomComponent {
	
private LOD2DemoState state;
	
	public SparqledManager(LOD2DemoState state){
		this.state = state;
		
		Embedded browser = new Embedded();
		try {
			URL url = new URL(state.getHostName() + "/sparqled/manager/");
			browser = new Embedded("", new ExternalResource(url));
			browser.setType(Embedded.TYPE_BROWSER);
			browser.setSizeFull();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		// The composition root MUST be set
		setCompositionRoot(browser);
	}

}
