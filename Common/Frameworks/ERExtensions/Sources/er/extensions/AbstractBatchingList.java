package er.extensions;

import java.util.AbstractList;

import com.webobjects.foundation.NSArray;

/**
 * Partial implementation of the IBatchingList interface that
 * handles page invalidation and most of the List interface.
 * 
 * @author mschrag
 */
public abstract class AbstractBatchingList extends AbstractList implements IBatchingList {
  private int myPageIndex;
  private int myPageSize;
  private NSArray mySortOrderings;

  public AbstractBatchingList() {
    myPageIndex = 1;
    myPageSize = 10;
  }

  /**
   * Returns whether or not the current page has been loaded.
   */
  protected abstract boolean isPageLoaded();

  /**
   * Loads one page into this list.
   * 
   * @param _startingIndex the index of the first element of the page (in element units, not page units)
   * @param _pageSize the size of the page to load
   * @param _sortOrderings the list of sort orderings
   */
  protected abstract void loadPage(int _startingIndex, int _pageSize, NSArray _sortOrderings);

  /**
   * Invalidates the current page of objects, requiring a reload.
   */
  protected abstract void invalidatePage();

  /**
   * Returns the total size of this list.
   */
  protected abstract int getTotalCount();

  /**
   * Returns the _index'th element from the current page.
   * 
   * @param _index the index of the element to return
   * @return the element
   */
  protected abstract Object getFromPage(int _index);

  protected void ensurePageLoaded(int _index) {
    int startIndex = myPageSize * (myPageIndex - 1);
    int endIndex = startIndex + myPageSize;
    int index = (_index == -1) ? startIndex : _index;
    boolean withinPage = index >= startIndex && index <= endIndex;
    if (!withinPage) {
      setPageIndex((_index / myPageSize) + 1);
    }
    if (!withinPage || !isPageLoaded()) {
      loadPage(startIndex, myPageSize, mySortOrderings);
    }
  }

  public Object get(int _index) {
    ensurePageLoaded(_index);
    Object obj = getFromPage(_index - myPageSize * (myPageIndex - 1));
    return obj;
  }

  public int size() {
    ensurePageLoaded(-1);
    int totalCount = getTotalCount();
    return totalCount;
  }

  public void setPageIndex(int _pageIndex) {
    myPageIndex = _pageIndex;
    invalidatePage();
  }

  public void setPageSize(int _pageSize) {
    myPageSize = _pageSize;
    invalidatePage();
  }

  public void setSortOrderings(NSArray _sortOrderings) {
    mySortOrderings = _sortOrderings;
    invalidatePage();
  }
}
