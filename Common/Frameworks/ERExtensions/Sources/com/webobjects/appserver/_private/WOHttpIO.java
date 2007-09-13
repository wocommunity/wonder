package com.webobjects.appserver._private;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableRange;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.ERXProperties;

/**
 * Bugfix class that adds support for chunked content, which a HTTP 1.0 client MUST
 * support. This is escpecially important for Axis clients > 1.1. <br />
 * If this class is going to be used, you need to make sure that it comes before
 * JavaWebObjects in the classpath.
 *
 * @author ak
 */
public class WOHttpIO {

    private static final int USE_KEEP_ALIVE_DEFAULT = 2;

    private int _keepAlive;

    private static final int _TheInputBufferSize = 2048;

    private static final int _HighWaterBufferSize;

    private static final Logger log = Logger.getLogger(WOHttpIO.class);

    public static String URIResponseString = " Apple WebObjects\r\n";

    private final WOHTTPHeaderValue KeepAliveValue = new WOHTTPHeaderValue("keep-alive");

    private final WOHTTPHeaderValue CloseValue = new WOHTTPHeaderValue("close");

    private final WOHTTPHeaderValue ChunkedValue = new WOHTTPHeaderValue("chunked");

    private final WOHTTPHeaderValue GzipValue = new WOHTTPHeaderValue("gzip");

    private final WOLowercaseCharArray ConnectionKey = new WOLowercaseCharArray("connection");

    private final WOLowercaseCharArray ContentLengthKey = new WOLowercaseCharArray("content-length");

    private final WOLowercaseCharArray TransferEncodingKey = new WOLowercaseCharArray("transfer-encoding");

    private byte _buffer[];

    private int _bufferLength;

    private int _bufferIndex;

    private int _lineStartIndex;

    StringBuffer _headersBuffer;

    public boolean _socketClosed;

    private final WOApplication _application = WOApplication.application();

    private static boolean _expectContentLengthHeader = true;

    private static int _contentTimeout = 5000;

    private final WOHTTPHeadersDictionary _headers = new WOHTTPHeadersDictionary();

    public static boolean _alwaysAppendContentLength = true;

    public static void expectContentLengthHeader(boolean flag, int i) {
        _expectContentLengthHeader = flag;
        _contentTimeout = i;
    }

    private int _readBlob(InputStream inputstream, int length) throws IOException {
        byte buffer[] = _buffer;
        int bufferLeft = _bufferLength - _bufferIndex;
        int bufferIndex = _bufferIndex;
        _ensureBufferIsLargeEnoughToRead(length - bufferLeft);
        if (_buffer != buffer) {
            System.arraycopy(buffer, bufferIndex, _buffer, 0, bufferLeft);
            _bufferLength = bufferLeft;
        }
        int l = bufferLeft;
        for (int i1 = 1; l < length && i1 > 0; l += i1) {
            i1 = inputstream.read(_buffer, _bufferIndex + l, length - l);
        }

        return l <= length ? l : length;
    }

    private int refillInputBuffer(InputStream is) throws IOException {
        int read = 0;
        boolean resetLineIndex = true;
        if (_bufferIndex >= 1) {
            if (_bufferLength < _buffer.length) {
                read = is.read(_buffer, _bufferLength, _buffer.length - _bufferLength);
                resetLineIndex = false;
            } else {
                byte abyte0[] = _buffer;
                int j = _bufferLength - _lineStartIndex;
                int k = _lineStartIndex;
                _ensureBufferIsLargeEnoughToRead(_buffer.length);
                System.arraycopy(abyte0, k, _buffer, 0, j);
                _bufferLength = j;
                read = is.read(_buffer, j, _buffer.length - j);
                _bufferIndex = j;
            }
        } else {
            _bufferLength = 0;
            _bufferIndex = 0;
            try {
                read = is.read(_buffer, 0, _buffer.length);
            } catch(IOException ex) {
                throw ex;
            }
        }
        if (read < 1) {
            return 0;
        }
        _bufferLength += read;
        if (resetLineIndex) {
            _lineStartIndex = 0;
        }
        return _bufferLength;
    }

