open module org.wocommunity.wonder.erjavamail {
	requires jakarta.mail;
	requires java.naming;
	requires org.slf4j;
	requires org.wocommunity.webobjects.foundation;
	requires org.wocommunity.webobjects.eocontrol;
	requires org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;

	exports er.javamail;
}