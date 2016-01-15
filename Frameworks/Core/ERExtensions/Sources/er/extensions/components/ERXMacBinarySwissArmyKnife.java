/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* MacBinarySwissArmyKnife.java created by travis on Wed 20-Sep-2000 */
package er.extensions.components;

import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSRange;

/* This draws liberally from code by Gregory L. Guerin:

/*
 ** Copyright 1998, 1999 by Gregory L. Guerin.
 ** Terms of use:
 **  - Brief terms: OPEN SOURCE... credit fairly, use freely, consider hiring me.
 **  - Complete terms: <http://www.amug.org/~glguerin/sw/index.html>
 */

/**
 * Useful for extracting files from binhexed files, ie when a Mac user uploads a file.
 */

public class ERXMacBinarySwissArmyKnife {
    /**
    ** Offsets in header where the fields are located.
     */
    public static final int
        ZERO_1_AT = 0,
        NAME_LEN_AT = 1,
        NAME_BYTES_AT = 2,
        FILE_TYPE_AT = 65,
        FILE_CREATOR_AT = 69,
        FINDER_FLAGS1_AT = 73,
        ZERO_2_AT = 74,
        FINDER_VERT_AT = 75,
        FINDER_HORZ_AT = 77,
        FINDER_WINDOW_AT = 79,
        FLAG_LOCKED_AT = 81,
        ZERO_3_AT = 82,
        LEN_DATA_FORK_AT = 83,
        LEN_RES_FORK_AT = 87,
        WHEN_CREATED_AT = 91,
        WHEN_MODIFIED_AT = 95,
        LEN_COMMENT_AT = 99,
        FINDER_FLAGS2_AT = 101,
        MB3_SIGNATURE_AT = 102,	// 'mBIN'
        NAME_SCRIPT_AT = 106,
        FINDER_FLAGS3_AT = 107,		// 1-byte
        LEN_SECONDARY_AT = 120,
        VERSION_TARGET_AT = 122,
        VERSION_MIN_AT = 123,
        CRC_AT = 124,
        VERSION_OBSOLETE_AT = 126,
        _AT = 0;

    /**
        ** Possible values in VERSION_TARGET_AT and VERSION_MIN_AT byte-sized fields.
     */
    public static final int
        MB1_VERSION = 128,		// MacBinary I
        MB2_VERSION = 129,		// MacBinary II
        MB3_VERSION = 130;		// MacBinary III

    /**
        ** Distinctive int-sized value in MB3_SIGNATURE_AT field, only present
     ** for MB-3 format.
     */
    public static final int
        MB3_SIGNATURE = 0x6D42494E;		// 'mBIN'


    /**
        ** Bit-masks for different levels.
     ** The mask at 0 clears only the "do not restore" flags, but none of the "reserved" bits.
     ** "Reserved" bits are cleared to zero in the other masks -- you may not want 
     */
    protected static final int[] levelMasks =
    {
        0x00FFFE7F,
        0x0000FC00,		// MacBinary-1
        0x0000FC4E,		// MacBinary-2
        0x0000FC4E,		// MacBinary-3
    };

    /**
        ** Length of a MacBinary header, always the first component of the file,
     ** and the only required element.  A MacBinary file must be at least this length.
     */
    public static final int MACBINARY_HEADER_LEN = 128;

    /**
        ** Enforce this limit on data-fork length, due either to Mac OS API or field-width.
     */
    public static final long LIMIT_DATAFORK = Integer.MAX_VALUE;

    /**
        ** Enforce this limit on resource-fork length, due either to Mac OS API or field-width.
     */
    public static final int LIMIT_RESFORK = (16 * 1024 * 1024) - 1;

    /**
        ** Enforce this limit on name-length, regardless of available space in header.
     */
    public static final int LIMIT_NAME = 31;

    private byte[] myBytes;
    private static final byte[] noBytes = new byte[ 0 ];

    protected static boolean strict = false;


    public ERXMacBinarySwissArmyKnife() {
        super();
    }

    public ERXMacBinarySwissArmyKnife(NSData fileData) {
        this();
        setByteArray(fileData.bytes(0,fileData.length()));
    }

    /**
        ** The array's index is the negative of the result-code.
     ** The result-code meanings are described at MacBinaryHeader.checkFormat().
     **
     ** @see MacBinaryHeader#checkFormat
     */
    private static String[] errorText =
    {
        "Zero-length name",
        "Invalid fork-lengths",
        "Non-zero byte at 74",
        "Non-zero bytes at 0 and/or 82"
    };

