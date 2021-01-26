package com.nbkelly.aux;

/** 
 * A runtime timer, which will be enabled if the enabled var is set.
 * <p>
 * The timer takes into account (or at least attempts to) the time spent processing within the 
 * timer. The timer can generate formatted output based on given input, which can be printed by
 * other services.
 * <p>
 * The use of the 'enabled' var is primarily to facilitate debuging: if the timer is
 * not enabled (ie when debugging will not take place), none of the pre-processing is done.
 */
public class Timer {
    private final String timer_disabled = "timer disabled";
    private static final double nano_to_seconds = 1000000000;
    
    private long initTime = -1;
    private long split = -1;
    private long timeSpent = 0;    
    private boolean enabled = true;

    /** 
     * Initializes a timer, which will be enabled if the enabled var is set.
     * <p>
     * The use of the 'enabled' var is primarily to facilitate debuging: if the timer is
     * not enabled (ie when debugging will not take place), none of the pre-processing is done.
     *
     * @param enabled is this timer enabled?
     */
    public Timer(boolean enabled) {
	initTime = split = System.nanoTime();
	this.enabled = enabled;
    }

    

    /**
     * Determines the total amount of time that has passed since the last split.
     * <p>
     * Determines the total amount of time that has passed since the last split.
     * Time that has been spent within the timer has been factored out.
     *
     * @param message the name of the event
     * @return A string representing the event and the total time passed since the last split.
     * @since 1.0
     */
    public String split(String message) {
	if(!enabled)
	    return timer_disabled;
	
	long newTime = System.nanoTime();
	//get the split
	long splitTime = newTime - split;

	//convert that into something human readable
	double seconds = splitTime / nano_to_seconds;

	String res = String.format("%s - Split Time: %08.5f", message, seconds);

	//make up for the string format time
	timeSpent += splitTime;
	split = System.nanoTime();
	
	return res;
    }

    /**
     * Determines the total amount of time that has passed since the last split.
     * <p>
     * Determines the total amount of time that has passed since the last split.
     * Time that has been spent within the timer has been factored out.
     * The time is formatted based on the input string
     *
     * @param message the name of the event
     * @return A format string representing the event and the total time passed since the last split.
     * @since 1.0
     */
    public String splitf(String message) {
	if(!enabled)
	    return timer_disabled;
	
	long newTime = System.nanoTime();
	//get the split
	long splitTime = newTime - split;

	//convert that into something human readable
	double seconds = splitTime / nano_to_seconds;

	String res = String.format(message, seconds);

	//make up for the string format time
	timeSpent += splitTime;
	split = System.nanoTime();
	
	return res;
    }
    

    /**
     * Determines the total amount of time that has passed since the last split.
     * <p>
     * Determines the total amount of time that has passed since the last split.
     * Time that has been spent within the timer has been factored out.
     *
     * @return A string representing the total time passed since the last split.
     * @since 1.0
     */
    public String split() {
	if(!enabled)
	    return timer_disabled;
	
	long newTime = System.nanoTime();
	//get the split
	long splitTime = newTime - split;

	//convert that into something human readable
	double seconds = splitTime / nano_to_seconds;

	String res = String.format("Split Time: %08.5f", seconds);

	//make up for the string format time
	timeSpent += splitTime;
	split = System.nanoTime();
	
	return res;
    }

    /**
     * Determines the total amount of time that has passed since this timer was enabled.
     * <p>
     * Determines the total amount of time that has passed since this timer was enabled.
     * Time that has been spent within the timer has been factored out.
     *
     * @return A string representing the total time passed.
     * @since 1.0
     */
    public String total() {
	if(!enabled)
	    return timer_disabled;
	
	//we want the time between now and the last split
	long currentTime = System.nanoTime();	
	long splitTime = currentTime - split;
	
	
	//add to that the amount of time we've split
	splitTime += timeSpent;
	double seconds = splitTime / nano_to_seconds;
	
	long totalTime = currentTime - initTime;	
	double _timer = totalTime / nano_to_seconds;

	String res = String.format("Total Time: %08.5f (Timer Time %08.5f)", seconds, _timer);

	return res;
    }
}
