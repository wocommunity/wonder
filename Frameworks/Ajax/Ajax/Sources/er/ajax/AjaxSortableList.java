package er.ajax;

import java.util.NoSuchElementException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSRange;

// PROTOTYPE FUNCTIONS (WRAPPER)
/**
 * 
 * @binding list the list to allow reordering on (required)
 * @binding item the repetition item (required)
 * @binding listItemIDKeyPath the key path of the id of each item (required)
 * @binding id the id of the list (required)
 * @binding listElementName the HTML element name of the list (defaults to 'ul')
 * @binding listItemElementName the HTML element name of the list item (defaults to 'li')
 * @binding listClass the CSS class of the list 
 * @binding listStyle the CSS style attribute of the list
 * @binding listItemClass the CSS class of the current list item
 * @binding listItemStyle the CSS style attribute of the current list item
 * @binding startIndex the start index of the list
 * @binding action the action to fire when the list is reordered
 * @binding tag
 * @binding only
 * @binding overlap
 * @binding constraint how to constraint moving elements, can take a value of <i>vertical</i>, <i>horizontal</i> 
 *          or <code>false</code> with <i>vertical</i> as default
 * @binding containment
 * @binding handle if an element should only be draggable by an embedded handle, takes a class name
 * @binding hoverclass
 * @binding ghosting shows ghosting copy during drag, defaults to <code>false</code>
 * @binding dropOnEmpty
 * @binding scroll
 * @binding onChange client side method, fires on updating the sort order during drag
 * @binding onUpdate client side method, fires on updating the sort order after dropping the element and order really changed
 * @binding index the repetition index
 * 
 * @author mschrag
 *
 */
