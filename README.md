# Insight_Digital_Wallet

> 
> ORIGINAL PROBLEM SPECIFICATIONS
> https://github.com/InsightDataScience/digital-wallet
>
> ====================================================================================================================

Java solution for Insight Data Engineering Fellowship program - Jan 2017 session.


**Implementation tools:**
- Java 7
- Eclipse MARS.2


External Libraries:
- None


Added Features:
- None


Solution:
- Feature 1: two users are friends when they are in the adjacency list of each another.

- Feature 2: two users are friends or second-level friends if they are friends (feature 1) or they have at least one mutual friend. In other words, the intersection of their adjacency list is non-empty.

- Feature 3: after checking for the first two features, I used BFS to search the graph for a path between the two users with the constraint 'Go no deeper than level 4'


Clean code:
- Using 'Google Java Style Guide' at https://google.github.io/styleguide/javaguide.html 


Data structures, Time, & Space:
- Adjacency list vs Adjacency matrix: Implemented using adjacency list as the social graph is very sparse
- An adjacency list saves the memory overhead associated with the implementation using the adjacency matrix, but stores each edge twice (one at each vertix).
- Optimization: users were represented using the 'Integer' class rather than String to save memory.
- Time complexity for the adjacency list: O(V+2E) as it is an undirected graph and we visit each edge twice.
- Used data structures: HashMap, HashSet, LinkedList (as a Queue) with constant time for all used operations.
- The Graph is implemented with HashMap< Integer , HashSet <Integer>> where the key is a Vertix in the Graph (a user) and the value is set of Edges connected to that Vertix.
