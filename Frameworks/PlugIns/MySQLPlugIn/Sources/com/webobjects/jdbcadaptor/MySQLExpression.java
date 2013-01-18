
package com.webobjects.jdbcadaptor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSStringUtilities;

/**
 * <p>
 * Overrides the default EOF MySQLExpression to adjust the sql generation of
 * string comparisons.
 * </p>
 * <p>
 * MySQL's default behaviour, as per usual, is to ignore the standards. Thus an
 * sql <code>like</code> performs a case insensitive comparison rather than case
 * sensitive which is vexing.
 * </p>
 * <p>
 * To enforce standard behaviour you can tell mysql by using
 * <code>like binary</code> for a case sensitive comparison. Thus a case
 * insensitive comparison simply requires using a <code>like</code> in order to
 * obtain the results desired.
 * </p>
 * <p>
 * <b>Note</b>: This assumes that you're not using binary columns.
 * </p>
 * <p>
 * The alternative is to define all of your columns as binary columns and
 * specifically specify the collation to use for case insensitive comparisons.
 * But that's a more complex approach in the author's view.
 * </p>
 * <p>
 * Another approach is to only ever use qualifiers and sort orderings that are
 * case sensitive (semantically) and choose in your model what external type to
 * map to in order to control the behaviour. In my view this is bad practice
 * because you're separating the logic of queries and returning results that are
 * not intended according to the code.
 * </p>
 * <p>
 * <p>
 * To summarise, if this class is enabled:
 * </p>
 * <ul>
 * <li>LIKE UPPER(foo) becomes LIKE foo
 * <li>LIKE foo becomes LIKE BINARY foo
 * </ul>
 * 
 * @property com.webobjects.jdbcadaptor.MySQLExpression.enable set to <code>true</code> to enable this class and change the behavior of the
 * <code>like</code> operator to be case-insensitive.
 * 
 * @author ldeck
 */
