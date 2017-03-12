package er.cayenne.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SortOrder;

import er.cayenne.example.ChainableOrdering.ChainableOrderings;

/**
 * A key in a DataObject.
 *
 * @param <E> The type this key returns.
 */

public class Key<E> {

	/**
	 * Name of the key in the object
	 */
	private String _key;

	/**
	 * Constructs a new key with the given name.
	 */
	public Key( String key ) {
		_key = key;
	}

	/**
	 * @return Name of the key in the object.
	 */
	public String key() {
		return _key;
	}

	/**
	 * @return An expression representing null.
	 */
	public Expression isNull() {
		return ExpressionFactory.matchExp( key(), null );
	}

	/**
	 * @return An expression representing a non-null value.
	 */
	public Expression isNotNull() {
		return ExpressionFactory.matchExp( key(), null ).notExp();
	}

	/**
	 * @return An expression representing equality.
	 */
	public Expression eq( E value ) {
		return ExpressionFactory.matchExp( key(), value );
	}

	/**
	 * @return An expression representing inequality.
	 */
	public Expression ne( E value ) {
		return ExpressionFactory.noMatchExp( key(), value );
	}
	
	/**
	 * @return An expression for a Database "Like" query.
	 */
	public Expression like( E value ) {
		return ExpressionFactory.likeExp( key(), value );
	}

	/**
	 * @return An expression for a case insensitive "Like" query.
	 */
	public Expression likeInsensitive( E value ) {
		return ExpressionFactory.likeIgnoreCaseExp( key(), value );
	}

	/**
	 * @return An expression applying to objects between the two given bounds (not inclusive)
	 *
	 * @param lower The lower bound. [null] represents infinity.
	 * @param upper The upper bound. [null] represents infinity.
	 */
	public Expression between( E lower, E upper ) {
		return between( lower, upper, false );
	}

	/**
	 * @return An expression applying to objects between the two given bounds (not inclusive)
	 *
	 * @param lower The lower bound. [null] represents infinity.
	 * @param upper The upper bound. [null] represents infinity.
	 */
	public Expression between( E lower, E upper, boolean inclusive ) {
		List<Expression> expressions = new ArrayList<>();

		if( lower != null ) {
			if( inclusive ) {
				expressions.add( gte( lower ) );
			}
			else {
				expressions.add( gt( lower ) );
			}
		}

		if( upper != null ) {
			if( inclusive ) {
				expressions.add( lte( upper ) );
			}
			else {
				expressions.add( lt( upper ) );
			}
		}

		return andExpressions( expressions );
	}

	/**
	 * @return An expression for finding objects with values in the given set.
	 */
	public Expression in( E ... values ) {
		return ExpressionFactory.inExp( key(), values );
	}

	/**
	 * @return A greater than Expression.
	 */
	public Expression gt( E value ) {
		return ExpressionFactory.greaterExp( key(), value );
	}

	/**
	 * @return A greater than or equal to Expression.
	 */
	public Expression gte( E value ) {
		return ExpressionFactory.greaterOrEqualExp( key(), value );
	}

	/**
	 * @return A less than Expression.
	 */
	public Expression lt( E value) {
		return ExpressionFactory.lessExp( key(), value );
	}

	/**
	 * @return A less than or equal to Expression.
	 */
	public Expression lte( E value) {
		return ExpressionFactory.lessOrEqualExp( key(), value );
	}

	/**
	 * @return Ascending sort orderings on this key.
	 */
	public ChainableOrdering asc() {
		return new ChainableOrdering( key(), SortOrder.ASCENDING );
	}

	/**
	 * @return Ascending sort orderings on this key.
	 */
	public ChainableOrderings ascs() {
		return new ChainableOrderings( asc() );
	}

	/**
	 * @return Ascending case insensitive sort orderings on this key.
	 */
	public ChainableOrdering ascInsensitive() {
		return new ChainableOrdering( key(), SortOrder.ASCENDING_INSENSITIVE );
	}

	/**
	 * @return Ascending case insensitive sort orderings on this key.
	 */
	public ChainableOrderings ascInsensitives() {
		return new ChainableOrderings( ascInsensitive() );
	}

	/**
	 * @return Descending sort orderings on this key.
	 */
	public ChainableOrdering desc() {
		return new ChainableOrdering( key(), SortOrder.DESCENDING );
	}

	/**
	 * @return Descending sort orderings on this key.
	 */
	public ChainableOrderings descs() {
		return new ChainableOrderings(desc() );
	}

	/**
	 * @return Descending case insensitive sort orderings on this key.
	 */
	public ChainableOrdering descInsensitive() {
		return new ChainableOrdering( key(), SortOrder.DESCENDING_INSENSITIVE );
	}

	/**
	 * @return Descending case insensitive sort orderings on this key.
	 */
	public ChainableOrderings descInsensitives() {
		return new ChainableOrderings( descInsensitive() );
	}

	/**
	 * @return An expression with the given expressions combined with logical "and".
	 */
	private static Expression andExpressions( List<Expression> expressions ) {
		Expression resultExpression = null;

		for( Expression e : expressions ) {
			if( resultExpression == null ) {
				resultExpression = e;
			}
			else {
				resultExpression = resultExpression.andExp( e );
			}
		}

		return resultExpression;
	}

	public String dot( String key ) {
		return key() + "." + key;
	}

	public String dot( Key<?> key ) {
		return key() + "." + key.key();
	}
}