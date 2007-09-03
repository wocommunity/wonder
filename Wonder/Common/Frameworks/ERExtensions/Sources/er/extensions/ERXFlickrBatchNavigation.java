package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

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
 * 
 * @author mschrag
 * @binding displayGroup the display group to paginate
 * @binding displayName the name of the items that are being display ("photo", "bug", etc)
 */
public class ERXFlickrBatchNavigation extends WOComponent {
	private int _lastPageCount;
	private int _lastPageSize;
	private int _lastCurrentPageNumber;
	private NSMutableArray<PageNumber> _pageNumbers;
	private PageNumber _repetitionPageNumber;

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
    WODisplayGroup displayGroup = displayGroup();
	  return displayGroup.batchCount() > 1;
	}
	
	public boolean hasPreviousPage() {
		WODisplayGroup displayGroup = displayGroup();
		return displayGroup.currentBatchIndex() > 1;
	}

	public WOActionResults previousPage() {
		WODisplayGroup displayGroup = displayGroup();
		displayGroup.displayPreviousBatch();
		return null;
	}

	public boolean hasNextPage() {
		WODisplayGroup displayGroup = displayGroup();
		return displayGroup.currentBatchIndex() < displayGroup.batchCount();
	}

	public WOActionResults nextPage() {
		WODisplayGroup displayGroup = displayGroup();
		displayGroup.displayNextBatch();
		return null;
	}

	public WOActionResults selectPage() {
		WODisplayGroup displayGroup = displayGroup();
		Integer pageNumber = _repetitionPageNumber.pageNumber();
		if (pageNumber != null) {
			displayGroup.setCurrentBatchIndex(pageNumber.intValue());
		}
		return null;
	}

	public String displayName() {
		String displayName = (String) valueForBinding("displayName");
		if (displayName == null) {
			displayName = ERXLocalizer.currentLocalizer().localizedStringForKey("ERXFlickrBatchNavigation.item");
		}
		return displayName;
	}

	public boolean isCurrentPageNumber() {
		Integer pageNumber = _repetitionPageNumber.pageNumber();
		return pageNumber != null && pageNumber.intValue() == _lastCurrentPageNumber;
	}

	public NSArray<PageNumber> pageNumbers() {
		WODisplayGroup displayGroup = displayGroup();
		int pageCount = displayGroup.batchCount();
		int currentPageNumber = displayGroup.currentBatchIndex();
		int pageSize = displayGroup.numberOfObjectsPerBatch();
		if (_lastPageCount != pageCount || _lastCurrentPageNumber != currentPageNumber || _lastPageSize != pageSize) {
			_pageNumbers = new NSMutableArray<PageNumber>();

			int endCount = 2;
			int nearCount = 3;
			
			if (pageCount <= 15) {
				addPageNumbers(1, pageCount);
			}
			else if (currentPageNumber <= 8) {
				addPageNumbers(1, Math.max(7, currentPageNumber + nearCount));
				addEllipsis();
				addPageNumbers(pageCount - endCount + 1, pageCount);
			}
			else if (currentPageNumber > pageCount - 8) {
				addPageNumbers(1, endCount);
				addEllipsis();
				addPageNumbers(Math.min(pageCount - 6, currentPageNumber - nearCount), pageCount);
			}
			else {
				addPageNumbers(1, endCount);
				addEllipsis();
				addPageNumbers(currentPageNumber - nearCount, currentPageNumber + nearCount);
				addEllipsis();
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

	public static class PageNumber {
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
}