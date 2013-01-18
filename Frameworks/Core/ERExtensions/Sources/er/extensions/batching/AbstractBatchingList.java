package er.extensions.batching;

import java.util.AbstractList;

import com.webobjects.foundation.NSArray;


/**
 * Partial implementation of the IBatchingList interface that
 * handles page invalidation and most of the List interface.
 * 
 * @author mschrag
 */
public abstract class AbstractBatchingList extends AbstractList implements IBatchingList {
  private int _pageIndex;
  private int _pageSize;
  private NSArray _sortOrderings;

  public AbstractBatchingList() {
    _pageIndex = 1;
    _pageSize = 10;
  }

  /**
   * Returns whether or not the current page has been loaded.
   */
  protected abstract boolean isPageLoaded();

  /**
   * Loads one page into this list.
   * 
   * @param startingIndex the index of the first element of the page (in element units, not page units)
   * @param pageSize the size of the page to load
   * @param sortOrderings the list of sort orderings
   */
  protected abstract void loadPage(int startingIndex, int pageSize, NSArray sortOrderings);

  /**
   * Invalidates the current page of objects, requiring a reload.
   */
  protected abstract void invalidatePage();

  /**
   * Returns the total size of this list.
   */
  protected abstract int getTotalCount();

  /**
   * Returns the index'th element from the current page.
   * 
   * @param index the index of the element to return
   * @return the element
   */
  protected abstract Object getFromPage(int index);

  protected void ensurePageLoaded(int index) {
    int startIndex = _pageSize * (_pageIndex - 1);
    int endIndex = startIndex + _pageSize;
    int effectiveIndex = (index == -1) ? startIndex : index;
    boolean withinPage = effectiveIndex >= startIndex && effectiveIndex <= endIndex;
    if (!withinPage) {
      setPageIndex((index / _pageSize) + 1);
    }
    if (!withinPage || !isPageLoaded()) {
      loadPage(startIndex, _pageSize, _sortOrderings);
    }
  }

  @Override
  public Object get(int index) {
    ensurePageLoaded(index);
    Object obj = getFromPage(index - _pageSize * (_pageIndex - 1));
    return obj;
  }

  @Override
  public int size() {
    ensurePageLoaded(-1);
    int totalCount = getTotalCount();
    return totalCount;
  }

  public void setPageIndex(int pageIndex) {
    _pageIndex = pageIndex;
    invalidatePage();
  }

  public void setPageSize(int pageSize) {
    _pageSize = pageSize;
    invalidatePage();
  }

  public void setSortOrderings(NSArray sortOrderings) {
    _sortOrderings = sortOrderings;
    invalidatePage();
  }
}
