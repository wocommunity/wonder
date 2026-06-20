module org.wocommunity.wonder.ercorebusinesslogic {
	exports er.corebusinesslogic.migrations;
	exports er.corebusinesslogic.audittrail;
	exports er.javamail.mailer;
	exports er.corebusinesslogic;

	requires jakarta.mail;
	requires java.sql;
	requires org.apache.commons.lang3;
	requires org.slf4j;
	requires org.wocommunity.webobjects.directtoweb;
	requires org.wocommunity.webobjects.eoaccess;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.directtoweb;
	requires org.wocommunity.wonder.erextensions;
	requires org.wocommunity.wonder.erjavamail;
}