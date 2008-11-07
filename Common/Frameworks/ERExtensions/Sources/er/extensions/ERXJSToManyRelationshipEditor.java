package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import java.util.Enumeration;
import org.apache.log4j.Logger;

/**
 * A fancy to-many relationship editor component.
 * @author Travis Cripps
 *
 * @binding displayString
 * @binding item
 * @binding list
 * @binding selections
 * @binding sortKey
 */
public class ERXJSToManyRelationshipEditor extends ERXNonSynchronizingComponent {

    public static final Logger log = Logger.getLogger(ERXJSToManyRelationshipEditor.class);

    public static interface Keys {
        public static final String DisplayString = "displayString";
        public static final String Item = "item";
        public static final String List = "list";
        public static final String Selections = "selections";
        public static final String SortKey = "sortKey";
    }

    protected NSArray _list;
    protected NSArray _selections;
    protected Object currentItem;
    
    private String _selectionsString;
    private NSDictionary _cachedPossibleValuesDict;
    private String _editorContextID;

    public ERXJSToManyRelationshipEditor(WOContext context) {
        super(context);
    }

    public boolean isStateless() {
        return true;
    }

    public void reset() {
        invalidateCaches();
    }

    /**
     * Gets the item currently being displayed.
     * @return the item
     */
    public Object currentItem() {
        setValueForBinding(currentItem, Keys.Item);
        return currentItem;
    }

    /**
     * Sets the item being displayed.
     * @param anItem to display
     */
    public void setCurrentItem(Object anItem) {
        currentItem = anItem;
        setValueForBinding(currentItem, Keys.Item);
    }

    /**
     * Gets the list of items to display.
     * @return the list of items
     */
    public NSArray list() {
        if (null == _list) {
            _list = listFromBindings();
        }
        return _list;
    }

    /**
     * Gets the selections.
     * @return the selections
     */
    public NSArray selections() {
        if (null == _selections) {
            _selections = selectionsFromBindings();
        }
        return _selections;
    }

    /**
     * Gets the display string for the current item.
     * @return the display string
     */
    public String displayString() {
        return stringValueForBinding(Keys.DisplayString);
    }

    /**
     * Resets the state variables.
     */
    public void invalidateCaches() {
        _list = null;
        _selections = null;
        currentItem = null;
        _selectionsString = null;
        _cachedPossibleValuesDict = null;
        _editorContextID = null;
    }

    public void sleep() {
        invalidateCaches();
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        invalidateCaches();
        super.appendToResponse(aResponse, aContext);
    }

    /**
     * Gets the list of items and sorts it by the sort key if a sort key is available.
     * @return the maybe sorted list
     */
    public NSArray maybeSortedList() {
        if (hasBinding(Keys.SortKey)) {
            String sortKey = (String)valueForBinding(Keys.SortKey);
            if (sortKey != null && sortKey.length() > 0) {
                NSMutableArray sortedList = new NSMutableArray(listFromBindings());
                ERXArrayUtilities.sortArrayWithKey(sortedList, sortKey);
                return sortedList;
            }
        }
        return listFromBindings();
    }

    /**
     * Determines if the list is empty
     * @return true if the list is empty
     */
    public boolean isListEmpty() {
        NSArray anItemList = listFromBindings();
        return (anItemList == null || anItemList.count() == 0);
    }

    /**
     * Gets the index of the current item in the overall list.
     * @return the index of the current item
     */
    public int itemIndex() {
        Object item = objectValueForBinding(Keys.Item);
        if (null == item) {
            return -1;
        }
        return indexOfObjectInArrayUsingERXEOControlUtilitiesEOEquals(item, listFromBindings());
    }

    private int indexOfObjectInArrayUsingERXEOControlUtilitiesEOEquals(Object anObject, NSArray anArray) {
        if (anObject instanceof EOEnterpriseObject) {
            return ERXArrayUtilities.indexOfObjectUsingEqualator(anArray, anObject, ERXEqualator.EOEqualsEqualator);
        } else {
            return anArray.indexOfObject(anObject);
        }
    }

