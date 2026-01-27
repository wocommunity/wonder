open module org.wocommunity.wonder.erattachment {
	requires java.desktop;
	requires org.slf4j;
	requires org.apache.commons.codec;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.ajax;
	requires org.wocommunity.wonder.erextensions;

	exports er.attachment;
	exports er.attachment.components;
	exports er.attachment.components.viewers;
	exports er.attachment.migrations;
	exports er.attachment.model;
	exports er.attachment.processors;
	exports er.attachment.thumbnail;
	exports er.attachment.upload;
	exports er.attachment.utils;
}