package com.nbkelly;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.FileCommand;
import com.nbkelly.aux.AllOrNothingCommand;
import com.nbkelly.aux.SingleChoiceCommand;
import com.nbkelly.aux.IntCommand;
import com.nbkelly.aux.Timer;

import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

public class QuickLinks21 extends Drafter {
    /* WORKFLOW:
     *  set all needed commands with setCommands()
     *  post-processing can be performed with actOnCommands()
     *  the rest of your work should be based around the solveProblem() function
     */

    /* commands */
    private IntCommand generatedNodes;
    private IntCommand generatedChecks;
    private AllOrNothingCommand generateInput;
    private SingleChoiceCommand inputCommand;    
    private FileCommand inputFile;

    /* vars */
    
    /* solve problem here */
    @Override public int solveProblem() throws Exception {
	Timer t = makeTimer();
	
	

	ArrayList<String> input = getInput();	

	DEBUG(1, t.split("Retrieved Input"));

	String fl = input.get(0);
	int N = Integer.parseInt(fl.split(" ")[0]);
	int K = Integer.parseInt(fl.split(" ")[1]);
	
	DEBUGF(2, "N:%d K:%d%n", N, K);
	
	/* nodes -> dest */
	int[] nodes = new int[N];	
	for(int i = 0; i < N; i++)
	    nodes[i] = Integer.parseInt(input.get(1 + i));

	DEBUG(1, t.split("Mapped Nodes"));
	DEBUG(2, (a2s(nodes)));

	/* construct all cycles */
	ArrayList<Cycle> cycles = determineCycles(nodes);
	DEBUG(1, t.split("Constructed Cycles"));
	
	/* all nodes that are within cycles */
	HashSet<Integer> withinCycle = withinCycle(cycles);
	DEBUG(1, t.split("Separated all nodes contained within cycles"));
	
	/* reverse map the node list */
	ArrayList<ArrayList<Integer>> reverse = reverseMap(nodes);
	DEBUG(1, t.split("Performed a reverse-mapping on all nodes"));
	
	/* determine all cycle entry points */
	DEBUG(2, "Entry Points:");
	ArrayList<Integer> cycleEntryPoints = cycleEntryPoints(reverse, withinCycle);
	printList(cycleEntryPoints, 2);
	DEBUG(1, t.split("Determined all cycle entry points"));
	
	/* construct all strands */
	      
	DEBUG(1, t.split("Finished Processing"));
	      
	return 0;
    }

    
    private ArrayList<Integer> cycleEntryPoints(ArrayList<ArrayList<Integer>> reverseMapping, HashSet<Integer> withinCycle) {
	ArrayList<Integer> cycleEntryPoints= new ArrayList<Integer>();
	
	for(int to = 0; to < reverseMapping.size(); to++) {
	    ArrayList<Integer> entryPoints = reverseMapping.get(to);
	    if(entryPoints.size() > 1 && withinCycle.contains(to))
		cycleEntryPoints.add(to);
	}

	return cycleEntryPoints;
    }

    /* O(N) : no node is examined more than once */
    private ArrayList<ArrayList<Integer>> reverseMap(int[] nodes) {
	ArrayList<ArrayList<Integer>> map = new ArrayList<>();

	for(int i = 0; i < nodes.length; i++)
	    map.add(new ArrayList<Integer>());

	for(int from = 0; from < nodes.length; from++) {
	    //to -> [from]
	    map.get(nodes[from]).add(from);
	}

	return map;
    }

    /* This is O(N) I think? is keyset() O(N)? */
    private HashSet<Integer> withinCycle(ArrayList<Cycle> cycles) {
	HashSet<Integer> within = new HashSet<Integer>();

	for(Cycle c : cycles)
	    within.addAll(c.cycle.keySet());

	return within;
    }

    /* This is O(N) : A single traversal defines all cycles, 
     * and each cycle requires one linear traversal, so the result is
     * O(2N) == O(N) */
    private ArrayList<Cycle> determineCycles(int[] nodes) {
	boolean[] visited = new boolean[nodes.length];
	ArrayList<Cycle> cycles = new ArrayList<Cycle>();
	
	for(int i = 0; i < visited.length; i++) {
	    if(!visited[i]) {
		HashSet<Integer> ct = new HashSet<Integer>();		
		int currentNode = i;
		while(!ct.contains(currentNode) && !visited[currentNode]) {
		    visited[currentNode] = true;
		    ct.add(currentNode);
		    currentNode = nodes[currentNode];		    
		}
		
		//we now know the duplicate value (most likely) - if ct contains the value,
		//then it was part of a cycle. Otherwise, it was part of a strand!
		if(ct.contains(currentNode)) {
		    //part of a cycle		    
		    HashMap<Integer, Integer> cycle = new HashMap<Integer, Integer>();
		    int index = 0;
		    while(!cycle.containsKey(currentNode)) {
			cycle.put(currentNode, index++);
			currentNode = nodes[currentNode];
		    }
		    
		    Cycle c = new Cycle(cycle);
		    c.printCycle(2);
		    cycles.add(c);
		}
	    }
	}

	return cycles;
    }

