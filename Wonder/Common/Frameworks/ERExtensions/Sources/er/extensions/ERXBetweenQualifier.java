//
//	===========================================================================
//
//	Title:		ERXBetweenQualifier.java
//	Description:	[Description]
//	Author:		Petite Abeille
//	Creation Date:	Mon 20-Aug-2001
//	Legal:		Copyright (C) 2001 Petite Abeille. All Rights Reserved.
//			This class is hereby released for all uses.
//			No warranties whatsoever.
//	Motto:		"Victory belongs to those who believe in it the longest"
//
//	---------------------------------------------------------------------------
//
package er.extensions;

import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOClassDescription;

import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOEntity;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSTimestamp;

/**
 * The between qualifier allows qualification on an
 * attribute that is between two values. This qualifier
 * supports both in-memory and sql based qualification.
 *
 * The SQL generated is of the form:
 * "FOO BETWEEN 1 AND 3"
 *
 * Note this qualifier supports qualifing against String, Number 
 * and NSTimestamp values.
 */
public class ERXBetweenQualifier extends EOQualifier implements EOQualifierSQLGeneration, EOQualifierEvaluation, Cloneable
{

//	===========================================================================
//	Constant(s)
//	---------------------------------------------------------------------------

        /** holds the between sql string */
	private static final String	BetweenStatement = " BETWEEN ";
        
        /** holds the and sql string */        
	private static final String	Separator = " AND ";

//	===========================================================================
//	Class variable(s)
//	---------------------------------------------------------------------------

//	===========================================================================
//	Instance variable(s)
//	---------------------------------------------------------------------------

        /** holds the key used to compare against */
	private String	_key = null;
        
        /** holds the minimun value */
	private Object	_minimumValue = null;
        
        /** holds the maximum value */
	private Object	_maximumValue = null;

//	===========================================================================
//	Constructor method(s)
//	---------------------------------------------------------------------------

        /**
         * Default public constructor.
         */
	public ERXBetweenQualifier() {
		super();
	}

        /**
         * Creates a qualifier for a given key with a
         * min and max value specified.
         * @param aKey key to qualify against
         * @param aMinimumValue minimum value of the key
         * @param aMaximumValue maximum value of the key
         */
	public ERXBetweenQualifier(String aKey, Object aMinimumValue, Object aMaximumValue) {
		this();

		this.setKey( aKey );
		this.setMinimumValue( aMinimumValue );
		this.setMaximumValue( aMaximumValue );
	}

//	===========================================================================
//	Class method(s)
//	---------------------------------------------------------------------------

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
         * Gets the minimum value.
         * @return minimum value.
         */
	public Object minimumValue() {
		return _minimumValue;
	}

        /**
         * Sets the minimum value.
         * @param aValue new minimum value
         */
	public void setMinimumValue(Object aValue) {
		_minimumValue = aValue;
	}

        /**
         * Gets the maximum value.
         * @return maximum value.
         */
	public Object maximumValue() {
		return _maximumValue;
	}

        /**
         * Sets the maximum value.
         * @param aValue new maximum value
         */
	public void setMaximumValue(Object aValue) {
		_maximumValue = aValue;
	}

//	===========================================================================
//	EOQualifier method(s)
//	---------------------------------------------------------------------------

        /**
         * Adds the qualification key of the qualifier to
         * the given set.
         * @param aSet to add the qualification key to.
         */
	public void addQualifierKeysToSet(NSMutableSet aSet) {
		if ( aSet != null )
		{
			String	aKey = this.key();

			if ( aKey != null )
			{
				aSet.addObject( aKey );
			}
		}
	}

        /**
         * Creates another qualifier after replacing the values of the bindings.
         * Since this qualifier does not support qualifier binding keys a clone
         * of the qualifier is returned.
         * @param someBindings some bindings
         * @param requiresAll tells if the qualifier requires all bindings
         * @return clone of the current qualifier.
         */    
	public EOQualifier qualifierWithBindings(NSDictionary someBindings, boolean requiresAll) {
		return (EOQualifier) this.clone();
	}
        
        /**
         * This qualifier does not perform validation. This
         * is a no-op method.
         * @param aClassDescription to validation the qualifier keys
         *		against.
         */
	// FIXME: Should do something here...
	public void validateKeysWithRootClassDescription(EOClassDescription aClassDescription) {
	}

//	===========================================================================
//	EOQualifierSQLGeneration method(s)
//	---------------------------------------------------------------------------

