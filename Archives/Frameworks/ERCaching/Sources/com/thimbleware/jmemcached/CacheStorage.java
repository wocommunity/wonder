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

import java.util.Set;

/**
 * Interface for a cache usable by the daemon.
 *
 * Read and write operations need not be thread safe as the caller (ServerSessionHandler) establishes
 * its own locks.
 */
public interface CacheStorage {


    /**
     * Retrieve an element from the cache.  The retriever is responsible for counting hits,
     * managing expirations, etc.
     *
     * @param keystring the key identifying the entry
     * @return a cache element
     */
    MCElement get(String keystring);

    /**
     * Put an entry into the cache or replace an existing entry.
     *
     * @param keystring the key identifying the entry
     * @param el the element to place in the cache
     * @param data_length
     */
    void put(String keystring, MCElement el, int data_length);


    /**
     * Remove an entry from the cache
     *
     * @param keystring the key to lookup
     */
    void remove(String keystring);

    /**
     * @return the list of keys currently managed in the cache
     */
    Set<String> keys();

    /**
     * Flush all entries from the cache
     */
    void clear();

    /**
     * @return the total count (in bytes) of all the elements in the cache
     */
    long getSize();

    /**
     * @return the maximum capacity (in bytes) of the cache
     */
    long getMaximumSize();

    /**
     * @return how many entries are in the cache
     */
    long count();

}
