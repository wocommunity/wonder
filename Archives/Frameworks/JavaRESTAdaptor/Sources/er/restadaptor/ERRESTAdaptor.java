package er.restadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSDictionary;

public class ERRESTAdaptor extends EOAdaptor {
  public ERRESTAdaptor(String name) {
    super(name);
  }

  @Override
  public void setConnectionDictionary(NSDictionary dictionary) {
    if (dictionary == null) {
      super.setConnectionDictionary((NSDictionary<String, Object>) NSDictionary.EmptyDictionary);
    }
    else {
      super.setConnectionDictionary(dictionary);
    }
  }

  @Override
  public void assertConnectionDictionaryIsValid() {
    // DO NOTHING
  }

  @Override
  public EOAdaptorContext createAdaptorContext() {
    return new ERRESTAdaptorContext(this);
  }

  @Override
  public Class defaultExpressionClass() {
    throw new UnsupportedOperationException("ERRESTAdaptor.defaultExpressionClass");
  }

  @Override
  public EOSQLExpressionFactory expressionFactory() {
    throw new UnsupportedOperationException("ERRESTAdaptor.expressionFactory");
  }

  @Override
  public boolean isValidQualifierType(String typeName, EOModel model) {
    return true;
  }

  @Override
  public EOSchemaGeneration synchronizationFactory() {
    throw new UnsupportedOperationException("ERRESTAdaptor.synchronizationFactory");
  }

  public EOSynchronizationFactory schemaSynchronizationFactory() {
    throw new UnsupportedOperationException("ERRESTAdaptor.schemaSynchronizationFactory");
  }
}
