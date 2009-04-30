package er.rest.routes.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.rest.ERXRestUtils;

public class ClassEntityProxy implements IERXEntity {
	private BeanInfo _beanInfo;

	public ClassEntityProxy(Class clazz) {
		if (clazz == null) {
			throw new NullPointerException("You must provide a class name.");
		}
		try {
			_beanInfo = Introspector.getBeanInfo(clazz);
		}
		catch (IntrospectionException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	protected boolean isAttribute(PropertyDescriptor descriptor) {
		return ERXRestUtils.isPrimitive(descriptor.getPropertyType());
	}

	public IERXAttribute attributeNamed(String name) {
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(name) && isAttribute(descriptor)) {
				return new BeanInfoAttributeProxy(descriptor);
			}
		}
		return null;
	}

	public NSArray<IERXAttribute> attributes() {
		NSMutableArray<IERXAttribute> attributes = new NSMutableArray<IERXAttribute>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (isAttribute(descriptor)) {
				attributes.addObject(new BeanInfoAttributeProxy(descriptor));
			}
		}
		return attributes;
	}

	public Object createInstance(EOEditingContext editingContext) {
		try {
			return _beanInfo.getBeanDescriptor().getBeanClass().newInstance();
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	public String name() {
		return _beanInfo.getBeanDescriptor().getBeanClass().getName();
	}
	
	public String shortName() {
		return _beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
	}

	public Object primaryKeyValue(Object obj) {
		//throw new UnsupportedOperationException("No idea how to give you a primary key for a java object.");
		return null;
	}
	
	public Object objectWithPrimaryKeyValue(EOEditingContext editingContext, Object pkValue) {
		throw new UnsupportedOperationException("No idea how to fetch an arbitrary java object.");
	}

	public IERXEntity parentEntity() {
		Class superclass = _beanInfo.getBeanDescriptor().getBeanClass().getSuperclass();
		return new ClassEntityProxy(superclass);
	}

	public NSArray<IERXAttribute> primaryKeyAttributes() {
		throw new UnsupportedOperationException("No idea how to give you primary key attributes for a java object.");
	}

	public NSArray<String> propertyNames() {
		NSMutableArray<String> propertyNames = new NSMutableArray<String>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			propertyNames.addObject(descriptor.getName());
		}
		return propertyNames;
	}

	public IERXRelationship relationshipNamed(String name) {
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(name) && !isAttribute(descriptor)) {
				return new BeanInfoRelationshipProxy(descriptor);
			}
		}
		return null;
	}

	public NSArray<IERXRelationship> relationships() {
		NSMutableArray<IERXRelationship> relationships = new NSMutableArray<IERXRelationship>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (!isAttribute(descriptor)) {
				relationships.addObject(new BeanInfoRelationshipProxy(descriptor));
			}
		}
		return relationships;
	}

	public IERXEntity siblingEntityNamed(String name) {
		return null;
	}

	public NSArray<IERXEntity> subEntities() {
		return new NSMutableArray<IERXEntity>(); // MS: ???
	}

}
