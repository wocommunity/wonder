package er.extensions.foundation;

import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;

/**
 * ERXLazyValue provides a way to model lazy-loaded values that invalidate with
 * different methods. This is very useful for storing expensive values that are
 * returned from bound component methods as well as values that are cached that
 * are influenced by different areas of a single page (i.e. ajax operations on
 * one part of the page that need to invalidate a cache on another part of the
 * page).
 * 
 * @author mschrag
 * 
 * @param <T>
 *            the type of the lazy value
 */
public class ERXLazyValue<T> {
	public static Logger log = Logger.getLogger(ERXLazyValue.class);

	private ERXLazyValue.Source<T> _dataSource;
	private ERXLazyValue.Invalidator _invalidator;
	private boolean _valueCached;
	private T _value;

	/**
	 * Constructs a new ERXLazyValue with a data source and a NeverInvalidator,
	 * which behaves like a "once" lazy value.
	 * 
	 * @param dataSource
	 *            the data source for the lazy value
	 */
	public ERXLazyValue(ERXLazyValue.Source<T> dataSource) {
		this(dataSource, new ERXLazyValue.NeverInvalidator());
	}

	/**
	 * Constructs a new ERXLazyValue with a shortcut for a KVCSource and a
	 * NeverInvalidator, which behaves like a "once" lazy value.
	 * 
	 * @param target
	 *            the target of the KVCSource
	 * @param keyPath
	 *            the keypath of the KVCSource
	 */
	public ERXLazyValue(Object target, String keyPath) {
		this(new KVCSource<T>(target, keyPath), new ERXLazyValue.NeverInvalidator());
	}

	/**
	 * Constructs a new ERXLazyValue with a shortcut for a KVCSource and an
	 * invalidator.
	 * 
	 * @param target
	 *            the target of the KVCSource
	 * @param keyPath
	 *            the keypath of the KVCSource
	 * @param invalidator
	 *            the invalidator to use
	 */
	public ERXLazyValue(Object target, String keyPath, ERXLazyValue.Invalidator invalidator) {
		this(new KVCSource<T>(target, keyPath), invalidator);
	}

	/**
	 * Constructs a new ERXLazyValue with a data source and an invalidator.
	 * 
	 * @param dataSource
	 *            the data source for the lazy value
	 * @param invalidator
	 *            the invalidator to use
	 */
	public ERXLazyValue(ERXLazyValue.Source<T> dataSource, ERXLazyValue.Invalidator invalidator) {
		_dataSource = dataSource;
		_invalidator = invalidator;
	}

	/**
	 * Forcefully invalidates the lazy value, regardless of the state of the
	 * invalidator.
	 */
	public synchronized void invalidate() {
		_valueCached = false;
		_value = null;
	}

	/**
	 * Returns the backing value for this lazy value, which will only sometimes
	 * trigger a call through to the data source.
	 * 
	 * @return the backing value
	 */
	public synchronized T value() {
		if (!_valueCached || _invalidator.shouldInvalidate()) {
			if (ERXLazyValue.log.isDebugEnabled()) {
				ERXLazyValue.log.debug("Fetching from " + _dataSource + " with invalidator " + _invalidator + " ...");
			}
			_value = _dataSource.value();
			_invalidator.fetchedValue(_value);
			_valueCached = true;
		}
		return _value;
	}

	/**
	 * Sets the backging value for this lazy value, which will always call
	 * through to the underlying data source. This will also cache the new value
	 * and notify the invalidator of a new fetched value (acting just like if
	 * value() was called on an invalidated cache).
	 * 
	 * @param value
	 *            the new value
	 */
	public synchronized void setValue(T value) {
		_value = value;
		_valueCached = true;
		_dataSource.setValue(value);
		_invalidator.fetchedValue(value);
	}

	/**
	 * A source provides the undelying value (which is usually uncached) to a
	 * lazy value.
	 * 
	 * @author mschrag
	 * 
	 * @param <T>
	 *            the type of the value
	 */
	public static interface Source<T> {
		/**
		 * Returns the underlying value.
		 * 
		 * @return the underlying value
		 */
		public T value();

		/**
		 * Sets the underlying value.
		 * 
		 * @param value
		 *            the new value
		 */
		public void setValue(T value);
	}

