open module org.wocommunity.wonder.jdbcadaptor.microsoft {
	requires org.apache.commons.lang3;
	requires mssql.jdbc;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.jdbcadaptor;

	exports er.jdbcadaptor.microsoft;
}