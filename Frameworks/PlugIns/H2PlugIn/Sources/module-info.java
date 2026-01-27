open module org.wocommunity.wonder.jdbcadaptor.h2 {
	requires transitive org.wocommunity.webobjects.foundation;
	requires transitive org.wocommunity.webobjects.eocontrol;
	requires transitive org.wocommunity.webobjects.eoaccess;
	requires transitive org.wocommunity.webobjects.jdbcadaptor;

	exports er.jdbcadaptor.h2;
}