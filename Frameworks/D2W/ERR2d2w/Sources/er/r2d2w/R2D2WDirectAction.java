package er.r2d2w;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERD2WDirectAction;
import er.directtoweb.pages.ERD2WListPage;
import er.extensions.appserver.ERXDirectAction;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

public class R2D2WDirectAction extends ERXDirectAction {
    protected static final Logger log = LoggerFactory.getLogger(ERD2WDirectAction.class);
	
    public static final String AUX_QUALIFIER_KEY = "aux";
	public static final String FETCH_LIMIT_KEY = "fl";
	public static final String FETCHSPEC_NAME_KEY = "fs";
	public static final String KEY_PATH_KEY = "kp";
	public static final String LANGUAGE_KEY = "lang";
	public static final String PRIMARY_KEY_KEY = "pk";
	public static final String SORT_ORDERINGS_KEY = "so";
	public static final String USES_DISTINCT_KEY = "fd";
	
	// Qualifier keys
	public static final String QUALIFIER_LHS_KEY = "k";
	public static final String QUALIFIER_OP_KEY = "o";
	public static final String QUALIFIER_RHS_KEY = "v";
	public static final String QUALIFIER_TYPE_KEY = "t";
	
	// Compound qualifiers
	public static final String QUALIFIER_KIND_AND = "A";
	public static final String QUALIFIER_KIND_NOT = "N";
	public static final String QUALIFIER_KIND_OR = "O";
	
	// Simple qualifiers
	public static final String QUALIFIER_KIND_KEY_VALUE = "KV";
	public static final String QUALIFIER_KIND_KEY_COMPARISON = "KC";
	
	public static final String QUALIFIER_PATH_SEPARATOR = NSKeyValueCodingAdditions.KeyPathSeparator;
	
	public static final String DATE_FORMAT = "MM-dd-yyyy HH:mm:ss z";
	
	// Short names for selectors
	public static final NSDictionary<String, NSSelector<Boolean>> OP_KEYS = 
		new NSDictionary<String, NSSelector<Boolean>>(
			new NSSelector[] {
				EOQualifier.QualifierOperatorCaseInsensitiveLike,
				EOQualifier.QualifierOperatorContains,
				EOQualifier.QualifierOperatorEqual,
				EOQualifier.QualifierOperatorGreaterThan,
				EOQualifier.QualifierOperatorGreaterThanOrEqualTo,
				EOQualifier.QualifierOperatorLessThan,
				EOQualifier.QualifierOperatorLessThanOrEqualTo,
				EOQualifier.QualifierOperatorLike,
				EOQualifier.QualifierOperatorNotEqual}, 
			new String[] {
				"ILIKE",
				"CONTAINS",
				"EQ",
				"GT",
				"GTE",
				"LT",
				"LTE",
				"LIKE",
				"NE"}
		);

	public static final NSDictionary<String, NSSelector> SORT_KEYS = 
		new NSDictionary<String, NSSelector>(
			new NSSelector[] {
				EOSortOrdering.CompareAscending,
				EOSortOrdering.CompareCaseInsensitiveAscending,
				EOSortOrdering.CompareCaseInsensitiveDescending,
				EOSortOrdering.CompareDescending
			}, 
			new String[] {
				"ASC",
				"IASC",
				"IDSC",
				"DSC"
			}
		);

	public R2D2WDirectAction(WORequest r) {
		super(r);
	}
	
	private static NSDictionary<String, String> langaugeCodes;
	
