package er.ajax;

import java.math.BigDecimal;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Abstract super class for a navigation bar that can be used with AjaxGrid. Use
 * of this is not mandatory, it is provided as a convenience.
 * <p>
 * <h3>Example Usage</h3>
 * This class has a symbiotic relationship with AjaxGrid. When this nav bar
 * changes the data (e.g. batch size, batch displayed) it updates the grid's
 * container <b>not</b> its own container. It needs to have the grid tell it to
 * update after the grid has refreshed. This allows the AjaxGrid to make any
 * needed changes to the display group before the contents of the nav bar are
 * updated.
 * 
 * <pre>
 *  Grid: AjaxGrid {
 *      configurationData = configData;
 *      displayGroup = displayGroup;
 *      afterUpdate = &quot;ajaxGridExampleNavBarUpdate();&quot;;
 *  }
 * 
 *  NavBar: AjaxGridExampleNavBar {
 *     containerID = &quot;ajaxGridExampleNavBar&quot;;
 *     displayGroup = displayGroup;
 *     configurationData = configData;
 *  }
 * </pre>
 * 
 * <h3>Example Sub-class</h3>
 * 
 * <pre>
 *   &lt;div class=&quot;ajaxGridNavBar&quot;&gt;
 *    &lt;webobject name=&quot;NavUpdater&quot;&gt;
 *        &lt;table&gt;&lt;tr&gt;
 *          &lt;td style=&quot;text-align:left;&quot;&gt;&lt;webobject name=&quot;PrevBatch&quot;&gt;&lt;&lt;&lt;/webobject&gt;  
 *          Page &lt;b&gt;&lt;span id=&quot;currentBatch&quot;&gt;&lt;webobject name=&quot;CurrentBatchIndex&quot;/&gt;&lt;/span&gt;&lt;/b&gt; of &lt;b&gt;&lt;webobject name=&quot;BatchCount&quot;/&gt;&lt;/b&gt;  
 *          &lt;webobject name=&quot;NextBatch&quot;&gt;&gt;&gt;&lt;/webobject&gt;
 *        &lt;/td&gt;
 *        &lt;td style=&quot;text-align:center;&quot;&gt;
 *           Number of lines per page: &lt;webobject name=&quot;BatchSizes&quot;/&gt;&lt;webobject name=&quot;UpdateBatchSize&quot;/&gt;
 *        &lt;/td&gt;
 *        &lt;td style=&quot;text-align:right;&quot;&gt;
 *          Displaying &lt;b&gt;&lt;webobject name=&quot;FirstIndex&quot;/&gt;&lt;/b&gt; to &lt;b&gt;&lt;webobject name=&quot;LastIndex&quot;/&gt;&lt;/b&gt; of &lt;b&gt;&lt;webobject name=&quot;TotalCount&quot;/&gt;&lt;/b&gt; entries.
 *        &lt;/td&gt;&lt;/tr&gt;&lt;/table&gt;
 *        &lt;webobject name=&quot;BatchSlider&quot;/&gt;
 *    &lt;/webobject name=&quot;NavUpdater&quot;&gt;
 *  &lt;/div&gt;
 * </pre>
 * 
 * <p>
 * 
 * <pre>
 *  NavUpdater: AjaxUpdateContainer {
 *     id = containerID;
 *  }
 * 
 *  BatchCount: WOString {
 *      value = displayGroup.batchCount;
 *  }
 * 
 *  BatchSlider: AjaxSlider {
 *     orientation = &quot;horizontal&quot;;
 *     value = currentBatchIndex;
 *     minimum = 1;
 *     maximum = displayGroup.batchCount;
 *     onChangeServer = updateGridContainer;
 *     onSlide = &quot;function(v) { $('currentBatch').innerHTML = Math.round(v) }&quot;;
 *     onChange = &quot;function(v) { $('currentBatch').innerHTML = Math.round(v) }&quot;;
 *  }
 *  
 *  CurrentBatchIndex : WOString {
 *     value = displayGroup.currentBatchIndex;
 *  }
 * 
 *  PrevBatch : AjaxUpdateLink {
 *     action = previousBatch;
 *     updateContainerID = gridContainerID;
 *  }
 * 
 *  NextBatch : AjaxUpdateLink {
 *     action = nextBatch;
 *     updateContainerID = gridContainerID;
 *  }
 * 
 *  BatchSizes: WOPopUpButton {
 *     list      = batchSizes;
 *     item      = batchSize;
 *     selection = currentBatchSize;
 *     onChange  = updateBatchSizeOnChange;
 *  }
 * 
 *  FirstIndex: WOString {
 *    value = displayGroup.indexOfFirstDisplayedObject;
 *  }
 * 
 *  LastIndex: WOString {
 *     value = displayGroup.indexOfLastDisplayedObject;
 *  }
 * 
 *  TotalCount: WOString {
 *     value = displayGroup.allObjects.count;
 *  }
 * 
 *  UpdateBatchSize: AjaxUpdateLink {
 *    action = batchSizeUpdated;
 *    functionName = updateBatchSizeName;
 *    updateContainerID = gridContainerID;
 * }
 * </pre>
 * 
 * @binding displayGroup the same WODisplayGroup passed to AjaxGrid
 * @binding configurationData the same NSMutableDictionary passed to AjaxGrid
 * @binding containerID unique ID for the AjaxUpdateContainer in this component.
 * 
 * @author Chuck Hill
 */
