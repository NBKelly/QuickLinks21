package com.nbkelly.aux;

/**
 * Extention of command to set boolean values
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The BooleanCommand version allows for the recognition of Boolean variables through a set-once interface
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class BooleanCommand extends Command {
    /**
     * The value of this command - this defaults to false, and becomes true when a match has been made
     */
    public boolean value;

    /**
     * Create a new set-once boolean command
     *
     * @param mandatory Is this command mandatory?
     * @param synonyms The set of synonyms that can be used to define this command
     */
    public BooleanCommand(boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = false;
    }

    @Override public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    matched++;
	    value = !value;
	    return index+1;	
	}

	if(synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}
	return 0; //doesnt match
    }

    /**
     * Return the current Boolean value for this command
     */
    public boolean getValue() {
	return value;
    }
}