    /**
        ** Return a brief text String explaining a format or result-code.
     */
    private static String formatExplained( int format ) {
        if ( format > 0 )
            return ( "MacBinary-" + format );
        return ( errorText[ -format ] );
    }

    public boolean isMacBinary(NSData fileData) {
        try {
            // Though we are lax in accepting the file-header, we are strict in displaying
            // the format-number.  This lets us spy into files to discover their true internal form.

            int dataLength;
            if (fileData.length() >= MACBINARY_HEADER_LEN) {
                dataLength = MACBINARY_HEADER_LEN;
            } else {
                //System.out.println("The data is too short to be a MacBinary.  Only received " + fileData.length() + "bytes.");
                return false;
            }

            // Feed bytes into a MacBinaryHeader and see what turns up...
            myBytes = fileData.bytes(0,dataLength);

            int fileFormat = checkFormat( strict );

            if ( fileFormat <= 0 ) {
                // System.out.println( "  ## Not MacBinary: " + formatExplained( fileFormat ) );
                return false;
            }

        } catch ( Throwable why ){
            // System.out.println( "### Problem with file: " );
            // why.printStackTrace( System.out );
            return false;
        }

        return true;
    }

    public NSData unwrapMacBinary(NSData fileContents) {
        int dataForkLength = getDataForkLength();
        return  fileContents.subdataWithRange( new NSRange( MACBINARY_HEADER_LEN,dataForkLength ) );
    }


    public int checkFormat( boolean strictFormat ) {
        // Check for absolute minimal conformance to MacBinary header form...
        int a = getUByteAt( ZERO_1_AT );
        int b = getUByteAt( ZERO_3_AT );
        if ( a != 0  ||  b != 0 )
            return ( -3 );

        // Check for more strict conformance to MacBinary header form...
        if ( strictFormat  &&  getUByteAt( ZERO_2_AT ) != 0   )
            return ( -2 );

        // Check for "safe" values known to exist in all MacBinary headers.
        // This is only done after the minimal conformance checks above.
        a = getDataForkLength();
        b = getResourceForkLength();
        if ( a < 0  ||  a > LIMIT_DATAFORK  ||  b < 0  ||  b > LIMIT_RESFORK )
            return ( -1 );

        // Check for usable name-len... Note that this check makes names with
        // length [32-63] illegal, even though they are legal on non-HFS (400K) diskettes.
        // This seemed safe enough, considering that names of that actual length
        // are very unlikely to appear in anything of recent vintage.
        // If need to decode something that old, change it here.
        a = getUByteAt( NAME_LEN_AT );
        if ( a < 1  ||  a > LIMIT_NAME )
            return ( 0 );

        // Getting here, it's now known to be at least MacBinary I format.

        // Check for MacBinary II features...
        if ( ! isValidCRC()  ||  getUByteAt( VERSION_MIN_AT ) < MB2_VERSION )
            return ( 1 );

        // now known to be at least MacBinary II format.

        // Check for MacBinary III features...
        a = getIntAt( MB3_SIGNATURE_AT );
        if ( a != MB3_SIGNATURE )
            return ( 2 );

        // now known to be at least MacBinary III format.

        return ( 3 );

    }

    /**
        ** Return the signed byte at the given offset.
     */
    public byte getByteAt( int offset ) { return ( myBytes[ offset ] ); }

    /**
        ** Return an int holding the unsigned byte at the given offset.
     */
    public int
        getUByteAt( int offset )
    {
            return ( 0x00FF & myBytes[ offset ] );
    }

    /**
        ** Return the signed short at the given offset.
     */
    public short
        getShortAt( int offset )
    {
            return ( (short) ( (myBytes[ offset ] << 8) + (0x00FF & myBytes[ offset + 1 ]) ) );
    }

    /**
        ** Return an int holding the unsigned short at the given offset.
     */
    public int
        getUShortAt( int offset )
    {
            return ( (0xFF00 & (myBytes[ offset ] << 8)) + (0x00FF & myBytes[ offset + 1 ]) );
    }

    /**
        ** Return the signed int at the given offset.
     */
    public int
        getIntAt( int offset )
    {
            int value = (0x0FF & myBytes[ offset ]) << 24;
            value += (0x0FF & myBytes[ offset + 1 ]) << 16;
            value += (0x0FF & myBytes[ offset + 2 ]) << 8;
            return ( value + (0x0FF & myBytes[ offset + 3 ]) );
    }

