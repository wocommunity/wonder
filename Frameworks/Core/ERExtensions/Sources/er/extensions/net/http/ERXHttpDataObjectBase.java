package er.extensions.net.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.CharEncoding;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import er.extensions.foundation.ERXProperties;

/**
 * Access Sample :
 * 
 * <pre>
 * ERXML.Doc doc = ERXML.doc(...); // Create the Post XML Doc
 *
 * ERXHttpPostData httpData = new ERXHttpPostData(<<hostname>>);
 * httpData.setScheme(ERXHttpPostData.SCHEME_HTTP);
 * httpData.setPath(<<Path>>);
 * httpData.setContentType("text/xml");
 * httpData.setSendEntity(doc.toString());
 * 
 * try {
 *  httpData.execute();
 *  
 *  if (httpData.html().startsWith("<?xml")) {
 *    ERXML.Doc parseDoc = ERXML.doc(httpData.html());
 *    ...
 * </pre>
 * 
 * @author ishimoto
 */
public abstract class ERXHttpDataObjectBase {

  protected static final Logger log = Logger.getLogger(ERXHttpDataObjectBase.class);

  //***********************************************************
  // Constants
  //***********************************************************

  public static int HTTP_GET = 1;
  public static int HTTP_POST = 2;

  public static int HTTP_PORT = 80;
  public static int HTTPS_PORT = 443;

  public static String SCHEME_HTTP = "http";
  public static String SCHEME_HTTPS = "https";

  public static String ENCODING_UTF8 = CharEncoding.UTF_8;
  public static String ENCODING_SJIS = "Shift_JIS";

  public static HttpVersion HTTP_1_0 = HttpVersion.HTTP_1_0;
  public static HttpVersion HTTP_1_1 = HttpVersion.HTTP_1_1;

  //***********************************************************
  // Constructor
  //***********************************************************

  public ERXHttpDataObjectBase(String hostname) {
    setHostname(hostname);
  }

  //***********************************************************
  // Set & Get Properties
  //***********************************************************

  protected void setHttpPost() {
    postOrGet = HTTP_POST;
  }
  protected boolean isHttpPost() {
    return postOrGet == HTTP_POST;
  }

  protected void setHttpGet() {
    postOrGet = HTTP_GET;
  }
  protected boolean isHttpGet() {
    return postOrGet == HTTP_GET;
  }

  private int postOrGet = HTTP_GET;

  // the hostname (IP or DNS name)
  protected void setHostname(String hostname) {
    this.hostname = hostname;
  }
  protected String hostname() {
    return hostname;
  }
  private String hostname = null;

  // the port number. -1 indicates the scheme default port.
  public void setPort(int port) {
    this.port = port;
  }
  protected int port() {
    return port;
  }
  private int port = -1;

  // The default scheme is "http".
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }
  protected String scheme() {
    return scheme;
  }
  private String scheme = SCHEME_HTTP;

  public void setPath(String path) {
    this.path = path;
  }
  protected String path() {
    return path;
  }
  private String path = "/";

  /**
   * <span class="en">Send Encoding</span>
   * <span class="ja">送信エンコーディング</span>
   * 
   */
  public void setSendEncoding(String sendEncoding) {
    this.sendEncoding = sendEncoding;
  }
  protected String sendEncoding() {
    return sendEncoding;
  }
  private String sendEncoding;

  /**
   * <span class="en">Receive Encoding</span>
   * <span class="ja">受信エンコーディング</span>
   * 
   */
  public void setReceiveEncoding(String receiveEncoding) {
    this.receiveEncoding = receiveEncoding;
  }
  protected String receiveEncoding() {
    return receiveEncoding;
  }
  private String receiveEncoding;

  protected void setResponse(HttpResponse response) {
    this.response = response;
  }
  public HttpResponse response() {
    return response;
  }
  private HttpResponse response;

  protected void setEntity(HttpEntity entity) {
    this.entity = entity;
  }
  public HttpEntity entity() {
    return entity;
  }	
  private HttpEntity entity;

  protected void setHtml(String html) {
    this.html = html;
  }
  public String html() {
    return html;
  }
  private String html;    

  public void setSendEntity(String sendEntity) {
    this.sendEntity = sendEntity;
  }
  protected String sendEntity() {
    return sendEntity;
  }
  private String sendEntity = null;

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  protected String contentType() {
    return contentType;
  }
  private String contentType = null;

  /**
   * <span class="en">absolute URI for check</span>
   * <span class="ja">URI がどの様に作成されている為に確認できるように</span>
   */
  protected void setURI(URI uri) {
    this.uri = uri;
  }
  public URI uri() {
    return uri;
  }
  private URI uri = null;

  /**
   * <span class="en">HTTP Version</span>
   * <span class="ja">HTTP バージョン</span>
   * 
   */
  public void setHttpVersion(HttpVersion httpVersion) {
    this.httpVersion = httpVersion;
  }
  public HttpVersion httpVersion() {
    return httpVersion;
  }
  private HttpVersion httpVersion = HTTP_1_1;

  /**
   * <span class="en">
   * Adding Query Parameters
   * @param key - Key
   * @param value - Value
   * </span>
   * 
   * <span class="ja">
   * 検索配列
   * 
   * @param key - キー
   * @param value - 値
   * </span>
   */
  public void addQueryParams(String key, String value) {
    queryParams.add(new BasicNameValuePair(key, value));
  }
  protected List<NameValuePair> queryParams() {
    return queryParams;
  }
  private List<NameValuePair> queryParams = new ArrayList<NameValuePair>();	

  //***********************************************************
  // Methods
  //***********************************************************

  protected Scheme createScheme() {
    if(SCHEME_HTTPS.equals(scheme())) {
      return new Scheme(scheme(), HTTPS_PORT, SSLSocketFactory.getSocketFactory());
    } else if(port() == -1) {
      return new Scheme(scheme(), HTTP_PORT, PlainSocketFactory.getSocketFactory());
    } else {
      return new Scheme(scheme(), port(), PlainSocketFactory.getSocketFactory());
    }
  }

  protected String userAgent() {
    // assemble User-Agent header
    StringBuilder useragent = new StringBuilder();
    useragent.append("WebObjects/ " + ERXProperties.webObjectsVersion() + " (");
    useragent.append(System.getProperty("os.arch"));
    useragent.append("; ");
    useragent.append(System.getProperty("os.name"));
    useragent.append(' ');
    useragent.append(System.getProperty("os.version"));
    useragent.append(')');
    return useragent.toString();
  }

  public abstract void execute() throws Exception;
}
