package er.vertx.woadaptor;

import com.webobjects.foundation.NSNotificationCenter;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryContext;
import io.vertx.core.eventbus.EventBus;

public class NSNotificationBridge {
	private final EventBus bus;
	private final Handler<DeliveryContext<Object>> interceptor;

	public NSNotificationBridge(final EventBus bus) {
		this.bus = bus;
		interceptor = this::copyToNotificationCenter;
	}

	/**
	 * NSNotificationCenter doesn't really have a concept of inbound/outbound, so
	 * this handler simply copies all eventbus messages onto the
	 * NSNotificationCenter. If you choose to handle one of these messages, it
	 * should be non-blocking or use
	 * {@link Vertx#executeBlocking(java.util.concurrent.Callable)}
	 *
	 * @param ctx the delivery context
	 */
	public void copyToNotificationCenter(final DeliveryContext<?> ctx) {
		NSNotificationCenter.defaultCenter().postNotification(ctx.message().address(), ctx.message());
	}

	public void registerForEvents() {
		bus.addInboundInterceptor(interceptor);
		bus.addOutboundInterceptor(interceptor);
	}

	public void unregisterForEvents() {
		bus.removeInboundInterceptor(interceptor);
		bus.removeOutboundInterceptor(interceptor);
	}
}
