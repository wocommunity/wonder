open module org.wocommunity.wonder.ajax {
	requires jabsorb;
	requires java.desktop;
	requires org.json;
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;
	
	exports er.ajax;
	exports er.ajax.json;
	exports er.ajax.json.client;
	exports er.ajax.json.localarg;
	exports er.ajax.json.serializer;
}