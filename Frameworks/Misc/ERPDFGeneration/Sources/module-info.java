open module org.wocommunity.wonder.erpdfgeneration {
	requires transitive flying.saucer;
	requires flying.saucer.pdf;
	requires com.github.librepdf.openpdf;
	requires ujac;
	requires org.slf4j;
	requires org.apache.xmlgraphics.fop.core;
	requires org.apache.xmlgraphics.fop;
	requires org.apache.xmlgraphics.commons;
	requires transitive java.xml;
	requires transitive org.wocommunity.webobjects.foundation;
	requires transitive org.wocommunity.webobjects.webobjects;
	requires org.wocommunity.wonder.erextensions;

	exports er.pdf;
	exports er.pdf.builder;
	exports er.pdf.components;
}