package com.nbkelly.aux;

public class BooleanCommand extends Command {
    public boolean value;
    
    public BooleanCommand(boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = false;
    }

    public int match(String[] argv, int index) {
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
    
    public boolean getValue() {
	return value;
    }
}
