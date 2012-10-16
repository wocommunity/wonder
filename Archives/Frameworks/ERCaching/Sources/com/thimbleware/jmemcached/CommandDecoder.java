/**
 *  Copyright 2008 ThimbleWare Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.thimbleware.jmemcached;

import static com.thimbleware.jmemcached.CommandDecoder.SessionState.ERROR;
import static com.thimbleware.jmemcached.CommandDecoder.SessionState.READY;
import static com.thimbleware.jmemcached.CommandDecoder.SessionState.WAITING_FOR_DATA;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * MINA MessageDecoderAdapter responsible for parsing inbound lines from the memcached protocol session.
 */
public final class CommandDecoder extends MessageDecoderAdapter {

    private final static int THIRTY_DAYS = 60 * 60 * 24 * 30;

    public static CharsetDecoder DECODER  = Charset.forName("US-ASCII").newDecoder();

    private static final int WORD_BUFFER_INIT_SIZE = 16;

    private static final String SESSION_STATUS = "sessionStatus";

    /**
     * Possible states that the current session is in.
     */
    enum SessionState {
        ERROR,
        WAITING_FOR_DATA,
        READY
    }

    /**
     * Object for holding the current session status.
     */
    final class SessionStatus implements Serializable {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        // the state the session is in
        public SessionState state;

        // if we are waiting for more data, how much?
        public int bytesNeeded;

        // the current working command
        public CommandMessage cmd;

        SessionStatus(SessionState state) {
            this.state = state;
        }

        SessionStatus(SessionState state, int bytesNeeded, CommandMessage cmd) {
            this.state = state;
            this.bytesNeeded = bytesNeeded;
            this.cmd = cmd;
        }
    }

    /**
     * Checks the specified buffer is decodable by this decoder.
     *
     * In our case checks the session state to see if we are waiting for data.  If we are, make sure
     * that we actually have all the data we need.
     *
     * @return {@link #OK} if this decoder can decode the specified buffer.
     *         {@link #NOT_OK} if this decoder cannot decode the specified buffer.
     *         {@link #NEED_DATA} if more data is required to determine if the
     *         specified buffer is decodable ({@link #OK}) or not decodable
     *         {@link #NOT_OK}.
     */
    public final MessageDecoderResult decodable(IoSession session, ByteBuffer in) {
        // ask the session for its state,
        SessionStatus sessionStatus = (SessionStatus) session.getAttribute(SESSION_STATUS);
        if (sessionStatus != null &&  sessionStatus.state == WAITING_FOR_DATA) {
            if (in.remaining() < sessionStatus.bytesNeeded + 2)
                return MessageDecoderResult.NEED_DATA;
        }
        return MessageDecoderResult.OK;
    }

    /**
     * Actually decodes inbound data from the memcached protocol session.
     *
     * MINA invokes {@link #decode(IoSession, ByteBuffer, ProtocolDecoderOutput)}
     * method with read data, and then the decoder implementation puts decoded
     * messages into {@link ProtocolDecoderOutput}.
     *
     * @return {@link #OK} if finished decoding messages successfully.
     *         {@link #NEED_DATA} if you need more data to finish decoding current message.
     *         {@link #NOT_OK} if you cannot decode current message due to protocol specification violation.
     *
     * @throws Exception if the read data violated protocol specification
     */
    public final MessageDecoderResult decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
        SessionStatus sessionStatus = (SessionStatus) session.getAttribute(SESSION_STATUS);
        SessionStatus returnedSessionStatus;
        if (sessionStatus != null && sessionStatus.state == WAITING_FOR_DATA) {
            if (in.remaining() < sessionStatus.bytesNeeded + 2) {
                return MessageDecoderResult.NEED_DATA;
            }
            // get the bytes we want, and that's it

            byte[] buffer = new byte[sessionStatus.bytesNeeded];
            in.get(buffer);

            // eat crlf at end
            String crlf = in.getString(2, DECODER);
            if (crlf.equals("\r\n"))
                returnedSessionStatus = continueSet(session, out, sessionStatus, buffer);
            else {
                session.setAttribute(SESSION_STATUS, new SessionStatus(READY));
                return MessageDecoderResult.NOT_OK;
            }
        } else {
            // retrieve the first line of the input, if there isn't a full one, request more
            StringBuffer wordBuffer = new StringBuffer(WORD_BUFFER_INIT_SIZE);
            ArrayList<String> words = new ArrayList<String>(8);
            in.mark();
            boolean completed = false;
            for (int i = 0; in.hasRemaining();) {
                char c = (char) in.get();

                if (c == ' ') {
                    words.add(wordBuffer.toString());
                    wordBuffer = new StringBuffer(WORD_BUFFER_INIT_SIZE);
                    i++;
                } else if (c == '\r' && in.hasRemaining() && in.get() == (byte) '\n') {
                    if (wordBuffer.length() != 0)
                        words.add(wordBuffer.toString());
                    completed = true;
                    break;
                } else {
                    wordBuffer.append(c);
                    i++;
                }
            }
            if (!completed) {
                in.reset();
                return MessageDecoderResult.NEED_DATA;
            }

            returnedSessionStatus = processLine(words, session, out);
        }

