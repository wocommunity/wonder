/**
 * Copyright (c) 2008 Greg Whalin
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the BSD license
 *
 * This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * You should have received a copy of the BSD License along with this
 * library.
 *
 * @author Kevin Burton
 * @author greg whalin <greg@meetup.com> 
 */
package com.meetup.memcached.test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meetup.memcached.MemcachedClient;
import com.meetup.memcached.SockIOPool;

public class UnitTests {
    
    // logger
    private static Logger log =
        Logger.getLogger( UnitTests.class.getName() );

    public static MemcachedClient mc  = null;

    public static void test1() {
        mc.set( "foo", Boolean.TRUE );
        Boolean b = (Boolean)mc.get( "foo" );
        assertTrue(b.booleanValue());
        log.debug( "+ store/retrieve Boolean type test passed" );
    }

    public static void test2() {
        mc.set( "foo", Integer.valueOf( Integer.MAX_VALUE ) );
        Integer i = (Integer)mc.get( "foo" );
        assertTrue(i.intValue() == Integer.MAX_VALUE);
        log.debug( "+ store/retrieve Integer type test passed" );
    }

    public static void test3() {
        String input = "test of string encoding";
        mc.set( "foo", input );
        String s = (String)mc.get( "foo" );
        assertTrue(s.equals( input ));
        log.debug( "+ store/retrieve String type test passed" );
    }
    
    public static void test4() {
        mc.set( "foo", new Character( 'z' ) );
        Character c = (Character)mc.get( "foo" );
        assertTrue(c.charValue() == 'z');
        log.debug( "+ store/retrieve Character type test passed" );
    }

    public static void test5() {
        mc.set( "foo", new Byte( (byte)127 ) );
        Byte b = (Byte)mc.get( "foo" );
        assertTrue(b.byteValue() == 127);
        log.debug( "+ store/retrieve Byte type test passed" );
    }

    public static void test6() {
        mc.set( "foo", new StringBuffer( "hello" ) );
        StringBuffer o = (StringBuffer)mc.get( "foo" );
        assertTrue(o.toString().equals( "hello" ));
        log.debug( "+ store/retrieve StringBuffer type test passed" );
    }

    public static void test7() {
        mc.set( "foo", Short.valueOf( (short)100 ) );
        Short o = (Short)mc.get( "foo" );
        assertTrue(o.shortValue() == 100);
        log.debug( "+ store/retrieve Short type test passed" );
    }

    public static void test8() {
        mc.set( "foo", Long.valueOf( Long.MAX_VALUE ) );
        Long o = (Long)mc.get( "foo" );
        assertTrue(o.longValue() == Long.MAX_VALUE);
        log.debug( "+ store/retrieve Long type test passed" );
    }

    public static void test9() {
        mc.set( "foo", Double.valueOf( 1.1 ) );
        Double o = (Double)mc.get( "foo" );
        assertTrue(o.doubleValue() == 1.1);
        log.debug( "+ store/retrieve Double type test passed" );
    }

    public static void test10() {
        mc.set( "foo", Float.valueOf( 1.1f ) );
        Float o = (Float)mc.get( "foo" );
        assertTrue(o.floatValue() == 1.1f);
        log.debug( "+ store/retrieve Float type test passed" );
    }

    public static void test11() {
        mc.set( "foo", Integer.valueOf( 100 ), new Date( System.currentTimeMillis() ));
        try { Thread.sleep( 1000 ); } catch ( Exception ex ) { }
        assertTrue(mc.get( "foo" ) == null);
        log.debug( "+ store/retrieve w/ expiration test passed" );
    }

    public static void test12() {
        long i = 0;
        mc.storeCounter("foo", i);
        mc.incr("foo"); // foo now == 1
        mc.incr("foo", (long)5); // foo now == 6
        long j = mc.decr("foo", (long)2); // foo now == 4
        assertTrue(j == 4);
        assertTrue(j == mc.getCounter( "foo" ));
        log.debug( "+ incr/decr test passed" );
    }

    public static void test13() {
        Date d1 = new Date();
        mc.set("foo", d1);
        Date d2 = (Date) mc.get("foo");
        assertTrue(d1.equals( d2 ));
        log.debug( "+ store/retrieve Date type test passed" );
    }

