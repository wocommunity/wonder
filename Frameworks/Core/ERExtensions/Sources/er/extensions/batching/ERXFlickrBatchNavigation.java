package er.extensions.batching;

import java.io.Serializable;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.components.ERXComponent;
import er.extensions.localization.ERXLocalizer;

/**
 * <p>
 * ERXFlickrBatchNavigation is a batch navigation component that provides 
 * pagination that behaves like the paginator on Flickr.com.
 * </p>
 * 
 * <p>
 * Include ERXFlickrBatchNavigation.css in ERExtensions for a default stylesheet
 * that looks (very) similar to Flickr.
 * </p>
 * <p>Can also be used for pagination on the parent component, where the objects being paginated may be POJOs in an array,
 * or where paging all the objects in the allObjects array is not feasible due to memory requirements.</p>
 * 
 * @author mschrag
 * @author rob, cug (non displayGroup batching)
 * 
 * @binding displayGroup the display group to paginate
 * @binding displayName the name of the items that are being display ("photo", "bug", etc)
 * @binding showPageRange if <code>true</code>, the range of items on the page is shown, for example "(1-7 of 200 items)"
 * @binding showBatchSizes if <code>true</code>, a menu to change the items per page is shown "Show: (10) 20 (100) (All) items per page"
 * @binding batchSizes can be either a string or an NSArray of numbers that define the batch sizes to chose from. The number "0" provides an "All" items batch size. For example "10,20,30" or "10,50,100,0"
 * @binding small if <code>true</code>, a compressed page count style is used 
 * 
 * @binding parentActionName (if you don't provide a displayGroup) the action to be executed on the parent component to get the next batch of items.
 * @binding currentBatchIndex (if you don't provide a displayGroup) used to get and set on the parent component the selected page index
 * @binding maxNumberOfObjects (if you don't provide a displayGroup) used to get the total number of objects that are being paginated.
 * @binding numberOfObjectsPerBatch (if you don't provide a displayGroup) the number of objects per batch (page)
 */
