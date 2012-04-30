// DO NOT EDIT.  Make changes to ${entity.classNameWithOptionalPackage}.java instead.
#if ($entity.superclassPackageName)
package $entity.superclassPackageName;

#end
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

#if ($entity.parentSet)
    #set ($parentClass = ${entity.parent.classNameWithDefault})
    #set ($parentClazzClass = "${entity.parent.classNameWithOptionalPackage}.${entity.parent.classNameWithoutPackage}Clazz<T>")
#else
    #set ($parentClass = "ERXGenericRecord")
    #set ($parentClazzClass = "ERXGenericRecord.ERXGenericRecordClazz<T>")
#end

@SuppressWarnings("all")
public abstract class ${entity.prefixClassNameWithoutPackage} extends #if ($entity.parentClassNameSet)${entity.parentClassName}#elseif ($entity.partialEntitySet)er.extensions.partials.ERXPartial<${entity.partialEntity.className}>#elseif ($entity.parentSet)${entity.parent.classNameWithDefault}#elseif ($EOGenericRecord)${EOGenericRecord}#else ERXGenericRecord#end {
#if ($entity.partialEntitySet)
  public static final String ENTITY_NAME = "$entity.partialEntity.name";
#else
  public static final String ENTITY_NAME = "$entity.name";
#end

  // Attributes
#foreach ($attribute in $entity.sortedClassAttributes)
  public static final ERXKey<$attribute.javaClassName> ${attribute.uppercaseUnderscoreName} = new ERXKey<$attribute.javaClassName>("$attribute.name");
  public static final String ${attribute.uppercaseUnderscoreName}_KEY = ${attribute.uppercaseUnderscoreName}.key();
#end

  // Relationships
#foreach ($relationship in $entity.sortedClassRelationships)
  public static final ERXKey<$relationship.actualDestination.classNameWithDefault> ${relationship.uppercaseUnderscoreName} = new ERXKey<$relationship.actualDestination.classNameWithDefault>("$relationship.name");
  public static final String ${relationship.uppercaseUnderscoreName}_KEY = ${relationship.uppercaseUnderscoreName}.key();
#end

  public static class _${entity.classNameWithoutPackage}Clazz<T extends ${entity.classNameWithOptionalPackage}> extends ${parentClazzClass} {
    /* more clazz methods here */
  }

  private static final Logger LOG = Logger.getLogger(${entity.prefixClassNameWithoutPackage}.class);

  public ${entity.classNameWithOptionalPackage}.${entity.classNameWithoutPackage}Clazz clazz() {
    return ${entity.classNameWithOptionalPackage}.clazz;
  }
  
#foreach ($attribute in $entity.sortedClassAttributes)
#if (!$attribute.inherited)
#if ($attribute.userInfo.ERXConstantClassName)
  public $attribute.userInfo.ERXConstantClassName ${attribute.name}() {
    Number value = (Number)storedValueForKey(${entity.prefixClassNameWithoutPackage}.${attribute.uppercaseUnderscoreName}_KEY);
    return ($attribute.userInfo.ERXConstantClassName)value;
  }

  public void set${attribute.capitalizedName}($attribute.userInfo.ERXConstantClassName value) {
    takeStoredValueForKey(value, ${entity.prefixClassNameWithoutPackage}.${attribute.uppercaseUnderscoreName}_KEY);
  }

#else
#if (!($attribute.userInfo.ERXLanguages))
  public $attribute.javaClassName ${attribute.name}() {
    return ($attribute.javaClassName) storedValueForKey(${entity.prefixClassNameWithoutPackage}.${attribute.uppercaseUnderscoreName}_KEY);
  }

#end
#if (!($attribute.userInfo.ERXLanguages))
  public void set${attribute.capitalizedName}($attribute.javaClassName value) {
    if (${entity.prefixClassNameWithoutPackage}.LOG.isDebugEnabled()) {
    	${entity.prefixClassNameWithoutPackage}.LOG.debug( "updating $attribute.name from " + ${attribute.name}() + " to " + value);
    }
    takeStoredValueForKey(value, ${entity.prefixClassNameWithoutPackage}.${attribute.uppercaseUnderscoreName}_KEY);
  }

