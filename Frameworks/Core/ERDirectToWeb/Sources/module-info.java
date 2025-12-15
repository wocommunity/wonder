open module org.wocommunity.wonder.directtoweb {
	requires org.apache.commons.lang3;
	requires org.slf4j;
	requires ch.qos.reload4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.webobjects.directtoweb;
	requires org.wocommunity.webobjects.woextensions;
	requires org.wocommunity.wonder.erextensions;

	exports er.directtoweb;
	exports er.directtoweb.assignments;
	exports er.directtoweb.assignments.defaults;
	exports er.directtoweb.assignments.delayed;
	exports er.directtoweb.components;
	exports er.directtoweb.components.attachments;
	exports er.directtoweb.components.bool;
	exports er.directtoweb.components.buttons;
	exports er.directtoweb.components.dates;
	exports er.directtoweb.components.misc;
	exports er.directtoweb.components.numbers;
	exports er.directtoweb.components.relationships;
	exports er.directtoweb.components.repetitions;
	exports er.directtoweb.components.strings;
	exports er.directtoweb.cvs;
	exports er.directtoweb.delegates;
	exports er.directtoweb.embed;
	exports er.directtoweb.interfaces;
	exports er.directtoweb.pages;
	exports er.directtoweb.pages.templates;
	exports er.directtoweb.printerfriendly;
	exports er.directtoweb.qualifiers;
	exports er.directtoweb.xml;
}