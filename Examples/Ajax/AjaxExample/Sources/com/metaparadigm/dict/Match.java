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

public class Match
{
    private String word;
    private String database;

    public String getDatabase() { return database; }
    public String getWord() { return word; }

    public Match(String database, String word)
    {
	this.database = database;
	this.word = word;
    }

    public String toString()
    {
	return "match: " + word + " \"" + database + "\"";
    }

}
