package er.extensions;

import java.util.List;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Replacement for WORepetition. Is installed via ERXPatcher.setClassForName(ERXWORepetition.class, "WORepetition") into
 * the runtime system, so you don't need to reference it explicitely.
 * <ul>
 * <li>adds support for {@see java.util.List} and {@see java.lang.Array}, in addition to
 * {@see com.webobjects.foundation.NSArray} and {@see java.util.Vector} (which is a {@see java.util.List} in 1.4). This
 * is listed as Radar #3325342 since June 2003.</li>
 * <li>help with backtracking issues by adding not only the current index, but also the current object's hash code to
 * the element id, so it looks like "x.y.12345.z".<br />
 * If they don't match when invokeAction is called, the list is searched for a matching object. If none is found, then
 * the action is ignored or - when the property <code>er.extensions.ERXWORepetition.raiseOnUnmatchedObject=true</code> -
 * an {@link ERXWORepetition.UnmatchedObjectException} is thrown.<br />
 * This feature is turned on globally if <code>er.extensions.ERXWORepetition.checkHashCodes=true</code> or on a
 * per-component basis by setting the <code>checkHashCodes</code> binding to true or false.<br />
 * <em>Known issues:</em>
 * <ul>
 * <li>you can't re-generate your list by creating new objects between the appendToReponse and the next
 * takeValuesFromRequest. <br />
 * When doing this by fetching EOs, this is should not a be problem, as the EO most probably has the same hashCode if
 * the EC stays the same. </li>
 * <li>Your moved object should still be in the list.</li>
 * <li>Form values are currently not fixed, which may lead to NullpointerExceptions or other failures. However, if they
 * happen, by default you would have used the wrong values, so it may be arguable that having an error is better...</li>
 * </li>
 * </ul>
 * Note that this implementation adds a small amount of overhead due to the creation of the Context for each RR phase,
 * but this is preferable to having to give so many parameters.
 * 
 * As an alternative to the default use of System.identityHashCode to unique your items, you can set the binding "uniqueKey" 
 * to be a string keypath on your items that can return a unique key for the item.  For instance, if you are using 
 * ERXGenericRecord, you can set uniqueKey = "rawPrimaryKey"; if your EO has an integer primary key, and this will make
 * the uniquing value be the primary key instead of the hash code.  While this reveals the primary keys of your items,
 * the set of possible valid matches is still restricted to only those that were in the list to begin with, so no 
 * additional capabilities are available to users.
 * 
 * @binding list the array or list of items to iterate over
 * @binding item the current item in the iteration
 * @binding count the total number of items to iterate over
 * @binding index the current index in the iteration
 * @binding uniqueKey a String keypath on item (relative to item, not relative to the component)
 * @binding checkHashCodes if true, checks the validity of repetition references during the RR loop
 * @binding raiseOnUnmatchedObject if true, an exception is thrown when the repetition does not find a matching object
 * @binding debugHashCodes if true, prints out hashcodes for each entry in the repetition as it is traversed
 * @binding batchFetch a comma-separated list of keypaths on the "list" array binding to batch fetch
 * 
 * @author ak
 */

public class ERXWORepetition extends WODynamicGroup {
	/** logging support */
	private static final Logger log = Logger.getLogger(ERXWORepetition.class);

	protected WOAssociation _list;
	protected WOAssociation _item;
	protected WOAssociation _count;
	protected WOAssociation _index;
	protected WOAssociation _uniqueKey;
	protected WOAssociation _checkHashCodes;
	protected WOAssociation _raiseOnUnmatchedObject;
	protected WOAssociation _eoSupport;
	protected WOAssociation _debugHashCodes;
	protected WOAssociation _batchFetch;

