package ag.kcmedia;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
/**
* Class for Component EOModeler.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Fri Jul 26 2002
 * @project DevStudio
 */
public class EOModeler extends WOComponent {
    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(EOModeler.class,"components");

    /**
     * Public constructor.
     * @param context the context
     */
    public EOModeler(WOContext context) {
        super(context);
    }
        /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }
    protected String modelPath = "/Volumes/Home/Desktop/armehaut/AHLogic/armehaut.eomodeld";
    public void setModelPath(String value) {
        modelPath = value;
        modelGroup = new EOModelGroup();
        model = modelGroup.addModelWithPath(modelPath);
        prototypes = model.entityNamed("EOPrototypes");
        if(prototypes == null)
            prototypes = model.entityNamed("EOJDBCPrototypes");
    }
    public String modelPath() {
        if(modelPath == null)
            modelPath = "/Volumes/Home/Desktop/armehaut/AHLogic/armehaut.eomodeld";
        return modelPath;
    }
    public boolean showTableNames = false;
    public EOModelGroup modelGroup;
    public EOModel model;
    public EOEntity entity;
    public EOEntity selectedEntity;
    public EOEntity subEntity;
    public EOEntity targetEntity;
    public EOAttribute attribute;
    public EORelationship relationship;
    public EOEntity destinationEntity;
    public EOAttribute prototype;
    public EOEntity prototypes;
    public String entityName = "NewEntity";
    public String sql;
    public NSArray attributesUsedInRelationships;
        public void awake() {
            super.awake();
            attributesUsedInRelationships = null;
        }
            public void updateClassProperties(EOEntity entity, Object o, boolean add) {
                NSMutableArray array = entity.classProperties().mutableClone();
                boolean contains = array.containsObject(o);
                if(add && !contains) {
                    array.addObject(o);
                    entity.setClassProperties(array);
                } else if(!add && contains){
                    array.removeObject(o);
                    entity.setClassProperties(array);
                }
            }
        /*** relationships **********/
        public void setRelationshipIsToMany(boolean value) {
            relationship.setToMany(value);
        }
        public boolean relationshipIsToMany() {
            return relationship.isToMany();
        }
        public void setRelationshipName(String value) {
            relationship.setName(value);
        }
        public String relationshipName() {
            return relationship.name();
        }
        public boolean relationshipIsClassProperty() {
            return selectedEntity.classProperties().containsObject(relationship);
        }
        public void setRelationshipIsClassProperty(boolean value) {
            updateClassProperties(selectedEntity,relationship,value);
        }
        /*** attributes **********/
        public void setAttributePrototype(EOAttribute value) {
            String oldName = attribute.columnName();
            attribute.setPrototype(value);
            attribute.setColumnName(oldName);
        }
        public EOAttribute attributePrototype() {
            return attribute.prototype();
        }
        public NSArray attributesUsedInRelationships() {
            if(attributesUsedInRelationships == null) {
                NSMutableArray array = new NSMutableArray((NSArray)valueForKeyPath("model.entities.relationships.joins.sourceAttribute"));
                array.addObject(valueForKeyPath("model.entities.relationships.joins.destinationAttribute"));
                attributesUsedInRelationships = (NSArray)array.valueForKeyPath("@flatten");
            }
            return attributesUsedInRelationships;
        }
        public boolean attributeHasRelationships() {
            return attributesUsedInRelationships().containsObject(attribute);
        }
        public void setAttributeName(String value) {
            attribute.setColumnName(value);
            attribute.setName(value);
        }
        public String attributeName() {
            return attribute.name();
        }
        public boolean attributeIsPrimaryKey() {
            return selectedEntity.primaryKeyAttributes().containsObject(attribute);
        }
        public boolean attributeIsClassProperty() {
            return selectedEntity.classProperties().containsObject(attribute);
        }
        public void setAttributeIsClassProperty(boolean value) {
            updateClassProperties(selectedEntity,attribute,value);
        }
        public String selectedEntityString() {
            String string = "none";
            try {
                if(selectedEntity != null)
                    return selectedEntity.toString();
            } catch(Exception ex) {
                string = "" + ex;
            }
            return string;
        }
        /*** actions **********/
        public WOComponent addAttribute() {
            selectedEntity.addAttribute(new EOAttribute());
            return null;
        }
        public WOComponent addRelationship() {
            return null;
        }
        public void addRelationship(EOEntity selectedEntity, EOEntity targetEntity, EOAttribute foreignAttribute, boolean isToMany, boolean addBack) {
            EORelationship relationship = new EORelationship();
            String targetName = targetEntity.name().toLowerCase();
            String selectedName = selectedEntity.name().toLowerCase();
            EOAttribute selectedPK = (EOAttribute)selectedEntity.primaryKeyAttributes().objectAtIndex(0);
            if(foreignAttribute == null) {
                foreignAttribute = new EOAttribute();
                targetEntity.addAttribute(foreignAttribute);
                foreignAttribute.setName(selectedName+"id");
                foreignAttribute.setPrototype(selectedPK.prototype());
                foreignAttribute.setColumnName(selectedName+"id");
                updateClassProperties(targetEntity,foreignAttribute,false);
            }
            selectedEntity.addRelationship(relationship);
            EOJoin join = new EOJoin(selectedPK, foreignAttribute);
            relationship.setName(targetName+(isToMany ? "s" : ""));
            relationship.addJoin(join);
            relationship.setToMany(isToMany);
            relationship.setJoinSemantic(EORelationship.InnerJoin);
            if(addBack) {
                relationship = new EORelationship();
                targetEntity.addRelationship(relationship);
                join = new EOJoin(foreignAttribute,selectedPK);
                relationship.setName(selectedName);
                relationship.addJoin(join);
                relationship.setToMany(false);
                relationship.setJoinSemantic(EORelationship.InnerJoin);
            }
        }
        public void addBackRelationship(EOEntity selectedEntity, EOEntity targetEntity, EOAttribute foreignAttribute, boolean isToMany, boolean addBack) {
            EORelationship relationship = new EORelationship();
            String targetName = targetEntity.name().toLowerCase();
            String selectedName = selectedEntity.name().toLowerCase();
            EOAttribute selectedPK = (EOAttribute)targetEntity.primaryKeyAttributes().objectAtIndex(0);
            if(foreignAttribute == null) {
                foreignAttribute = new EOAttribute();
                selectedEntity.addAttribute(foreignAttribute);
                foreignAttribute.setName(targetName+"id");
                foreignAttribute.setPrototype(selectedPK.prototype());
                foreignAttribute.setColumnName(targetName+"id");
                updateClassProperties(selectedEntity,foreignAttribute,false);
            }
            selectedEntity.addRelationship(relationship);
            EOJoin join = new EOJoin(foreignAttribute,selectedPK);
            relationship.setName(targetName+(isToMany ? "s" : ""));
            relationship.addJoin(join);
            relationship.setToMany(isToMany);
            relationship.setJoinSemantic(EORelationship.InnerJoin);
        }
        public void addRelationship(EOAttribute foreignAttribute, boolean isToMany, boolean addBack) {
            addRelationship(selectedEntity, targetEntity, foreignAttribute, isToMany, addBack);
        }
        public WOComponent addBackToOneRelationship() {
            addBackRelationship(selectedEntity,targetEntity,null,false,false);
            return null;
        }
        public WOComponent addBackToManyRelationship() {
            addBackRelationship(selectedEntity,targetEntity,null,true,false);
            return null;
        }
        public WOComponent addToOneRelationship() {
            addRelationship(null, false, false);
            return null;
        }
        public WOComponent addToManyRelationship() {
            addRelationship(null, true, false);
            return null;
        }
        public WOComponent addToOneWithBackRelationship() {
            addRelationship(null, false, true);
            return null;
        }
        public WOComponent addToManyWithBackRelationship() {
            addRelationship(null, true, true);
            return null;
        }
        public WOComponent addFlattenedManyToManyRelationship() {
            String name = "X"+selectedEntity.name()+targetEntity.name();
            addManyToManyRelationship();
            name = name.toLowerCase() + "s";
            updateClassProperties(selectedEntity, selectedEntity.relationshipNamed(name), false);
            updateClassProperties(targetEntity, targetEntity.relationshipNamed(name), false);
            EORelationship relationship;
            EOJoin join;
            relationship = new EORelationship();
            selectedEntity.addRelationship(relationship);
            relationship.setName(targetEntity.name().toLowerCase()+"s");
            relationship.setDefinition(name + "." + targetEntity.name().toLowerCase());
            relationship.setJoinSemantic(EORelationship.InnerJoin);
            relationship = new EORelationship();
            targetEntity.addRelationship(relationship);
            relationship.setName(targetEntity.name().toLowerCase()+"s");
            relationship.setDefinition(name + "." + selectedEntity.name().toLowerCase());
            relationship.setJoinSemantic(EORelationship.InnerJoin);
            return null;
        }
        public WOComponent addManyToManyRelationship() {
            EOEntity intermediate = new EOEntity();
            String name = "X"+selectedEntity.name()+targetEntity.name();
            intermediate.setName(name);
            intermediate.setExternalName(name.toLowerCase());
            model.addEntity(intermediate);
            addRelationship(selectedEntity,intermediate,null,true,true);
            addRelationship(targetEntity,intermediate,null,true,true);
            intermediate.setPrimaryKeyAttributes(intermediate.attributes());
            intermediate.setName(name);
            intermediate.setClassProperties(new NSArray());
            return null;
        }
            public WOComponent update() {
                sql = "";
                try {
                    EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
                    EOSchemaGeneration synchronizationFactory = adaptor.synchronizationFactory();
                    NSArray array = synchronizationFactory.createTableStatementsForEntityGroup(new NSArray(selectedEntity));
                    sql =  ((NSArray)array.valueForKeyPath("statement")).componentsJoinedByString(";\n");
                } catch(Exception ex) {
                    sql = "" + ex;
                }
                return null;
            }
        public WOComponent selectEntity() {
            selectedEntity = entity;
            return null;
        }
        public WOComponent removeAttribute() {
            selectedEntity.removeAttribute(attribute);
            return null;
        }
        public WOComponent removeRelationship() {
            selectedEntity.removeRelationship(relationship);
            return null;
        }
            public WOComponent removeEntity() {
                model.removeEntity(selectedEntity);
                selectedEntity = null;
                return null;
            }
        public String entityName() {
            return selectedEntity.name();
        }
        public void setEntityName(String name) {
            selectedEntity.setName(name);
            selectedEntity.setExternalName(name.toLowerCase());
        }
        public WOComponent addEntity() {
            entity = new EOEntity();
            entity.setName(entityName);
            entity.setExternalName(entityName.toLowerCase());
            model.addEntity(entity);
            EOAttribute oid = new EOAttribute();
            entity.addAttribute(oid);
            NSArray pks = new NSArray(oid);
            entity.setPrimaryKeyAttributes(pks);
            oid.setPrototype(prototypes.attributeNamed("id"));
            oid.setColumnName("oid");
            oid.setName("oid");
            selectedEntity = entity;
            return null;
        }
        public WOComponent selectDestinationEntity() {
            selectedEntity = relationship.destinationEntity();
            return null;
        }
}