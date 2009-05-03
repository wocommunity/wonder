package er.rest;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

public class BeanInfoClassDescription extends EOClassDescription {
	private BeanInfo _beanInfo;

	public BeanInfoClassDescription(Class clazz) {
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

	@Override
	public String entityName() {
		return _beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
	}

	protected boolean isAttribute(PropertyDescriptor descriptor) {
		return ERXRestUtils.isPrimitive(descriptor.getPropertyType());
	}

	protected boolean isToMany(PropertyDescriptor descriptor) {
		return List.class.isAssignableFrom(descriptor.getPropertyType());
	}

	@Override
	public NSArray attributeKeys() {
		NSMutableArray<String> attributes = new NSMutableArray<String>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (isAttribute(descriptor)) {
				attributes.addObject(descriptor.getName());
			}
		}
		return attributes;
	}

	@Override
	public NSArray toOneRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<String>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (!isAttribute(descriptor) && !isToMany(descriptor)) {
				relationships.addObject(descriptor.getName());
			}
		}
		return relationships;
	}

	@Override
	public NSArray toManyRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<String>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (isToMany(descriptor)) {
				relationships.addObject(descriptor.getName());
			}
		}
		return relationships;
	}

	@Override
	public EOClassDescription classDescriptionForDestinationKey(String detailKey) {
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(detailKey)) {
				return ERXRestClassDescriptionFactory.classDescriptionForClass(descriptor.getPropertyType(), false);
			}
		}
		return null;
	}

	public Object createInstance() {
		try {
			return _beanInfo.getBeanDescriptor().getBeanClass().newInstance();
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

}
