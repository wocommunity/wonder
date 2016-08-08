package er.ajax.example2.components;

import java.util.Map;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXComponent;

public class FoundationAndjQueryTab extends ERXComponent {
	private NSMutableArray<Map> selectedPersons = new NSMutableArray<Map>();
	private NSArray<Map> persons;
	private Map person;
	
	public FoundationAndjQueryTab(WOContext context) {
        super(context);
    }
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		persons = (NSArray<Map>) valueForBinding("persons");
		super.appendToResponse(response, context);
	}
    
	public NSArray<Map> persons() {
		return persons;
	}
	public void setPersons(NSArray<Map> persons) {
		this.persons = persons;
	}

	public Map person() {
		return person;
	}
	public void setPerson(Map person) {
		this.person = person;
	}
	
	public String personTooltip() {
		return "<b>Person</b><br/>"
				+ "<b>Name: </b>"+person.get("name")+"<br/>"
				+ "<b>Age: </b>"+person.get("age");
	}
    
	public NSArray<Map> selectedPersons() {
		return selectedPersons;
	}

	public boolean personSelected() {
		return selectedPersons.contains(person());
	}

	public void setPersonSelected(boolean value) {
		if (value && personSelected() == false) {
			selectedPersons.add(person());
		}
		if (value == false && personSelected()) {
			selectedPersons.remove(person());
		}
	}
}