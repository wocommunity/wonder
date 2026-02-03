package er.r2d2w.delegates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.ERD2WContainer;
import er.directtoweb.delegates.ERDBranchDelegate;
import er.directtoweb.delegates.ERDBranchInterface;
import er.directtoweb.delegates.ERDQueryValidationDelegate;
import er.directtoweb.interfaces.ERDErrorPageInterface;
import er.directtoweb.interfaces.ERDListPageInterface;
import er.directtoweb.pages.ERD2WInspectPage;
import er.directtoweb.pages.ERD2WPage;
import er.directtoweb.pages.ERD2WQueryPage;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;
import er.r2d2w.pages.R2D2WInspectPage;

public class R2DDefaultBranchDelegate extends ERDBranchDelegate {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(R2DDefaultBranchDelegate.class);
	
	public boolean shouldSaveChanges(D2WContext c) { return ERXValueUtilities.booleanValue(c.valueForKey("shouldSaveChanges")); }
	public boolean shouldValidateBeforeSave(D2WContext c) { return ERXValueUtilities.booleanValue(c.valueForKey("shouldValidateBeforeSave")); }
	public boolean shouldRecoverFromOptimisticLockingFailure(D2WContext c) { return ERXValueUtilities.booleanValueWithDefault(c.valueForKey("shouldRecoverFromOptimisticLockingFailure"), false); }
	public boolean shouldRevertUponSaveFailure(D2WContext c) { return ERXValueUtilities.booleanValueWithDefault(c.valueForKey("shouldRevertUponSaveFailure"), false); }

    public WOComponent _cancelEdit(WOComponent sender) {
		EOEnterpriseObject eo = object(sender);
		ERD2WInspectPage page = ERD2WUtilities.enclosingComponentOfClass(sender, ERD2WInspectPage.class);
		EOEditingContext ec = eo != null?eo.editingContext():null;
		if (ec != null && page.shouldRevertChanges()) {
            ec.revert();
        }
        return page.nextPage(false);
	}

	public WOComponent _selectAll(WOComponent sender) {
		ERDListPageInterface page = ERD2WUtilities.enclosingComponentOfClass(sender, ERDListPageInterface.class);
		WODisplayGroup dg = page.displayGroup();
		dg.setSelectedObjects(dg.allObjects());
		return sender.context().page();
	}

	public WOComponent _selectNone(WOComponent sender) {
		ERDListPageInterface page = ERD2WUtilities.enclosingComponentOfClass(sender, ERDListPageInterface.class);
		page.displayGroup().setSelectedObjects(NSArray.EmptyArray);
		return sender.context().page();
	}

	public WOComponent _confirmCancelEdit(WOComponent sender) {
		//TODO
		return null;
	}
	
	public WOComponent _confirmDelete(WOComponent sender) {
		//TODO
		return null;
	}
	
	public WOComponent _confirmSave(WOComponent sender) {
		//TODO
		return null;
	}
	
	public WOComponent _create(WOComponent sender) {
		WOComponent nextPage = null;
		try {
			D2WContext c = d2wContext(sender);
			EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(c.entity().name(), sender.session());
			epi.setNextPage(sender.context().page());
			nextPage = (WOComponent)epi;
		} catch(IllegalArgumentException e) {
			ErrorPageInterface err = D2W.factory().errorPage(sender.session());
			err.setMessage(e.toString());
			err.setNextPage(sender.context().page());
			nextPage = (WOComponent)err;
		}
		return nextPage;
	}
	
	public WOComponent _delete(WOComponent sender) {
		return _deleteObject(sender, sender.context().page());
	}
	
	public WOComponent _deleteReturn(WOComponent sender) {
		WOComponent nextPage = _deleteObject(sender, _return(sender));
		return nextPage;
	}
	
	public WOComponent _edit(WOComponent sender) {
		EOEnterpriseObject obj = object(sender);
		if(obj == null) { return sender.context().page(); }

		Object value = d2wContext(sender).valueForKey("useNestedEditingContext");
    	boolean createNestedContext = ERXValueUtilities.booleanValue(value);
    	EOEnterpriseObject eo = ERXEOControlUtilities.editableInstanceOfObject(obj, createNestedContext);
    	
    	EditPageInterface epi = D2W.factory().editPageForEntityNamed(eo.entityName(), sender.session());
    	epi.setObject(eo);
        epi.setNextPage(sender.context().page());
        eo.editingContext().hasChanges(); // Ensuring it survives.
        return (WOComponent)epi;
	}
	
	public WOComponent _inspect(WOComponent sender) {
		EOEnterpriseObject eo = object(sender);
		if(eo == null) { return sender.context().page(); }

		InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed(eo.entityName(), sender.session());
		ipi.setObject(eo);
		ipi.setNextPage(sender.context().page());
		eo.editingContext().hasChanges(); // Ensuring it survives.
		return (WOComponent)ipi;
	}
	
