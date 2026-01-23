open module org.wocommunity.wonder.erpersistentsessionstorage {
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;
	requires org.wocommunity.wonder.erprototypes;

	exports er.persistentsessionstorage;
	exports er.persistentsessionstorage.migrations;
	exports er.persistentsessionstorage.model;
	exports er.persistentsessionstorage.model.eogen;
}