open module org.wocommunity.wonder.errest {
	requires java.desktop;
	requires java.xml;
	requires json.lib;
	requires org.apache.commons.lang3;
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;

	exports er.rest;
	exports er.rest.format;
	exports er.rest.routes;
	exports er.rest.routes.components;
	exports er.rest.routes.jsr311;
	exports er.rest.util;
}