    public static void test14() {
        assertTrue(!mc.keyExists( "foobar123" ));
        mc.set( "foobar123", Integer.valueOf( 100000) );
        assertTrue(mc.keyExists( "foobar123" ));
        log.debug( "+ store/retrieve test passed" );

        assertTrue(!mc.keyExists( "counterTest123" ));
        mc.storeCounter( "counterTest123", 0 );
        assertTrue(mc.keyExists( "counterTest123" ));
        log.debug( "+ counter store test passed" );
    }

    public static void test15() {

        Map stats = mc.statsItems();
        assertTrue(stats != null);

        stats = mc.statsSlabs();
        assertTrue(stats != null);

        log.debug( "+ stats test passed" );
    }

    public static void test16() {
        assertTrue(!mc.set( "foo", null ));
        log.debug( "+ invalid data store [null] test passed" );
    }
    
    public static void test17() {
        mc.set( "foo bar", Boolean.TRUE );
        Boolean b = (Boolean)mc.get( "foo bar" );
        assertTrue(b.booleanValue());
        log.debug( "+ store/retrieve Boolean type test passed" );
    }
    
    public static void test18() {
        mc.addOrIncr( "foo" ); // foo now == 0
        mc.incr( "foo" ); // foo now == 1
        mc.incr( "foo", (long)5 ); // foo now == 6

        mc.addOrIncr( "foo" ); // foo now 7

        long j = mc.decr( "foo", (long)3 ); // foo now == 4
        assertTrue(j == 4);
        assertTrue(j == mc.getCounter( "foo" ));

        log.debug( "+ incr/decr test passed" );
    }

    public static void test19() {
        int max = 100;
        String[] keys = new String[ max ];
        for ( int i=0; i<max; i++ ) {
            keys[i] = Integer.toString(i);
            mc.set( keys[i], "value"+i );
        }
        
        Map<String,Object> results = mc.getMulti( keys );
        for ( int i=0; i<max; i++ ) {
            assertTrue(results.get( keys[i]).equals( "value"+i ));
        }
        log.debug( "+ getMulti test passed" );
    }
    
    public static void test20( int max, int skip, int start ) {
        int numEntries = max/skip+1;
        log.debug( String.format( "test 20 starting with start=%5d skip=%5d max=%7d num=%d", start, skip, max, numEntries,  numEntries*start ) );
        String[] keys = new String[ numEntries ];
        byte[][] vals = new byte[ numEntries ][];
        
        int size = start;
        for ( int i=0; i<numEntries; i++ ) {
            keys[i] = Integer.toString( size );
            vals[i] = new byte[size + 1];
            for ( int j=0; j<size + 1; j++ )
                vals[i][j] = (byte)j;
            
            mc.set( keys[i], vals[i] );
            size += skip;
        }
        
        Map<String,Object> results = mc.getMulti( keys );
        for ( int i=0; i<numEntries; i++ ) {
            byte res[] =  (byte[])results.get(keys[i]);
            byte except[] =  vals[i];
            // mc.stats();
            assertTrue(Arrays.equals(res, except));
        }
        
        log.info(String.format( "test 20 finished with start=%5d skip=%5d max=%7d num=%d bytes=%d", start, skip, max, numEntries, numEntries * size ) );
    }

    public static void test21() {
        mc.set( "foo", new StringBuilder( "hello" ) );
        StringBuilder o = (StringBuilder)mc.get( "foo" );
        assertTrue(o.toString().equals( "hello" ));
        log.debug( "+ store/retrieve StringBuilder type test passed" );
    }

    public static void test22() {
        byte[] b = new byte[10];
        for ( int i = 0; i < 10; i++ )
            b[i] = (byte)i;

        mc.set( "foo", b );
        assertTrue(Arrays.equals( (byte[])mc.get( "foo" ), b ));
        log.debug( "+ store/retrieve byte[] type test passed" );
    }

    public static void test23() {
        TestClass tc = new TestClass( "foo", "bar", Integer.valueOf( 32 ) );
        mc.set( "foo", tc );
        assertTrue(tc.equals( (TestClass)mc.get( "foo" ) ));
        log.debug( "+ store/retrieve serialized object test passed" );
    }

