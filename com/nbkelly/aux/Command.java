package com.nbkelly.aux;

import java.util.TreeSet;

/**
 * A command interprets arguments arrays into variables.
 * <p>
 * A command interprets arguments arrays into variables. It is capable of self-matching on an argument array,
 * updating the given input, and recognizing errors or duplicate inputs.
 *
 * @author  NB Kelly <N.B.Kelly@protonmail.com>
 * @version 1.0
 * @since   1.0 
 */
public abstract class Command {

    /**
     * All of the synonyms which can be used to address this command.     
     */
    public TreeSet<String> synonyms = new TreeSet<String>();        

    /**
     * Is this command mandatory?
     */
    public boolean mandatory = false;

    /**
     * Number of times this command has been matched
     */
    public int matched = 0;

    /**
     * Number of repeat inputs given to this command
     */
    protected int repeated = 0;

    /**
     * Number of invalid inputs given to this command
     */
    protected int invalid = 0;

    /**
     * The name of the command
     */
    private String name = "";

    /** 
     * A description of the command.
     */
    private String description = "";

    /**
     * Does this command require an input argument?
     */
    protected boolean takesInput = false;

    /**
     * The type of input this command takes (if any). Default is 'generic'.
     */
    protected String type = "generic";

    /**
     * Is this command terminal?
     * <p>
     * If a command is terminal, 
     * then after it has been matched, no other commands may be matched.
     */
    protected boolean terminal = false;

    /**
     * Gets the name of this command.
     *
     * @return The name of this command.
     */
    public String getName() {
	return name;
    }

    /**
     * Is this command terminal?
     * <p>
     * A command is terminal if, on being matched, no further input is allowed.
     *
     * @return True is the command is terminal
     */
    public boolean isTerminal() { return terminal; }

    /**
     * Sets a command to be terminal.
     * <p>
     * A command is terminal if, on being matched, no further input is allowed.
     *
     * @return The same command, but terminal
     */
    @SuppressWarnings("unchecked")
    public <Sneed extends Command> Sneed setTerminal() {
	terminal = true;
	return (Sneed)this;
    }
    
    /**
     * Adds a set of synonyms to this command
     * 
     * @param args a list of synonyms. They do not need to be unique, but they should not match the synonyms of any other command
     * @return The same command, but with synonyms added
     */
    public Command addSynonyms(String... args) {
	for(int i = 0; i < args.length; i++)
	    synonyms.add(args[i]);

	return this;
    }

    /**
     * Sets the mandatory status of this command
     * <p>
     * If a command is mandatory, the program will not run unless it has been specified (and some pretty output will be displayed when you fail to run the program).
     * @param mandatory is this command mandatory
     * @return The same command, but with mandatory set.
     */
    public Command setMandatory(boolean mandatory) {
	this.mandatory = mandatory;

	return this;
    }

    /**
     * Has this command matched some input?
     *
     * @return True if this command has matched some input
     */
    public boolean matched() {
	return matched >= 1;
    }

    /**
     * Given a list of arguments, and an index, attempts to match the argument at this index.
     * @param argv the input arguments for the program
     * @param index the current index of matching
     * @return (new_index gt 0) if there was a match, 0 if there was no match, or -1 if there was an error
     */
    public abstract int match(String[] argv, int index);

    /**
     * Returns wether or not the command is invalid.
     * <p>
     * A command is invalid if it has been matched more than once, or is mandatory and has not been matched,
     *  or if invalid input has been given for the command.
     *
     * @return True if the command is invalid
     */
    public boolean invalid() {
	return mandatory && matched == 0 || repeated > 0 || invalid > 0;
    }

    /**
     * Returns wether or not the command is valid.
     * <p>
     * It is worth noting that valid != -invalid: valid refers to the completion status of multi-part commands
     * (and should probably be renamed for that), and invalid refers to the match status of single-part commands
     *
     * @return True if the command is valid
     */
    public boolean valid() {
	return true;
    }

