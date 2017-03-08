package er.rest;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

public class BeanInfoClassDescription extends EOClassDescription implements IERXNonEOClassDescription {
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
	
	@Override
	public Class classForAttributeKey(String key) {
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(key) && isAttribute(descriptor)) {
				return descriptor.getPropertyType();
			}
		}
		return null;
	}

	protected boolean isAttribute(PropertyDescriptor descriptor) {
		return isAttribute(descriptor.getPropertyType());
	}

	protected boolean isAttribute(Class type) {
		return ERXRestUtils.isPrimitive(type);
	}

	protected boolean isToMany(PropertyDescriptor descriptor) {
		return isToMany(descriptor.getPropertyType());
	}

	protected boolean isToMany(Class type) {
		return List.class.isAssignableFrom(type);
	}

	public boolean isAttributeMethod(String methodName) {
		for (MethodDescriptor descriptor : _beanInfo.getMethodDescriptors()) {
			Method descriptorMethod = descriptor.getMethod();
			Class descriptorReturnType = descriptorMethod.getReturnType();
			if (descriptor.getName().equals(methodName) && descriptorReturnType != void.class && descriptorMethod.getParameterTypes().length == 0 && isAttribute(descriptorReturnType)) {
				return true;
			}
		}
		return false;
	}

	public boolean isToOneMethod(String methodName) {
		for (MethodDescriptor descriptor : _beanInfo.getMethodDescriptors()) {
			Method descriptorMethod = descriptor.getMethod();
			Class descriptorReturnType = descriptorMethod.getReturnType();
			if (descriptor.getName().equals(methodName) && descriptorReturnType != void.class && descriptorMethod.getParameterTypes().length == 0 && !isAttribute(descriptorReturnType) && !isToMany(descriptorReturnType)) {
				return true;
			}
		}
		return false;
	}

	public boolean isToManyMethod(String methodName) {
		for (MethodDescriptor descriptor : _beanInfo.getMethodDescriptors()) {
			Method descriptorMethod = descriptor.getMethod();
			Class descriptorReturnType = descriptorMethod.getReturnType();
			if (descriptor.getName().equals(methodName) && descriptorReturnType != void.class && descriptorMethod.getParameterTypes().length == 0 && isToMany(descriptorReturnType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public NSArray<String> attributeKeys() {
		NSMutableArray<String> attributes = new NSMutableArray<>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (isAttribute(descriptor)) {
				attributes.addObject(descriptor.getName());
			}
		}
		return attributes;
	}

	@Override
	public NSArray<String> toOneRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (!isAttribute(descriptor) && !isToMany(descriptor) && !"class".equals(descriptor.getName())) {
				relationships.addObject(descriptor.getName());
			}
		}
		return relationships;
	}

	@Override
	public NSArray<String> toManyRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<>();
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (isToMany(descriptor)) {
				relationships.addObject(descriptor.getName());
			}
		}
		return relationships;
	}

	protected Class<?> toManyComponentType(Type componentType) {
		Class<?> componentTypeClass = Object.class;
		if (componentType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) componentType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				componentTypeClass = (Class<?>) typeArguments[0];
			}
		}
		return componentTypeClass;
	}

	@Override
	public EOClassDescription classDescriptionForDestinationKey(String detailKey) {
		for (PropertyDescriptor descriptor : _beanInfo.getPropertyDescriptors()) {
			if (descriptor.getName().equals(detailKey)) {
				if (isToMany(descriptor)) {
					if (descriptor instanceof IndexedPropertyDescriptor) {
						return ERXRestClassDescriptionFactory.classDescriptionForClass(((IndexedPropertyDescriptor)descriptor).getIndexedPropertyType(), true);
					}
					else {
						Type componentType = null;
						Method method = descriptor.getReadMethod();
						if (method != null) {
							componentType = method.getGenericReturnType();
						}
						else {
							method = descriptor.getWriteMethod();
							if (method != null) {
								componentType = method.getGenericParameterTypes()[0];
							}
						}
						return ERXRestClassDescriptionFactory.classDescriptionForClass(toManyComponentType(componentType), true);
					}
				}
				else {
					return ERXRestClassDescriptionFactory.classDescriptionForClass(descriptor.getPropertyType(), false);
				}
			}
		}
		
		// If we didn't find a getMethod, fall back and look for any method with that name
		for (MethodDescriptor descriptor : _beanInfo.getMethodDescriptors()) {
			Method descriptorMethod = descriptor.getMethod();
			Class<?> descriptorReturnType = descriptorMethod.getReturnType();
			if (descriptor.getName().equals(detailKey) && descriptorReturnType != void.class && descriptorMethod.getParameterTypes().length == 0) {
				if (isToMany(descriptorReturnType)) {
					return ERXRestClassDescriptionFactory.classDescriptionForClass(toManyComponentType(descriptorMethod.getGenericReturnType()), true);
				}
				else {
					return ERXRestClassDescriptionFactory.classDescriptionForClass(descriptorReturnType, false);
				}
			}
		}
		
		return null;
	}

	@Override
	public Object createInstance() {
		try {
			return _beanInfo.getBeanDescriptor().getBeanClass().newInstance();
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

}
