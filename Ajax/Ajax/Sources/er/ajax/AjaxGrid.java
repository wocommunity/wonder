package er.ajax;

import java.text.Format;

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
 * Ajax powered grid based on HTML Table that provides drag and drop column re-ordering, complex sorting, and the ability
 * to embed components in cells.  Class names and spans are used extensively to allow the display to be heavily customized with 
 * CSS.
 * <p>
 * The data is taken from a WODisplayGroup. Use er.extensions.ERXBatchingDisplayGroup to provide high performance access to large
 * data sets.
 * <p>
 * Navigation between batches is not implemented as implementing in it another component bound to the display group will allow for a more flexible UI.
 * <p>
 * <h3>Configuration</h3>
 * Configuration is provided by an NSMutableDictionary and NSMutableArray data structure.  This reduces the number of bindings, eases
 * keeping related lists of information in synch, and provides an easy path for serializing a user's customizations to configuration for
 * persistent storage.  Described as a plist, the format of the configuration information is:
 * <pre>
 * {
 *      tableID = "exampleAjaxGrid";                // Goes on main table tag
 *      updateContainerID = "ajaxGridContainer";    // Goes on div wrapping table, used with AjaxUpdate* components
 *      cssClass = "ajaxGrid";                      // CSS class attribute on main table tag, optional
 *      cssStyle = "border: thin solid #000000;";   // CSS style attribute on main table tag, optional
 *      evenRowCSSClass = "yellowBackground";       // CSS class attribute on even rows, optional
 *      oddRowCSSClass = "greyBackground";          // CSS class attribute on odd rows, optional
 *      evenRowCSSStyle = "background:lightyellow"  // CSS style attribute on even rows, optional
 *      oddRowCSSStyle = "background:lightgrey";    // CSS style attribute on odd rows, optional
 *      canReorder = true;                          // Enables (or disables) drag and drop reordering of columns
 *      batchSize = 10;                             // Controls size of batch in display group, use zero for no batching
 *
 *      columns = (                                 // List of columns to display, controls the initial display order
 *          {
 *              title = "Name";                     // Title for this column in the table header row
 *              keyPath = "fullName";               // Key path into row returning the data to display in this colunmn
 *          },                                      // keyPath is optional if component is specified
 *          {
 *              title = "Department";
 *              keyPath = "department.name";
 *          },
 *          {
 *              title = "Hire Date";
 *              keyPath = "hireDate";
 *              formatterClass = "com.webobjects.foundation.NSTimestampFormatter"; // Class of formatter to apply to values in this column
 *              formatPattern = "%m/%d/%y";         // Optional pattern if needed by formatter
 *          },
 *          {
 *              title = "Salary";
 *              keyPath = "salary";
 *              formatterClass = "com.webobjects.foundation.NSNumberFormatter";
 *              formatPattern = "$###,##0.00";
 *              cssClass = "alignRight";            // CSS class attribute td tag in this column, optional
 *          },
 *          {
 *              title = "Vacation Days";
 *              keyPath = "daysVacation";
 *              formatterClass = "com.webobjects.foundation.NSNumberFormatter";
 *              cssStyle = "text-align: right;";    // CSS style attribute td tag in this column, useful for text-align and width, optional
 *          },
 *          {
 *              title =  "Actions";
 *              keyPath = "";                       // Missing or empty keypath results in the current object itself being passed to component
 *              component = "EmployeeActions";      // Name of WOComponent to be displayed in this column.  Gets passed a single binding, value
 *              cssClass = "alignCenter";
 *          }
 *      );
 *      sortOrder = (
 *          {
 *              keyPath = "department.name";
 *              direction = "ascending";
 *          },
 *          {
 *              keyPath = "salary";
 *              direction = "descending";
 *          },
 *          {
 *              keyPath = "daysVacation";
 *              direction = "ascending";
 *          }
 *    );
 *
 *}
 * </pre>
 * <h4>Initializing Configuration From a File</h4>
 * You can get the configuration information from a plist file with code like this:
 * <pre>
 * public NSMutableDictionary configData() {
 *     if (configData == null) {
 *         // Get data from user preferences here if available, otherwise load the defaults
 *         configData = mySession().user().preferencesFor("AjaxGridExample");
 *         if (configData == null) {
 *             NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AjaxGridExampleConfiguration.plist", null, NSArray.EmptyArray));
 *             configData = new NSMutableDictionary((NSDictionary)NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
 *         }
 *      }
 *
 *      return configData;
 * }
 * </pre>   
 * <h3>Updating the Grid From a Different Component</h3>
 * Two things need to happen when the grid contents are updated.  First, the AjaxUpdateContainer needs to be updated. The
 * grid configuration <code>updateContainerID</code> gives the ID for this.  This can be used as the <code>updateContainerID</code>
 * in a <code>AjaxUpdateLink</code> or <code>&lt;updateContainerID&gt;Update()</code> can be called directly. Secondly, the drag and drop
 * functionality needs to be re-enabled by calling <code>ajaxGrid_init($(&lt;tableID&gt;));</code> where <code>tableID</code> is the 
 * ID specified in the configuration.
 * <p>
 * Here is an example.  In this example, <code>updateContainerID</code> is used to update the grid, and then the <code>NavBar</code> container
 * is updated manually from <code>onComplete</code>.  The reason for this is that the grid needs to update first so that the correct 
 * values are available for updating the  <code>NavBar</code>.
 * <p>
 * Relevant Configuration:
 * <pre>
 * {
 *    tableID = "exampleAjaxGrid";
 *    updateContainerID = "ajaxGridContainer";
 *    . . .
 * </pre>
 * WOD Bindings:
 * <pre>
 * NavUpdater: AjaxUpdateContainer {
 *     id = "NavBar";
 * }
 * 
 * NextBatch : AjaxUpdateLink {
 *     action = nextBatch;
 *     updateContainerID = "ajaxGridContainer";
 *     onComplete = "function(request) { NavBarUpdate(); ajaxGrid_init($(\"exampleAjaxGrid\")); }";
 * }
 * </pre>
 * <h3>Advanced Styling of the Grid</h3>
 * The grid contains several places were there are nested anonymous <code>span</code> tags wrapping default text content.  
 * These are there so that the span wrapping the default content can be set to <code>display: none</code> and the content of the
 * outer div given in CSS.  For this HTML:  
 * <pre>
 * &lt;th class="ajaxGridRemoveSorting"&gt;&lt;span&gt;&lt;span&gt;X&lt;/span&gt;&lt;/span&gt;&lt;/th&gt;
 * </pre>
 * The default <b>X</b> can replaced with an image with this CSS:
 * <pre>
 * th.ajaxGridRemoveSorting span span {display: none}
 * th.ajaxGridRemoveSorting span {background-image: url(/remove.gif); }
 * </pre>
 * You can also use <code>content</code> to replace the span content with text if your browser (not IE) supports it.
 * <h3>To Do</h3>
 * <ul>
 * <li>wrap JavaScript in function literal for namespace protection</li>
 * <li>drag ghost of column instead of changing column background color when dragging.  See <a href="http://www.webreference.com/programming/javascript/mk/column2/index.html">reference</a>.</li>
 * <li>support multiple grids on a single page</li>
 * <li>make stateless</li>
 * <li>allow auto configuration from .woo file</li>
 * <li>allow sorting to be enabled / disabled</li>
 * </ul>
 *  
 * @binding displayGroup WODisplayGroup acting as source and batching engine for the data to be displayed
 * @binding configurationData NSMutableDictionary used to configure grid, see documentation for details
 * @binding selectedObjects NSMutableArray list of rows that the user has selected from the grid
 * 
 * @author chill
 */
public class AjaxGrid extends WOComponent 
{
    
    
    private WODisplayGroup displayGroup;    // binding
    private NSMutableDictionary configurationData; // binding
    private NSMutableArray selectedObjects; // binding
    
    private NSMutableDictionary columnsByKeypath;    // optimization
    private NSMutableDictionary sortOrdersByKeypath;    // optimization
    private NSMutableDictionary formattersByKeypath;    // optimization
    
    private NSKeyValueCodingAdditions row;    // local  binding
    private NSDictionary currentColumn;    // local binding
    public int rowIndex;  // local binding
    
    public static final String KEY_PATH = "keyPath";
    public static final String SORT_DIRECTION = "direction";
    public static final String SORT_ASCENDING = "ascending";
    public static final String SORT_DESCENDING = "descending";
    public static final String SORT_ORDER = "sortOrder";
    public static final String COLUMNS = "columns";
    public static final String BATCH_SIZE = "batchSize";
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
    public static final String COMPONENT_NAME = "component";


    
    public AjaxGrid(WOContext context) {
        super(context);
    }

    
    
    /**
     * Adds movecolumns.js to the header.
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        AjaxUtils.addScriptResourceInHead(context, response, "movecolumns.js");
    }

    
    
    /**
     * Ajax action method for when columns are dragged and dropped.  Updates configurationData().
     */
    public void columnOrderUpdated() {
        // The Java script should ensure that these are valid
        int sourceIndex = Integer.parseInt((String)context().request().formValueForKey(SOURCE_COLUMN_FORM_VALUE)) - 1;
        int destinationIndex = Integer.parseInt((String)context().request().formValueForKey(DESTINATION_COLUMN_FORM_VALUE)) - 1;
        
        Object sourceColumn = columns().objectAtIndex(sourceIndex);
        columns().remove(sourceIndex);
        columns().insertObjectAtIndex(sourceColumn, destinationIndex);
    }
    
    
    
    /**
     * Ajax action method for when column titles are clicked to change sorting.  Updates configurationData()
     * and displayGroup().
     */
    public void  sortOrderUpdated() {
        String keyPath = (String)currentColumn().objectForKey(KEY_PATH);
        
        // Columns without a key path can't be sorted
        if (keyPath == null) {
            return;
        }
        
        NSMutableDictionary sortOrder = currentColumnSortOrder();        
        if (sortOrder == null) {
            NSMutableDictionary newSortOrder = new NSMutableDictionary(2);
            newSortOrder.setObjectForKey(keyPath, KEY_PATH);
            newSortOrder.setObjectForKey(SORT_ASCENDING, SORT_DIRECTION);
            
            sortOrders().add(newSortOrder);
            clearCachedConfiguration();
        } else {
            String direction = (String)sortOrder.objectForKey(SORT_DIRECTION);
            sortOrder.setObjectForKey(SORT_ASCENDING.equals(direction) ? SORT_DESCENDING : SORT_ASCENDING, SORT_DIRECTION);
        }
        
        updateDisplayGroup();
    }
    
    
    
    /**
     * Ajax action method for when control clicked to remove all sorting.  Updates configurationData()
     * and displayGroup().
     */
    public void removeSorting() {
        configurationData().setObjectForKey(new NSMutableArray(), SORT_ORDER);
        clearCachedConfiguration();
        updateDisplayGroup();
    }


    
    /**
     * Binding value for onComplete functions that need to re-initialize drag and drop on the grid (e.g. after
     * the grid contents have been updated).
     * 
     * @return Binding value for Ajax onComplete bindings
     */
    public String columnOrderUpdateOnComplete() {
        return canReorder() ? "function(request) { ajaxGrid_init($(\"" + tableID() + "\")); }" : null;
    }
    
    
    
    /**
     * Script that goes on this page to initialize drag and drop on the grid when the page loads
     * 
     * @return JavaScript to initialize drag and drop on the grid
     */
    public String initScript() {
        return canReorder() ? "<script type=\"text/javascript\">ajaxGrid_init($(\"" + tableID() +"\"));</script>" : null;
    }

    
    
    /**
     * @return the configurationData
     */
    public NSMutableDictionary configurationData() {
        return configurationData;
    }

    
    
    /**
     * Sets configuration data.
     *
     * @param configData the configurationData to set
     */
    public void setConfigurationData(NSMutableDictionary configData) {
        configurationData = configData;
    }

    
    
    /**
     * Clears local cache of configuration data so that fresh data will be cached.
     *
     */
    protected void clearCachedConfiguration()     {
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
        return Boolean.valueOf((String)configurationData().valueForKey(CAN_REORDER)).booleanValue();
    }
    
    
    
    /**
     * Returns TABLE_ID value from configurationData()
     * 
     * @return HTML ID for <table> implementing the grid
     */
    public String tableID() {
        return (String)configurationData().valueForKey(TABLE_ID);
    }
    
    

    
    /**
     * Returns COLUMNS value from configurationData()
     * 
     * @return list of configuration for the columns to display in the grid
     */
    protected NSMutableArray columns() {
        return (NSMutableArray)configurationData().valueForKey(COLUMNS);
    }
    
    
    
    /**
     * Returns SORT_ORDER value from configurationData()
     * 
     * @return list of sort orders controlling display of data in the grid
     */
    protected NSMutableArray sortOrders() {
        return (NSMutableArray)configurationData().valueForKey(SORT_ORDER);
    }

    
    
    /**
     * Returns BATCH_SIZE value from configurationData()
     * 
     * @return batch size for the display grid
     */
    protected int batchSize() {
        return Integer.parseInt((String)configurationData().valueForKey(BATCH_SIZE));
    }
    
    
    
    /**
     * Returns EVEN_ROW_CSS_CLASS or ODD_ROW_CSS_CLASS, depending on rowIndex, value from configurationData()
     * 
     * @return CSS class for this row
     */
    public String rowClass() {
        boolean isEven = rowIndex % 2 == 0;
        return (String)configurationData().valueForKey(isEven ? EVEN_ROW_CSS_CLASS : ODD_ROW_CSS_CLASS);
    }
    
    
    
    /**
     * Returns EVEN_ROW_CSS_STYLE or ODD_ROW_CSS_STYLE, depending on rowIndex, value from configurationData()
     * 
     * @return CSS class for this row
     */
    public String rowStyle() {
        boolean isEven = rowIndex % 2 == 0;
        return (String)configurationData().valueForKey(isEven ? EVEN_ROW_CSS_STYLE : ODD_ROW_CSS_STYLE);
    }


    
    /**
     * @return dictionary of columns() keyed on KEY_PATH of column
     */
    public NSMutableDictionary columnsByKeypath() {
        if (columnsByKeypath == null) {
            NSArray columns = columns();
            columnsByKeypath = new NSMutableDictionary(columns.count());
            for (int i = 0; i < columns.count(); i ++) {
                columnsByKeypath.setObjectForKey(columns.objectAtIndex(i), ((NSKeyValueCoding)columns.objectAtIndex(i)).valueForKey(KEY_PATH));
            }
        }

        return columnsByKeypath;
    }
    
    
    
    /**
     * @return dictionary of sortOrders() keyed on KEY_PATH of column
     */
    public NSMutableDictionary sortOrdersByKeypath()
    {
        if (sortOrdersByKeypath == null) {
            NSArray sortOrders = sortOrders();
            sortOrdersByKeypath = new NSMutableDictionary(sortOrders.count());
            for (int i = 0; i < sortOrders.count(); i ++) {
                sortOrdersByKeypath.setObjectForKey(sortOrders.objectAtIndex(i), ((NSKeyValueCoding)sortOrders.objectAtIndex(i)).valueForKey(KEY_PATH));
            }
        }
        return sortOrdersByKeypath;
    }

    
    
    
    
    /**
     * @return dictionary of formatters for columns() keyed on KEY_PATH of column
     */
    public NSMutableDictionary formattersByKeypath()
    {
        if (formattersByKeypath == null) {
            NSArray columns = columns();
            formattersByKeypath = new NSMutableDictionary(columns.count());
            for (int i = 0; i < columns.count(); i ++) {
                NSDictionary column = (NSDictionary)columns.objectAtIndex(i);
                String className = (String) column.valueForKey(FORMATTER_CLASS);
                if (className != null)
                {
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
     * Updates batch size and sort orders on the display group
     */
    protected void updateDisplayGroup()
    {
        NSArray sort = sortOrders();
        NSMutableArray sortOrders = new NSMutableArray(sort.count());
        for (int i = 0; i < sort.count(); i++) {
            NSDictionary column = (NSDictionary)sort.objectAtIndex(i);
            sortOrders.add(new EOSortOrdering((String)column.objectForKey(KEY_PATH), 
                    (SORT_ASCENDING.equals(column.objectForKey(SORT_DIRECTION)) ? EOSortOrdering.CompareCaseInsensitiveAscending : EOSortOrdering.CompareCaseInsensitiveDescending)));
        }

        displayGroup().setSortOrderings(sortOrders);
        displayGroup().updateDisplayedObjects();
        
        // Bug - when does this get updated
        if (displayGroup().numberOfObjectsPerBatch() != batchSize()) {
            displayGroup().setNumberOfObjectsPerBatch(batchSize());    
        }
    }
    
    
    
    /**
     * @return the displayGroup
     */
    public WODisplayGroup displayGroup() {
        return displayGroup;
    }

    
    
    /**
     * Sets the display group and calls updateDisplayGroup() if it is not the same object

     * @param dg the displayGroup to set
     */
    public void setDisplayGroup(WODisplayGroup dg) {
        if (displayGroup != dg) {
            displayGroup = dg;
            updateDisplayGroup();
        }
        
        // After being set by updateDisplayGroup(), the batch size in the displayGroup drives (is recorded in) the configuration
        configurationData().setObjectForKey(String.valueOf(displayGroup().numberOfObjectsPerBatch()), BATCH_SIZE);
    }


    
    /**
     * @return <code>true</code> if currentColumn() is part of the sort ordering
     */
    public boolean isCurrentColumnSorted()
    {
        return  currentColumnSortOrder() != null;
    }

    
    
    /**
     * @return the sort order dictionary for currentColumn() or null if ! isCurrentColumnSorted()
     */
    public NSMutableDictionary currentColumnSortOrder() {
        return (NSMutableDictionary) sortOrdersByKeypath().objectForKey(currentKeyPath());
    }
    
    
    
    /**
     * @return <code>true</code> if currentColumn() is part of the sort ordering and is being sorted in ascending order
     */
    public boolean isCurrentColumnSortedAscending() {
        return  isCurrentColumnSorted() ? SORT_ASCENDING.equals(currentColumnSortOrder().valueForKey(SORT_DIRECTION)) : false;
    }
    
    
    
    /**
     * @return the index (1 based) of this columns precedence in sorting or -1 if it is not part of the sort order
     */
    public int currentColumnSortIndex() {
        return isCurrentColumnSorted() ? sortOrders().indexOf(currentColumnSortOrder()) + 1 : -1;
    }
    

    
    /**
     * @return the value from row() corresponding to currentColumn(), formatted as configured
     */
    public Object columnValue() {
        
        // Special case when there is no keyPath: return the row object.  This is intended for custom components
        if (currentKeyPath() == null || currentKeyPath().length() == 0) {
            return row();
        }
        
        Object rawValue = row().valueForKeyPath(currentKeyPath());
        Format formatter = columnFormatter();
        return formatter != null ? formatter.format(rawValue) : rawValue; 
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
        return (String)currentColumn().valueForKey(KEY_PATH);
    }
    
    
    
    /**
     * @return the name of the WOComponent to use to display the value from currentColumn()
     */
    public String columnComponentName() {
        String componentName = (String)currentColumn().valueForKey(COMPONENT_NAME);
        return componentName != null ? componentName : "WOString";
    }
    
    
    
    /**
     * This is implemented by AjaxGrid and is not based on the display group's selected objects.  The
     * list of selected objects is maintained across all batches.
     * 
     * @return list of user selected objects
     */
    public NSMutableArray selectedObjects() {
        if (selectedObjects == null) {
            selectedObjects = new NSMutableArray();
        }
            
        return selectedObjects;
    }

    
    
    /**
     * Toggles inclusion of row into selectedObjects() (i.e. selects and de-selects it). 
     */
    public void toggleRowSelection() {
        if (isRowSelected()) {
            selectedObjects().removeObject(row);
        } else {
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
     * @param newColumn current column being rendered
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
     * @param newRow current row being rendered
     */
    public void setRow(NSKeyValueCodingAdditions newRow) {
        row = newRow;
    }
    
    

}