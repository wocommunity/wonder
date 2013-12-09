package com.webobjects.jdbcadaptor;

public class FrontBaseTypes {
	public static final int FB_Boolean = 1;
	public static final int FB_Integer = 2;
	public static final int FB_SmallInteger = 3;
	public static final int FB_Float = 4;
	public static final int FB_Real = 5;
	public static final int FB_Double = 6;
	public static final int FB_Numeric = 7;
	public static final int FB_Decimal = 8;
	public static final int FB_Character = 9;
	public static final int FB_VCharacter = 10;
	public static final int FB_Bit = 11;
	public static final int FB_VBit = 12;
	public static final int FB_Date = 13;
	public static final int FB_Time = 14;
	public static final int FB_TimeTZ = 15;
	public static final int FB_Timestamp = 16;
	public static final int FB_TimestampTZ = 17;
	public static final int FB_YearMonth = 18;
	public static final int FB_DayTime = 19;
	public static final int FB_CLOB = 20;
	public static final int FB_BLOB = 21;
	public static final int FB_TinyInteger = 22;
	public static final int FB_LongInteger = 23;

	public FrontBaseTypes() {
		super();
	}

	public static int internalTypeForExternal(String externalType) {
		if (externalType.equals("BOOLEAN"))
			return FB_Boolean;
		else if (externalType.equals("INTEGER") || externalType.equals("INT"))
			return FB_Integer;
		else if (externalType.equals("SMALLINT"))
			return FB_SmallInteger;
		else if (externalType.equals("LONGINT"))
			return FB_LongInteger;
		else if (externalType.equals("TINYINT"))
			return FB_TinyInteger;
		else if (externalType.equals("FLOAT"))
			return FB_Float;
		else if (externalType.equals("REAL"))
			return FB_Real;
		else if (externalType.equals("DOUBLE PRECISION"))
			return FB_Double;
		else if (externalType.equals("NUMERIC"))
			return FB_Numeric;
		else if (externalType.equals("DECIMAL"))
			return FB_Decimal;
		else if (externalType.equals("CHAR") || externalType.equals("CHARACTER"))
			return FB_Character;
		else if (externalType.equals("VARCHAR") || externalType.equals("CHARACTER VARYING") || externalType.equals("CHAR VARYING"))
			return FB_VCharacter;
		else if (externalType.equals("BIT") || externalType.equals("BYTE"))
			return FB_Bit;
		else if (externalType.equals("BIT VARYING") || externalType.equals("BYTE VARYING"))
			return FB_VBit;
		else if (externalType.equals("INTERVAL"))
			return FB_DayTime;
		else if (externalType.equals("DATE"))
			return FB_Date;
		else if (externalType.equals("TIME"))
			return FB_Time;
		else if (externalType.equals("TIME WITH TIME ZONE"))
			return FB_TimeTZ;
		else if (externalType.equals("TIMESTAMP"))
			return FB_Timestamp;
		else if (externalType.equals("TIMESTAMP WITH TIME ZONE"))
			return FB_TimestampTZ;
		else if (externalType.equals("BLOB"))
			return FB_BLOB;
		else if (externalType.equals("CLOB"))
			return FB_CLOB;
		return -1;
	}
}
