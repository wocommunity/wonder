package er.vertx.woadaptor;

import io.vertx.core.Vertx;

public interface VertxConfigDelegate {
	Vertx createVertx(VertxWOAdaptor adaptor);
}
