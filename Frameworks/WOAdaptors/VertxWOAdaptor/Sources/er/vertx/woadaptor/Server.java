package er.vertx.woadaptor;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOInputStreamData;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VerticleBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class Server extends VerticleBase {
	private static final Handler<BridgeEvent> SOCKET_EVENT_HANDLER = event->{
		/*
		 * Capture the wosession id on messages from the client, if one exists.
		 */
		if(BridgeEventType.SEND.equals(event.type()) || BridgeEventType.PUBLISH.equals(event.type())) {
			final String key = WOApplication.application().sessionIdKey();
			final Cookie sessionCookie = event.socket().routingContext().request().getCookie(key);
			if(sessionCookie != null) {
				final JsonObject json = event.getRawMessage();
				json.put(key, sessionCookie.getValue());
				event.setRawMessage(json);
				event.complete(true);
			}
		}
	};

	private final VertxWOAdaptor adaptor;

	private HttpServer server;

	Server(final VertxWOAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	private long calculateMaxAge(final WOCookie wocookie) {
		final Long result;
		if (wocookie.timeOut() >= 0) {
			result = (long) wocookie.timeOut();
		} else if (wocookie.expires() != null) {
			result = (wocookie.expires().getTime() - System.currentTimeMillis()) / 1000;
		} else {
			result = Long.MIN_VALUE;
		}
		return result;
	}

	private void handleResponseInContext(final WOResponse response, final RoutingContext context) {
		final HttpServerResponse res = context.response();
		res.setStatusCode(response.status());
		response.headers().entrySet().stream().forEach(entry -> res.putHeader(entry.getKey(), entry.getValue()));
		response.cookies().stream().map(wocookie -> {
			final Cookie cookie = Cookie.cookie(wocookie.name(), wocookie.value());
			cookie.setHttpOnly(wocookie.isHttpOnly());
			cookie.setSecure(wocookie.isSecure());
			cookie.setSameSite(WOCookie.SameSite.NORMAL.equals(wocookie.sameSite()) 
					? CookieSameSite.NONE 
					: CookieSameSite.valueOf(wocookie.sameSite().name()));
			cookie.setDomain(wocookie.domain());
			cookie.setPath(wocookie.path());
			cookie.setMaxAge(calculateMaxAge(wocookie));
			return cookie;
		}).forEach(res::addCookie); 
		res.end(Buffer.buffer(response.content().bytes()));
	}

	HttpServer server() {
		return server;
	}

	@Override
	public Future<?> start() throws Exception {
		final Router router = Router.router(vertx);
		final SockJSHandler sockJS = SockJSHandler.create(vertx);
		final SockJSBridgeOptions bridgeOpts = new SockJSBridgeOptions()
				.addInboundPermitted(new PermittedOptions().setAddressRegex("client-req\\..+"))
				.addOutboundPermitted(new PermittedOptions().setAddressRegex("server-event\\..+"));
		router.route("/socket/*").subRouter(sockJS.bridge(bridgeOpts, SOCKET_EVENT_HANDLER));
		router.route()
		/*
		 * The vertx 5 default is 10MB but a WORequest can handle int content length.
		 * Anything over this value will return a 413 content too large.
		 */
		//.handler(BodyHandler.create(false).setBodyLimit(Integer.MAX_VALUE))
		/*
		 * BodyHandler will parse form content and we want to pass it unmolested to WO.
		 * So we will do it ourselves in this handler. This does NOT work in the
		 * blocking handler below, because of errors indicating the request was already
		 * read.
		 */
		.handler(context -> {
			Buffer body = Buffer.buffer();
			context.request().handler(body::appendBuffer);
			context.request().endHandler(nil -> {
				context.put("WORequestData", body);
				context.next();
			});
		})
		// TODO make WO non-blocking? :)
		.blockingHandler(context -> {
			Buffer body = context.get("WORequestData");
			NSData data = body.length() > 0 
					? new WOInputStreamData(new NSData(body.getBytes())) 
					: NSData.EmptyData;
			final WORequest request = woRequestFromRoutingContext(context, data);
			final WOResponse response = WOApplication.application().dispatchRequest(request);
			handleResponseInContext(response, context);
		});
		final HttpServerOptions options = new HttpServerOptions()
				// always on
				.setSsl(true)
				.setKeyCertOptions(CertificateUtils.optionsForHostAndDir(adaptor.wohost(), adaptor.certificatePath()))
				// support compressed requests from client
				.setDecompressionSupported(true)
				// support compression responses to client
				.setCompressionSupported(true)
				.addCompressor(StandardCompressionOptions.deflate())
				.addCompressor(StandardCompressionOptions.gzip())
				.addCompressor(StandardCompressionOptions.brotli())
				.addCompressor(StandardCompressionOptions.zstd())
				// HTTP/2 support
				.setUseAlpn(true);
		return vertx.createHttpServer(options)
				.requestHandler(router)
				.listen(adaptor.port(),adaptor.wohost())
				.onSuccess(server -> {
					NSLog.out.appendln("HTTP server started on port " + server.actualPort());
					System.setProperty(WOProperties._PortKey, Integer.toString(server.actualPort()));
					this.server = server;
				}).onFailure(th -> {
					th.printStackTrace();
				});
	}

	@Override
	public Future<?> stop() throws Exception {
		return server == null
				? super.stop() 
				: server.close();
	}

	private WORequest woRequestFromRoutingContext(final RoutingContext context, NSData bodyData) {
		// Read the headers
		final NSMutableDictionary<String, NSMutableArray<String>> headers =
				context.request().headers().entries().stream()
				.collect(Collectors.groupingBy(
						Map.Entry::getKey,
						NSMutableDictionary::new,
						Collectors.mapping(
								Map.Entry::getValue,
								Collectors.toCollection(NSMutableArray::new))));

		final String httpVersion = switch (context.request().version()) {
		case HTTP_1_0:
			yield "HTTP/1.0";
		case HTTP_1_1:
			yield "HTTP/1.1";
		case HTTP_2:
			yield "HTTP/2";
		};
		final WORequest worequest = WOApplication.application().createRequest(
				context.request().method().name(),
				context.request().absoluteURI(),
				httpVersion,
				headers,
				bodyData,
				null // userInfo dictionary
				);
		worequest._setOriginatingAdaptor(adaptor);
		Optional.of(context)
				.map(RoutingContext::request)
				.map(HttpServerRequest::remoteAddress)
				.map(SocketAddress::hostAddress)
				.map(InetAddress::ofLiteral)
				.ifPresent(worequest::_setOriginatingAddress);
		Optional.of(context)
				.map(RoutingContext::request)
				.map(HttpServerRequest::remoteAddress)
				.map(SocketAddress::port)
				.ifPresent(worequest::_setOriginatingPort);
		context.request().cookies().stream()
				.map(cookie ->  new WOCookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain(), null /*expires*/, cookie.isSecure(), cookie.isHttpOnly()))
				.forEach(worequest::addCookie);
		return worequest;
	}
}
