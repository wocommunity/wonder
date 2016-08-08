package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.ajax.AjaxUpdateContainer;
import er.directtoweb.components.ERDCustomComponent;
import er.extensions.batching.ERXBatchNavigationBar;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * A modern batch size controll that uses an AjaxInplaceEditor to edit
 * the batch size.
 * 
 * @d2wKey itemString
 * @d2wKey showString
 * @d2wKey separatorString
 * @d2wKey updateContainerID
 * @d2wKey localizer
 * 
 * @author davidleber
 *
 */
public class ERMDBatchSizeControl extends ERDCustomComponent {
	
	public static interface Keys {
		 public static final String itemString = "itemString";
		 public static final String showString = "showString";
		 public static final String separatorString = "separatorString";
		 public static final String updateContainerID = "updateContainerID";
		 public static final String localizer = "localizer";
	}
	
	private String _itemsString;
	private String _showString;
	private transient ERXLocalizer _localizer;
	private String _updateContainerID;
	private String _separatorString;
	private String _batchSizeFieldID;
	
	public ERMDBatchSizeControl(WOContext context) {
        super(context);
    }
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * The string displayed for 100 [item]s : Show 10
	 * <p>
	 * Default is "item"
	 */
	public String itemsString() {
		if (_itemsString == null) {
			String key = stringValueForBinding(Keys.itemString, "item");
			_itemsString = localizer().localizedStringForKey("ERMBatchSizeControl." + key);
			if (_itemsString == null ) {
				_itemsString = localizer().localizedStringForKeyWithDefault(key);
			}
		}
		return _itemsString;
	}

	/**
	 * The string displayed for: 100 items : [Show] 10
	 * <p>
	 * Defaults to "Show"
	 */
	public String showString() {
		if (_showString == null) {
			String key = stringValueForBinding(Keys.showString, "Show");
			_showString = localizer().localizedStringForKey("ERMBatchSizeControl." + key);
			if (_showString == null) {
				_showString = localizer().localizedStringForKeyWithDefault(key);
			}
		}
		return _showString;
	}
	
	/**
	 * The string displayed for: 100 items [:] Show 10
	 * <p>
	 * Defaults to ":"
	 */
	public String separatorString() {
		if (_separatorString == null) {
			String key = stringValueForBinding(Keys.separatorString, ":");
			_separatorString = localizer().localizedStringForKey("ERMBatchSizeControl." + key);
			if (_separatorString == null) {
				_separatorString = localizer().localizedStringForKeyWithDefault(key);
			}
		}
		return _separatorString;
	}
	
	/**
	 * Localizer. 
	 * <p>
	 * Defaults to ERXLocalizer.currentLocalizer()
	 */
    @Override
    public ERXLocalizer localizer() {
		if (_localizer == null) {
			_localizer = (ERXLocalizer)objectValueForBinding(Keys.localizer, ERXLocalizer.currentLocalizer());
		}
		return _localizer;
	}
	
    /**
     * Update container id for the displayGroup's list.
     * <p>
     * Defaults to the first parent update container id.
     */
	public String updateContainerID() {
        if (_updateContainerID == null) {
            _updateContainerID = (String) valueForBinding(Keys.updateContainerID);
            if (_updateContainerID == null) {
                _updateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
            }
        }
        return _updateContainerID;
	}

	/**
	 * Returns a unique id for this batch size control
	 */
	public String batchSizeFieldID() {
        if (_batchSizeFieldID == null) {
            _batchSizeFieldID = updateContainerID() + "_BSIF";
        }
        return _batchSizeFieldID;
	}
	
	public void setBatchSizeFieldID(String fieldID) {
		_batchSizeFieldID = fieldID;
	}

	/**
	 * Returns the js function to update the updateContainerID
	 */
	public String updateFunction() {
		return "function(e) { " + updateContainerID() + "Update() }";
	}
	
	public WODisplayGroup displayGroup() {
		return (WODisplayGroup)valueForBinding("displayGroup");
	}
	
	public int numberOfObjectsPerBatch() {
		return displayGroup().numberOfObjectsPerBatch();
	}
	
	public void setNumberOfObjectsPerBatch(Integer number) {
		displayGroup().setNumberOfObjectsPerBatch(number);
		NSNotificationCenter.defaultCenter().postNotification(
				ERXBatchNavigationBar.BatchSizeChanged,
				ERXConstant.integerForInt(number.intValue()),
                new NSDictionary(d2wContext(),"d2wContext") );
	}
}
