open module org.wocommunity.wonder.ermodernlook {
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
	requires org.wocommunity.wonder.ermoderndirecttoweb;

	exports er.modern.look.components;
	exports er.modern.look.pages;
}