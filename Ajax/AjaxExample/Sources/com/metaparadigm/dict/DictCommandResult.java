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

public class DictCommandResult
{
    protected int code;
    protected String msg;

    public int getCode() { return code; }
    public String getMessage() { return msg; }

    public static int INTERNAL_SOCKET_EOF = 900;
    public static int INTERNAL_STATUS_PARSE_ERROR = 901;

    public static int DATABASES_PRESENT = 110;
    public static int STRATEGIES_PRESENT = 111;

    public static int DEFINE_NUM_RECIEVED = 150;
    public static int DEFINE_RESULT = 151;
    public static int MATCH_NUM_RECIEVED = 152;

    public static int STATUS = 210;
    public static int BANNER = 220;
    public static int OKAY = 250;
    public static int CLOSING_CONNECTION = 221;

    public static int TEMP_UNAVAILABLE = 420;

    public static int INVALID_DATABASE = 550;
    public static int INVALID_STRATEGY = 551;
    public static int NO_MATCH = 552;

    protected DictCommandResult(String s)
    {
	if(s == null) {
	    code = INTERNAL_SOCKET_EOF;
	    msg = "Connection closed";
	    return;
	}
	try {
	    code = Integer.parseInt(s.substring(0,3));
	    msg = s.substring(4, s.length());
	} catch (Exception e) {
	    code = INTERNAL_STATUS_PARSE_ERROR;
	    msg = "Can't parse status line";
	}
    }

    public String toString()
    {
	return "code=" + code + " msg=\"" + msg + "\"";
    }
}
