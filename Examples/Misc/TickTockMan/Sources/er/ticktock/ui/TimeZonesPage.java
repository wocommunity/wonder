package er.ticktock.ui;

import java.util.TimeZone;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class TimeZonesPage extends WOComponent {

	public TimeZonesPage(WOContext context) {
		super(context);
	}

	public static final String ID_KEY = "id";
	public static final NSArray<EOSortOrdering> idSortsAsc;
	public static final NSArray<EOSortOrdering> idSortsDesc;

	public static final String NAME_KEY = "displayName";
	public static final NSArray<EOSortOrdering> nameSortsAsc;
	public static final NSArray<EOSortOrdering> nameSortsDesc;

	public static final String DST_KEY = "dstSavings";
	public static final NSArray<EOSortOrdering> dstSortsAsc;
	public static final NSArray<EOSortOrdering> dstSortsDesc;

	public static final String OFF_KEY = "rawOffset";
	public static final NSArray<EOSortOrdering> offsetSortsAsc;
	public static final NSArray<EOSortOrdering> offsetSortsDesc;

	
	static {
		idSortsAsc = new NSArray(EOSortOrdering.sortOrderingWithKey(ID_KEY, EOSortOrdering.CompareAscending));
		idSortsDesc = new NSArray(EOSortOrdering.sortOrderingWithKey(ID_KEY, EOSortOrdering.CompareDescending));

		nameSortsAsc = new NSArray(EOSortOrdering.sortOrderingWithKey(NAME_KEY, EOSortOrdering.CompareAscending));
		nameSortsDesc = new NSArray(EOSortOrdering.sortOrderingWithKey(NAME_KEY, EOSortOrdering.CompareDescending));

		dstSortsAsc = new NSArray(EOSortOrdering.sortOrderingWithKey(DST_KEY, EOSortOrdering.CompareAscending));
		dstSortsDesc = new NSArray(EOSortOrdering.sortOrderingWithKey(DST_KEY, EOSortOrdering.CompareDescending));

		offsetSortsAsc = new NSArray(EOSortOrdering.sortOrderingWithKey(OFF_KEY, EOSortOrdering.CompareAscending));
		offsetSortsDesc = new NSArray(EOSortOrdering.sortOrderingWithKey(OFF_KEY, EOSortOrdering.CompareDescending));
}

	public NSArray<EOSortOrdering> currentSorts = idSortsAsc;

	public WOComponent sortById() { if (currentSorts == idSortsAsc) currentSorts = idSortsDesc; else currentSorts = idSortsAsc; return null; }
	public WOComponent sortByName() { if (currentSorts == nameSortsAsc) currentSorts = nameSortsDesc; else currentSorts = nameSortsAsc; return null; }
	public WOComponent sortByDSTSavings() { if (currentSorts == dstSortsAsc) currentSorts = dstSortsDesc; else currentSorts = dstSortsAsc; return null; }
	public WOComponent sortByRawOffset() { if (currentSorts == offsetSortsAsc) currentSorts = offsetSortsDesc; else currentSorts = offsetSortsAsc; return null; }

	NSDictionary<String,NSDictionary<String,Object>> zones = null;

	public NSArray<String> allTimeZones() {
		if (zones == null) {
			NSMutableDictionary<String,NSDictionary<String,Object>> zonesHash = new NSMutableDictionary<String,NSDictionary<String,Object>>();
			String foundZones[] = TimeZone.getAvailableIDs();

			zones = new NSMutableDictionary<String,NSDictionary<String,Object>>();

			for (int idx = 0; idx < foundZones.length; idx++) {

				String id = foundZones[idx];
				TimeZone tz = TimeZone.getTimeZone(id);

				NSArray<String> keys = new NSArray<String>(new String[] { ID_KEY, NAME_KEY, DST_KEY, OFF_KEY } );
				NSArray<Object> vals = new NSArray<Object>(new Object[] { id, tz.getDisplayName(), Integer.valueOf(tz.getDSTSavings()), Integer.valueOf(tz.getRawOffset()) } );
				NSDictionary foundValues = new NSDictionary(vals,keys);

				zonesHash.takeValueForKey(foundValues, id);
			}
			zones = zonesHash.immutableClone();
		}
		NSArray<NSDictionary<String,Object>> sorted = EOSortOrdering.sortedArrayUsingKeyOrderArray(zones.allValues(), currentSorts);
		return (NSArray<String>)sorted.valueForKey(ID_KEY);
	}

	public String timeZoneId;

	public String tzDisplayName() {
		return (String)((NSDictionary)zones.valueForKey(timeZoneId)).valueForKey(NAME_KEY);
	}

	public Integer tzDSTSavings() {
		return (Integer)((NSDictionary)zones.valueForKey(timeZoneId)).valueForKey(DST_KEY);
	}

	public Integer tzRawOffset() {
		return (Integer)((NSDictionary)zones.valueForKey(timeZoneId)).valueForKey(OFF_KEY);
	}

	public WOComponent timeDisplayNow() {
		WOComponent nextPage = pageWithName("TimeDisplayNow");
		nextPage.takeValueForKey(timeZoneId, "timeZoneID");
		return nextPage;
	}
}