    /**
        ** Return a long holding the unsigned int at the given offset.
     */
    public long
        getUIntAt( int offset )
    {
            long value = 0xFFFFFFFFL & getIntAt( offset );
            return ( value );
    }

    /**
        ** Return the signed long at the given offset.
     */
    public long
        getLongAt( int offset )
    {
            long value = getIntAt( offset );
            return ( (value << 32) + getUIntAt( offset + 4 ) );
    }



    /**
        ** Put the given byte at the supplied offset.
     */
    public void
        putByteAt( byte value, int offset )
    {
            myBytes[ offset ] = value;
    }

    /**
        ** Put all the given bytes at the supplied offset.
     */
    public void
        putBytesAt( byte[] data, int offset )
    {
            System.arraycopy( data, 0, myBytes, offset, data.length );
    }

    /**
        ** Put the given bytes at the supplied offset.
     */
    public void
        putBytesAt( byte[] data, int offset, int count )
    {
            System.arraycopy( data, 0, myBytes, offset, count );
    }

    /**
        ** Put the given short at the supplied offset.
     */
    public void
        putShortAt( short value, int offset )
    {
            myBytes[ offset ] = (byte) (value >> 8);
            myBytes[ offset + 1 ] = (byte) (value);
    }

    /**
        ** Put the given int at the supplied offset.
     */
    public void
        putIntAt( int value, int offset )
    {
            myBytes[ offset ] = (byte) (value >> 24);
            myBytes[ offset + 1 ] = (byte) (value >> 16);
            myBytes[ offset + 2 ] = (byte) (value >> 8);
            myBytes[ offset + 3 ] = (byte) (value);
    }

    /**
        ** Put the given long at the supplied offset.
     */
    public void
        putLongAt( long value, int offset )
    {
            putIntAt( (int) (value >> 32), offset );
            putIntAt( (int) (value), offset + 4 );
    }



    /**
        ** Get the internal byte-array.
     */
    public final byte[] getByteArray() {  return ( myBytes );  }

    /**
        ** Use the given array to hold bytes.
     ** Accepts null and/or zero-length arrays without error,
     ** though a subsequent get or put will throw an exception.
     */
    public final void setByteArray( byte[] bytes ) {  myBytes = bytes;  }

    /**
        ** Get the data-fork length from the header.
     */
    public int getDataForkLength() {  return ( getIntAt( LEN_DATA_FORK_AT ) );  }

    /**
        ** Get the resource-fork length from the header.
     */
    public int getResourceForkLength() { return ( getIntAt( LEN_RES_FORK_AT ) ); }

    // ###  C R C  ###

    /**
        ** Calculate a MacBinary CRC using the given starting seed, and proceeding over the given
     ** range of bytes.  The returned CRC value is a 16-bit result in an int, because it's unsigned.
     */
    public static int calculateCRC( int seed, byte[] bytes, int offset, int count ) {
        for ( count += offset;  offset < count;  ++offset ) {
            seed ^= (0xFF & bytes[ offset ]) << 8;
            seed = (seed << 8) ^ crcTable[ 0xFF & (seed >> 8) ];
        }
        return ( 0xFFFF & seed );
    }


