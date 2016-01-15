package er.extensions.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOKeyValueAssociation;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXUtilities;

/**
 * This is an effort to consolidate the WOToOneRelationship, WOToManyRelationship and descendant components.
 * <p>
 * As most of the code between the two is shared anyway, it makes sense to provide a base class and only
 * handle the differences in the descendants. One core difference if that this component can handle POJOs both as the
 * source and the destination objects. You can't instantiate one of these yourself.
 * <p>
 * This class can handle to-one, to-many and simple attribute selections. You can can set the list via 
 * possibleChoices, dataSource, destinationEntityName or via sourceEntityName and relationshipKey.
 * <p>
 * The main difference between this component and the former WOToOne/WOToMany is that it is non-synchronizing. So if
 * you have custom subclasses of WOToOne/WOToMany you need to take this into account.
 * Also adds the values that are not included in the restricted-choice list. These items are marked by [name of item]. 
 * This should ensure they end up at the bottom of the list.
 * You can also specify the editingContext the component uses to fetch the related objects into.
 * NOTE: currently "includeUnmatchedValues" is set to false
 * @author ak (but most stuff is pulled over from the pre-existing WOToOne/WOToMany)
 */
public abstract class ERXArrayChooser extends ERXStatelessComponent {
	private static final Logger log = LoggerFactory.getLogger(ERXArrayChooser.class);

	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static boolean localizeDisplayKeysDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXArrayChooser.localizeDisplayKeysDefault", false);
    public static boolean includeUnmatchedValuesDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXArrayChooser.includeUnmatchedValuesDefault", false);
    public static boolean sortCaseInsensitiveDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXArrayChooser.sortCaseInsensitive", false);
       
    protected final static String NO_SELECTION_STRING = "ERXArrayChooser.NoSelectionString";
    protected final static String NO_SORT_STRING = "WONoSorting";
    
    protected Boolean _localizeDisplayKeys;
    protected Boolean _includeUnmatchedValues;
    protected Boolean _sortCaseInsensitive;

    protected String _sourceEntityName;
    protected String _destinationEntityName;
    protected String _relationshipKey;
    protected Object _sourceObject;

    protected String _destinationDisplayKey;
    protected EODataSource _dataSource;
    protected String _uiStyle;
    protected Boolean _isMandatory;
    protected NSArray _list;
    protected NSArray _unmatchedValues;
 
    protected String _destinationSortKey;
    protected String _noneString;

    protected Object theCurrentItem;

    public ERXArrayChooser(WOContext aContext) {
        super(aContext);
    }

    protected abstract boolean isSingleSelection();

    @Override
    public void reset() {
        super.reset();
        _sourceEntityName = null;
        _destinationEntityName = null;
        _relationshipKey = null;
        _sourceObject = null;
        
        _destinationDisplayKey = null;
        _dataSource = null;
        _uiStyle = null;
        _isMandatory = null;
        _list = null;

        _destinationSortKey = null;
        _noneString = null;
        _localizeDisplayKeys = null;
        _sortCaseInsensitive = null;
        _includeUnmatchedValues = null;
        _unmatchedValues = null;
    }

    public String noneString() {
        if(_noneString == null) {
            _noneString = (String)valueForBinding("noSelectionString");
            if(_noneString == null) {
                _noneString = "ERXArrayChooser.noneString";
            }
            if(_noneString != null) {
            	_noneString = localizer().localizedStringForKeyWithDefault(_noneString);
            }
        }
        return _noneString;
    }

