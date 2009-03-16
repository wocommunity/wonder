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

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

// TODO implement 'delete queue' (time on delete) and flush_all delay

/**
 * The heart of the daemon, responsible for handling the creation and destruction of network
 * sessions, keeping cache statistics, and (most importantly) processing inbound (parsed) commands and then passing on
 * a response message for output.
 */
public final class ServerSessionHandler implements IoHandler {

    final Logger logger = LoggerFactory.getLogger(ServerSessionHandler.class);

    public String version;
    public int curr_conns;
    public int total_conns;
    public int started;          /* when the process was started */

    public static long bytes_read;
    public static long bytes_written;
    public static long curr_bytes;

    public int idle_limit;
    public boolean verbose;

    public static CharsetEncoder ENCODER = Charset.forName("US-ASCII").newEncoder();

    /**
     */
    protected Cache cache;

    /**
     * Construct the server session handler
     *
     * @param cache            the cache to use
     * @param memcachedVersion the version string to return to clients
     * @param verbosity        verbosity level for debugging
     * @param idle             how long sessions can be idle for
     */
    public ServerSessionHandler(Cache cache, String memcachedVersion, boolean verbosity, int idle) {
        initStats();

        this.cache = cache;

        started = Now();
        version = memcachedVersion;
        verbose = verbosity;
        idle_limit = idle;
    }

    /**
     * Handle the creation of a new protocol session.
     *
     * @param session the MINA session object
     */
    public void sessionCreated(IoSession session) {
        int conn = total_conns++;
        session.setAttribute("sess_id", valueOf(conn));
        curr_conns++;
        if (this.verbose) {
            logger.info(session.getAttribute("sess_id") + " CONNECTED");
        }
    }


    /**
     * Handle the opening of a new session.
     *
     * @param session the MINA session object
     */
    public void sessionOpened(IoSession session) {
        if (this.idle_limit > 0) {
            session.setIdleTime(IdleStatus.BOTH_IDLE, this.idle_limit);
        }

        session.setAttribute("waiting_for", 0);
    }

    /**
     * Handle the closing of a session.
     *
     * @param session the MINA session object
     */
    public void sessionClosed(IoSession session) {
        curr_conns--;
        if (this.verbose) {
            logger.info(session.getAttribute("sess_id") + " DIS-CONNECTED");
        }
    }