    public int readLine(InputStream inputstream) throws IOException {
        boolean lfFound = false;
        boolean crFound = false;
        boolean lineFound = false;
        _lineStartIndex = _bufferIndex;
        do {
            do {
                if (_bufferIndex >= _bufferLength) {
                    break;
                }
                if (lfFound) {
                    if (_buffer[_bufferIndex] == '\t') {
                        _buffer[_bufferIndex] = ' ';
                        lfFound = crFound = false;
                    } else if (_buffer[_bufferIndex] == ' ') {
                        lfFound = crFound = false;
                    } else {
                        lineFound = true;
                    }
                } else if (_buffer[_bufferIndex] == '\r') {
                    _buffer[_bufferIndex] = ' ';
                    crFound = true;
                } else if (_buffer[_bufferIndex] == '\n') {
                    _buffer[_bufferIndex] = ' ';
                    lfFound = true;
                    if (_bufferIndex - _lineStartIndex < 2) {
                        lineFound = true;
                        _bufferIndex++;
                    }
                }
                if (lineFound) {
                    break;
                }
                _bufferIndex++;
            } while (true);
            if (_bufferIndex < _bufferLength || lineFound || refillInputBuffer(inputstream) != 0) {
                continue;
            }
            if (!lfFound) {
                return 0;
            }
            break;
        } while (!lineFound);
        int end = _bufferIndex;
        if (_bufferIndex > _bufferLength) {
            _bufferIndex = _bufferLength;
        }
        if (lfFound) {
            end--;
            if (crFound) {
                end--;
            }
        }
        return end - _lineStartIndex;
    }

    public WOHttpIO() {
        _socketClosed = false;
        _buffer = new byte[_TheInputBufferSize];
        _headersBuffer = new StringBuffer(_TheInputBufferSize);
        // log.setLevel(Level.DEBUG);
    }

    public void resetBuffer() {
        _bufferLength = 0;
        _bufferIndex = 0;
        _lineStartIndex = 0;
    }

    private void _ensureBufferIsLargeEnoughToRead(int numBytes) {
        int size = _buffer.length;
        if (numBytes + _bufferLength > size) {
            for (; numBytes + _bufferLength > size; size <<= 1) {
            }
            _buffer = new byte[size];
            resetBuffer();
        }
    }

    private void _shrinkBufferToHighWaterMark() {
        if (_buffer.length > _HighWaterBufferSize) {
            _buffer = new byte[_TheInputBufferSize];
            resetBuffer();
        }
    }

    protected WORequest readRequestFromSocket(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        int i = 0;
        int k = 0;
        WORequest worequest = null;
        String httpVersion = null;
        String uri = null;
        String method = null;
        resetBuffer();
        _headers.dispose();
        int l = readLine(is);
        if (l == 0) {
            return null;
        }
        k = _lineStartIndex;
        int i1;
        for (i1 = l - 1; _buffer[i + k] != ' ' && i < i1; i++) {
        }
        if (i < i1) {
            int j;
            for (j = i1; _buffer[j + k] != ' ' && j > i; j--) {
            }
            int j1 = i1 - j;
            if (j1 > 0) {
                method = _NSStringUtilities.stringForBytes(_buffer, j + k + 1, j1, "ISO-8859-1");
            }
            j1 = j - i - 1;
            if (j1 > 0) {
                uri = _NSStringUtilities.stringForBytes(_buffer, i + k + 1, j1, "ISO-8859-1");
            }
            j1 = i;
            if (j1 > 0) {
                httpVersion = _NSStringUtilities.stringForBytes(_buffer, k, j1, "ISO-8859-1");
            }
        }
        _keepAlive = 2;
        InputStream contentStream = _readHeaders(is, true, true, false);
        WOInputStreamData woinputstreamdata = null;
        int contentLength = 0;
        NSArray nsarray = (NSArray) _headers.objectForKey(ContentLengthKey);
        if (nsarray != null && nsarray.count() == 1 && contentStream != null) {
            try {
                contentLength = Integer.parseInt(nsarray.lastObject().toString());
            } catch (NumberFormatException numberformatexception) {
                log.debug("Unable to parse content-length header: '" + nsarray.lastObject() + "'.");
            }
            if (contentLength > 0) {
                woinputstreamdata = new WOInputStreamData(contentStream, contentLength);
            }
        } else {
            NSData nsdata = _content(is, socket, false);
            if (nsdata != null) {
                woinputstreamdata = new WOInputStreamData(nsdata);
            }
        }
        worequest = _application.createRequest(httpVersion, uri, method, _headers, woinputstreamdata, null);
        if (worequest != null) {
            worequest._setOriginatingAddress(socket.getInetAddress());
            worequest._setOriginatingPort(socket.getPort());
        }
        _shrinkBufferToHighWaterMark();
        return worequest;
    }

