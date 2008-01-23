package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * BatchNavigationBar that uses a direct action and a "batch" URL parameter to
 * switch to a specific batch. You have to do the batching yourself and
 * get the information from the request though. 
 * 
 * Tip for this:
 * ERXEOControlUtilities.objectsInRange is a really big help doing that!
 * 
 * @binding actionName (String) - the name of the directAction to call
 * @binding actionClass (String) - the name of the class for the directAction
 *          call
 * @binding additionalUrlParameters (NSDictionary) - parameters that get added
 *          to navigation URLs
 * @binding batchsize (Integer) - the size of the batches
 * @binding currentBatchIndex (Integer) - the index of the current page
 * @binding numberOfObjects (Integer) - the number of objects to batch
 * @binding containerCssClass (String) - the css class to use for the
 *          surrounding div container
 * @binding backString (String) - the string to use for the "back" link (HTML
 *          enabled); default = &laquo;&nbsp;back
 * @binding forwardString (String) - the string to use for the "forward" link
 *          (HTML enabled); default = back&nbsp;&raquo;
 * @binding showPageNumber (Boolean) - whether or not to show the page numbers
 *          (you might want to show only the back and forward links); default =
 *          true
 * @binding showPageString (Boolean) - whether or not to show the "Page:" string
 *          to the left of the nav (will be made more flexible); default = false
 * @binding showBatchNavigationForSinglePage (Boolean) - whether or not to show the
 * 			the batch navigation if we have only a single page; default = true
 * 
 * @author cug - Sep 20, 2007
 */
public class ERXDirectActionBatchNavigationBar extends ERXStatelessComponent {

	/**
	 * the batchnumbers to display, cached in this instance, resetted after completion
	 */
	private NSArray<NSDictionary<String, Object>> batchNumbers;

	/**
	 * The one used in the repetition
	 */
	public NSDictionary dictInRepetition;

	// see the constants for the keys at the end of this file

	// *******************************************************************
	// implementation

	/**
	 * Standard constructor
	 * 
	 * @param context
	 */
	public ERXDirectActionBatchNavigationBar(WOContext context) {
		super(context);
	}

	/**
	 * Reset the instance variables
	 */
	public void reset() {
		// reset all ivars
		this.dictInRepetition = null;
		this.batchNumbers = null;
	}

	/**
	 * Returns the number of batches, the first batch has value 1.
	 * If the size of one batch is 0, the number of products will
	 * be returned, in order to avoid {@link ArithmeticException}.
	 * If the number of products is 0, it will return 0.
	 * 
	 * @param productCount number of products
	 * @param sizeOfOneBatch size of one batch
	 * 
	 * @author edgar - Nov 23, 2007
	 * @return number of batches
	 */
	private int numberOfBatches(int productCount, int sizeOfOneBatch) {
		if (sizeOfOneBatch == 0) {
			return productCount;
		}

		// this part is needed since the calculation below would return 1
		if (productCount == 0) {
			return productCount;
		}

		int numberOfBatches = productCount / sizeOfOneBatch;
		if (productCount % sizeOfOneBatch != 0) {
			numberOfBatches++;
		}

		return numberOfBatches;
	}

