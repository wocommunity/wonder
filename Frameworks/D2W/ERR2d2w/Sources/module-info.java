open module org.wocommunity.wonder.err2d2w {
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.webobjects.woextensions;
	requires org.wocommunity.webobjects.dtwgeneration;
	requires org.wocommunity.webobjects.eoproject;
	requires org.wocommunity.webobjects.directtoweb;
	requires org.wocommunity.wonder.directtoweb;
	requires org.wocommunity.wonder.erextensions;
	requires org.wocommunity.wonder.erattachment;

	exports er.r2d2w;
	exports er.r2d2w.assignments;
	exports er.r2d2w.components;
	exports er.r2d2w.components.buttons;
	exports er.r2d2w.components.calendar;
	exports er.r2d2w.components.charting;
	exports er.r2d2w.components.misc;
	exports er.r2d2w.components.relationships;
	exports er.r2d2w.components.repetitions;
	exports er.r2d2w.delegates;
	exports er.r2d2w.foundation;
	exports er.r2d2w.interfaces;
	exports er.r2d2w.pages;
}