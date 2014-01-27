package er.ajax.mootools;

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

import er.ajax.AjaxComponent;
import er.ajax.AjaxOption;
import er.ajax.AjaxUtils;

public class MTAjaxSortableList extends AjaxComponent {
    
	private static final long serialVersionUID = 1L;

	private String _id;
	private String _actionUrl;
	private String _sortOrderKeyName;
	private String _var;	
	
	public MTAjaxSortableList(WOContext context) {
        super(context);
    }

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {

		_actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
		_id = hasBinding("id") ? (String) valueForBinding("id") : safeElementID();
		_var = hasBinding("var") ? (String) valueForBinding("var") : safeElementID();

		_sortOrderKeyName = safeElementID(); 

		super.appendToResponse(response, context);

	}	
	
	public String listElementName() {
		return valueForStringBinding("listElementName", "ul");
	}

	public String listItemElementName() {
		return valueForStringBinding("listItemElementName", "li");
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
	protected void addRequiredWebResources(WOResponse res) {
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context(), res, "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		if(!canGetValueForBinding("list")) {
			throw new IllegalArgumentException("You must specify a readable 'list'.");
		}

		if (!canGetValueForBinding("listItemIDKeyPath")) {
			throw new IllegalArgumentException("You must specify 'listItemIDKeyPath' if you specify 'list'.");
		}

		String listItemIDKeyPath = (String) valueForBinding("listItemIDKeyPath");
		Object listItemIDArrayObj = request.formValues().objectForKey(_sortOrderKeyName + "[]");
		NSArray<String> listItemIDArray;

		if (listItemIDArrayObj instanceof NSArray) {
			listItemIDArray = (NSArray<String>) listItemIDArrayObj;
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

		if(mutableList) {
			reorderedList = (NSMutableArray) list;
		} else {
			reorderedList = new NSMutableArray();
		}

		int startIndex = 0;
		if(canGetValueForBinding("startIndex")) {
			Number startIndexNumber = (Number) valueForBinding("startIndex");
			startIndex = startIndexNumber.intValue();
			if(!mutableList) {
				for(int i = 0; i < startIndex; i++) {
					reorderedList.addObject(list.objectAtIndex(i));
				}
			}
		}

		int listItemIDCount = listItemIDArray.count();
		for(int listItemIDIndex = 0; listItemIDIndex < listItemIDCount; listItemIDIndex++) {

			String itemID = listItemIDArray.objectAtIndex(listItemIDIndex);
			NSRange itemPageRange;

			if(mutableList) {
				itemPageRange = new NSRange(startIndex + listItemIDIndex, listItemIDCount - listItemIDIndex);
			} else {
				itemPageRange = new NSRange(startIndex, listItemIDCount);
			}

			NSArray itemPageArray = list.subarrayWithRange(itemPageRange);
			EOQualifier itemIDQualifier = new EOKeyValueQualifier(listItemIDKeyPath, EOQualifier.QualifierOperatorEqual, itemID);
			NSArray matchingItems = EOQualifier.filteredArrayWithQualifier(itemPageArray, itemIDQualifier);

			if(matchingItems.count() == 0) {
				throw new NoSuchElementException("There was no item that matched the ID '" + itemID + "' in " + list + ".");
			} else if(matchingItems.count() > 1) {
				throw new IllegalStateException("There was more than one item that matched the ID '" + itemID + "' in " + list + ".");
			}

			Object replacingItem = matchingItems.objectAtIndex(0);

			if(mutableList) {
				int replacedItemIndex = itemPageRange.location();
				Object replacedItem = reorderedList.objectAtIndex(replacedItemIndex);
				if(replacedItem != replacingItem) {
					int replacingItemIndex = replacedItemIndex + itemPageArray.indexOfObject(replacingItem);
					reorderedList.replaceObjectAtIndex(replacingItem, replacedItemIndex);
					reorderedList.replaceObjectAtIndex(replacedItem, replacingItemIndex);
				}
			} else {
				reorderedList.addObject(replacingItem);
			}

			if(! mutableList) {
				int listCount = list.count();
				for(int i = startIndex + reorderedList.count(); i < listCount; i++) {
					reorderedList.addObject(list.objectAtIndex(i));
				}
				setValueForBinding(reorderedList, "list");
			}

			if(canGetValueForBinding("action")) {
				WOActionResults results = (WOActionResults) valueForBinding("action");
				if(results != null) {
					System.out.println("Not quite sure what to do with non-null results yet ...");
				}
			}

		}

		return null;	
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NSDictionary createAjaxOptions() {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("clone", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("constrain", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("handle", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSort", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onStart", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("opacity", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("revert", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("snap", AjaxOption.NUMBER));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
		return options;
	}	
	
	public String onComplete() {
		StringBuilder onCompleteBuffer = new StringBuilder();
		onCompleteBuffer.append("function(container) {");
		onCompleteBuffer.append("var data = ").append(var()).append(".serialize(false, function(element, index) {\n")
		.append("return '").append(_sortOrderKeyName).append("[]' + '=' + element.getProperty('id').replace('").append(_sortOrderKeyName).append("_','');}).join('&');");
		onCompleteBuffer.append("var ajaxRequest = new Request({url: '").append(_actionUrl).append("'}).send( { method: 'get', data: data } );");
		if(canGetValueForBinding("onComplete")) {
			String onComplete = (String) valueForBinding("onComplete");
			onCompleteBuffer.append(" var parentOnComplete = ")
			.append(onComplete).append(';').append("parentOnUpdate(container);");
		}
		onCompleteBuffer.append('}');

		return onCompleteBuffer.toString();
	}


	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getId() {
		return _id;
	}

	/**
	 * @return the var
	 */
	public String var() {
		return _var;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		_var = var;
	}	

}