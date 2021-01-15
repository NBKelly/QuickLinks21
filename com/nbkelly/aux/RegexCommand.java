package com.nbkelly.aux;

public class RegexCommand extends Command {
    public String value;
    public final String regex;

    public RegexCommand(String defaultValue, boolean mandatory, String regex, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = defaultValue;
	this.takesInput = true;
	this.type = String.format("String (regex)", regex);
	this.regex = regex;
    }

    public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    if(index + 1 < argv.length) {
		if(argv[index + 1].matches(regex)) {
		    matched++;
		    value = argv[index+1];
		    return index+2;
		}
	    }
	    return -1; //matches but invalid	    
	}

	if(synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}

	return 0; //doesnt match
    }
    
    public String getValue() {
	return value;
    }
}
