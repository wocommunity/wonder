package er.woadaptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.util.SessionUtil;

import com.webobjects.appserver.WOAdaptor;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class ERWOAdaptor extends WOAdaptor {

    private static final Logger log = Logger.getLogger(ERWOAdaptor.class);

    private int _port;

    private String _host;

    private int _maxSocketIdleTime;

    private static WOResponse _lastDitchErrorResponse;

    private static WOApplication _app;

    private String _requestIdKey;

    private IoAcceptor acceptor;

    private static ExecutorService _executor;
    
    public ERWOAdaptor(String name, NSDictionary config) {
        super(name, config);
        _lastDitchErrorResponse = new WOResponse();
        _lastDitchErrorResponse.setStatus(500);
        _lastDitchErrorResponse.setContent("An error occured");
        _lastDitchErrorResponse.setHeaders(NSDictionary.EmptyDictionary);

        _app = WOApplication.application();
        Number number = (Number) config.objectForKey(WOProperties._PortKey);
        if (number != null)
            _port = number.intValue();
        if (_port < 0)
            _port = 0;
        _app.setPort(_port);
        _executor = Executors.newCachedThreadPool();
        _host = (String) config.objectForKey(WOProperties._HostKey);
        _app._setHost(_host);
    }

    @Override
    public void registerForEvents() {
        try {
            acceptor = new SocketAcceptor(16, Executors.newCachedThreadPool());
            SocketAcceptorConfig cfg = new SocketAcceptorConfig();
            cfg.setThreadModel(ThreadModel.MANUAL);

            cfg.getFilterChain().addLast("logger", new LoggingFilter());
            cfg.getFilterChain().addLast("protocolFilter", new ProtocolCodecFilter(new CodecFactory()));
            acceptor.bind(new InetSocketAddress(_host, _port), new Handler(), cfg);
            log.info("Started adaptor");
        } catch (IOException ex) {
            log.error(ex, ex);
        }
    }

    @Override
    public void unregisterForEvents() {
        if (acceptor != null) {
            acceptor.unbindAll();
            acceptor = null;
        }
    }

    public boolean dispatchesRequestsConcurrently() {
        return true;
    }

    public static class CodecFactory extends DemuxingProtocolCodecFactory {
        public CodecFactory() {
            super.register(RequestDecoder.class);
            super.register(ResponseEncoder.class);
        }
    }

    public static class RequestDecoder implements MessageDecoder {
        
        private static final byte[] CONTENT_LENGTH = new String("Content-Length:").getBytes();

        private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

        public RequestDecoder() {
        }

        public MessageDecoderResult decodable(IoSession session, ByteBuffer in) {
            try {
                return headerComplete(in) ? MessageDecoderResult.OK : MessageDecoderResult.NEED_DATA;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return MessageDecoderResult.NOT_OK;
        }

        public MessageDecoderResult decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
            // Try to decode body
            WORequest request = parseRequest(new StringReader(in.getString(decoder)));

            if (request == null) {
                return MessageDecoderResult.NEED_DATA;
            }
            out.write(request);

            return MessageDecoderResult.OK;
        }

        private boolean headerComplete(ByteBuffer in) throws Exception {

            int last = in.remaining() - 1;
            if (in.remaining() < 4)
                return false;

            int eoh = -1;
            for (int i = 0; i < last - 2; i++) {
                if (in.get(i) == (byte) 0x0D && in.get(i + 1) == (byte) 0x0A && in.get(i + 2) == (byte) 0x0D && in.get(i + 3) == (byte) 0x0A) {
                    eoh = i + 3;
                    break;
                }
            }
            if (eoh == -1)
                return false;
            byte[] bytes = new byte[eoh - 3];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = in.get(i);
            }
            String headers = new String(bytes);
            for (int i = 0; i < last; i++) {
                boolean found = false;
                for (int j = 0; j < CONTENT_LENGTH.length; j++) {
                    if (in.get(i + j) != CONTENT_LENGTH[j]) {
                        found = false;
                        break;
                    }
                    found = true;
                }
                if (found) {
                    // retrieve value from this position till next 0x0D 0x0A
                    StringBuilder contentLength = new StringBuilder();
                    for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
                        if (in.get(j) == 0x0D)
                            break;
                        contentLength.append(new String(new byte[] { in.get(j) }));
                    }
                    // if content-length worth of data has been received then
                    // the message is complete
                    return (Integer.parseInt(contentLength.toString().trim()) + eoh == in.remaining());
                } else {
                    // this may be wrong...
                    return true;
                }
            }

            // the message is not complete and we need more data
            return false;
        }

        private WORequest parseRequest(Reader is) throws IOException {
            BufferedReader rdr = new BufferedReader(is);
            String line = rdr.readLine();
            String[] parts = line.trim().split(" ", 3);
            String url = parts[1];
            String method = parts[0];
            String version = parts[2];
            NSMutableDictionary<String, NSMutableArray<String>> headers = new NSMutableDictionary<String, NSMutableArray<String>>() {
                @Override
                public NSMutableArray<String> objectForKey(Object key) {
                    return super.objectForKey(key.toString().toLowerCase());
                }
            };

            // Read header
            while ((line = rdr.readLine()) != null && line.length() > 0) {
                String[] tokens = line.split(":\\s*", 2);
                String key = tokens[0];
                String value = tokens[1];
                NSMutableArray<String> items = headers.objectForKey(key);
                if (items == null) {
                    items = new NSMutableArray<String>();
                    headers.setObjectForKey(items, key.toLowerCase());
                }
                items.addObject(value);
            }

            if (headers.objectForKey("Content-Length") != null) {
                int len = Integer.parseInt(headers.objectForKey("Content-Length").lastObject());
                char[] buf = new char[len];
                if (rdr.read(buf) == len) {
                    line = String.copyValueOf(buf);
                }
            }
            WORequest request = WOApplication.application().createRequest(method, url, version, headers, null, null);

            return request;
        }

        public void finishDecode(IoSession iosession, ProtocolDecoderOutput protocoldecoderoutput) throws Exception {
            // TODO Auto-generated method stub

        }
    }
    
    public static class ResponseWrapper {
        
        private WOResponse _response;
        
        public ResponseWrapper(WOResponse response) {
            _response = response;
        }
        
        public WOResponse response() {
            return _response;
        }
        
    }

    public static class ResponseEncoder implements MessageEncoder {

        private static final Set TYPES;

        static {
            Set types = new HashSet();
            types.add(ResponseWrapper.class);
            TYPES = Collections.unmodifiableSet(types);
        }

        private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

        public ResponseEncoder() {
        }

        public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
            WOResponse msg = (WOResponse) ((ResponseWrapper)message).response();
            ByteBuffer buf = ByteBuffer.allocate(256);
            // Enable auto-expand for easier encoding
            buf.setAutoExpand(true);

            try {
                // output all headers except the content length
                CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
                buf.putString(msg.httpVersion(), encoder);
                buf.putString(" ", encoder);
                buf.putString(String.valueOf(msg.status()), encoder);
                switch (msg.status()) {
                case WOResponse.HTTP_STATUS_OK:
                    buf.putString(" OK", encoder);
                    break;
                case WOResponse.HTTP_STATUS_NOT_FOUND:
                    buf.putString(" Not Found", encoder);
                    break;
                default:
                    buf.putString(" Whatever", encoder);
                }
                buf.put(CRLF);
                for (Iterator<Entry<Object, NSArray>> it = msg.headers().entrySet().iterator(); it.hasNext();) {
                    Entry<Object, NSArray> entry = it.next();
                    for (Object item : entry.getValue()) {
                        buf.putString(entry.getKey().toString(), encoder);
                        buf.putString(": ", encoder);
                        buf.putString(item.toString(), encoder);
                        buf.put(CRLF);
                    }
                }
                // now the content length is the body length
                buf.put(CRLF);
                // add body
                //buf.flip();
                //log.info(msg);
                buf.put(msg.content()._bytesNoCopy());
            } catch (CharacterCodingException ex) {
                ex.printStackTrace();
            }

            buf.flip();
            out.write(buf);
        }

        public Set getMessageTypes() {
            return TYPES;
        }
    }

    public static class Handler implements IoHandler {
        
        private final class Runner implements Runnable {
            
            private WORequest request;
            private IoSession session;

            public Runner(IoSession session, WORequest message) {
                this.request = message;
                this.session = session;
            }

            public WOResponse runOnce() {
                WOResponse woresponse = _lastDitchErrorResponse;
                try {
                    boolean process = request != null;
                    process &= !WOApplication.application().isDirectConnectEnabled();
                    process &= !request.isUsingWebServer();
                    process &= !"womp".equals(request.requestHandlerKey());
                    
                    if (process) {
                        woresponse = _app.dispatchRequest(request);
                        NSDelayedCallbackCenter.defaultCenter().eventEnded();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return woresponse;
            }

            public void run() {
                WOResponse response = runOnce();

                if (response != null) {
                    session.write(new ResponseWrapper(response)).join();
                }
                //session.close();
            }
        }

        public void sessionCreated(IoSession session) throws Exception {
            SessionUtil.initialize(session);
        }

        public void sessionOpened(IoSession session) {
            session.setIdleTime(IdleStatus.BOTH_IDLE, 60);
        }

        public void sessionClosed(IoSession session) {
        }

        public void messageReceived(IoSession session, Object message) {
            Runnable callable = new Runner(session, (WORequest) message);
            _executor.submit(callable);
        }
        
        public void messageSent(IoSession session, Object message) {
            // session.close();
        }

        public void sessionIdle(IoSession session, IdleStatus status) {
            session.close();
        }

        public void exceptionCaught(IoSession session, Throwable cause) {
            log.info("exceptionCaught: " + cause, cause);
            session.close();
        }

    }
}
