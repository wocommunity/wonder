/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rackspacecloud.client.cloudfiles.wrapper.RequestEntityWrapper;

/**
 * 
 * A client for Cloud Files.  Here follows a basic example of logging in, creating a container and an 
 * object, retrieving the object, and then deleting both the object and container.  For more examples, 
 * see the code in com.rackspacecloud.client.cloudfiles.sample, which contains a series of examples.
 * 
 * <pre>
 * 
 *  //  Create the client object for username "jdoe", password "johnsdogsname". 
 * 	FilesClient myClient = FilesClient("jdoe", "johnsdogsname");
 * 
 *  // Log in (<code>login()</code> will return false if the login was unsuccessful.
 *  assert(myClient.login());
 * 
 *  // Make sure there are no containers in the account
 *  assert(myClient.listContainers.length() == 0);
 *  
 *  // Create the container
 *  assert(myClient.createContainer("myContainer"));
 *  
 *  // Now we should have one
 *  assert(myClient.listContainers.length() == 1);
 *  
 *  // Upload the file "alpaca.jpg"
 *  assert(myClient.storeObject("myContainer", new File("alapca.jpg"), "image/jpeg"));
 *  
 *  // Download "alpaca.jpg"
 *  FilesObject obj = myClient.getObject("myContainer", "alpaca.jpg");
 *  byte data[] = obj.getObject();
 *  
 *  // Clean up after ourselves.
 *  // Note:  Order here is important, you can't delete non-empty containers.
 *  assert(myClient.deleteObject("myContainer", "alpaca.jpg"));
 *  assert(myClient.deleteContainer("myContainer");
 * </pre>
 * 
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesCli"
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesAuth"
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesCopy"
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesList"
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesRemove"
 * @see "com.rackspacecloud.client.cloudfiles.sample.FilesMakeContainer"
 * 
 * @author lvaughn
 */
public class FilesClient
{
    public static final String VERSION = "v1";

    private String username = null;
    private String password = null;
    private String account = null;
    private String authenticationURL;
    private int connectionTimeOut;
    private String storageURL = null;
    private String cdnManagementURL = null;
    private String authToken = null;
    private boolean isLoggedin = false;
    private boolean useETag = true;
    private boolean snet = false;
    private String snetAddr = "https://snet-";
 
    private HttpClient client = null;

    private static Logger logger = Logger.getLogger(FilesClient.class); 

    /**
     * @param client    The HttpClient to talk to Swift
     * @param username  The username to log in to 
     * @param password  The password
     * @param authUrl 
     * @param account   The Cloud Files account to use
     * @param connectionTimeOut  The connection timeout, in ms.
     */
   public FilesClient(HttpClient client, String username, String password, String authUrl, String account, int connectionTimeOut) {
        this.client = client;
        this.username = username;
        this.password = password;
        this.account = account;
        if(authUrl == null) {
            authUrl = FilesUtil.getProperty("auth_url");
        }
        if(account != null && account.length() > 0) {
            authenticationURL = authUrl + VERSION + "/" + account + FilesUtil.getProperty("auth_url_post");
        }
        else {
            authenticationURL = authUrl;
        }
        this.connectionTimeOut = connectionTimeOut;

        setUserAgent(FilesConstants.USER_AGENT);

        if(logger.isDebugEnabled()) {
            logger.debug("UserName: " + this.username);
            logger.debug("AuthenticationURL: " + authenticationURL);
            logger.debug("ConnectionTimeOut: " + this.connectionTimeOut);
        }
     }

