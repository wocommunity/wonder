//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package com.amazon.s3;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Utils {
    static final String METADATA_PREFIX = "x-amz-meta-";
    static final String AMAZON_HEADER_PREFIX = "x-amz-";
    static final String ALTERNATIVE_DATE_HEADER = "x-amz-date";
    static final String DEFAULT_HOST = "s3.amazonaws.com";
    static final int SECURE_PORT = 443;
    static final int INSECURE_PORT = 80;


    /**
     * HMAC/SHA1 Algorithm per RFC 2104.
     */
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    static String makeCanonicalString(String method, String resource, Map headers) {
        return makeCanonicalString(method, resource, headers, null);
    }

    /**
     * Calculate the canonical string.  When expires is non-null, it will be
     * used instead of the Date header.
     * @param method 
     * @param resource 
     * @param headers 
     * @param expires 
     * @return canoncical string
     */
    static String makeCanonicalString(String method, String resource,
                                             Map headers, String expires)
    {
    	StringBuilder buf = new StringBuilder();
        buf.append(method + "\n");

        // Add all interesting headers to a list, then sort them.  "Interesting"
        // is defined as Content-MD5, Content-Type, Date, and x-amz-
        SortedMap interestingHeaders = new TreeMap();
        if (headers != null) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                if (key == null) continue;
                String lk = key.toLowerCase();

                // Ignore any headers that are not particularly interesting.
                if (lk.equals("content-type") || lk.equals("content-md5") || lk.equals("date") ||
                    lk.startsWith(AMAZON_HEADER_PREFIX))
                {
                    List s = (List)headers.get(key);
                    interestingHeaders.put(lk, concatenateList(s));
                }
            }
        }

        if (interestingHeaders.containsKey(ALTERNATIVE_DATE_HEADER)) {
            interestingHeaders.put("date", "");
        }

        // if the expires is non-null, use that for the date field.  this
        // trumps the x-amz-date behavior.
        if (expires != null) {
            interestingHeaders.put("date", expires);
        }

        // these headers require that we still put a new line in after them,
        // even if they don't exist.
        if (! interestingHeaders.containsKey("content-type")) {
            interestingHeaders.put("content-type", "");
        }
        if (! interestingHeaders.containsKey("content-md5")) {
            interestingHeaders.put("content-md5", "");
        }

        // Finally, add all the interesting headers (i.e.: all that startwith x-amz- ;-))
        for (Iterator i = interestingHeaders.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (key.startsWith(AMAZON_HEADER_PREFIX)) {
                buf.append(key).append(':').append(interestingHeaders.get(key));
            } else {
                buf.append(interestingHeaders.get(key));
            }
            buf.append('\n');
        }

        // don't include the query parameters...
        int queryIndex = resource.indexOf('?');
        if (queryIndex == -1) {
            buf.append("/" + resource);
        } else {
            buf.append("/" + resource.substring(0, queryIndex));
        }

        // ...unless there is an acl or torrent parameter
        if (resource.matches(".*[&?]acl($|=|&).*")) {
            buf.append("?acl");
        } else if (resource.matches(".*[&?]torrent($|=|&).*")) {
            buf.append("?torrent");
        }

        return buf.toString();
    }

    /**
     * Calculate the HMAC/SHA1 on a string.
     * @param awsSecretAccessKey passcode to sign with
     * @param canonicalString string to sign
     * @param urlencode <code>true</code> to urlencode the result
     * @return Signature
     */
    static String encode(String awsSecretAccessKey, String canonicalString,
                                boolean urlencode)
    {
        // The following HMAC/SHA1 code for the signature is taken from the
        // AWS Platform's implementation of RFC2104 (amazon.webservices.common.Signature)
        //
        // Acquire an HMAC/SHA1 from the raw key bytes.
        SecretKeySpec signingKey =
            new SecretKeySpec(awsSecretAccessKey.getBytes(), HMAC_SHA1_ALGORITHM);

        // Acquire the MAC instance and initialize with the signing key.
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            throw new RuntimeException("Could not find sha1 algorithm", e);
        }
        try {
            mac.init(signingKey);
        } catch (InvalidKeyException e) {
            // also should not happen
            throw new RuntimeException("Could not initialize the MAC algorithm", e);
        }

        // Compute the HMAC on the digest, and set it.
        String b64 = Base64.encodeBase64String(mac.doFinal(canonicalString.getBytes()));

        if (urlencode) {
            return urlencode(b64);
        } else {
            return b64;
        }
    }

    static String pathForListOptions(String bucket, String prefix, String marker, Integer maxKeys) {
    	StringBuilder path = new StringBuilder(bucket);
        path.append('?');

        // these two params must be url encoded
        if (prefix != null) path.append("prefix=" + urlencode(prefix) + "&");
        if (marker != null) path.append("marker=" + urlencode(marker) + "&");

        if (maxKeys != null) path.append("max-keys=" + maxKeys + "&");
        path.deleteCharAt(path.length()-1);     // we've always added exactly one too many chars

        return path.toString();
    }

    static String urlencode(String unencoded) {
        try {
            return URLEncoder.encode(unencoded, CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException("Could not url encode to UTF-8", e);
        }
    }

    static XMLReader createXMLReader() {
        try {
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // oops, lets try doing this (needed in 1.4)
            System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
        }
        try {
            // try once more
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Couldn't initialize a sax driver for the XMLReader");
        }
    }

    /**
     * Concatenates a bunch of header values, seperating them with a comma.
     * @param values List of header values.
     * @return String of all headers, with commas.
     */
    private static String concatenateList(List values) {
    	StringBuilder buf = new StringBuilder();
        for (int i = 0, size = values.size(); i < size; ++ i) {
            buf.append(((String)values.get(i)).replaceAll("\n", "").trim());
            if (i != (size - 1)) {
                buf.append(',');
            }
        }
        return buf.toString();
    }
}
