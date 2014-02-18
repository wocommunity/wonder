package er.extensions.components._private;

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
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.eof.ERXBatchFetchUtilities;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXDatabaseContextDelegate;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Replacement for WORepetition. It is installed via ERXPatcher.setClassForName(ERXWORepetition.class, "WORepetition") 
 * into the runtime system, so you don't need to reference it explicitly.
 * <ul>
 * <li>adds support for {@link java.util.List} and {@link java.lang.Array}, in addition to
 * {@link com.webobjects.foundation.NSArray} and {@link java.util.Vector} (which is a {@link java.util.List} in 1.4). This
 * is listed as Radar #3325342 since June 2003.</li>
 * <li>help with backtracking issues by adding not only the current index, but also the current object's hash code to
 * the element id, so it looks like "x.y.12345.z".<br />
 * If they don't match when invokeAction is called, the list is searched for a matching object. If none is found, then:
 * <ul>
 * <li>if the property <code>er.extensions.ERXWORepetition.raiseOnUnmatchedObject=true</code> -
 * an {@link ERXWORepetition.UnmatchedObjectException} is thrown</li>
 * <li>if <code>notFoundMarker</code> is bound, that is used for the item in the repetition.  This can be used to flag
 * special handling in the action method, possibly useful for Ajax requests</li>
 * <li>otherwise, the action is ignored</li>
 * </ul>
 * This feature is turned on globally if <code>er.extensions.ERXWORepetition.checkHashCodes=true</code> or on a
 * per-component basis by setting the <code>checkHashCodes</code> binding to true or false.<br />
 * <em>Known issues:</em>
 * <ul>
 * <li>you can't re-generate your list by creating new objects between the appendToReponse and the next
 * takeValuesFromRequest unless you use <code>uniqueKey</code> and the value for that key is consistent across
 * the object instances<br />
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
 * additional capabilities are available to users.  <code>uniqueKey</code> does <b>not</b> have to return an integer.
 * 
 * @binding list the array or list of items to iterate over
 * @binding item the current item in the iteration
 * @binding count the total number of items to iterate over
 * @binding index the current index in the iteration
 * @binding uniqueKey a String keypath on item (relative to item, not relative to the component) returning a value whose
 * toString() is unique for this component
 * @binding checkHashCodes if true, checks the validity of repetition references during the RR loop
 * @binding raiseOnUnmatchedObject if true, an exception is thrown when the repetition does not find a matching object
 * @binding debugHashCodes if true, prints out hashcodes for each entry in the repetition as it is traversed
 * @binding batchFetch a comma-separated list of keypaths on the "list" array binding to batch fetch
 * @binding eoSupport try to use globalIDs to determine the hashCode for EOs
 * @binding notFoundMarker used for the item in the repetition if checkHashCodes is true, don't bind directly to null as
 * that will be translated to false
 * 
 * @property er.extensions.ERXWORepetition.checkHashCodes add hash codes to element IDs so backtracking can be controlled
 * @property er.extensions.ERXWORepetition.raiseOnUnmatchedObject if an object wasn't found, raise an exception (if unset, the wrong object is used)
 * @property er.extensions.ERXWORepetition.eoSupport use hash code of GlobalID instead of object's hash code if it is an EO
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
	protected WOAssociation _notFoundMarker;

	private static boolean _checkHashCodesDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXWORepetition.checkHashCodes", ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".checkHashCodes"));
	private static boolean _raiseOnUnmatchedObjectDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXWORepetition.raiseOnUnmatchedObject", ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".raiseOnUnmatchedObject"));
	private static boolean _eoSupportDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXWORepetition.eoSupport", ERXProperties.booleanForKey(ERXWORepetition.class.getName() + ".eoSupport"));
	
	public static class UnmatchedObjectException extends RuntimeException {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public UnmatchedObjectException() {

		}
	}

	/**
	 * WOElements must be reentrant, so we need a context object or will have to add the parameters to every method.
	 * Note that it's OK to have no object at all.
	 */
	protected static class Context {
		protected NSArray<Object> nsarray;
		protected List<Object> list;
		protected Object[] array;

		public Context(Object object) {
			if (object != null) {
				if (object instanceof NSArray) {
					nsarray = (NSArray<Object>) object;
				}
				else if (object instanceof List) {
					list = (List<Object>) object;
				}
				else if (object instanceof Object[]) {
					array = (Object[]) object;
				}
				else {
					throw new IllegalArgumentException("Evaluating 'list' binding returned a " + object.getClass().getName() +
							" when it should return either a NSArray, an Object[] array or a java.util.List .");
				}
			}
		}

		/**
		 * Gets the number of elements from any object.
		 * 
		 * @return size of the list 
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
		 * 
		 * @param i index
		 * @return object at index
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

	/**
	 * Designated Constructor. Gets called by the template parser. Checks if the bindings are valid.
	 *  
	 * @param string 
	 * @param associations 
	 * @param woelement
	 **/
	public ERXWORepetition(String string, NSDictionary<String, WOAssociation> associations, WOElement woelement) {
		super(null, null, woelement);

		_list = associations.objectForKey("list");
		_item = associations.objectForKey("item");
		_count = associations.objectForKey("count");
		_index = associations.objectForKey("index");
		_uniqueKey = associations.objectForKey("uniqueKey");
		_checkHashCodes = associations.objectForKey("checkHashCodes");
		_raiseOnUnmatchedObject = associations.objectForKey("raiseOnUnmatchedObject");
		_debugHashCodes = associations.objectForKey("debugHashCodes");
		_eoSupport = associations.objectForKey("eoSupport");
		_batchFetch = associations.objectForKey("batchFetch");
		_notFoundMarker = associations.objectForKey("notFoundMarker");
		
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

	/**
	 * Utility to throw an exception if the bindings are incomplete.
	 * 
	 * @param message 
	 **/
	protected void _failCreation(String message) {
		throw new WODynamicElementCreationException("<" + getClass().getName() + "> " + message);
	}

	@Override
	public String toString() {
		return new StringBuilder().append('<').append(getClass().getName())
				.append(" list: ").append(_list)
				.append(" item: ").append(_item)
				.append(" count: ").append(_count)
				.append(" index: ").append(_index).append('>').toString();
	}

	private int hashCodeForObject(WOComponent component, Object object) {
		int hashCode;
		if (object == null) {
			hashCode = 0;
		}
		else if (eoSupport(component) && object instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject)object;
			EOEditingContext editingContext = eo.editingContext();
			EOGlobalID gid = null;
			if (editingContext != null) {
				gid = editingContext.globalIDForObject(eo);
			}
			// If the EO isn't in an EC, or it has a null GID, then just fall back to the hash code
			if (gid == null) {
				hashCode = System.identityHashCode(object);
			}
			else {
				hashCode = gid.hashCode();
			}
		}
		else {
			hashCode = System.identityHashCode(object);
		}
		// @see java.lang.Math#abs for an explanation of this
		if (hashCode == Integer.MIN_VALUE) {
			hashCode = 37; // MS: random prime number
		}
		hashCode = Math.abs(hashCode);
		if (_debugHashCodes != null && _debugHashCodes.booleanValueInComponent(component)) {
			log.info("debugHashCodes for '" + _list.keyPath() + "', " + object + " = " + hashCode);
		}
		return hashCode;
	}
	
	private String keyForObject(WOComponent component, Object object) {
		String uniqueKeyPath = (String)_uniqueKey.valueInComponent(component);
		Object uniqueKey = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, uniqueKeyPath);
		if (uniqueKey == null) {
			throw new IllegalArgumentException("Can't use null as uniqueKey for " + object);
		}
		
		String key = ERXStringUtilities.safeIdentifierName(uniqueKey.toString());

		if (_debugHashCodes != null && _debugHashCodes.booleanValueInComponent(component)) {
			log.info("debugHashCodes for '" + _list.keyPath() + "', " + object + " = " + key);
		}
		return key;
	}

	/**
	 * Prepares the WOContext for the loop iteration.
	 * 
	 * @param context 
	 * @param index 
	 * @param wocontext 
	 * @param wocomponent 
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
				String elementID = null;
				if (_uniqueKey == null) {
					int hashCode = hashCodeForObject(wocomponent, object);
					if (hashCode != 0) {
						elementID = String.valueOf(hashCode);
					}
				}
				else {
					elementID = keyForObject(wocomponent, object);
				}

				if (elementID != null) {
					if (index != 0) {
						wocontext.deleteLastElementIDComponent();
					}
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

	/**
	 * Cleans the WOContext after the loop iteration.
	 * 
	 * @param i 
	 * @param wocontext 
	 * @param wocomponent 
	 **/
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

	/**
	 * Fills the context with the object given in the "list" binding.
	 * 
	 * @param senderID 
	 * @param elementID 
	 * @return index string
	 **/
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
		if(list instanceof NSArray) {
			if (_batchFetch != null) {
				String batchFetchKeyPaths = (String)_batchFetch.valueInComponent(wocomponent);
				if (batchFetchKeyPaths != null) {
					NSArray<String> keyPaths = NSArray.componentsSeparatedByString(batchFetchKeyPaths, ",");
					if (keyPaths.count() > 0) {
						ERXBatchFetchUtilities.batchFetch((NSArray)list, keyPaths, true);
					}
				}
			}
			ERXDatabaseContextDelegate.setCurrentBatchObjects((NSArray)list);
		}
		return new Context(list);
	}

	@Override
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

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		Context repetitionContext = createContext(wocomponent);

		int count = _count(repetitionContext, wocomponent);

		WOActionResults woactionresults = null;
		String indexString = _indexOfChosenItem(worequest, wocontext);

		int index = 0;
		boolean checkHashCodes = checkHashCodes(wocomponent);

		if (indexString != null && ! checkHashCodes) {
			index = Integer.parseInt(indexString);
		}
		
		if (indexString != null) {
			if (_item != null) {
				Object object = null;
				if (checkHashCodes) {
					boolean found = false;
					
					if (_uniqueKey == null) {
						int hashCode = Integer.parseInt(indexString);
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
						if (! found) log.warn("Wrong object: " + otherHashCode + " vs " + hashCode + " (array = " + repetitionContext.nsarray + ")");
						if (found && log.isDebugEnabled()) log.debug("Found object: " + otherHashCode + " vs " + hashCode);
					}
					else {
						String key = indexString;
						String otherKey = null;
						for (int i = 0; i < repetitionContext.count() && !found; i++) {
							Object o = repetitionContext.objectAtIndex(i);
							otherKey = keyForObject(wocomponent, o);
							if (otherKey.equals(key)) {
								object = o;
								index = i;
								found = true;
							}
						}
						if (! found) log.warn("Wrong object: " + otherKey + " vs " + key + " (array = " + repetitionContext.nsarray + ")");
						if (found && log.isDebugEnabled()) log.debug("Found object: " + otherKey + " vs " + key);
					}

					if (!found) {
						if (raiseOnUnmatchedObject(wocomponent)) {
							throw new UnmatchedObjectException();
						}
						if (_notFoundMarker == null) {
							return wocontext.page();
						}
						object = _notFoundMarker.valueInComponent(wocomponent);
					}
				}
				else {
					if (index >= repetitionContext.count()) {
						if (raiseOnUnmatchedObject(wocomponent)) {
							throw new UnmatchedObjectException();
						}
						return wocontext.page();
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
			for (int i = 0; i < count && woactionresults == null; i++) {
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
	
	@Override
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
			appendChildrenToResponse(woresponse, wocontext);
		}
		if (count > 0) {
			_cleanupAfterIteration(count, wocontext, wocomponent);
		}
	}
}
