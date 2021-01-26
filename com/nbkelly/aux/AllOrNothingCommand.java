package com.nbkelly.aux;

import java.util.TreeSet;

/**
 * Extended meta-command of command to read all of, or none of, a collection of commands.
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The AllOrNothing version allows for the recognition of exactly all, or none, of a series of commands.
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class AllOrNothingCommand extends Command {
    /**
     * A list of subcommands which map to this command
     */
    public Command[] subCommands;

    /**
     * The match status of each subcommand
     */
    public int[] match_status;

    /**
     * Create a new all-or-nothing command
     *
     * @param name The name of this command
     * @param subCommands The set of commands comprising this command
     */
    public AllOrNothingCommand(String name, Command... subCommands) {
	if(subCommands.length == 0)
	    throw new IllegalArgumentException("All or Nothing given with zero inputs");
	
	this.subCommands = subCommands;
	this.match_status = new int[subCommands.length];
	mandatory = true;
	setName(name);
    }

    @Override public int match(String[] argv, int index) {
	for(int i = 0; i < subCommands.length; i++) {
	    //get the previous match
	    int pre = match_status[i];

	    //see if we match anything here
	    if(subCommands[i] == null)
		throw new IllegalArgumentException("command is null");
	    int submatch = subCommands[i].match(argv, index);
	    if(submatch < 0) {
		match_status[i] = submatch;
		invalid++;
		return submatch;		
	    }
	    else if(submatch > 0) {
		match_status[i] = submatch;
		return submatch;
	    }
	}

	return 0;
    }

    @Override public String usage(boolean colorEnabled, boolean supressMandatory) {
	return usage(colorEnabled);
    }
    
    @Override public String usage(boolean colorEnabled) {
	String header = "All or Nothing:";
	String res = "\n";
	if(!valid())
	    res = Color.colorize(colorEnabled, "\nAll or Nothing rule violated", Color.RED_BOLD) + res;	
	for(int i = 0; i < subCommands.length; i++) {
	    res = res + subCommands[i].usage(colorEnabled, /* suppressMandatory */ valid());
	    if(i != subCommands.length - 1)
		res = res + "\n";
	}

	return header + frontPad(res);
    }
    
    private String frontPad(String s) {
	return s.replaceAll("\n", "\n    | ");
    }

    @Override public boolean valid() {
	return matched();
    }

    @Override public boolean matched() {
	int hits = 0;
	for(int i = 0; i < match_status.length; i++) {
	    if(match_status[i] > 0) {
		if(subCommands[i].valid())
		    hits++;
		else
		    return false;
	    }
	    else if (match_status[i] < 0)
		return false;
	}

	return (hits == 0 || hits == match_status.length);	
    }
}
