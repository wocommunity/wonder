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
 * A delegate around the internal thread-safe LRUCache implementation.
 */
public final class LRUCacheStorageDelegate implements CacheStorage {

    private LRUCache<String, MCElement> cache;

    public LRUCacheStorageDelegate(int maxSize, long maxBytes, long ceilingSize) {
        //Create a CacheStorage specifying its configuration.
        cache = new LRUCache<String, MCElement>(maxSize, maxBytes, ceilingSize);
    }

    public MCElement get(String keystring) {
        return cache.get(keystring);
    }

    public void put(String keystring, MCElement el, int data_length) {
        cache.put(keystring, el, el.data_length);
    }

    public void remove(String keystring) {
        cache.remove(keystring);
    }

    public Set<String> keys() {
        return cache.keys();
    }

    public long getSize() {
        return cache.getSize();
    }

    public void clear() {
        cache.clear();
    }

    public long count() {
        return cache.count();
    }

    public long getMaximumSize() {
        return cache.getMaximumSize();
    }
}