	/**
	 * Returns "batchNumber" objects which are dictionaries with key-value-pairs for the String to display.  
	 * 
	 * @author cug - Sep 20, 2007
	 */
	public NSArray<NSDictionary<String, Object>> batchNumbers() {

		if (this.batchNumbers == null) {
			NSMutableArray<NSDictionary<String, Object>> tmpArray = new NSMutableArray<NSDictionary<String, Object>>();

			// get some stuff into the primitives for easy access
			int currentBatchIndex = this.currentBatchIndex().intValue();

			int batchSize = this.batchSize().intValue();
			int numberOfObjects = this.numberOfObjects().intValue();

			int tmp = currentBatchIndex % 9;
			if (tmp == 0)
				tmp = 9;

			int batchIndex = currentBatchIndex - tmp + 1;
			int maxPages = this.numberOfBatches(numberOfObjects, batchSize);

			int batchStart = batchIndex;
			int batchEnd = 0;

			// set the currentBatchIndex in the middle
			if (currentBatchIndex > 5 && maxPages > 9) {
				// TODO cug: showing 9 page numbers right now as a default, maybe make this configurable
				batchIndex = batchStart = currentBatchIndex - 4;
			}

			// fill the array with dictionaries, one dictionary for each page number to be displayed
			while (tmpArray.count() < 9 && batchIndex <= maxPages) {
				// TODO cug: showing 9 page numbers right now as a default, maybe make this configurable

				NSMutableDictionary<String, Object> entry = new NSMutableDictionary<String, Object>();
				entry.setObjectForKey(new Integer(batchIndex), "batchNumber");
				entry.setObjectForKey(new Integer(batchIndex).toString(), "batchString");
				entry.setObjectForKey(Boolean.FALSE, "disable");

				tmpArray.addObject(entry);
				batchEnd = batchIndex;

				batchIndex++;
			}

			if (batchStart >= 2) {
				// we add page number 1 at the start and some dots between 1 and the other numbers

				NSMutableDictionary<String, Object> entry = new NSMutableDictionary<String, Object>();
				entry.setObjectForKey(new Integer(1), "batchNumber");
				entry.setObjectForKey(new Integer(1).toString(), "batchString");
				entry.setObjectForKey(Boolean.FALSE, "disabled");

				tmpArray.insertObjectAtIndex(entry, 0);

				if (batchStart > 2) {
					// there are batches hidden, so we add the dots in here

					entry = new NSMutableDictionary<String, Object>();
					entry.setObjectForKey(new Integer(0), "batchNumber");
					entry.setObjectForKey("...", "batchString");
					entry.setObjectForKey(Boolean.TRUE, "disabled");

					tmpArray.insertObjectAtIndex(entry, 1);
				}
				else {
					// to remove the inconsistency, we remove the batch number 10 from the array
					tmpArray.removeObjectAtIndex(9);
				}
			}

			if (batchEnd < maxPages - 1) {
				// add dots at the end, because we have more than one batch more on the right side
				NSMutableDictionary<String, Object> entry = new NSMutableDictionary<String, Object>();

				entry = new NSMutableDictionary<String, Object>();
				entry.setObjectForKey(new Integer(0), "batchNumber");
				entry.setObjectForKey("...", "batchString");
				entry.setObjectForKey(Boolean.TRUE, "disabled");

				tmpArray.addObject(entry);

			}

			if (batchEnd < maxPages) {
				// add the last batch
				NSMutableDictionary<String, Object> entry = new NSMutableDictionary<String, Object>();

				entry.setObjectForKey(lastBatch(), "batchNumber");
				entry.setObjectForKey(lastBatch().toString(), "batchString");
				entry.setObjectForKey(Boolean.FALSE, "disabled");

				tmpArray.addObject(entry);
			}

			this.batchNumbers = tmpArray.immutableClone();
		}
		return batchNumbers;
	}

	/**
	 * Returns whether we have more than one batch and should show the navigation at all 
	 * 
	 * @author cug - Nov 20, 2007
	 */
	public boolean hasMoreThanOneBatch() {
		if (this.batchNumbers() != null && this.batchNumbers().count() > 1) {
			return true;
		}
		return false;
	}

	/**
	 * Should we show the batch navigation bar? Checks the binding "showBatchNavigationForSinglePage"
	 * 
	 * @author cug - Nov 20, 2007
	 * @return true if "showBatchNavigationForSinglePage" is true or there is more than one page
	 */
	public boolean showNavigationBar() {
		return this.hasMoreThanOneBatch() || this.showBatchNavigationForSinglePage();
	}

	/**
	 * Returns whether the currently generated item from the repetition is the selected one
	 * 
	 * @return true for the selected batch (page)
	 */
	public boolean isSelected() {
		return (this.dictInRepetition.valueForKey("batchNumber").equals(this.currentBatchIndex()));
	}

	/**
	 * Returns the number for the previous batch
	 * 
	 * @return the previous batch number
	 */
	public Integer previousBatch() {
		return new Integer(this.currentBatchIndex().intValue() - 1);
	}

	/**
	 * Returns the number for the next batch
	 * 
	 * @return the number for the next batch
	 */
	public Integer nextBatch() {
		return new Integer(this.currentBatchIndex().intValue() + 1);
	}

	/**
	 * Returns the number of the last page
	 * 
	 */
	public Integer lastBatch() {
		return new Integer(this.numberOfBatches(numberOfObjects().intValue(), batchSize()));
	}

