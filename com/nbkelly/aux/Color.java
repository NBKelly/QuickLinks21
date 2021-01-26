package com.nbkelly.aux;

/**
 * A range of terminal color codes by name
 */
public enum Color {
    //Color end string, color reset
    /**
     * Reset color to standard
     */
    RESET("\033[0m"),

    // Regular Colors. Normal color, no bold, background color etc.
    /**
     * Sets text color to black
     */
    BLACK("\033[0;30m"),    // BLACK
    /**
     * Sets text color to red
     */
    RED("\033[0;31m"),      // RED
    /**
     * Sets text color to green
     */
    GREEN("\033[0;32m"),    // GREEN
    /**
     * Sets text color to yellow
     */
    YELLOW("\033[0;33m"),   // YELLOW
    /**
     * Sets text color to blue
     */
    BLUE("\033[0;34m"),     // BLUE
    /**
     * Sets text color to magenta
     */
    MAGENTA("\033[0;35m"),  // MAGENTA
    /**
     * Sets text color to cyan
     */
    CYAN("\033[0;36m"),     // CYAN
    /**
     * Sets text color to white
     */
    WHITE("\033[0;37m"),    // WHITE

    // Bold versions
    /**
     * Sets text color to black and bold
     */
    BLACK_BOLD("\033[1;30m"),   // BLACK
    /**
     * Sets text color to red and bold
     */
    RED_BOLD("\033[1;31m"),     // RED
    /**
     * Sets text color to green and bold
     */
    GREEN_BOLD("\033[1;32m"),   // GREEN
    /**
     * Sets text color to yellow and bold
     */
    YELLOW_BOLD("\033[1;33m"),  // YELLOW
    /**
     * Sets text color to blue and bold
     */
    BLUE_BOLD("\033[1;34m"),    // BLUE
    /**
     * Sets text color to magenta and bold
     */
    MAGENTA_BOLD("\033[1;35m"), // MAGENTA
    /**
     * Sets text color to cyan and bold
     */
    CYAN_BOLD("\033[1;36m"),    // CYAN
    /**
     * Sets text color to white and bold
     */
    WHITE_BOLD("\033[1;37m");   // WHITE

    private final String code;
    private Color(String code) {
        this.code = code;
    }

    /**
     * Gets the ASNI terminal color code associated with this color
     */
    @Override public String toString() {
        return code;
    }

    /**
     * Given a string and a color, colorizes a string based on wether color is enabled
     *
     * @param enabled Is colorization enabled
     * @param s String to colorize
     * @param c Color to paint the string
     * @return a string that may or may not be colorized
     */
    public static String colorize(boolean enabled, String s, Color c) {
	if(!enabled)
	    return s;
	
	if(c == RESET)
	    return s;

	return String.format("%s%s%s", c.toString(), s, RESET.toString());
    }
}