    public boolean sortCaseInsensitive() {
        if(_sortCaseInsensitive == null) {
        	_sortCaseInsensitive = booleanValueForBinding("sortCaseInsensitive", false) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _sortCaseInsensitive.booleanValue();
    }

    public boolean localizeDisplayKeys() {
        if(_localizeDisplayKeys == null) {
            _localizeDisplayKeys = booleanValueForBinding("localizeDisplayKeys", localizeDisplayKeysDefault) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _localizeDisplayKeys.booleanValue();
    }

    public boolean includeUnmatchedValues() {
        if(_includeUnmatchedValues == null) {
        	_includeUnmatchedValues = booleanValueForBinding("includeUnmatchedValues", includeUnmatchedValuesDefault) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _includeUnmatchedValues.booleanValue();
    }

    public String sourceEntityName() {
        if(_sourceEntityName == null) {
            _sourceEntityName = (String)valueForBinding("sourceEntityName");
            if (_sourceEntityName == null) {
            	loadBindingsFromSelection();
            }
        }
        return _sourceEntityName;
    }
 
    public String destinationSortKey() {
        if (_destinationSortKey == null) {
            _destinationSortKey = (String)valueForBinding("destinationSortKey");
            if (_destinationSortKey == null|| _destinationSortKey.length() == 0) {
                _destinationSortKey = destinationDisplayKey();
            } else if (NO_SORT_STRING.equals(_destinationSortKey)) {
            	_destinationSortKey = null;
            }
        }
        return _destinationSortKey;
    }
    
    public NSArray unmatchedValues() {
    	return _unmatchedValues;
    }
    
    protected NSArray destinationSortKeys() {
        String destinationSortKey = destinationSortKey();
        NSArray destinationSortKeys;
        if (destinationSortKey != null) {
        	destinationSortKeys = NSArray.componentsSeparatedByString(destinationSortKey, ",");
        }
        else {
        	destinationSortKeys = NSArray.EmptyArray;
        }
        return destinationSortKeys;
    }

    public EOEditingContext editingContext() {
        EOEditingContext ec = null;
        if(sourceObject() instanceof EOEnterpriseObject) {
        	ec = ((EOEnterpriseObject)sourceObject()).editingContext();
        } else {
        	ec = (EOEditingContext) valueForBinding("editingContext");
        	if(ec == null) {
        		ec = session().defaultEditingContext();
        	}
        }
        return ec;
    }
    
    /**
     * <p>
     * I'm lazy. I don't want to bind sourceObject, sourceEntityName, and relationshipKey.
     * Work it out, Wonder, that's what I say.  So if you bind, for instance:
     * </p>
     * 
     * <code>
     * selection = person.company;
     * </code>
     * 
     * <p>
     * ... it will figure out that the sourceObject is "person", the relationshipKey is "company"
     * and the sourceEntityName is "Person".
     * </p>
     */
    protected void loadBindingsFromSelection() {
    	WOKeyValueAssociation selectionAssociation = (WOKeyValueAssociation)_associationWithName("selection");
    	if (selectionAssociation != null) {
	    	String selectionKeyPath = selectionAssociation.keyPath();
	    	WOComponent parent = parent();
	    	int lastDotIndex = selectionKeyPath.lastIndexOf('.');
	    	if (lastDotIndex == -1) {
	    		_sourceObject = parent;
	    		_relationshipKey = selectionKeyPath;
	    	}
	    	else {
	    		String sourceObjectKeyPath = selectionKeyPath.substring(0, lastDotIndex);
	    		_sourceObject = parent.valueForKeyPath(sourceObjectKeyPath);
	    		_relationshipKey = selectionKeyPath.substring(lastDotIndex + 1);
		    	if (_sourceObject instanceof EOEnterpriseObject) {
		    		_sourceEntityName = ((EOEnterpriseObject)_sourceObject).entityName();
		    	}
	    	}
    	}
    }


    protected EOEntity destinationEntity() {
        return ERXEOAccessUtilities.entityNamed(editingContext(), destinationEntityName());
    }

    public String destinationEntityName() {
    	return _destinationEntityName(true);
    }
    
    public String _destinationEntityName(boolean throwExceptionIfMissing) {
        if(_destinationEntityName == null) {
            _destinationEntityName = (String)valueForBinding("destinationEntityName");
            if(_destinationEntityName == null) {
                Object _source = sourceObject();
                EOEditingContext ec = editingContext();
                EOEntity destinationEntity = null;
                
                if(_source instanceof EOEnterpriseObject) {
                    EORelationship relationship = ERXUtilities.relationshipWithObjectAndKeyPath((EOEnterpriseObject)_source, relationshipKey());
                    
                    destinationEntity = relationship != null ? relationship.destinationEntity() : null;
                } else {
                    String anEntityName = sourceEntityName();
                    if(anEntityName != null) {
                        EOEntity anEntity = ERXEOAccessUtilities.entityNamed(ec, anEntityName);
                        if (anEntity == null) {
                            throw new IllegalStateException("<" + getClass().getName() + " could not find entity named " + anEntityName + ">");
                        }
                        destinationEntity = ERXEOAccessUtilities.destinationEntityForKeyPath(anEntity, relationshipKey());
                    } else {
                    	// MS: This seems bogus -- anEntityName is null here!
                        destinationEntity = ERXEOAccessUtilities.entityNamed(ec, anEntityName);
                    }
                }
                if(destinationEntity == null && throwExceptionIfMissing) {
                    throw new IllegalStateException("Destination entity could not be retrieved from EO of bindings. Either set the \"sourceObject\" to an EO, provide the \"sourceEntityName\" and \"relationshipKey\", the \"destinationEntityName\" or the \"possibleChoices\" binding.");
                }
                if (destinationEntity != null) {
                	_destinationEntityName = destinationEntity.name();
                }
            }
        }
        return _destinationEntityName;
    }
    
    public String relationshipKey() {
        if(_relationshipKey == null) {
            _relationshipKey = (String)valueForBinding("relationshipKey");
            if (_relationshipKey == null) {
            	loadBindingsFromSelection();
            }
        }
        return _relationshipKey;
    }

    public Object sourceObject() {
        if(_sourceObject == null) {
            _sourceObject = valueForBinding("sourceObject");
            if(_sourceObject == null) {
            	loadBindingsFromSelection();
            	if (_sourceObject == null) {
            		throw new IllegalStateException("sourceObject is a required binding.");
            	}
            }
        }
        return _sourceObject;
    }

    public String destinationDisplayKey() {
        if(_destinationDisplayKey == null) {
            _destinationDisplayKey = (String)valueForBinding("destinationDisplayKey");
            if(_destinationDisplayKey == null) {
            	if (_destinationEntityName(false) != null) {
            		_destinationDisplayKey = "userPresentableDescription";
            	}
            }
        }
        return _destinationDisplayKey;
    }
    
    public EOQualifier qualifier() {
    	return (EOQualifier)valueForBinding("qualifier");
    }

    public EODataSource dataSource() {
        if(_dataSource == null) {
            _dataSource = (EODataSource)valueForBinding("dataSource");
        	EOQualifier qualifier = qualifier();
            if (_dataSource == null) {
            	String destinationEntityName = destinationEntityName();
            	EOEditingContext editingContext = editingContext();
            	if (ERXEOAccessUtilities.entityWithNamedIsShared(editingContext(), destinationEntityName) ) {
            	 	EOArrayDataSource arrayDataSource = new EOArrayDataSource(destinationEntity().classDescriptionForInstances(), editingContext);
            	 	NSArray sharedEOs = ERXEOControlUtilities.sharedObjectsForEntityNamed(destinationEntityName);
            	 	
            	 	if (sharedEOs == null) {
            	 		sharedEOs = NSArray.EmptyArray;
            	 	}
            	 	else if (qualifier != null) {
            	 		sharedEOs = EOQualifier.filteredArrayWithQualifier(sharedEOs, qualifier);
            	 	}
            	 	
            	 	arrayDataSource.setArray(sharedEOs);
            	 	_dataSource = arrayDataSource;
            	}
            	else {
            		_dataSource = new EODatabaseDataSource(editingContext, destinationEntityName);
            		if (qualifier != null) {
            			((EODatabaseDataSource)_dataSource).setAuxiliaryQualifier(qualifier);
            		}
            	}
            }       
            else if (qualifier != null) {
            	throw new IllegalArgumentException("You specified a dataSource binding and a qualifier, which is not currently supported.");
            }
        }
        return _dataSource;
    }
    
    public String uiStyle() {
        if(_uiStyle == null) {
            _uiStyle = (String)valueForBinding("uiStyle");
            if(_uiStyle == null) {
                int aSize = theList().count();
                if(isSingleSelection()) {
                    if (aSize <= 5) {
                        _uiStyle = "radio";
                    }
                    if ((aSize >= 5) && (aSize < 20)) {
                        _uiStyle = "popup";
                    }
                    if (aSize >= 20) {
                        _uiStyle = "browser";
                    }
                } else {
                    if (aSize <= 5) {
                        _uiStyle = "checkbox";
                    }
                    if (aSize > 5) {
                        _uiStyle = "browser";
                    }
                }
            }
        }
        return _uiStyle;
    }

    public boolean isMandatory() {
        if(_isMandatory == null) {
            _isMandatory = booleanValueForBinding("isMandatory") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _isMandatory.booleanValue();
    }
    
    public boolean isCheckBox() {
        return uiStyle().equals("checkbox");
    }
    public boolean isRadio() {
        return uiStyle().equals("radio");
    }

    public boolean isPopup() {
        return uiStyle().equals("popup");
    }

    public boolean isBrowser() {
        return uiStyle().equals("browser");
    }


    public Object theCurrentItem() {
        return theCurrentItem;
    }

    public void setTheCurrentItem(Object aValue) {
        theCurrentItem = aValue;
        if (hasBinding("item")) {
        	setValueForBinding(theCurrentItem, "item");
        }
    }

    public abstract NSArray currentValues();
     
    public NSArray theList() {
        if (_list==null) {
            if(hasBinding("possibleChoices")) {
                _list = (NSArray)valueForBinding("possibleChoices");
                if(_list != null && _list.lastObject() instanceof EOEnterpriseObject) {
                    _list = ERXEOControlUtilities.localInstancesOfObjects(editingContext(), _list);
                }
            }
            if(_list == null) {
            	EODataSource ds = dataSource();
            	if(ds.editingContext()!=null) {
            		_list = ds.fetchObjects();
            		if(ds.editingContext() != editingContext()) {
            			_list = ERXEOControlUtilities.localInstancesOfObjects(editingContext(), _list);
            		}
            	} else {
            		log.error("EC of datasource is null, possible resubmit: {}", ERXApplication.erxApplication().extraInformationForExceptionInContext(null, context()));
            		_list = NSArray.EmptyArray;
            	}
            }
            NSArray destinationSortKeys = destinationSortKeys();
            NSSelector sorting = (sortCaseInsensitive() ? EOSortOrdering.CompareAscending : EOSortOrdering.CompareCaseInsensitiveAscending);
            if (destinationSortKeys != null && destinationSortKeys.count() > 0) {
	            _list = ERXArrayUtilities.sortedArraySortedWithKeys(_list, destinationSortKeys, sorting);
            }
            if(includeUnmatchedValues()) {
            	NSArray currentValues = currentValues();
            	if(currentValues.count() > 0) {
            		_unmatchedValues = ERXArrayUtilities.arrayMinusArray(currentValues(), _list);
            		
            		if(_unmatchedValues.count() > 0) {
            			_unmatchedValues = ERXArrayUtilities.arrayMinusArray(_unmatchedValues, new NSArray(NO_SELECTION_STRING));
                		if(_unmatchedValues.lastObject() instanceof EOEnterpriseObject) {
            				_unmatchedValues = ERXEOControlUtilities.localInstancesOfObjects(editingContext(), _unmatchedValues);
            			}
                        if (destinationSortKeys != null && destinationSortKeys.count() > 0) {
                        	_unmatchedValues = ERXArrayUtilities.sortedArraySortedWithKeys(_unmatchedValues, destinationSortKeys, sorting);
                        }
            			_list = _list.arrayByAddingObjectsFromArray(_unmatchedValues);
            		}
            	} else {
            		_unmatchedValues = NSArray.EmptyArray;
            	}
            }
        }
        return _list;
    }

    public Object theCurrentValue() {
        // handle the case where it's the - none - string
        Object currentValue;
        if (theCurrentItem==NO_SELECTION_STRING) {
            currentValue = noneString();
        } else if (hasBinding("displayString")) {
            currentValue = valueForBinding("displayString");
        }
        else {
            currentValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(theCurrentItem, destinationDisplayKey());
        }
        if(localizeDisplayKeys() && currentValue != null && theCurrentItem!=NO_SELECTION_STRING) {
            currentValue = localizer().localizedStringForKeyWithDefault(currentValue.toString());
        }
        if(includeUnmatchedValues() && theCurrentItem!=NO_SELECTION_STRING && unmatchedValues().containsObject(theCurrentItem)) {
        	currentValue = "[" + currentValue + "]";
        }
        return currentValue;
    }
    

    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        // we want to pass the validation here for the case where we are creating a new object
        // and are given isMandatory=0 on a mandatory relationship to force users to pick one..
        super.takeValuesFromRequest(r, c);
        
        if (c.wasFormSubmitted()) {
          Object realSource = realSourceObject();
          if(realSource instanceof EOEnterpriseObject) {
              EOEnterpriseObject localObject = (EOEnterpriseObject)realSource;
              String realRelationshipKey = realRelationshipKey();
              Object value = localObject.valueForKeyPath(realRelationshipKey);
              try {
                  localObject.validateValueForKey(value, realRelationshipKey);
              } catch (NSValidation.ValidationException eov) {
                  parent().validationFailedWithException(eov, value, relationshipKey());
              }
          }
        }
    }
    
    protected Object realSourceObject() {
        Object realSourceObject = sourceObject();
        //NOTE ak: this check is needed if we are used in a query binding and want to query a keyPath
        if(realSourceObject instanceof EOEnterpriseObject) {
            String masterKey = relationshipKey();
            if(masterKey.indexOf('.') != -1) {
                String partialPath = ERXStringUtilities.keyPathWithoutLastProperty(masterKey);
                realSourceObject = NSKeyValueCodingAdditions.Utility.valueForKeyPath(realSourceObject, partialPath);
            }
        }
        return realSourceObject;
    }
    
    protected String realRelationshipKey() {
        if(sourceObject() instanceof EOEnterpriseObject) {
            return ERXStringUtilities.lastPropertyKeyInKeyPath(relationshipKey());
        }
        return relationshipKey();
    }
}