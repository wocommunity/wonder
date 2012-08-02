package er.neo4jadaptor.utils;

import java.util.Comparator;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSSelector;

import er.neo4jadaptor.ersatz.Ersatz;


@SuppressWarnings("unchecked")
public class SortingComparator <T extends Comparable> implements Comparator<Ersatz> {
	private final NSArray<EOSortOrdering> sortOrdering;
	private final EOEntity entity;
	
	public SortingComparator(EOEntity entity, NSArray<EOSortOrdering> sortOrdering) {
		this.sortOrdering = sortOrdering;
		this.entity = entity;
	}
	
	private static boolean isAscendingSelector(NSSelector<?> selector) { 
		return selector.equals(EOSortOrdering.CompareAscending) || selector.equals(EOSortOrdering.CompareCaseInsensitiveAscending);
	}
	
	private static boolean isCaseInsensitive(NSSelector<?> selector) {
		return selector.equals(EOSortOrdering.CompareCaseInsensitiveAscending) || selector.equals(EOSortOrdering.CompareCaseInsensitiveDescending);
	}

	public int compare(Ersatz u1, Ersatz u2) {
		for (EOSortOrdering so : sortOrdering) {
			EOAttribute att = entity.attributeNamed(so.key());
			T c1 = (T) u1.get(att);
			T c2 = (T) u2.get(att);
			final int result;
			
			if (c2 == null) {
				if (c1 == null) {
					return 0;
				} else {
					return -1;
				}
			}
			if (c1 == null) {
				return -compare(u2, u1);
			}
			if (isCaseInsensitive(so.selector())) {
				String s1 = (String) c1;
				String s2 = (String) c2;
				
				result = s1.compareToIgnoreCase(s2);
			} else {
				result = c1.compareTo(c2);
			}
			
			if (result != 0) {
				if (isAscendingSelector(so.selector())) {
					return result;
				} else {
					return - result;
				}
			}
		}
		return 0;
	}
}
