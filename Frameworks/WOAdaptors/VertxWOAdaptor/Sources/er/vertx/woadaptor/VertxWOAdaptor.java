package er.vertx.woadaptor;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.webobjects.appserver.WOAdaptor;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class VertxWOAdaptor extends WOAdaptor implements VertxConfigDelegate {
	static {
		// Automagically set the WOAdaptor property
		// WOProperties._AdaptorKey is undefined here, so just use "WOAdaptor"
		System.setProperty("WOAdaptor", VertxWOAdaptor.class.getName());
		/*
		 * Likewise, WOProperties._CGIAdaptorURLKey is undefined. Here it is set to
		 * https, since VertxWOAdaptor is always secure. The localhost part is parsed
		 * out by WOApplication._parseCGIAdaptorURL and replaced with your wohost.
		 * WOApplication.directConnectURL then appends the port.
		 */
		System.setProperty("WOAdaptorURL", "https://localhost/cgi-bin/WebObjects");
	}

	private final Vertx vertx;
	private String serverId;
	private Integer actualPort;
	private final int woport;
	private final int maxWorkers;
	private final String wohost;
	private final File certificatePath;

	public VertxWOAdaptor(final String aName, final NSDictionary<String, Object> arguments) {
		super(aName, arguments);
		maxWorkers = Optional.of(WOProperties._WorkerThreadCountMaxKey)
				.map(arguments::objectForKey)
				.filter(Number.class::isInstance)
				.map(Number.class::cast)
				.map(Number::intValue)
				.orElse(128);
		woport = Optional.of(WOProperties._PortKey)
				.map(arguments::objectForKey)
				.filter(Number.class::isInstance)
				.map(Number.class::cast)
				.map(Number::intValue)
				.orElse(0);
		wohost = Optional.of(WOProperties._HostKey)
				.map(arguments::objectForKey)
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.map(s -> {
					String host;
					try {
						host = InetAddress.getByName(s).getHostName();
					} catch (final UnknownHostException e) {
						NSLog.out.appendln("Failed to find host named" + s);
						host = null;
					}
					return host;
				}).orElseGet(()->{
					String host;
					try {
						NSLog.out.appendln("Defaulting to localhost");
						host = InetAddress.getLocalHost().getHostName();
					} catch (final UnknownHostException e) {
						NSLog.out.appendln("Failed to find localhost");
						host = "0.0.0.0";
					}
					return host;
				});
		certificatePath = Optional.of("acme.certbot.certificates.path")
				.map(System::getProperty)
				.map(File::new)
				.filter(File::exists)
				.orElseGet(() -> new File("/tmp/devcerts"));
		/*
		 * We will provide a default configuration, but give the app full control if it
		 * implements the delegate.
		 */
		vertx = Optional.of(WOApplication.application())
				.filter(VertxConfigDelegate.class::isInstance)
				.map(VertxConfigDelegate.class::cast)
				.orElse(this)
				.createVertx(this);
	}

	public File certificatePath() {
		return certificatePath;
	}

	@Override
	public final Vertx createVertx(final VertxWOAdaptor adaptor) {
		return Vertx.vertx(new VertxOptions().setWorkerPoolSize(maxWorkers));
	}

	@Override
	public boolean dispatchesRequestsConcurrently() {
		return true;
	}

	public int maxWorkers() {
		return maxWorkers;
	}

	@Override
	public int port() {
		return Optional.ofNullable(actualPort).orElse(woport);
	}

	@Override
	public void registerForEvents() {
		final Server server = new Server(this);
		/*
		 * Block here until success. Otherwise, the auto launch might send your browser
		 * to port -1.
		 */
		final CountDownLatch latch = new CountDownLatch(1);
		NSLog.out.appendln("VertxWOAdaptor starting on " + wohost + ":" + woport + " (host:port)");
		vertx.deployVerticle(server).onSuccess(id -> {
			serverId = id;
			actualPort = server.server().actualPort();
			NSLog.out.appendln("VertxWOAdaptor started on port " + actualPort);
			latch.countDown();
		}).onFailure(err -> {
			NSLog.err.appendln("Failed to start VertxWOAdaptor.");
			NSLog.err.appendln(err);
			latch.countDown();
		});
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				NSLog.err.appendln("VertxWOAdaptor still starting after 10 seconds!");
			}
		} catch (final InterruptedException e) {
			// reset interrupt status
			Thread.currentThread().interrupt();
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	@Override
	public void unregisterForEvents() {
		Optional.ofNullable(serverId).ifPresent(vertx::undeploy);
	}

	public Vertx vertx() {
		return vertx;
	}

	public String wohost() {
		return wohost;
	}
}
