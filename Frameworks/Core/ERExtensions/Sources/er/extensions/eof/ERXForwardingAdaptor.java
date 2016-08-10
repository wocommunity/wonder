//
//  ERXForwardingAdaptor.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.extensions.eof;

import java.lang.reflect.Method;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

public abstract class ERXForwardingAdaptor extends EOAdaptor {
	protected abstract String forwardedAdaptorName();

	private EOAdaptor _forwardedAdaptor;

	public ERXForwardingAdaptor(String name) {
		super(name);
		Object delegate = delegate();
		_forwardedAdaptor = EOAdaptor.adaptorWithName(forwardedAdaptorName());
		if (delegate != null) {
			_forwardedAdaptor.setDelegate(delegate);
		}
	}

	@Override
	public EOSQLExpressionFactory expressionFactory() {
		return _forwardedAdaptor.expressionFactory();
	}

	@Override
	public EOSchemaGeneration synchronizationFactory() {
		return _forwardedAdaptor.synchronizationFactory();
	}

	@Override
	public EOSynchronizationFactory schemaSynchronizationFactory() {
		try {
			Method schemaSynchronizationFactoryMethod = _forwardedAdaptor.getClass().getMethod("schemaSynchronizationFactory");
			return (EOSynchronizationFactory) schemaSynchronizationFactoryMethod.invoke(_forwardedAdaptor);
		}
		catch (Throwable e) {
			throw new RuntimeException("Failed to retrieve schemaSynchronizationFactory.", e);
		}
	}

	@Override
	public NSArray prototypeAttributes() {
		return _forwardedAdaptor.prototypeAttributes();
	}

	public EOAdaptor forwardedAdaptor() {
		return _forwardedAdaptor;
	}

	@Override
	public EOAdaptorContext createAdaptorContext() {
		return _forwardedAdaptor.createAdaptorContext();
	}

	@Override
	public void handleDroppedConnection() {
		_forwardedAdaptor.handleDroppedConnection();
	}

	@Override
	public Class expressionClass() {
		return _forwardedAdaptor.expressionClass();
	}

	@Override
	public Class defaultExpressionClass() {
		return _forwardedAdaptor.defaultExpressionClass();
	}

	@Override
	public boolean isValidQualifierType(String typeName, EOModel model) {
		return _forwardedAdaptor.isValidQualifierType(typeName, model);
	}

	@Override
	public void assertConnectionDictionaryIsValid() {
		_forwardedAdaptor.assertConnectionDictionaryIsValid();
	}

	@Override
	public boolean hasOpenChannels() {
		return _forwardedAdaptor.hasOpenChannels();
	}

	@Override
	public NSDictionary connectionDictionary() {
		return _forwardedAdaptor.connectionDictionary();
	}

	@Override
	public void setConnectionDictionary(NSDictionary dictionary) {
		_forwardedAdaptor.setConnectionDictionary(dictionary);
	}

	@Override
	public boolean canServiceModel(EOModel model) {
		return _forwardedAdaptor.canServiceModel(model);
	}

	@Override
	public Object fetchedValueForValue(Object value, EOAttribute att) {
		return _forwardedAdaptor.fetchedValueForValue(value, att);
	}

	@Override
	public String fetchedValueForStringValue(String value, EOAttribute att) {
		return _forwardedAdaptor.fetchedValueForStringValue(value, att);
	}

	@Override
	public Number fetchedValueForNumberValue(Number value, EOAttribute att) {
		return _forwardedAdaptor.fetchedValueForNumberValue(value, att);
	}

	@Override
	public NSTimestamp fetchedValueForDateValue(NSTimestamp value, EOAttribute att) {
		return _forwardedAdaptor.fetchedValueForDateValue(value, att);
	}

	@Override
	public NSData fetchedValueForDataValue(NSData value, EOAttribute att) {
		return _forwardedAdaptor.fetchedValueForDataValue(value, att);
	}

	@Override
	public boolean isDroppedConnectionException(Exception exception) {
		return _forwardedAdaptor.isDroppedConnectionException(exception);
	}

	@Override
	public Object delegate() {
		return (_forwardedAdaptor == null) ? super.delegate() : _forwardedAdaptor.delegate();
	}

	@Override
	public void setDelegate(Object delegate) {
		if (_forwardedAdaptor != null) {
			_forwardedAdaptor.setDelegate(delegate);
		}
		else {
			super.setDelegate(delegate);
		}
	}

	@Override
	public String internalTypeForExternalType(String extType, EOModel model) {
		return _forwardedAdaptor.internalTypeForExternalType(extType, model);
	}

	@Override
	public NSArray externalTypesWithModel(EOModel model) {
		return _forwardedAdaptor.externalTypesWithModel(model);
	}

	@Override
	public void assignExternalTypeForAttribute(EOAttribute attribute) {
		_forwardedAdaptor.assignExternalTypeForAttribute(attribute);
	}

	@Override
	public void assignExternalInfoForAttribute(EOAttribute attribute) {
		_forwardedAdaptor.assignExternalInfoForAttribute(attribute);
	}

	@Override
	public void assignExternalInfoForEntity(EOEntity entity) {
		_forwardedAdaptor.assignExternalInfoForEntity(entity);
	}

	@Override
	public void assignExternalInfoForEntireModel(EOModel model) {
		_forwardedAdaptor.assignExternalInfoForEntireModel(model);
	}

	@Override
	public void dropDatabaseWithAdministrativeConnectionDictionary(NSDictionary administrativeConnectionDictionary) {
		_forwardedAdaptor.dropDatabaseWithAdministrativeConnectionDictionary(administrativeConnectionDictionary);
	}

	@Override
	public void createDatabaseWithAdministrativeConnectionDictionary(NSDictionary administrativeConnectionDictionary) {
		_forwardedAdaptor.createDatabaseWithAdministrativeConnectionDictionary(administrativeConnectionDictionary);
	}

	@Override
	public NSDictionary administrativeConnectionDictionaryForAdaptor(EOAdaptor adaptor) {
		return _forwardedAdaptor.administrativeConnectionDictionaryForAdaptor(adaptor);
	}

}