public class ERXFlickrBatchNavigation extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private int _lastPageCount;
	private int _lastPageSize;
	private int _lastCurrentPageNumber;
	private NSMutableArray<PageNumber> _pageNumbers;
	private PageNumber _repetitionPageNumber; 
	
	public Integer currentBatchSize;
	
	//Note: Lazily Cached
	private String _parentActionName;

	public ERXFlickrBatchNavigation(WOContext context) {
		super(context);
		_lastPageCount = -1;
		_lastCurrentPageNumber = -1;
		_lastPageSize = -1;
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public NSArray objects() {
		NSArray objects = null;
		if(displayGroup() != null){
			if (displayGroup() instanceof ERXDisplayGroup) {
				ERXDisplayGroup dg = (ERXDisplayGroup) displayGroup();
				objects =  dg.filteredObjects();
			} else {
				objects =  displayGroup().allObjects();
			}
		} 
		return objects;
	}
	
	public WODisplayGroup displayGroup() {
		return (WODisplayGroup) valueForBinding("displayGroup");
	}

	public void setRepetitionPageNumber(PageNumber repetitionPageNumber) {
		_repetitionPageNumber = repetitionPageNumber;
	}

	public PageNumber repetitionPageNumber() {
		return _repetitionPageNumber;
	}
	
	public boolean hasMultiplePages() {
		if(batchCount() > 1) {
			return true;
		}
		if(showBatchSizes() && possibleBatchSizes().objectAtIndex(0).intValue() < displayNameCount()) {
			return true;
		}
		return false;
	}
	
	public boolean showLabels() {
		if(batchCount() > 1) {
			return true;
		}
		if(showBatchSizes()) {
			return batchSize() != 0;
		}
		return false;
	}

	public boolean hasPreviousPage() {
		return currentBatchIndex() > 1;
	}

	public WOActionResults previousPage() {
		WOActionResults previousPage = null;
		if(displayGroup() != null){
			WODisplayGroup displayGroup = displayGroup();
			displayGroup.displayPreviousBatch();
		} else if(parentActionName() != null){
			Integer previousBatchIndex = Integer.valueOf((currentBatchIndex() - 1));
			if(previousBatchIndex.intValue() < 1){
				previousBatchIndex = Integer.valueOf(1);
			} 
			setValueForBinding(previousBatchIndex, "currentBatchIndex");
			previousPage = performParentAction(parentActionName());
		}
		return previousPage;
	}

	public boolean hasNextPage() {
		return currentBatchIndex() < batchCount();
	}

	public WOActionResults nextPage() {
		WOActionResults nextPage = null;
		if(displayGroup() != null){
			WODisplayGroup displayGroup = displayGroup();
			displayGroup.displayNextBatch();
		} else if(parentActionName() != null){
			Integer nextBatchIndex = Integer.valueOf(currentBatchIndex() + 1);
			int pageCount  = batchCount();
			if(nextBatchIndex.intValue()  > pageCount){
				nextBatchIndex = Integer.valueOf(pageCount);
			} 
			setValueForBinding(nextBatchIndex, "currentBatchIndex");
			nextPage = performParentAction(parentActionName());
		}
		return nextPage;
	}

	public WOActionResults selectPage() {
		WOActionResults selectPage = null;
		Integer pageNumber = _repetitionPageNumber.pageNumber();
		if (pageNumber != null) {
			if (displayGroup() != null) {
				displayGroup().setCurrentBatchIndex(pageNumber.intValue());
			} else {
				setValueForBinding(pageNumber, "currentBatchIndex");
				selectPage = performParentAction(parentActionName());
			}
		}
		return selectPage;
	}

	public String displayName() {
		String displayName = stringValueForBinding("displayName");
		if (displayName == null) {
			displayName = stringValueForBinding("objectName"); // CHECKME should this be deprecated?
		}
		if (displayName == null) {
			displayName = ERXLocalizer.currentLocalizer().localizedStringForKey("ERXFlickrBatchNavigation.item");
		}
		return displayName;
	}
	
	public Integer displayNameCount(){
		Integer displayNameCount = Integer.valueOf(0);
		if(displayGroup() != null){
			if (displayGroup() instanceof ERXBatchingDisplayGroup) {
				displayNameCount = Integer.valueOf(((ERXBatchingDisplayGroup)displayGroup()).rowCount());
			} else {
				NSArray objects = objects();
				if(objects != null && objects.count() > 0){
					displayNameCount = Integer.valueOf(objects.count());
				}
			}
		} else {
			displayNameCount = Integer.valueOf(maxNumberOfObjects());
		}
		
		return displayNameCount;
	}

	public boolean isCurrentPageNumber() {
		Integer pageNumber = _repetitionPageNumber.pageNumber();
		return pageNumber != null && pageNumber.intValue() == _lastCurrentPageNumber;
	}

	public NSArray<PageNumber> pageNumbers() {
		int pageCount = batchCount();
		int currentPageNumber = currentBatchIndex();
		int pageSize = numberOfObjectsPerBatch();
		if (_lastPageCount != pageCount || _lastCurrentPageNumber != currentPageNumber || _lastPageSize != pageSize) {
			_pageNumbers = new NSMutableArray<PageNumber>();

			int nearEdgeCount;
			int endCount;
			int nearCount;
			int minimumCount;

			if (booleanValueForBinding("small")) {
				nearEdgeCount = 1;
				endCount = 1;
				nearCount = 0;
				minimumCount = 5;
			}
			else {
				nearEdgeCount = 8;
				endCount = 2;
				nearCount = 3;
				minimumCount = 15;
			}
			
			if (pageCount <= minimumCount) {
				addPageNumbers(1, pageCount);
			}
			else if (currentPageNumber <= nearEdgeCount) {
				addPageNumbers(1, Math.max(nearEdgeCount - 1, currentPageNumber + nearCount));
				addEllipsis();
				addPageNumbers(pageCount - endCount + 1, pageCount);
			}
			else if (currentPageNumber > pageCount - nearEdgeCount) {
				addPageNumbers(1, endCount);
				addEllipsis();
				addPageNumbers(Math.min(pageCount - nearEdgeCount + 2, currentPageNumber - nearCount), pageCount);
			}
			else {
				addPageNumbers(1, endCount);
				if (currentPageNumber - nearCount > (endCount + 1)) {
					addEllipsis();
				}
				addPageNumbers(Math.max(endCount + 1, currentPageNumber - nearCount), Math.min(currentPageNumber + nearCount, pageCount - endCount));
				if (currentPageNumber + nearCount < pageCount - endCount) {
					addEllipsis();
				}
				addPageNumbers(pageCount - endCount + 1, pageCount);
			}

			_lastPageCount = pageCount;
			_lastCurrentPageNumber = currentPageNumber;
			_lastPageSize = pageSize;
		}
		return _pageNumbers;
	}
	
	protected void addEllipsis() {
		_pageNumbers.addObject(new PageNumber(null, true));
		
	}
	protected void addPageNumbers(int startIndex, int endIndex) {
		for (int pageNumber = startIndex; pageNumber <= endIndex; pageNumber++) {
			_pageNumbers.addObject(new PageNumber(Integer.valueOf(pageNumber), false));
		}
	}

	public static class PageNumber implements Serializable {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		private Integer _pageNumber;
		private boolean _ellipsis;

		public PageNumber(Integer pageNumber, boolean ellipsis) {
			_pageNumber = pageNumber;
			_ellipsis = ellipsis;
		}

		public Integer pageNumber() {
			return _pageNumber;
		}

		public boolean isEllipsis() {
			return _ellipsis;
		}
	}
	
	public int batchCount(){
		int batchCount = 0;
		if(displayGroup() != null){
			batchCount = displayGroup().batchCount();
		} else {
			int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
			int maxNumberOfObjects = maxNumberOfObjects();
			if (numberOfObjectsPerBatch != 0){	
				if (maxNumberOfObjects == 0)
					batchCount = 1;
				else
					batchCount = (maxNumberOfObjects - 1) / numberOfObjectsPerBatch + 1;
			}
		}
		return batchCount;	
	}
	
	public int numberOfObjectsPerBatch(){
		int numberOfObjects = 0;
		if(displayGroup() != null){
			numberOfObjects = displayGroup().numberOfObjectsPerBatch();
		} else {
			numberOfObjects = intValueForBinding("numberOfObjectsPerBatch", 0);
			if (numberOfObjects < 0) {
				numberOfObjects = 0;
			}
		}
		return numberOfObjects;
	}
	
	public int maxNumberOfObjects() {
		int maxNumber = intValueForBinding("maxNumberOfObjects", 0); 
		if (maxNumber < 0) {
			maxNumber = 0;
		}
		return maxNumber;
	}
	
	public int currentBatchIndex(){
		int index = 1;
		if(displayGroup() != null){
			index = displayGroup().currentBatchIndex();
		} else {
			index = intValueForBinding("currentBatchIndex", 1);
			if (index < 1) {
				index = 1;
			}
		}
		return index;
	}
	
	public String parentActionName(){
		if(_parentActionName == null){
			_parentActionName = stringValueForBinding("parentActionName");
		}
		return _parentActionName;
	}
	
	public int firstIndex(){
		int firstIndex = 0;
		if(displayGroup() != null){
			firstIndex  = displayGroup().indexOfFirstDisplayedObject();
		} else {
			int currentBatchIndex = currentBatchIndex();
			int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
			firstIndex = (currentBatchIndex * numberOfObjectsPerBatch) - (numberOfObjectsPerBatch - 1); 
		}
		return firstIndex;
	}
	
	public int lastIndex(){
		int lastIndex = 0;
		if(displayGroup() != null){
			lastIndex  = displayGroup().indexOfLastDisplayedObject();
		} else {
			int currentBatchIndex = currentBatchIndex();
			int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
			lastIndex = currentBatchIndex * numberOfObjectsPerBatch;
			if(lastIndex > maxNumberOfObjects()) {
				lastIndex = maxNumberOfObjects();
			}
		}
		return lastIndex;
	}
	
	public boolean showBatchSizes() {
		if(booleanValueForBinding("showBatchSizes") || valueForBinding("batchSizes") != null) {
			return true;
		}
		return false;
	}
	
	public NSArray<? extends Number> possibleBatchSizes() {
		Object value = valueForBinding("batchSizes");
		if(value == null) {
			return new NSArray<Integer>(new Integer[] {Integer.valueOf(10), Integer.valueOf(50), Integer.valueOf(100), Integer.valueOf(0)});
		}
		NSMutableArray<Integer> result = new NSMutableArray<Integer>();
		if (value instanceof String) {
			String[] parts = value.toString().split("\\s*,");
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				result.addObject(Integer.valueOf(part));
			}
		} else if (value instanceof NSArray) {
			result.addObjectsFromArray((NSArray)value);
		}
		return result;
	}

	public int batchSize() {
		if(displayGroup() == null) {
			return 0;
		}
		return displayGroup().numberOfObjectsPerBatch();
	}
	
	public String currentBatchSizeString() {
		return currentBatchSize == 0 ? ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault("ERXFlickrBatchNavigation.all") :  (currentBatchSize + "");
	}
	
	public boolean isCurrentBatchSizeSelected() {
		if(currentBatchSize == null) {
			return batchSize() == 0;
		}
		return currentBatchSize.equals(batchSize());
	}
	
	public WOActionResults selectBatchSize() {
		displayGroup().setNumberOfObjectsPerBatch(currentBatchSize);
		return context().page();
	}
}