    private void appendMessageHeaders(WOMessage womessage) {
        Object obj = womessage.headers();
        if (obj != null) {
            if (!(obj instanceof NSMutableDictionary)) {
                obj = ((NSDictionary) (obj)).mutableClone();
            }
            ((NSMutableDictionary) obj).removeObjectForKey(ContentLengthKey);
            NSArray nsarray = ((NSDictionary) (obj)).allKeys();
            int i = nsarray.count();
            for (int j = 0; j < i; j++) {
                Object obj1 = nsarray.objectAtIndex(j);
                NSArray nsarray1 = womessage.headersForKey(obj1);
                int k = nsarray1.count();
                if (obj1 instanceof WOLowercaseCharArray) {
                    char ac[] = ((WOLowercaseCharArray) obj1).toCharArray();
                    for (int i1 = 0; i1 < k; i1++) {
                        _headersBuffer.append(ac);
                        _headersBuffer.append(": ");
                        _headersBuffer.append(nsarray1.objectAtIndex(i1));
                        _headersBuffer.append("\r\n");
                    }

                    continue;
                }
                for (int l = 0; l < k; l++) {
                    _headersBuffer.append(obj1);
                    _headersBuffer.append(": ");
                    _headersBuffer.append(nsarray1.objectAtIndex(l));
                    _headersBuffer.append("\r\n");
                }

            }

        }
    }

    protected boolean sendResponse(WOResponse woresponse, Socket socket, WORequest worequest) throws IOException {
        String s = woresponse.httpVersion();
        _headersBuffer.setLength(0);
        _headersBuffer.append(s);
        _headersBuffer.append(' ');
        _headersBuffer.append(woresponse.status());
        _headersBuffer.append(URIResponseString);
        return sendMessage(woresponse, socket, s, worequest);
    }

    public void sendRequest(WORequest worequest, Socket socket) throws IOException {
        String s = worequest.httpVersion();
        _headersBuffer.setLength(0);
        _headersBuffer.append(worequest.method());
        _headersBuffer.append(' ');
        _headersBuffer.append(worequest.uri());
        _headersBuffer.append(' ');
        _headersBuffer.append(s);
        _headersBuffer.append("\r\n");
        sendMessage(worequest, socket, s, null);
    }

