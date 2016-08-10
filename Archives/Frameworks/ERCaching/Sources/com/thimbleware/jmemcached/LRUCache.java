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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple non-thread-safe LRU hash map cache.  Thread safety is expected to be provided by the caller. 
 */
public final class LRUCache<ID_TYPE, ITEM_TYPE> {

    /**
     * Map containing the actual storage
     */
    private final Map<ID_TYPE, CacheEntry<ITEM_TYPE>> items;

    private long size = 0; // current size in bytes

    private final long maximumSize; // in bytes
    private long ceilingSize;
    private int maximumItems;
    private static final int INITIAL_TABLE_SIZE = 2048;

    final class CacheEntry<ITEM_TYPE> {
        long size;
        ITEM_TYPE item;

        CacheEntry(long size, ITEM_TYPE item) {
            this.size = size;
            this.item = item;
        }
    }

    /**
     * Caches are created as empty, and populated through use.
     *
     * @param maximumItems maximum number of items allowed in the cache
     * @param maximumSize maximum size in bytes of the cache
     * @param ceilingSize number of bytes to attempt to leave as ceiling room
     */
    public LRUCache(final int maximumItems, final long maximumSize, final long ceilingSize) {
        this.maximumItems = maximumItems;
        this.maximumSize = maximumSize;
        this.ceilingSize = ceilingSize;

        /**
         * Creates a linked hash map which expels old elements on declared criterion
         */
        items = new LinkedHashMap<ID_TYPE, CacheEntry<ITEM_TYPE>>(INITIAL_TABLE_SIZE) {
        	/**
        	 * Do I need to update serialVersionUID?
        	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
        	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
        	 */
        	private static final long serialVersionUID = 1L;

            protected boolean removeEldestEntry(Map.Entry<ID_TYPE, CacheEntry<ITEM_TYPE>> eldest) {
            	if ((maximumSize > 0 && (size + ceilingSize > maximumSize)) || (maximumItems > 0 && size() > maximumItems)) {
                    size -= eldest.getValue().size;
                    return true;
                } else return false;
            }
        };

    }

    /**
     * Return true only if the corresponding item is in the cache, and has been
     * in it for no more that fRefreshInterval milliseconds; if caching is
     * disabled, then always return false.
     *
     * @param aId is non-null.
     * @return if the corresponding item is in the cache
     * @throws IllegalArgumentException if a param does not comply.
     */
    public boolean has(ID_TYPE aId) {
        if (aId == null) throw new IllegalArgumentException("Id must not be null.");

        return items.containsKey(aId);
    }

    /**
     * Retrieve an existing item from the cache.
     *
     * @param aId is non-null, and corresponds to an existing item in the cache.
     * @return a non-null Object
     * @throws IllegalArgumentException if aId is null, or if the item is not in the cache
     * @throws IllegalStateException    if the item in the cache is null or the cache is disabled
     */
    public ITEM_TYPE get(ID_TYPE aId) {
        if (aId == null) throw new IllegalArgumentException("Id must not be null.");

        ITEM_TYPE result;
        if (items.containsKey(aId)) {
            result = items.get(aId).item;
            if (result == null) {
                throw new IllegalStateException("Stored item should not be null. Id:" + aId);
            }
        } else {
            return null;
        }
        return result;
    }

    /**
     * If the item is already present, then replace it; otherwise, add it.
     *
     * If the cache is disabled, do nothing.
     *
     * @param aId   is non-null
     * @param aItem is non-null
     * @param item_size is the size of aItem in bytes
     * @throws IllegalArgumentException if param does not comply
     */
    public void put(ID_TYPE aId, ITEM_TYPE aItem, long item_size) {
        if (aId == null) throw new IllegalArgumentException("Id must not be null.");
        if (aItem == null) throw new IllegalArgumentException("Item must not be null.");

        // if the item already exists in the cache, subtract its old size
        if (items.containsKey(aId)) {
            size -= items.get(aId).size;
        }
        items.put(aId, new CacheEntry<ITEM_TYPE>(item_size, aItem));
        size += item_size;
    }

    /**
     * Remove an entry from the cache
     * @param key the key for the entry
     */
    public void remove(ID_TYPE key) {
        CacheEntry<ITEM_TYPE> item = items.get(key);
        if (item != null) {
            items.remove(key);
            size -= item.size;
        }
    }


    /**
     * Start from beginning, and remove all items from the cache; if cache is
     * disabled, do nothing.
     *
     * Forces a re-population of all items into the cache.
     */
    public void clear() {
        items.clear();
        size = 0;
    }

    /**
     * @return the set of all keys in the cache
     */
    public Set<ID_TYPE> keys() {
        return items.keySet();
    }

    /**
     * @return the number of entries in the cache
     */
    public long count() {
        return items.size();
    }

    /**
     * @return the maximum number of items in the cache
     */
    public int getMaximumItems() {
        return maximumItems;
    }

    /**
     * @return the size (in bytes) of all entries in the cache.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the maximum capacity (in bytes) of the cache
     */
    public long getMaximumSize() {
        return maximumSize;
    }

    /**
     * @return the reserved headroom/ceiling size (in bytes)
     */
    public long getCeilingSize() {
        return ceilingSize;
    }

}