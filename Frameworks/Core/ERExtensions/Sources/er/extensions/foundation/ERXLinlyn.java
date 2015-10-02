//////////////////////////////////////////
//  split here for Linlyn.java          //
//  At last!  Java code to read/write files on the server from an applet!
//  This is the famous Linlyn code.
//
//  Use:  
//    compile this file, and have your applet call it as below.
//
//    to upload a file:  
//          Linlyn ftp = new Linlyn( <servername>, <user>, <password> );
//          ftp.upload( <directory>, <filename>, <contents of file> );
//
//    to download a file:  
//          Linlyn ftp = new Linlyn( <servername>, <user>, <password> );
//          String contents = ftp.download( <directory>, <filename> );
//
//          the default is ASCII transfer, an overloaded method does bin.
//
//    All parameters and return values are Strings. E.g.
//          Linlyn ftp = new Linlyn( "rtfm.mit.edu", "anonymous", "linden@" );
//          String contents = ftp.download( 
//                        "/pub/usenet-by-group/comp.lang.java.programmer"
//                        "Java_Programmers_FAQ" );
//
//          [the actual values above are not generally valid, substitute
//           your own server for your first attempt, see note 1.]
//
//    Notes:
//      1.  Usual applet security rules apply: you can only get a file
//          from the server that served the applet.
//      2.  The applet server must also be an FTP server.  This is NOT true
//          for some ISPs, such as best.com.  They have separate FTP and
//          http machines.  This code may work on such a setup if you put
//          the classfiles into the ftp area, and in the HTML file say:
//            <applet  codebase="ftp:///home/linden/ftp"  code="t.class" 
//      3.  This code does not break Java security.
//          It uses FTP to transfer files.  If the author of the applet
//          has FTP disabled you are out of luck.
//          It breaks regular system security however, as it publishes
//          (effectively) your ftp password.  Only use on an Intranet and
//          with authorization.
//      4.  Compiling this causes some deprecation warnings.   We wanted to 
//          stick with code that would work in JDK 1.0 browsers.
//      5.  Each upload or download creates, uses, and terminates a new
//          ftp session.  This is intended for low volume transfer, such
//          as the ever popular high-score files.
//      6.  Look at the source for the methods for binary transfers.
//
//      7.  On the Windows platform (particularly an NT ftp server
//          accessed through the IE5 browser) we have noticed a Microsoft
//          bug.  Sometimes the file methods: STOR, DELE, APPE, etc. "stall".
//          The file operation starts, but for some reason never completes.
//
//          The workaround is to nudge the buggy Microsoft server by sending
//          a "NOOP" command after each file operation, e.g.
//              ...
//              ftpSendCmd("STOR "+file);
//              ftpSendCmd("NOOP");
//              ...
//          Your mileage may vary, just passing on a tip.
//
//      8.  FTP is specified in RFC 959 and 1123.
//          It is based on the telnet protocol which is RFC 854.
//          There are more FTP RFC's such as 1579, 1635, 1639 and 2228,
//          if you are interested in FTP development.
//          You can find RFCs online in many places, including
//                  http://www.ietf.org
//                  http://www.faqs.org/rfcs/rfc959.html
//                  ftp://ftp.isi.edu/in-notes/rfc959.txt
//
//    Version 1.0   May 6 1998.
//    Version 1.1   May 20 1998. -- added a debugging flag
//    Version 1.1a  May 26 1998. -- fixed the ASCII/BIN flag inversion
//    Version 1.1b  May 29 1998. -- added the security warning.
//    Version 2.0   Jul 1, 1998. -- Updated to parse multi-string responses 
//                                  a la RFC 959
//    Version 2.1   Aug 5, 1998. -- Updated to work with VMS ftp servers
//                                  VMS does not send either a ")" OR a ")."
//                                  terminating the IP number, port sequence 
//                                  in response to PASV.
//    Version 2.1a  Aug 6, 1998  -- more than one line as a "hello" message.
//                                  (tvalesky@patriot.net)
//    Version 2.2   Sep 22 1998  -- added a flush() in ftpSendCmd.
//    Version 2.2a  Dec 09 1998  -- added comments on compiling w/o deprecation
//    Version 2.2b  May 05 1999  -- added dos.close() to upload
//    Version 2.2c  May 19 1999  -- moved the dos.close line.
//    Version 2.2c  May 23 1999  -- updated comment on license
//    Version 2.2d  Jul 22 1999  -- fixed for weird "XITAMI" servers
//    Version 3.0   Aug 18 1999  -- added "append to file" feature
//    Version 3.1   Jan 23 2000  -- submit and finish one request at a time.
//                                  Stall further requests, and swallow 
//                                  multiple responses from server
//    
//    Authors:
//          Robert Lynch
//          Peter van der Linden  (Author of "Just Java" book).
//   
//    Support:
//          Unsupported: That's why we give you the source.
//          Help may be available on time & materials basis only.
//            You can get copious debug information by changing to
//            DEBUG=true     below and recompiling.
//
//    Copyright 1998,1999,2000 Robert Lynch, Peter van der Linden
//    This work is distributed under the ARTISTIC LICENSE listed at the
//    end of this file. 
//    We (the benevolent and philathropic authors) 
//    hereinafter referred to as "We, the benevolent and philathropic authors"
//    don't intend to make any money off this, and
//    this code may be used for commercial purposes without charges or 
//    license fees, provided that this source code is included with the 
//    distribution. 
//
//    Those using the code do so at their own risk and the authors 
//    are not responsible for any costs, loss, or damage which may
//    thereby be incurred.
package er.extensions.foundation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Can upload and download files from ftp servers
 */