    /**
     * Handle the reception of an inbound command, which has already been pre-processed by the CommandDecoder.
     *
     * @param session the MINA session
     * @param message the message itself
     * @throws CharacterCodingException
     */
    public void messageReceived(IoSession session, Object message) throws CharacterCodingException {
        CommandMessage command = (CommandMessage) message;
        String cmd = command.cmd;
        int cmdKeysSize = command.keys.size();

        // first process any messages in the delete queue
        cache.processDeleteQueue();

        // now do the real work
        if (this.verbose) {
            StringBuffer log = new StringBuffer();
            log.append(session.getAttribute("sess_id")).append(" ");
            log.append(cmd);
            if (command.element != null) {
                log.append(" ").append(command.element.keystring);
            }
            for (int i = 0; i < cmdKeysSize; i++) {
                log.append(" ").append(command.keys.get(i));
            }
            logger.info(log.toString());
        }

        ResponseMessage r = new ResponseMessage();
        if (cmd == Commands.GET || cmd == Commands.GETS) {
            for (int i = 0; i < cmdKeysSize; i++) {
                MCElement result = get(command.keys.get(i));
                if (result != null) {
                    r.out.putString("VALUE " + result.keystring + " " + result.flags + " " + result.data_length + (cmd == Commands.GETS ? " " + result.cas_unique : "") + "\r\n", ENCODER);
                    r.out.put(result.data, 0, result.data_length);
                    r.out.putString("\r\n", ENCODER);
                }
            }

            r.out.putString("END\r\n", ENCODER);
        } else if (cmd == Commands.SET) {
            String ret = set(command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.CAS) {
            String ret = cas(command.cas_key, command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.ADD) {
            String ret = add(command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.REPLACE) {
            String ret = replace(command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.APPEND) {
            String ret = append(command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.PREPEND) {
            String ret = prepend(command.element);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.INCR) {
            String ret = get_add(command.keys.get(0), parseInt(command.keys.get(1)));
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.DECR) {
            String ret = get_add(command.keys.get(0), -1 * parseInt(command.keys.get(1)));
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.DELETE) {
            String ret = delete(command.keys.get(0), command.time);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else if (cmd == Commands.STATS) {
            String option = "";
            if (cmdKeysSize > 0) {
                option = command.keys.get(0);
            }
            r.out.putString(stat(option), ENCODER);
        } else if (cmd == Commands.VERSION) {
            r.out.putString("VERSION ", ENCODER);
            r.out.putString(version, ENCODER);
            r.out.putString("\r\n", ENCODER);
        } else if (cmd == Commands.QUIT) {
            session.close();
        } else if (cmd == Commands.FLUSH_ALL) {

            String ret = flush_all(command.time);
            if (!command.noreply)
                r.out.putString(ret, ENCODER);
        } else {
            r.out.putString("ERROR\r\n", ENCODER);
            logger.error("error; unrecognized command: " + cmd);
        }
        session.write(r);
    }


    /**
     * Called on message delivery.
     *
     * @param session the MINA session
     * @param message the message sent
     */
    public void messageSent(IoSession session, Object message) {
        if (this.verbose) {
            logger.info(session.getAttribute("sess_id") + " SENT");
        }
    }

    /**
     * Triggered when a session has gone idle.
     *
     * @param session the MINA session
     * @param status  the idle status
     */
    public void sessionIdle(IoSession session, IdleStatus status) {
        // disconnect an idle client
        session.close();
    }

    /**
     * Triggered when an exception is caught by the protocol handler
     *
     * @param session the MINA session
     * @param cause   the exception
     */
    public void exceptionCaught(IoSession session, Throwable cause) throws CharacterCodingException {
        // close the connection on exceptional situation
        logger.error(session.getAttribute("sess_id") + " EXCEPTION", cause);

        ResponseMessage r = new ResponseMessage();

        // this needs to make a better distinction between server and client error messages
        r.out.putString("CLIENT_ERROR\r\n", ENCODER);
        session.write(r);
    }

    /**
     * Handle the deletion of an item from the cache.
     *
     * @param key  the key for the item
     * @param time only delete the element if time (time in seconds)
     * @return the message response
     */
    protected String delete(String key, int time) {
        return getDeleteResponseString(cache.delete(key, time));
    }

    private String getDeleteResponseString(Cache.DeleteResponse deleteResponse) {
        if (deleteResponse == Cache.DeleteResponse.DELETED) return "DELETED\r\n";
        else return "NOT_FOUND\r\n";
    }

    /**
     * Add an element to the cache
     *
     * @param e the element to add
     * @return the message response string
     */
    protected String add(MCElement e) {
        return getStoreResponseString(cache.add(e));
    }

    /**
     * Find the string response message which is equivalent to a response to a set/add/replace message
     * in the cache
     *
     * @param storeResponse the response code
     * @return the string to output on the network
     */
    protected String getStoreResponseString(Cache.StoreResponse storeResponse) {
        switch (storeResponse) {
            case EXISTS:
                return "EXISTS\r\n";
            case NOT_FOUND:
                return "NOT_FOUND\r\n";
            case NOT_STORED:
                return "NOT_STORED\r\n";
            case STORED:
                return "STORED\r\n";
        }
        throw new RuntimeException("unknown store response from cache: " + storeResponse);
    }

    /**
     * Replace an element in the cache
     *
     * @param e the element to replace
     * @return the message response string
     */
    protected String replace(MCElement e) {
        return getStoreResponseString(cache.replace(e));
    }

    /**
     * Append bytes to an element in the cache
     * @param element the element to append to
     * @return the message response string
     */
    protected String append(MCElement element) {
        return getStoreResponseString(cache.append(element));
    }

    /**
     * Prepend bytes to an element in the cache
     * @param element the element to append to
     * @return the message response string
     */
    protected String prepend(MCElement element) {
        return getStoreResponseString(cache.prepend(element));
    }

    /**
     * Set an element in the cache
     *
     * @param e the element to set
     * @return the message response string
     */
    protected String set(MCElement e) {
        return getStoreResponseString(cache.set(e));
    }

    /**
     * Check and set an element in the cache
     *
     * @param cas_key the unique cas id for the element, to match against
     * @param e       the element to set @return the message response string
     * @return the message response string
     */
    protected String cas(Long cas_key, MCElement e) {
        return getStoreResponseString(cache.cas(cas_key, e));
    }

    /**
     * Increment an (integer) element in the cache
     *
     * @param key the key to increment
     * @param mod the amount to add to the value
     * @return the message response
     */
    protected String get_add(String key, int mod) {
        Integer ret = cache.get_add(key, mod);
        if (ret == null)
            return "NOT_FOUND\r\n";
        else
            return valueOf(ret)  + "\r\n";
    }


    /**
     * Check whether an element is in the cache and non-expired
     *
     * @param key the key for the element to lookup
     * @return whether the element is in the cache and is live
     */
    protected boolean is_there(String key) {
        return cache.isThere(key);
    }

    /**
     * Get an element from the cache
     *
     * @param key the key for the element to lookup
     * @return the element, or 'null' in case of cache miss.
     */
    protected MCElement get(String key) {
        return cache.get(key);
    }


    /**
     * @return the current time in seconds (from epoch), used for expiries, etc.
     */
    protected final int Now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * Initialize all statistic counters
     */
    protected void initStats() {
        curr_bytes = 0;
        curr_conns = 0;
        total_conns = 0;
        bytes_read = 0;
        bytes_written = 0;
    }

    /**
     * Return runtime statistics
     *
     * @param arg additional arguments to the stats command
     * @return the full command response
     */
    protected String stat(String arg) {

        StringBuilder builder = new StringBuilder();

        if (arg.equals("keys")) {
            Iterator itr = this.cache.keys().iterator();
            while (itr.hasNext()) {
                builder.append("STAT key ").append(itr.next()).append("\r\n");
            }
            builder.append("END\r\n");
            return builder.toString();
        }

        // stats we know
        builder.append("STAT version ").append(version).append("\r\n");
        builder.append("STAT cmd_gets ").append(valueOf(cache.getGetCmds())).append("\r\n");
        builder.append("STAT cmd_sets ").append(valueOf(cache.getSetCmds())).append("\r\n");
        builder.append("STAT get_hits ").append(valueOf(cache.getGetHits())).append("\r\n");
        builder.append("STAT get_misses ").append(valueOf(cache.getGetMisses())).append("\r\n");
        builder.append("STAT curr_connections ").append(valueOf(curr_conns)).append("\r\n");
        builder.append("STAT total_connections ").append(valueOf(total_conns)).append("\r\n");
        builder.append("STAT time ").append(valueOf(Now())).append("\r\n");
        builder.append("STAT uptime ").append(valueOf(Now() - this.started)).append("\r\n");

        builder.append("STAT cur_items ").append(valueOf(this.cache.getCurrentItems())).append("\r\n");
        builder.append("STAT limit_maxbytes ").append(valueOf(this.cache.getLimitMaxBytes())).append("\r\n");
        builder.append("STAT current_bytes ").append(valueOf(this.cache.getCurrentBytes())).append("\r\n");
        builder.append("STAT free_bytes ").append(valueOf(Runtime.getRuntime().freeMemory())).append("\r\n");

        // stuff we know nothing about
        builder.append("STAT pid 0\r\n");
        builder.append("STAT rusage_user 0:0\r\n");
        builder.append("STAT rusage_system 0:0\r\n");
        builder.append("STAT connection_structures 0\r\n");
        builder.append("STAT bytes_read 0\r\n");
        builder.append("STAT bytes_written 0\r\n");
        builder.append("END\r\n");

        return builder.toString();
    }

    /**
     * Flush all cache entries
     *
     * @return command response
     */
    protected boolean flush_all() {
        return cache.flush_all();
    }

    /**
     * Flush all cache entries with a timestamp after a given expiration time
     *
     * @param expire the flush time in seconds
     * @return command response
     */
    protected String flush_all(int expire) {
        return cache.flush_all(expire) ? "OK\r\n" : "ERROR\r\n";
    }


}