

/* FSAdaptor - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/
package er.fsadaptor;
import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;

public final class FSAdaptor extends EOAdaptor
{
    public FSAdaptor(String aName) {
        super(aName);
    }

    public void assertConnectionDictionaryIsValid() {
        System.out.println(this.connectionDictionary());
    }

    public EOAdaptorContext createAdaptorContext() {
        return new FSAdaptorContext(this);
    }

    public Class defaultExpressionClass() {
        throw new UnsupportedOperationException
        ("FSAdaptor.defaultExpressionClass");
    }

    public EOSQLExpressionFactory expressionFactory() {
        throw new UnsupportedOperationException("FSAdaptor.expressionFactory");
    }

    public boolean isValidQualifierType(String aTypeName, EOModel aModel) {
        return true;
    }

    public EOSchemaGeneration synchronizationFactory() {
        throw new UnsupportedOperationException
        ("FSAdaptor.synchronizationFactory");
    }
}