	/**
	 * ConstantSource provides a Source implementation on top of a fixed value.
	 * 
	 * @author mschrag
	 * 
	 * @param <T>
	 *            the type of the value
	 */
	public static class ConstantSource<T> implements ERXLazyValue.Source<T> {
		private T _value;

		/**
		 * Constructs a ConstantSource with a fixed value.
		 * 
		 * @param value
		 *            the value
		 */
		public ConstantSource(T value) {
			_value = value;
		}

		/**
		 * Returns the fixed value.
		 * 
		 * @return the fixed value
		 */
		public T value() {
			return _value;
		}

		/**
		 * Sets a new fixed value.
		 * 
		 * @param value
		 *            the new value
		 */
		public void setValue(T value) {
			_value = value;
		}

		@Override
		public String toString() {
			return "[ConstantSource: value=" + _value + "]";
		}
	}

	/**
	 * KVCSource provides a wrapper around a KVC binding, which is very useful
	 * in components. As an example, you might have your people() method return
	 * a lazy value that is bound to a KVCSource&lt;Person&gt;(this,
	 * "uncachedPeople").
	 * 
	 * @author mschrag
	 * 
	 * @param <T>
	 *            the type of the value
	 */
	public static class KVCSource<T> implements ERXLazyValue.Source<T> {
		private Object _target;
		private String _keyPath;

		/**
		 * Constructs a new KVCSource.
		 * 
		 * @param target
		 *            the target of the KVC binding
		 * @param keyPath
		 *            the keypath to return a value from
		 */
		public KVCSource(Object target, String keyPath) {
			_target = target;
			_keyPath = keyPath;
		}

		/**
		 * Returns the value of the kaypath on the target.
		 * 
		 * @return the value
		 */
		public T value() {
			return (T) NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(_target, _keyPath);
		}

		/**
		 * Sets the value of the keypath on the target.
		 * 
		 * @param value
		 *            the new value
		 */
		public void setValue(T value) {
			NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(_target, value, _keyPath);
		}

		@Override
		public String toString() {
			return "[KVCSource: target=" + _target.getClass().getSimpleName() + "; keyPath=" + _keyPath + "]";
		}
	}

	/**
	 * Invalidator provides an mechanism for selectively invalidating the cached
	 * lazy value.
	 * 
	 * @author mschrag
	 */
	public static interface Invalidator {
		/**
		 * Called when the lazy value is refetched from the source.
		 * 
		 * @param value
		 *            the new value
		 */
		public void fetchedValue(Object value);

		/**
		 * Returns whether or not the lazy value should invalidate its cache.
		 * 
		 * @return whether or not the lazy value should invalidate its cache
		 */
		public boolean shouldInvalidate();
	}

	/**
	 * TimedInvalidator specifies that the cached value should be invalidated
	 * after a specified duration. When the value is refetched, the timer is
	 * restarted.
	 * 
	 * @author mschrag
	 */
	public static class TimedInvalidator implements ERXLazyValue.Invalidator {
		private long _timeToLiveInMillis;
		private long _cacheTime;

		/**
		 * Constructs a new TimedInvalidator.
		 * 
		 * @param timeToLiveInMillis
		 *            the time-to-live in milliseconds
		 */
		public TimedInvalidator(long timeToLiveInMillis) {
			_timeToLiveInMillis = timeToLiveInMillis;
			_cacheTime = -1;
		}

		public void fetchedValue(Object value) {
			_cacheTime = System.currentTimeMillis();
		}

		public boolean shouldInvalidate() {
			return _cacheTime == -1 || (System.currentTimeMillis() - _cacheTime) > _timeToLiveInMillis;
		}
	}

	/**
	 * Returns true from shouldInvalidate, causing the cache to always refresh.
	 * 
	 * @author mschrag
	 */
	public static class AlwaysInvalidator implements ERXLazyValue.Invalidator {
		public void fetchedValue(Object value) {
			// DO NOTHING
		}

		public boolean shouldInvalidate() {
			return true;
		}
	}

	/**
	 * Returns false from shouldInvalidate, causing the cache to never refresh.
	 * 
	 * @author mschrag
	 */
	public static class NeverInvalidator implements ERXLazyValue.Invalidator {
		public void fetchedValue(Object value) {
			// DO NOTHING
		}