    /**
     * Formats the selection string for the hidden field from the indexes of the items in the selection subset of the list.
     * @return the selection string
     */
    public String selectionsString() {
        if (null == _selectionsString) {
            NSArray list = listFromBindings();
            NSArray selections = selectionsFromBindings();
            NSMutableArray indexes = new NSMutableArray();
            for (Enumeration selectionsEnum = selections.objectEnumerator(); selectionsEnum.hasMoreElements();) {
                Object aSelection = selectionsEnum.nextElement();
                int index = list.indexOfObject(aSelection);
                if (index >= 0) {
                    indexes.addObject(index);
                }
            }
            _selectionsString = indexes.componentsJoinedByString(", ");
        }
        return _selectionsString;
    }

    /**
     * Sets the selection string from the input.  Parses the input string to isolate the indexes of the selected items
     * and looks them up in the array.  Then pushes the selections up to the parent binding.
     * @param value of the selections string
     */
    public void setSelectionsString(String value) {
        _selectionsString = value;

        if (_selectionsString != null && _selectionsString.trim().length() > 0) {
            NSArray list = list();
            NSMutableArray selections = new NSMutableArray();

            NSArray itemOffsets = NSArray.componentsSeparatedByString(_selectionsString, ", ");
            for (Enumeration offsetsEnum = itemOffsets.objectEnumerator(); offsetsEnum.hasMoreElements();) {
                String offsetString = (String)offsetsEnum.nextElement();
                int offset = ERXStringUtilities.integerWithString(offsetString);
                if (offset < list.count()) {
                    selections.addObject(list.objectAtIndex(offset));
                }
            }

            _selections = selections;
            pushSelectionsBinding(selections);
        } else {
            pushSelectionsBinding(NSArray.EmptyArray);
        }
    }

    /**
     * Pushes the selections up to the parent component.
     * @param selections array
     */
    private void pushSelectionsBinding(NSArray selections) {
        if (canSetValueForBinding(Keys.Selections)) {
            setValueForBinding(selections, Keys.Selections);
        }
    }

    /**
     * Pulls the selections from the <code>selections</code> binding.
     * @return the selections, or an empty array if null
     */
    private NSArray selectionsFromBindings() {
        if (canGetValueForBinding(Keys.Selections)) {
            return (NSArray)valueForBinding(Keys.Selections);
        }
        return NSArray.EmptyArray;
    }

    /**
     * Pulls the list from the <code>list</code> binding.
     * @return the list, or an empty array if null
     */
    private NSArray listFromBindings() {
        if (canGetValueForBinding(Keys.List)) {
            return (NSArray)valueForBinding(Keys.List);
        }
        return NSArray.EmptyArray;
    }

    /**
     * Gets the context ID of the editor (this component), escaped for use in JavaScript.
     * @return the context ID
     */
    public String editorContextID() {
        if (null == _editorContextID) {
            _editorContextID = ERXStringUtilities.safeIdentifierName(context().elementID());
        }
        return _editorContextID;
    }

    /**
     * Formats the name of this editor instance.
     * @return the editor name
     */
    public String editorName() {
        return "ERXJSToManyRelationshipEditor_" + editorContextID();
    }

    /**
     * Formats the name of the hidden field for this editor instance.
     * @return the hidden field's name
     */
    public String hiddenFieldName() {
        return "ERXJSToManyRelationshipEditor_SelectedValues_" + editorContextID();
    }

    /**
     * Formats the name of the selected values table for this editor instance.
     * @return the table's name
     */
    public String selectedValuesTableName() {
        return "ERXJSToManyRelationshipEditor_SelectedValuesTable_" + editorContextID();
    }

    /**
     * Formats the JavaScript used to initialize this instance of the editor.
     * @return the script for this editor
     */
    public String javascriptForThisEditorInstance() {
        String safeElementID = ERXStringUtilities.safeIdentifierName(context().elementID());
        String editorName = editorName();
        StringBuilder sb = new StringBuilder();
        sb.append("var ").append(editorName()).append(" = new ERXJSToManyRelationshipEditor();\n");
        sb.append(editorName).append(".elementID = '").append(safeElementID).append("';\n");
        sb.append(editorName).append(".possibleValues = ").append(possibleValuesHashForScript()).append(";\n");
        sb.append(editorName).append(".selectedValues = ").append(selectedValuesArrayForScript());
        return sb.toString();
    }

