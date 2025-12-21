open module org.wocommunity.wonder.erjgroups {
	requires jgroups;
	requires org.slf4j;
	requires org.wocommunity.wonder.erextensions;

	exports er.jgroups;
}