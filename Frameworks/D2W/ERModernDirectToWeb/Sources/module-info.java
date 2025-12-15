open module org.wocommunity.wonder.ermoderndirecttoweb {
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.webobjects.woextensions;
	requires org.wocommunity.webobjects.dtwgeneration;
	requires org.wocommunity.webobjects.eoproject;
	requires org.wocommunity.webobjects.directtoweb;
	requires org.wocommunity.wonder.ajax;
	requires org.wocommunity.wonder.directtoweb;
	requires org.wocommunity.wonder.erextensions;
	requires org.wocommunity.wonder.ercoolcomponents;

	exports er.modern.directtoweb.assignments;
	exports er.modern.directtoweb.assignments.defaults;
	exports er.modern.directtoweb.components;
	exports er.modern.directtoweb.components.buttons;
	exports er.modern.directtoweb.components.embedded;
	exports er.modern.directtoweb.components.header;
	exports er.modern.directtoweb.components.query;
	exports er.modern.directtoweb.components.relationships;
	exports er.modern.directtoweb.components.repetitions;
	exports er.modern.directtoweb.delegates;
	exports er.modern.directtoweb.interfaces;
}