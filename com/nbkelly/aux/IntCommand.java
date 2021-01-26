package com.nbkelly.aux;

/**
 * Extention of command to read integers within a given range.
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The IntCommand version allows for the recognition of Integers within a given range, and takes one input
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class IntCommand extends Command {
    /**
     * The value of this command - this may have a value even when no match has been made
     */
    public int value;

    /**
     * The minimum acceptable value for this command
     */
    public int min;

    /**
     * The maximum acceptable value for this command
     */
    public int max;

    /**
     * Create a new IntCommand. 
     *
     * @param min The minimum acceptable value for this command
     * @param max The maximum acceptable value for this command
     * @param mandatory Is this command mandatory?
     * @param defaultValue The default value for this command
     * @param synonyms The set of synonyms that can be used to define this command
     */
    public IntCommand(int min, int max, boolean mandatory,
		      int defaultValue, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = defaultValue;
	this.min = min;
	this.max = max;
	this.takesInput = true;
	this.type = "Integer";
    }

    @Override public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    if(index + 1 < argv.length) {
		try {
		    int res = Integer.parseInt(argv[index+1]);
		    if(res >= min && res <= max) {
			value = res;		
			matched++;
			return index + 2; //matches and valid
		    }			
		}
		catch (Exception e) { } //it's marked as invalid regardless		    
	    }
	    invalid++;
	    return -1; //matches but invalid
	}

	//can't match the same argument more than once (with this system)
	if (matched > 0 && synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}	    

	return 0; //doesnt match
    }

    /**
     * Return the current Integer value for this command
     */
    public int getValue() {
	return value;
    }
}
