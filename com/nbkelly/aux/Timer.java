package com.nbkelly.aux;

public class Timer {
    private final String timer_disabled = "timer disabled";
    //TODO: get rid of this main function later
    public static void main(String[] argv) {
	Timer t = new Timer(true);

	for(int i = 0; i < 100000; i++) {
	    int a = i * i * i;
	    if(i % 10000 == 0)
		System.out.println(t.split());
	}

	System.out.println(t.total());
    }
    
    private static final double nano_to_seconds = 1000000000;
    
    private long initTime = -1;
    private long split = -1;
    private long timeSpent = 0;    
    private boolean enabled = true;

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
