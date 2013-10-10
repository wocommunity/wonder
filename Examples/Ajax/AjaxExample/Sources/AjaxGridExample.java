import java.util.Random;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

public class AjaxGridExample extends WOComponent {

	private NSMutableDictionary configData;
	public WODisplayGroup displayGroup;

	public AjaxGridExample(WOContext context) {
		super(context);
		displayGroup = new WODisplayGroup();
		displayGroup().setObjectArray(sampleData());

		// We preset the batch size so that our x / N display is updated when
		// the page first renders
		displayGroup().setNumberOfObjectsPerBatch(Integer.parseInt((String) configData().valueForKey("batchSize")));
	}

	private NSMutableArray sampleData() {
		NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AjaxGridExampleTestData.plist", null, NSArray.EmptyArray));
		NSMutableArray sampleData = new NSMutableArray((NSArray) NSPropertyListSerialization.propertyListFromData(data, CharEncoding.UTF_8));
		Random random = new Random(new NSTimestamp().getNanos());
		for (int i = 0; i < sampleData.count(); i++) {
			NSMutableDictionary<String,Object> row = (NSMutableDictionary<String,Object>) sampleData.objectAtIndex(i);
			row.setObjectForKey(Integer.valueOf(random.nextInt()), "number");
			row.setObjectForKey(new NSTimestamp(random.nextLong()), "date");
			row.setObjectForKey(Integer.valueOf(i + 1).toString(), "level");
		}

		return sampleData;

	}

	public NSMutableDictionary configData() {
		if (configData == null) {
			NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AjaxGridExampleConfiguration.plist", null, NSArray.EmptyArray));
			configData = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, CharEncoding.UTF_8));
		}

		return configData;
	}

	/**
	 * @return the disply group containing the data
	 */
	public WODisplayGroup displayGroup() {
		return displayGroup;
	}

	public void updateBatchSize() {
	}

	public void nextBatch() {
		displayGroup().displayNextBatch();
		displayGroup().setSelectedObject(null);
	}

	public void previousBatch() {
		displayGroup().displayPreviousBatch();
		displayGroup().setSelectedObject(null);
	}

}
