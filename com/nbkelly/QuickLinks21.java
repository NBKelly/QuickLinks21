package com.nbkelly;

/* imports */
import com.nbkelly.aux.Drafter;
import com.nbkelly.aux.Command;
import com.nbkelly.aux.FileCommand;
import com.nbkelly.aux.BooleanCommand;
import com.nbkelly.aux.AllOrNothingCommand;
import com.nbkelly.aux.SingleChoiceCommand;
import com.nbkelly.aux.IntCommand;
import com.nbkelly.aux.Timer;

import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.IntStream;

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
    private BooleanCommand onlyPre;
    
    /* vars */
    
    /* solve problem here */
    @Override public int solveProblem() throws Exception {
	Timer t = makeTimer();
		
	/* retrieve (or generate) input */
	ArrayList<String> input = getInput();	
	DEBUG(1, t.split("Retrieved Input"));
	
	/* get N and K */
	String fl = input.get(0);
	int N = Integer.parseInt(fl.split(" ")[0]);
	int K = Integer.parseInt(fl.split(" ")[1]);
	
	DEBUGF(2, "N:%d K:%d%n", N, K);
	
	/* nodes -> dest */
	int[] nodes = new int[N];
	Scanner nodeScanner = new Scanner(input.get(1));
	for(int i = 0; i < N; i++)
	    nodes[i] = nodeScanner.nextInt();//Integer.parseInt(input.get(1 + i));
	DEBUG(1, t.split("Parsed Input (2)"));
	
	/* lookups */
	int[] from = new int[K];
	int[] to   = new int[K];
	parseLookups(N, K, from, to, input);
	
	input = null;
	DEBUG(1, t.split("Parsed Input (2)"));
	//printNodes(nodes, 2);
	
	DEBUG(1, t.split("Mapped Nodes"));
	if(GET_DEBUG_LEVEL() >= 2)
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
	ArrayList<Integer> entryPointsList = entryPoints(reverse);
	HashSet<Integer> entryPoints = new HashSet<Integer>();
	for(Integer i : entryPointsList)
	    entryPoints.add(i);
	
	printList(entryPointsList, 2);
	DEBUG(1, t.split("Identified all entry points"));
	
	/* determine all leaves */
	DEBUG(2, "Leaves:");
	ArrayList<Integer> leaves = leaves(reverse);
	printList(leaves, 2);
	DEBUG(1, t.split("Identified all leaves"));

	/* construct all strands top-down */
	ArrayList<Strand> strands = strands(leaves, entryPoints, nodes, withinCycle, reverse);
	DEBUG(1, t.split("Constructed all strands"));
	for(Strand s : strands)
	    s.printStrand(nodes);

	
	/* map all nodes to their containing chunks (strands/cycles) */
	Chunk[] chunkMap = mapChunks(strands, cycles, nodes);
	DEBUG(1, t.split("Mapped all chunks from nodes"));
	
	/* map all strands based on height */
	/* and also associate all strands with their next strand */
	heightMap(strands, chunkMap, nodes);
	DEBUG(1, t.split("Mapped all strands based on height"));

	if(onlyPre.value)
	    return 0;
	
	/* then, solve the problem :) */

	/*
	 * Before we do this, what memory can we free?
	 */
	reverse = null;
	cycles = null;
	strands = null;
	withinCycle = null;
	
	lookup(K, from, to,
	       nodes, chunkMap);
	DEBUG(1, t.split("Determined all result values"));
	
	DEBUG(1, t.total());	
	return 0;
    }    
    
    private void lookup(int K, int[] _from, int[] _to,
			int[] nodes, Chunk[] chunkMap) {
	final int[] results = new int[K];
	IntStream.range(0, K).parallel().forEach(index ->
						 {
						     int from = _from[index];
						     int to   =   _to[index];
	    
						     results[index] = lookup_value(from, to, nodes, chunkMap);
						 });

	
	for(int index = 0; index < K; index++) {
	    DEBUGF(2, "Index %d: ", index);
	    println(results[index]);
	    //int from = _from[index];
	    //int to   =   _to[index];
	    
	    
	    //println(lookup_value(from, to, nodes, chunkMap));
	}
    }
    
    private int lookup_value(int from, int to,
			     int[] nodes, Chunk[] chunkMap) {
	//trivial case - identity
	if(from == to)
	    return 0;
	else {
	    //test if we are in a ring or a strand
	    Chunk fromChunk = chunkMap[from];
	    Chunk toChunk = chunkMap[to];
	    /* if the destination is a cycle, the target must be in the same cycle */
	    if(fromChunk.getClass() == Cycle.class) {
		//a result can only exist if toChunk is in the same cycle
		if(toChunk == fromChunk)
		    return ((Cycle)fromChunk).distance(from, to);
		else
		    return -1;
	    }
	    else { //(fromChunk.getClass() == Strand.class) 
		//if the destination is a cycle, then we can do a little bit of kung-fu to get there
		assert fromChunk.getClass() == Strand.class;
 
		/* if we travel from a strand to a cycle, we basically just need to check that 
		   they both resolve in the same cycle, then compare height at cycle entry point
		   + cycle distance */
		int fromHeight = ((Strand)fromChunk).strand.get(from);
		if(toChunk.getClass() == Cycle.class) {			
		    Chunk current = fromChunk;
		    //this resoltuion takes O(logN) over all strands,
		    // and there can be at most O(logN) strands!
		    // thus O(logSqN);
		    while(current.nextChunk.getClass() != Cycle.class)
			current = current.nextChunk;

		    assert current.nextChunk.getClass() == Cycle.class;
		    /* check if the cycles match */
		    if(toChunk == current.nextChunk) {
			int entryPoint = ((Strand)current).last;
			entryPoint = nodes[entryPoint];
			int cycleDist = ((Cycle)toChunk).distance(entryPoint, to);
			return cycleDist + fromHeight + 1;
		    }

		    return -1;
		}

		//toChunk.class == strand
		/* trivial case here is compare height */
		/* equal heights is already taken care of from trivial scenario */
		int toHeight = ((Strand)toChunk).strand.get(to);
		int res = fromHeight - toHeight;
		if(res < 0)
		    return -1;
		
		//see if it's in our current strand
		if(toChunk == fromChunk) {				
		    //if it's above us in the stand, then we can't give that as an answer
		    return res;
		}

		
		//get the last element in this chunk
		boolean resolved = false;
		Strand current = (Strand)fromChunk;
		//get the last item in this strand, see if it matches
		int last = current.last;
		//get the entry point into the next item
		int entry = nodes[last];
		while(current.nextChunk.getClass() != Cycle.class) {
		    current = (Strand)(current.nextChunk);
		    int entryHeight = current.strand.get(entry);
		    if(entryHeight < toHeight)
			return -1;
		    else if(current == toChunk)
			return res;

		    entry = nodes[current.last];
		}

		return -1;
	    }
	}
    }
    
    private void parseLookups(int N, int K, int[] from, int[] to, ArrayList<String> input) {
	for(int index = 0; index < K; index++) {
	    String[] line = input.get(2 + index).split(" ");;
	    from[index] = Integer.parseInt(line[0]);
	    to[index] = Integer.parseInt(line[1]);
	}
    }
    
    /* O(N Log^2 N):
       Strands are worst case log N compared to size
       So sorting that is N LogSquared N worst case
    */
    private void heightMap(ArrayList<Strand> strands, Chunk[] chunkMap, int[] nodes) {
	//first, we must sort the strands
	Collections.sort(strands);

	//we need to start with the longest strands, so our sort should place longest elements first
	//hence s.comp(j) -> j.size() - s.size()
	//for(Strand s : strands)
	//    println("Size: " + s.size());
	for(Strand strand : strands) {
	    int last = strand.last;
	    int next = nodes[last];
	    Chunk nextChunk = chunkMap[next];

	    assert nextChunk != strand : "Error circular chunk definition";
	    
	    int baseNumber = 0;
	    strand.family = nextChunk;
	    
	    if (nextChunk.getClass() == Strand.class) {
		//get the entry point
		baseNumber = ((Strand)nextChunk).strand.get(next) + 1;
		assert nextChunk.nextChunk != null;
		strand.family = nextChunk.family;
	    }
	    
	    strand.nextChunk = nextChunk;
	    
	    int size = strand.size();
	    
	    //size - 1 - val + base
	    HashMap<Integer, Integer> addresses = new HashMap<Integer, Integer>();
	    for(int key : strand.strand.keySet()) {
		//get the value
		int value = strand.strand.get(key);
		addresses.put(key, size - 1 - value + baseNumber);
	    }

	    strand.strand = addresses;
	}
    }
    
    private Strand largest(ArrayList<Strand> s) {
	var largest = s.get(0);
	for(Strand t : s)
	    if(t.size() > largest.size())
		largest = t;
	return largest;
    }
    
    /* O(N) - assertions purely for peace of mind */
    private Chunk[] mapChunks(ArrayList<Strand> strands, ArrayList<Cycle> cycles, int[] nodes) {
	Chunk[] chunks = new Chunk[nodes.length];
	for(Strand strand : strands) {
	    for(int i : strand.strand.keySet()) {
		assert chunks[i] == null : ("repeat at " + i);
		
		chunks[i] = strand;
	    }
	}

	for(Cycle cycle : cycles) {
	    for(int i : cycle.cycle.keySet()) {
		assert chunks[i] == null : ("repeat at " + i);
		
		chunks[i] = cycle;
	    }
	}

	assert !Arrays.asList(chunks).contains(null) : "empty elements in chunks";
	
	return chunks;
    }
			   
    private void printNodes(int[] nodes, int level) {
	for(int i = 0; i < nodes.length; i++) {
	    if(level == 0)
		printf("%d -> %d%n", i, nodes[i]);
	    else
		DEBUGF(level, "%d -> %d%n", i, nodes[i]);
	}
    }

    /* This should be O(N). Consider the following:
       1. All lookups to entryPoints, nodes, withinCycle, reverseMapping take place in O(1) time
       2. At each step, we look at the next node. 
          a) If it is not an entry point, we continue to the next node
	  b) If it is an entry point, then we add the strand to a consideration list
	  c) once the consideration list has members equal to the size of the entry point,
	       we select the largest element from that list and add it to the end of the working group
	  d) The number of entry points * average entry point size is always < size of tree
	  c) hence additional considerations occur <N times in all cases
      3. checking the size of a strand takes O(1) time, as does checking the size of an entry point
      4. Hence, the entire function is O(N) in time
    */
    private ArrayList<Strand> strands(ArrayList<Integer> leaves, HashSet<Integer> entryPoints,
				      int[] nodes, HashSet<Integer> withinCycle,
				      ArrayList<ArrayList<Integer>> reverseMapping) {	
	HashMap<Integer, ArrayList<Strand>> waiting = new HashMap<>();
	ArrayList<Strand> strands = new ArrayList<Strand>();
	ArrayDeque<Strand> stack = new ArrayDeque<>();

	for(int leaf : leaves) { 
	    Strand s = new Strand();
	    strands.add(s);
	    /* if it's one of the initial L strands, provide the initial value*/	    
	    s.add(leaf);
	}
	
	stack.addAll(strands);

	while(stack.size() > 0) { //for(int i = 0; i < strands.size(); i++) {
	    Strand s = stack.pop();
	    int ct = s.last;
	    
	    //follow the next trail until we find an entry point
	    while(!entryPoints.contains(nodes[ct])) {
		s.add(nodes[ct]);
		ct = nodes[ct];
	    }

	    /* we don't follow the trace into a rabbit hole */
	    if(withinCycle.contains(nodes[ct]))
		continue;

	    /* if we need to initialize this list, do so! */
	    if(!waiting.containsKey(nodes[ct]))
		waiting.put(nodes[ct], new ArrayList<Strand>());
	    
	    waiting.get(nodes[ct]).add(s);

	    //then, we check the following:
	    // 1. is the value we're waiting on part of a cycle? if so, terminate there
	    // 2. do we have a number of waiting strands equal to the width of the entry point?
	    //       if so, select the longest one and continue on at that point
	    //       how do we continue? easy enough, add it to the end of the strands list
	    //       Now, we have a question: why use a list and not a stack?
	    //       for very large applications, memory will get tough!
	    if(waiting.get(nodes[ct]).size() == reverseMapping.get(nodes[ct]).size()) {
		//get the largest strand
		Strand largest = largest(waiting.get(nodes[ct]));
		largest.add(nodes[ct]);
		stack.push(largest);
		//how much of a performance hit does this have?
		waiting.remove(nodes[ct]);
	    }
	}
	
	return strands;
    }
    
    /* O(N) - No point is examined more once */
    private ArrayList<Integer> entryPoints(ArrayList<ArrayList<Integer>> reverseMapping) {
	ArrayList<Integer> allEntryPoints= new ArrayList<Integer>();
	
	for(int to = 0; to < reverseMapping.size(); to++) {
	    ArrayList<Integer> entryPoints = reverseMapping.get(to);
	    if(entryPoints.size() > 1)
		allEntryPoints.add(to);
	}

	return allEntryPoints;
    }

    /* O(N) - No point is examined more once */
    private ArrayList<Integer> leaves(ArrayList<ArrayList<Integer>> reverseMapping) {
	ArrayList<Integer> leaves= new ArrayList<Integer>();
	
	for(int to = 0; to < reverseMapping.size(); to++) {
	    ArrayList<Integer> entryPoints = reverseMapping.get(to);
	    if(entryPoints.size() == 0)
		leaves.add(to);
	}

	return leaves;
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
    
    private abstract class Chunk implements Comparable<Chunk> {
	public final int baseAddress = __CYCLE_ADDRESS++;
	
	//public ArrayList<Integer> address = new ArrayList<Integer>();
	public abstract int size();

	public int compareTo(Chunk c) {
	    return c.size() - size();
	}

	public Chunk nextChunk = null;
	public Chunk family = null;
    }
    
    /* class representing a strand: a linear portion of a tree containing one exit point */
    private class Strand extends Chunk {
	//A strand contains:
	//  a list of all points in the strand (start of list = bottom)
	//  a destination
	//  a size
	//  an 'address book'
	//  a destination cycle
	//each strand has a unique numeric address, and contains an address book in the form
	// [cycle, s1, ..., this]

	// node -> position
	private HashMap<Integer, Integer> strand = new HashMap<>();
	private int counter = 0;
	
	public void add(int node) {
	    strand.put(this.last = node, counter++);
	}
	
	@Override public int size() {
	    return strand.size();
	}

	public int last;

	public void printStrand(int[] nodes) {
	    int level = 2;
	    if(GET_DEBUG_LEVEL() >= level) {
		int[] str = new int[strand.size()];
		for(int node : strand.keySet()) {
		    str[strand.get(node)] = node;
		}

		DEBUGF(level, "Strand %d: ", baseAddress);
		for(int i = 0; i < str.length; i++) {
		    if(i != str.length - 1)
			DEBUGF(level, "%d -> ", str[i]);
		    else
			DEBUGF(level, "%d ", str[i]);
		}
		/* this nice little trick colors them a different color */
		DEBUGF(level + 1, "-> (%d)", nodes[str[str.length-1]]);
		DEBUG(level, "");
	    }
	}
    }

    /* class representing a cycle : a linear portion of a tree that forms a cycle*/
    private class Cycle extends Chunk {	
	private HashMap<Integer, Integer> cycle;

	public Cycle(HashMap<Integer, Integer> cycle) {
	    this.cycle = cycle;
	}

	/* will print at given debug level */
	public void printCycle(int level) {
	    if(GET_DEBUG_LEVEL() >= level) {
		String s = String.format("Cycle #%d:%n", baseAddress);
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
	    return (size() + to - from) % size();
	}

	/* O(1) */
	@Override public int size() {
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
	 *  N1  ... NN
	 *  K1A K1B
	 *  ...
	 *  K2A K2B
	 */

	Random rand = new Random();
	StringBuilder nodes = new StringBuilder();
	ArrayList<String> res = new ArrayList<String>();
	res.add(String.format("%d %d", num_nodes, num_checks));
	
	for(int n = 0; n < num_nodes; n++)
	    nodes.append(String.format("%d ", rand.nextInt(num_nodes)));	

	res.add(nodes.toString());
	
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

	onlyPre = new BooleanCommand(OPTIONAL, "--only-preprocess")
	    .setName("Pre-process termination")
	    .setDescription("Terminate after pre-processing");
	inputCommand = new SingleChoiceCommand("Get Input", generateInput, inputFile);
	return new Command[]{inputCommand, onlyPre};
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