    private static int __CYCLE_ADDRESS = 0;
    private class Cycle {	
	public final int baseAddress = ++__CYCLE_ADDRESS;

	private HashMap<Integer, Integer> cycle;

	public Cycle(HashMap<Integer, Integer> cycle) {
	    this.cycle = cycle;
	}

	/* will print at given debug level */
	public void printCycle(int level) {
	    if(GET_DEBUG_LEVEL() >= level) {
		String s = String.format("Address: %d%n", baseAddress);
		for(int i : cycle.keySet())
		    s = s + String.format(" %d ->", i);
		if(level <= 0)
		    println(s);
		else
		    DEBUG(level, s);
	    }
	}
	
	/* O(1) */
	public boolean contains(int node) {
	    return cycle.containsKey(node);
	}

	/* O(1) */
	public int distance(int fromNode, int toNode) {
	    if(!contains(fromNode) || !contains(toNode))
		return -1;
	    /* gauranteed to contain fromNode and toNode */
	    
	    if(fromNode == toNode)
		return 0;
	    /* take care of trivial case */

	    int from = cycle.get(fromNode);
	    int to = cycle.get(toNode);

	    //distance from A to B on a ring network
	    return (to - from) % size();
	}

	/* O(1) */
	public int size() {
	    return cycle.size();
	}
    }
    
    /* 
     * Target Procedure:
     *  > 1. get the list of all nodes from A->B
     *  > 2. determine all backwards links (B->[A])
     *  > 3. construct all cycles (this is O(N) -> linear crawl)
     *  > 4. determine all cycle entry points (knowing the cycles, this is also a linear crawl)
     *  > 5. build strands and substrands based on those entry points (knowing the entry points, this is linear time)
     *
     * Notes:
     *  each cycle has a unique address - cycle reduction can be done in O(1)
     *  lookups within a cycle can be done in O(1) also
     *  lookups within a strand can be done in O(1)
     *  address in the vein of [cycle, strand0, ..., strandN] (this is a hashset ?)
     *  extra (pre-processed) files in vein of [dist_to_cycle, dist_to_strand0, ..., etc]
     *  finally, list of entrypoints in vein of [cycle_entry, strand0_entry, ..., etc]
     *  this means with one address lookup, we can route ourselves to any other known address in O(1) if possible (distance to strand/cycle, entry point, done)
     *  this data can all be assembled in O(1) (kind of - space might get a bit "tight"
     */

    private ArrayList<String> getInput() {
	/* either one or the other of these alternatives is guaranteed by generateInput/inputCommand */
	if(generatedNodes.matched > 0 && generatedChecks.matched > 0)
	    return generateInput(generatedNodes.getValue(), generatedChecks.getValue());	    
	else if (inputFile.matched())
	    return readFileLines(inputFile.getValue());
	
	/* purely for the compiler :) */
	return null;
    }
    
    private <T> void printList(ArrayList<T> li, int level) {	
	for(T t : li)
	    if(level <= 0)
		println(t);
	    else
		DEBUG(level, t);
    }
    
    /* Generate Input */
    public ArrayList<String> generateInput(int num_nodes, int num_checks) {
	/*
	 * Format:
	 *  N K
	 *  N1
	 *  ...
	 *  NN
	 *  K1A K1B
	 *  ...
	 *  K2A K2B
	 */

	Random rand = new Random();
	ArrayList<String> res = new ArrayList<String>();
	res.add(String.format("%d %d", num_nodes, num_checks));
	
	for(int n = 0; n < num_nodes; n++)
	    res.add(String.format("%d", rand.nextInt(num_nodes)));	

	for(int k = 0; k < num_checks; k++)
	    res.add(String.format("%d %d", rand.nextInt(num_nodes), rand.nextInt(num_nodes)));

	return res;
    }
    
    /* set commands */
    @Override public Command[] setCommands() {
	inputFile = new FileCommand(/*name =      */ "Input File",
				    /*description=*/ "Auxiliary data for this program",
				    /*mandatory  =*/ true,
				    /*[synonyms] =*/ "-f", "--file");	

	//do you want paged input to be optional? This is mainly a debugging thing,
	//or a memory management/speed thing
	_PAGE_OPTIONAL = false; //page does not show up as a user input command
	_PAGE_ENABLED = false;  //page is set to disabled by default

	generatedNodes = new IntCommand(1, 10000000, false, 0, "--generated-nodes")
	    .setName("Generate Nodes")
	    .setDescription("Number of nodes to generate");
	generatedChecks = new IntCommand(1, 10000000, false, 0, "--generated-checks")
	    .setName("Generate Checks")
	    .setDescription("Number of checks to generate");
	generateInput = new AllOrNothingCommand("Generate Input", generatedNodes, generatedChecks);

	inputCommand = new SingleChoiceCommand("Get Input", generateInput, inputFile);
	return new Command[]{inputCommand};
    }
    
    /* act after commands processed */
    @Override public void actOnCommands() {
	//do whatever you want based on the commands you have given
	//at this stage, they should all be resolved
    }
    
    public static void main(String[] argv) {
        new QuickLinks21().run(argv);
    }
}
