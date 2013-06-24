//
// ERXSoundexQualifier.java
// Project ERXSoundexQualifier
//
// Created by jvaillancourt on Sat May 20 2012
//
package er.extensions.eof.qualifiers;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSCoder;

import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * The ERXSoundexQualifier is useful for creating qualifiers that
 * will generate SQL using the 'SOUNDEX' keyword.<br>
 * <br>
 * For example constructing this qualifer:<br>
 * <code>ERXSoundexQualifier q = new ERXSoundexQualifier("CITY", "montréal");</code>
 * Then this qualifier would generate SQL of the form:
 * SOUNDEX(CITY) = SOUNDEX('montréal')
 * <br>
 * <b>NOTE:</b>
 * <br><u>MIGTH BE DATABASE DEPENDANT</u>, this syntax has been validated against Oracle 10+
 *
 */
// ENHANCEME: Should support restrictive qualifiers, don't need to subclass KeyValueQualifier
public class ERXSoundexQualifier extends ERXKeyValueQualifier implements Cloneable {

	/** register SQL generation support for the qualifier */
    static {
        EOQualifierSQLGeneration.Support.setSupportForClass(new SoundexQualifierQualifierSQLGenerationSupport(), ERXSoundexQualifier.class);
    }

    //	===========================================================================
    //	Constant(s)
    //	---------------------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    /** holds the soundex sql string */
    private static final String	SoundexBeginStatement = " SOUNDEX(";
    private static final String	SoundexEndStatement = ") ";

    /** holds the = sql string */
    private static final String	Separator = " = ";

    //	===========================================================================
    //	Class variable(s)
    //	---------------------------------------------------------------------------

    //	===========================================================================
    //	Instance variable(s)
    //	---------------------------------------------------------------------------


    /**
    * Constructs an soundex qualifer for a given
     * attribute name and a value.
     * @param key attribute name
     * @param value a value
     */
    public ERXSoundexQualifier(final String key, final Object value) {
        super(key, EOQualifier.QualifierOperatorEqual, value);
        setKey(key);
        setValue(value);
    }

    //	===========================================================================
    //	Instance method(s)
    //	---------------------------------------------------------------------------

    /**
        * Gets the key to qualify against.
        * @return qualifier key
        */
    public String key() {
        return _key;
    }

    /**
        * Sets the qualification key.
        * @param aValue for the qualification key.
        */
    public void setKey(String aValue) {
        _key = aValue;
    }

    /**
        * Gets the value.
        * @return value.
        */
    public Object value() {
        return _value;
    }

    /**
        * Sets the value.
        * @param aValue new value
        */
    public void setValue(Object aValue) {
    	_value = aValue;
    }

    /**
    * String representation of the in
     * qualifier.
     * @return string description of the qualifier
     */
    public String toString() {
        return " <" + getClass().getName() + " key: " + key() + " = SOUNDEX '" + value() + "'";
    }

    /*
     * EOF seems to be wanting to clone qualifiers when
     * they are inside an and-or qualifier without this
     * method, ERXSoundexQualifier is cloned into
     * an EOKeyValueQualifier and the generated SQL is incorrect..
     * @return cloned primary key list qualifier.
     */
    public Object clone() {
        return new ERXSoundexQualifier(key(), value());
    }

    /**
     * Adds SQL generation support. Note that the database needs to support
     * the SOUNDEX operator.
     */
    public static class SoundexQualifierQualifierSQLGenerationSupport extends EOQualifierSQLGeneration._KeyValueQualifierSupport {

        /**
         * Public constructor
         */
        public SoundexQualifierQualifierSQLGenerationSupport() {
            super();
        }


        /**
         * Generates the SQL string for an ERXSoundexQualifier.
         * @param eoqualifier a soundex qualifier
         * @param aSQLExpression current eo sql expression
         * @return SQL for the current qualifier.
         */
        public String sqlStringForSQLExpression(final EOQualifier eoqualifier, final EOSQLExpression aSQLExpression) {
            if ( ( aSQLExpression != null ) && ( aSQLExpression.entity() != null ) )
            {
            	ERXSoundexQualifier soundexQualifier = (ERXSoundexQualifier)eoqualifier;
                EOEntity	anEntity = aSQLExpression.entity();
                String		aKey = soundexQualifier.key();
                Object		aValue = soundexQualifier.value();

                if ( ( aKey != null ) && ( aValue != null ) )
                {
                    StringBuffer		aBuffer = new StringBuffer();
                    EOKeyValueQualifier	aQualifier = new EOKeyValueQualifier( aKey, EOQualifier.QualifierOperatorEqual, aValue );

                    aQualifier = (EOKeyValueQualifier) anEntity.schemaBasedQualifier( aQualifier );

                    aBuffer.append( ERXSoundexQualifier.SoundexBeginStatement );

                    aBuffer.append( aSQLExpression.sqlStringForAttributeNamed( aQualifier.key() ) );

                    aBuffer.append( ERXSoundexQualifier.SoundexEndStatement );

                    aBuffer.append( ERXSoundexQualifier.Separator );

                    aBuffer.append( ERXSoundexQualifier.SoundexBeginStatement );

                    aBuffer.append( aSQLExpression.sqlStringForValue( aQualifier.value(), aQualifier.key() ) );

                    aBuffer.append( ERXSoundexQualifier.SoundexEndStatement );

                    return aBuffer.toString();
                }
            }

            return null;
        }

        // ENHANCEME: This should support restrictive qualifiers on the root entity
        public EOQualifier schemaBasedQualifierWithRootEntity(final EOQualifier eoqualifier, final EOEntity eoentity) {
            return (EOQualifier)eoqualifier.clone();
            }

        public EOQualifier qualifierMigratedFromEntityRelationshipPath(final EOQualifier eoqualifier, final EOEntity eoentity, final String s) {
            // the key migration is the same as for EOKeyValueQualifier
            ERXSoundexQualifier soundexQualifier=(ERXSoundexQualifier)eoqualifier;
            String newPath = EOQualifierSQLGeneration.Support._translateKeyAcrossRelationshipPath(soundexQualifier.key(), s, eoentity);
            return new ERXSoundexQualifier(newPath, soundexQualifier.value());
        }
    }

    @Override
    public Class classForCoder() {
    	return getClass();
    }

	public static Object decodeObject(final NSCoder coder) {
		String key = (String) coder.decodeObject();
		NSArray values = (NSArray)coder.decodeObject();
		return new ERXSoundexQualifier(key, values);
	}

	@Override
	public void encodeWithCoder(final NSCoder coder) {
		coder.encodeObject(key());
		coder.encodeObject(value());
	}

	@Override
	public void encodeWithKeyValueArchiver(final EOKeyValueArchiver archiver) {
		archiver.encodeObject(key(), "key");
		archiver.encodeObject(value(), "value");
	}

	public static Object decodeWithKeyValueUnarchiver(final EOKeyValueUnarchiver unarchiver) {
		return new ERXSoundexQualifier(
				(String)unarchiver.decodeObjectForKey("key"),
				unarchiver.decodeObjectForKey("value"));
	}

}
