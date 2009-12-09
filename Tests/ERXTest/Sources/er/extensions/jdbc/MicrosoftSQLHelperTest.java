package er.extensions.jdbc;

import er.erxtest.ERXTestCase;
import er.extensions.jdbc.ERXSQLHelper.MicrosoftSQLHelper;

public class MicrosoftSQLHelperTest extends ERXTestCase {

	private MicrosoftSQLHelper helper;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		helper = new ERXSQLHelper.MicrosoftSQLHelper();
	}

	public void testLimitExpressionFixPaginationRange() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, "select t1.column1 from table1 as t1", 0, 20 );

		assertTrue( result.contains( "eo_rownum >= 1" ) );
		assertTrue( result.contains( "eo_rownum < 21" ) );
	}

	public void testLimitExpressionForEmptySql() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, "", 0, 0 );

		assertEquals( "", result );
	}

	public void testLimitExpressionForNullSql() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, null, 0, 0 );

		assertNull( result );
	}

	public void testLimitExpressionForSimpleSql() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, "select t1.column1 from table1 as t1", 0, 5 );

		assertEquals( "select * from (select t1.column1, row_number() over (order by t1.column1) eo_rownum from table1 as t1) as temp_row_number where eo_rownum >= 1 and eo_rownum < 6 order by eo_rownum", result );
	}

	public void testLimitExpressionForSqlWithOrderByClause() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, "select t1.column1, t1.column2 from table1 as t1 order by t1.column2", 0, 5 );

		assertEquals( "select * from (select t1.column1, t1.column2, row_number() over (order by t1.column2) eo_rownum from table1 as t1) as temp_row_number where eo_rownum >= 1 and eo_rownum < 6 order by eo_rownum", result );
	}

	public void testLimitExpressionIsCaseInsensitive() throws Exception
	{
		String result = helper.limitExpressionForSQL( null, null, "SeLecT t1.column1 FrOM table1 AS t1", 0, 5 );

		assertEquals( "select * from (select t1.column1, row_number() over (order by t1.column1) eo_rownum from table1 as t1) as temp_row_number where eo_rownum >= 1 and eo_rownum < 6 order by eo_rownum", result );
	}
}