    protected boolean sendMessage(WOMessage womessage, Socket socket, String s, WORequest worequest) throws IOException {
        boolean keepAlive;
        int i;
        InputStream inputstream;
        int j;
        OutputStream outputstream;
        i = 0;
        NSData nsdata = null;
        appendMessageHeaders(womessage);
        if (s.equals("HTTP/1.1")) {
            if (_keepAlive == 0) {
                _headersBuffer.append("connection: close\r\n");
                keepAlive = false;
            } else {
                keepAlive = true;
            }
        } else {
            if (_keepAlive == 1) {
                _headersBuffer.append("connection: keep-alive\r\n");
                keepAlive = true;
            } else {
                keepAlive = false;
            }
        }
        if (worequest != null) {
            NSData nsdata1 = worequest.content();
            if (nsdata1 != null && (nsdata1 instanceof WOInputStreamData)) {
                WOInputStreamData woinputstreamdata = (WOInputStreamData) nsdata1;
                InputStream inputstream1 = woinputstreamdata._stream();
                if (inputstream1 != null && (inputstream1 instanceof WONoCopyPushbackInputStream)) {
                    WONoCopyPushbackInputStream wonocopypushbackinputstream = (WONoCopyPushbackInputStream) inputstream1;
                    if (wonocopypushbackinputstream.wasPrematurelyTerminated()) {
                        return false;
                    }
                    String s1 = worequest.headerForKey("content-length");
                    long l = s1 == null ? 0L : Long.parseLong(s1);
                    if (l > 0L) {
                        long l1 = 0L;
                        byte byte0 = -1;
                        try {
                            int oldTimeout = setSocketTimeout(socket, _contentTimeout);
                            wonocopypushbackinputstream.drain();
                            log.debug("Drained socket");
                             setSocketTimeout(socket, oldTimeout);
                        } catch (IOException ex) {
                            log.error("Finished reading before content length of " + l + " : " + ex.getMessage(), ex);
                        }
                    }
                }
            }
        }
        inputstream = null;
        j = 0;
        if (womessage instanceof WOResponse) {
            WOResponse woresponse = (WOResponse) womessage;
            inputstream = woresponse.contentInputStream();
            if (inputstream != null) {
                j = woresponse.contentInputStreamBufferSize();
                i = (int)woresponse.contentInputStreamLength();
            }
        }
        if (inputstream == null) {
            nsdata = womessage.content();
            i = nsdata != null ? nsdata.length() : 0;
        }
        if (_alwaysAppendContentLength || i > 0) {
            _headersBuffer.append("content-length: ");
            _headersBuffer.append(i);
        }
        _headersBuffer.append("\r\n\r\n");
        outputstream = socket.getOutputStream();
        byte abyte0[] = _NSStringUtilities.bytesForIsolatinString(new String(_headersBuffer));
        outputstream.write(abyte0, 0, abyte0.length);
        String s2 = worequest == null ? "" : worequest.method();
        boolean flag1 = s2.equals("HEAD");
        try {
            if (!(i <= 0 || flag1)) {
                if (inputstream == null) {
                    NSMutableRange nsmutablerange = new NSMutableRange();
                    byte abyte2[] = nsdata.bytesNoCopy(nsmutablerange);
                    outputstream.write(abyte2, nsmutablerange.location(), nsmutablerange.length());
                } else {
                    byte abyte1[] = new byte[j];
                    do {
                        if (i <= 0) {
                            break;
                        }
                        int k = inputstream.read(abyte1, 0, i <= j ? i : j);
                        if (k == -1) {
                            break;
                        }
                        i -= k;
                        outputstream.write(abyte1, 0, k);
                    } while (true);
                }
            }
        } finally {
            try {
                if (inputstream != null) {
                    inputstream.close();
                }
            } catch (Exception exception) {
                log.error("Failed to close content InputStream: " + exception, exception);
            }

        }
        outputstream.flush();
        return keepAlive;
    }

