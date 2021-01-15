package com.nbkelly.aux;

public class IntCommand extends Command {
    public int value;
    public int min;
    public int max;
    
    public IntCommand(int min, int max, boolean mandatory, int defaultValue, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = defaultValue;
	this.min = min;
	this.max = max;
	this.takesInput = true;
	this.type = "Integer";
    }
    
    public int match(String[] argv, int index) {
	String cmd = argv[index];
	if(matched == 0 && synonyms.contains(cmd)) { //don't match if already matched
	    if(index + 1 < argv.length) {
		try {
		    int res = Integer.parseInt(argv[index+1]);
		    if(res >= min && res <= max) {
			value = res;		
			matched++;
			return index + 2; //matches and valid
		    }			
		}
		catch (Exception e) { } //it's marked as invalid regardless		    
	    }
	    invalid++;
	    return -1; //matches but invalid
	}

	//can't match the same argument more than once (with this system)
	if (matched > 0 && synonyms.contains(cmd)) {
	    repeated++;
	    return -1;
	}	    

	return 0; //doesnt match
    }
    
    public int getValue() {
	return value;
    }
}
