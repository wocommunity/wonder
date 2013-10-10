import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.ajax.example.ExampleDataFactory;
import er.ajax.example.Word;

public class UpdateDisplayGroupExample extends WOComponent {
	private static final Logger log = Logger.getLogger(UpdateDisplayGroupExample.class);

	public WODisplayGroup dg;
	public Word current;

	public UpdateDisplayGroupExample(WOContext context) {
		super(context);
		dg = new WODisplayGroup();
		dg.setObjectArray(ExampleDataFactory.allWords());
		dg.setNumberOfObjectsPerBatch(20);
	}

	private void setSortOrder(String name) {
		NSArray<EOSortOrdering> oldArray = dg.sortOrderings();
		EOSortOrdering oldOrdering = null;
		EOSortOrdering newOrdering = null;
		if (oldArray != null) {
			oldOrdering = oldArray.lastObject();
		}
		if (oldOrdering != null && oldOrdering.key().equals(name)) {
			newOrdering = EOSortOrdering.sortOrderingWithKey(name, oldOrdering.selector() == EOSortOrdering.CompareDescending ? EOSortOrdering.CompareAscending : EOSortOrdering.CompareDescending);
		}
		else {
			newOrdering = EOSortOrdering.sortOrderingWithKey(name, EOSortOrdering.CompareAscending);
		}
		dg.setSortOrderings(new NSArray<EOSortOrdering>(newOrdering));
		dg.qualifyDisplayGroup();
	}

	@Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		super.takeValuesFromRequest(worequest, wocontext);
	}

	public long millis() {
		return System.currentTimeMillis();
	}

	public void sortByName() {
		setSortOrder("name");
	}

	public void sortByValue() {
		setSortOrder("value");
	}

	public void nextBatch() {
		dg.displayNextBatch();
		dg.setSelectedObject(null);
	}

	public void previousBatch() {
		dg.displayPreviousBatch();
		dg.setSelectedObject(null);
	}

	public void selectObject() {
		dg.setSelectedObject(current);
	}

	public void save() {
		// do nothing
		log.info(dg.selectedObject());
	}
}