    public WOResponse readResponseFromSocket(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        int i = 0;
        int k = 0;
        WOResponse woresponse = null;
        String status = null;
        String version = null;
        resetBuffer();
        int bytesRead = readLine(is);
        if (bytesRead == 0) {
            return null;
        }
        k = _lineStartIndex;
        int i1;
        for (i1 = bytesRead - 1; _buffer[i + k] != ' ' && i < i1; i++) {
        }
        if (i < i1) {
            int j;
            for (j = i + 1; _buffer[j + k] != ' ' && j < i1; j++) {
            }
            status = _NSStringUtilities.stringForBytes(_buffer, i + k + 1, j - i - 1, "ISO-8859-1");
            version = _NSStringUtilities.stringForBytes(_buffer, k, i, "ISO-8859-1");
        }
        if (_application != null) {
            woresponse = _application.createResponseInContext(null);
        } else {
            woresponse = new WOResponse();
        }
        woresponse.setHTTPVersion(version);
        woresponse.setStatus(Integer.parseInt(status));
        _readHeaders(is, false, false, false);
        woresponse.setHeaders(_headers);
        boolean closeConnection = false;
        NSArray connectionHeaders = (NSArray) _headers.valueForKey("Connection");
        if (connectionHeaders != null) {
            int count = connectionHeaders.count();
            for(int index = 0; !closeConnection && index < count; index++) {
                String curr = (String) connectionHeaders.objectAtIndex(index);
                if (curr.equalsIgnoreCase("close")) {
                    closeConnection = true;
                }
            }
        }
        NSData nsdata = _content(is, socket, closeConnection);
        woresponse.setContent(nsdata);
        _shrinkBufferToHighWaterMark();
        if (closeConnection || version.equals("HTTP/1.1") && _keepAlive == 0 || !version.equals("HTTP/1.1") && _keepAlive != 1) {
            socket.close();
            _socketClosed = true;
        }
        return woresponse;
    }

    public NSDictionary headers() {
        return _headers;
    }

    public InputStream _readHeaders(InputStream inputstream, boolean keepAlive, boolean expectContentLength, boolean fromIterator) throws IOException {
        do {
            int headerLineBytes = readLine(inputstream);
            if (headerLineBytes == 0) {
                break;
            }
            int i = _lineStartIndex;
            int j = 0;
            int k = 0;
            for (int l = 0; l < headerLineBytes; l++) {
                if (_buffer[i + l] != ':') {
                    continue;
                }
                k = l;
                for (l++; l < headerLineBytes && _buffer[i + l] == ' '; l++) {
                }
                if (l >= headerLineBytes) {
                    continue;
                }
                j = l;
                break;
            }

            if (j != 0) {
                int j1 = i;
                int k1 = k;
                int l1 = i + j;
                int i2 = headerLineBytes - j;
                WOHTTPHeaderValue wohttpheadervalue = _headers.setBufferForKey(_buffer, l1, i2, j1, k1);
                WOLowercaseCharArray wolowercasechararray = _headers.lastInsertedKey();
                if (keepAlive && _keepAlive == 2 && ConnectionKey.equals(wolowercasechararray)) {
                    if (wohttpheadervalue.equalsIgnoreCase(KeepAliveValue)) {
                        _keepAlive = 1;
                    } else if (wohttpheadervalue.equalsIgnoreCase(CloseValue)) {
                        _keepAlive = 0;
                    }
                }
            }
        } while (true);
        WONoCopyPushbackInputStream result = null;
        int bufferLeft = _bufferLength - _bufferIndex;
        if (expectContentLength) {
            int contentLength = 0;
            NSArray nsarray = (NSArray) _headers.objectForKey(ContentLengthKey);
            if (nsarray != null && nsarray.count() == 1) {
                try {
                    contentLength = Integer.parseInt(nsarray.lastObject().toString());
                } catch (NumberFormatException numberformatexception) {
                    log.debug("Unable to parse content-length header: '" + nsarray.lastObject() + "'.");
                }
                if (bufferLeft > contentLength) {
                    contentLength = bufferLeft;
                    _headers.setObjectForKey(new NSMutableArray("" + bufferLeft), ContentLengthKey);
                }
                result = new WONoCopyPushbackInputStream(new BufferedInputStream(inputstream), contentLength - bufferLeft);
            } else {
                result = null;
            }
        } else if (fromIterator) {
            if (inputstream instanceof WONoCopyPushbackInputStream) {
                result = (WONoCopyPushbackInputStream) inputstream;
            } else {
                result = null;
            }
        }
        if (result != null && bufferLeft > 0) {
            result.unread(_buffer, _bufferIndex, bufferLeft);
        }
        return result;
    }

