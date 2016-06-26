package er.ajax.example2.components;

import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXArrayUtilities;

public class FoundationAndjQuery extends AjaxWOWODCPage {
    private NSArray<Map<String, Object>> personsFromOntario;
	private NSArray<Map<String, Object>> personsFromQuebec;
	private boolean tabOntarioSelected = true;
	private boolean tabQuebecSelected = false;

	public FoundationAndjQuery(WOContext context) {
        super(context);
        loadData();
    }

	@Override
	protected boolean useDefaultComponentCSS() {
		return true;
	}

	private void loadData() {
    	WOResourceManager resourceManager = application().resourceManager();
    	byte[] fileContent = resourceManager.bytesForResourceNamed("sampleData.json", null, null);
    	String json;
    	try {
    		json = new String(fileContent, "utf-8");

    		JSONParser parser = new JSONParser();

			List<Map<String,Object>> fileArray = (List<Map<String, Object>>)parser.parse(json);
			
    		NSDictionary<String, NSArray<Map<String, Object>>> peopleByRegion = ERXArrayUtilities.arrayGroupedByKeyPath(fileArray, "region");
			
    		personsFromOntario = peopleByRegion.get("Ontario");
    		personsFromQuebec = peopleByRegion.get("Quebec");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	public NSArray<Map<String,Object>> personsFromOntario() {
		return personsFromOntario;
	}

	public NSArray<Map<String,Object>> personsFromQuebec() {
		return personsFromQuebec;
	}

	public boolean tabOntarioSelected() {
		return tabOntarioSelected;
	}
	public void setTabOntarioSelected(boolean tabOntarioSelected) {
		this.tabOntarioSelected = tabOntarioSelected;
	}

	public boolean tabQuebecSelected() {
		return tabQuebecSelected;
	}
	public void setTabQuebecSelected(boolean tabQuebecSelected) {
		this.tabQuebecSelected = tabQuebecSelected;
	}
}