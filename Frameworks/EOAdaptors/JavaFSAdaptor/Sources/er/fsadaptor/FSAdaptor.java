package er.fsadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;

public final class FSAdaptor extends EOAdaptor {
    public FSAdaptor(String aName) {
        super(aName);
    }

    @Override
    public void assertConnectionDictionaryIsValid() {
        System.out.println(connectionDictionary());
    }

    @Override
    public EOAdaptorContext createAdaptorContext() {
        return new FSAdaptorContext(this);
    }

    @Override
    public Class defaultExpressionClass() {
        throw new UnsupportedOperationException("FSAdaptor.defaultExpressionClass");
    }

    @Override
    public EOSQLExpressionFactory expressionFactory() {
        throw new UnsupportedOperationException("FSAdaptor.expressionFactory");
    }

    @Override
    public boolean isValidQualifierType(String aTypeName, EOModel aModel) {
        return true;
    }

    @Override
    public EOSchemaGeneration synchronizationFactory() {
        throw new UnsupportedOperationException("FSAdaptor.synchronizationFactory");
    }

    @Override
    public EOSynchronizationFactory schemaSynchronizationFactory() {
      throw new UnsupportedOperationException("FSAdaptor.schemaSynchronizationFactory");
    }
}