#end
#end
#end
#end
#foreach ($relationship in $entity.sortedClassToOneRelationships)
#if (!$relationship.inherited) 
  public $relationship.actualDestination.classNameWithDefault ${relationship.name}() {
    return ($relationship.actualDestination.classNameWithDefault)storedValueForKey(${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
  }
  
  public void set${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault value) {
    takeStoredValueForKey(value, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
  }

  public void set${relationship.capitalizedName}Relationship($relationship.actualDestination.classNameWithDefault value) {
    if (${entity.prefixClassNameWithoutPackage}.LOG.isDebugEnabled()) {
      ${entity.prefixClassNameWithoutPackage}.LOG.debug("updating $relationship.name from " + ${relationship.name}() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	set${relationship.capitalizedName}(value);
    }
    else if (value == null) {
    	$relationship.actualDestination.classNameWithDefault oldValue = ${relationship.name}();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
    }
  }
#end
#end
#foreach ($relationship in $entity.sortedClassToManyRelationships)
#if (!$relationship.inherited) 
  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}() {
    return (NSArray<${relationship.actualDestination.classNameWithDefault}>)storedValueForKey(${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
  }

#if (!$relationship.inverseRelationship || $relationship.flattened || !$relationship.inverseRelationship.classProperty)
  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}(EOQualifier qualifier) {
    return ${relationship.name}(qualifier, null);
  }
#else
  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}(EOQualifier qualifier) {
    return ${relationship.name}(qualifier, null, false);
  }

  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}(EOQualifier qualifier, boolean fetch) {
    return ${relationship.name}(qualifier, null, fetch);
  }
#end

  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings#if ($relationship.inverseRelationship && !$relationship.flattened && $relationship.inverseRelationship.classProperty), boolean fetch#end) {
    NSArray<${relationship.actualDestination.classNameWithDefault}> results;
#if ($relationship.inverseRelationship && !$relationship.flattened && $relationship.inverseRelationship.classProperty)
    if (fetch) {
      EOQualifier fullQualifier;
#if (${relationship.actualDestination.genericRecord})
      EOQualifier inverseQualifier = new EOKeyValueQualifier("${relationship.inverseRelationship.name}", EOQualifier.QualifierOperatorEqual, this);
#else
      EOQualifier inverseQualifier = new EOKeyValueQualifier(${relationship.actualDestination.classNameWithDefault}.${relationship.inverseRelationship.uppercaseUnderscoreName}_KEY, EOQualifier.QualifierOperatorEqual, this);
#end
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

#if (${relationship.actualDestination.genericRecord})
      EOFetchSpecification fetchSpec = new EOFetchSpecification("${relationship.actualDestination.name}", qualifier, sortOrderings);
      fetchSpec.setIsDeep(true);
      results = (NSArray<${relationship.actualDestination.classNameWithDefault}>)editingContext().objectsWithFetchSpecification(fetchSpec);
#else
      results = ${relationship.actualDestination.classNameWithDefault}.clazz.objectsMatchingQualifier(editingContext(), fullQualifier, sortOrderings);
#end
    }
    else {
#end
      results = ${relationship.name}();
      if (qualifier != null) {
        results = (NSArray<${relationship.actualDestination.classNameWithDefault}>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<${relationship.actualDestination.classNameWithDefault}>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
#if ($relationship.inverseRelationship && !$relationship.flattened && $relationship.inverseRelationship.classProperty)
    }
#end
    return results;
  }
  
  public void addTo${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault object) {
    includeObjectIntoPropertyWithKey(object, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
  }

  public void removeFrom${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault object) {
    excludeObjectFromPropertyWithKey(object, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
  }

  public void addTo${relationship.capitalizedName}Relationship($relationship.actualDestination.classNameWithDefault object) {
    if (${entity.prefixClassNameWithoutPackage}.LOG.isDebugEnabled()) {
      ${entity.prefixClassNameWithoutPackage}.LOG.debug("adding " + object + " to ${relationship.name} relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addTo${relationship.capitalizedName}(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
    }
  }

  public void removeFrom${relationship.capitalizedName}Relationship($relationship.actualDestination.classNameWithDefault object) {
    if (${entity.prefixClassNameWithoutPackage}.LOG.isDebugEnabled()) {
      ${entity.prefixClassNameWithoutPackage}.LOG.debug("removing " + object + " from ${relationship.name} relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFrom${relationship.capitalizedName}(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
    }
  }

  public $relationship.actualDestination.classNameWithDefault create${relationship.capitalizedName}Relationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName(#if(${relationship.actualDestination.genericRecord})"${relationship.actualDestination.name}"#else ${relationship.actualDestination.classNameWithDefault}.ENTITY_NAME #end);
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
    return ($relationship.actualDestination.classNameWithDefault) eo;
  }

  public void delete${relationship.capitalizedName}Relationship($relationship.actualDestination.classNameWithDefault object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, ${entity.prefixClassNameWithoutPackage}.${relationship.uppercaseUnderscoreName}_KEY);
#if (!$relationship.ownsDestination)
    editingContext().deleteObject(object);
#end
  }

  public void deleteAll${relationship.capitalizedName}Relationships() {
    Enumeration<$relationship.actualDestination.classNameWithDefault> objects = ${relationship.name}().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      delete${relationship.capitalizedName}Relationship(objects.nextElement());
    }
  }

#end
#end

#foreach ($fetchSpecification in $entity.sortedFetchSpecs)
#if (true || $fetchSpecification.distinctBindings.size() > 0)
  public static NSArray#if ($fetchSpecification.fetchEnterpriseObjects)<${entity.className}>#else<NSDictionary>#end fetch${fetchSpecification.capitalizedName}(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("${fetchSpecification.name}", ${entity.prefixClassNameWithoutPackage}.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray#if ($fetchSpecification.fetchEnterpriseObjects)<${entity.className}>#else<NSDictionary>#end)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
#end
  public static NSArray#if ($fetchSpecification.fetchEnterpriseObjects)<${entity.className}>#else<NSDictionary>#end fetch${fetchSpecification.capitalizedName}(EOEditingContext editingContext#foreach ($binding in $fetchSpecification.distinctBindings),
	${binding.attributePath.childClassName} ${binding.name}Binding#end)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("${fetchSpecification.name}", ${entity.prefixClassNameWithoutPackage}.ENTITY_NAME);
#if ($fetchSpecification.distinctBindings.size() > 0)
    NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
#foreach ($binding in $fetchSpecification.distinctBindings)
    bindings.takeValueForKey(${binding.name}Binding, "${binding.name}");
#end
	fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
#end
    return (NSArray#if ($fetchSpecification.fetchEnterpriseObjects)<${entity.className}>#else<NSDictionary>#end)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
#end
}