    /**
     * Gets the usage tag for this command, suppressing mandatory text.
     * <p>
     * Errors will be highlighted, and it will be noted if mandatory arguments have no been filled. 
     *  If colorEnabled is true, it will also color pieces where errors have occured.
     *
     * @param colorEnabled true if color is enabled
     * @return The usage text of this command
     */
    public String usage(boolean colorEnabled, boolean suppressMandatory) {
	//return: name : list of synonyms - mandatory - description
	String res = name;
	res += " : { ";

	for(String s : synonyms)
	    res += s + " ";

	if(takesInput)
	    res += "} [" + type + "]";
	else
	    res += "}";

	
	res += "\n    | " + (mandatory ? "Mandatory":"Optional");
	if(takesInput)
	    res += "\n    | Expects Argument of type [" + type + "]";// + takesInput;
	res += "\n    | > " + description;

	//check if this one is invalid
	if(mandatory && matched == 0 || repeated > 0 || invalid > 0) {
	    String tag = "";
	    //determine on a case by case basis
	    if(!suppressMandatory)
		if(mandatory && matched == 0)
		    tag = String.format("ERROR: mandatory argument '%s' not supplied%n%s", name, tag);
	    if(repeated > 0)
		tag = String.format("ERROR: argument '%s' supplied more than once%n%s", name, tag);
	    if(invalid > 0)
		tag = String.format("ERROR: input for argument '%s' is invalid%n%s", name, tag);
	    
	    res = Color.colorize(colorEnabled, tag, Color.RED_BOLD) + res;
	}
	
	return res;
    }

    
    /**
     * Gets the usage tag for this command.
     * <p>
     * Errors will be highlighted, and it will be noted if mandatory arguments have no been filled. 
     *  If colorEnabled is true, it will also color pieces where errors have occured.
     *
     * @param colorEnabled true if color is enabled
     * @return The usage text of this command
     */
    public String usage(boolean colorEnabled) {
	//return: name : list of synonyms - mandatory - description
	String res = name;
	res += " : { ";

	for(String s : synonyms)
	    res += s + " ";

	if(takesInput)
	    res += "} [" + type + "]";
	else
	    res += "}";

	res += "\n    | " + (mandatory ? "Mandatory":"Optional");
	if(takesInput)
	    res += "\n    | Expects Argument of type [" + type + "]";// + takesInput;
	res += "\n    | > " + description;

	//check if this one is invalid
	if(mandatory && matched == 0 || repeated > 0 || invalid > 0) {
	    String tag = "";
	    //determine on a case by case basis
	    if(mandatory && matched == 0)
		tag = String.format("ERROR: mandatory argument '%s' not supplied%n%s", name, tag);
	    if(repeated > 0)
		tag = String.format("ERROR: argument '%s' supplied more than once%n%s", name, tag);
	    if(invalid > 0)
		tag = String.format("ERROR: input for argument '%s' is invalid%n%s", name, tag);
	    
	    res = Color.colorize(colorEnabled, tag, Color.RED_BOLD) + res;
	}
	
	return res;
    }

    /**
     * Sets the name of a command
     *
     * @return The same command, but named
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> T setName(String name) {
	this.name = name;
	return (T)this;
    }

    /**
     * Sets the description on this command
     *
     * @return The same command, but described
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> T setDescription(String desc) {
	this.description = wrapString(desc, "\n", 80).replaceAll("\\n", "\n    | > ");
	return (T)this;
    }

    private static String wrapString(String s, String deliminator, int length) {
	String result = "";
	int lastdelimPos = 0;
	for (String token : s.split(" ", -1)) {
	    if (result.length() - lastdelimPos + token.length() > length) {
		result = result + deliminator + token;
		lastdelimPos = result.length() + 1;
	    }
	    else {
		result += (result.isEmpty() ? "" : " ") + token;
	    }
	}
	return result;
    }
}