public class MySQLExpression
    extends
        com.webobjects.jdbcadaptor.MySQLPlugIn.MySQLExpression
{

	private static final List< NSSelector > SORT_ORDERING_ASC_SELECTORS =
	    Arrays.asList( EOSortOrdering.CompareAscending, EOSortOrdering.CompareCaseInsensitiveAscending );
	private static final List< NSSelector > SORT_ORDERING_BIN_SELECTORS = Arrays.asList( EOSortOrdering.CompareAscending, EOSortOrdering.CompareDescending );
	private static final List< NSSelector > SORT_ORDERING_DESC_SELECTORS =
	    Arrays.asList( EOSortOrdering.CompareCaseInsensitiveDescending, EOSortOrdering.CompareDescending );
	
	private final Pattern likeOperatorRegex;
	private final Pattern upperFunctionNameRegex;
	
	/**
	 * @param entity
	 */
	public MySQLExpression( EOEntity entity )
	{
		super( entity );
		upperFunctionNameRegex = Pattern.compile( "\\Q" + _upperFunctionName + "\\E\\(([^\\)]+)\\)" );
		likeOperatorRegex = Pattern.compile( "([Ll][Ii][Kk][Ee])" );
	}
	
	/**
	 * @see com.webobjects.eoaccess.EOSQLExpression#addOrderByAttributeOrdering(com.webobjects.eocontrol.EOSortOrdering)
	 */
	@Override
	public void addOrderByAttributeOrdering( EOSortOrdering sortOrdering )
	{
		NSSelector< ? > selector = sortOrdering.selector();
		String attPath = sortOrdering.key();
		String sqlString1 = sqlStringForAttributeNamed( attPath );
		if ( sqlString1 == null )
		{
			throw new IllegalStateException( new StringBuilder()
			    .append( "addOrderByAttributeOrdering: attempt to generate SQL for " )
			    .append( sortOrdering.getClass().getName() )
			    .append( " " )
			    .append( sortOrdering )
			    .append( " failed because attribute identified by key '" )
			    .append( sortOrdering.key() )
			    .append( "' was not reachable from from entity '" )
			    .append( _entity.name() )
			    .append( "'" )
			    .toString() );
		}
		String format;
		boolean isAscending = false;
		if ( ( isAscending = SORT_ORDERING_ASC_SELECTORS.contains( selector ) ) || SORT_ORDERING_DESC_SELECTORS.contains( selector ) )
		{
			String orderType = isAscending ? "ASC" : "DESC";
			String binaryOperator =
			    SORT_ORDERING_BIN_SELECTORS.contains( selector )
			    && entity()._attributeForPath( attPath ).adaptorValueType() == EOAttribute.AdaptorCharactersType ? "BINARY " : "";
			
			format = _NSStringUtilities.concat( binaryOperator, " ", sqlString1, " ", orderType );
		}
		else
		{
			format = _NSStringUtilities.concat( "(", sqlString1, ")" );
		}
		appendItemToListString( format, _orderByString() );
	}
	
	/**
	 * @see com.webobjects.jdbcadaptor.JDBCExpression#appendItemToOrderByString(java.lang.String)
	 */
	@Override
	protected void appendItemToOrderByString( String sqlString )
	{
		super.appendItemToOrderByString( replaceStringForCaseInsensitiveLike( sqlString ) );
	}
	
	protected Pattern likeOperatorRegex()
	{
		return likeOperatorRegex;
	}
	
	protected String replaceStringForCaseInsensitiveLike( String string )
	{
		StringBuffer result = new StringBuffer();
		
		Matcher matcher = upperFunctionNameRegex().matcher( string );
		while ( matcher.find() )
		{
			matcher.appendReplacement( result, Matcher.quoteReplacement( matcher.group( 1 ) ) );
		}
		matcher.appendTail( result );
		
		return result.toString();
	}
	
	protected String replaceStringForCaseSensitiveLike( String string )
	{
		StringBuffer result = new StringBuffer();
		
		Matcher matcher = likeOperatorRegex().matcher( string );
		while ( matcher.find() )
		{
			matcher.appendReplacement( result, Matcher.quoteReplacement( matcher.group( 1 ) + " BINARY" ) );
		}
		matcher.appendTail( result );
		
		return result.toString();
	}
	
	/**
	 * @see com.webobjects.eoaccess.EOSQLExpression#sqlStringForCaseInsensitiveLike(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String sqlStringForCaseInsensitiveLike( String valueString, String keyString )
	{
		return replaceStringForCaseInsensitiveLike( super.sqlStringForCaseInsensitiveLike( valueString, keyString ) );
	}
	
	/**
	 * @see com.webobjects.eoaccess.EOSQLExpression#sqlStringForKeyComparisonQualifier(com.webobjects.eocontrol.EOKeyComparisonQualifier)
	 */
	@Override
	public String sqlStringForKeyComparisonQualifier( EOKeyComparisonQualifier qualifier )
	{
		String leftKey = qualifier.leftKey();
		String rightKey = qualifier.rightKey();
		if ( leftKey != null && leftKey.equals( rightKey ) )
		{
			return "(1=1)";
		}
		EOAttribute att = _entity._attributeForPath( leftKey );
		String leftKeyString = sqlStringForAttributeNamed( leftKey );
		if ( leftKeyString == null )
		{
			throw new IllegalStateException( new StringBuilder()
			    .append( "sqlStringForKeyComparisonQualifier: attempt to generate SQL for " )
			    .append( qualifier.getClass().getName() )
			    .append( " " )
			    .append( qualifier )
			    .append( " failed because attribute identified by key '" )
			    .append( leftKey )
			    .append( "' was not reachable from from entity '" )
			    .append( _entity.name() )
			    .append( "'" )
			    .toString() );
		}
		leftKeyString = formatSQLString( leftKeyString, att.readFormat() );
		att = _entity._attributeForPath( rightKey );
		String rightKeyString = sqlStringForAttributeNamed( rightKey );
		if ( rightKeyString == null )
		{
			throw new IllegalStateException( new StringBuilder()
			    .append( "sqlStringForKeyComparisonQualifier: attempt to generate SQL for " )
			    .append( qualifier.getClass().getName() )
			    .append( " " )
			    .append( qualifier )
			    .append( " failed because attribute identified by key '" )
			    .append( rightKey )
			    .append( "' was not reachable from from entity '" )
			    .append( _entity.name() )
			    .append( "'" )
			    .toString() );
		}
		else
		{
			rightKeyString = formatSQLString( rightKeyString, att.readFormat() );
			String operatorString = sqlStringForSelector( qualifier.selector(), null );
			
			EOAttribute leftAttribute = _entity._attributeForPath( leftKey );
			EOAttribute rightAttribute = _entity._attributeForPath( rightKey );
			
			NSSelector qualifierSelector = qualifier.selector();
			boolean isLike =
			    qualifierSelector.equals( EOQualifier.QualifierOperatorLike ) || qualifierSelector.equals( EOQualifier.QualifierOperatorCaseInsensitiveLike );
			boolean isStringComparison =
			    isLike
			    || EOAttribute.AdaptorCharactersType == leftAttribute.adaptorValueType()
			    || EOAttribute.AdaptorCharactersType == rightAttribute.adaptorValueType();
			
			String binaryOperator = isStringComparison ? "BINARY " : "";
			
			return _NSStringUtilities.concat( binaryOperator, leftKeyString, " ", operatorString, " ", rightKeyString );
		}
	}
	
	/**
	 * @see com.webobjects.eoaccess.EOSQLExpression#sqlStringForKeyValueQualifier(com.webobjects.eocontrol.EOKeyValueQualifier)
	 */
	@Override
	public String sqlStringForKeyValueQualifier( EOKeyValueQualifier qualifier )
	{
		String key = qualifier.key();
		String keyString = sqlStringForAttributeNamed( key );
		if ( keyString == null )
		{
			throw new IllegalStateException( new StringBuilder()
			    .append( "sqlStringForKeyValueQualifier: attempt to generate SQL for " )
			    .append( qualifier.getClass().getName() )
			    .append( " " )
			    .append( qualifier )
			    .append( " failed because attribute identified by key '" )
			    .append( key )
			    .append( "' was not reachable from from entity '" )
			    .append( _entity.name() )
			    .append( "'" )
			    .toString() );
		}
		Object qualifierValue = qualifier.value();
		if ( qualifierValue instanceof EOQualifierVariable )
		{
			throw new IllegalStateException( new StringBuilder()
			    .append( "sqlStringForKeyValueQualifier: attempt to generate SQL for " )
			    .append( qualifier.getClass().getName() )
			    .append( " " )
			    .append( qualifier )
			    .append( " failed because the qualifier variable '$" )
			    .append( ( ( EOQualifierVariable )qualifierValue ).key() )
			    .append( "' is unbound." )
			    .toString() );
		}
		
		EOAttribute attribute = _entity._attributeForPath( key );
		
		keyString = formatSQLString( keyString, attribute.readFormat() );
		NSSelector qualifierSelector = qualifier.selector();
		boolean isLike =
		    qualifierSelector.equals( EOQualifier.QualifierOperatorLike ) || qualifierSelector.equals( EOQualifier.QualifierOperatorCaseInsensitiveLike );
		boolean isStringComparison = isLike || EOAttribute.AdaptorCharactersType == attribute.adaptorValueType();
		
		Object value;
		if ( isLike )
		{
			value = sqlPatternFromShellPattern( ( String )qualifierValue );
		}
		else
		{
			value = qualifierValue;
		}
		String string;
		if ( qualifierSelector.equals( EOQualifier.QualifierOperatorCaseInsensitiveLike ) )
		{
			String valueString = sqlStringForValue( value, key );
			String operatorString = sqlStringForSelector( qualifierSelector, value );
			string = sqlStringForCaseInsensitiveLike( valueString, keyString );
		}
		else if ( EOQualifier.QualifierOperatorLike.equals( qualifierSelector ) || isStringComparison )
		{
			String valueString = sqlStringForValue( value, key );
			String operatorString = sqlStringForSelector( qualifierSelector, value );
			string = _NSStringUtilities.concat( "BINARY ", keyString, " ", operatorString, " ", valueString );
		}
		else
		{
			String valueString = sqlStringForValue( value, key );
			String operatorString = sqlStringForSelector( qualifierSelector, value );
			string = _NSStringUtilities.concat( keyString, " ", operatorString, " ", valueString );
		}
		if ( isLike )
		{
			char escapeChar = sqlEscapeChar();
			if ( escapeChar != 0 )
			{
				string = _NSStringUtilities.concat( string, new StringBuilder().append( " ESCAPE '" ).append( escapeChar ).append( "'" ).toString() );
			}
		}
		return string;
	}
	
	protected Pattern upperFunctionNameRegex()
	{
		return upperFunctionNameRegex;
	}
	
}
