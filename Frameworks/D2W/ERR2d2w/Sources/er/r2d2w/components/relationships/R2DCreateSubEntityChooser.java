package er.r2d2w.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.directtoweb.components.ERDCustomComponent;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXValueUtilities;

public class R2DCreateSubEntityChooser extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private transient EOEntity subEntity;
	private transient EOEntity selectedEntity;
	private String subEntityName;
	private String selectedEntityName;
	private String labelID;
	private D2WContext ctx;
	private transient NSArray<EOEntity> createSubEntities;
	
	public R2DCreateSubEntityChooser(WOContext context) {
		super(context);
	}
	
	private D2WContext ctx() {
		if(ctx == null) {
			ctx = ERD2WContext.newContext(session());
		}
		return ctx;
	}
	
	@SuppressWarnings("unchecked")
	public NSArray<EOEntity> createSubEntities() {
		if(createSubEntities == null) {
			createSubEntities = (NSArray<EOEntity>) d2wContext().valueForKey("createSubEntities");
		}
		return createSubEntities;
	}
	
	/**
	 * @return the subEntity
	 */
	public EOEntity subEntity() {
		if(subEntity == null && subEntityName != null) {
			subEntity = EOModelGroup.defaultGroup().entityNamed(subEntityName);
		}
		return subEntity;
	}

	/**
	 * @param subEntity the subEntity to set
	 */
	public void setSubEntity(EOEntity subEntity) {
		this.subEntity = subEntity;
		this.subEntityName = subEntity == null?null:subEntity.name();
	}

	/**
	 * @return the selectedEntity
	 */
	public EOEntity selectedEntity() {
		if(selectedEntity == null && selectedEntityName != null) {
			selectedEntity = EOModelGroup.defaultGroup().entityNamed(selectedEntityName);
		}
		return selectedEntity;
	}

	/**
	 * @param selectedEntity the selectedEntity to set
	 */
	public void setSelectedEntity(EOEntity selectedEntity) {
		this.selectedEntity = selectedEntity;
		this.selectedEntityName = selectedEntity == null?null:selectedEntity.name();
	}

	/**
	 * @return the labelID
	 */
	public String labelID() {
		if(labelID == null) {
			labelID = ERXWOContext.safeIdentifierName(context(), true);
		}
		return labelID;
	}

	public void createAction() {
		EditRelationshipPageInterface erpi = ERD2WUtilities.enclosingComponentOfClass(this, EditRelationshipPageInterface.class);
		if(erpi != null && selectedEntity() != null) {
			EOEnterpriseObject eo = (EOEnterpriseObject)NSKeyValueCoding.Utility.valueForKey(erpi, "masterObject");
			String relationshipKey = (String)NSKeyValueCoding.Utility.valueForKey(erpi, "relationshipKey");
			if(!ERXValueUtilities.isNull(eo) && relationshipKey != null && !relationshipKey.isBlank()) {
				EOEditingContext nestedEC = ERXEC.newEditingContext(eo.editingContext());
				EOClassDescription relatedObjectClassDescription = selectedEntity().classDescriptionForInstances();
				EOEnterpriseObject relatedObject = (EOEnterpriseObject)EOUtilities.createAndInsertInstance(nestedEC, relatedObjectClassDescription.entityName());
				EOEnterpriseObject localObj = EOUtilities.localInstanceOfObject(relatedObject.editingContext(), eo);
				if (localObj instanceof ERXGenericRecord) {
					((ERXGenericRecord)localObj).setValidatedWhenNested(false);
				}
				localObj.addObjectToBothSidesOfRelationshipWithKey(relatedObject, relationshipKey);
				NSKeyValueCoding.Utility.takeValueForKey(erpi, relatedObject, "objectInRelationship");
				NSKeyValueCoding.Utility.takeValueForKey(erpi, "create", "inlineTaskSafely");
				ctx().setEntity(selectedEntity());
				String config = (String)ctx().valueForKey("editEmbeddedConfigurationName");
				NSKeyValueCoding.Utility.takeValueForKey(erpi, config, "inspectConfigurationName");
			}
		}
	}

	public String displayNameForEntity() {
		ctx().setEntity(subEntity());
		return (String)ctx().valueForKey("displayNameForEntity");
	}
}