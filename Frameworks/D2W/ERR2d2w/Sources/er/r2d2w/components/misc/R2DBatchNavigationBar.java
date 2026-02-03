package er.r2d2w.components.misc;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXValueUtilities;

public class R2DBatchNavigationBar extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 2L;

	private WODisplayGroup displayGroup;
	private transient D2WContext d2wContext;
	private Object batchSize;
	private NSArray<Integer> availableBatchSizes;
	private Integer batchStart;
	private Integer index;

	public R2DBatchNavigationBar(WOContext context) {
		super(context);
	}

	public void reset() {
		super.reset();
		displayGroup = null;
		d2wContext = null;
		batchSize = null;
		availableBatchSizes = null;
		batchStart = null;
		index = null;
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
	
	public int batchLimit() {
		int batchLimit = ERXValueUtilities.intValue(d2wContext().valueForKey("batchLimit"));
		return batchLimit;
	}
	
	public int limitedBatchCount() {
		return Math.min(displayGroup().batchCount(), batchLimit());
	}

	/**
	 * @return the batchSize
	 */
	public Object batchSize() {
		return batchSize;
	}

	/**
	 * @param batchSize
	 *            the batchSize to set
	 */
	public void setBatchSize(Object batchSize) {
		this.batchSize = batchSize;
	}

	public void selectBatchSize() {
		int batchSize = ERXValueUtilities.intValue(batchSize());
		displayGroup().setNumberOfObjectsPerBatch(batchSize);
		displayGroup().setCurrentBatchIndex(1);
		displayGroup().clearSelection();
		NSNotificationCenter.defaultCenter().postNotification("BatchSizeChanged",
				ERXConstant.integerForInt(batchSize),
				new NSDictionary<String, Object>(d2wContext(), "d2wContext"));
	}

	public boolean currentBatchSizeSelected() {
		int batchSize = ERXValueUtilities.intValue(batchSize());
		return displayGroup().numberOfObjectsPerBatch() == batchSize;
	}

	public String batchSizeString() {
		if(ERXValueUtilities.intValue(batchSize()) == 0) {
			String all = localizer().localizedStringForKey("R2DBatchSize.all");
			return all;
		}
		return String.valueOf(batchSize());
	}

	/**
	 * @return the availableBatchSizes
	 */
	public NSArray<Integer> availableBatchSizes() {
		if(availableBatchSizes == null) {
			NSArray<Object> rawSizes = (NSArray<Object>) d2wContext().valueForKey("availableBatchSizes");
			if(rawSizes != null) {
				NSMutableArray<Integer> sizes = new NSMutableArray<Integer>();
				int allObjects = displayGroup().allObjects().count();
				for(int i = 0, count = rawSizes.count(); i < count; ++i) {
					int size = ERXValueUtilities.intValue(rawSizes.objectAtIndex(i));
					if(size < allObjects) {
						sizes.addObject(Integer.valueOf(size));
					}
				}
				availableBatchSizes = sizes.immutableClone();
			} else {
				availableBatchSizes = NSArray.emptyArray();
			}
		}
		return availableBatchSizes;
	}

	public boolean hasMultipleBatchSizes() {
		return availableBatchSizes().count() > 1;
	}

	public Integer startValue() {
		if(batchStart == null) {
			int batchCount = displayGroup().batchCount();
			int currentBatch = displayGroup().currentBatchIndex();
			int batchLimit = batchLimit();
			batchLimit = Math.max(batchLimit, 5);
			batchLimit = Math.min(batchLimit, batchCount);
			int centerBatch = batchLimit/2 + batchLimit%2;
			if(currentBatch <= centerBatch) {
				batchStart = 1;
			} else if(currentBatch > batchCount - centerBatch) {
				batchStart = batchCount - batchLimit + 1;
			} else {
				batchStart = currentBatch - centerBatch + 1;
			}
		}
		return batchStart;
	}

	/**
	 * @return the batchIndex
	 */
	public Integer batchIndex() {
		return startValue().intValue() + index().intValue();
	}

	public WOActionResults selectBatch() {
		displayGroup().setCurrentBatchIndex(batchIndex());
		return null;
	}

	public boolean isCurrentBatch() {
		return displayGroup().currentBatchIndex() == batchIndex().intValue();
	}

	/**
	 * @return the index
	 */
	public Integer index() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}
}