public class ERXLinlyn {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXLinlyn.class);
    
    // FOR DEBUGGING: set the variable to "true"
    private boolean DEBUG = false;

    // constructor needs servername, username and passwd
    public ERXLinlyn(String server, String user, String pass) {
        this(server, DEFAULT_CNTRL_PORT, user, pass);
    }

    public ERXLinlyn(String server, int portNum, String user, String pass) {
        try {
            ftpConnect(server, portNum);
            ftpLogin(user, pass);
        } catch(IOException ioe) {ioe.printStackTrace();}
    }

    /**
 	 * Returns a list of files and associated attributes in a given directory.
 	 * There is no FTP-server independent way to retrieve only the file name
 	 * portion of the result, but calling
 	 * substring(lastIndexOf(' ')+1)
 	 * on each element in the array will work so long as there are no spaces in filenames.
 	 */
    public String[] listFiles(String dir) throws IOException {
        ftpSetTransferType(false);
        dsock = ftpGetDataSock();
        InputStream is = dsock.getInputStream();
        ftpSendCmd("LIST "+dir);

        String contents = getAsString(is);
        if (log.isDebugEnabled()) { log.debug(contents); }
        String[] files = contents.split("\n");
        return files;
    }

    public String download(String dir, String file)
        throws IOException { return download(dir, file, true); }

    public String download(String dir, String file, boolean asc)
        throws IOException {
        return download(dir, file, asc, true);
    }

    public String download(String dir, String file, boolean asc, boolean keepAlive)
        throws IOException {
        ftpSetDir(dir);
        ftpSetTransferType(asc);
        dsock = ftpGetDataSock();
        InputStream is = dsock.getInputStream();
        ftpSendCmd("RETR "+file);
        
        String contents = getAsString(is);
        if (!keepAlive) {
            ftpLogout();
        }
        return contents;
    }

    public void append(String dir, String file, String what, boolean asc)
        throws IOException {
        ftpSetDir(dir);
        ftpSetTransferType(asc);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("APPE "+file); 
        dos.writeBytes(what);
        dos.flush();
        dos.close();
        ftpLogout();
    }


    public void upload(String dir, String file, String what) throws IOException {
        upload(dir, file, what, true);
    }

    public void upload(String dir, String file, byte[] bytes) throws IOException {
        upload(dir, file, bytes, true);
    }

    public void upload(String dir, String file, byte[] bytes, boolean keepAlive) throws IOException {
        ftpSetDir(dir);
        ftpSetTransferType(false);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("STOR "+file);
        dos.write(bytes);
        dos.flush();
        dos.close();
        if (!keepAlive) {
            ftpLogout();
        }
    }
    
    public void upload(String dir, String file, String what, boolean asc) throws IOException {
        upload(dir, file, what, asc, true);
    }

    public void upload(String dir, String file, String what, boolean asc, boolean keepAlive) throws IOException {
        ftpSetDir(dir);
        ftpSetTransferType(asc);
        dsock = ftpGetDataSock();
        OutputStream os = dsock.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("STOR "+file);
        dos.writeBytes(what);
        dos.flush();
        dos.close();
        if (!keepAlive) {
            ftpLogout();
        }
    }

    /**
     * Removes a file from a directory on the server.
     * @return true if the file was deleted (response 250), false otherwise.
     */
    public boolean deleteFile(String dir, String file) throws IOException {
        return deleteFile(dir, file, true);
    }

    public boolean deleteFile(String dir, String file, boolean keepAlive) throws IOException {
        ftpSetDir(dir);
        String response = ftpSendCmd("DELE "+file);
        if (!keepAlive) {
            ftpLogout();
        }
        if (response.startsWith("250")) {
            return true;
        }

        return false;
    }

    ///////////////// private fields ////////////////////
    private boolean pauser = false;  // it's a hack. We're going to 
          // stall (refuse further requests) till we get a reply back 
          // from server for the current request.

    private String getAsString(InputStream is) {
        int c=0;
        int doubler = 65536;
        char lineBuffer[]=new char[doubler], buf[]=lineBuffer;
        int room= buf.length, offset=0;
        try {
          loop: while (true) {
              // read chars into a buffer which grows as needed
                  switch (c = is.read() ) {
                      case -1: break loop;

                      default: if (--room < 0) {
                                   if (log.isDebugEnabled()) { log.debug("Growing array by: " + doubler); }
                                   buf = new char[offset + doubler];
                                   doubler *= 2; // double the size of the array each time it grows.
                                   room = buf.length - offset - 1;
                                   System.arraycopy(lineBuffer, 0, 
                                            buf, 0, offset);
                                   lineBuffer = buf;
                               }
                               buf[offset++] = (char) c;
                               break;
                  }
          }
        } catch(IOException ioe) {ioe.printStackTrace();}
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    private void ftpConnect(String server, int portNum)
        throws IOException {
        // Set up socket, control streams, connect to ftp server
        // Open socket to server control port (default is 21)
        csock = new Socket(server, portNum);
        // Open control streams
        InputStream cis = csock.getInputStream();
        dcis =  new BufferedReader(new InputStreamReader(cis));
        OutputStream cos = csock.getOutputStream();
        pos = new PrintWriter(cos, true); // set auto flush true.
        // See if server is alive or dead...
        String numerals = responseHandler(null); 
        if(numerals.substring(0,3).equals("220")) {
          // ftp server alive
          // System.out.println("Connected to ftp server");
        }        
        else {
          System.err.println("Error connecting to ftp server.");
        }
    }

    private void ftpLogin(String user, String pass)
        throws IOException {
        ftpSendCmd("USER "+user);
        ftpSendCmd("PASS "+pass);
    }

    private void ftpSetDir(String dir) throws IOException {
        // cwd to dir
        ftpSendCmd("CWD "+dir);
    }

    private void ftpSetTransferType(boolean asc)
    throws IOException {
    // set file transfer type
        String ftype = (asc? "A" : "I");
        ftpSendCmd("TYPE "+ftype);
    }    

    private Socket ftpGetDataSock()
        throws IOException {
        // Go to PASV mode, capture server reply, parse for socket setup
        // V2.1: generalized port parsing, allows more server variations
        String reply = ftpSendCmd("PASV");

        // New technique: just find numbers before and after ","!
        StringTokenizer st = new StringTokenizer(reply, ",");
        String[] parts = new String[6]; // parts, incl. some garbage
        int i = 0; // put tokens into String array
        while(st.hasMoreElements()) {
            // stick pieces of host, port in String array
            try {
                parts[i] = st.nextToken();
                i++;
            } catch(NoSuchElementException nope){nope.printStackTrace();}
        } // end getting parts of host, port

        // Get rid of everything before first "," except digits
        String[] possNum = new String[3];
        for(int j = 0; j < 3; j++) {
            // Get 3 characters, inverse order, check if digit/character
            possNum[j] = parts[0].substring(parts[0].length() - (j + 1),
                parts[0].length() - j); // next: digit or character?
            if(!Character.isDigit(possNum[j].charAt(0)))
                possNum[j] = "";
        }
        parts[0] = possNum[2] + possNum[1] + possNum[0];
        // Get only the digits after the last ","
        String[] porties = new String[3];
        for(int k = 0; k < 3; k++) {
            // Get 3 characters, in order, check if digit/character
            // May be less than 3 characters
            if((k + 1) <= parts[5].length())
                porties[k] = parts[5].substring(k, k + 1);
            else porties[k] = "FOOBAR"; // definitely not a digit!
            // next: digit or character?
            if(!Character.isDigit(porties[k].charAt(0)))
                    porties[k] = "";
        } // Have to do this one in order, not inverse order
        parts[5] = porties[0] + porties[1] + porties[2];
        // Get dotted quad IP number first
        String ip = parts[0]+"."+parts[1]+"."+parts[2]+"."+parts[3];

        // Determine port
        int port = -1;
        try { // Get first part of port, shift by 8 bits.
            int big = Integer.parseInt(parts[4]) << 8;
            int small = Integer.parseInt(parts[5]);
            port = big + small; // port number
        } catch(NumberFormatException nfe) {nfe.printStackTrace();}
        if((ip != null) && (port != -1))

            dsock = new Socket(ip, port);
        else throw new IOException();
        return dsock;
    }

    private String ftpSendCmd(String cmd)
        throws IOException
    { // This sends a dialog string to the server, returns reply
      // V2.0 Updated to parse multi-string responses a la RFC 959
      // Prints out only last response string of the lot.
        if (pauser) // i.e. we already issued a request, and are
                  // waiting for server to reply to it.  
            {
                if (dcis != null)
                {
                    String discard = dcis.readLine(); // will block here
                    // preventing this further client request until server
                    // responds to the already outstanding one.
                    if (DEBUG) {
                        System.out.println("keeping handler in sync"+
                            " by discarding next response: ");
                        System.out.println(discard);
                    }
                    pauser = false;
                }
            }
        pos.print(cmd + "\r\n" );
        pos.flush(); 
        String response = responseHandler(cmd);
        if(log.isDebugEnabled()) {
            log.debug("command = "+cmd);
            log.debug("response = "+response);
        }
        return response;
    }

     // new method to read multi-line responses
     // responseHandler: takes a String command or null and returns
     // just the last line of a possibly multi-line response
     private String responseHandler(String cmd) 
         throws IOException
     { // handle more than one line returned
        String reply = responseParser(dcis.readLine());
        String numerals = reply.substring(0, 3);
        String hyph_test = reply.substring(3, 4);
        String next = null;
        if(hyph_test.equals("-")) {
            // Create "tester", marks end of multi-line output
            String tester = numerals + " ";
            boolean done = false;
            while(!done) { // read lines til finds last line
                next = dcis.readLine();
                // Read "over" blank line responses
                while (next.equals("") || next.equals("  ")) {
                    next = dcis.readLine();
                }

                // If next starts with "tester", we're done
               if(next.substring(0,4).equals(tester))
                   done = true;
            }

            if(DEBUG)
                if(cmd != null)
                    System.out.println("Response to: "+cmd+" was: "+next);
                else
                    System.out.println("Response was: "+next);
            return next;

        } else // "if (hyph_test.equals("-")) not true"
            if(DEBUG)
                if(cmd != null)
                    System.out.println("Response to: "+cmd+" was: "+reply);
                else
                    System.out.println("Response was: "+reply);
            return reply;
    }

    // responseParser: check first digit of first line of response
    // and take action based on it; set up to read an extra line
    // if the response starts with "1"
    private String responseParser(String resp)
        throws IOException
    { // Check first digit of resp, take appropriate action.
        String digit1 = resp.substring(0, 1);
        if(digit1.equals("1")) {
            // server to act, then give response
            if(DEBUG) System.out.println("in 1 handler");
            // set pauser
            pauser = true;
            return resp;
        }
        else if(digit1.equals("2")) { // do usual handling
            if(DEBUG) System.out.println("in 2 handler");
            // reset pauser
            pauser = false;
            return resp;
        }
        else if(digit1.equals("3") || digit1.equals("4")
            || digit1.equals("5")) { // do usual handling
            if(DEBUG) System.out.println("in 3-4-5 handler");
            return resp;
        }
        else { // not covered, so return null
            return null;
        }
    }


    private void ftpLogout() {// logout, close streams
        try { 
            if(DEBUG) System.out.println("sending BYE");
            pos.print("BYE" + "\r\n" );
            pos.flush();
            pos.close();
            dcis.close();
            csock.close();
            dsock.close();
        } catch(IOException ioe) {ioe.printStackTrace();}
    }


    private static final int DEFAULT_CNTRL_PORT = 21;
    private Socket csock = null;
    private Socket dsock = null;
    private BufferedReader dcis;
    private PrintWriter pos;
}

