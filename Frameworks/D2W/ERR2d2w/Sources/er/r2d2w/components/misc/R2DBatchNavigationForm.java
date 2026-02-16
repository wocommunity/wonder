package er.r2d2w.components.misc;

import java.util.List;
import java.util.Optional;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.batching.ERXBatchingDisplayGroup;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXValueUtilities;

public class R2DBatchNavigationForm extends ERXNonSynchronizingComponent {

	private static final long serialVersionUID = 1L;

	private WODisplayGroup displayGroup;
	private transient D2WContext d2wContext;
	private NSArray<Integer> availableBatchSizes;
	private Integer batchSize;
	private Integer rowCount;
	private Integer selectedBatchSize;

	private Integer pageNumber;

	public R2DBatchNavigationForm(WOContext context) {
		super(context);
	}

	public void reset() {
		super.reset();
		displayGroup = null;
		d2wContext = null;
		availableBatchSizes = null;
		batchSize = null;
		rowCount = null;
		selectedBatchSize = null;
	}

	public WODisplayGroup displayGroup() {
		if (displayGroup == null) {
			displayGroup = (WODisplayGroup) valueForBinding("displayGroup");
		}
		return displayGroup;
	}

	public D2WContext d2wContext() {
		if (d2wContext == null) {
			d2wContext = (D2WContext) valueForBinding("d2wContext");
		}
		return d2wContext;
	}

	/**
	 * @return the availableBatchSizes
	 */
	public NSArray<Integer> availableBatchSizes() {
		if (availableBatchSizes == null) {
			NSArray<?> sizes = Optional.of("availableBatchSizes")
				.map(d2wContext()::valueForKey)
				.filter(NSArray.class::isInstance)
				.map(NSArray.class::cast)
				.orElseGet(NSArray::emptyArray);
			NSArray<Integer> batchSizes = sizes.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.map(Integer::parseInt)
				.collect(NSMutableArray::new, NSMutableArray::add, NSMutableArray::addAll);
			availableBatchSizes = batchSizes.immutableClone();
		}
		return availableBatchSizes;
	}

	public boolean hasMultipleBatchSizes() {
		return availableBatchSizes().count() > 1;
	}

	/**
	 * @return the batchSize
	 */
	public Integer batchSize() {
		return batchSize;
	}

	/**
	 * @param batchSize the batchSize to set
	 */
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public String batchSizeString() {
		if (ERXValueUtilities.intValue(batchSize()) == 0) {
			String all = localizer().localizedStringForKey("R2DBatchSize.all");
			return all;
		}
		return String.valueOf(batchSize());
	}

	public Integer rowCount() {
		if (rowCount == null) {
			if (displayGroup() instanceof ERXBatchingDisplayGroup b) {
				rowCount = b.rowCount();
			} else if (displayGroup().dataSource() != null) {
				rowCount = displayGroup().dataSource().fetchObjects().count();
			} else if (displayGroup().allObjects() != null) {
				rowCount = displayGroup().allObjects().count();
			} else {
				rowCount = 0;
			}
		}
		return rowCount;
	}

	/**
	 * @return the selectedBatchSize
	 */
	public Integer selectedBatchSize() {
		if(selectedBatchSize == null) {
			selectedBatchSize = displayGroup().numberOfObjectsPerBatch();
		}
		return selectedBatchSize;
	}

	/**
	 * @param selectedBatchSize the selectedBatchSize to set
	 */
	public void setSelectedBatchSize(Integer selectedBatchSize) {
		this.selectedBatchSize = selectedBatchSize;
	}
	
	public boolean isFirstBatch() {
		return displayGroup().currentBatchIndex() == 1;
	}
	
	public boolean isNotFirstBatch() {
		return !isFirstBatch();
	}
	
	public boolean isLastBatch() {
		return displayGroup().currentBatchIndex() == displayGroup().batchCount();
	}
	
	public boolean isNotLastBatch() {
		return !isLastBatch();
	}

	public WOActionResults selectBatchSize() {
		displayGroup().setNumberOfObjectsPerBatch(selectedBatchSize());
		displayGroup().setCurrentBatchIndex(1);
		return null;
	}

	public WOActionResults goToPage() {
		displayGroup().setCurrentBatchIndex(pageNumber());
		return null;
	}

	/**
	 * @return the pageNumber
	 */
	public Integer pageNumber() {
		if(pageNumber == null) {
			pageNumber = displayGroup.currentBatchIndex();
		}
		return pageNumber;
	}

	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public String page() {
		return String.valueOf(pageNumber());
	}
	
	public void setPage(String page) {
		setPageNumber(Integer.parseInt(page));
	}

	public WOActionResults displayNextBatch() {
		displayGroup().displayNextBatch();
		pageNumber = displayGroup().currentBatchIndex();
		return null;
	}
	
	public WOActionResults displayPreviousBatch() {
		displayGroup().displayPreviousBatch();
		pageNumber = displayGroup().currentBatchIndex();
		return null;
	}
}
