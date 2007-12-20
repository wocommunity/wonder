// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ${entity.classNameWithOptionalPackage}.java instead.
#if ($entity.superclassPackageName)
package $entity.superclassPackageName;

#end
import er.extensions.ERXGenericRecord;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;

#if ($entity.parentSet)
    #set ($parentClass = ${entity.parent.classNameWithDefault})
    #set ($parentClazzClass = "${entity.parent.classNameWithoutPackage}.${entity.parent.classNameWithoutPackage}Clazz")
#else
    #set ($parentClass = "ERXGenericRecord")
    #set ($parentClazzClass = "ERXGenericRecord.ERXGenericRecordClazz<${entity.classNameWithoutPackage}>")
#end

@SuppressWarnings("all")
public abstract class ${entity.prefixClassNameWithoutPackage} extends $parentClass {

	public static final String ENTITY_NAME = "$entity.name";

    public interface Key {
	// Attributes
#foreach ($attribute in $entity.sortedClassAttributes)
	   public static final String ${attribute.uppercaseUnderscoreName} = "$attribute.name";
#end

	// Relationships
#foreach ($relationship in $entity.sortedClassRelationships)
	   public static final String ${relationship.uppercaseUnderscoreName} = "$relationship.name";
#end
    }

    public static class _${entity.classNameWithoutPackage}Clazz extends ${parentClazzClass} {
        /* more clazz methods here */
    }

#foreach ($attribute in $entity.sortedClassAttributes)
#if (!$attribute.inherited)
#if ($attribute.userInfo.ERXConstantClassName)
  public $attribute.userInfo.ERXConstantClassName ${attribute.name}() {
    Number value = (Number)storedValueForKey(Key.${attribute.uppercaseUnderscoreName});
    return ($attribute.userInfo.ERXConstantClassName)value;
  }
  public void set${attribute.capitalizedName}($attribute.userInfo.ERXConstantClassName value) {
    takeStoredValueForKey(value, Key.${attribute.uppercaseUnderscoreName});
  }
#else
  public $attribute.javaClassName ${attribute.name}() {
    return ($attribute.javaClassName) storedValueForKey(Key.${attribute.uppercaseUnderscoreName});
  }
  public void set${attribute.capitalizedName}($attribute.javaClassName value) {
    takeStoredValueForKey(value, Key.${attribute.uppercaseUnderscoreName});
  }
#end

#end
#end
#foreach ($relationship in $entity.sortedClassToOneRelationships)
#if (!$relationship.inherited) 
  public $relationship.actualDestination.classNameWithDefault ${relationship.name}() {
    return ($relationship.actualDestination.classNameWithDefault)storedValueForKey(Key.${relationship.uppercaseUnderscoreName});
  }
  public void set${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault value) {
    takeStoredValueForKey(value, Key.${relationship.uppercaseUnderscoreName});
  }

#end
#end
#foreach ($relationship in $entity.sortedClassToManyRelationships)
#if (!$relationship.inherited) 
  public NSArray<${relationship.actualDestination.classNameWithDefault}> ${relationship.name}() {
    return (NSArray<${relationship.actualDestination.classNameWithDefault}>)storedValueForKey(Key.${relationship.uppercaseUnderscoreName});
  }
  public void addTo${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault object) {
      includeObjectIntoPropertyWithKey(object, Key.${relationship.uppercaseUnderscoreName});
  }
  public void removeFrom${relationship.capitalizedName}($relationship.actualDestination.classNameWithDefault object) {
      excludeObjectFromPropertyWithKey(object, Key.${relationship.uppercaseUnderscoreName});
  }
#end

#end
}