	/**
	 * Returns true if the currently displayed batch is the last one (for hiding the arrows on the right) 
	 * 
	 * @return true for the last batch
	 */
	public boolean isLastBatch() {
		return this.currentBatchIndex().intValue() == this.numberOfBatches(numberOfObjects().intValue(), batchSize());
	}

	/**
	 * Returns true if the currently displayed batch is the first one (for hiding the arrows on the left)
	 *  
	 * @return true for the first batch
	 */
	public boolean isFirstBatch() {
		return this.currentBatchIndex().intValue() == 1;
	}

	/**
	 * Convenience method to get the localizer.
	 * 
	 */
	public ERXLocalizer localizer() {
		if (this.context().hasSession()) {
			return ERXLocalizer.currentLocalizer();
		}
		else {
			return ERXLocalizer.localizerForLanguages(this.context().request().browserLanguages());
		}
	}

	// *******************************************************************
	// bindings

	/**
	 * Returns the name of the direct action to call
	 * 
	 * @return a direct action name
	 */
	public String actionName() {
		return this.stringValueForBinding(ACTION_NAME_KEY);
	}

	/**
	 * Sets the name of the direct action to call
	 * 
	 * @param name - a direct action name
	 */
	public void setActionName(String name) {
		this.setValueForBinding(name, ACTION_NAME_KEY);
	}

	/**
	 * Returns the name of the direct action class, if empty the default class is used (DirectAction)
	 *  
	 * @return a name for a subclass of WODirectAction 
	 */
	public String actionClass() {
		if (this.stringValueForBinding(ACTION_CLASS_KEY) != null) {
			return this.stringValueForBinding(ACTION_CLASS_KEY);
		}
		else
			return "DirectAction";
	}

	/**
	 * Sets the name of the actionClass to use for the direct action call  
	 * 
	 * @param className - name for the directAction class
	 */
	public void setActionClass(String className) {
		this.setValueForBinding(className, ACTION_CLASS_KEY);
	}

	/**
	 * The current batch size
	 * 
	 * @return the batch size 
	 */
	public Integer batchSize() {
		return new Integer(this.intValueForBinding(BATCH_SIZE_KEY, defaultBatchSize));
	}

	/**
	 * Set the size of the batches to create the pager
	 *  
	 * @param size
	 */
	public void setBatchSize(Integer size) {
		this.setValueForBinding(size, BATCH_SIZE_KEY);
	}

	/**
	 * The total number of objects for which we create the batches
	 * 
	 * @return total number of objects
	 */
	public Integer numberOfObjects() {
		return new Integer(this.intValueForBinding(NUMBER_OF_OBJECTS_KEY, 0));
	}

	/**
	 * Set total number of objects for which we create the batches
	 * 
	 * @param n
	 */
	public void setNumberOfObjects(Integer n) {
		this.takeValueForKey(n, NUMBER_OF_OBJECTS_KEY);
	}

	/**
	 * The current batch index
	 * 
	 * @return the current batch index
	 */
	public Integer currentBatchIndex() {
		return new Integer(this.intValueForBinding(CURRENT_BATCH_INDEX_KEY, 0));
	}

	/**
	 * Set the current batch index
	 * 
	 * @param index - the index to set
	 */
	public void setCurrentBatchIndex(Integer index) {
		this.setValueForBinding(index, CURRENT_BATCH_INDEX_KEY);
	}

	/**
	 * The parameters to add to each link
	 * 
	 * @return dict of parameters
	 */
	@SuppressWarnings("unchecked")
	public NSDictionary<String, Object> additionalUrlParameters() {
		return (NSDictionary<String, Object>) this.valueForBinding(ADDITIONAL_URL_PARAMETERS_KEY);
	}

	/**
	 * Set the parameters to add to each link
	 * 
	 * @param dict
	 */
	public void setAdditionalUrlParameters(NSDictionary<String, Object> dict) {
		this.setValueForBinding(dict, ADDITIONAL_URL_PARAMETERS_KEY);
	}

	/**
	 * The css class to use for the surrounding div
	 * 
	 * @return css class name
	 */
	public String containerCssClass() {
		return this.stringValueForBinding(CONTAINER_CSS_CLASS_KEY, "ERXDABatchNav");
	}

	/**
	 * Set the class name for the surrounding div
	 * 
	 * @param cssClass
	 */
	public void setContainerCssClass(String cssClass) {
		this.takeValueForKey(cssClass, CONTAINER_CSS_CLASS_KEY);
	}

