package com.nbkelly;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.FileCommand;
import com.nbkelly.aux.Timer;

import java.util.Scanner;

public class Compare extends Drafter {
    /* WORKFLOW:
     *  set all needed commands with setCommands()
     *  post-processing can be performed with actOnCommands()
     *  the rest of your work should be based around the solveProblem() function
     */

    private FileCommand inputFile;
    private FileCommand inputFile2;

    /* solve problem here */
    @Override public int solveProblem() throws Exception {
	Timer t = makeTimer();
	
	Scanner s1 = new Scanner(inputFile.value);
	Scanner s2 = new Scanner(inputFile2.value);

	int lineCount = 0;
	while(s1.hasNextLine() && s2.hasNextLine()) {
	    String first = s1.nextLine();
	    String second = s2.nextLine();

	    DEBUGF(2, "Line %d: ", lineCount);
	    if(first.equals(second))
		DEBUG(1, "match");
	    else
		printf("Mismatch at line %d: (%s,%s)%n", lineCount, first, second);
	    
	    lineCount++;
	}

	DEBUG(1, t.split("Finished Processing"));

	return 0;
    }

    
    /* set commands */
    @Override public Command[] setCommands() {
	inputFile = new FileCommand(/*name =      */ "Input File 1",
				    /*description=*/ "Auxiliary data for this program",
				    /*mandatory  =*/ true,
				    /*[synonyms] =*/ "-f1", "--file-one");	

	inputFile2 = new FileCommand(/*name =      */ "Input File 1",
				    /*description=*/ "Auxiliary data for this program",
				    /*mandatory  =*/ true,
				    /*[synonyms] =*/ "-f2", "--file-two");	

	//do you want paged input to be optional? This is mainly a debugging thing,
	//or a memory management/speed thing
	_PAGE_OPTIONAL = false; //page does not show up as a user input command
	_PAGE_ENABLED = false;  //page is set to disabled by default
	
	//return new Command[] {inputFile};
	return new Command[]{inputFile, inputFile2};
    }
    
    /* act after commands processed */
    @Override public void actOnCommands() {
	//do whatever you want based on the commands you have given
	//at this stage, they should all be resolved
    }
    
    public static void main(String[] argv) {
        new Compare().run(argv);
    }
}