	private NSDictionary<String, String> languageCodes() {
		if(langaugeCodes == null) {
			langaugeCodes = ERXDictionaryUtilities.dictionaryFromPropertyList("Languages", NSBundle.bundleForName("JavaWebObjects"));
		}
		return langaugeCodes;
	}

	
    public WOActionResults performActionNamed(String actionName) {
    	WOActionResults result = null;
    	
    	if(ERXLocalizer.isLocalizationEnabled() && context().hasSession()) {
    		ERXSession session = (ERXSession)session();
    		String languageCode = context().request().stringFormValueForKey(LANGUAGE_KEY);
    		String localizerCode = session.localizer().languageCode();
    		
    		//TODO this should redirect to default lang if !availableLangs.containsKey(lang)
    		//also should attach a language if one is missing.
    		if(languageCode != null && !localizerCode.equals(languageCode) && languageCodes().containsKey(languageCode)) {
    			String lang = (String)languageCodes().get(languageCode);
    			session.setLanguage(lang);
    		}
    	}
    	
    	try {
    		result = super.performActionNamed(actionName);
    	} catch (NSForwardException fe) {
    		if(!(fe.originalException() instanceof NoSuchMethodException)) {throw fe;}
    	} catch (Exception e) {
    		if(!(e instanceof NoSuchMethodException)) {e.printStackTrace();}
    	}
    	
    	if(result == null) {
            if(allowPageConfiguration(actionName)) {
                result = dynamicPageForActionNamed(actionName);
            } else {
                result = forbiddenAction();
            }
        }
    	
    	return result;
    }

    public WOActionResults dynamicPageForActionNamed(String actionName) {
        WOComponent result = null;

        try {
            result = D2W.factory().pageForConfigurationNamed(actionName, session());
        } catch (IllegalStateException ex) {
            // this will get thrown when a page simply isn't found. We don't really need to report it
            log.debug("dynamicPageForActionNamed failed for Action:" + actionName, ex);
            return null;
        }

        D2WContext c = null; 
        if(result instanceof D2WPage) {
            c = ((D2WPage)result).d2wContext();
        } else {
            c = ERD2WContext.newContext(session());
            c.setDynamicPage(actionName);
        }
        EOEntity entity = (EOEntity)c.entity();

        if(entity != null) {
            String entityName = entity.name();

            if(result instanceof InspectPageInterface) {
                prepareInspectPage(c, (InspectPageInterface)result, entityName);
            } else if(result instanceof ListPageInterface) {
                prepareListPage(c, (ListPageInterface)result, entityName);
            }
        }
        return result;
    }

    protected void prepareInspectPage(D2WContext c, InspectPageInterface ipi, String entityName) {
        EOEditingContext ec = session().defaultEditingContext();
        try {
        	NSDictionary<String, Object> pkDict = primaryKeyFromRequest(ec, entityName);
        	if(pkDict.count() == 1) {
	        	EOEnterpriseObject eo = EOUtilities.objectWithPrimaryKey(ec, entityName, pkDict);
	        	ipi.setObject(eo);
        	}
        } catch (EOObjectNotAvailableException e) {
        	// No object found. Nothing to set.
        	log.debug(e.getMessage());
        }
    }

    protected void prepareListPage(D2WContext c, ListPageInterface lpi, String entityName) {
        EOEditingContext ec = session().defaultEditingContext();
        WORequest r = context().request();
        EODataSource ds = null;
        
        // If there's a keypath, we need a EODetailDataSource
        String keyPath = r.stringFormValueForKey(KEY_PATH_KEY);
        if(keyPath!=null) {
        	int indexOfDot = keyPath.indexOf(QUALIFIER_PATH_SEPARATOR);
        	if(indexOfDot > 0) {
                String masterEntity = keyPath.substring(0, indexOfDot);
                String detailKey = keyPath.substring(indexOfDot+1, keyPath.length());
                NSDictionary<String, Object> primaryKey = primaryKeyFromRequest(ec, masterEntity);
                EOEnterpriseObject masterObject = EOUtilities.objectWithPrimaryKey(ec, masterEntity, primaryKey);
                EOClassDescription mecd = EOClassDescription.classDescriptionForEntityName(masterEntity);
                EODetailDataSource dds = new EODetailDataSource(mecd, detailKey);
                dds.qualifyWithRelationshipKey(detailKey, masterObject);
                ds = dds;
        	}
        	
            NSArray<EOSortOrdering> orderings = sortOrderingsFromRequest(r);
            if(orderings.count() > 0) {
            	WODisplayGroup dg = ((ERD2WListPage)lpi).displayGroup();
            	dg.setSortOrderings(orderings);
            }        	
        }
        
        // Failing that, create a EODatabaseDataSource
        if(ds == null) {
            EODatabaseDataSource dbds = new EODatabaseDataSource(ec, entityName);
            
            EOQualifier aux = auxQualifierFromRequest(entityName, r);
            dbds.setAuxiliaryQualifier(aux);
            
            EOFetchSpecification fs = namedFetchSpecificationFromRequest(entityName);
            if(fs == null) {
            	EOQualifier q = qualifierFromRequest(entityName, r);
                NSArray<EOSortOrdering> orderings = sortOrderingsFromRequest(r);
                fs = new EOFetchSpecification(entityName, q, orderings);
            }
            
            int fetchLimit = ERXValueUtilities.intValueWithDefault(c.valueForKey("fetchLimit"), 200);
            fs.setFetchLimit(fetchLimit);
            boolean refresh = ERXValueUtilities.booleanValueWithDefault(c.valueForKey("refreshRefetchedObjects"), false);
            fs.setRefreshesRefetchedObjects(refresh);
            boolean usesDictinct = ERXValueUtilities.booleanValueWithDefault(context().request().stringFormValueForKey(USES_DISTINCT_KEY), true);
			fs.setUsesDistinct(usesDictinct);
            dbds.setFetchSpecification(fs);
            
            ds = dbds;        	
        }
        
        lpi.setDataSource(ds);
    }
    
