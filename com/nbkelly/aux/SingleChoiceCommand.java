package com.nbkelly.aux;

import java.util.TreeSet;

/**
 * Extended meta-command of command to read exactly one of a collection of commands.
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 * <p>
 * The SinglechoiceCommand version allows for the recognition of exactly one of a series of commands.
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public class SingleChoiceCommand extends Command {
    /**
     * A list of subcommands which map to this command
     */
    public Command[] subCommands;

    /**
     * The match status of each subcommand
     */
    public int[] match_status;

    /**
     * The last matched command
     */
    public Command matchedCommand = null;

    /**
     * Create a new single choice command
     *
     * @param name The name of this command
     * @param subCommands The set of commands comprising this command
     */
    public SingleChoiceCommand(String name, Command... subCommands) {
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
		if(matchedCommand != null && matchedCommand != subCommands[i]) {
		    repeated++;
		    return -1;
		}
		else
		    matchedCommand = subCommands[i];		    

		return submatch;
	    }
	}

	return 0;
    }

    @Override public String usage(boolean colorEnabled, boolean supressMandatory) {
	return usage(colorEnabled);
    }
    
    @Override public String usage(boolean colorEnabled) {
	String header = "Single Choice:";
	String res = "\n";
	if(repeated > 0 || !valid())
	    res = Color.colorize(colorEnabled, "\nSingle choice rule violated", Color.RED_BOLD) + res;
	for(int i = 0; i < subCommands.length; i++) {
	    res = res + subCommands[i].usage(colorEnabled, /* suppressMandatory */ valid() && repeated == 0);
	    if(i != subCommands.length - 1)
		res = res + "\n";
	}

	return header + frontPad(res);
    }

    
    private String frontPad(String s) {
	return s.replaceAll("\n", "\n    | ");
    }

    @Override public boolean valid() {
	return matched() || !mandatory;
    }
    
    @Override public boolean matched() {
	return matchedCommand != null && matchedCommand.valid();
    }
}
