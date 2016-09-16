/*
 * Simple Java Dict Client (RFC2229)
 *
 * $Id$
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.lang3.CharEncoding;

public class DictClient implements Serializable
{
    private final static long serialVersionUID = 1;

    private final static boolean debug = false;

    private static String DEFAULT_HOST = "localhost";
    private static int DEFAULT_PORT = 2628;
 
    private String host;
    private int port;

    private transient ArrayList<Strategy> strategies;
    private transient ArrayList<Database> databases;

    private transient String ident;
    private transient Socket sock;
    private transient PrintWriter out;
    private transient BufferedReader in;

    public DictClient()
    {
	host = DEFAULT_HOST;
	port = DEFAULT_PORT;
    }

    public DictClient(String host)
    {
	this.host = host;
	port = DEFAULT_PORT;
    }

    public DictClient(String host , int port)
    {
	this.host = host;
	this.port = port;
    }

    public void setHost(String host)
    {
	if(sock != null && !this.host.equals(host)) close();
	this.host = host;
    }

    public void setPort(int port)
    {
	if(sock != null && this.port != port) close();
	this.port = port;
    }

    private synchronized void connect()
	throws IOException, DictClientException
    {
	System.out.println("DictClient.connect: opening connection to "
			   + host + ":" + port);
        sock = new Socket(host, port);
        in = new BufferedReader
	    (new InputStreamReader(sock.getInputStream(), CharEncoding.UTF_8));
        out = new PrintWriter
	    (new OutputStreamWriter(sock.getOutputStream(), CharEncoding.UTF_8));
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.BANNER) {
	    close();
	    throw new DictClientException(r);
	}
	ident = r.msg;
	System.out.println("DictClient.connect: connected to "
			   + host + ":" + port + " ident=\"" + ident + "\"");
    }

    @Override
	public void finalize() { close(); }

    private synchronized String status()
	throws IOException, DictClientException
    {
	out.print("STATUS\n");
	out.flush();
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.STATUS)
	    throw new DictClientException(r);
	if(debug) System.out.println("DictClient.status: " + r.msg);
	return r.msg;
    }

    public synchronized String checkConnection()
	throws IOException, DictClientException
    {
	if(sock == null) {
	    connect();
	    return status();
	}
	try {
	    return status();
	} catch(Exception e) {
	    System.out.println("DictClient.status: Exception " + e);
	    close();
	    connect();
	    return status();
	}
    }

    public synchronized void close()
    {
	if(sock == null) return;

	try {
	    out.print("QUIT\n");
	    out.flush();
	    String line = in.readLine();
	    if(line != null /* EOF */ ) {
		DictCommandResult r = new DictCommandResult(line);
		if(r.code != DictCommandResult.CLOSING_CONNECTION) {
		    System.out.println("DictClient.close: Exception: " + r);
		}
	    }
	} catch (IOException e) {
	    System.out.println
		("DictClient.close: IOException while closing: " + e);
	} finally {
	    try { sock.close(); } catch (IOException e) {}
	    sock = null;
	    in = null;
	    out = null;
	    System.out.println("DictClient.close: connection closed");
	}
    }

    public synchronized ArrayList getDatabases()
	throws IOException, DictClientException
    {
	if(databases == null) fetchDatabases();
	return databases;
    }

    private synchronized void fetchDatabases()
	throws IOException, DictClientException
    {
	checkConnection();

	out.print("SHOW DATABASES\n");
	out.flush();
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.DATABASES_PRESENT)
	    throw new DictClientException(r);

	databases = new ArrayList<>();
	String line;
	while(true) {
	    line = in.readLine();
	    if(line.equals(".")) break;
	    String database = line.substring(0, line.indexOf(' '));
	    String description = line.substring(line.indexOf('"') + 1,
						line.lastIndexOf('"'));
	    databases.add(new Database(database, description));
	}

	r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.OKAY)
	    throw new DictClientException(r);
    }

    public synchronized  ArrayList getStrategies()
	throws IOException, DictClientException
    {
	if(strategies == null) fetchStrategies();
	return strategies;
    }

    private synchronized void fetchStrategies()
	throws IOException, DictClientException
    {
	checkConnection();

	out.print("SHOW STRATEGIES\n");
	out.flush();
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.STRATEGIES_PRESENT)
	    throw new DictClientException(r);

	strategies = new ArrayList<>();
	String line;
	while(true) {
	    line = in.readLine();
	    if(line.equals(".")) break;
	    String strategy = line.substring(0, line.indexOf(' '));
	    String description = line.substring(line.indexOf('"') + 1,
						line.lastIndexOf('"'));
	    strategies.add(new Strategy(strategy, description));
	}

	r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.OKAY)
	    throw new DictClientException(r);
    }

    public synchronized ArrayList matchWord(String db,
					    String strategy, String word)
	throws IOException, DictClientException
    {
	checkConnection();

	if(debug)
	    System.out.println("DictClient.matchWord(\"" + db + "\", \"" +
			       strategy + "\", \"" + word + "\")");

	ArrayList<Match> matches = new ArrayList<>();

	out.print("MATCH " + db + " " + strategy +
		  " \"" + word + "\"\n");
	out.flush();
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code == DictCommandResult.NO_MATCH) return matches;
	else if(r.code != DictCommandResult.MATCH_NUM_RECIEVED)
	    throw new DictClientException(r);

	while(true) {
	    String line = in.readLine();
	    if(line.equals(".")) break;
	    String rDb = line.substring(0, line.indexOf(' '));
	    String rWord = line.substring(line.indexOf('"') + 1,
					 line.lastIndexOf('"'));
	    matches.add(new Match(rDb, rWord));
	}
	r = new DictCommandResult(in.readLine());
	if(r.code != DictCommandResult.OKAY) { 
	throw new DictClientException(r);
  }
  return matches;
    }

    public synchronized ArrayList defineWord(String db, String word)
	throws IOException, DictClientException
    {
	checkConnection();

	if(debug)
	    System.out.println
		("DictClient.defineWord(\"" + db + "\", \"" + word + "\")");

	ArrayList<Definition> definitions = new ArrayList<>();

	out.print("DEFINE " + db + " \"" + word + "\"\n");
	out.flush();
	DictCommandResult r = new DictCommandResult(in.readLine());
	if(r.code == DictCommandResult.NO_MATCH) return definitions;
	else if(r.code != DictCommandResult.DEFINE_NUM_RECIEVED)
	    throw new DictClientException(r);

	while(true) {
	    r = new DictCommandResult(in.readLine());
	    if(r.code == DictCommandResult.OKAY) return definitions;

	    int qoff;
	    String line = r.msg;
	    String rWord = line.substring((qoff = line.indexOf('"') + 1),
					  (qoff = line.indexOf('"', qoff+1)));
	    String rDb = line.substring(qoff+2, line.indexOf(' ', qoff+2));
	    StringBuilder def = new StringBuilder();
	    while(true) {
		line = in.readLine();
		if(line.equals(".")) break;
		def.append(line);
		def.append("\n");
	    }
	    definitions.add(new Definition(rDb, rWord, def.toString()));
	}
    }

}