        if (returnedSessionStatus.state != ERROR) {
            session.setAttribute(SESSION_STATUS, returnedSessionStatus);
            return MessageDecoderResult.OK;
        } else
            return MessageDecoderResult.NOT_OK;
        
    }


    /**
     * Process an individual completel protocol line and either passes the command for processing by the
     * session handler, or (in the case of SET-type commands) partially parses the command and sets the session into
     * a state to wait for additional data.
     * @param parts the (originally space separated) parts of the command
     * @param session the MINA IoSession
     * @param out the MINA protocol decoder output to pass our command on to
     * @return the session status we want to set the session to
     */
    private SessionStatus processLine(List<String> parts, IoSession session, ProtocolDecoderOutput out) {
        CommandMessage cmd = new CommandMessage(parts.get(0).toUpperCase().intern());

        if (cmd.cmd == Commands.ADD ||
                cmd.cmd == Commands.SET ||
                cmd.cmd == Commands.REPLACE ||
                cmd.cmd == Commands.CAS ||
                cmd.cmd == Commands.APPEND ||
                cmd.cmd == Commands.PREPEND) {

            // if we don't have all the parts, it's malformed
            if (parts.size() < 5) {
                return new SessionStatus(ERROR);
            }


            int size = Integer.parseInt(parts.get(4));

            cmd.element = new MCElement();
            cmd.element.keystring = parts.get(1);
            cmd.element.flags = parts.get(2);
            cmd.element.expire = Integer.parseInt(parts.get(3));
            if (cmd.element.expire != 0 && cmd.element.expire <= THIRTY_DAYS) {
                cmd.element.expire += Now();
            }
            cmd.element.data_length = size;

            // look for cas and "noreply" elements
            if (parts.size() > 5) {
                int noreply = cmd.cmd == Commands.CAS ? 6 : 5;
                if (cmd.cmd == Commands.CAS) {
                    cmd.cas_key = Long.valueOf(parts.get(5));
                }

                if (parts.size() == noreply + 1 && parts.get(noreply).equalsIgnoreCase("noreply"))
                    cmd.noreply = true;

            }

            return new SessionStatus(WAITING_FOR_DATA, size, cmd);

        } else if (cmd.cmd == Commands.GET ||
                cmd.cmd == Commands.GETS ||
                cmd.cmd == Commands.STATS ||
                cmd.cmd == Commands.QUIT ||
                cmd.cmd == Commands.VERSION) {
            // CMD <options>*
            cmd.keys.addAll(parts.subList(1, parts.size()));

            out.write(cmd);
        } else if (cmd.cmd == Commands.INCR ||
                cmd.cmd == Commands.DECR) {

            if (parts.size() < 2 || parts.size() > 3)
                return new SessionStatus(ERROR);

            cmd.keys.add(parts.get(1));
            cmd.keys.add(parts.get(2));
            if (parts.size() == 3 && parts.get(2).equalsIgnoreCase("noreply")) {
                cmd.noreply = true;
            }

            out.write(cmd);
        } else if (cmd.cmd == Commands.DELETE) {
            cmd.keys.add(parts.get(1));

            if (parts.size() >= 2) {
                if (parts.get(parts.size() - 1).equalsIgnoreCase("noreply")) {
                    cmd.noreply = true;
                    if (parts.size() == 4)
                        cmd.time = Integer.valueOf(parts.get(2));
                } else if (parts.size() == 3)
                    cmd.time = Integer.valueOf(parts.get(2));
            }

            out.write(cmd);
        } else if (cmd.cmd == Commands.FLUSH_ALL) {
            if (parts.size() >= 1) {
                if (parts.get(parts.size() - 1).equalsIgnoreCase("noreply")) {
                    cmd.noreply = true;
                    if (parts.size() == 3)
                        cmd.time = Integer.valueOf(parts.get(1));
                } else if (parts.size() == 2)
                    cmd.time = Integer.valueOf(parts.get(1));
            }
            out.write(cmd);
        } else {
            return new SessionStatus(ERROR);
        }


        return new SessionStatus(READY);

    }

    /**
     * Handles the continuation of a SET/ADD/REPLACE command with the data it was waiting for.
     *
     * @param session the MINA IoSession
     * @param out the MINA protocol decoder output which we signal with the completed command
     * @param state the current session status (unused)
     * @param remainder the bytes picked up
     * @return the new status to set the session to
     */
    private SessionStatus continueSet(IoSession session, ProtocolDecoderOutput out, SessionStatus state, byte[] remainder) {
        state.cmd.element.data = remainder;

        out.write(state.cmd);

        return new SessionStatus(READY);
    }

    /**
     * @return the current time in seconds
     */
    public final int Now() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