	private static boolean _checkHashCodesDefault = ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".checkHashCodes");
	private static boolean _raiseOnUnmatchedObjectDefault = ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".raiseOnUnmatchedObject");
	private static boolean _eoSupportDefault = ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".eoSupport");
	
	public static class UnmatchedObjectException extends RuntimeException {
		public UnmatchedObjectException() {

		}
	}

	/**
	 * WOElements must be reentrant, so we need a context object or will have to add the parameters to every method.
	 * Note that it's OK to have no object at all.
	 */
	protected class Context {
		protected NSArray nsarray;
		protected List list;
		protected Object[] array;

		public Context(Object object) {
			if (object != null) {
				if (object instanceof NSArray) {
					nsarray = (NSArray) object;
				}
				else if (object instanceof List) {
					list = (List) object;
				}
				else if (object instanceof Object[]) {
					array = (Object[]) object;
				}
				else {
					throw new IllegalArgumentException("Evaluating 'list' binding returned a " + object.getClass().getName() + " when it should return either a NSArray, an Object[] array or a java.util.List .");
				}
			}
		}

		/**
		 * Gets the number of elements from any object.
		 */
		protected int count() {
			if (nsarray != null) {
				return nsarray.count();
			}
			else if (list != null) {
				return list.size();
			}
			else if (array != null) {
				return array.length;
			}
			return 0;
		}

		/**
		 * Gets the object at the given index from any object.
		 */
		protected Object objectAtIndex(int i) {
			if (nsarray != null) {
				return nsarray.objectAtIndex(i);
			}
			else if (list != null) {
				return list.get(i);
			}
			else if (array != null) {
				return array[i];
			}
			return null;
		}
	}

	/** Designated Constructor. Gets called by the template parser. Checks if the bindings are valid. */
	public ERXWORepetition(String string, NSDictionary associations, WOElement woelement) {
		super(null, null, woelement);

		_list = (WOAssociation) associations.objectForKey("list");
		_item = (WOAssociation) associations.objectForKey("item");
		_count = (WOAssociation) associations.objectForKey("count");
		_index = (WOAssociation) associations.objectForKey("index");
		_uniqueKey = (WOAssociation) associations.objectForKey("uniqueKey");
		_checkHashCodes = (WOAssociation) associations.objectForKey("checkHashCodes");
		_raiseOnUnmatchedObject = (WOAssociation) associations.objectForKey("raiseOnUnmatchedObject");
		_debugHashCodes = (WOAssociation) associations.objectForKey("debugHashCodes");
		_eoSupport = (WOAssociation) associations.objectForKey("eoSupport");
		_batchFetch = (WOAssociation) associations.objectForKey("batchFetch");

		if (_list == null && _count == null) {
			_failCreation("Missing 'list' or 'count' attribute.");
		}
		if (_list != null && _item == null) {
			_failCreation("Missing 'item' attribute with 'list' attribute.");
		}
		if (_list != null && _count != null) {
			_failCreation("Illegal use of 'count' attribute with 'list' attribute.");
		}
		if (_count != null && (_list != null || _item != null)) {
			_failCreation("Illegal use of 'list', or 'item'attributes with 'count' attribute.");
		}
		if (_item != null && !_item.isValueSettable()) {
			_failCreation("Illegal read-only 'item' attribute.");
		}
		if (_index != null && !_index.isValueSettable()) {
			_failCreation("Illegal read-only 'index' attribute.");
		}
	}

	/** Utility to throw an exception if the bindings are incomplete. */
	protected void _failCreation(String message) {
		throw new WODynamicElementCreationException("<" + this.getClass().getName() + "> " + message);
	}

	/** Human readable description. */
	public String toString() {
		return ("<" + this.getClass().getName() + " list: " + (_list != null ? _list.toString() : "null") + " item: " + (_item != null ? _item.toString() : "null") + " count: " + (_count != null ? _count.toString() : "null") + " index: " + (_index != null ? _index.toString() : "null") + ">");
	}

	private int hashCodeForObject(WOComponent component, Object object) {
		int hashCode;
		if (object == null) {
			hashCode = 0;
		}
		else if (_uniqueKey != null) {
			String uniqueKeyPath = (String)_uniqueKey.valueInComponent(component);
			Object uniqueKey = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, uniqueKeyPath);
			if (uniqueKey instanceof Number) {
				hashCode = Math.abs(((Number)uniqueKey).intValue());
			}
			else if (uniqueKey instanceof String) {
				hashCode = Math.abs(Integer.parseInt((String)uniqueKey));
			}
			else {
				throw new IllegalArgumentException("Unable to convert " + uniqueKey + " into a number.");
			}
		}
		else if (eoSupport(component) && object instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject)object;
			hashCode = eo.editingContext().globalIDForObject(eo).hashCode();
		}
		else {
			hashCode = Math.abs(System.identityHashCode(object));
		}
		if (_debugHashCodes != null && _debugHashCodes.booleanValueInComponent(component)) {
			log.info("debugHashCodes for '" + _list.keyPath() + "', " + object + " = " + hashCode);
		}
		return hashCode;
		// return (object == null ? 0 : Math.abs(object.hashCode()));
	}

	/**
	 * Prepares the WOContext for the loop iteration.
	 * 
	 * @param checkHashCodes
	 */
	protected void _prepareForIterationWithIndex(Context context, int index, WOContext wocontext, WOComponent wocomponent, boolean checkHashCodes) {
		Object object = null;
		if (_item != null) {
			object = context.objectAtIndex(index);
			_item._setValueNoValidation(object, wocomponent);
		}
		if (_index != null) {
			Integer integer = ERXConstant.integerForInt(index);
			_index._setValueNoValidation(integer, wocomponent);
		}
		boolean didAppend = false;
		if (checkHashCodes) {
			if (object != null) {
				int hashCode = hashCodeForObject(wocomponent, object);
				if (hashCode != 0) {
					if (index != 0) {
						wocontext.deleteLastElementIDComponent();
					}
					String elementID = String.valueOf(hashCode);
					if (log.isDebugEnabled()) {
						log.debug("prepare " + elementID + "->" + object);
					}
					wocontext.appendElementIDComponent(elementID);
					didAppend = true;
				}
			}
		}
		if (!didAppend) {
			if (index != 0) {
				wocontext.incrementLastElementIDComponent();
			}
			else {
				wocontext.appendZeroElementIDComponent();
			}
		}
	}

	/** Cleans the WOContext after the loop iteration. */
	protected void _cleanupAfterIteration(int i, WOContext wocontext, WOComponent wocomponent) {
		if (_item != null) {
			_item._setValueNoValidation(null, wocomponent);
		}
		if (_index != null) {
			Integer integer = ERXConstant.integerForInt(i);
			_index._setValueNoValidation(integer, wocomponent);
		}
		wocontext.deleteLastElementIDComponent();
	}

	/** Fills the context with the object given in the "list" binding. */
	protected String _indexStringForSenderAndElement(String senderID, String elementID) {
		int dotOffset = elementID.length() + 1;
		int nextDotOffset = senderID.indexOf('.', dotOffset);
		String indexString;
		if (nextDotOffset < 0) {
			indexString = senderID.substring(dotOffset);
		}
		else {
			indexString = senderID.substring(dotOffset, nextDotOffset);
		}
		return indexString;
	}

	protected String _indexOfChosenItem(WORequest worequest, WOContext wocontext) {
		String indexString = null;
		String senderID = wocontext.senderID();
		String elementID = wocontext.elementID();
		if (senderID.startsWith(elementID)) {
			int i = elementID.length();
			if (senderID.length() > i && senderID.charAt(i) == '.')
				indexString = _indexStringForSenderAndElement(senderID, elementID);
		}
		return indexString;
	}

	protected int _count(Context context, WOComponent wocomponent) {
		int count;
		if (_list != null) {
			count = context.count();
		}
		else {
			Object object = _count.valueInComponent(wocomponent);
			if (object != null) {
				count = ERXValueUtilities.intValue(object);
			}
			else {
				log.error(toString() + " 'count' evaluated to null in component " + wocomponent.toString() + ".\nRepetition  count reset to 0.");
				count = 0;
			}
		}
		return count;
	}

	protected Context createContext(WOComponent wocomponent) {
		Object list = (_list != null ? _list.valueInComponent(wocomponent) : null);
		if (_batchFetch != null && list instanceof NSArray) {
			String batchFetchKeyPaths = (String)_batchFetch.valueInComponent(wocomponent);
			if (batchFetchKeyPaths != null) {
				NSArray<String> keyPaths = NSArray.componentsSeparatedByString(batchFetchKeyPaths, ",");
				if (keyPaths.count() > 0) {
					ERXRecursiveBatchFetching.batchFetch((NSArray)list, keyPaths, true);
				}
			}
		}
		return new Context(list);
	}

	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		Context context = createContext(wocomponent);

		int count = _count(context, wocomponent);
		boolean checkHashCodes = checkHashCodes(wocomponent);
		if (log.isDebugEnabled()) {
			log.debug("takeValuesFromRequest: " + wocontext.elementID() + " - " + wocontext.request().formValueKeys());
		}
		for (int index = 0; index < count; index++) {
			_prepareForIterationWithIndex(context, index, wocontext, wocomponent, checkHashCodes);
			super.takeValuesFromRequest(worequest, wocontext);
		}
		if (count > 0) {
			_cleanupAfterIteration(count, wocontext, wocomponent);
		}
	}

	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		Context repetitionContext = createContext(wocomponent);

		int count = _count(repetitionContext, wocomponent);

		WOActionResults woactionresults = null;
		String indexString = _indexOfChosenItem(worequest, wocontext);

		int index = 0;
		int hashCode = 0;
		boolean checkHashCodes = checkHashCodes(wocomponent);

		if (indexString != null) {
			if (checkHashCodes) {
				hashCode = Integer.parseInt(indexString);
			}
			else {
				index = Integer.parseInt(indexString);
			}
		}
		
		if (indexString != null) {
			if (_item != null) {
				Object object = null;
				if (checkHashCodes) {
					boolean found = false;
					int otherHashCode = 0;
					for (int i = 0; i < repetitionContext.count() && !found; i++) {
						Object o = repetitionContext.objectAtIndex(i);
						otherHashCode = hashCodeForObject(wocomponent, o);
						if (otherHashCode == hashCode) {
							object = o;
							index = i;
							found = true;
						}
					}
					if (!found) {
						log.warn("Wrong object: " + otherHashCode + " vs " + hashCode + " (array = " + repetitionContext.nsarray + ")");
						if (raiseOnUnmatchedObject(wocomponent)) {
							throw new UnmatchedObjectException();
						}
						return wocontext.page();
					}
					else {
						if (log.isDebugEnabled()) {
							log.debug("Found object: " + otherHashCode + " vs " + hashCode);
						}
					}
				}
				else {
					if (index >= repetitionContext.count()) {
						if (raiseOnUnmatchedObject(wocomponent)) {
							throw new UnmatchedObjectException();
						}
						else {
							return null;
						}
					}
					object = repetitionContext.objectAtIndex(index);
				}
				_item._setValueNoValidation(object, wocomponent);
			}
			if (_index != null) {
				Integer integer = ERXConstant.integerForInt(index);
				_index._setValueNoValidation(integer, wocomponent);
			}
			wocontext.appendElementIDComponent(indexString);
			if (log.isDebugEnabled()) {
				log.debug("invokeAction:" + wocontext.elementID());
			}
			woactionresults = super.invokeAction(worequest, wocontext);
			wocontext.deleteLastElementIDComponent();
		}
		else {
			int start = indexString == null ? 0 : index;
			int end = indexString == null ? count : (index + 1);

			for (int i = start; i < end && woactionresults == null; i++) {
				_prepareForIterationWithIndex(repetitionContext, i, wocontext, wocomponent, checkHashCodes);
				woactionresults = super.invokeAction(worequest, wocontext);
			}
			if (count > 0) {
				_cleanupAfterIteration(count, wocontext, wocomponent);
			}
		}
		return woactionresults;
	}

	private boolean checkHashCodes(WOComponent wocomponent) {
		if (_checkHashCodes != null) {
			return _checkHashCodes.booleanValueInComponent(wocomponent);
		}
		return _checkHashCodesDefault;
	}

	private boolean raiseOnUnmatchedObject(WOComponent wocomponent) {
		if (_raiseOnUnmatchedObject != null) {
			return _raiseOnUnmatchedObject.booleanValueInComponent(wocomponent);
		}
		return _raiseOnUnmatchedObjectDefault;
	}

	private boolean eoSupport(WOComponent wocomponent) {
		if (_eoSupport != null) {
			return _eoSupport.booleanValueInComponent(wocomponent);
		}
		return _eoSupportDefault;
	}
	
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		Context context = createContext(wocomponent);

		int count = _count(context, wocomponent);
		boolean checkHashCodes = checkHashCodes(wocomponent);
		if (log.isDebugEnabled()) {
			log.debug("appendToResponse:" + wocontext.elementID());
		}

		for (int index = 0; index < count; index++) {
			_prepareForIterationWithIndex(context, index, wocontext, wocomponent, checkHashCodes);
			super.appendChildrenToResponse(woresponse, wocontext);
		}
		if (count > 0) {
			_cleanupAfterIteration(count, wocontext, wocomponent);
		}
	}
}