public abstract class AjaxGridNavBar extends WOComponent {

	public static final String CONTAINER_ID_BINDING = "containerID";
	public static final String DISPLAY_GROUP_BINDING = "displayGroup";
	public static final String CONFIGURATION_DATA_BINDING = "configurationData";

	public AjaxGridNavBar(WOContext context) {
		super(context);
	}

	/**
	 * @return false, AjaxGridNavBar is stateless and manually synchronized
	 */
	public boolean isStateless() {
		return true;
	}

	/**
	 * Ajax action method to select the next batch.
	 */
	public void nextBatch() {
		displayGroup().displayNextBatch();
		displayGroup().setSelectedObject(null);
	}

	/**
	 * Ajax action method to select the previous batch.
	 */
	public void previousBatch() {
		displayGroup().displayPreviousBatch();
		displayGroup().setSelectedObject(null);
	}

	/**
	 * Intended to be bound to Ajax slider or selection of batch to display.
	 * 
	 * @param newValue
	 *            new batch number from AjaxSlider
	 */
	public void setCurrentBatchIndex(Number newValue) {
		// Right now the value returned is a BigDecimal. KVC changes this to an
		// int by narrowing, so that 4.9876402 becomes 4.
		// If we round this instead, the slider movement is more inutuitive,
		// especially with smaller batch sizes.
		if (newValue instanceof BigDecimal) {
			int roundedIndex = new Float(((BigDecimal) newValue).floatValue() + 0.5).intValue();
			displayGroup().setCurrentBatchIndex(roundedIndex);
		}
		else {
			displayGroup().setCurrentBatchIndex(newValue.intValue());
		}
	}

	/**
	 * @return value for AjaxSlider
	 */
	public int currentBatchIndex() {
		return displayGroup().currentBatchIndex();
	}

	/**
	 * Returns JavaScript to update the AjaxUpdateContainer identified by
	 * gridContainerID(). This is intended for use as onChangeServer binding for
	 * a AjaxSlider.
	 * 
	 * @return JavaScript calls to update the Ajax grid
	 */
	public String updateGridContainer() {
		return gridContainerID() + "Update(); ";
	}

	/**
	 * @return JavaScript for option control to pass selected batch size when
	 *         changed
	 */
	public String updateBatchSizeOnChange() {
		return updateBatchSizeName() + "('batchSize=' + this.value);";
	}

	/**
	 * @return unique name for the AjaxUpdateContainer wrapping this
	 *         AjaxGridNavBar
	 */
	public String updateBatchSizeName() {
		return containerID() + "UpdateBatchSize";
	}

	/**
	 * Ajax action method for updates to batch size. Grabs batch size from
	 * request and updates configuration.
	 */
	public void batchSizeUpdated() {
		String batchSizeString = (String) context().request().formValueForKey("batchSize");
		int batchSizeIndex = Integer.parseInt(batchSizeString);
		configurationData().setObjectForKey(batchSizes().objectAtIndex(batchSizeIndex), AjaxGrid.BATCH_SIZE);

		// Keep display group in synch to avoid display update issues
		
		displayGroup().setNumberOfObjectsPerBatch(currentBatchSize());
	}

	/**
	 * @return the list of batch sizes to show in the popup
	 */
	public abstract NSArray batchSizes();

	/**
	 * @return displayGroup().numberOfObjectsPerBatch() as a String for the
	 *         option input
	 */
	public int currentBatchSize() {
		Object batchSizeObj = configurationData().objectForKey(AjaxGrid.BATCH_SIZE);
		int batchSize;
		if (batchSizeObj instanceof String) {
			batchSize = Integer.parseInt((String)batchSizeObj);
		}
		else {
			batchSize = ((Number)batchSizeObj).intValue();
		}
		return batchSize;
	}

	/**
	 * @return UPDATE_CONTAINER_ID from configurationData(), the update
	 *         container ID from the AjaxGrid
	 */
	public String gridContainerID() {
		return (String) configurationData().objectForKey(AjaxGrid.UPDATE_CONTAINER_ID);
	}

	/**
	 * @return TABLE_ID from configurationData(), the table ID from the AjaxGrid
	 */
	public String tableID() {
		return (String) configurationData().objectForKey(AjaxGrid.TABLE_ID);
	}

	/**
	 * @return value bound to displayGroup
	 */
	public WODisplayGroup displayGroup() {
		return (WODisplayGroup) valueForBinding(DISPLAY_GROUP_BINDING);
	}

	/**
	 * @return value bound to configurationData
	 */
	public NSMutableDictionary configurationData() {
		return (NSMutableDictionary) valueForBinding(CONFIGURATION_DATA_BINDING);
	}

	/**
	 * @return value bound to containerID
	 */
	public String containerID() {
		return (String) valueForBinding(CONTAINER_ID_BINDING);
	}

}