public class AjaxSortableList extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private String _id;
  private String _actionUrl;
  private String _sortOrderKeyName;

  public AjaxSortableList(WOContext context) {
    super(context);
  }

  public String id() {
    return _id;
  }

  public String listElementName() {
    String elementName = "ul";
    if (hasBinding("listElementName")) {
      elementName = (String) valueForBinding("listElementName");
    }
    return elementName;
  }

  public String listItemElementName() {
    String elementName = "li";
    if (hasBinding("listItemElementName")) {
      elementName = (String) valueForBinding("listItemElementName");
    }
    return elementName;
  }

  public String listItemID(String itemID) {
    String listID = (String) valueForBinding("id");
    String listItemIDWithoutIndex = listID + "_" + itemID;
    return listItemIDWithoutIndex;
  }

  public String listItemID() {
    Object item = valueForBinding("item");
    String listItemIDKeyPath = (String) valueForBinding("listItemIDKeyPath");
    String itemID = String.valueOf(NSKeyValueCodingAdditions.Utility.valueForKeyPath(item, listItemIDKeyPath));
    String listItemID = listItemID(itemID);
    return listItemID;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    _actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
    if (hasBinding("id")) {
      _id = (String) valueForBinding("id");
    }
    else {
      _id = safeElementID();
    }
    _sortOrderKeyName = safeElementID();
    super.appendToResponse(response, context);
  }

  @Override
  protected void addRequiredWebResources(WOResponse res) {
    addScriptResourceInHead(res, "prototype.js");
    addScriptResourceInHead(res, "effects.js");
    addScriptResourceInHead(res, "dragdrop.js");
  }

  public NSDictionary createAjaxOptions() {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("tag", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("treeTag", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("only", AjaxOption.STRING_ARRAY));
    ajaxOptionsArray.addObject(new AjaxOption("overlap", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("constraint", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("containment", AjaxOption.STRING_ARRAY));
    ajaxOptionsArray.addObject(new AjaxOption("handle", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("hoverclass", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("ghosting", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("dropOnEmpty", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("scroll", AjaxOption.BOOLEAN));
    ajaxOptionsArray.addObject(new AjaxOption("onChange", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
    return options;
  }

  public String onUpdate() {
	StringBuilder onUpdateBuffer = new StringBuilder();
    onUpdateBuffer.append("function(container) {");
    // onComplete:ajaxResponse
    String containerID = (String) valueForBinding("id");
    onUpdateBuffer.append("var data = Sortable.serialize('" + containerID + "', { name:'" + _sortOrderKeyName + "'});");
    onUpdateBuffer.append("var ajaxRequest = new Ajax.Request('" + _actionUrl + "', {method: 'get', parameters: data});");
    if (canGetValueForBinding("onUpdate")) {
      String onUpdate = (String) valueForBinding("onUpdate");
      onUpdateBuffer.append(" var parentOnUpdate = ");
      onUpdateBuffer.append(onUpdate);
      onUpdateBuffer.append(';');
      onUpdateBuffer.append("parentOnUpdate(container);");
    }
    onUpdateBuffer.append('}');
    return onUpdateBuffer.toString();
  }

  @Override
  public WOActionResults handleRequest(WORequest request, WOContext context) {
    if (!canGetValueForBinding("list")) {
      throw new IllegalArgumentException("You must specify a readable 'list'.");
    }
    if (!canGetValueForBinding("listItemIDKeyPath")) {
      throw new IllegalArgumentException("You must specify 'listItemIDKeyPath' if you specify 'list'.");
    }
    String listItemIDKeyPath = (String) valueForBinding("listItemIDKeyPath");
    Object listItemIDArrayObj = request.formValues().objectForKey(_sortOrderKeyName + "[]");
    NSArray listItemIDArray;
    if (listItemIDArrayObj instanceof NSArray) {
      listItemIDArray = (NSArray) listItemIDArrayObj;
    }
    else if (listItemIDArrayObj instanceof String) {
      String listItemIDStr = (String) listItemIDArrayObj;
      listItemIDArray = new NSArray(listItemIDStr);
    }
    else {
      throw new IllegalArgumentException("Unknown list item ID array " + listItemIDArrayObj);
    }

    NSArray list = (NSArray) valueForBinding("list");
    boolean mutableList = (list instanceof NSMutableArray);
    NSMutableArray reorderedList;
    if (mutableList) {
      reorderedList = (NSMutableArray) list;
    }
    else {
      reorderedList = new NSMutableArray();
    }

    int startIndex = 0;
    // If we're starting at an index > 0, add the initial objects
    if (canGetValueForBinding("startIndex")) {
      Number startIndexNumber = (Number) valueForBinding("startIndex");
      startIndex = startIndexNumber.intValue();
      if (!mutableList) {
        for (int i = 0; i < startIndex; i++) {
          reorderedList.addObject(list.objectAtIndex(i));
        }
      }
    }

    // Add the reordered objects
    int listItemIDCount = listItemIDArray.count();
    for (int listItemIDIndex = 0; listItemIDIndex < listItemIDCount; listItemIDIndex++) {
      String itemID = (String) listItemIDArray.objectAtIndex(listItemIDIndex);
      NSRange itemPageRange;
      if (mutableList) {
        itemPageRange = new NSRange(startIndex + listItemIDIndex, listItemIDCount - listItemIDIndex);
      }
      else {
        itemPageRange = new NSRange(startIndex, listItemIDCount);
      }
      NSArray itemPageArray = list.subarrayWithRange(itemPageRange);
      EOQualifier itemIDQualifier = new EOKeyValueQualifier(listItemIDKeyPath, EOQualifier.QualifierOperatorEqual, itemID);
      NSArray matchingItems = EOQualifier.filteredArrayWithQualifier(itemPageArray, itemIDQualifier);
      if (matchingItems.count() == 0) {
        throw new NoSuchElementException("There was no item that matched the ID '" + itemID + "' in " + list + ".");
      }
      else if (matchingItems.count() > 1) {
        throw new IllegalStateException("There was more than one item that matched the ID '" + itemID + "' in " + list + ".");
      }
      Object replacingItem = matchingItems.objectAtIndex(0);
      if (mutableList) {
        int replacedItemIndex = itemPageRange.location();
        Object replacedItem = reorderedList.objectAtIndex(replacedItemIndex);
        if (replacedItem != replacingItem) {
          int replacingItemIndex = replacedItemIndex + itemPageArray.indexOfObject(replacingItem);
          reorderedList.replaceObjectAtIndex(replacingItem, replacedItemIndex);
          reorderedList.replaceObjectAtIndex(replacedItem, replacingItemIndex);
        }
      }
      else {
        reorderedList.addObject(replacingItem);
      }
    }

    // If we're just looking at a page, add all the objects AFTER the page
    if (!mutableList) {
      int listCount = list.count();
      for (int i = startIndex + reorderedList.count(); i < listCount; i++) {
        reorderedList.addObject(list.objectAtIndex(i));
      }
      setValueForBinding(reorderedList, "list");
    }

    if (canGetValueForBinding("action")) {
      WOActionResults results = (WOActionResults) valueForBinding("action");
      if (results != null) {
        System.out.println("AjaxDroppable.handleRequest: Not quite sure what to do with non-null results yet ...");
      }
    }

    return null;
  }
}
