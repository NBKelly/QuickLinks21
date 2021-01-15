package com.nbkelly.aux;

import java.util.TreeSet;

public abstract class Command {
    public TreeSet<String> synonyms = new TreeSet<String>();
    public int count = 0;
    public boolean mandatory = false;
    public int matched = 0;
    protected int repeated = 0;
    protected int invalid = 0;
    private String name = "";
    private String description = "";
    protected boolean takesInput = false;
    protected String type = "generic";
    protected boolean terminal = false;

    public String getName() {
	return name;
    }
    
    public boolean isTerminal() { return terminal; }

    @SuppressWarnings("unchecked")
    public <Sneed extends Command> Sneed setTerminal() {
	terminal = true;
	return (Sneed)this;
    }
    
    //by default this is empty
    public Command addSynonyms(String... args) {
	for(int i = 0; i < args.length; i++)
	    synonyms.add(args[i]);

	return this;
    }

    //by default this is false
    public Command setMandatory(boolean mandatory) {
	this.mandatory = mandatory;

	return this;
    }

    public boolean matched() {
	return matched >= 1;
    }
    
    public abstract int match(String[] argv, int index);

    public boolean invalid() {
	return mandatory && matched == 0 || repeated > 0 || invalid > 0;
    }

    public boolean valid() {
	return true;
    }
    
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

    
    //public abstract String us
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

    @SuppressWarnings("unchecked")
    public <T extends Command> T setName(String name) {
	this.name = name;
	return (T)this;
    }

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
