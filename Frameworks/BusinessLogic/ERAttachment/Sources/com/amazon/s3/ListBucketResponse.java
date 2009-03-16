//  This software code is made available "AS IS" without warranties of any
//  kind.  You may copy, display, modify and redistribute the software
//  code either by itself or as incorporated into your code; provided that
//  you do not remove any proprietary notices.  Your use of this software
//  code is at your own risk and you waive any claim against Amazon
//  Digital Services, Inc. or its affiliates with respect to your use of
//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
//  affiliates.

package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;


/**
 * Returned by AWSAuthConnection.listBucket()
 */
public class ListBucketResponse extends Response {
    /**
     * A List of ListEntry objects representing the objects in the given bucket.  This will
     * be null if the request fails.
     */
    public List entries = null;

    public ListBucketResponse(HttpURLConnection connection) throws IOException {
        super(connection);
        if (connection.getResponseCode() < 400) {
            try {
                XMLReader xr = Utils.createXMLReader();
                ListBucketHandler handler = new ListBucketHandler();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);

                xr.parse(new InputSource(connection.getInputStream()));
                this.entries = handler.getEntries();
            } catch (SAXException e) {
                throw new RuntimeException("Unexpected error parsing ListBucket xml", e);
            }
        }
    }

    class ListBucketHandler extends DefaultHandler {

        private List entries = null;
        private ListEntry currEntry = null;
        private StringBuffer currText = null;
        private SimpleDateFormat iso8601Parser = null;

        public ListBucketHandler() {
            super();
            entries = new ArrayList();
            this.iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            this.iso8601Parser.setTimeZone(new SimpleTimeZone(0, "GMT"));
            this.currText = new StringBuffer();
        }

        public void startDocument() {
            // ignore
        }

        public void endDocument() {
            // ignore
        }

        public void startElement(String uri, String name, String qName, Attributes attrs) {
            if (name.equals("Contents")) {
                this.currEntry = new ListEntry();
            } else if (name.equals("Owner")) {
                this.currEntry.owner = new Owner();
            }
        }

        public void endElement(String uri, String name, String qName) {
            if (name.equals("Contents")) {
                this.entries.add(this.currEntry);
            } else if (name.equals("Key")) {
                this.currEntry.key = this.currText.toString();
            } else if (name.equals("LastModified")) {
                try {
                    this.currEntry.lastModified = this.iso8601Parser.parse(this.currText.toString());
                } catch (ParseException e) {
                    throw new RuntimeException("Unexpected date format in list bucket output", e);
                }
            } else if (name.equals("ETag")) {
                this.currEntry.eTag = this.currText.toString();
            } else if (name.equals("Size")) {
                this.currEntry.size = Long.parseLong(this.currText.toString());
            } else if (name.equals("ID")) {
                this.currEntry.owner.id = this.currText.toString();
            } else if (name.equals("DisplayName")) {
                this.currEntry.owner.displayName = this.currText.toString();
            } else if (name.equals("StorageClass")) {
                this.currEntry.storageClass = this.currText.toString();
            }
            this.currText = new StringBuffer();
        }

        public void characters(char ch[], int start, int length) {
            this.currText.append(ch, start, length);
        }

        public List getEntries() {
            return this.entries;
        }
    }
}

