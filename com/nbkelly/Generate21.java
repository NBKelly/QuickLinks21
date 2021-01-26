package com.nbkelly;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.IntCommand;
import com.nbkelly.aux.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Extension of Drafter directed towards a general case.
 *
 * @see <a href="https://nbkelly.github.io/Drafter/com/nbkelly/package-summary.html" target="_top">
 * here</a> for the up to date online javadocs
 */
public class Generate21 extends Drafter {
    /* WORKFLOW:
     *  set all needed commands with setCommands()
     *  post-processing can be performed with actOnCommands()
     *  the rest of your work should be based around the solveProblem() function
     */
    //private FileCommand inputFile;
    private IntCommand nodeCountCommand;
    private IntCommand chunkSizeCommand;
    
    Random rand = new Random(2020);

    private int checks_done = 0;
    /* solve problem here */
    @Override public int solveProblem() throws Exception {
	Timer t = makeTimer();
	
	//types:
	//cycle (lots of big cycles)
	//thorns (cycle with lots of trees)
	//broom (lots of straight strands)
	//tree (an actual tree, very big)	
	int index = 0;
	int size = nodeCountCommand.value; //8000000;
	int component_size = chunkSizeCommand.value; //200000;
	int check_multiplier = 3;

	ArrayList<Integer> intervals = new ArrayList<Integer>();
	
	while(index < size) {
	    int start = index;
	    index = generate_component(component_size, index);
	    if(start != index) {
		intervals.add(start);
		intervals.add(index);
	    }

	    DEBUGF("SI: %d -> %d%n", start, index);
	}

	
	intervals.add(0);
	intervals.add(index);
	intervals.add(0);
	intervals.add(index);
	intervals.add(0);
	intervals.add(index);
	intervals.add(0);
	intervals.add(index);
	intervals.add(0);
	intervals.add(index);
	
	
	println();
	for(int i = 0; i < intervals.size(); i += 2) {
	    int start = intervals.get(i);
	    int end = intervals.get(i+1);
	    //checks_done += diff;
	    DEBUGF("%d <-> %d%n", start, end);
	    generate_checks(start, end, component_size * check_multiplier);
	}

	DEBUGF(1, "echo '%d %d' | cat - sample_problem.txt > temp && mv temp sample_problem.txt%n",
	       index,
	       checks_done);
	DEBUG(1, t.split("Finished Processing"));
	
	return 0;
    }

    public ArrayList<Integer> generate_checks(int start, int end, int count) {
	int diff = end - start;
	ArrayList<Integer> res = new ArrayList<Integer>();

	for(int i = 0; i < count; i++) {
	    int from = rand.nextInt(diff) + start;
	    int to = rand.nextInt(diff) + start;
	    
	    //res.add(from);
	    //res.add(to);

	    printf("%d %d%n", from, to);
	    checks_done++;
	}

	return res;
    }
    
    public int generate_component(int size, int start) {
	int selection = rand.nextInt(5);
	int base_select = rand.nextInt(3);
	int base = 0;

	switch(base_select) {
	case 0:
	    base = 0;
	    break;
	case 1:
	    if(start != 0)
		base = rand.nextInt(start);
	    break;
	case 2:
	    base = start;
	    break;
	}
	
	switch(selection) {
	case 0:
	    return generate_cycle(size, start);
	case 1:
	    return generate_tree(base, size, start);
	case 2:
	    return generate_strand(base, size, start);
	case 3:
	    int cs = size/30;
	    int tree_count = cs * 2;
	    int tree_size = 15;
	    return generate_thorns(cs, tree_count, tree_size, start);
	case 4:
	    int strand_size = 15;
	    return generate_strand_tree(base, size, start, strand_size);
	default:
	    DEBUG("missed the post");
	    return start;
	}
    }
    
    public int generate_thorns(int cycle_size, int tree_count, int tree_size, int start) {
	//first we generate a cycle of size n/3
	int index = start;
	
	index = generate_cycle(cycle_size, index);

	//anything between start and start + cycle-size is fair game
	//generate trees of size ~tree_size starting at random values in the cycle
	//we basically want (on average) one tree for every node, and each tree should have 9 values
	for(int i = 0; i < tree_count; i++) {
	    int base = rand.nextInt(cycle_size) + start; //between start and start + cycle_size
	    if(rand.nextBoolean())
		index = generate_tree(base, tree_size, index);
	    else
		index = generate_strand(base, tree_size, index);	    
	}

	return index;
    }
    
    public int transform(int val) {
	return val;
    }

    public int generate_strand_tree(int base, int size, int start, int strand_size) {
	ArrayList<Integer> withinTree = new ArrayList<Integer>();

	int index = start;
	withinTree.add(start);
	
	printf("%d ", base);
	//start -> base
	
	index++;

	while(index < start + size) {
	    //pick a random element within our tree to act as the base
	    int elem = withinTree.get(rand.nextInt(withinTree.size()));
	    index = generate_strand(elem, strand_size, index);
	    //last item in the strand was the one at index-1
	    withinTree.add(index-1);
	}

	return index;
    }
    
    public int generate_strand(int base, int size, int start) {
	int index = start;
	printf("%d ", base);
	index++;

	while(index < start + size) {
	    printf("%d ", index - 1);
	    index++;
	}

	return index;
    }
    
    public int generate_tree(int base, int size, int start) {
	ArrayList<Integer> withinTree = new ArrayList<Integer>();
	
	//the first thing to do is link start to base
	int index = start;
	withinTree.add(start);

	printf("%d ", base);
	//start -> base

	index++;
	
	while(index < start + size) {
	    //pick a random element from our tree to act as a base
	    int elem = withinTree.get(rand.nextInt(withinTree.size()));
	    printf("%d ", elem);
	    withinTree.add(index);
	    index++;
	}

	assert index == start + size;
	
	return index;
    }
    
    public int generate_cycle(int size, int start) {
	//make an arraylist, fill it with all the values we want
	//then shuffle it
	//each item gives the address of the next item, and the last item gives the address of 0th item
	ArrayList<Integer> cycle = new ArrayList<Integer>();
	for(int index = start; index < start + size; index++)
	    cycle.add(index);
	
	for(int index = 0; index < cycle.size(); index++) {
	    printf("%d ", transform(cycle.get((index + 1) % cycle.size())));
	}

	return start + size;
    }
    
    /* set commands */
    @Override public Command[] setCommands() {
	//inputFile = new FileCommand(/*name =      */ "Input File",
	//			    /*description=*/ "Auxiliary data for this program",
	//			    /*mandatory  =*/ true,
	//			    /*[synonyms] =*/ "-f", "--file");	

	//do you want paged input to be optional? This is mainly a debugging thing,
	//or a memory management/speed thing
	_PAGE_OPTIONAL = false; //page does not show up as a user input command
	_PAGE_ENABLED = false;  //page is set to disabled by default

	nodeCountCommand = new IntCommand(50, 100000000, true, 5000, "--node-count", "-nc")
	    .setName("Node Count");
	chunkSizeCommand = new IntCommand(50, 1000000, true, 5000, "--chunk-size", "-cs")
	    .setName("Chunk Size")
	    .setDescription("Ideal = 10% or less of node-count");
	
	//return new Command[] {inputFile};
	return new Command[] { nodeCountCommand, chunkSizeCommand };
    }
    
    /* act after commands processed */
    @Override public void actOnCommands() {
	//do whatever you want based on the commands you have given
	//at this stage, they should all be resolved
    }

    /**
     * Creates and runs an instance of your class - do not modify
     */
    public static void main(String[] argv) {
        new Generate21().run(argv);
    }
}