        /**
         * Constructs the BETWEEN sql string for sql qualification.
         * @param aSQLExpression to contruct the qualifier for.
         * @return BETWEEN sql string for the qualifier.
         */
	public String sqlStringForSQLExpression(EOSQLExpression aSQLExpression) {
		if ( ( aSQLExpression != null ) && ( aSQLExpression.entity() != null ) )
		{
			EOEntity	anEntity = aSQLExpression.entity();
			String		aKey = this.key();
			Object		aMinimumValue = this.minimumValue();
			Object		aMaximumValue = this.maximumValue();

			if ( ( aKey != null ) && ( aMinimumValue != null ) && ( aMaximumValue != null ) )
			{
				StringBuffer		aBuffer = new StringBuffer();
				EOKeyValueQualifier	aMinimumQualifier = new EOKeyValueQualifier( aKey, EOQualifier.QualifierOperatorEqual, aMinimumValue );
				EOKeyValueQualifier	aMaximumQualifier = new EOKeyValueQualifier( aKey, EOQualifier.QualifierOperatorEqual, aMaximumValue );

				aMinimumQualifier = (EOKeyValueQualifier) anEntity.schemaBasedQualifier( aMinimumQualifier );
				aMaximumQualifier = (EOKeyValueQualifier) anEntity.schemaBasedQualifier( aMaximumQualifier );

				aBuffer.append( aSQLExpression.sqlStringForAttributeNamed( aMinimumQualifier.key() ) );

				aBuffer.append( ERXBetweenQualifier.BetweenStatement );

				aBuffer.append( aSQLExpression.sqlStringForValue( aMinimumQualifier.value(), aMinimumQualifier.key() ) );

				aBuffer.append( ERXBetweenQualifier.Separator );

				aBuffer.append( aSQLExpression.sqlStringForValue( aMaximumQualifier.value(), aMaximumQualifier.key() ) );

				return aBuffer.toString();
			}
		}

		return null;
	}


        /**
         * Implementation of the EOQualifierSQLGeneration interface. Just
         * clones the qualifier.
         * @param anEntity an entity.
         * @return clone of the current qualifier.
         */
        // CHECKME: Not sure if cloning is the correct behaviour, also might want to integrate a restricting qualifer
        //		from the entity.
	public EOQualifier schemaBasedQualifierWithRootEntity(EOEntity anEntity) {
		return (EOQualifier) this.clone();
	}

        /**
         * Implementation of the EOQualifierSQLGeneration interface. Just
         * clones the qualifier.
         * @param anEntity an entity
         * @param aPath relationship path
         * @return clone of the current qualifier.
         */
	public EOQualifier qualifierMigratedFromEntityRelationshipPath(EOEntity anEntity, String aPath) {
		return (EOQualifier) this.clone();
	}

//	===========================================================================
//	EOQualifierEvaluation method(s)
//	---------------------------------------------------------------------------

        /**
         * Determines the comparator to use for a given object based
         * on the object's class.
         * @param anObject to find the comparator for
         * @return comparator to use when comparing objects of a given
         *	   class.
         */
         // ENHANCEME: Should have a way to extend this.
	protected NSComparator comparatorForObject(Object anObject) {
		if ( anObject != null ) {
			Class		anObjectClass = anObject.getClass();
			Class[]		someClasses = { String.class, Number.class, NSTimestamp.class };
			NSComparator[]	someComparators = { NSComparator.AscendingStringComparator, NSComparator.AscendingNumberComparator, NSComparator.AscendingTimestampComparator };
			int		count = someClasses.length;

			for ( int index = 0; index < count; index++ ) {
				Class	aClass = someClasses[ index ];

				if ( aClass.isAssignableFrom( anObjectClass ) == true ) {
					return someComparators[ index ];
				}
			}
		}

		return null;
	}

        /**
         * Compares an object to determine if it is within the
         * between qualification of the current qualifier. This
         * method is only used for in-memory qualification.
         * @return if the given object is within the boundries of
         *         the qualifier.
         */
	public boolean evaluateWithObject(Object anObject) {
		if ( ( anObject != null ) && ( ( anObject instanceof NSKeyValueCoding ) == true ) ) {
			String	aKey = this.key();
			Object	aMinimumValue = this.minimumValue();
			Object	aMaximumValue = this.maximumValue();

			if ( ( aKey != null ) && ( aMinimumValue != null ) && ( aMaximumValue != null ) ) {
				Object	aValue = ( (NSKeyValueCoding) anObject ).valueForKey( aKey );

				if ( aValue != null ) {
					NSComparator	aComparator = this.comparatorForObject( aValue );

					if ( aComparator != null ) {
						boolean	containsObject = false;

						try {
							int	anOrder = aComparator.compare( aMinimumValue, aValue );

							if ( ( anOrder == NSComparator.OrderedSame ) || ( anOrder == NSComparator.OrderedAscending ) )
							{
								anOrder = aComparator.compare( aMaximumValue, aValue );

								if ( ( anOrder == NSComparator.OrderedSame ) || ( anOrder == NSComparator.OrderedDescending ) )
								{
									containsObject = true;
								}
							}
						} catch(NSComparator.ComparisonException anException) {
						}

						return containsObject;
					}
				}
			}
		}

		return false;
	}

//	===========================================================================
//	Cloneable method(s)
//	---------------------------------------------------------------------------

        /**
         * Implementation of the Clonable interface.
         * @return clone of the qualifier
         */
	public Object clone() {
		ERXBetweenQualifier	aClone = new ERXBetweenQualifier();

		aClone.setKey( new String( this.key() ) );
		aClone.setMinimumValue( this.minimumValue() );
		aClone.setMaximumValue( this.maximumValue() );

		return aClone;
	}
}
