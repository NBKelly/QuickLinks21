package com.nbkelly.aux;

import java.io.File;

/**
 * Extention of command to recognize files
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The FileCommand version allows for the recognition of Files through the second argument, 
 * and does not assert that a file already exists
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class FileCommand extends Command {
    /**
     * The value of this command - this defaults to null
     */
    public File value;

    /**
     * Create a new file command
     *
     * @param mandatory Is this command mandatory?
     * @param synonyms The set of synonyms that can be used to define this command
     */
    public FileCommand(boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = null;
	this.takesInput = true;
	this.type = "FileName";
    }

    /**
     * Create a new file command
     *
     * @param name The name of this command
     * @param description The description of this command
     * @param mandatory Is this command mandatory?
     * @param synonyms The set of synonyms that can be used to define this command
     */
    public FileCommand(String name, String description, boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = null;
	this.takesInput = true;
	this.type = "FileName";

	setName(name);
	setDescription(description);
    }
    
    @Override public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    if(index + 1 < argv.length) {
		String path = argv[index+1];
		File f = new File(path);
		matched++;
		if (f.exists() && f.isFile()) {
		    value = f;		    
		    return index + 2;
		}
	    }
	    invalid++;
	    return -1; //matches but invalid	    
	}

	if(synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}

	return 0; //doesnt match
    }

    /**
     * Return the current File value for this command
     */
    public File getValue() {
	return value;
    }
}
