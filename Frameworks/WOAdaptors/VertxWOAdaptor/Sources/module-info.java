open module org.wocommunity.wonder.vertxwoadaptor {
	requires com.aayushatharva.brotli4j;
	requires com.github.luben.zstd_jni;
	requires org.bouncycastle.provider;
	requires org.bouncycastle.pkix;
	requires io.netty.codec.compression;
	requires transitive io.vertx.core;
	requires io.vertx.eventbusbridge;
	requires io.vertx.web;
	requires transitive org.wocommunity.webobjects.foundation;
	requires transitive org.wocommunity.webobjects.webobjects;

	exports er.vertx.woadaptor;
}