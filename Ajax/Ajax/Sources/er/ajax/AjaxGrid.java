package er.ajax;

import java.text.Format;
import java.text.ParseException;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


/**
 * Ajax powered grid based on HTML Table that provides drag and drop column
 * re-ordering, complex sorting, and the ability to embed components in cells.
 * Class names and spans are used extensively to allow the display to be heavily
 * customized with CSS.
 * <p>
 * The data is taken from a WODisplayGroup. Use
 * er.extensions.ERXBatchingDisplayGroup to provide high performance access to
 * large data sets.
 * <p>
 * Navigation between batches is not implemented as implementing in it another
 * component bound to the display group will allow for a more flexible UI.
 * <p>
 * <h3>Configuration</h3>
 * Configuration is provided by an NSMutableDictionary and NSMutableArray data
 * structure. This reduces the number of bindings, eases keeping related lists
 * of information in synch, and provides an easy path for serializing a user's
 * customizations to configuration for persistent storage. Described as a plist,
 * the format of the configuration information is:
 * 
 * <pre>
 *   {
 *        tableID = &quot;exampleAjaxGrid&quot;;                // Goes on main table tag
 *        updateContainerID = &quot;ajaxGridContainer&quot;;    // Goes on div wrapping table, used with AjaxUpdate* components
 *        updateFrequency = 60;                                 // Optional frequency of automatic updates of the grid contents
 *                                                              // This function uses the Ajax.PeriodicalUpdater which does an
 *                                                              // update when it is first created, rather than waiting for the 
 *                                                              // frequency time before making the first request
 *        cssClass = &quot;ajaxGrid&quot;;                      // CSS class attribute on main table tag, optional
 *        cssStyle = &quot;border: thin solid #000000;&quot;;   // CSS style attribute on main table tag, optional
 *        evenRowCSSClass = &quot;yellowBackground&quot;;       // CSS class attribute on even rows, optional
 *        oddRowCSSClass = &quot;greyBackground&quot;;          // CSS class attribute on odd rows, optional
 *        evenRowCSSStyle = &quot;background:lightyellow;&quot; // CSS style attribute on even rows, optional
 *        oddRowCSSStyle = &quot;background:lightgrey;&quot;;   // CSS style attribute on odd rows, optional
 *        selectedRowCSSClass = &quot;yellowBackground&quot;;   // Secondary CSS class attribute on selected rows, optional
 *        unselectedRowCSSClass = &quot;greyBackground&quot;;   // Secondary CSS class attribute on unselected rows, optional
 *        selectedRowCSSStyle = &quot;background:lightyellow;&quot;; // Secondary CSS style attribute on selected rows, optional
 *        unselectedRowCSSStyle = &quot;background:lightgrey;&quot;;// Secondary CSS style attribute on unselected rows, optional
 *        canReorder = true;                                    // Enables (or disables) drag and drop reordering of columns
 *        batchSize = 10;                                       // Controls size of batch in display group, use zero for no batching
 *  
 *        columns = (                                           // List of columns to display, controls the initial display order
 *            {
 *                title = &quot;Name&quot;;                     // Title for this column in the table header row
 *                keyPath = &quot;fullName&quot;;               // Key path into row returning the data to display in this colunmn
 *            },                                                // keyPath is optional if component is specified
 *            {
 *                title = &quot;Department&quot;;
 *                keyPath = &quot;department.name&quot;;
 *            },
 *            {
 *                title = &quot;Hire Date&quot;;
 *                keyPath = &quot;hireDate&quot;;
 *                formatterClass = &quot;com.webobjects.foundation.NSTimestampFormatter&quot;; // Class of formatter to apply to values in this column
 *                formatPattern = &quot;%m/%d/%y&quot;;         // Optional pattern if needed by formatter
 *            },
 *            {
 *                title = &quot;Salary&quot;;
 *                keyPath = &quot;salary&quot;;
 *                formatterClass = &quot;com.webobjects.foundation.NSNumberFormatter&quot;;
 *                formatPattern = &quot;$###,##0.00&quot;;
 *                cssClass = &quot;alignRight&quot;;            // CSS class attribute td tag in this column, optional
 *            },
 *            {
 *                title = &quot;Vacation Days&quot;;
 *                keyPath = &quot;daysVacation&quot;;
 *                formatterClass = &quot;com.webobjects.foundation.NSNumberFormatter&quot;;
 *                cssStyle = &quot;text-align: right;&quot;;    // CSS style attribute td tag in this column, useful for text-align and width, optional
 *            },
 *            {
 *                title =  &quot;Actions&quot;;
 *                keyPath = &quot;&quot;;                       // Missing or empty keypath results in the current object itself being passed to component
 *                component = &quot;EmployeeActions&quot;;      // Name of WOComponent to be displayed in this column.  Gets passed two bindings: value (Object), 
 *                                                              // and grid (AjaxGrid) so that any other needed data can be accessed
 *                cssClass = &quot;alignCenter&quot;;
 *            }
 *        );
 *        sortOrder = (
 *            {
 *                keyPath = &quot;department.name&quot;;
 *                direction = &quot;ascending&quot;;
 *            },
 *            {
 *                keyPath = &quot;salary&quot;;
 *                direction = &quot;descending&quot;;
 *            },
 *            {
 *                keyPath = &quot;daysVacation&quot;;
 *                direction = &quot;ascending&quot;;
 *            }
 *      );
 *  
 *  }
 * </pre>
 * 
 * <h4>Initializing Configuration From a File</h4>
 * You can get the configuration information from a plist file with code like
 * this:
 * 
 * <pre>
 * public NSMutableDictionary configData() {
 * 	if (configData == null) {
 * 		// Get data from user preferences here if available, otherwise load the defaults
 * 		configData = mySession().user().preferencesFor(&quot;AjaxGridExample&quot;);
 * 		if (configData == null) {
 * 			NSData data = new NSData(application().resourceManager().bytesForResourceNamed(&quot;AjaxGridExampleConfiguration.plist&quot;, null, NSArray.EmptyArray));
 * 			configData = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, &quot;UTF-8&quot;));
 * 		}
 * 	}
 * 
 * 	return configData;
 * }
 * </pre>
 * 
 * <h3>Updating the Grid From a Different Component</h3>
 * When the grid contents are updated the AjaxUpdateContainer needs to be
 * updated. The grid configuration <code>updateContainerID</code> gives the ID
 * for this. This can be used as the <code>updateContainerID</code> in a
 * <code>AjaxUpdateLink</code> or
 * <code>&lt;updateContainerID&gt;Update()</code> can be called directly. This
 * results in a call to <code>ajaxGrid_init($(&lt;tableID&gt;));</code> to be
 * re-enable drag and drop on the table.
 * <p>
 * Here is an example. In this example, <code>updateContainerID</code> is used
 * to update the grid, and then the <code>NavBar</code> container is updated
 * manually from <code>onComplete</code>. The reason for this is that the
 * grid needs to update first so that the correct values are available for
 * updating the <code>NavBar</code>.
 * <p>
 * Relevant Configuration:
 * 
 * <pre>
 *   {
 *      tableID = &quot;exampleAjaxGrid&quot;;
 *      updateContainerID = &quot;ajaxGridContainer&quot;;
 *      . . .
 * </pre>
 * 
 * WOD Bindings:
 * 
 * <pre>
 *   NavUpdater: AjaxUpdateContainer {
 *       id = &quot;NavBar&quot;;
 *   }
 *   
 *   NextBatch : AjaxUpdateLink {
 *       action = nextBatch;
 *       updateContainerID = &quot;ajaxGridContainer&quot;;
 *       onComplete = &quot;function(request) { NavBarUpdate(); }&quot;;
 *   }
 * </pre>
 * 
 * <h3>CSS Classes Used by AjaxGrid</h3>
 * In addition to the classes defined in the grid configuration, AjaxGrid uses
 * some set class names: <table>
 * <tr>
 * <th>Class Name</th>
 * <th>Used For</th>
 * </tr>
 * <tr>
 * <td>ajaxGridRemoveSorting</td>
 * <td>The th of cell containing link to remove all sorting</td>
 * </tr>
 * <tr>
 * <td>ajaxGridColumnTitle</td>
 * <td>The th of cells containing column titles</td>
 * </tr>
 * <tr>
 * <td>ajaxGridSortAscending</td>
 * <td>The span that wraps index and directoin indicator of columns sorted in
 * ascending order</td>
 * </tr>
 * <tr>
 * <td>ajaxGridSortDescending</td>
 * <td>The span that wraps index and directoin indicator of columns sorted in
 * descending order</td>
 * </tr>
 * <tr>
 * <td>ajaxGridSelectRow</td>
 * <td>The td of cells containing the row selection link</td>
 * </tr>
 * </table
 * <h3>Advanced Styling of the Grid</h3>
 * The grid contains several places were there are nested anonymous
 * <code>span</code> tags wrapping default text content. These are there so
 * that the span wrapping the default content can be set to
 * <code>display: none</code> and the content of the outer div given in CSS.
 * For this HTML:
 * 
 * <pre>
 *   &lt;th class=&quot;ajaxGridRemoveSorting&quot;&gt;&lt;span&gt;X&lt;/span&gt;&lt;em&gt;&amp;nbsp;&lt;/em&gt;&lt;/th&gt;
 * </pre>
 * 
 * The default <b>X</b> can replaced with an image with this CSS:
 * 
 * <pre>
 *   th.ajaxGridRemoveSorting a span {display: none;}
 *   th.ajaxGridRemoveSorting a  em {	
 *      background-image: url(http://vancouver.global-village.net/WebObjects/Frameworks/JavaDirectToWeb.framework/WebServerResources/trashcan-btn.gif);
 *      background-repeat:no-repeat;
 *      padding-right: 12px;
 *      padding-bottom: 5px;
 *   }
 * </pre>
 * 
 * You can also use <code>content</code> to replace the span content with text
 * if your browser (not IE) supports it.
 * 
 * <h3>Updating the Configuration</h3>
 * If you update the configuration after the grid has been displayed, some items will not update as the information is cached.  Add a value
 * under the key AjaxGrid.CONFIGURATION_UPDATED to notify the grid to discard the cached information.  The grid will remove the value under
 * this key after it has cleared the cache.
 * <h3>To Do</h3>
 * <ul>
 * <li>wrap JavaScript in function literal for namespace protection</li>
 * <li>drag ghost of column instead of changing column background color when
 * dragging. See <a
 * href="http://www.webreference.com/programming/javascript/mk/column2/index.html">reference</a>.</li>
 * <li>support multiple grids on a single page</li>
 * <li>make stateless</li>
 * <li>allow auto configuration from .woo file</li>
 * <li>allow sorting to be enabled / disabled</li>
 * </ul>
 * 
 * @binding displayGroup required, WODisplayGroup acting as source and batching
 *          engine for the data to be displayed
 * @binding configurationData required, NSMutableDictionary used to configure
 *          grid, see documentation for details
 * @binding selectedObjects optional, NSMutableArray list of rows that the user
 *          has selected from the grid
 * @binding willUpdate optional, Ajax action method called when the
 *          AjaxUpdateContainer is being updated, but before it renders its
 *          content
 * @binding afterUpdate optional, JavaScript to execute client-side after the
 *          grid has updated
 * 
 * @author chill
 */