	public void _nextStep(WOComponent sender) {
		R2D2WInspectPage page = ERD2WUtilities.enclosingComponentOfClass(sender, R2D2WInspectPage.class);
		NSArray<ERD2WContainer> tabs = page.tabSectionsContents();
		ERD2WContainer tab = page.currentTab();
		int index = tabs.indexOf(tab);
		if(page.errorMessages().isEmpty() && index + 1 < tabs.count()) {
			ERD2WContainer next = tabs.objectAtIndex(index + 1);
			page.setCurrentTab(next);
		}
	}
	
	public void _prevStep(WOComponent sender) {
		R2D2WInspectPage page = ERD2WUtilities.enclosingComponentOfClass(sender, R2D2WInspectPage.class);
		NSArray<ERD2WContainer> tabs = page.tabSectionsContents();
		ERD2WContainer tab = page.currentTab();
		int index = tabs.indexOf(tab);
		if(index != 0) {
			page.clearValidationFailed();
			ERD2WContainer prev = tabs.objectAtIndex(index - 1);
			page.setCurrentTab(prev);
		}
		
	}

	public WOComponent _query(WOComponent sender) {
		ERD2WQueryPage page = ERD2WUtilities.enclosingComponentOfClass(sender, ERD2WQueryPage.class);
        WOComponent nextPage = null;

        // If we have a validation delegate, validate the query values before actually performing the query.
        ERDQueryValidationDelegate queryValidationDelegate = page.queryValidationDelegate();
        if (queryValidationDelegate != null) {
//        	page.clearValidationFailed();
//        	page.setErrorMessage(null);
            try {
                queryValidationDelegate.validateQuery(page);
            } catch (NSValidation.ValidationException ex) {
    			page.setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotQuery", ex));
    			page.validationFailedWithException(ex, null, "queryExceptionKey");
    		}
        }

        if (page.hasErrors()) {
            return sender.context().page();
        }

        D2WContext c = d2wContext(sender);
        if (ERXValueUtilities.booleanValue(c.valueForKey("showListInSamePage"))) {
            page.setShowResults(true);
        } else {
        	nextPage = _nextPageFromDelegate(page);
            if (nextPage == null) {
                String listConfigurationName = (String) c.valueForKey("listConfigurationName");
                ListPageInterface listpageinterface;
                if (listConfigurationName != null) {
                    listpageinterface = (ListPageInterface) D2W.factory().pageForConfigurationNamed(listConfigurationName, sender.session());
                } else {
                    listpageinterface = D2W.factory().listPageForEntityNamed(c.entity().name(), sender.session());
                }
                listpageinterface.setDataSource(page.queryDataSource());
                listpageinterface.setNextPage(sender.context().page());
                nextPage = (WOComponent) listpageinterface;
            }
        }
        return nextPage;
	}
	
	public WOComponent _return(WOComponent sender) {
		D2WPage page = ERD2WUtilities.enclosingComponentOfClass(sender, D2WPage.class);
		WOComponent nextPage = _nextPageFromDelegate(page);
		if(nextPage == null) {
			nextPage = D2W.factory().defaultPage(sender.session());
		}
		return nextPage;
	}
	
