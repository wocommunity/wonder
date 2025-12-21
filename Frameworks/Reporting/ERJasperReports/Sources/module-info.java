open module org.wocommunity.wonder.jasperreports {
	requires transitive org.wocommunity.webobjects.foundation;
	requires transitive org.wocommunity.webobjects.eocontrol;
	requires transitive net.sf.jasperreports.core;
	requires org.apache.commons.lang3;
	requires org.slf4j;
	requires org.wocommunity.wonder.erextensions;

	exports er.jasperreports;
}