public class AjaxGrid extends WOComponent {

	private WODisplayGroup displayGroup; // binding
	private NSMutableArray selectedObjects; // binding

	private NSMutableDictionary columnsByKeypath; // optimization
	private NSMutableDictionary sortOrdersByKeypath; // optimization
	private NSMutableDictionary formattersByKeypath; // optimization

	private NSKeyValueCodingAdditions row; // local binding
	private NSDictionary currentColumn; // local binding
	public int rowIndex; // local binding

	public static final String KEY_PATH = "keyPath";
	public static final String SORT_DIRECTION = "direction";
	public static final String SORT_ASCENDING = "ascending";
	public static final String SORT_DESCENDING = "descending";
	public static final String SORT_ORDER = "sortOrder";
	public static final String COLUMNS = "columns";
	public static final String BATCH_SIZE = "batchSize";
	public static final String UPDATE_CONTAINER_ID = "updateContainerID";
	public static final String TABLE_ID = "tableID";
	public static final String CAN_REORDER = "canReorder";
	public static final String SOURCE_COLUMN_FORM_VALUE = "sourceColumn";
	public static final String DESTINATION_COLUMN_FORM_VALUE = "destinationColumn";
	public static final String FORMATTER_CLASS = "formatterClass";
	public static final String FORMAT_PATTERN = "formatPattern";
	public static final String EVEN_ROW_CSS_CLASS = "evenRowCSSClass";
	public static final String ODD_ROW_CSS_CLASS = "oddRowCSSClass";
	public static final String EVEN_ROW_CSS_STYLE = "evenRowCSSStyle";
	public static final String ODD_ROW_CSS_STYLE = "oddRowCSSStyle";
	public static final String SELECTED_ROW_CSS_CLASS = "selectedRowCSSClass";
	public static final String UNSELECTED_ROW_CSS_CLASS = "unselectedRowCSSClass";
	public static final String SELECTED_ROW_CSS_STYLE = "selectedRowCSSStyle";
	public static final String UNSELECTED_ROW_CSS_STYLE = "unselectedRowCSSStyle";
	public static final String CONFIGURATION_UPDATED = "configurationUpdated";
	public static final String COMPONENT_NAME = "component";
	public static final String UPDATE_FREQUENCY = "updateFrequency";