//////////////////////////////////////////////////////////////////////
// 
//              The "Artistic License"   (from a KDE module)
// 
//                 Preamble
// 
// The intent of this document is to state the conditions under which this
// software may be copied, such that the Copyright Holder maintains some
// semblance of artistic control over the development of the software,
// while giving the users of the software the right to use and distribute
// the software in a more-or-less customary fashion, plus the right to make
// reasonable modifications.
// 
// Definitions:
// 
//     "Package" refers to the collection of files distributed by the
//     Copyright Holder, and derivatives of that collection of files
//     created through textual modification.
// 
//     "Standard Version" refers to such a Package if it has not been
//     modified, or has been modified in accordance with the wishes
//     of the Copyright Holder as specified below.
// 
//     "Copyright Holder" is whoever is named in the copyright or
//     copyrights for the package.
// 
//     "You" is you, if you're thinking about copying or distributing
//     this Package.
// 
//     "Reasonable copying fee" is whatever you can justify on the
//     basis of media cost, duplication charges, time of people involved,
//     and so on.  (You will not be required to justify it to the
//     Copyright Holder, but only to the computing community at large
//     as a market that must bear the fee.)
// 
//     "Freely Available" means that no fee is charged for the item
//     itself, though there may be fees involved in handling the item.
//     It also means that recipients of the item may redistribute it
//     under the same conditions they received it.
// 
// 1. You may make and give away verbatim copies of the source form of the
// Standard Version of this Package without restriction, provided that you
// duplicate all of the original copyright notices and associated disclaimers.
// 
// 2. You may apply bug fixes, portability fixes and other modifications
// derived from the Public Domain or from the Copyright Holder.  A Package
// modified in such a way shall still be considered the Standard Version.
// 
// 3. You may otherwise modify your copy of this Package in any way, provided
// that you insert a prominent notice in each changed file stating how and
// when you changed that file, and provided that you do at least ONE of the
// following:
// 
//     a) place your modifications in the Public Domain or otherwise make them
//     Freely Available, such as by posting said modifications to Usenet or
//     an equivalent medium, or placing the modifications on a major archive
//     site such as uunet.uu.net, or by allowing the Copyright Holder to include
//     your modifications in the Standard Version of the Package.
// 
//     b) use the modified Package only within your corporation or organization.
// 
//     c) rename any non-standard executables so the names do not conflict
//     with standard executables, which must also be provided, and provide
//     a separate manual page for each non-standard executable that clearly
//     documents how it differs from the Standard Version.
// 
//     d) make other distribution arrangements with the Copyright Holder.
// 
// 4. You may distribute the programs of this Package in object code or
// executable form, provided that you do at least ONE of the following:
// 
//     a) distribute a Standard Version of the executables and library files,
//     together with instructions (in the manual page or equivalent) on where
//     to get the Standard Version.
// 
//     b) accompany the distribution with the machine-readable source of
//     the Package with your modifications.
// 
//     c) give non-standard executables non-standard names, and clearly
//     document the differences in manual pages (or equivalent), together
//     with instructions on where to get the Standard Version.
// 
//     d) make other distribution arrangements with the Copyright Holder.
// 
// 5. You may charge a reasonable copying fee for any distribution of this
// Package.  You may charge any fee you choose for support of this
// Package.  You may not charge a fee for this Package itself.  However,
// you may distribute this Package in aggregate with other (possibly
// commercial) programs as part of a larger (possibly commercial) software
// distribution provided that you do not advertise this Package as a
// product of your own.  You may embed this Package's interpreter within
// an executable of yours (by linking); this shall be construed as a mere
// form of aggregation, provided that the complete Standard Version of the
// interpreter is so embedded.
// 
// 6. The scripts and library files supplied as input to or produced as
// output from the programs of this Package do not automatically fall
// under the copyright of this Package, but belong to whomever generated
// them, and may be sold commercially, and may be aggregated with this
// Package.  If such scripts or library files are aggregated with this
// Package via the so-called "undump" or "unexec" methods of producing a
// binary executable image, then distribution of such an image shall
// neither be construed as a distribution of this Package nor shall it
// fall under the restrictions of Paragraphs 3 and 4, provided that you do
// not represent such an executable image as a Standard Version of this
// Package.
// 
// 7. C subroutines (or comparably compiled subroutines in other
// languages) supplied by you and linked into this Package in order to
// emulate subroutines and variables of the language defined by this
// Package shall not be considered part of this Package, but are the
// equivalent of input as in Paragraph 6, provided these subroutines do
// not change the language in any way that would cause it to fail the
// regression tests for the language.
// 
// 8. Aggregation of this Package with a commercial distribution is always
// permitted provided that the use of this Package is embedded; that is,
// when no overt attempt is made to make this Package's interfaces visible
// to the end user of the commercial distribution.  Such use shall not be
// construed as a distribution of this Package.
// 
// 9. The name of the Copyright Holder may not be used to endorse or 
// promote products derived from this software without specific prior 
// written permission.
// 
// 10. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
// WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
// 
// 11.  You must not be Microsoft, or currently be employed by any
// company that is partly or wholly owned by Microsoft, if there are
// any companies left in the software industry for which this is true.
//
//                 The End
