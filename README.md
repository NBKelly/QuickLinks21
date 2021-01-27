# QuickLinks21
Programming contest question - mass evaluation of chains in massive disjointed semi-cyclic data structure. A detailed problem description could be found on this page, if it still existed: https://progcontest.aut.ac.nz/index.php/9-results/47-contest-report-2019

Since that link is dead, here is the description as I remember it:
Given a collection of nodes, where each node points to exactly one other node, and a collection of start-points and end-points, determine for each start-point end-point pair the distance from the start-point to the end-point if a path exists, or return -1 if it does not.

On the first line, two integers are given seperated by a space in the form `%d %d`: 
* N: number of nodes <= 100,000
* K: number of checks <= 100,000

On the second line, there are N space seperated integers which describe the target of each node, in the form `%d0 %d1 ... %dn-1`.

On the following K lines, there are two space-seperated integers describing the start-point and end-point of a path, in the form `%d %d`.

An example file may look like this:

```
11 5
1 0 2 2 3 4 5 4 7 8 9
0 1
1 2
7 4
9 3
10 6
```

Which would give as results:
```
1
-1
1
4
-1
```

with the paths:
```
0 -> 1
N/A
7 -> 4
9 -> 8 -> 7 -> 4 -> 3
N/A

```

This is primarily done as a test of my personal workflow tool, [Drafter](https://github.com/NBKelly/Drafter), 
but also as an attempt to improve upon my previous iteration of this problem.

# Process
The process I use is as follows:
* parse the input: get nodes as `int[] nodes`.
* construct all cycles. A cycle is a connected ring, where every node within the ring is reachable by every other node within the ring. Each cycle has an address, which is itself. Constructing all cycles can be done in linear time. Cycles are stored as `ArrayList<Cycle> cycles`.
* determine all nodes that are within cycles, as `HashSet<Integer> withinCycle`. This relies on ```cycles```.
* construct a graph in which each node points to the set of it's ancestors, in the form `ArrayList<ArrayList<Integer>> reverse`. There is probably a better data structure I could use, but this is still done in linear time.
* determine all "entry points" - nodes with greater than one ancestor. Store as ```HashSet<Integer> entryPoints```. This relies on ```reverse```.
* determine all leaves - nodes that have no ancestors. Store as ```ArrayList<Integer> leaves```. This relies on ```reverse```.
* construct all strands - a strand is either a path from a cycle to a leaf, of from a strand to a leaf. Store as ```ArrayList<Strand> strands```. This relies on ```leaves, entryPoints, nodes, withinCycle, reverse```.
* map all nodes to their parent chunks(strands/cycles), save as ```Chunk[] chunkMap```. This relies on ```strands, cycles, nodes```.
* map every node within a strand based on height. This relies on ```strands, chunkMap, nodes```.
* perform all lookups.
