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

public class Definition
{
    private String word;
    private String database;
    private String definition;

    public String getDatabase() { return database; }
    public String getWord() { return word; }
    public String getDefinition() { return definition; }

    public Definition(String database, String word, String definition)
    {
	this.database = database;
	this.word = word;
	this.definition = definition;
    }

    public String toString()
    {
	return "definition: " + word + " \"" + database + "\"\n" + definition;
    }

}