    public static void test24() {

        String[] allKeys = { "key1", "key2", "key3", "key4", "key5", "key6", "key7" };
        String[] setKeys = { "key1", "key3", "key5", "key7" };

        for ( String key : setKeys ) {
            mc.set( key, key );
        }

        Map<String,Object> results = mc.getMulti( allKeys );

        assertTrue(allKeys.length == results.size());
        for ( String key : setKeys ) {
            String val = (String)results.get( key );
            assertTrue(key.equals( val ));
        }

        log.debug( "+ getMulti w/ keys that don't exist test passed" );
    }

    public static void runAlTests( MemcachedClient mc ) {
        test14();
        for ( int t = 0; t < 2; t++ ) {
            mc.setCompressEnable( ( t&1 ) == 1 );
            
            test1();
            test2();
            test3();
            test4();
            test5();
            test6();
            test7();
            test8();
            test9();
            test10();
            test11();
            test12();
            test13();
            test15();
            test16();
            test17();
            test21();
            test22();
            test23();
            test24();
            
            for ( int i = 0; i < 3; i++ )
                test19();
            
            test20( 8191, 1, 0 );
            test20( 8192, 1, 0 );
            test20( 8193, 1, 0 );
            
            test20( 16384, 100, 0 );
            test20( 17000, 128, 0 );
            
            test20( 128*1024, 1023, 0 );
            test20( 128*1024, 1023, 1 );
            test20( 128*1024, 1024, 0 );
            test20( 128*1024, 1024, 1 );
            
            test20( 128*1024, 1023, 0 );
            test20( 128*1024, 1023, 1 );
            test20( 128*1024, 1024, 0 );
            test20( 128*1024, 1024, 1 );
            
            test20( 900*1024, 32*1024, 0 );
            test20( 900*1024, 32*1024, 1 );
        }

    }

    private static void assertTrue(boolean v) {
        if(!v) throw new AssertionError();
    }
    
    /**
     * This runs through some simple tests of the MemcacheClient.
     *
     * Command line args:
     * args[0] = number of threads to spawn
     * args[1] = number of runs per thread
     * args[2] = size of object to store 
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //BasicConfigurator.configure();
        Logger.getLogger("com").setLevel( Level.WARN );
    	log.warn("starting tests");
    	long time = System.currentTimeMillis();
        try {
            //assertTrue(false);
            //System.err.println( "WARNING: assertions are disabled!" );
        } catch(AssertionError ex) {
        }
        if ( !UnitTests.class.desiredAssertionStatus() ) {
            try { Thread.sleep( 3000 ); } catch ( InterruptedException e ) {}
        }
        
        String[] serverlist = {
            "127.0.0.1:12345"
        };

        Integer[] weights = { 1, 1, 1, 1, 10, 5, 1, 1, 1, 3 };

        if ( args.length > 0 )
            serverlist = args;

        // initialize the pool for memcache servers
        SockIOPool pool = SockIOPool.getInstance( "test" );
        try {
        	pool.setServers( serverlist );
        	pool.setWeights( weights );
        	pool.setMaxConn( 250 );
        	pool.setNagle( false );
        	pool.setHashingAlg( SockIOPool.CONSISTENT_HASH );
        	pool.initialize();

        	mc = new MemcachedClient( "test" );
        	mc.flushAll();
        	runAlTests( mc );
        } finally {
        	pool.shutDown();
        }
        log.warn("Finished: " + ((System.currentTimeMillis() - time)/1000) + "s");
    }

    /** 
     * Class for testing serializing of objects. 
     * 
     * @author $Author: $
     * @version $Revision: $ $Date: $
     */
    public static final class TestClass implements Serializable {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        private String field1;
        private String field2;
        private Integer field3;

        public TestClass( String field1, String field2, Integer field3 ) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public String getField1() { return this.field1; }
        public String getField2() { return this.field2; }
        public Integer getField3() { return this.field3; }

        public boolean equals( Object o ) {
            if ( this == o ) return true;
            if ( !( o instanceof TestClass ) ) return false;

            TestClass obj = (TestClass)o;

            return ( ( this.field1 == obj.getField1() || ( this.field1 != null && this.field1.equals( obj.getField1() ) ) )
                    && ( this.field2 == obj.getField2() || ( this.field2 != null && this.field2.equals( obj.getField2() ) ) )
                    && ( this.field3 == obj.getField3() || ( this.field3 != null && this.field3.equals( obj.getField3() ) ) ) );
        }
    }
}