    public EOFetchSpecification namedFetchSpecificationFromRequest(String entityName) {
    	EOFetchSpecification result = null;
    	String fsName = context().request().stringFormValueForKey(FETCHSPEC_NAME_KEY);
    	if(!ERXStringUtilities.stringIsNullOrEmpty(fsName)) {
    		result = EOFetchSpecification.fetchSpecificationNamed(fsName, entityName);
			NSMutableDictionary<String, String> bindings = new NSMutableDictionary<String, String>();
			for(String key: result.qualifier().bindingKeys()) {
				String formValue = context().request().stringFormValueForKey(key);
				if(!ERXStringUtilities.stringIsNullOrEmpty(formValue) && !formValue.equals(NSKeyValueCoding.NullValue.toString())) {
					bindings.setObjectForKey(formValue, key);
				}
			}
    	}
    	return result;
    }
    
    public NSDictionary<String, Object> primaryKeyFromRequest(EOEditingContext ec, String entityName) {
        String pkString = context().request().stringFormValueForKey(PRIMARY_KEY_KEY);
        return (pkString == null)?new NSDictionary<String, Object>():ERXEOControlUtilities.primaryKeyDictionaryForString(ec, entityName, pkString);
    }

    /**
     * Checks if a page configuration is allowed to render.
     * Override for a more intelligent access scheme as the default just returns true.
     * @param pageConfiguration
     */
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return true;
    }
    
    /**
     * Returns a response with a 401 (access denied) message. Override this for something more user friendly.
     */
    public WOActionResults forbiddenAction() {
        WOResponse response = new WOResponse();
        response.setStatus(401);
        response.setContent("Access denied");
        return response;
    }
    
    public static NSDictionary<String, Object> dictionaryFromSortOrderings(NSArray<? extends EOSortOrdering> orderings) {
    	NSMutableDictionary<String, Object> kvPairs = new NSMutableDictionary<String, Object>();
    	int count = orderings.count();
    	for(int i = 0; i < count; i++) {
    		EOSortOrdering order = orderings.objectAtIndex(i);
    		String selectorKey = SORT_KEYS.allKeysForObject(order.selector()).objectAtIndex(0).toString();
    		kvPairs.put(Integer.toString(i), new StringBuilder(selectorKey).append(NSKeyValueCodingAdditions.KeyPathSeparator).append(order.key()).toString());
    	}
    	return kvPairs;
    }
    
    public static NSArray<EOSortOrdering> sortOrderingsFromRequest(WORequest request) {
    	NSMutableArray<EOSortOrdering> orderings = new NSMutableArray<EOSortOrdering>();
    	StringBuilder sb = new StringBuilder(SORT_ORDERINGS_KEY).append(NSKeyValueCodingAdditions.KeyPathSeparator);
    	int sbLength = sb.length();
    	for(int i = 0; true; i++) {
    		String requestKey = sb.append(i).toString();
    		sb.setLength(sbLength);
    		String soString = request.stringFormValueForKey(requestKey);
    		if(soString == null) { break; }
    		int dotIndex = soString.indexOf(NSKeyValueCodingAdditions.KeyPathSeparator);
    		String selectorKey = soString.substring(0, dotIndex);
    		String sortKey = soString.substring(dotIndex + 1, soString.length());
    		EOSortOrdering order = EOSortOrdering.sortOrderingWithKey(sortKey, SORT_KEYS.get(selectorKey));
    		orderings.add(order);
    	}
    	return orderings.immutableClone();
    }

	public static NSDictionary<String, Object> dictionaryFromQualifier(EOQualifier q) {
		return dictionaryFromQualifier(q, 0);
	}
	
	private static NSDictionary<String, Object> dictionaryFromQualifier(EOQualifier q, int index) {
		NSMutableDictionary<String, Object> result = new NSMutableDictionary<String, Object>();
		
		if(q instanceof EOAndQualifier) {
			NSMutableArray<NSDictionary<String, Object>> qualifierDicts = new NSMutableArray<NSDictionary<String,Object>>();
			NSArray<EOQualifier> qualifiers = ((EOAndQualifier) q).qualifiers();
			for(int i = 0; i < qualifiers.count(); i++) {
				EOQualifier qualifier = qualifiers.objectAtIndex(i);
				qualifierDicts.add(dictionaryFromQualifier(qualifier, i));
			}
			result.put(index + QUALIFIER_PATH_SEPARATOR + QUALIFIER_TYPE_KEY, QUALIFIER_KIND_AND);
			result.put(Integer.toString(index), qualifierDicts);
		
		} else if (q instanceof EOOrQualifier) {
			NSMutableArray<NSDictionary<String, Object>> qualifierDicts = new NSMutableArray<NSDictionary<String,Object>>();
			NSArray<EOQualifier> qualifiers = ((EOOrQualifier) q).qualifiers();
			for(int i = 0; i < qualifiers.count(); i++) {
				EOQualifier qualifier = qualifiers.objectAtIndex(i);
				qualifierDicts.add(dictionaryFromQualifier(qualifier, i));
			}
			result.put(index + QUALIFIER_PATH_SEPARATOR + QUALIFIER_TYPE_KEY, QUALIFIER_KIND_OR);
			result.put(Integer.toString(index), qualifierDicts);
		
		} else if(q instanceof EONotQualifier) {
			result.put(index + QUALIFIER_PATH_SEPARATOR + QUALIFIER_TYPE_KEY, QUALIFIER_KIND_NOT);
			result.put(Integer.toString(index), dictionaryFromQualifier(((EONotQualifier) q).qualifier(), 0));
		//CHECKME make sure this handles null values okay.
		} else if(q instanceof EOKeyValueQualifier) {
			NSMutableDictionary<String, Object> kv = new NSMutableDictionary<String, Object>();
			kv.put(QUALIFIER_LHS_KEY, ((EOKeyValueQualifier) q).key());
			Object val = ((EOKeyValueQualifier) q).value();
			if(val instanceof java.util.Date) {
				val = new SimpleDateFormat(DATE_FORMAT).format((java.util.Date)val);
			}
			kv.put(QUALIFIER_RHS_KEY, val);
			String shortName = OP_KEYS.allKeysForObject(((EOKeyValueQualifier) q).selector()).objectAtIndex(0).toString();
			kv.put(QUALIFIER_OP_KEY, shortName);
			kv.put(QUALIFIER_TYPE_KEY, QUALIFIER_KIND_KEY_VALUE);
			result.put(Integer.toString(index), kv);
		
		} else if(q instanceof EOKeyComparisonQualifier) {
			NSMutableDictionary<String, Object> kc = new NSMutableDictionary<String, Object>();
			kc.put(QUALIFIER_LHS_KEY, ((EOKeyValueQualifier) q).key());
			kc.put(QUALIFIER_RHS_KEY, ((EOKeyValueQualifier) q).value());
			String shortName = OP_KEYS.allKeysForObject(((EOKeyValueQualifier) q).selector()).objectAtIndex(0).toString();
			kc.put(QUALIFIER_OP_KEY, shortName);
			kc.put(QUALIFIER_TYPE_KEY, QUALIFIER_KIND_KEY_COMPARISON);
			result.put(Integer.toString(index), kc);
		}
		
		return result;
	}

	public static EOQualifier qualifierFromRequest(String entityName, WORequest request) {
		return qualifierFromRequest(entityName, request, "0");
	}
	
	public static EOQualifier auxQualifierFromRequest(String entityName, WORequest request) {
		String auxPath = new StringBuilder(AUX_QUALIFIER_KEY).append(QUALIFIER_PATH_SEPARATOR).append(0).toString();
		return qualifierFromRequest(entityName, request, auxPath);
	}
	
	private static EOQualifier qualifierFromRequest(String entityName, WORequest request, String path) {
		EOQualifier result = null;
		String kindPath = new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_TYPE_KEY).toString();
		String qualKind = request.stringFormValueForKey(kindPath);

		if(qualKind==null) { return result;}
			
		if(QUALIFIER_KIND_AND.equals(qualKind) || QUALIFIER_KIND_OR.equals(qualKind)) {
			NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
			int i = 0;
			while(true) {
				String typeKey = new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(i).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_TYPE_KEY).toString();
				if(request.stringFormValueForKey(typeKey)==null) {break;}
				String nextPath = new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(i).toString();
				EOQualifier q = qualifierFromRequest(entityName, request, nextPath);
				if(q!=null){qualifiers.add(q);}
				i++;
			}
			if(qualifiers.count() > 1) {
				result = (QUALIFIER_KIND_AND.equals(qualKind))?new EOAndQualifier(qualifiers):new EOOrQualifier(qualifiers);
			}
			
		} else if(QUALIFIER_KIND_NOT.equals(qualKind)) {
			String typeKey = new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(0).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_TYPE_KEY).toString();
			if(request.stringFormValueForKey(typeKey)!=null) {
				EOQualifier q = qualifierFromRequest(entityName, request, new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(0).toString());
				if(q!=null) {result = new EONotQualifier(q);}
			}
			
		} else if(QUALIFIER_KIND_KEY_COMPARISON.equals(qualKind)) {
			String lhs = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_LHS_KEY).toString());
			String opKey = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_OP_KEY).toString());
			String rhs = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_RHS_KEY).toString());
			
			if(lhs!=null && opKey!=null && OP_KEYS.containsKey(opKey) && rhs!=null) {
				NSSelector<Boolean> op = OP_KEYS.get(opKey);
				EOQualifier q = new EOKeyComparisonQualifier(lhs, op, rhs);
				if(q!=null) {
					// TODO catch errors thrown by invalid qualifiers
					q.validateKeysWithRootClassDescription(EOClassDescription.classDescriptionForEntityName(entityName));
				}
				result = q;
			}
		
		} else if(QUALIFIER_KIND_KEY_VALUE.equals(qualKind)) {
			String lhs = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_LHS_KEY).toString());
			String opKey = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_OP_KEY).toString());
			String rhs = request.stringFormValueForKey(new StringBuilder(path).append(QUALIFIER_PATH_SEPARATOR).append(QUALIFIER_RHS_KEY).toString());

			if(lhs!=null && opKey!=null && OP_KEYS.containsKey(opKey) && rhs!=null) {
				Object val = null;
				NSSelector<Boolean> op = OP_KEYS.get(opKey);
				
				// Attribute value reconstruction.
				if(!NSKeyValueCoding.NullValue.toString().equals(rhs)) {
				    EOEntity entity = ERXEOAccessUtilities.entityNamed(ERXEC.newEditingContext(), entityName);
				    String attributeName = lhs;
				    
				    // If this is a key path, get the destination entity and attribute
				    if(lhs.indexOf(QUALIFIER_PATH_SEPARATOR) > 0) {
    					String pathToAtt = ERXStringUtilities.keyPathWithoutLastProperty(lhs);
    					attributeName = ERXStringUtilities.lastPropertyKeyInKeyPath(lhs);
    					entity = ERXEOAccessUtilities.destinationEntityForKeyPath(entity, pathToAtt);
    				}
				    EOAttribute attr = entity.anyAttributeNamed(attributeName);
				    // Assuming we found an attribute, restore the value
				    if(attr!=null){
				    	val = ERXStringUtilities.attributeValueFromString(attr, rhs, request.formValueEncoding(), new SimpleDateFormat(DATE_FORMAT));
				    }
				    
				    // If the value was restored, create a qualifier
				    if(val != null) {
						result = new EOKeyValueQualifier(lhs, op, val);
				    }
				    
				} else {
					result = new EOKeyValueQualifier(lhs, op, val);
				}
			}
		}

		return result;
	}
	
	public static NSDictionary<String, Object> queryDictionaryForListPage(EODataSource ds, NSArray<EOSortOrdering> orderings) {
		NSDictionary<String, Object> queryDictionary = new NSMutableDictionary<String, Object>();

		if(ds instanceof EODetailDataSource) {
			EODetailDataSource dds = (EODetailDataSource)ds;
			EOEnterpriseObject eo = (EOEnterpriseObject)dds.masterObject();
			String pk = ERXEOControlUtilities.primaryKeyStringForObject(eo);
			queryDictionary.put(R2D2WDirectAction.PRIMARY_KEY_KEY, pk);			
			String keyPath = eo.entityName() + NSKeyValueCodingAdditions.KeyPathSeparator + dds.detailKey();
			queryDictionary.put(R2D2WDirectAction.KEY_PATH_KEY, keyPath);

		} else if (ds instanceof EODatabaseDataSource) {
			EODatabaseDataSource dbds = (EODatabaseDataSource)ds;
			EOFetchSpecification fs = dbds.fetchSpecification();
			EOQualifier q = fs.qualifier();
			if(q!=null) {
				queryDictionary.putAll(R2D2WDirectAction.dictionaryFromQualifier(q));
			}
			EOQualifier aux = dbds.auxiliaryQualifier();
			if(aux!=null) {
				queryDictionary.put(R2D2WDirectAction.AUX_QUALIFIER_KEY, R2D2WDirectAction.dictionaryFromQualifier(aux));
			}

		} else {
			log.debug("EODataSource: " + ds);
		}

		if(ERXLocalizer.isLocalizationEnabled()) {
			queryDictionary.put(R2D2WDirectAction.LANGUAGE_KEY, ERXLocalizer.currentLocalizer().languageCode());
		}
		
		if(orderings != null && orderings.count() > 0) {
			queryDictionary.put(R2D2WDirectAction.SORT_ORDERINGS_KEY,R2D2WDirectAction.dictionaryFromSortOrderings(orderings));
		}
		
		return queryDictionary.immutableClone();
	}

	// Not <String, String> because context() da methods require <Sting, Object>...
	public static NSDictionary<String, Object> inspectQueryDictionary(EOEnterpriseObject eo) {
		NSDictionary<String, Object> result = new NSDictionary<String, Object>();
		if(eo != null) {
			String pk = ERXEOControlUtilities.primaryKeyStringForObject(eo);
			result = inspectQueryDictionaryForKey(pk);
		}
		return result;
    }
	
	public static NSDictionary<String, Object> inspectQueryDictionaryForKey(String pk) {
		NSMutableDictionary<String, Object> result = new NSMutableDictionary<String, Object>();
		result.put(R2D2WDirectAction.PRIMARY_KEY_KEY, pk);
		if (ERXLocalizer.isLocalizationEnabled()) {
			result.put(R2D2WDirectAction.LANGUAGE_KEY, ERXLocalizer.currentLocalizer().languageCode());
		}
		return result.immutableClone();
	}

    /**
     * Overrides super class method to return an XHTML compliant configuration page.
     * @return {@link R2DLog4JConfiguration} for modifying current logging settings.
     */
    public WOComponent log4jAction() {
        WOComponent result=null;
        if (canPerformActionWithPasswordKey("er.extensions.ERXLog4JPassword")) {
            	result=D2W.factory().pageForConfigurationNamed("Log4JConfiguration", session());//pageWithName(R2DLog4JConfiguration.class);
            	session().setObjectForKey(Boolean.TRUE, "ERXLog4JConfiguration.enabled");
        }
        return result;
    }


}
