package com.nbkelly.aux;

/**
 * Extention of command to read Strings within a given range.
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The StringCommand version allows for the recognition of Strings, and takes one input (which may be blank)
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class StringCommand extends Command {
    /**
     * The value of this command - this may have a value even when no match has been made
     */
    public String value;

    /**
     * Create a new IntCommand. 
     *
     * @param mandatory Is this command mandatory?
     * @param defaultValue The default value for this command
     * @param synonyms The set of synonyms that can be used to define this command
     */
    public StringCommand(String defaultValue, boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = defaultValue;
	this.takesInput = true;
	this.type = "String";
    }

    @Override public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    if(index + 1 < argv.length) {
		matched++;
		value = argv[index+1];
		return index+2;
	    }
	    return -1; //matches but invalid	    
	}

	if(synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}

	return 0; //doesnt match
    }

    /**
     * Return the current String value for this command
     */
    public String getValue() {
	return value;
    }
}
