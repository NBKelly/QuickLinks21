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
The nodes form a `directed simple graph permitting loops`, which can be unrolled into a series of cycles (which can be computed in O(1) time) and trees.

The trees themselves can be further processesed, such that the complexity for determining if a path exists between two nodes in a tree becomes O(log(log(N))(I think) for the size of the tree.

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

The process can be seen pictorially here:
![(I hope)](https://raw.githubusercontent.com/NBKelly/QuickLinks21/master/graphs/Process.png "Some parallelization possible")

It should immediately be obvious that some parallelization (in the pre-processing) is possible. On this I will say a few things:
* The overhead exceeds the savings when there are either minimal cycles or there is a minimal number of leaves. This means that on some crafted inputs, the overhead will exceed the savings
* When the number of nodes is small (< 50,000), the overhead exceeds the speedup when pre-processing
* In general, the cases where the input is large and the pre-processing speedup is negative will still get a net gain in the post-processing (lookup) phase
* pre-process savings can be around 30% for large files.

The overall worst case ends up being somewhere between O(NlogN) and O(NlogLogN).

Here are the metrics I recorded using ```./time.sh``` for QuickLinks 21:
![QuickLinks 21](https://raw.githubusercontent.com/NBKelly/QuickLinks21/master/graphs/QuickLinks%2021.png "QL21")

And compared to the previous version of QuickLinks:
![QuickLinks 19vs21](https://raw.githubusercontent.com/NBKelly/QuickLinks21/master/graphs/QuickLinks%2019%20vs%20QuickLinks%2021.png "QL19vs21")