		public boolean shouldInvalidate() {
			return false;
		}
	}

	/**
	 * The base class for any invalidator that is triggered by the change in a
	 * cache key.
	 * 
	 * @author mschrag
	 */
	public static abstract class CacheKeyInvalidator implements ERXLazyValue.Invalidator {
		private Object _lastCacheKey;

		public void fetchedValue(Object value) {
			_lastCacheKey = cacheKey();
		}

		/**
		 * Returns the current value of the cache key.
		 * 
		 * @return the current value of the cache key
		 */
		protected abstract Object cacheKey();

		public boolean shouldInvalidate() {
			Object currentCacheKey = cacheKey();
			return ObjectUtils.notEqual(_lastCacheKey, currentCacheKey);
		}
	}

	/**
	 * The base class for any invalidator that is triggered by the change in a
	 * cache key with support for changing the value.
	 * 
	 * @author mschrag
	 */
	public static abstract class MutableCacheKeyInvalidator extends ERXLazyValue.CacheKeyInvalidator {
		/**
		 * Sets the current value of the cache key.
		 * 
		 * @param value
		 *            the current value of the cache key
		 */
		protected abstract void setCacheKey(Object value);

		/**
		 * Sets the current value of the cache key to be a randomly generated
		 * UUID.
		 */
		public void uuid() {
			setCacheKey(UUID.randomUUID());
		}

		/**
		 * Sets the current value of the cache key to be
		 * System.currentTimeMillis.
		 */
		public void timestamp() {
			setCacheKey(Long.valueOf(System.currentTimeMillis()));
		}
	}

	/**
	 * ThreadStorageCacheKeyInvalidator triggers a cache invalidation when the
	 * value of the specified key changes in the ERXThreadStorage.
	 * 
	 * @author mschrag
	 */
	public static class ThreadStorageCacheKeyInvalidator extends ERXLazyValue.MutableCacheKeyInvalidator {
		private String _key;

		public ThreadStorageCacheKeyInvalidator(String key) {
			_key = key;
		}

		@Override
		protected Object cacheKey() {
			return ERXThreadStorage.valueForKey(_key);
		}

		@Override
		public void setCacheKey(Object value) {
			ERXThreadStorage.takeValueForKey(value, _key);
		}
	}

	/**
	 * PageUserInfoCacheKeyInvalidator triggers a cache invalidation when the
	 * value of the specified key changes in the ERXResponseRewriter's
	 * pageUserInfo. This is useful for triggering cache refreshes based on ajax
	 * updates to other parts of the page.
	 * 
	 * @author mschrag
	 */
	public static class PageUserInfoCacheKeyInvalidator extends ERXLazyValue.MutableCacheKeyInvalidator {
		private String _key;

		public PageUserInfoCacheKeyInvalidator(String key) {
			_key = key;
		}

		@Override
		protected Object cacheKey() {
			return ERXResponseRewriter.pageUserInfo(ERXWOContext.currentContext()).objectForKey(_key);
		}

		@Override
		public void setCacheKey(Object value) {
			ERXResponseRewriter.pageUserInfo(ERXWOContext.currentContext()).setObjectForKey(value, _key);
		}
	}

	/**
	 * AjaxPageUserInfoCacheKeyInvalidator triggers a cache invalidation when
	 * the value of the specified key changes in the ERXResponseRewriter's
	 * ajaxPageUserInfo. This is similar to PageUserInfoCacheKeyInvalidator
	 * except that it uses the ajaxPageUserInfo, so the underlying value
	 * survives across multiple ajax requests to the same page.
	 * 
	 * @author mschrag
	 */
	public static class AjaxPageUserInfoCacheKeyInvalidator extends ERXLazyValue.MutableCacheKeyInvalidator {
		private String _key;

		public AjaxPageUserInfoCacheKeyInvalidator(String key) {
			_key = key;
		}

		@Override
		protected Object cacheKey() {
			return ERXResponseRewriter.ajaxPageUserInfo(ERXWOContext.currentContext()).objectForKey(_key);
		}

		@Override
		public void setCacheKey(Object value) {
			ERXResponseRewriter.ajaxPageUserInfo(ERXWOContext.currentContext()).setObjectForKey(value, _key);
		}
	}
}