    /**
     * Creates a JSON/JavaScript-formatted hash of the list item offsets and their display strings.
     * @return the hash representation
     */
    private String possibleValuesHashForScript() {
        NSDictionary valuesDict = cachedPossibleValues();
        NSMutableArray jsHashValues = new NSMutableArray();
        for (int i = 0; i < valuesDict.count(); i++) {
            String displayString = (String)valuesDict.objectForKey(i);
            jsHashValues.addObject(i + " : '" + displayString + "'");
        }
        return "{ " + jsHashValues.componentsJoinedByString(", ") + " }";
    }

    /**
     * Creates a JSON/JavaScript-formatted array of the selected list items' offsets.
     * @return the array representation
     */
    private String selectedValuesArrayForScript() {
        NSMutableArray offsets = new NSMutableArray();
        NSArray sortedList = maybeSortedList();
        for (Enumeration selectionsEnum = selections().objectEnumerator(); selectionsEnum.hasMoreElements();) {
            Object obj = selectionsEnum.nextElement();
            offsets.addObject(indexOfObjectInArrayUsingERXEOControlUtilitiesEOEquals(obj, sortedList));
        }
        return "[" + offsets.componentsJoinedByString(", ") + "]";
    }

    /**
     * Builds a cached dictionary of the list item offsets and their display strings.
     * @return the dictionary
     */
    private NSDictionary cachedPossibleValues() {
        if (null == _cachedPossibleValuesDict) {
            NSMutableDictionary result = new NSMutableDictionary();
            NSArray allValues = maybeSortedList();
            for (int i = 0; i < allValues.count(); i++) {
                Object item = allValues.objectAtIndex(i);
                setCurrentItem(item); // Force the item to push up to the parent component, so we can ask it for the displayString.
                String displayString = stringValueForBinding(Keys.DisplayString);
                String value = displayString != null ? displayString : item.toString();
                result.setObjectForKey(value, i);
            }
            _cachedPossibleValuesDict = result;
        }
        return _cachedPossibleValuesDict;
    }

    /**
     * Builds the JavaScript used to remove the item from the selections.
     * @return the script
     */
    public String removeItemScript() {
        return editorName() + ".removeFromSelectedValues(this, " + indexOfObjectInArrayUsingERXEOControlUtilitiesEOEquals(currentItem, maybeSortedList()) + "); return false;";
    }

    /**
     * Builds the JavaScript used to add the selected item from the selections popup to the selected items.
     * @return the script
     */
    public String addItemScript() {
        return editorName() + ".addToSelectedValues(); return false;";
    }

    /**
     * Builds the select menu for adding available items to the selected items.
     * @return the string for the select menu
     */
    public String availableValuesPopupMenu() {
        String selectTagName = "ERXJSToManyRelationshipEditor_SelectedValuesPopup_" + editorContextID();
        StringBuilder sb = new StringBuilder();
        sb.append("<select id=\"").append(selectTagName).append("\" name=\"").append(selectTagName).append("\">\n");
        NSDictionary allValuesDict = cachedPossibleValues();
        NSArray sortedValues = maybeSortedList();
        NSArray selections = selections();
        for (Enumeration keysEnum = allValuesDict.allKeys().objectEnumerator(); keysEnum.hasMoreElements();) {
            Integer key = (Integer)keysEnum.nextElement();
            String displayName = (String)allValuesDict.objectForKey(key);
            Object currentObject = sortedValues.objectAtIndex(key);
            if (!selections.containsObject(currentObject)) {
                sb.append("\t<option value=\"").append(key).append("\">").append(displayName).append("</option>\n");
            }
        }
        sb.append("</select>");
        return sb.toString();
    }
}
