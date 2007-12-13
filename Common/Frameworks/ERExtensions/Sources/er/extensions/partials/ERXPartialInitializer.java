package er.extensions.partials;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSUtilities;

import er.extensions.ERXConstant;
import er.extensions.ERXEntityClassDescription;
import er.extensions.ERXModelGroup;
import er.extensions.ERXProperties;

/**
 * ERXPartialInitializer is registered at startup and is responsible for merging
 * partial entities together into a single entity.
 * 
 * @author mschrag
 */
public class ERXPartialInitializer {
	private static final ERXPartialInitializer _initializer = new ERXPartialInitializer();

	private NSMutableDictionary<EOEntity, NSMutableArray<Class<ERXPartial>>> _partialsForEntity = new NSMutableDictionary<EOEntity, NSMutableArray<Class<ERXPartial>>>();

	public static ERXPartialInitializer initializer() {
		return _initializer;
	}

	public static void registerModelGroupListener() {
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.partials.enabled", false)) {
			NSNotificationCenter.defaultCenter().addObserver(_initializer, new NSSelector("modelGroupAdded", ERXConstant.NotificationClassArray), ERXModelGroup.ModelGroupAddedNotification, null);
			// NSNotificationCenter.defaultCenter().addObserver(_initializer,
			// new NSSelector("workaroundStupidLoadingProblem",
			// ERXConstant.NotificationClassArray),
			// WOApplication.ApplicationDidFinishLaunchingNotification, null);
		}
	}

	public void workaroundClassDescriptionResetProblem() {
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.partials.enabled", false)) {
			for (EOEntity partialEntity : _partialsForEntity.keySet()) {
				ERXEntityClassDescription ecd = (ERXEntityClassDescription) partialEntity.classDescriptionForInstances();
				for (Class<ERXPartial> partialClass : _partialsForEntity.objectForKey(partialEntity)) {
					ecd._addPartialClass(partialClass);
				}
			}
		}
	}

	public void modelGroupAdded(NSNotification notification) {
		ERXModelGroup modelGroup = (ERXModelGroup) notification.object();
		initializePartialEntities(modelGroup);
	}

	@SuppressWarnings("unchecked")
	public void initializePartialEntities(EOModelGroup modelGroup) {
		NSMutableDictionary<EOEntity, EOEntity> baseForPartial = new NSMutableDictionary<EOEntity, EOEntity>();

		Enumeration modelsEnum = modelGroup.models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			Enumeration entitiesEnum = model.entities().objectEnumerator();
			while (entitiesEnum.hasMoreElements()) {
				EOEntity partialExtensionEntity = (EOEntity) entitiesEnum.nextElement();
				NSDictionary userInfo = partialExtensionEntity.userInfo();
				NSDictionary entityModelerDictionary = (NSDictionary) userInfo.objectForKey("_EntityModeler");
				if (entityModelerDictionary != null) {
					String partialEntityName = (String) entityModelerDictionary.objectForKey("partialEntity");
					if (partialEntityName != null) {
						EOEntity partialEntity = modelGroup.entityNamed(partialEntityName);
						if (partialEntity == null) {
							throw new IllegalArgumentException("The entity '" + partialExtensionEntity.name() + "' claimed to be a partialEntity for the entity '" + partialEntity.name() + "', but there is no entity of that name.");
						}

						Enumeration partialAttributes = partialExtensionEntity.attributes().objectEnumerator();
						while (partialAttributes.hasMoreElements()) {
							EOAttribute partialAttribute = (EOAttribute) partialAttributes.nextElement();
							if (partialEntity.attributeNamed(partialAttribute.name()) == null) {
								NSMutableDictionary<String, Object> attributePropertyList = new NSMutableDictionary<String, Object>();
								partialAttribute.encodeIntoPropertyList(attributePropertyList);
								String factoryMethodArgumentType = (String) attributePropertyList.objectForKey("factoryMethodArgumentType");
								// OFFICIALLY THE DUMBEST DAMN THING I'VE EVER
								// SEEN
								if ("EOFactoryMethodArgumentIsString".equals(factoryMethodArgumentType)) {
									attributePropertyList.setObjectForKey("EOFactoryMethodArgumentIsNSString", "factoryMethodArgumentType");
								}
								EOAttribute primaryAttribute = new EOAttribute(attributePropertyList, partialEntity);
								primaryAttribute.awakeWithPropertyList(attributePropertyList);
								partialEntity.addAttribute(primaryAttribute);
							}
							else {
								ERXModelGroup.log.debug("Skipping partial attribute " + partialExtensionEntity.name() + "." + partialAttribute.name() + " because " + partialEntity.name() + " already has an attribute of the same name.");
							}
						}

						Enumeration partialRelationships = partialExtensionEntity.relationships().objectEnumerator();
						while (partialRelationships.hasMoreElements()) {
							EORelationship partialRelationship = (EORelationship) partialRelationships.nextElement();
							if (partialEntity.relationshipNamed(partialRelationship.name()) == null) {
								NSMutableDictionary<String, Object> relationshipPropertyList = new NSMutableDictionary<String, Object>();
								partialRelationship.encodeIntoPropertyList(relationshipPropertyList);

								EORelationship primaryRelationship = new EORelationship(relationshipPropertyList, partialEntity);
								primaryRelationship.awakeWithPropertyList(relationshipPropertyList);
								partialEntity.addRelationship(primaryRelationship);
							}
							else {
								ERXModelGroup.log.debug("Skipping partial relationship " + partialExtensionEntity.name() + "." + partialRelationship.name() + " because " + partialEntity.name() + " already has a relationship of the same name.");
							}
						}

						NSMutableArray<Class<ERXPartial>> partialsForEntity = _partialsForEntity.objectForKey(partialEntity);
						if (partialsForEntity == null) {
							partialsForEntity = new NSMutableArray<Class<ERXPartial>>();
							_partialsForEntity.setObjectForKey(partialsForEntity, partialEntity);
						}
						Class<ERXPartial> partialClass = (Class<ERXPartial>) _NSUtilities.classWithName(partialExtensionEntity.className());
						ERXEntityClassDescription ecd = (ERXEntityClassDescription) partialEntity.classDescriptionForInstances();
						ecd._addPartialClass(partialClass);
						partialsForEntity.addObject(partialClass);
						baseForPartial.setObjectForKey(partialEntity, partialExtensionEntity);
					}
				}
			}
		}

		modelsEnum = modelGroup.models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			Enumeration entitiesEnum = model.entities().objectEnumerator();
			while (entitiesEnum.hasMoreElements()) {
				EOEntity entity = (EOEntity) entitiesEnum.nextElement();
				Enumeration relationships = entity.relationships().immutableClone().objectEnumerator();
				while (relationships.hasMoreElements()) {
					EORelationship relationship = (EORelationship) relationships.nextElement();
					EOEntity destinationEntity = relationship.destinationEntity();
					EOEntity baseEntity = baseForPartial.objectForKey(destinationEntity);
					if (baseEntity != null) {
						NSMutableDictionary<String, Object> relationshipPropertyList = new NSMutableDictionary<String, Object>();
						relationship.encodeIntoPropertyList(relationshipPropertyList);
						relationshipPropertyList.setObjectForKey(baseEntity.name(), "destination");

						EORelationship primaryRelationship = new EORelationship(relationshipPropertyList, entity);
						primaryRelationship.awakeWithPropertyList(relationshipPropertyList);
						entity.removeRelationship(relationship);
						entity.addRelationship(primaryRelationship);
					}
				}
			}
		}

		for (EOEntity partialExtensionEntity : baseForPartial.allKeys()) {
			partialExtensionEntity.model().removeEntity(partialExtensionEntity);
		}
	}
}
