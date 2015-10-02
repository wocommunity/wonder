/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package er.extensions.foundation;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import er.extensions.formatters.ERXTimeDurationFormatter;
import er.extensions.formatters.ERXUnitAwareDecimalFormat;

/**
 * <code>StopWatch</code> provides a convenient API for timings.
 * <p>
 * To start the watch, call {@link #start()}. At this point you can:
 * <ul>
 * <li>{@link #split()} the watch to get the time whilst the watch continues in the background. {@link #unsplit()} will
 * remove the effect of the split. At this point, these three options are available again.</li>
 * <li>{@link #suspend()} the watch to pause it. {@link #resume()} allows the watch to continue. Any time between the
 * suspend and resume will not be counted in the total. At this point, these three options are available again.</li>
 * <li>{@link #stop()} the watch to complete the timing session.</li>
 * </ul>
 * It is intended that the output methods {@link #toString()} and {@link #getTime()} should only be called after stop,
 * split or suspend, however a suitable result will be returned at other points.
 * <p>
 * NOTE: As from v2.1, the methods protect against inappropriate calls. Thus you cannot now call stop before start,
 * resume before suspend or unsplit before split.
 * <ol>
 * <li>split(), suspend(), or stop() cannot be invoked twice</li>
 * <li>unsplit() may only be called if the watch has been split()</li>
 * <li>resume() may only be called if the watch has been suspend()</li>
 * <li>start() cannot be called twice without calling reset()</li>
 * </ol>
 * This class is not thread-safe
 * 
 * @author Apache Software Foundation
 * @author kieran (Sep 1, 2010) - borrowed apache lang StopWatch for ERExtensions and changed to our own time duration formatter
 * to eliminate other commons lang dependencies
 * @since 2.0
 * @version $Id: StopWatch.java 959577 2010-07-01 09:48:23Z sebb $
 */
public class ERXStopWatch {

    private static final long NANO_2_MILLIS = 1000000L;

    // running states
    private static final int STATE_UNSTARTED = 0;

    private static final int STATE_RUNNING = 1;

    private static final int STATE_STOPPED = 2;

    private static final int STATE_SUSPENDED = 3;

    // split state
    private static final int STATE_UNSPLIT = 10;

    private static final int STATE_SPLIT = 11;

    /**
     * The current running state of the StopWatch.
     */
    private int runningState = STATE_UNSTARTED;

    /**
     * Whether the stopwatch has a split time recorded.
     */
    private int splitState = STATE_UNSPLIT;

    /**
     * The start time.
     */
    private long startTime;

    /**
     * The start time in Millis - nanoTime is only for elapsed time so we 
     * need to also store the currentTimeMillis to maintain the old 
     * getStartTime API.
     */
    private long startTimeMillis;

    /**
     * The stop time.
     */
    private long stopTime;
    