	public static final String DISPLAY_GROUP_BINDING = "displayGroup";
	public static final String CONFIGURATION_DATA_BINDING = "configurationData";
	public static final String SELECTED_OBJECTS_BINDING = "selectedObjects";
	public static final String WILL_UPDATE_BINDING = "willUpdate";
	public static final String AFTER_UPDATE_BINDING = "afterUpdate";


	public AjaxGrid(WOContext context) {
		super(context);
	}

	/**
	 * @return false, AjaxGrid is manually synchronized
	 */
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/**
	 * Adds movecolumns.js to the header.
	 */
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		AjaxUtils.addScriptResourceInHead(context, response, "movecolumns.js");
	}

	/**
	 * Ajax action method for when columns are dragged and dropped. Updates
	 * configurationData().
	 */
	public void columnOrderUpdated() {
		// The Java script should ensure that these are valid
		int sourceIndex = Integer.parseInt((String) context().request().formValueForKey(SOURCE_COLUMN_FORM_VALUE)) - 1;
		int destinationIndex = Integer.parseInt((String) context().request().formValueForKey(DESTINATION_COLUMN_FORM_VALUE)) - 1;

		Object sourceColumn = columns().objectAtIndex(sourceIndex);
		columns().removeObjectAtIndex(sourceIndex);
		columns().insertObjectAtIndex(sourceColumn, destinationIndex);
	}

	/**
	 * Ajax action method for when column titles are clicked to change sorting.
	 * Updates configurationData() and displayGroup().
	 */
	public void sortOrderUpdated() {
		String keyPath = (String) currentColumn().objectForKey(KEY_PATH);

		// Columns without a key path can't be sorted
		if (keyPath == null) {
			return;
		}

		NSMutableDictionary sortOrder = currentColumnSortOrder();
		if (sortOrder == null) {
			NSMutableDictionary newSortOrder = new NSMutableDictionary(2);
			newSortOrder.setObjectForKey(keyPath, KEY_PATH);
			newSortOrder.setObjectForKey(SORT_ASCENDING, SORT_DIRECTION);

			sortOrders().addObject(newSortOrder);
			clearCachedConfiguration();
		}
		else {
			String direction = (String) sortOrder.objectForKey(SORT_DIRECTION);
			sortOrder.setObjectForKey(SORT_ASCENDING.equals(direction) ? SORT_DESCENDING : SORT_ASCENDING, SORT_DIRECTION);
		}

		updateDisplayGroupSort();
	}

	/**
	 * Ajax action method for when control clicked to remove all sorting.
	 * Updates configurationData() and displayGroup().
	 */
	public void removeSorting() {
		configurationData().setObjectForKey(new NSMutableArray(), SORT_ORDER);
		clearCachedConfiguration();
		updateDisplayGroupSort();
	}

	/**
	 * This method is called when the AjaxUpdateContainer is about to update. It
	 * invokes the willUpdate action binding, if set, and discards the result.
	 * 
	 */
	public void containerUpdated() {
		if (hasBinding(WILL_UPDATE_BINDING)) {
			valueForBinding(WILL_UPDATE_BINDING);
		}

		if (batchSize() != displayGroup().numberOfObjectsPerBatch()) {
			displayGroup().setNumberOfObjectsPerBatch(batchSize());
		}
	}

	/**
	 * Script that goes on this page to initialize drag and drop on the grid
	 * when the page loads / re-loads
	 * 
	 * @return JavaScript to initialize drag and drop on the grid
	 */
	public String initScript() {
		return canReorder() ? "<script type=\"text/javascript\">AjaxGrid.ajaxGrid_init($(\"" + tableID() + "\"));</script>" : null;
	}

	/**
	 * Binding value for onRefreshComplete function of AjaxUpdate container.
	 * Returns the value from the AFTER_UPDATE_BINDING followed by
	 * enableDragAndDrop().
	 * 
	 * @return value for AFTER_UPDATE_BINDING concatenated with
	 *         enableDragAndDrop()
	 */
	public String afterUpdate() {
		String afterUpdate = hasBinding(AFTER_UPDATE_BINDING) ? (String) valueForBinding(AFTER_UPDATE_BINDING) : "";
		afterUpdate += enableDragAndDrop();
		return afterUpdate.length() > 0 ? afterUpdate : null;
	}

	/**
	 * Returns Javacript to (re)initialize drag and drop on the grid.
	 * 
	 * @return ajaxGrid_init(TABLE);
	 */
	public String enableDragAndDrop() {
		return canReorder() ? "AjaxGrid.ajaxGrid_init($('" + tableID() + "'));" : "";
	}

	/**
	 * @return the configurationData
	 */
	public NSMutableDictionary configurationData() {
		NSMutableDictionary configurationData = (NSMutableDictionary) valueForBinding(CONFIGURATION_DATA_BINDING);
		if (configurationData.objectForKey(CONFIGURATION_UPDATED) != null)
		{
			clearCachedConfiguration();
			configurationData.removeObjectForKey(CONFIGURATION_UPDATED);
		}

		return configurationData;
	}

	/**
	 * Clears local cache of configuration data so that fresh data will be
	 * cached.
	 * 
	 */
	protected void clearCachedConfiguration() {
		columnsByKeypath = null;
		sortOrdersByKeypath = null;
		formattersByKeypath = null;
	}

	/**
	 * Returns CAN_REORDER value from configurationData()
	 * 
	 * @return <code>true</code> if column re-ordering is enabled
	 */
	public boolean canReorder() {
		return Boolean.valueOf((String) configurationData().valueForKey(CAN_REORDER)).booleanValue();
	}

	/**
	 * Returns TABLE_ID value from configurationData()
	 * 
	 * @return HTML ID for <table> implementing the grid
	 */
	public String tableID() {
		return (String) configurationData().valueForKey(TABLE_ID);
	}

	/**
	 * Returns COLUMNS value from configurationData()
	 * 
	 * @return list of configuration for the columns to display in the grid
	 */
	protected NSMutableArray columns() {
		return (NSMutableArray) configurationData().valueForKey(COLUMNS);
	}

	/**
	 * Returns SORT_ORDER value from configurationData()
	 * 
	 * @return list of sort orders controlling display of data in the grid
	 */
	protected NSMutableArray sortOrders() {
		return (NSMutableArray) configurationData().valueForKey(SORT_ORDER);
	}

	/**
	 * Returns BATCH_SIZE value from configurationData()
	 * 
	 * @return batch size for the display grid
	 */
	protected int batchSize() {
		return Integer.parseInt((String) configurationData().valueForKey(BATCH_SIZE));
	}

	/**
	 * Returns EVEN_ROW_CSS_CLASS or ODD_ROW_CSS_CLASS, depending on rowIndex,
	 * value from configurationData() followed by SELECTED_ROW_CSS_CLASS or
	 * UNSELECTED_ROW_CSS_CLASS, depending on isRowSelected(), value from
	 * configurationData()
	 * 
	 * @return CSS class for this row
	 */
	public String rowClass() {
		boolean isEven = rowIndex % 2 == 0;
		String userClass = (String) configurationData().valueForKey(isEven ? EVEN_ROW_CSS_CLASS : ODD_ROW_CSS_CLASS);
		String selectionClass = (String) configurationData().valueForKey(isRowSelected() ? SELECTED_ROW_CSS_CLASS : UNSELECTED_ROW_CSS_CLASS);

		if (userClass == null) {
			return selectionClass;
		}

		if (selectionClass == null) {
			return userClass;
		}

		return userClass + " " + selectionClass;
	}

	/**
	 * Returns EVEN_ROW_CSS_STYLE or ODD_ROW_CSS_STYLE, depending on rowIndex,
	 * value from configurationData() folowed by SELECTED_ROW_CSS_STYLE or
	 * UNSELECTED_ROW_CSS_STYLE, depending on isRowSelected(), value from
	 * configurationData()
	 * 
	 * @return CSS class for this row
	 */
	public String rowStyle() {
		boolean isEven = rowIndex % 2 == 0;
		String userStyle = (String) configurationData().valueForKey(isEven ? EVEN_ROW_CSS_STYLE : ODD_ROW_CSS_STYLE);
		String selectionStyle = (String) configurationData().valueForKey(isRowSelected() ? SELECTED_ROW_CSS_STYLE : UNSELECTED_ROW_CSS_STYLE);

		if (userStyle == null) {
			return selectionStyle;
		}

		if (selectionStyle == null) {
			return userStyle;
		}

		return userStyle + " " + selectionStyle;
	}

	/**
	 * @return dictionary of columns() keyed on KEY_PATH of column
	 */
	public NSMutableDictionary columnsByKeypath() {
		if (columnsByKeypath == null) {
			NSArray columns = columns();
			columnsByKeypath = new NSMutableDictionary(columns.count());
			for (int i = 0; i < columns.count(); i++) {
				columnsByKeypath.setObjectForKey(columns.objectAtIndex(i), ((NSKeyValueCoding) columns.objectAtIndex(i)).valueForKey(KEY_PATH));
			}
		}

		return columnsByKeypath;
	}

	/**
	 * @return dictionary of sortOrders() keyed on KEY_PATH of column
	 */
	public NSMutableDictionary sortOrdersByKeypath() {
		if (sortOrdersByKeypath == null) {
			NSArray sortOrders = sortOrders();
			sortOrdersByKeypath = new NSMutableDictionary(sortOrders.count());
			for (int i = 0; i < sortOrders.count(); i++) {
				sortOrdersByKeypath.setObjectForKey(sortOrders.objectAtIndex(i), ((NSKeyValueCoding) sortOrders.objectAtIndex(i)).valueForKey(KEY_PATH));
			}
		}
		return sortOrdersByKeypath;
	}

	/**
	 * @return dictionary of formatters for columns() keyed on KEY_PATH of
	 *         column
	 */
	public NSMutableDictionary formattersByKeypath() {
		if (formattersByKeypath == null) {
			NSArray columns = columns();
			formattersByKeypath = new NSMutableDictionary(columns.count());
			for (int i = 0; i < columns.count(); i++) {
				NSDictionary column = (NSDictionary) columns.objectAtIndex(i);
				String className = (String) column.valueForKey(FORMATTER_CLASS);
				if (className != null) {
					try {
						Format formatter = (Format) Class.forName(className).newInstance();
						String pattern = (String) column.valueForKey(FORMAT_PATTERN);
						if (pattern != null) {
							NSKeyValueCoding.DefaultImplementation.takeValueForKey(formatter, pattern, "pattern");
						}
						formattersByKeypath.setObjectForKey(formatter, column.valueForKey(KEY_PATH));
					}
					catch (Exception e) {
						throw NSForwardException._runtimeExceptionForThrowable(e);
					}
				}
			}
		}

		return formattersByKeypath;
	}

	/**
	 * Updates sort orders on the display group
	 */
	protected void updateDisplayGroupSort() {
		NSArray sort = sortOrders();
		NSMutableArray sortOrders = new NSMutableArray(sort.count());
		for (int i = 0; i < sort.count(); i++) {
			NSDictionary column = (NSDictionary) sort.objectAtIndex(i);
			sortOrders.addObject(new EOSortOrdering((String) column.objectForKey(KEY_PATH), (SORT_ASCENDING.equals(column.objectForKey(SORT_DIRECTION)) ? EOSortOrdering.CompareCaseInsensitiveAscending : EOSortOrdering.CompareCaseInsensitiveDescending)));
		}

		displayGroup().setSortOrderings(sortOrders);
		displayGroup().updateDisplayedObjects();
	}

	/**
	 * @return the displayGroup
	 */
	public WODisplayGroup displayGroup() {

		if (displayGroup == null) {
			displayGroup = (WODisplayGroup) valueForBinding(DISPLAY_GROUP_BINDING);
			updateDisplayGroupSort();

			if (displayGroup().numberOfObjectsPerBatch() != batchSize()) {
				displayGroup().setNumberOfObjectsPerBatch(batchSize());
			}
		}

		return displayGroup;
	}

	/**
	 * @return <code>true</code> if currentColumn() is part of the sort
	 *         ordering
	 */
	public boolean isCurrentColumnSorted() {
		return currentColumnSortOrder() != null;
	}

	/**
	 * @return the sort order dictionary for currentColumn() or null if !
	 *         isCurrentColumnSorted()
	 */
	public NSMutableDictionary currentColumnSortOrder() {
		return (NSMutableDictionary) sortOrdersByKeypath().objectForKey(currentKeyPath());
	}

	/**
	 * @return <code>true</code> if currentColumn() is part of the sort
	 *         ordering and is being sorted in ascending order
	 */
	public boolean isCurrentColumnSortedAscending() {
		return isCurrentColumnSorted() ? SORT_ASCENDING.equals(currentColumnSortOrder().valueForKey(SORT_DIRECTION)) : false;
	}

	/**
	 * @return the index (1 based) of this columns precedence in sorting or -1
	 *         if it is not part of the sort order
	 */
	public int currentColumnSortIndex() {
		return isCurrentColumnSorted() ? sortOrders().indexOf(currentColumnSortOrder()) + 1 : -1;
	}

	/**
	 * @return the value from row() corresponding to currentColumn(), formatted
	 *         as configured
	 */
	public Object columnValue() {

		// Special case when there is no keyPath: return the row object. This is
		// intended for custom components
		// where there is no specific value
		if (currentKeyPath() == null || currentKeyPath().length() == 0) {
			return row();
		}

		Object rawValue = row().valueForKeyPath(currentKeyPath());
		Format formatter = columnFormatter();

		return (formatter != null && rawValue != null) ? formatter.format(rawValue) : rawValue;
	}

	/**
	 * Method used with automatic synchronizing custom components.
	 * 
	 * @param value
	 *            new value for row() in this column
	 */
	public void setColumnValue(Object value) {
		// Special case when there is no keyPath: there is nothing to set
		if (currentKeyPath() == null || currentKeyPath().length() == 0) {
			return;
		}

		Format formatter = columnFormatter();
		if (formatter != null && value instanceof String) {
			try {
				value = formatter.parseObject((String) value);
			}
			catch (ParseException e) {
				throw new NSForwardException(e);
			}
		}
		row().takeValueForKey(value, currentKeyPath());
	}

	/**
	 * @return the value from row() corresponding to currentColumn()
	 */
	public Format columnFormatter() {
		return (Format) formattersByKeypath().valueForKey(currentKeyPath());
	}

	/**
	 * @return the keyPath value from currentColumn()
	 */
	public String currentKeyPath() {
		return (String) currentColumn().valueForKey(KEY_PATH);
	}

	/**
	 * @return the name of the WOComponent to use to display the value from
	 *         currentColumn()
	 */
	public String columnComponentName() {
		String componentName = (String) currentColumn().valueForKey(COMPONENT_NAME);
		return componentName != null ? componentName : "WOString";
	}

	/**
	 * This list is implemented by AjaxGrid and is not based on the display
	 * group's selected objects. The list of selected objects is maintained
	 * across all batches.
	 * 
	 * @return list of user selected objects
	 */
	public NSMutableArray selectedObjects() {
		if (selectedObjects == null) {
			selectedObjects = hasBinding(SELECTED_OBJECTS_BINDING) ? (NSMutableArray) valueForBinding(SELECTED_OBJECTS_BINDING) : new NSMutableArray();
		}

		return selectedObjects;
	}

	/**
	 * Toggles inclusion of row into selectedObjects() (i.e. selects and
	 * de-selects it).
	 */
	public void toggleRowSelection() {
		if (isRowSelected()) {
			selectedObjects().removeObject(row);
		}
		else {
			selectedObjects().addObject(row);
		}
	}

	/**
	 * @return <code>true</code> if row() is in selectedObjects()
	 */
	public boolean isRowSelected() {
		return selectedObjects().containsObject(row);
	}

	/**
	 * Cover method for item binding of a WORepetition.
	 * 
	 * @return the current column being rendered
	 */
	public NSDictionary currentColumn() {
		return currentColumn;
	}

	/**
	 * Cover method for item binding of a WORepetition.
	 * 
	 * @param newColumn
	 *            current column being rendered
	 */
	public void setCurrentColumn(NSDictionary newColumn) {
		currentColumn = newColumn;
	}

	/**
	 * Cover method for item binding of a WORepetition.
	 * 
	 * @return the current row being rendered
	 */
	public NSKeyValueCodingAdditions row() {
		return row;
	}

	/**
	 * Cover method for item binding of a WORepetition.
	 * 
	 * @param newRow
	 *            current row being rendered
	 */
	public void setRow(NSKeyValueCodingAdditions newRow) {
		row = newRow;
	}

	/**
	 * @return this component so that it can be used in bindings
	 */
	public AjaxGrid thisComponent() {
		return this;
	}

}