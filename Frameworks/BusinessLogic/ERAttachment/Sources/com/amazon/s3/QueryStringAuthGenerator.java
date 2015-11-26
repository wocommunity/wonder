//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package com.amazon.s3;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class mimics the behavior of AWSAuthConnection, except instead of actually performing
 * the operation, QueryStringAuthGenerator will return URLs with query string parameters that
 * can be used to do the same thing.  These parameters include an expiration date, so that
 * if you hand them off to someone else, they will only work for a limited amount of time.
 */
public class QueryStringAuthGenerator {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private boolean isSecure;
    private String server;
    private int port;

    private Long expiresIn = null;
    private Long expires = null;

    // by default, expire in 1 minute.
    private static final Long DEFAULT_EXPIRES_IN = Long.valueOf(60 * 1000);

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey) {
        this(awsAccessKeyId, awsSecretAccessKey, true);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey,
                                    boolean isSecure)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, Utils.DEFAULT_HOST);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey,
                                    boolean isSecure, String server)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server,
             isSecure ? Utils.SECURE_PORT : Utils.INSECURE_PORT);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey,
                             boolean isSecure, String server, int port)
    {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.isSecure = isSecure;
        this.server = server;
        this.port = port;

        expiresIn = DEFAULT_EXPIRES_IN;
        expires = null;
    }

    public void setExpires(long millisSinceEpoch) {
        expires = Long.valueOf(millisSinceEpoch);
        expiresIn = null;
    }

    public void setExpiresIn(long millis) {
        expiresIn = Long.valueOf(millis);
        expires = null;
    }

    public String createBucket(String bucket, Map headers)
    {
        return generateURL("PUT", bucket, headers);
    }

    public String listBucket(String bucket, String prefix, String marker,
                                         Integer maxKeys, Map headers)
    {
        String path = Utils.pathForListOptions(bucket, prefix, marker, maxKeys);
        return generateURL("GET", path, headers);
    }

    public String deleteBucket(String bucket, Map headers)
    {
        return generateURL("DELETE", bucket, headers);
    }

    public String put(String bucket, String key, S3Object object, Map headers) {
        Map metadata = null;
        if (object != null) {
            metadata = object.metadata;
        }

        return generateURL("PUT", bucket + "/" + Utils.urlencode(key), mergeMeta(headers, metadata));
    }

    public String get(String bucket, String key, Map headers)
    {
        return generateURL("GET", bucket + "/" + Utils.urlencode(key), headers);
    }

    public String delete(String bucket, String key, Map headers)
    {
        return generateURL("DELETE", bucket + "/" + Utils.urlencode(key), headers);
    }

    public String getBucketACL(String bucket, Map headers) {
        return getACL(bucket, "", headers);
    }

    public String getACL(String bucket, String key, Map headers)
    {
        return generateURL("GET", bucket + "/" + Utils.urlencode(key) + "?acl", headers);
    }

    public String putBucketACL(String bucket, String aclXMLDoc, Map headers) {
        return putACL(bucket, "", aclXMLDoc, headers);
    }

    public String putACL(String bucket, String key, String aclXMLDoc, Map headers)
    {
        return generateURL("PUT", bucket + "/" + Utils.urlencode(key) + "?acl", headers);
    }

    public String listAllMyBuckets(Map headers)
    {
        return generateURL("GET", "", headers);
    }

    public String makeBareURL(String bucket, String key) {
        StringBuilder buffer = new StringBuilder();
        if (isSecure) {
            buffer.append("https://");
        } else {
            buffer.append("http://");
        }
        buffer.append(server).append(':').append(port).append('/').append(bucket);
        buffer.append('/').append(Utils.urlencode(key));

        return buffer.toString();
    }

    private String generateURL(String method, String path, Map headers) {
        long expires = 0L;
        if (expiresIn != null) {
            expires = System.currentTimeMillis() + expiresIn.longValue();
        } else if (this.expires != null) {
            expires = this.expires.longValue();
        } else {
            throw new RuntimeException("Illegal expires state");
        }

        // convert to seconds
        expires /= 1000;

        String canonicalString = Utils.makeCanonicalString(method, path, headers, ""+expires);
        String encodedCanonical = Utils.encode(awsSecretAccessKey, canonicalString, true);

        StringBuilder buffer = new StringBuilder();
        if (isSecure) {
            buffer.append("https://");
        } else {
            buffer.append("http://");
        }

        buffer.append(server).append(':').append(port).append('/').append(path);

        if (path.indexOf('?') == -1) {
            // no other query parameters
            buffer.append('?');
        } else {
            // there exist other query parameters
            buffer.append('&');
        }

        buffer.append("Signature=").append(encodedCanonical);
        buffer.append("&Expires=").append(expires);
        buffer.append("&AWSAccessKeyId=").append(awsAccessKeyId);

        return buffer.toString();
    }

    private Map mergeMeta(Map headers, Map metadata) {
        Map merged = new TreeMap();
        if (headers != null) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                merged.put(key, headers.get(key));
            }
        }
        if (metadata != null) {
            for (Iterator i = metadata.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                String metadataKey = Utils.METADATA_PREFIX + key;
                if (merged.containsKey(metadataKey)) {
                    ((List)merged.get(metadataKey)).addAll((List)metadata.get(key));
                } else {
                    merged.put(metadataKey, metadata.get(key));
                }
            }
        }
        return merged;
    }
}