    private final NumberFormat durationFormat = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.SECOND);

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public ERXStopWatch() {
        super();
    }

    /**
     * <p>
     * Start the stopwatch.
     * </p>
     * 
     * <p>
     * This method starts a new timing session, clearing any previous values.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch is already running.
     */
    public void start() {
        if (runningState == STATE_STOPPED) {
            throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
        }
        if (runningState != STATE_UNSTARTED) {
            throw new IllegalStateException("Stopwatch already started. ");
        }
        startTime = System.nanoTime();
        startTimeMillis = System.currentTimeMillis();
        runningState = STATE_RUNNING;
    }

    /**
     * <p>
     * Stop the stopwatch.
     * </p>
     * 
     * <p>
     * This method ends a new timing session, allowing the time to be retrieved.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch is not running.
     */
    public void stop() {
        if (runningState != STATE_RUNNING && runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if (runningState == STATE_RUNNING) {
            stopTime = System.nanoTime();
        }
        runningState = STATE_STOPPED;
    }

    /**
     * <p>
     * Resets the stopwatch. Stops it if need be.
     * </p>
     * 
     * <p>
     * This method clears the internal values to allow the object to be reused.
     * </p>
     */
    public void reset() {
        runningState = STATE_UNSTARTED;
        splitState = STATE_UNSPLIT;
    }

    /**
     * <p>
     * Split the time.
     * </p>
     * 
     * <p>
     * This method sets the stop time of the watch to allow a time to be extracted. The start time is unaffected,
     * enabling {@link #unsplit()} to continue the timing from the original start point.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch is not running.
     */
    public void split() {
        if (runningState != STATE_RUNNING) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        stopTime = System.nanoTime();
        splitState = STATE_SPLIT;
    }

    /**
     * <p>
     * Remove a split.
     * </p>
     * 
     * <p>
     * This method clears the stop time. The start time is unaffected, enabling timing from the original start point to
     * continue.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch has not been split.
     */
    public void unsplit() {
        if (splitState != STATE_SPLIT) {
            throw new IllegalStateException("Stopwatch has not been split. ");
        }
        splitState = STATE_UNSPLIT;
    }

    /**
     * <p>
     * Suspend the stopwatch for later resumption.
     * </p>
     * 
     * <p>
     * This method suspends the watch until it is resumed. The watch will not include time between the suspend and
     * resume calls in the total time.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch is not currently running.
     */
    public void suspend() {
        if (runningState != STATE_RUNNING) {
            throw new IllegalStateException("Stopwatch must be running to suspend. ");
        }
        stopTime = System.nanoTime();
        runningState = STATE_SUSPENDED;
    }

    /**
     * <p>
     * Resume the stopwatch after a suspend.
     * </p>
     * 
     * <p>
     * This method resumes the watch after it was suspended. The watch will not include time between the suspend and
     * resume calls in the total time.
     * </p>
     * 
     * @throws IllegalStateException
     *             if the StopWatch has not been suspended.
     */
    public void resume() {
        if (runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch must be suspended to resume. ");
        }
        startTime += (System.nanoTime() - stopTime);
        runningState = STATE_RUNNING;
    }

    /**
     * <p>
     * Get the time on the stopwatch.
     * </p>
     * 
     * <p>
     * This is either the time between the start and the moment this method is called, or the amount of time between
     * start and stop.
     * </p>
     * 
     * @return the time in milliseconds
     */
    public long getTime() {
        return getNanoTime() / NANO_2_MILLIS;
    }
    
    /**
     * <p>
     * Get the time on the stopwatch in nanoseconds.
     * </p>
     * 
     * <p>
     * This is either the time between the start and the moment this method is called, or the amount of time between
     * start and stop.
     * </p>
     * 
     * @return the time in nanoseconds
     * @since 3.0
     */
    public long getNanoTime() {
        if (runningState == STATE_STOPPED || runningState == STATE_SUSPENDED) {
            return stopTime - startTime;
        } else if (runningState == STATE_UNSTARTED) {
            return 0;
        } else if (runningState == STATE_RUNNING) {
            return System.nanoTime() - startTime;
        }
        throw new RuntimeException("Illegal running state has occured. ");
    }

    /**
     * <p>
     * Get the split time on the stopwatch.
     * </p>
     * 
     * <p>
     * This is the time between start and latest split.
     * </p>
     * 
     * @return the split time in milliseconds
     * 
     * @throws IllegalStateException
     *             if the StopWatch has not yet been split.
     * @since 2.1
     */
    public long getSplitTime() {
        return getSplitNanoTime() / NANO_2_MILLIS;
    }
    
    /**
     * <p>
     * Get the split time on the stopwatch in nanoseconds.
     * </p>
     * 
     * <p>
     * This is the time between start and latest split.
     * </p>
     * 
     * @return the split time in nanoseconds
     * 
     * @throws IllegalStateException
     *             if the StopWatch has not yet been split.
     * @since 3.0
     */
    public long getSplitNanoTime() {
        if (splitState != STATE_SPLIT) {
            throw new IllegalStateException("Stopwatch must be split to get the split time. ");
        }
        return stopTime - startTime;
    }

    /**
     * Returns the time this stopwatch was started.
     * 
     * @return the time this stopwatch was started
     * @throws IllegalStateException
     *             if this StopWatch has not been started
     * @since 2.4
     */
    public long getStartTime() {
        if (runningState == STATE_UNSTARTED) {
            throw new IllegalStateException("Stopwatch has not been started");
        }
        // System.nanoTime is for elapsed time
        return startTimeMillis;
    }
//
//    /**
//     * <p>
//     * Gets a summary of the time that the stopwatch recorded as a string.
//     * </p>
//     * 
//     * <p>
//     * The format used is ISO8601-like, <i>hours</i>:<i>minutes</i>:<i>seconds</i>.<i>milliseconds</i>.
//     * </p>
//     * 
//     * @return the time as a String
//     */
//    @Override
//    public String toString() {
//        return DurationFormatUtils.formatDurationHMS(getTime());
//    }
    
    /**
     * Our modified implementation since apache commons lang jar not allowed in ERJars.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return durationFormat().format(getTime());
    }

//    /**
//     * <p>
//     * Gets a summary of the split time that the stopwatch recorded as a string.
//     * </p>
//     * 
//     * <p>
//     * The format used is ISO8601-like, <i>hours</i>:<i>minutes</i>:<i>seconds</i>.<i>milliseconds</i>.
//     * </p>
//     * 
//     * @return the split time as a String
//     * @since 2.1
//     */
//    public String toSplitString() {
//        return DurationFormatUtils.formatDurationHMS(getSplitTime());
//    }
    
    
    public String toSplitString() {
        return durationFormat().format(getSplitTime());
    }
    
    private ERXTimeDurationFormatter _durationFormat;
	
	/** @return the formatter for the elapsed time */
	public ERXTimeDurationFormatter durationFormat() {
		if ( _durationFormat == null ) {
			_durationFormat = new ERXTimeDurationFormatter(TimeUnit.MILLISECONDS, false, false);
		}
		return _durationFormat;
	}

}
