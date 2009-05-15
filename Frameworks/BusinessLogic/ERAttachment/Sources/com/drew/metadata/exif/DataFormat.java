/**
 * Created by IntelliJ IDEA.
 * User: dnoakes
 * Date: 04-Nov-2003
 * Time: 00:24:21
 * To change this template use Options | File Templates.
 */
package com.drew.metadata.exif;

import com.drew.metadata.MetadataException;

public class DataFormat
{
    public static final DataFormat BYTE = new DataFormat("BYTE", 1);
    public static final DataFormat STRING = new DataFormat("STRING", 2);
    public static final DataFormat USHORT = new DataFormat("USHORT", 3);
    public static final DataFormat ULONG = new DataFormat("ULONG", 4);
    public static final DataFormat URATIONAL = new DataFormat("URATIONAL", 5);
    public static final DataFormat SBYTE = new DataFormat("SBYTE", 6);
    public static final DataFormat UNDEFINED = new DataFormat("UNDEFINED", 7);
    public static final DataFormat SSHORT = new DataFormat("SSHORT", 8);
    public static final DataFormat SLONG = new DataFormat("SLONG", 9);
    public static final DataFormat SRATIONAL = new DataFormat("SRATIONAL", 10);
    public static final DataFormat SINGLE = new DataFormat("SINGLE", 11);
    public static final DataFormat DOUBLE = new DataFormat("DOUBLE", 12);

    private final String myName;
    private final int value;

    public static DataFormat fromValue(int value) throws MetadataException
    {
        switch (value)
        {
            case 1:  return BYTE;
            case 2:  return STRING;
            case 3:  return USHORT;
            case 4:  return ULONG;
            case 5:  return URATIONAL;
            case 6:  return SBYTE;
            case 7:  return UNDEFINED;
            case 8:  return SSHORT;
            case 9:  return SLONG;
            case 10: return SRATIONAL;
            case 11: return SINGLE;
            case 12: return DOUBLE;
        }

        throw new MetadataException("value '"+value+"' does not represent a known data format.");
    }

    private DataFormat(String name, int value)
    {
        myName = name;
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return myName;
    }
}
