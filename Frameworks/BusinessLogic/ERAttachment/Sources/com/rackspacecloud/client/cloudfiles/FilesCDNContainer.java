/**
 * 
 */
package com.rackspacecloud.client.cloudfiles;

/**
 * @author lvaughn
 *
 */
public class FilesCDNContainer {
	private boolean enabled;
	private String userAgentACL;
	private String referrerACL;
	private int ttl;
	private String cdnURL;
	private String sslURL = null;
	private String streamingURL = null;
	private String name;
	private boolean retainLogs;
	
	/**
	 * @return the retainLogs
	 */
	public boolean getRetainLogs() {
		return retainLogs;
	}

	/**
	 * @param retainLogs the retainLogs to set
	 */
	public void setRetainLogs(boolean retainLogs) {
		this.retainLogs = retainLogs;
	}

	public FilesCDNContainer() {
	}
	
	public FilesCDNContainer(String cdnURL) {
		this.cdnURL = cdnURL;
	}
	
	public FilesCDNContainer(String cdnURL, String sslUrl) {
		this.cdnURL = cdnURL;
		sslURL = sslUrl;
	}
	
	public FilesCDNContainer(String cdnURL, String sslUrl, String streamingUrl) {
		this.cdnURL = cdnURL;
		sslURL = sslUrl;
		streamingURL = streamingUrl;
	}
	
	public FilesCDNContainer(String name, boolean enabled, int ttl, boolean retainLogs) {
		this.enabled = enabled;
		this.ttl = ttl;
		this.name = name;
		this.retainLogs = retainLogs;
	}
	

	
	/**
	 * @return Is this container CDN enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * @return the userAgentACL
	 */
	public String getUserAgentACL() {
		return userAgentACL;
	}
	/**
	 * @param userAgentACL the userAgentACL to set
	 */
	public void setUserAgentACL(String userAgentACL) {
		this.userAgentACL = "".equals(userAgentACL) ? null : userAgentACL;
	}
	/**
	 * @return the refererACL
	 */
	public String getReferrerACL() {
		return referrerACL;
	}
	/**
	 * @param referrerACL the refererACL to set
	 */
	public void setReferrerACL(String referrerACL) {
		this.referrerACL = "".equals(referrerACL) ? null : referrerACL;
	}
	/**
	 * @return the ttl
	 */
	public int getTtl() {
		return ttl;
	}
	/**
	 * @param ttl the ttl to set
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	/**
	 * @return the cdnURL
	 */
	public String getCdnURL() {
		return cdnURL;
	}

	/**
	 * @param cdnURL the cdnURL to set
	 */
	public void setCdnURL(String cdnURL) {
		this.cdnURL = cdnURL;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The URL for accessing content in this container via the CDN
	 */
	public String getSSLURL() {
		return sslURL;
	}

	/**
	 * 
	 * @param sslURL the sslURL to set
	 */
	void setSSLURL(String sslURL) {
		this.sslURL = sslURL;
	}
	
	/**
	 * @return The  Streaming URL for accessing content in this container via the CDN
	 */
	public String getStreamingURL() {
		return streamingURL;
	}

	/**
	 * 
	 * @param streamingURL the streamingURL to set
	 */
	void setStreamingURL(String streamingURL) {
		this.streamingURL = streamingURL;
	}
}