    /**
        ** Conveniently, UniCode chars are unsigned 16-bit values.
     ** Equally convenient, it's very easy to create constant String objects,
     ** then retrieve individual chars from them later.
     ** In particular, the String-constant rendering of the code consumes vastly fewer byte-codes
     ** than an ordinary array initializer would.  And we still get a char[] out of it.
     */
    private static final char[] crcTable =
        (
         "\u0000\u1021\u2042\u3063\u4084\u50a5\u60c6\u70e7" +
         "\u8108\u9129\ua14a\ub16b\uc18c\ud1ad\ue1ce\uf1ef" +
         "\u1231\u0210\u3273\u2252\u52b5\u4294\u72f7\u62d6" +
         "\u9339\u8318\ub37b\ua35a\ud3bd\uc39c\uf3ff\ue3de" +
         "\u2462\u3443\u0420\u1401\u64e6\u74c7\u44a4\u5485" +
         "\ua56a\ub54b\u8528\u9509\ue5ee\uf5cf\uc5ac\ud58d" +
         "\u3653\u2672\u1611\u0630\u76d7\u66f6\u5695\u46b4" +
         "\ub75b\ua77a\u9719\u8738\uf7df\ue7fe\ud79d\uc7bc" +
         "\u48c4\u58e5\u6886\u78a7\u0840\u1861\u2802\u3823" +
         "\uc9cc\ud9ed\ue98e\uf9af\u8948\u9969\ua90a\ub92b" +
         "\u5af5\u4ad4\u7ab7\u6a96\u1a71\u0a50\u3a33\u2a12" +
         "\udbfd\ucbdc\ufbbf\ueb9e\u9b79\u8b58\ubb3b\uab1a" +
         "\u6ca6\u7c87\u4ce4\u5cc5\u2c22\u3c03\u0c60\u1c41" +
         "\uedae\ufd8f\ucdec\uddcd\uad2a\ubd0b\u8d68\u9d49" +
         "\u7e97\u6eb6\u5ed5\u4ef4\u3e13\u2e32\u1e51\u0e70" +
         "\uff9f\uefbe\udfdd\ucffc\ubf1b\uaf3a\u9f59\u8f78" +
         "\u9188\u81a9\ub1ca\ua1eb\ud10c\uc12d\uf14e\ue16f" +
         "\u1080\u00a1\u30c2\u20e3\u5004\u4025\u7046\u6067" +
         "\u83b9\u9398\ua3fb\ub3da\uc33d\ud31c\ue37f\uf35e" +
         "\u02b1\u1290\u22f3\u32d2\u4235\u5214\u6277\u7256" +
         "\ub5ea\ua5cb\u95a8\u8589\uf56e\ue54f\ud52c\uc50d" +
         "\u34e2\u24c3\u14a0\u0481\u7466\u6447\u5424\u4405" +
         "\ua7db\ub7fa\u8799\u97b8\ue75f\uf77e\uc71d\ud73c" +
         "\u26d3\u36f2\u0691\u16b0\u6657\u7676\u4615\u5634" +
         "\ud94c\uc96d\uf90e\ue92f\u99c8\u89e9\ub98a\ua9ab" +
         "\u5844\u4865\u7806\u6827\u18c0\u08e1\u3882\u28a3" +
         "\ucb7d\udb5c\ueb3f\ufb1e\u8bf9\u9bd8\uabbb\ubb9a" +
         "\u4a75\u5a54\u6a37\u7a16\u0af1\u1ad0\u2ab3\u3a92" +
         "\ufd2e\ued0f\udd6c\ucd4d\ubdaa\uad8b\u9de8\u8dc9" +
         "\u7c26\u6c07\u5c64\u4c45\u3ca2\u2c83\u1ce0\u0cc1" +
         "\uef1f\uff3e\ucf5d\udf7c\uaf9b\ubfba\u8fd9\u9ff8" +
         "\u6e17\u7e36\u4e55\u5e74\u2e93\u3eb2\u0ed1\u1ef0"
         ).toCharArray();


    /**
        ** Calculate and set the CRC over the header bytes previously set,
     ** invoking calcCRC() to calculate the 16-bit value to store.
     ** You should only invoke this after setting every other field of interest,
     ** such as name and name-encoding, data-fork len, res-fork len, secondary-header len, etc.
     ** Since the CRC is calculated internally, there is no parameter to this method,
     ** even though it's a "setter" method.
     **<p>
     ** In most cases, you will find finishHeader() more useful since it finishes
     ** all the assembly for a particular header-format you select.
     **
     */
    public void setCRC() {  putShortAt( (short) calcCRC(), CRC_AT );  }

    /**
        ** Calculate and check the header CRC.
     */
    public boolean isValidCRC() {  return ( calcCRC() == getCRC() );  }

    /**
        ** Calculate and return a CRC over the header.
     ** The CRC value is in the low 16-bits of the returned int.
     */
    public int calcCRC() {  return ( calculateCRC( 0, getByteArray(), 0, CRC_AT ) );  }

    /**
        ** Calculate and return an alternative header CRC.
     ** Where calcCRC() calculates over the first 124 bytes of the header,
     ** this calculates over the first 126 bytes of the header.
     ** I'm guessing that with a valid CRC-value in the header, the returned value
     ** is zero, but I don't know enough about CRC theory and practice to assert that confidently.
     ** This is more of a Greg's-hacky-toy than anything that might actually be useful.
     */
    public int calcCRC2() { return ( calculateCRC( 0, getByteArray(), 0, CRC_AT + 2 ) ); }

    /**
        ** Return the header CRC, as embedded in the header itself.
     */
    public int getCRC() { return ( getUShortAt( CRC_AT ) ); }
}
