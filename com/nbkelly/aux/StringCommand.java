package com.nbkelly.aux;

public class StringCommand extends Command {
    public String value;
    
    public StringCommand(String defaultValue, boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = defaultValue;
	this.takesInput = true;
	this.type = "String";
    }

    public int match(String[] argv, int index) {
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
    
    public String getValue() {
	return value;
    }
}
