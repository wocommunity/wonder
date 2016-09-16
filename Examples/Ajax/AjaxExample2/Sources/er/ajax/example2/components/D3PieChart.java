package er.ajax.example2.components;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.parser.JSONParser;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXKey;
import er.extensions.foundation.ERXArrayUtilities;

public class D3PieChart extends AjaxWOWODCPage {
	private NSArray<PieSegment> segments;
	private PieSegment segment;
	private Map person;
	private int totalNumberOfPersons;

	public D3PieChart(WOContext context) {
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
			totalNumberOfPersons = fileArray.size();
			
    		NSDictionary<String, NSArray<Map<String, Object>>> peopleByRegion = ERXArrayUtilities.arrayGroupedByKeyPath(fileArray, "region");
			
    		List<PieSegment> dataList = peopleByRegion.entrySet().stream().map(entry -> new PieSegment(entry.getKey(), entry.getValue())).collect(Collectors.toList());
			dataList.sort((e1, e2) -> e2.nbPersons - e1.nbPersons);

    		segments = new NSArray<>(dataList);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	public String pieDatas() {
		StringBuilder pieData = new StringBuilder("[");
		for (PieSegment segment : segments) {
			pieData.append("{");
			pieData.append("\"id\": \"").append(segment.containerID()).append("\", ");
			pieData.append("\"label\": \"").append(segment.region).append("\", ");
			pieData.append("\"value\": ").append(segment.nbPersons).append(", ");
			pieData.append("},");
		}
		pieData.append("]");
		return pieData.toString();
	}

	static public class PieSegment {
		static public ERXKey<String> REGION = new ERXKey<>("region");
		static public ERXKey<Map> PERSONS = new ERXKey<>("persons");
		
		public String region;
		public NSArray<Map<String,Object>> persons;
		public int nbPersons;
		public boolean open = false;
		
		public PieSegment(String region, NSArray<Map<String,Object>> persons) {
			this.region = region;
			this.persons = persons;
			this.nbPersons = persons.size();
		}
		
		public String containerID() {
			return "personList_"+region.replaceAll(" ", "_");
		}
		
		public String buttonName() {
			return (open ? "Hide" : "Show");
		}
	}
	
	public NSArray<PieSegment> segments() {
		return segments;
	}

	public PieSegment segment() {
		return segment;
	}

	public void setSegment(PieSegment value) {
		this.segment = value;
	}

	public WOActionResults toggleSegment() {
		segment().open ^= true;
		return null;
	}

	public String toggleFunctionName() {
		return "toggle_"+segment().containerID();
	}

	public String onClickHandler() {
		return toggleFunctionName()+"();";
	}

	public int nbTotal() {
		return totalNumberOfPersons;
	}

	public int pourcentType() {
		return segment().nbPersons * 100 / totalNumberOfPersons;
	}

	public Map person() {
		return person;
	}

	public void setPerson(Map person) {
		this.person = person;
	}
}