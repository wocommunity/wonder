//
// ERXNSPrintWriterLogger.java
// Project ERExtensions
//
// Created by Tatsuya Kawano on Wed Aug 21 2002
// Updated by Masahiro Urakami on Wed Jan 29 2003 
//   so that it works properly on Windows as well. 
//
package er.extensions.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import com.webobjects.foundation.NSLog;

/**
 * <code>ERXNSPrintWriterLogger</code> is an alternative of 
 * {@link com.webobjects.foundation.NSLog.PrintStreamLogger NSLog.PrintStreamLogger} 
 * but accepts character oriented {@link java.io.PrintWriter} instead of 
 * byte oriented {@link java.io.PrintStream}
 * so that the character encoding for logging can be 
 * explicitly set. 
 * <p>
 * Usage: 
 * <pre>
 * // Create a PrintWriter with Japanese "SJIS" encoding. 
 * OutputStreamWriter sjisOutWriter = new OutputStreamWriter(new FileOutputStream("app.log"), "SJIS");
 * PrintWriter sjisPrintWriter = new PrintWriter(sjisOutWriter, true);
 *
 * // Create a logger then set it to NSLog.
 * NSLog.Logger logger = new ERXNSPrintWriterLogger(sjisPrintWriter);
 * NSLog.setDebug(logger);
 * NSLog.setOut(logger);
 * NSLog.setErr(logger);
 * </pre>
 */
public class ERXNSPrintWriterLogger extends NSLog.Logger {

    /** java.io.PrintWriter to print logs */ 
    private PrintWriter _printWriter; 

    /** 
     * Apple built-in logger, used internally to generate 
     * a vervose header "[2002-08-31 09:01:00 EDT] <main> " 
     */ 
    private NSLog.PrintStreamLogger _vervoseStreamLogger; 
    
    /** an output from _vervoseStreamLogger */ 
    private ByteArrayOutputStream _verboseHeaderStream;
    
    public ERXNSPrintWriterLogger() {
        _printWriter = new PrintWriter(System.out);
    }
    
    public ERXNSPrintWriterLogger(PrintWriter printWriter) {
        _printWriter = printWriter;
    }

    public ERXNSPrintWriterLogger(PrintStream printStream) {
        _printWriter = new PrintWriter(printStream);
    }

    public ERXNSPrintWriterLogger(String encodingName) throws java.io.UnsupportedEncodingException {
        _printWriter = new PrintWriter(new OutputStreamWriter(System.out, encodingName), true);
    }
    
    @Override
    public void appendln() {
        if (isEnabled())
            _printWriter.println();
    }
    
    @Override
    public void appendln(Object object) {
        if (isEnabled()) {
            if (isVerbose()) 
                _printWriter.print(_verboseHeader());
            _printWriter.println(object);
        }
    }
    
    @Override
    public void flush() {
        _printWriter.flush();
    }

    private static final String _lineEndingString = " " + System.getProperty("line.separator");

    private String _verboseHeader() {
        _verboseHeaderStream().reset();  // clear the stream.

        // NSLog.PrintStreamLogger will set below to _verboseHeaderStream ByteArrayOutputStream:
        // "[2002-08-31 09:01:00 EDT] <main> " + " " + "\n" 
        _vervoseStreamLogger().appendln(" ");  
        String verboseHeader = _verboseHeaderStream().toString();
        
        // remove the _lineEndingString (" \n", " \r" or " \r\n") from the end of the line
        int lastIndex = verboseHeader.lastIndexOf(_lineEndingString);
        if (lastIndex > 0)   // ignores if _lineEndingString is the first char (lastIndex == 0)
            verboseHeader = verboseHeader.substring(0, lastIndex);
        return verboseHeader;
    }

    private ByteArrayOutputStream _verboseHeaderStream() {
        if (_verboseHeaderStream == null) 
            _verboseHeaderStream =  new ByteArrayOutputStream("[2002-08-31 09:01:00 EDT] <main> ".length() + 20);
        return _verboseHeaderStream;
    }

    private NSLog.PrintStreamLogger _vervoseStreamLogger() {
        if (_vervoseStreamLogger == null) 
            _vervoseStreamLogger = new NSLog.PrintStreamLogger(new PrintStream(_verboseHeaderStream()));
        return _vervoseStreamLogger;
    }

}
