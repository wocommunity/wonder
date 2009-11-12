package er.memoryadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSDictionary;

/**
 * ERMemoryAdaptor is an EOAdaptor implementation that runs entirely in memory.  There is currently
 * no persistent datastore, but this provides a useful mechanism for writing testcases or simply
 * testing a model without the overhead of setting up a database.  Internally, ERMemoryAdaptor
 * just keeps a dictionary that maps entities to arrays of row dictionaries (it looks a lot like
 * the snapshot cache).
 * 
 * @author mschrag
 */
public class ERMemoryAdaptor extends EOAdaptor {
  public ERMemoryAdaptor(String name) {
    super(name);
  }

  @Override
  public void setConnectionDictionary(NSDictionary dictionary) {
    if (dictionary == null) {
      super.setConnectionDictionary(NSDictionary.EmptyDictionary);
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
    return new ERMemoryAdaptorContext(this);
  }

  @Override
  public boolean isValidQualifierType(String typeName, EOModel model) {
    return true;
  }

  // Required for Migrations
  @Override
  public Class defaultExpressionClass() {
    return ERMemoryExpression.class;
  }

  @Override
  public EOSQLExpressionFactory expressionFactory() {
    return new ERMemoryExpressionFactory(this);
  }

  @Override
  public EOSchemaGeneration synchronizationFactory() {
    return new ERMemorySynchronizationFactory(this);
  }

  public EOSynchronizationFactory schemaSynchronizationFactory() {
    return new ERMemorySynchronizationFactory(this);
  }
}