    private int contentLength() {
        int length = 0;
        NSArray nsarray = (NSArray) _headers.objectForKey(ContentLengthKey);
        if (nsarray != null && nsarray.count() == 1) {
            try {
                length = Integer.parseInt(nsarray.lastObject().toString());
            } catch (NumberFormatException numberformatexception) {
                log.debug("Unable to parse content-length header: '" + nsarray.lastObject() + "'.");
            }
        }
        return length;
    }
    
    protected int setSocketTimeout(Socket socket, int timeout) {
        int old = timeout;
        try {
            old = socket.getSoTimeout();
            if (timeout != -1) {
                socket.setSoTimeout(timeout);
            }
        } catch (SocketException ex) {
            if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 0L)) {
                NSLog.err.appendln("<WOHttpIO>: Unable to set socket timeout:" + ex.getMessage());
            }
        }
        return old;
    }

    private NSData _content(InputStream inputstream, Socket socket, boolean closeConnection) throws IOException {
        byte buffer[] = null;
        int bytesToRead = 0;
        int start = 0;
        NSData nsdata = null;
        NSMutableArray lengthKeys = (NSMutableArray) _headers.objectForKey(ContentLengthKey);
        if (lengthKeys != null && lengthKeys.count() == 1) {
            try {
                bytesToRead = Integer.parseInt((String) lengthKeys.lastObject());
            } catch (NumberFormatException numberformatexception) {
                log.debug("Unable to parse content-length header: '" + lengthKeys.lastObject() + "'.");
            }
            if (bytesToRead != 0) {
                bytesToRead = _readBlob(inputstream, bytesToRead);
                start = _bufferIndex;
                if (bytesToRead > 0) {
                    buffer = _buffer;
                } else {
                    buffer = null;
                    start = 0;
                    bytesToRead = 0;
                }
            }
            try {
                if (buffer != null) {
                    nsdata = new NSData(buffer, new NSRange(start, bytesToRead), true);
                }
            } catch (Exception exception) {
                NSLog.err.appendln("<" + getClass().getName() + "> Error: Request creation failed!\n" + exception.toString());
                if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 0L)) {
                    NSLog.debug.appendln(exception);
                }
            }
        } else {
            boolean readChunks = false;
            NSMutableArray encodingKeys = (NSMutableArray) _headers.objectForKey(TransferEncodingKey);
            if (encodingKeys != null && encodingKeys.count() == 1) {
                String encoding = (String) encodingKeys.lastObject();
                if("chunked".equals(encoding)) {
                    readChunks = true;
                }
            } 
            if(readChunks) {
                nsdata = _readChunks(inputstream, socket);
            } else {
                if (closeConnection || !_expectContentLengthHeader) {
                    nsdata = _forceReadContent(inputstream, socket);
                }
            }
        }
        return nsdata;
    }

    /**
     * Reads a chunk size and returns the length of the next chuck.
     * Also reads all trailing data (;foo CRLF)
     * @param is
     * @return
     * @throws IOException
     */
    private int readChunkSizeLine(InputStream is) throws IOException {
        int contentBytesToRead = 0;
        boolean hasCR = false;
        boolean skip = false;
        StringBuffer sb = new StringBuffer();
        while(true) {
            int b = is.read();
            sb.append((char)b);
            if(b == ';') {
                skip = true;
            } else if (b == '\r') {
                is.read();
                break;
            }
            if(!skip) {
                int intVal = (b >= 'A' ?  (b >= 'a' ? b - 'a' : b - 'A') + 10 : b - '0');
                contentBytesToRead = contentBytesToRead * 16;
                contentBytesToRead = contentBytesToRead + intVal;
            }
        }
        return contentBytesToRead;
    }

    /**
     * Reads HTTP/1.1 chunked content. It looks like:<pre><code>
     * &lt;chunk size in hex&gt;[; some trailer stuff]&lt;CR&gt;&lt;LF&gt;
     * &lt;some data if the given size&gt;[; some trailer stuff]&lt;CR&gt;&lt;LF&gt;
     * ...
     * &lt;0, marking the last chunk&gt;[; some trailer stuff]&lt;CR&gt;&lt;LF&gt;
     * </code></pre>
     * @param is
     * @param socket
     * @return
     * @throws IOException 
     */
    private NSData _readChunks(InputStream is, Socket socket) throws IOException {
        int oldTimeout = setSocketTimeout(socket, _contentTimeout);
        try {
            int bytesInBuffer = _bufferLength - _bufferIndex;
            // we have some bytes left over from reading the headers which
            // we need to copy over to the input stream
            if(bytesInBuffer > 0) {
                is = new PushbackInputStream(is, bytesInBuffer);
                ((PushbackInputStream)is).unread(_buffer, _bufferIndex, bytesInBuffer);
            }
            resetBuffer();
            byte buffer[] = new byte[_TheInputBufferSize];
            NSMutableData result = new NSMutableData();
            while(true) {
                int contentBytesToRead = readChunkSizeLine(is);

                if(log.isDebugEnabled()) {
                    log.debug("Header is: contentBytesToRead: " + contentBytesToRead);
                }
                if(contentBytesToRead > 0) {
                    contentBytesToRead += 2;
                    if(contentBytesToRead > buffer.length) {
                        buffer = new byte[contentBytesToRead];
                    }
                    int bytesRead = is.read(buffer, 0, contentBytesToRead);
                    if(bytesRead > contentBytesToRead) {
                        bytesRead = contentBytesToRead;
                    }
                    if (bytesRead > 0) {
                        if(log.isDebugEnabled()) {
                            NSData data = new NSData(buffer, new NSRange(0, bytesRead));
                            log.debug("Read directly: " + new String(data.bytes()));
                        }
                        result.appendBytes(buffer, new NSRange(0, bytesRead-2));
                    }
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("Bytes read: " + new String(result.bytes()));
                    }
                    return result;
                }
            }
        } finally {
            oldTimeout = setSocketTimeout(socket, oldTimeout);
        }
    }

    private NSData _forceReadContent(InputStream is, Socket socket) {
        int oldTimeout = setSocketTimeout(socket, _contentTimeout);
        try {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte buffer[] = new byte[_TheInputBufferSize];
            NSMutableData result;

            if (_bufferLength > _bufferIndex) {
                result = new NSMutableData(_bufferLength - _bufferIndex);
                result.appendBytes(_buffer, new NSRange(_bufferIndex, _bufferLength - _bufferIndex));
            } else {
                result = new NSMutableData();
            }
            do {
                int bytesRead = bis.read(buffer, 0, _TheInputBufferSize);
                if (bytesRead >= 0) {
                    result.appendBytes(buffer, new NSRange(0, bytesRead));
                } else {
                    return result;
                }
            } while (true);
        } catch (IOException ex) {
            log.error(ex, ex);
            return null;
        } finally {
            oldTimeout = setSocketTimeout(socket, oldTimeout);
        }
    }

    public String toString() {
        return "<" + getClass().getName() + " keepAlive='" + _keepAlive + "' buffer=" + _buffer + " >";
    }

    static {
        int maxSize = ERXProperties.intForKey("WOMaxIOBufferSize");
        if (maxSize != 0) {
            _HighWaterBufferSize = maxSize >= _TheInputBufferSize ? maxSize : _TheInputBufferSize;
        } else {
            _HighWaterBufferSize = _TheInputBufferSize * 2;
        }
    }
}
