open module org.wocommunity.wonder.ajax {
	requires java.desktop;
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;
	
	exports er.ajax;
}