package ns.foundation;


import java.util.Comparator;

@SuppressWarnings("unchecked")
public abstract class NSComparator<T> implements Comparator<T> {

	public static final int OrderedAscending = -1;
	public static final int OrderedSame = 0;
	public static final int OrderedDescending = 1;

	public NSComparator() {
		super();
	}
	
	@Override
  public abstract int compare (T object1, T object2) throws ComparisonException;
	
	public static int _compareObjects(Comparable<Comparable<?>> object1, Comparable<?> object2) {
	  if(object1 == object2)
	    return 0;
	  if(object1 == null)
	    return -1;
	  if(object2 == null)
	    return 1;
	  int comparison = object1.compareTo(object2);
	  if(comparison < 0)
	    return -1;
	  return comparison > 0 ? 1 : 0;
	}
	
  private static final NSComparator<? extends Comparable<?>> AscendingComparableComparator = new NSComparator<Comparable<Object>>() {
		@Override
    public int compare(Comparable<Object> object1, Comparable<Object> object2) {
			if (object1 == null && object2 == null)
				return OrderedSame;
			else if (object1 == null)
				return OrderedDescending;
			else if (object2 == null)
				return OrderedAscending;
			else {
				int result = object1.compareTo(object2);
				if (result > 0)
					return OrderedDescending;
				else if (result < 0)
					return OrderedAscending;
				else
					return OrderedSame;
			}
		}
	};
	
  private static final NSComparator<? extends Comparable<?>> DescendingComparableComparator = new NSComparator<Comparable<Object>>() {
		@Override
    public int compare(Comparable<Object> object1, Comparable<Object> object2) {
			if (object1 == null && object2 == null)
				return OrderedSame;
			else if (object1 == null)
				return OrderedAscending;
			else if (object2 == null)
				return OrderedDescending;
			else {
				int result = object1.compareTo(object2);
				if (result < 0)
					return OrderedDescending;
				else if (result > 0)
					return OrderedAscending;
				else
					return OrderedSame;
			}		
		}
	};
	
  public static final NSComparator<String> AscendingStringComparator = (NSComparator<String>) AscendingComparableComparator;	
	public static final NSComparator<String> DescendingStringComparator = (NSComparator<String>) DescendingComparableComparator;
	
	public static final NSComparator<Number> AscendingNumberComparator = (NSComparator<Number>) AscendingComparableComparator;
	public static final NSComparator<Number> DescendingNumberComparator = (NSComparator<Number>) DescendingComparableComparator;
	
	public static final NSComparator<NSTimestamp> AscendingTimestampComparator = (NSComparator<NSTimestamp>) AscendingComparableComparator;
	public static final NSComparator<NSTimestamp> DescendingTimestampComparator = (NSComparator<NSTimestamp>) DescendingComparableComparator;
	
	public static final NSComparator<String> AscendingCaseInsensitiveStringComparator = new NSComparator<String>() {
		@Override
    public int compare(String object1, String object2) {
			if (object1 == null && object2 == null)
				return OrderedSame;
			else if (object1 == null)
				return OrderedDescending;
			else if (object2 == null)
				return OrderedAscending;
			else {
				int result = object1.toLowerCase().compareTo(object2.toLowerCase());
				if (result > 0)
					return OrderedDescending;
				else if (result < 0)
					return OrderedAscending;
				else
					return OrderedSame;
			}
		}
	};
	
	public static final NSComparator<String> DescendingCaseInsensitiveStringComparator = new NSComparator<String>() {
		@Override
    public int compare(String object1, String object2) {
			if (object1 == null && object2 == null)
				return OrderedSame;
			else if (object1 == null)
				return OrderedAscending;
			else if (object2 == null)
				return OrderedDescending;
			else {
				int result = object1.toLowerCase().compareTo(object2.toLowerCase());
				if (result < 0)
					return OrderedDescending;
				else if (result > 0)
					return OrderedAscending;
				else
					return OrderedSame;
			}		
		}
	};
	
	@SuppressWarnings("serial")
  public static class ComparisonException extends RuntimeException {
		public ComparisonException(String message) {
			super(message);
		}
	}
}