    /**
     * @param username  The username to log in to 
     * @param password  The password
     * @param authUrl 
     * @param account   The Cloud Files account to use
     * @param connectionTimeOut  The connection timeout, in ms.
     */
    public FilesClient(String username, String password, String authUrl, String account, final int connectionTimeOut)
    {
    	   this(new DefaultHttpClient() {
    	        @Override
    	        protected HttpParams createHttpParams() {
    	            BasicHttpParams params = new BasicHttpParams();
    	            org.apache.http.params.HttpConnectionParams.setSoTimeout(params, connectionTimeOut);
    	            params.setParameter("http.socket.timeout", connectionTimeOut);
    	            return params;
    	        }

    	        @Override
                protected ClientConnectionManager createClientConnectionManager() {
       	            SchemeRegistry schemeRegistry = new SchemeRegistry();
       	            schemeRegistry.register(
       	                    new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
       	            schemeRegistry.register(
       	                    new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
       	            return new ThreadSafeClientConnManager(createHttpParams(), schemeRegistry);
       	        }
    	    }, username, password, authUrl, account, connectionTimeOut);

     }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT.  If <code>account</code>
     * is null, "Mosso Style" authentication is assumed, otherwise standard Cloud Files authentication is used.
     * 
     * @param username The username to log in to
     * @param password The password
     * @param authUrl
     */
    public FilesClient(String username, String password, String authUrl)
    {
        this (username, password, authUrl, null, FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * Mosso-style authentication (No accounts).
     * 
     * @param username     Your CloudFiles username
     * @param apiAccessKey Your CloudFiles API Access Key
     */
    public FilesClient(String username, String apiAccessKey)
    {
        this (username, apiAccessKey, null, null, FilesUtil.getIntProperty("connection_timeout"));
    	//lConnectionManagerogger.warn("LGV");
        //logger.debug("LGV:" + client.getHttpConnectionManager()); 
    }

    /**
     * This method uses the default connection time out of CONNECTON_TIMEOUT and username, password, 
     * and account from FilesUtil
     * 
     */
    public FilesClient()
    {
        this (FilesUtil.getProperty("username"), 
        	  FilesUtil.getProperty("password"),
        	  null,
        	  FilesUtil.getProperty("account"), 
        	  FilesUtil.getIntProperty("connection_timeout"));
    }

    /**
     * Returns the Account associated with the URL
     * 
     * @return The account name
     */
    public String getAccount()
    {
        return account;
    }

    /**
     * Set the Account value and reassemble the Authentication URL.
     *
     * @param account
     */
    public void setAccount(String account)
    {
        this.account = account;
        if (account != null && account.length() > 0) {
        	authenticationURL = FilesUtil.getProperty("auth_url")+VERSION+"/"+account+FilesUtil.getProperty("auth_url_post");
        }
        else {
        	authenticationURL = FilesUtil.getProperty("auth_url");
        }
    }

    /**
     * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
     * 
     * @return true if the login was successful, false otherwise.
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     */
    public boolean login() throws IOException, HttpException
    {
        HttpGet method = new HttpGet(authenticationURL);
        method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);

        method.setHeader(FilesUtil.getProperty("auth_user_header", FilesConstants.X_STORAGE_USER_DEFAULT), 
        		username);
        method.setHeader(FilesUtil.getProperty("auth_pass_header", FilesConstants.X_STORAGE_PASS_DEFAULT), 
        		password);

        FilesResponse response = new FilesResponse(client.execute(method));
        
        if (response.loginSuccess())
        {
            isLoggedin   = true;
            if(usingSnet() || envSnet()){
            	storageURL = snetAddr + response.getStorageURL().substring(8);
            }
            else{
            	storageURL = response.getStorageURL();
            }
            cdnManagementURL = response.getCDNManagementURL();
            authToken = response.getAuthToken();
            logger.debug("storageURL: " + storageURL);
            logger.debug("authToken: " + authToken);
            logger.debug("cdnManagementURL:" + cdnManagementURL);
            logger.debug("ConnectionManager:" + client.getConnectionManager());
        }
        method.abort();

        return isLoggedin;
    }
    

    /**
     * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
     * 
     * @param authToken 
     * @param storageURL 
     * @param cdnManagmentUrl 
     * @return 
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     */
    public boolean login(String authToken, String storageURL, String cdnManagmentUrl) throws IOException, HttpException
    {
    	isLoggedin   = true;
    	this.storageURL = storageURL;
    	cdnManagementURL = cdnManagmentUrl;
    	this.authToken = authToken;
    	return true;
    }
    
    /**
     * List all of the containers available in an account, ordered by container name.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainersInfo(-1, null);
    }
    	
    /**
     * List the containers available in an account, ordered by container name.
     * 
     * @param limit The maximum number of containers to return.  -1 returns an unlimited number.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainersInfo(limit, null);
    }

    /**
     * List the containers available in an account, ordered by container name.
     *
     *  @param limit The maximum number of containers to return.  -1 returns an unlimited number.
	 *  @param marker Return containers that occur after this lexicographically.  
	 *  
     *  @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainerInfo> listContainersInfo(int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	if (!isLoggedin()) {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	HttpGet method = null;
    	try {
    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
       		if(limit > 0) {
    			parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new BasicNameValuePair("marker", marker));
    		}
       		parameters.add(new BasicNameValuePair("format", "xml"));
        		String uri = makeURI(storageURL, parameters);
    		method = new HttpGet(uri);
    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    		FilesResponse response = new FilesResponse(client.execute(method));
    		
    		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
    			if(login()) {
    				method = new HttpGet(uri);
    	    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				response = new FilesResponse(client.execute(method));
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		
    		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document document = builder.parse(response.getResponseBodyAsStream());

    			NodeList nodes = document.getChildNodes();
    			Node accountNode = nodes.item(0);
    			if (! "account".equals(accountNode.getNodeName())) {
    				logger.error("Got unexpected type of XML");
    				return null;
    			}
    			ArrayList <FilesContainerInfo> containerList = new ArrayList<FilesContainerInfo>();
    			NodeList containerNodes = accountNode.getChildNodes();
    			for(int i=0; i < containerNodes.getLength(); ++i) {
    				Node containerNode = containerNodes.item(i);
    				if(!"container".equals(containerNode.getNodeName())) continue;
    				String name = null;
    				int count = -1;
    				long size = -1;
    				NodeList objectData = containerNode.getChildNodes(); 
    				for(int j=0; j < objectData.getLength(); ++j) {   					
    					Node data = objectData.item(j);
    					if ("name".equals(data.getNodeName())) {
    						name = data.getTextContent();
    					}
    					else if ("bytes".equals(data.getNodeName())) {
    						size = Long.parseLong(data.getTextContent());
    					}
    					else if ("count".equals(data.getNodeName())) {
    						count = Integer.parseInt(data.getTextContent());
    					}
    					else {
    						logger.debug("Unexpected container-info tag:" + data.getNodeName());
    					}
    				}
    				if (name != null) {
    					FilesContainerInfo obj = new FilesContainerInfo(name, count, size);
    					containerList.add(obj);
    				}
    			}
    			return containerList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			return new ArrayList<FilesContainerInfo>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Account not Found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected Return Code", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (Exception ex) {
    		throw new FilesException("Unexpected problem, probably in parsing Server XML", ex);
    	}
    	finally {
    		if (method != null)
    			method.abort();
    	}
    }

    /**
     * List the containers available in an account.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainer> listContainers() throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainers(-1, null);
    }
   /**
    * List the containers available in an account.
    *
    * @param limit The maximum number of containers to return.  -1 denotes no limit.

    * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.  
    *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
    */
     public List<FilesContainer> listContainers(int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	return listContainers(limit, null);
    }

    /**
     * List the containers available in an account.
     * 
     * @param limit The maximum number of containers to return.  -1 denotes no limit.
     * @param marker Only return containers after this container.  Null denotes starting at the beginning (lexicographically).  
     *
     * @return A List of FilesContainer with all of the containers in the account.  
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesContainer> listContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (!isLoggedin()) {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	HttpGet method = null;
    	try {
    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
    		
       		if(limit > 0) {
    			parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new BasicNameValuePair("marker", marker));
    		}
       		
       		String uri = parameters.size() > 0 ? makeURI(storageURL, parameters) : storageURL;
    		method = new HttpGet(uri);
    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);   		
    		FilesResponse response = new FilesResponse(client.execute(method));
    		
       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.abort();
    			if(login()) {
    				method = new HttpGet(uri);
    	    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
     				response = new FilesResponse(client.execute(method));
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
  		
    		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			// logger.warn(method.getResponseCharSet());
    			StrTokenizer tokenize = new StrTokenizer(response.getResponseBodyAsString());
    			tokenize.setDelimiterString("\n");
    			String [] containers = tokenize.getTokenArray();    			
    			ArrayList <FilesContainer> containerList = new ArrayList<FilesContainer>();
    			for(String container : containers) { 
    				containerList.add(new FilesContainer(container, this));
    			}
    			return containerList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			return new ArrayList<FilesContainer>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Account was not found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected resposne from server", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    		throw new FilesException("Unexpected error, probably parsing Server XML", ex);
    	}
    	finally {
    		if (method != null) method.abort();
    	}
    }

    /**
     * List all of the objects in a container with the given starting string.
     * 
     * @param container  The container name
     * @param startsWith The string to start with
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjectsStartingWith (String container, String startsWith, String path, int limit, String marker) throws IOException, FilesException
    {
    	return listObjectsStartingWith(container, startsWith, path, limit, marker, null);
    }
    /**
         * List all of the objects in a container with the given starting string.
         * 
         * @param container The container name
         * @param startsWith The string to start with
         * @param path Only look for objects in this path
         * @param limit Return at most <code>limit</code> objects
         * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
         * @param delimiter Use this argument as the delimiter that separates "directories"
         * 
         * @return A list of FilesObjects starting with the given string
         * @throws IOException   There was an IO error doing network communication
         * @throws FilesException There was another error in the request to the server.
         * @throws FilesAuthorizationException The client's login was invalid.
         */
        public List<FilesObject> listObjectsStartingWith (String container, String startsWith, String path, int limit, String marker, Character delimiter) throws IOException, FilesException
        {
    	if (!isLoggedin()) {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	if (!isValidContainerName(container))  {
    		throw new FilesInvalidNameException(container);
    	}
    	HttpGet method = null;
    	try {    		
    		LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
    		parameters.add(new BasicNameValuePair ("format", "xml"));
    		if (startsWith != null) {
    			parameters.add(new BasicNameValuePair (FilesConstants.LIST_CONTAINER_NAME_QUERY, startsWith));    		}
       		if(path != null) {
    			parameters.add(new BasicNameValuePair("path", path));
    		}
       		if(limit > 0) {
    			parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
    		}
       		if(marker != null) {
    			parameters.add(new BasicNameValuePair("marker", marker));
    		}
       		if (delimiter != null) {
       			parameters.add(new BasicNameValuePair("delimiter", delimiter.toString()));
       		}
       		
       		String uri = parameters.size() > 0 ? makeURI(storageURL+"/"+sanitizeForURI(container), parameters) : storageURL;
       		method = new HttpGet(uri);
    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
   		FilesResponse response = new FilesResponse(client.execute(method));
    		
       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    			method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
    			if(login()) {
    				method = new HttpGet(uri);
    	    		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	    		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
     				response = new FilesResponse(client.execute(method));
    			}
    			else {
    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}

       		if (response.getStatusCode() == HttpStatus.SC_OK)
    		{
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			DocumentBuilder builder = factory.newDocumentBuilder();
    			Document document = builder.parse(response.getResponseBodyAsStream());

    			NodeList nodes = document.getChildNodes();
    			Node containerList = nodes.item(0);
    			if (! "container".equals(containerList.getNodeName())) {
    				logger.error("Got unexpected type of XML");
    				return null;
    			}
       			ArrayList <FilesObject> objectList = new ArrayList<FilesObject>();
    			NodeList objectNodes = containerList.getChildNodes();
    			for(int i=0; i < objectNodes.getLength(); ++i) {
    				Node objectNode = objectNodes.item(i);
    				String nodeName = objectNode.getNodeName();
     				if(!("object".equals(nodeName) || "subdir".equals(nodeName))) continue;
    				String name = null;
    				String eTag = null;
    				long size = -1;
    				String mimeType = null;
    				String lastModified = null;
    				NodeList objectData = objectNode.getChildNodes(); 
    				if ("subdir".equals(nodeName)) {
    					size = 0;
    					mimeType = "application/directory";
    					name = objectNode.getAttributes().getNamedItem("name").getNodeValue();
    				}
    				for(int j=0; j < objectData.getLength(); ++j) {
    					Node data = objectData.item(j);
    					if ("name".equals(data.getNodeName())) {
    						name = data.getTextContent();
    					}
    					else if ("content_type".equals(data.getNodeName())) {
    						mimeType = data.getTextContent();
    					}
    					else if ("hash".equals(data.getNodeName())) {
    						eTag = data.getTextContent();
    					}
       					else if ("bytes".equals(data.getNodeName())) {
    						size = Long.parseLong(data.getTextContent());
    					}
       					else if ("last_modified".equals(data.getNodeName())) {
       						lastModified = data.getTextContent();
    					}
    					else {
    						logger.warn("Unexpected tag:" + data.getNodeName());
    					}
    				}
    				if (name != null) {
    					FilesObject obj = new FilesObject(name, container, this);
    					if (eTag != null) obj.setMd5sum(eTag);
    					if (mimeType != null) obj.setMimeType(mimeType); 
    					if (size >= 0) obj.setSize(size);
    					if (lastModified != null) obj.setLastModified(lastModified);
    					objectList.add(obj);
    				}
    			}
    			return objectList;
    		}		
    		else if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    		{	
    			logger.debug ("Container "+container+" has no Objects");
    			return new ArrayList<FilesObject>();
    		}
    		else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    		{
    			throw new FilesNotFoundException("Container was not found", response.getResponseHeaders(), response.getStatusLine());
    		}
    		else {
    			throw new FilesException("Unexpected Server Result", response.getResponseHeaders(), response.getStatusLine());
    		}
    	}
    	catch (FilesNotFoundException fnfe) {
    		throw fnfe;
    	}
    	catch (Exception ex) {
    		logger.error("Error parsing xml", ex);
    		throw new FilesException("Error parsing server resposne", ex);
    	}
    	finally {
    		if (method != null) method.abort();
    	}
    }

        /**
         * List the objects in a container in lexicographic order.  
         * 
         * @param container  The container name
         * 
         * @return A list of FilesObjects starting with the given string
         * @throws IOException   There was an IO error doing network communication
         * @throws FilesException There was another error in the request to the server.
         * @throws FilesAuthorizationException The client's login was invalid.
         */
        public List<FilesObject> listObjects(String container) throws IOException, FilesAuthorizationException, FilesException {
        	return listObjectsStartingWith(container, null, null, -1, null, null);
        }

        /**
         * List the objects in a container in lexicographic order.  
         * 
         * @param container  The container name
         * @param delimiter Use this argument as the delimiter that separates "directories"
         * 
         * @return A list of FilesObjects starting with the given string
         * @throws IOException   There was an IO error doing network communication
         * @throws FilesException There was another error in the request to the server.
         * @throws FilesAuthorizationException The client's login was invalid.
         */
        public List<FilesObject> listObjects(String container, Character delimiter) throws IOException, FilesAuthorizationException, FilesException {
        	return listObjectsStartingWith(container, null, null, -1, null, delimiter);
        }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param limit Return at most <code>limit</code> objects
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, null, limit, null, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesObject> listObjects(String container, String path) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, path, -1, null, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * @param delimiter Use this argument as the delimiter that separates "directories"
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesObject> listObjects(String container, String path, Character delimiter) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, path, -1, null, delimiter);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, String path, int limit) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, path, limit, null);
    }

    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param path Only look for objects in this path
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesObject> listObjects(String container, String path, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, path, limit, marker);
    }
    
    /**
     * List the objects in a container in lexicographic order.  
     * 
     * @param container  The container name
     * @param limit Return at most <code>limit</code> objects
     * @param marker Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.  
     * 
     * @return A list of FilesObjects starting with the given string
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
    public List<FilesObject> listObjects(String container, int limit, String marker) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	return listObjectsStartingWith(container, null, null, limit, marker);
    }

    /**
     * Convenience method to test for the existence of a container in Cloud Files.
     * 
     * @param container The name of the container.
     * @return true if the container exists.  false otherwise.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     */
    public boolean containerExists(String container) throws IOException, HttpException
    {
        try {
        	getContainerInfo(container);
        	return true;
        }
        catch(FilesException fnfe) {
        	return false;
        }
     }
      
    /**
     * Gets information for the given account.
     * 
     * @return The FilesAccountInfo with information about the number of containers and number of bytes used
     *         by the given account.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
     */
   public FilesAccountInfo getAccountInfo() throws IOException, HttpException, FilesAuthorizationException, FilesException
   {
    	if (isLoggedin()) {
    		HttpHead method = null;

    		try {
    			method = new HttpHead(storageURL);
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			FilesResponse response = new FilesResponse(client.execute(method));
    			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
    				if(login()) {
    					method.abort();
    					method = new HttpHead(storageURL);
    					method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    					method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    					response = new FilesResponse(client.execute(method));
    				}
    				else {
    					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}

    			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    			{
    				int nContainers = response.getAccountContainerCount();
    				long totalSize  = response.getAccountBytesUsed();
    				return new FilesAccountInfo(totalSize,nContainers);
    			}
    			else {
    				throw new FilesException("Unexpected return from server", response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		finally {
    			if (method != null) method.abort();
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Get basic information on a container (number of items and the total size).
     *
     * @param container The container to get information for
     * @return ContainerInfo object of the container is present or null if its not present
     * @throws IOException  There was a socket level exception while talking to CloudFiles
     * @throws HttpException There was an protocol level exception while talking to Cloudfiles
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesNotFoundException The container was not found
     * @throws FilesAuthorizationException The client was not logged in or the log in expired.
     */
    public FilesContainerInfo getContainerInfo (String container) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container))
    		{

    			HttpHead method = null;
    			try {
    				method = new HttpHead(storageURL+"/"+sanitizeForURI(container));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.removeHeaders(FilesConstants.X_AUTH_TOKEN);
    					if(login()) {
    						method = new HttpHead(storageURL+"/"+sanitizeForURI(container));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
     						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					int objCount = response.getContainerObjectCount();
    					long objSize  = response.getContainerBytesUsed();
    					return new FilesContainerInfo(container, objCount,objSize);
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Container not found: " + container, response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected result from server", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.abort();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(container);
    		}
    	}
    	else
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    }


    /**
     * Creates a container
     *
     * @param name The name of the container to be created
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client was not property logged in
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesInvalidNameException The container name was invalid
     */
    public void createContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(name))
    		{
    			HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(name));
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			
    			try {
    				FilesResponse response = new FilesResponse(client.execute(method));    	
    				
    	       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.abort();
    	    			if(login()) {
    	    	   			method = new HttpPut(storageURL+"/"+sanitizeForURI(name));
    	        			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	        			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    				response = new FilesResponse(client.execute(method));
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    	    		}

	    			if (response.getStatusCode() == HttpStatus.SC_CREATED)
	    			{
	    				return;
	    			}
	    			else if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
	    			{	
	    				throw new FilesContainerExistsException(name, response.getResponseHeaders(), response.getStatusLine());
	    			}
	    			else {
	    				throw new FilesException("Unexpected Response", response.getResponseHeaders(), response.getStatusLine());
	    			}
    			}
    			finally {
    				method.abort();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Deletes a container
     * 
     * @param name  The name of the container
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The user is not Logged in
     * @throws FilesInvalidNameException   The container name is invalid
     * @throws FilesNotFoundException      The container doesn't exist
     * @throws FilesContainerNotEmptyException The container was not empty
     */
    public boolean deleteContainer(String name) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException, FilesContainerNotEmptyException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(name))
    		{
    			HttpDelete method = new HttpDelete(storageURL+"/"+sanitizeForURI(name));
    			try {
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	   			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        			FilesResponse response = new FilesResponse(client.execute(method));

    	       		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.abort();
    	    			if(login()) {
    	    	   			method = new HttpDelete(storageURL+"/"+sanitizeForURI(name));
    	    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	    	   			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    				response = new FilesResponse(client.execute(method));
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    	    		}

    	       		if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
        			{
        				logger.debug ("Container Deleted : "+name);
        				return true;
        			}
        			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
        			{
        				logger.debug ("Container does not exist !");
           				throw new FilesNotFoundException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
        			}
        			else if (response.getStatusCode() == HttpStatus.SC_CONFLICT)
        			{
        				logger.debug ("Container is not empty, can not delete a none empty container !");
        				throw new FilesContainerNotEmptyException("You can't delete an non-empty container", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {
    				method.abort();
    			}
    		}
    		else
    		{
           		throw new FilesInvalidNameException(name);
    		}
    	}
		else
		{
       		throw new FilesAuthorizationException("You must be logged in", null, null);
		}
    	return false;
    }
    
    /**
     * Enables access of files in this container via the Content Delivery Network.
     * 
     * @param name The name of the container to enable
     * @return The CDN Url of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was an error talking to the CDN Server.
     */
    public String cdnEnableContainer(String name) throws IOException, HttpException, FilesException
    {
    	String returnValue = null;
    	if (isLoggedin())
    	{
    		if (isValidContainerName(name))
    		{
    			HttpPut method = null;
    			try {
    				method = new HttpPut(cdnManagementURL+"/"+sanitizeForURI(name));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						method = new HttpPut(cdnManagementURL+"/"+sanitizeForURI(name));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_ACCEPTED)
    				{
    					returnValue = response.getCdnUrl();
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally	{
    				method.abort();
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else
		{
       		throw new FilesAuthorizationException("You must be logged in", null, null);
		}
    	return returnValue;
    }
    
    public String cdnUpdateContainer(String name, int ttl, boolean enabled, boolean retainLogs) 
    throws IOException, HttpException, FilesException
    {
    	return cdnUpdateContainer(name, ttl, enabled, null, null, retainLogs);
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     * 
     * @param name The name of the container to enable
     * @param ttl How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
     * @param enabled True if this container should be accessible, false otherwise
     * @param retainLogs True if cdn access logs should be kept for this container, false otherwise
     * @return The CDN Url of the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was an error talking to the CDN Service
     */
    /*
     * @param referrerAcl Unused for now
     * @param userAgentACL Unused for now
     */
    private String cdnUpdateContainer(String name, int ttl, boolean enabled, String referrerAcl, String userAgentACL, boolean retainLogs) 
    throws IOException, HttpException, FilesException
    {
    	String returnValue = null;
    	if (isLoggedin())
    	{
    		if (isValidContainerName(name))
    		{
    			HttpPost method = null;
    			try {
    				method = new HttpPost(cdnManagementURL+"/"+sanitizeForURI(name));

    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				// TTL
    				if (ttl > 0) {
    					method.setHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
    				}
       				// Enabled
    				method.setHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));

    				// Log Retention
    				method.setHeader(FilesConstants.X_CDN_RETAIN_LOGS, Boolean.toString(retainLogs));

    				// Referrer ACL
    				if(referrerAcl != null) {
    					method.setHeader(FilesConstants.X_CDN_REFERRER_ACL, referrerAcl);
    				}

    				// User Agent ACL
    				if(userAgentACL != null) {
    					method.setHeader(FilesConstants.X_CDN_USER_AGENT_ACL, userAgentACL);
    				}
     				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						new HttpPost(cdnManagementURL+"/"+sanitizeForURI(name));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						// TTL
    						if (ttl > 0) {
    							method.setHeader(FilesConstants.X_CDN_TTL, Integer.toString(ttl));
    						}
    						// Enabled
    						method.setHeader(FilesConstants.X_CDN_ENABLED, Boolean.toString(enabled));					
    						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_ACCEPTED)
    				{
    					returnValue = response.getCdnUrl();
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected Server Response",response.getResponseHeaders(), response.getStatusLine());
    				}
    			} finally {
    				if (method != null) {
    					method.abort();
    				}
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(name);
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return returnValue;
    }
   
    
    /**
     * Gets current CDN sharing status of the container
     * 
     * @param container The name of the container to enable
     * @return Information on the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was an error talking to the CloudFiles Server
     * @throws FilesNotFoundException The Container has never been CDN enabled
     */
    public FilesCDNContainer getCDNContainerInfo(String container) throws IOException, FilesNotFoundException, HttpException, FilesException
    {
    	if (isLoggedin()) {
    		if (isValidContainerName(container))
    		{
    			HttpHead method = null;
    			try {
    				method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
     						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					FilesCDNContainer result = new FilesCDNContainer(response.getCdnUrl());
    					result.setName(container);
    					result.setSSLURL(response.getCdnSslUrl());
    					result.setStreamingURL(response.getCdnStreamingUrl());
    					for (Header hdr : response.getResponseHeaders()) { 
    						String name = hdr.getName().toLowerCase();
    						if ("x-cdn-enabled".equals(name)) {
    							result.setEnabled(Boolean.valueOf(hdr.getValue()));
    						}
    						else if ("x-log-retention".equals(name)) {
    							result.setRetainLogs(Boolean.valueOf(hdr.getValue()));
    						}
    						else if ("x-ttl".equals(name)) {
    							result.setTtl(Integer.parseInt(hdr.getValue()));
    						}
    						else if ("x-referrer-acl".equals(name)) {
    							result.setReferrerACL(hdr.getValue());
    						}
    						else if ("x-user-agent-acl".equals(name)) {
    							result.setUserAgentACL(hdr.getValue());
    						}
    					}
    					return result;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
       				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
     					throw new FilesNotFoundException("Container is not CDN enabled",response.getResponseHeaders(), response.getStatusLine());
    				}
 
    				else {
    					throw new FilesException("Unexpected result from server: ", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) {
    					method.abort();
    				}
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(container);
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    
    /**
     * Gets current CDN sharing status of the container
     * 
     * @param container The name of the container to enable
     * @return Information on the container
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was an error talking to the CloudFiles Server
     * @throws FilesNotFoundException The Container has never been CDN enabled
     */
    public boolean isCDNEnabled(String container) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin()) {
    		if (isValidContainerName(container))
    		{
    			HttpHead method = null;
    			try {
    				method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						method= new HttpHead(cdnManagementURL+"/"+sanitizeForURI(container));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
     					for (Header hdr : response.getResponseHeaders()) { 
    						String name = hdr.getName().toLowerCase();
    						if ("x-cdn-enabled".equals(name)) {
    							 return Boolean.valueOf(hdr.getValue());
    						}
    					}
     					throw new FilesException("Server did not return X-CDN-Enabled header: ", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					logger.warn("Unauthorized access");
    					throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    				}
       				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
     					return false;
    				}
 
    				else {
    					throw new FilesException("Unexpected result from server: ", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) {
    					method.abort();
    				}
    			}
    		}
    		else
    		{
    			throw new FilesInvalidNameException(container);
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    
    
    /**
     * Creates a path (but not any of the sub portions of the path)
     * 
     * @param container The name of the container.
     * @param path  The name of the Path
     * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
     * @throws IOException There was an error at the socket layer while talking to CloudFiles
     * @throws FilesException There was another error while taking to the CloudFiles server
     */
    public void createPath(String container, String path) throws HttpException, IOException, FilesException {

		if (!isValidContainerName(container))
			throw new FilesInvalidNameException(container);
		if (!isValidObjectName(path))
			throw new FilesInvalidNameException(path);
		storeObject(container, new byte[0], "application/directory", path,
				new HashMap<String, String>());
	}

    /**
     * Create all of the path elements for the entire tree for a given path.  Thus, <code>createFullPath("myContainer", "foo/bar/baz")</code> 
     * creates the paths "foo", "foo/bar" and "foo/bar/baz".
     * 
     * @param container The name of the container
     * @param path The full name of the path
     * @throws HttpException There was an error at the protocol layer while talking to CloudFiles
     * @throws IOException There was an error at the socket layer while talking to CloudFiles
     * @throws FilesException There was another error while taking to the CloudFiles server
     */
    public void createFullPath(String container, String path) throws HttpException, IOException, FilesException {
    	String parts[] = path.split("/");
    	
    	for(int i=0; i < parts.length; ++i) {
    		StringBuilder sb = new StringBuilder();
    		for (int j=0; j <= i; ++j) {
    			if (sb.length() != 0) 
    				sb.append('/');
    			sb.append(parts[j]);
    		}
    		createPath(container, sb.toString());
    	}
    	
    }

    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<String> listCdnContainers(int limit) throws IOException, HttpException, FilesException
    {
    	return listCdnContainers(limit, null);
    }

    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
   public List<String> listCdnContainers() throws IOException, HttpException, FilesException
    {
    	return listCdnContainers(-1, null);
    }

    
    /**
     * Gets the names of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @param marker All of the results will come after <code>marker</code> lexicographically.
     * @return A list of container names
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<String> listCdnContainers(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		HttpGet method = null;
    		try {
     			LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
    			if (limit > 0) {
    				params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
    			}
    			if (marker != null) {
    				params.add(new BasicNameValuePair("marker", marker));
    			}
    			String uri = (params.size() > 0) ? makeURI(cdnManagementURL, params) : cdnManagementURL;
       			method = new HttpGet(uri);
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			FilesResponse response = new FilesResponse(client.execute(method));

    			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				method.abort();
    				if(login()) {
    					method = new HttpGet(uri);
    					method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    					method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    					response = new FilesResponse(client.execute(method));
    				}
    				else {
    					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				StrTokenizer tokenize = new StrTokenizer(response.getResponseBodyAsString());
    				tokenize.setDelimiterString("\n");
    				String [] containers = tokenize.getTokenArray();
    				List<String> returnValue = new ArrayList<String>();
    				for (String containerName: containers)
    				{
    					returnValue.add(containerName);
    				}
    				return returnValue;
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				logger.warn("Unauthorized access");
    				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    			}
    			else {
    				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		finally {
    			if (method != null) method.abort();
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    
    /**
     * Purges all items from a given container from the CDN
     * 
     * @param container The name of the container
     * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete. 
     *                       <code>null</code> if desired.
     * @throws IOException Error talking to the CDN management server
     * @throws HttpException Error with HTTP
     * @throws FilesAuthorizationException Log in was not successful, or account is suspended 
     * @throws FilesException Other error
     */
    public void purgeCDNContainer(String container, String emailAddresses) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	if (! isLoggedin) {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
       	if (!isValidContainerName(container))  {
    		throw new FilesInvalidNameException(container);
    	}
    	HttpDelete method = null;
    	try {
    		String deleteUri = cdnManagementURL + "/" + sanitizeForURI(container);
			method = new HttpDelete(deleteUri);
			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
			if (emailAddresses != null) {
				method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
			}
		
			FilesResponse response = new FilesResponse(client.execute(method));
			
			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				method.abort();
				if(login()) {
					method = new HttpDelete(deleteUri);
					method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
					method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
					if (emailAddresses != null) {
						method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
					}
					response = new FilesResponse(client.execute(method));
				}
				else {
					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
				}
			}

			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
			{
				return;
			}
			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
			}
			else {
				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
			}
		}
		finally {
			if (method != null) method.abort();
		}

    }

    /**
     * Purges all items from a given container from the CDN
     * 
     * @param container The name of the container
     * @param object The name of the object
     * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete. 
     *                       <code>null</code> if desired.
     * @throws IOException Error talking to the CDN management server
     * @throws HttpException Error with HTTP
     * @throws FilesAuthorizationException Log in was not successful, or account is suspended 
     * @throws FilesException Other error
     */
    public void purgeCDNObject(String container, String object, String emailAddresses) throws IOException, HttpException, FilesAuthorizationException, FilesException {
    	if (! isLoggedin) {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
       	if (!isValidContainerName(container))  {
    		throw new FilesInvalidNameException(container);
    	}
    	HttpDelete method = null;
    	try {
    		String deleteUri = cdnManagementURL + "/" + sanitizeForURI(container) +"/"+sanitizeAndPreserveSlashes(object);
			method = new HttpDelete(deleteUri);
			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
			if (emailAddresses != null) {
				method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
			}
		
			FilesResponse response = new FilesResponse(client.execute(method));
			
			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				method.abort();
				if(login()) {
					method = new HttpDelete(deleteUri);
					method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
					method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
					if (emailAddresses != null) {
						method.setHeader(FilesConstants.X_PURGE_EMAIL, emailAddresses);
					}
					response = new FilesResponse(client.execute(method));
				}
				else {
					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
				}
			}

			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
			{
				return;
			}
			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
			}
			else {
				System.out.println(response.getStatusLine());
				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
			}
		}
		finally {
			if (method != null) method.abort();
		}

    }

    /**
     * Gets list of all of the containers associated with this account.
     * 
      * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesCDNContainer> listCdnContainerInfo() throws IOException, HttpException, FilesException
    {
    	return listCdnContainerInfo(-1, null);
    }
    /**
     * Gets list of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
   public List<FilesCDNContainer> listCdnContainerInfo(int limit) throws IOException, HttpException, FilesException
    {
    	return listCdnContainerInfo(limit, null);
    }
    /**
     * Gets list of all of the containers associated with this account.
     * 
     * @param limit The maximum number of container names to return
     * @param marker All of the names will come after <code>marker</code> lexicographically.
     * @return A list of containers
     * 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public List<FilesCDNContainer> listCdnContainerInfo(int limit, String marker) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		HttpGet method = null;
    		try {
    			LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
    			params.add(new BasicNameValuePair("format", "xml"));
    			if (limit > 0) {
    				params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
    			}
    			if (marker != null) {
    				params.add(new BasicNameValuePair("marker", marker));
    			}
    			String uri = params.size() > 0 ? makeURI(cdnManagementURL, params) : cdnManagementURL;
    			method = new HttpGet(uri);
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    		
    			FilesResponse response = new FilesResponse(client.execute(method));

    			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				method.abort();
    				if(login()) {
    					method = new HttpGet(uri);
    					method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    					method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    			
    					response = new FilesResponse(client.execute(method));
    				}
    				else {
    					throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}

    			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
     				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     				DocumentBuilder builder = factory.newDocumentBuilder();
     				Document document = builder.parse(response.getResponseBodyAsStream());

     	    		NodeList nodes = document.getChildNodes();
     	    		Node accountNode = nodes.item(0);
     	    		if (! "account".equals(accountNode.getNodeName())) {
     	    			logger.error("Got unexpected type of XML");
     	    			return null;
     	    		}
     	    		ArrayList <FilesCDNContainer> containerList = new ArrayList<FilesCDNContainer>();
     	    		NodeList containerNodes = accountNode.getChildNodes();
     	    		for(int i=0; i < containerNodes.getLength(); ++i) {
     	    			Node containerNode = containerNodes.item(i);
     	    			if(!"container".equals(containerNode.getNodeName())) continue;
     	    			FilesCDNContainer container = new FilesCDNContainer();
     	    			NodeList objectData = containerNode.getChildNodes(); 
     	    			for(int j=0; j < objectData.getLength(); ++j) {   					
     	    				Node data = objectData.item(j);
     	    				if ("name".equals(data.getNodeName())) {
     	    					container.setName(data.getTextContent());
     	    				}
    	    				else if ("cdn_url".equals(data.getNodeName())) {
     	    					container.setCdnURL(data.getTextContent());
     	    				}
    	    				else if ("cdn_ssl_url".equals(data.getNodeName())) {
     	    					container.setSSLURL(data.getTextContent());
     	    				}
    	    				else if ("cdn_streaming_url".equals(data.getNodeName())) {
     	    					container.setStreamingURL(data.getTextContent());
     	    				}
     	    				else if ("cdn_enabled".equals(data.getNodeName())) {
     	    					container.setEnabled(Boolean.parseBoolean(data.getTextContent()));
     	    				}
     	    				else if ("log_retention".equals(data.getNodeName())) {
     	    					container.setRetainLogs(Boolean.parseBoolean(data.getTextContent()));
     	    				}
     	    				else if ("ttl".equals(data.getNodeName())) {
     	    					container.setTtl(Integer.parseInt(data.getTextContent()));
     	    				}
     	    				else if ("referrer_acl".equals(data.getNodeName())) {
     	    					container.setReferrerACL(data.getTextContent());
     	    				}
     	    				else if ("useragent_acl".equals(data.getNodeName())) {
     	    					container.setUserAgentACL(data.getTextContent());
     	    				}
     	    				else {
     	    					//logger.warn("Unexpected container-info tag:" + data.getNodeName());
     	    				}
     	    			}	
     	    			if (container.getName() != null) {
     	    				containerList.add(container);
     	    			}
     	    		}
     	    		return containerList;
    			}	
    			else if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    				logger.warn("Unauthorized access");
    				throw new FilesAuthorizationException("User not Authorized!",response.getResponseHeaders(), response.getStatusLine());
    			}
    			else {
    				throw new FilesException("Unexpected server response",response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		catch (SAXException ex) {
    			// probably a problem parsing the XML
    			throw new FilesException("Problem parsing XML", ex);
    		}
    		catch (ParserConfigurationException ex) {
    			// probably a problem parsing the XML
    			throw new FilesException("Problem parsing XML", ex);
    		}
    		finally {
    			if (method != null) method.abort();
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }
    /**
     * Create a manifest on the server, including metadata
     * 
     * @param container   The name of the container
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param manifest    Set manifest content here
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	return createManifestObject(container, contentType, name, manifest, new HashMap<String, String>(), callback);
    }
    /**
     * Create a manifest on the server, including metadata
     * 
     * @param container   The name of the container
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param manifest    Set manifest content here
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	return createManifestObject(container, contentType, name, manifest, metadata, null);
    }
    /**
     * Create a manifest on the server, including metadata
     * 
     * @param container   The name of the container
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param manifest    Set manifest content here
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public boolean createManifestObject(String container, String contentType, String name, String manifest, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	byte[] arr = new byte[0];
    	if (isLoggedin())
    	{
    		String objName	 =  name;
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{

    			HttpPut method = null;
    			try {
    				method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			    method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
    				ByteArrayEntity entity = new ByteArrayEntity (arr);
    				entity.setContentType(contentType);
    				method.setEntity(new RequestEntityWrapper(entity, callback));
    				for(String key : metadata.keySet()) {
    					// logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
    					method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    				}
    				
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						if (manifest != null){
    	    					method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
    	    				}    						
    		   				entity = new ByteArrayEntity (arr);
    	    				entity.setContentType(contentType);
    						method.setEntity(new RequestEntityWrapper(entity, callback));
    						for(String key : metadata.keySet()) {
    							method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    						}
    						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_CREATED)
    				{
    					return true;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
    				{
    					throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
    				{
    					throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else 
    				{
    					throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally{
    				if (method != null) method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Store a file on the server
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @return The ETAG if the save was successful, null otherwise
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public String storeObjectAs (String container, File obj, String contentType, String name) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), null);
    }	
    
    /**
     * Store a file on the server
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @return The ETAG if the save was successful, null otherwise
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public String storeObjectAs (String container, File obj, String contentType, String name, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, name, new HashMap<String,String>(), callback);
    }	
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return The ETAG if the save was successful, null otherwise
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     * @throws FilesAuthorizationException The client's login was invalid.
      */
    public String storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs (container, obj, contentType, name, metadata, null);
    }
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param callback    The callback object that will be called as the data is sent
     * @return The ETAG if the save was successful, null otherwise
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public String storeObjectAs (String container, File obj, String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(name) )
    		{
    			if (!obj.exists())
    			{
    				throw new FileNotFoundException(name + " does not exist");
    			}

    			if (obj.isDirectory())
    			{
    				throw new IOException("The alleged file was a directory");				
    			}

    			HttpPut method = null;
    			try {
    				method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				if (useETag) {
    					method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
    				}
    				ContentType ct = ContentType.create(contentType);
    				method.setEntity(new RequestEntityWrapper(new FileEntity(obj, ct), callback));
    				for(String key : metadata.keySet()) {
    					method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    				}
    				FilesResponse response = new FilesResponse(client.execute(method));
    				
    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    	       			method.abort();
    	    			if(login()) {
    	    				method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(name));
    	    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    	    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    	    				if (useETag) {
    	    					method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
    	    				}
    	    				method.setEntity(new RequestEntityWrapper(new FileEntity(obj, ct), callback));
    	    				for(String key : metadata.keySet()) {
    	    					method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    	    				}
    	    				response = new FilesResponse(client.execute(method));
    	    			}
    	    			else {
    	    				throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    	    			}
    				}
    				if (response.getStatusCode() == HttpStatus.SC_CREATED)
    				{
    					return response.getResponseHeader(FilesConstants.E_TAG).getValue();
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
    				{
    					throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
    				{
    					throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else 
    				{
    					throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(name)) {
    				throw new FilesInvalidNameException(name);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }


    /**
     * Copies the file to Cloud Files, keeping the original file name in Cloud Files.
     * 
     * @param container    The name of the container to place the file in
     * @param obj          The File to transfer
     * @param contentType  The file's MIME type
     * @return The ETAG if the save was successful, null otherwise
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public String storeObject (String container, File obj, String contentType) throws IOException, HttpException, FilesException
    {
    	return storeObjectAs(container, obj, contentType, obj.getName());
    }

    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	return storeObject(container, obj, contentType, name, metadata, null);
    }
    
    /**
     * Store a file on the server, including metadata
     * 
     * @param container   The name of the container
     * @param obj         The File containing the file to copy over
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @param callback    The object to which any callbacks will be sent (null if you don't want callbacks)
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String,String> metadata, IFilesTransferCallback callback) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		String objName	 =  name;
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{

    			HttpPut method = null;
    			try {
    				method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				if (useETag) {
    					method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
    				}
    				ByteArrayEntity entity = new ByteArrayEntity (obj);
    				entity.setContentType(contentType);
    				method.setEntity(new RequestEntityWrapper(entity, callback));
    				for(String key : metadata.keySet()) {
    					// logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
    					method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    				}
    				
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    					method.abort();
    					if(login()) {
    						method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    						method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    						method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    						if (useETag) {
    							method.setHeader(FilesConstants.E_TAG, md5Sum (obj));
    						}
    		   				entity = new ByteArrayEntity (obj);
    	    				entity.setContentType(contentType);
    						method.setEntity(new RequestEntityWrapper(entity, callback));
    						for(String key : metadata.keySet()) {
    							method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    						}
    						response = new FilesResponse(client.execute(method));
    					}
    					else {
    						throw new FilesAuthorizationException("Re-login failed", response.getResponseHeaders(), response.getStatusLine());
    					}
    				}

    				if (response.getStatusCode() == HttpStatus.SC_CREATED)
    				{
    					return true;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED)
    				{
    					throw new FilesException("Etag missmatch", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_LENGTH_REQUIRED)
    				{
    					throw new FilesException("Length miss-match", response.getResponseHeaders(), response.getStatusLine());
    				}
    				else 
    				{
    					throw new FilesException("Unexpected Server Response", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally{
    				if (method != null) method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Store a file on the server, including metadata, with the contents coming from an input stream.  This allows you to 
     * not know the entire length of your content when you start to write it.  Nor do you have to hold it entirely in memory
     * at the same time.
     * 
     * @param container   The name of the container
     * @param data        Any object that implements InputStream
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return 
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public String storeStreamedObject(String container, InputStream data, String contentType, String name, Map<String,String> metadata) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
			String objName	 =  name;
			if (isValidContainerName(container) && isValidObjectName(objName))
    		{
				HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
     			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			InputStreamEntity entity = new InputStreamEntity(data, -1);
    			entity.setChunked(true);
    			entity.setContentType(contentType);
    			method.setEntity(entity);
    			for(String key : metadata.keySet()) {
    				// logger.warn("Key:" + key + ":" + sanitizeForURI(metadata.get(key)));
    				method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    			}
    			method.removeHeaders("Content-Length");
  
    			
    			try {
        			FilesResponse response = new FilesResponse(client.execute(method));
        			
        			if (response.getStatusCode() == HttpStatus.SC_CREATED)
        			{
        				return response.getResponseHeader(FilesConstants.E_TAG).getValue();
        			}
        			else {
        				logger.error(response.getStatusLine());
        				throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {	
    				method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {       		
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

   /**
    * 
    * 
    * @param container The name of the container
    * @param name The name of the object
    * @param entity The name of the request entity (make sure to set the Content-Type
    * @param metadata The metadata for the object
    * @param md5sum The 32 character hex encoded MD5 sum of the data
    * @return The ETAG if the save was successful, null otherwise
    * @throws IOException There was a socket level exception talking to CloudFiles
    * @throws HttpException There was a protocol level error talking to CloudFiles
    * @throws FilesException There was an error talking to CloudFiles.
    */
public String storeObjectAs(String container, String name, HttpEntity entity, Map<String,String> metadata, String md5sum) throws IOException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
			String objName	 =  name;
			if (isValidContainerName(container) && isValidObjectName(objName))
    		{
				HttpPut method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			method.setEntity(entity);
   				if (useETag && md5sum != null) {
					method.setHeader(FilesConstants.E_TAG, md5sum);
   				}
    			method.setHeader(entity.getContentType());
    
    			for(String key : metadata.keySet()) {
    				method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
    			}
    			
    			try {
        			FilesResponse response = new FilesResponse(client.execute(method));
        			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
        				method.abort();
        				login();
        				method = new HttpPut(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
            			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
            			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
            			method.setEntity(entity);
            			method.setHeader(entity.getContentType());
            			for(String key : metadata.keySet()) {
            				method.setHeader(FilesConstants.X_OBJECT_META + key, sanitizeForURI(metadata.get(key)));
            			}
            			response = new FilesResponse(client.execute(method));
        			}
        			
        			if (response.getStatusCode() == HttpStatus.SC_CREATED)
        			{
        				return response.getResponseHeader(FilesConstants.E_TAG).getValue();
        			}
        			else {
        				logger.debug(response.getStatusLine());
        				throw new FilesException("Unexpected result", response.getResponseHeaders(), response.getStatusLine());
        			}
    			}
    			finally {	
    				method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {       		
    		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * This method copies the object found in the source container with the
     * source object name to the destination container with the destination
     * object name.
     * @param sourceContainer of object to copy
     * @param sourceObjName of object to copy
     * @param destContainer where object copy will be copied
     * @param destObjName of object copy
     * @return ETAG if successful, else null
     * @throws IOException indicates a socket level error talking to CloudFiles
     * @throws HttpException indicates a protocol level error talking to CloudFiles
     * @throws FilesException indicates an error talking to CloudFiles
     */
    public String copyObject(String sourceContainer,
                             String sourceObjName,
                             String destContainer,
                             String destObjName)
        throws HttpException, IOException {
        String etag = null;
        if (isLoggedin()) {

            if (isValidContainerName(sourceContainer) &&
                isValidObjectName(sourceObjName) &&
                isValidContainerName(destContainer) &&
                isValidObjectName(destObjName)) {

                HttpPut method = null;
                try {
                    String sourceURI = sanitizeForURI(sourceContainer) +
                        "/" + sanitizeForURI(sourceObjName);
                    String destinationURI = sanitizeForURI(destContainer) +
                        "/" + sanitizeForURI(destObjName);

                    method = new HttpPut(storageURL + "/" + destinationURI);
                    method.getParams().setIntParameter("http.socket.timeout",
                                                       connectionTimeOut);
                    method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                    method.setHeader(FilesConstants.X_COPY_FROM, sourceURI);

                    FilesResponse response = new FilesResponse(client.execute(
                        method));

                    if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                        method.abort();

                        login();
                        method = new HttpPut(storageURL + "/" + destinationURI);
                        method.getParams().setIntParameter("http.socket.timeout",
                                                           connectionTimeOut);
                        method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        method.setHeader(FilesConstants.X_COPY_FROM, sourceURI);

                        response = new FilesResponse(client.execute(method));
                    }

                    if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                        etag = response.getResponseHeader(FilesConstants.E_TAG)
                            .getValue();

                    } else {
                        throw new FilesException("Unexpected status from server",
                                                 response.getResponseHeaders(),
                                                 response.getStatusLine());
                    }

                } finally {
                    if (method != null) {
                        method.abort();
                    }
                }
            } else {
                if (!isValidContainerName(sourceContainer)) {
                    throw new FilesInvalidNameException(sourceContainer);
                } else if (!isValidObjectName(sourceObjName)) {
                    throw new FilesInvalidNameException(sourceObjName);
                } else if (!isValidContainerName(destContainer)) {
                    throw new FilesInvalidNameException(destContainer);
                } else {
                    throw new FilesInvalidNameException(destObjName);
                }
            }
        } else {
            throw new FilesAuthorizationException("You must be logged in",
                                                  null,
                                                  null);
        }

        return etag;
    }

    /**
     * Delete the given object from it's container.
     * 
     * @param container  The container name
     * @param objName    The object name
     * @throws IOException   There was an IO error doing network communication
     * @throws FilesNotFoundException The container does not exist
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesException There was another error in the request to the server.
     */
    public void deleteObject (String container, String objName) throws IOException, FilesNotFoundException, HttpException, FilesException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{
    			HttpDelete method = null;
    			try {
    				method = new HttpDelete(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));
    				
           			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
           				method.abort();
        				login();
           				method = new HttpDelete(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
            			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
        				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
        				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        				response = new FilesResponse(client.execute(method));
        			}


    				if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT)
    				{
    					logger.debug ("Object Deleted : "+objName);
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Object was not found " + objName, response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    					throw new FilesException("Unexpected status from server", response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				if (method != null) method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }

    /**
     * Get an object's metadata
     * 
     * @param container The name of the container
     * @param objName   The name of the object
     * @return The object's metadata
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     * @throws FilesNotFoundException The file was not found
     */
    public FilesObjectMetaData getObjectMetaData (String container, String objName) throws IOException, FilesNotFoundException, HttpException, FilesAuthorizationException, FilesInvalidNameException
    {
    	FilesObjectMetaData metaData;
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{
    			HttpHead method = new HttpHead(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			try {
    				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    				FilesResponse response = new FilesResponse(client.execute(method));
   				
           			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
           				method.abort();
        				login();
           				method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
        				method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        				response = new FilesResponse(client.execute(method));
        			}

           			if (response.getStatusCode() == HttpStatus.SC_NO_CONTENT ||
           			    response.getStatusCode() == HttpStatus.SC_OK)
    				{
    					logger.debug ("Object metadata retreived  : "+objName);
    					String mimeType = response.getContentType();
    					String lastModified = response.getLastModified();
    					String eTag = response.getETag();
    					String contentLength = response.getContentLength();

    					metaData = new FilesObjectMetaData(mimeType, contentLength, eTag, lastModified);

    					Header [] headers = response.getResponseHeaders();
    					HashMap<String,String> headerMap = new HashMap<String,String>();

    					for (Header h: headers)
    					{
    						if ( h.getName().startsWith(FilesConstants.X_OBJECT_META) )
    						{
    							headerMap.put(h.getName().substring(FilesConstants.X_OBJECT_META.length()), unencodeURI(h.getValue()));
    						}
    					}
    					if (headerMap.size() > 0)
    						metaData.setMetaData(headerMap);

    					return metaData;
    				}
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Container: " + container + " did not have object " + objName, 
								 response.getResponseHeaders(), response.getStatusLine());
    				}
    				else {
    						throw new FilesException("Unexpected Return Code from Server", 
    								response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    }


    /**
     * Get the content of the given object
     * 
     * @param container  The name of the container
     * @param objName    The name of the object
     * @return The content of the object
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws FilesInvalidNameException 
     * @throws FilesNotFoundException The container does not exist
     */
    public byte[] getObject (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{
    			HttpGet method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);

    			try {
    				FilesResponse response = new FilesResponse(client.execute(method));

    				if (response.getStatusCode() == HttpStatus.SC_OK)
    				{	
    					logger.debug ("Object data retreived  : "+objName);
    					return response.getResponseBody();
    				}	
    				else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    				{
    					throw new FilesNotFoundException("Container: " + container + " did not have object " + objName, 
									 response.getResponseHeaders(), response.getStatusLine());
    				}
    			}
    			finally {
    				method.abort();
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }

    /**
     * Get's the given object's content as a stream
     * 
     * @param container  The name of the container
     * @param objName    The name of the object
     * @return An input stream that will give the objects content when read from.
     * @throws IOException   There was an IO error doing network communication
     * @throws HttpException There was an error with the HTTP protocol
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws FilesNotFoundException The container does not exist
     * @throws FilesInvalidNameException 
     */
    public InputStream getObjectAsStream (String container, String objName) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{
    			if (objName.length() > FilesConstants.OBJECT_NAME_LENGTH)
    			{
    				logger.warn ("Object Name supplied was truncated to Max allowed of " + FilesConstants.OBJECT_NAME_LENGTH + " characters !");
    				objName = objName.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
    				logger.warn ("Truncated Object Name is: " + objName);
    			}

    			HttpGet method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
    			FilesResponse response = new FilesResponse(client.execute(method));

      			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
       				method.abort();
    				login();
    				method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
        			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
        			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        			response = new FilesResponse(client.execute(method));
    			}

      			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				logger.info ("Object data retreived  : "+objName);
    				// DO NOT RELEASE THIS CONNECTION
    				return response.getResponseBodyAsStream();
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    			{
    				method.abort();
					throw new FilesNotFoundException("Container: " + container + " did not have object " + objName, 
							 response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }

    public InputStream getObjectAsRangedStream (String container, String objName, long offset, long length) throws IOException, HttpException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
    	if (isLoggedin())
    	{
    		if (isValidContainerName(container) && isValidObjectName(objName))
    		{
    			if (objName.length() > FilesConstants.OBJECT_NAME_LENGTH)
    			{
    				logger.warn ("Object Name supplied was truncated to Max allowed of " + FilesConstants.OBJECT_NAME_LENGTH + " characters !");
    				objName = objName.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
    				logger.warn ("Truncated Object Name is: " + objName);
    			}

    			HttpGet method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
    			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
    			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
                        method.setHeader("Range", "bytes="+offset+"-"+length);
    			FilesResponse response = new FilesResponse(client.execute(method));

      			if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
       				method.abort();
    				login();
    				method = new HttpGet(storageURL+"/"+sanitizeForURI(container)+"/"+sanitizeForURI(objName));
        			method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
        			method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
        			response = new FilesResponse(client.execute(method));
    			}

      			if (response.getStatusCode() == HttpStatus.SC_OK)
    			{
    				logger.info ("Object data retreived  : "+objName);
    				// DO NOT RELEASE THIS CONNECTION
    				return response.getResponseBodyAsStream();
    			}
    			else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND)
    			{
    				method.abort();
					throw new FilesNotFoundException("Container: " + container + " did not have object " + objName, 
							 response.getResponseHeaders(), response.getStatusLine());
    			}
    		}
    		else
    		{
    			if (!isValidObjectName(objName)) {
    				throw new FilesInvalidNameException(objName);
    			}
    			else {
    				throw new FilesInvalidNameException(container);
    			}
    		}
    	}
    	else {
       		throw new FilesAuthorizationException("You must be logged in", null, null);
    	}
    	return null;
    }


    /**
     * Utility function to write an InputStream to a file
     * 
     * @param is stream
     * @param f file to write the stream to
     * @throws IOException
     */
    static void writeInputStreamToFile (InputStream is, File f) throws IOException
    {
    	BufferedOutputStream bf = new BufferedOutputStream (new FileOutputStream (f));
    	byte[] buffer = new byte [1024];
    	int read = 0;

    	while ((read = is.read(buffer)) > 0)
    	{
    		bf.write(buffer, 0, read);
    	}

    	is.close();
    	bf.flush();
    	bf.close();
    }
    
    /**
     * Reads an input stream into a stream
     * 
     * @param stream The input stream
     * @param encoding encoding of the stream
     * @return The contents of the stream stored in a string.
     * @throws IOException
     */
    static String inputStreamToString(InputStream stream, String encoding) throws IOException {
    	char buffer[] = new char[4096];
    	StringBuilder sb = new StringBuilder();
    	InputStreamReader isr = new InputStreamReader(stream, "utf-8"); // For now, assume utf-8 to work around server bug
    	
    	int nRead = 0;
    	while((nRead = isr.read(buffer)) >= 0) {
    		sb.append(buffer, 0, nRead);
    	}
    	isr.close();
    	
    	return sb.toString();
    }

    /**
     * Calculates the MD5 checksum of a file, returned as a hex encoded string
     * 
     * @param f The file
     * @return The MD5 checksum, as a base 16 encoded string
     * @throws IOException
     */
    public static String md5Sum (File f) throws IOException
    {
    	MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
	    	InputStream is = new FileInputStream(f);
	    	byte[] buffer = new byte[1024];
	    	int read = 0;

	    	while( (read = is.read(buffer)) > 0)
	    	{
	    		digest.update(buffer, 0, read);
	    	}

	    	is.close ();

	    	byte[] md5sum = digest.digest();
	    	BigInteger bigInt = new BigInteger(1, md5sum);

	       	// Front load any zeros cut off by BigInteger
	    	String md5 = bigInt.toString(16);
	    	while (md5.length() != 32) {
	    		md5 = "0" + md5;
	    	}
	    	return md5;
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("The JRE is misconfigured on this computer", e);
			return null;
		}
    }
    
    /**
     * Calculates the MD5 checksum of an array of data
     * 
     * @param data The data to checksum
     * @return The checksum, represented as a base 16 encoded string.
     * @throws IOException
     */
    public static String md5Sum (byte[] data) throws IOException
    {
    	try {
    		MessageDigest digest = MessageDigest.getInstance("MD5");
        	byte[] md5sum = digest.digest(data);
        	BigInteger bigInt = new BigInteger(1, md5sum);

        	// Front load any zeros cut off by BigInteger
        	String md5 = bigInt.toString(16);
        	while (md5.length() != 32) {
        		md5 = "0" + md5;
        	}
        	return md5;
    	}
    	catch (NoSuchAlgorithmException nsae) {
    		logger.fatal("Major problems with your Java configuration", nsae);
    		return null;
    	}

    }
    
    /**
     * Encode any unicode characters that will cause us problems.
     * 
     * @param str string to sanitize
     * @return The string encoded for a URI
     */
    public static String sanitizeForURI(String str) {
    	URLCodec codec= new URLCodec();
    	try {
    		return codec.encode(str).replaceAll("\\+", "%20");
    	}
    	catch (EncoderException ee) {
    		logger.warn("Error trying to encode string for URI", ee);
    		return str;
    	}
    }
    
    public static String sanitizeAndPreserveSlashes(String str) {
    	URLCodec codec= new URLCodec();
    	try {
    		return codec.encode(str).replaceAll("\\+", "%20").replaceAll("%2F", "/");
     	}
    	catch (EncoderException ee) {
    		logger.warn("Error trying to encode string for URI", ee);
    		return str;
    	}
    }
    
    public static String unencodeURI(String str) {
       	URLCodec codec= new URLCodec();
    	try {
    		return codec.decode(str);
    	}
    	catch (DecoderException ee) {
    		logger.warn("Error trying to encode string for URI", ee);
    		return str;
    	}
   	
    }

    /**
     * @return The connection timeout used for communicating with the server (in milliseconds)
     */
    public int getConnectionTimeOut()
    {
    	return connectionTimeOut;
    }

    /**
     * The timeout we will use for communicating with the server (in milliseconds)
     * 
     * @param connectionTimeOut The new timeout for this connection
     */
    public void setConnectionTimeOut(int connectionTimeOut)
    {
    	this.connectionTimeOut = connectionTimeOut;
    }

    /**
     * @return The storage URL on the other end of the ReST api
     */
    public String getStorageURL()
    {
    	return storageURL;
    }

    /**
     * @return Get's our storage token.
     */
    @Deprecated
    public String getStorageToken()
    {
    	return authToken;
    }

    /**
     * @return Get's our storage token.
     */
    public String getAuthToken()
    {
    	return authToken;
    }

    /**
     * Has this instance of the client authenticated itself?  Note, this does not mean that a call 
     * right now will work, if the auth token has timed out, you will need to re-auth.
     * 
     * @return True if we logged in, false otherwise.
     */
    public boolean isLoggedin()
    {
    	return isLoggedin;
    }

    /**
     * The username we are logged in with.
     * 
     * @return The username
     */
    public String getUserName()
    {
    	return username;
    }

    /**
     * Set's the username for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
     * 
     * @param userName the username
     */
    public void setUserName(String userName)
    {
    	username = userName;
    }

    /**
     * The password the client will use for the login.
     * 
     * @return The password
     */
    public String getPassword()
    {
    	return password;
    }

    /**
     * Set's the password for this client. Note, setting this after login has no real impact unless the <code>login()</code>
     * method is called again.
	 *
     * @param password The new password
     */
    public void setPassword(String password)
    {
    	this.password = password;
    }

    /**
     * The URL we will use for Authentication
     * 
     * @return The URL (represented as a string)
     */
    public String getAuthenticationURL()
    {
    	return authenticationURL;
    }

    /**
     * Changes the URL of the authentication service.  Note, if one is logged in, this doesn't have an effect unless one calls login again.
     * 
     * @param authenticationURL The new authentication URL
     */
    public void setAuthenticationURL(String authenticationURL)
    {
    	this.authenticationURL = authenticationURL;
    }

	/**
	 * @return the useETag
	 */
	public boolean getUseETag() {
		return useETag;
	}

	/**
	 * @param useETag the useETag to set
	 */
	public void setUseETag(boolean useETag) {
		this.useETag = useETag;
	}
	
	public void setUserAgent(String userAgent) {
		client.getParams().setParameter(HTTP.USER_AGENT, userAgent);
	}
	
	public String getUserAgent() {
		return client.getParams().getParameter(HTTP.USER_AGENT).toString();
	}
	
	public static boolean isValidContainerName(String name) {
		if (name == null) return false;
		int length = name.length();
		if (length == 0 || length > FilesConstants.CONTAINER_NAME_LENGTH) return false;
		if (name.indexOf('/') != -1) return false;
		//if (name.indexOf('?') != -1) return false;
		return true;
	}
        public static boolean isValidObjectName(String name) {
		if (name == null) return false;
		int length = name.length();
		if (length == 0 || length > FilesConstants.OBJECT_NAME_LENGTH) return false;
		//if (name.indexOf('?') != -1) return false;
		return true;
	}

	/**
	 * @return the cdnManagementURL
	 */
	public String getCdnManagementURL() {
		return cdnManagementURL;
	}
	

    /**
     * @param container The container name
     * @param object 
     * @param manifest Set manifest content here
     * @return 
     * @throws FilesAuthorizationException The client's login was invalid.
     * @throws HttpException There was an error with the HTTP protocol
     * @throws IOException 
     * @throws FilesInvalidNameException 
     */
	public boolean updateObjectManifest(String container, String object, String manifest) throws FilesAuthorizationException, 
			HttpException, IOException, FilesInvalidNameException
			{
		      return updateObjectMetadataAndManifest(container, object, new HashMap<String, String>(), manifest);
			}

	/**
	 * @param container The container name
	 * @param object 
	 * @param metadata A map with the metadata as key names and values as the metadata values
	 * @return 
	 * @throws FilesAuthorizationException The client's login was invalid.
	 * @throws HttpException There was an error with the HTTP protocol
	 * @throws IOException 
	 * @throws FilesInvalidNameException 
     */
	public boolean updateObjectMetadata(String container, String object, 
			Map<String,String> metadata) throws FilesAuthorizationException, 
			HttpException, IOException, FilesInvalidNameException
			{
			    return updateObjectMetadataAndManifest(container, object, metadata, null);
			}

	/**
	 * @param container The container name
	 * @param object 
	 * @param metadata A map with the metadata as key names and values as the metadata values
	 * @param manifest Set manifest content here
	 * @return 
	 * @throws FilesAuthorizationException The client's login was invalid.
	 * @throws HttpException There was an error with the HTTP protocol
	 * @throws IOException 
	 * @throws FilesInvalidNameException 
     */
		public boolean updateObjectMetadataAndManifest(String container, String object, 
			Map<String,String> metadata, String manifest) throws FilesAuthorizationException, 
			HttpException, IOException, FilesInvalidNameException {
			FilesResponse response;
			
	    	if (!isLoggedin) {
	       		throw new FilesAuthorizationException("You must be logged in", 
	       			null, null);
	    	}
	    	if (!isValidContainerName(container))
	    		throw new FilesInvalidNameException(container);	
	    	if (!isValidObjectName(object))
				throw new FilesInvalidNameException(object);
	    	
	    	String postUrl = storageURL + "/"+FilesClient.sanitizeForURI(container) +
	    		"/"+FilesClient.sanitizeForURI(object);
	    	
	    	HttpPost method = null;
	    	try {
		    	method = new HttpPost(postUrl);
				if (manifest != null){
					method.setHeader(FilesConstants.MANIFEST_HEADER, manifest);
				}
		   		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
		   		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
		   		if (!(metadata == null || metadata.isEmpty())) {
		   			for(String key:metadata.keySet())
		   				method.setHeader(FilesConstants.X_OBJECT_META+key, 
		   					FilesClient.sanitizeForURI(metadata.get(key)));
		   		}
	    		HttpResponse resp = client.execute(method);
	    		response = new FilesResponse(resp);
	    		if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
	    			method.abort(); 
	    			if(login()) {
	    				method = new HttpPost(postUrl);
	    		   		method.getParams().setIntParameter("http.socket.timeout", connectionTimeOut);
	    		   		method.setHeader(FilesConstants.X_AUTH_TOKEN, authToken);
	    		   		if (!(metadata == null || metadata.isEmpty())) {
	    		   			for(String key:metadata.keySet())
	    		   				method.setHeader(FilesConstants.X_OBJECT_META+key, 
	    		   					FilesClient.sanitizeForURI(metadata.get(key)));
	    		   		}
	    	    		client.execute(method);
	    			}
	    		}
	    		
	    		return true;
	    	} finally {
	    		if (method != null) 
	    			method.abort();
	    	}
	    	
		}
		
		private String makeURI(String base, List<NameValuePair> parameters) {
			return base + "?" + URLEncodedUtils.format(parameters, CharEncoding.UTF_8);
		}
		
		/*
		 * 
		 *private void setQueryParameters(HttpRequestBase method, List<NameValuePair> parameters) throws FilesException{
			URI oldURI = method.getURI();
			try {
				URI newURI = URIUtils.createURI(oldURI.getScheme(), oldURI.getHost(), -1, 
						URLEncoder.encode(oldURI.getPath(), "UTF-8"), URLEncodedUtils.format(parameters, "UTF-8"), null);
				logger.warn("Old Path: " + oldURI.getPath());
				logger.warn("New URI: " + newURI);
				method.setURI(newURI);
			}
			catch (UnsupportedEncodingException uee) {
				logger.error("Somehow, we don't have UTF-8, this is quite a surprise", uee);
				throw new FilesException("Somehow, we don't have UTF-8, this is quite a surprise", uee);
			}
			catch (URISyntaxException use) {
				logger.error("Bad Syntax", use);
				throw new FilesException("Bad URL Syntax", use);
			}
		}
		*/
		public void useSnet(){
			if(snet){
			}
			else{
			snet = true;
			if(storageURL != null){
				storageURL = snetAddr + storageURL.substring(8);
			 }
			}
		}
		public void usePublic(){
			if(!snet){
			}
			else{
			snet = false;
			if(storageURL != null){
				storageURL = "https://" + storageURL.substring(snetAddr.length());
			}
			}
		}
		public boolean usingSnet(){
			return snet;
		}
		private boolean envSnet(){
			if (System.getenv("RACKSPACE_SERVICENET") == null) {
				return false;
			}
			else{
				snet = true;
				return true;
			}
		}
}
