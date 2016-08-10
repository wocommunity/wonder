package er.extensions.net.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXStringUtilities;

/**
 * <span class="en">
 * Use a URL and access a HTTP Server, then give back the Result
 * </span>
 * 
 * <span class="ja">
 * HTTP Server へ URL を使ってアクセスし、結果が戻ります。
 * </span>
 * 
 * This Method is written a few Years ago, it works but can be written
 * better with the new Http-Client 4
 * 
 * @author ishimoto
 */
public class ERXHttpPostData extends ERXHttpDataObjectBase{
	private static final Logger log = LoggerFactory.getLogger(ERXHttpDataObjectBase.class);

  //***********************************************************
  // Constructor
  //***********************************************************

  public ERXHttpPostData(String hostname) {
    super(hostname);
    setHttpPost();
  }

  //***********************************************************
  // Methods
  //***********************************************************

  @Override
  public void execute() throws Exception {

    // Create Http Host
    // Holds all of the variables needed to describe an HTTP connection to a host. This includes remote host name, port and scheme.
    HttpHost targetHost = new HttpHost(hostname(), port(), scheme());

    // Set all Access Parameters
    //　HttpParams interface represents a collection of immutable values that define a runtime behavior of a component.
    HttpParams params = new BasicHttpParams();
    //　Utility class for accessing protocol parameters in HttpParams.
    //　Defines the ProtocolVersion used per default.
    HttpProtocolParams.setVersion(params, httpVersion());
    //　Defines the charset to be used per default for encoding content body.
    HttpProtocolParams.setContentCharset(params, sendEncoding());
    //　Defines the content of the User-Agent header.
    HttpProtocolParams.setUserAgent(params, userAgent());

    // Creates a new, empty scheme registry.
    SchemeRegistry supportedSchemes = new SchemeRegistry();
    supportedSchemes.register(createScheme());

    // Ste the Path
    // Constructs a {@link URI} using all the parameters.
    if(queryParams() == null || queryParams().isEmpty()) {
      setURI(new URIBuilder().setScheme(scheme()).setHost(hostname()).setPort(port()).setPath(path()).build());
    } else {
      setURI(URIUtils.createURI(scheme(), hostname(), port(), path(), URLEncodedUtils.format(queryParams(), sendEncoding()), null));
    }

    // create Threadsafe Client
    ClientConnectionManager connMgr = new PoolingClientConnectionManager(supportedSchemes);

    // Create default httpClient
    // Creates a new HTTP client from parameters and a connection manager.
    DefaultHttpClient httpclient = new DefaultHttpClient(connMgr, params);

    // HTTP POST method.
    HttpPost httpost = new HttpPost(uri());

    // Content-Type ?
    if(contentType() != null) {
      httpost.setHeader("Content-Type", contentType() + "; charset=" + ERXStringUtilities.quote(sendEncoding(), "\""));
    }

    // Set Entity
    if(!ERXStringUtilities.stringIsNullOrEmpty(sendEntity())) {
      StringEntity se = new StringEntity(sendEntity());
      httpost.setEntity(se);
    } else {
      httpost.setEntity(new UrlEncodedFormEntity(queryParams(), sendEncoding()));
    }

    log.debug("executing request to {}", targetHost);

    // Executes a request to the target using the default context.
    HttpResponse httpResponse = httpclient.execute(targetHost, httpost);

    // Set Response
    setResponse(httpResponse);

    if (response().getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      setEntity(response().getEntity());

      if(entity() != null) {
        if(receiveEncoding() == null) {
          setHtml(EntityUtils.toString(entity()));
        } else {
          setHtml(EntityUtils.toString(entity(), receiveEncoding()));
        }
      }
    }

    if (log.isDebugEnabled()) {
      log.debug("Status: {}", response().getStatusLine().getStatusCode());
      log.debug("the response looks like: {}\n\n", response());
      log.debug("the response entity looks like: {}\n\n", html());
    }

    // When HttpClient instance is no longer needed, 
    // shut down the connection manager to ensure
    // immediate deallocation of all system resources
    httpclient.getConnectionManager().shutdown();
  }
}