	public WOComponent _save(WOComponent sender) {
		WOComponent nextPage = sender.context().page();
		EOEnterpriseObject eo = object(sender);
		D2WContext c = d2wContext(sender);
		ERD2WPage page = (ERD2WPage)ERD2WUtilities.enclosingComponentOfClass(sender, InspectPageInterface.class);
		
		if(eo != null && eo.editingContext() == null) {
			page.setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERD2WInspect.alreadyAborted", c));
			page.clearValidationFailed();
			return nextPage;
		}

		if(!page.errorMessages().isEmpty()) {
			page.setErrorMessage(null);
			return nextPage;
		}
		
		if(eo == null) { return _nextPageFromDelegate(page); }
		
		boolean shouldRevert = false;
		EOEditingContext ec = eo.editingContext();
		ec.lock();
		try {
			if(shouldValidateBeforeSave(c)) {
				if (ec.insertedObjects().containsObject(eo)) {
					eo.validateForInsert();
				} else {
					eo.validateForUpdate();
				}
			}
			boolean hasChanges = ec.hasChanges();
			if(shouldSaveChanges(c) && hasChanges) {
				try {
					ec.saveChanges();
					nextPage = _nextPageFromDelegate(page);
					// Refresh object to update derived attributes
					ec.refreshObject(eo);
				} catch(RuntimeException e) {
					if(shouldRevertUponSaveFailure(c)) { shouldRevert = true; }
					throw e;
				}
			} else if(!hasChanges) {
				nextPage = _nextPageFromDelegate(page);
			}
		} catch(NSValidation.ValidationException e) {
			page.setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSave", e));
			page.validationFailedWithException(e, e.object(), "saveChangesExceptionKey");
		} catch(EOGeneralAdaptorException e) {
			if(ERXEOAccessUtilities.isOptimisticLockingFailure(e) && shouldRecoverFromOptimisticLockingFailure(c)) {
				EOEnterpriseObject obj = ERXEOAccessUtilities.refetchFailedObject(ec, e);
				page.setErrorMessage(ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSavePleaseReapply", c));
				page.validationFailedWithException(e, obj, "CouldNotSavePleaseReapply");
			} else {
				throw e;
			}
		} finally {
			try {
				if(shouldRevert) { ec.revert(); }
			} finally {
				ec.unlock();
			}
		}
		
		return nextPage;
	}
	
	/**
	 * The select action for a select page interface.
	 * @param sender
	 * @return
	 */
	public WOComponent _select(WOComponent sender) {
		SelectPageInterface spi = ERD2WUtilities.parentSelectPage(sender);
		if(spi != null) {
			EOEnterpriseObject eo = object(sender);
			spi.setSelectedObject(eo);
			if(spi instanceof D2WPage) {
				D2WPage page = (D2WPage)spi;
				return ERD2WUtilities.nextPageInPage(page);
			}
		}
		return sender.context().page();
	}
	
	/**
	 * The inline create action for an edit relationship page.
	 * @param sender
	 */
	public void _createRelated(WOComponent sender) {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		if(erpi != null) {
			EOEnterpriseObject eo = (EOEnterpriseObject)NSKeyValueCoding.Utility.valueForKey(erpi, "masterObject");
			String relationshipKey = (String)NSKeyValueCoding.Utility.valueForKey(erpi, "relationshipKey");
			if(!ERXValueUtilities.isNull(eo) && relationshipKey != null && !relationshipKey.isBlank()) {
				EOEditingContext nestedEC = ERXEC.newEditingContext(eo.editingContext());
				EOClassDescription relatedObjectClassDescription = eo.classDescriptionForDestinationKey(relationshipKey);
				EOEnterpriseObject relatedObject = (EOEnterpriseObject)EOUtilities.createAndInsertInstance(nestedEC, relatedObjectClassDescription.entityName());
				EOEnterpriseObject localObj = EOUtilities.localInstanceOfObject(relatedObject.editingContext(), eo);
				if (localObj instanceof ERXGenericRecord) {
					((ERXGenericRecord)localObj).setValidatedWhenNested(false);
				}
				localObj.addObjectToBothSidesOfRelationshipWithKey(relatedObject, relationshipKey);
				NSKeyValueCoding.Utility.takeValueForKey(erpi, relatedObject, "objectInRelationship");
				NSKeyValueCoding.Utility.takeValueForKey(erpi, "create", "inlineTaskSafely");
			}
		}
	}
	
	public void _editRelated(WOComponent sender) {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		EOEnterpriseObject eo = object(sender);

		if(erpi != null && eo != null) {
			EOEnterpriseObject localObj = ERXEOControlUtilities.editableInstanceOfObject(eo, true);
			NSKeyValueCoding.Utility.takeValueForKey(erpi, "edit", "inlineTaskSafely");
			NSKeyValueCoding.Utility.takeValueForKey(erpi, localObj, "objectInRelationship");
		}
	}
	
	public void _inspectRelated(WOComponent sender) {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		EOEnterpriseObject eo = object(sender);
		
		if(erpi != null && eo != null) {
			EOEnterpriseObject localObj = ERXEOControlUtilities.editableInstanceOfObject(eo, true);
			NSKeyValueCoding.Utility.takeValueForKey(erpi, "inspect", "inlineTaskSafely");
			NSKeyValueCoding.Utility.takeValueForKey(erpi, localObj, "objectInRelationship");
		}
	}
	
	/**
	 * The inline query action for an edit relationship page.
	 * @param sender
	 */
	public void _queryRelated(WOComponent sender) {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		if(erpi != null) {
			NSKeyValueCoding.Utility.takeValueForKey(erpi, "query", "inlineTaskSafely");
		}
	}
	
	/**
	 * Removes the object from a relationship on an edit relationship page.
	 * @param sender
	 */
	public void _removeRelated(WOComponent sender) {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		if(erpi != null && erpi instanceof D2WPage) {
			D2WPage page = (D2WPage)erpi;
			EODataSource ds = page.dataSource();
			EOEnterpriseObject eo = page.object();
			ds.deleteObject(eo);
		}
	}

	/**
	 * The return action for an edit relationship page.
	 * @param sender
	 * @return
	 */
	public WOComponent _returnRelated(WOComponent sender) {
		WOComponent nextPage = null;
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(sender, EditRelationshipPageInterface.class);
		if(erpi != null) {
			EOEnterpriseObject eo = (EOEnterpriseObject)NSKeyValueCoding.Utility.valueForKey(erpi, "masterObject");
			if(eo != null) {
				//eo should be in a non-validating nested ec so no exceptions should throw
				eo.editingContext().saveChanges();
			}
			if(erpi instanceof D2WPage) {
				nextPage = _nextPageFromDelegate((D2WPage)erpi);
			}
			if (nextPage == null && eo != null) {
				nextPage = (WOComponent)D2W.factory().editPageForEntityNamed(eo.entityName(), sender.session());
				((EditPageInterface)nextPage).setObject(eo);
			}
		} else {
			nextPage = _return(sender);
		}
		return nextPage;
	}
	
	protected WOComponent _deleteObject(WOComponent sender, WOComponent nextPage) {
		EOEnterpriseObject eo = object(sender);
		if(eo != null && eo.editingContext() != null) {
			EOEditingContext ec = eo.editingContext();
			NSValidation.ValidationException exception = null;
			
			try {
				if(ec instanceof EOSharedEditingContext) {
					EOEditingContext ec2 = ERXEC.newEditingContext();
					ec2.lock();
					try {
						ec2.setSharedEditingContext(null);
						eo = ERXEOControlUtilities.localInstanceOfObject(ec2, eo);
						ec2.deleteObject(eo);
						ec2.saveChanges();
					} finally {
						ec2.unlock();
						ec2.dispose();
					}
				} else {
					/*
					 * Delete the object in a nested ec first to prevent the
					 * appearance of deletion from display groups when if
					 * validation fails
					 */
					EOEnterpriseObject obj = ERXEOControlUtilities.editableInstanceOfObject(eo, true);
					EOEditingContext childEC = obj.editingContext();
					childEC.deleteObject(obj);
					childEC.saveChanges();
					
					if(ERXEOControlUtilities.isNewObject(eo)) {
						ec.processRecentChanges();
					} else {
						ec.saveChanges();
					}
				}
			} catch(EOObjectNotAvailableException e) {
				exception = ERXValidationFactory.defaultFactory().createCustomException(eo, "EOObjectNotAvailableException");
			} catch(EOGeneralAdaptorException e) {
				NSDictionary<?,?> userInfo = e.userInfo();
				if(userInfo != null) {
					EODatabaseOperation op = (EODatabaseOperation)userInfo.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
					if(op.databaseOperator() == EODatabaseOperation.DatabaseDeleteOperator) {
						exception = ERXValidationFactory.defaultFactory().createCustomException(eo, "EOObjectNotAvailableException");
					}
				}
				if(exception == null) {
					exception = ERXValidationFactory.defaultFactory().createCustomException(eo, "Database error: " + e.getMessage());
				}
			} catch(NSValidation.ValidationException e) {
				exception = e;
			}
			if(exception != null) {
				if (exception instanceof ERXValidationException) {
					ERXValidationException ex = (ERXValidationException)exception;
					D2WContext c = d2wContext(sender);
					ex.setContext(c);
					
					Object o = ex.object();
					if(o instanceof EOEnterpriseObject) {
						EOEnterpriseObject obj = (EOEnterpriseObject)o;
						c.takeValueForKey(obj.entityName(), "entityName");
						c.takeValueForKey(ex.propertyKey(), "propertyKey");
					}
				}
				if(log.isDebugEnabled()) {
					log.debug("Validation Exception: " + exception.getMessage(), exception);
				}
				ec.revert();
				String errorMessage = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("CouldNotSave", exception);
				ErrorPageInterface epf = D2W.factory().errorPage(sender.session());
				if (epf instanceof ERDErrorPageInterface) {
					ERDErrorPageInterface err = (ERDErrorPageInterface)epf;
					err.setException(exception);
				}
				epf.setMessage(errorMessage);
				epf.setNextPage(nextPage);
				return (WOComponent)epf;
			}
		}
		return nextPage;
	}

	protected WOComponent _nextPageFromDelegate(D2WPage page) {
		WOComponent nextPage = null;
		NextPageDelegate delegate = page.nextPageDelegate();
		if(delegate != null) {
			if (!((delegate instanceof ERDBranchDelegate) && (((ERDBranchInterface)page).branchName() == null))) {
				/* 
				 * we assume here, because nextPage() in ERDBranchDelegate 
				 * is final, we can't do something reasonable when none of 
				 * the branch buttons was selected. This allows us to throw 
				 * a branch delegate at any page, even when no branch was 
				 * taken
				 */
				nextPage = delegate.nextPage(page);
			}
		}
		if(nextPage == null) {
			nextPage = page.nextPage();
		}
		return nextPage;
	}
	
}