	/**
	 * Returns whether or not to show the string for "Page:"
	 * 
	 * @return the value for the binding
	 */
	public Boolean showPageString() {
		return new Boolean(this.booleanValueForBinding(SHOW_PAGE_STRING_KEY, false));
	}

	/**
	 * Set whether to show the string "Page:" 
	 * 
	 * @param flag
	 */
	public void setShowPageString(Boolean flag) {
		this.setValueForBinding(flag, SHOW_PAGE_STRING_KEY);
	}

	/**
	 * The string for the forward link
	 * 
	 */
	public String forwardString() {
		return this.stringValueForBinding(FORWARD_STRING, "forward&nbsp;&raquo;");
	}

	/**
	 * Set the string for the "forward" link
	 * 
	 * @param s
	 */
	public void setForwardString(String s) {
		this.setValueForBinding(s, FORWARD_STRING);
	}

	/**
	 * the string for the "back" link
	 */
	public String backString() {
		return this.stringValueForBinding(BACK_STRING, "&laquo;&nbsp;back");
	}

	/**
	 * Set the string for the "back" link
	 * 
	 * @param s
	 */
	public void setBackString(String s) {
		this.setValueForBinding(s, BACK_STRING);
	}

	/**
	 * Returns whether to show the page numbers
	 * 
	 * @return true for showing the numbers, defaults to true
	 */
	public Boolean showPageNumbers() {
		return new Boolean(this.booleanValueForBinding(SHOW_PAGE_NUMBERS, true));
	}

	/**
	 * Set whether to show the page numbers
	 * 
	 * @param flag
	 */
	public void setShowPageNumbers(Boolean flag) {
		this.setValueForBinding(flag, SHOW_PAGE_NUMBERS);
	}

	/**
	 * Return the value for the showBatchNavigationForSinglePage binding
	 * 
	 * @author cug - Nov 20, 2007
	 * @return value for the showBatchNavigationForSinglePage binding
	 */
	public Boolean showBatchNavigationForSinglePage() {
		return this.booleanValueForBinding(SHOW_BATCH_NAVIGATION_FOR_SINGLE_PAGE, true) ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Set the binding value for showBatchNavigationForSinglePage
	 *  
	 * @author cug - Nov 20, 2007
	 * @param flag
	 */
	public void setShowBatchNavigationForSinglePage(Boolean flag) {
		this.setValueForBinding(flag, SHOW_BATCH_NAVIGATION_FOR_SINGLE_PAGE);
	}

	// *************************************************************************
	// some constants

	/**
	 * key for showBatchNavigationForSinglePage binding
	 */
	private static final String SHOW_BATCH_NAVIGATION_FOR_SINGLE_PAGE = "showBatchNavigationForSinglePage";

	/**
	 * Key for the actionName binding
	 */
	private static final String ACTION_NAME_KEY = "actionName";

	/**
	 * key for the actionClass binding
	 */
	private static final String ACTION_CLASS_KEY = "actionClass";

	/**
	 * key for the batchSize binding
	 */
	private static final String BATCH_SIZE_KEY = "batchSize";

	/**
	 * key for the numberOfObjects binding
	 */
	private static final String NUMBER_OF_OBJECTS_KEY = "numberOfObjects";

	/**
	 * key for the currentBatchIndex binding
	 */
	private static final String CURRENT_BATCH_INDEX_KEY = "currentBatchIndex";

	/**
	 * the default batchSize, defaults to 20
	 */
	private static int defaultBatchSize = 20;

	/**
	 * the key for the additionalUrlParameters binding
	 */
	private static final String ADDITIONAL_URL_PARAMETERS_KEY = "additionalUrlParameters";

	/**
	 * the key for the surrounding div container class name
	 */
	private static final String CONTAINER_CSS_CLASS_KEY = "containerCssClass";

	/**
	 * the key for the showPageString binding
	 */
	private static final String SHOW_PAGE_STRING_KEY = "showPageString";

	/**
	 * the key for the "backString" binding
	 */
	private static final String BACK_STRING = "backString";

	/**
	 * the key for the "forwardString" binding
	 */
	private static final String FORWARD_STRING = "forwardString";

	/**
	 * the key for the "showPageNumbers" bindings
	 */
	private static final String SHOW_PAGE_NUMBERS = "showPageNumbers";

}
