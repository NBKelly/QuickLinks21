package com.nbkelly.aux;

import java.io.File;

public class FileCommand extends Command {
    public File value;
        
    public FileCommand(boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = null;
	this.takesInput = true;
	this.type = "FileName";
    }

    public FileCommand(String name, String description, boolean mandatory, String... synonyms) {
	addSynonyms(synonyms).setMandatory(mandatory);
	this.value = null;
	this.takesInput = true;
	this.type = "FileName";

	setName(name);
	setDescription(description);
    }
    
    public int match(String[] argv, int index) {
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
    
    public File getValue() {
	return value;
    }
}
