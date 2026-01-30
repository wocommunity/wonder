open module org.wocommunity.wonder.erjavamail {
	requires java.naming;
	requires jakarta.mail;
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;

	exports er.javamail;
}