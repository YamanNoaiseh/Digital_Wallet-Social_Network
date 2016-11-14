import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author Yaman Noaiseh
 * @date November 13, 2016
 */
public class Antifraud {
	
	private Graph paymo = new Graph();
	
	/**
	 * The main method of the program. Creates a graph object and processes it
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		Antifraud antifraud = new Antifraud();
		antifraud.buildInitialState(); // Build the initial graph
		antifraud.processInput();	   // Process the streaming input
	}
	
	/**
	 * Processes the batch input file to build the initial state of the graph
	 */
	public void buildInitialState() {
		String batch = "./../paymo_input/batch_payment.csv";
		int bufferSize = 8*1024;
		try (BufferedReader bReader = new BufferedReader(new FileReader(batch), bufferSize)) {
			String line = bReader.readLine(); // Pass the first line
			while ((line = bReader.readLine()) != null) {
				Integer[] ids;
				// Ignore invalid lines
				try {
					ids = getUserIds(line);
				} catch (RuntimeException e) {
					continue;
				}
				paymo.addEdge(ids[0], ids[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Processes the stream input file, and creates and writes in the output files
	 */
	public void processInput() {
		String stream = "./../paymo_input/stream_payment.csv";
		String out1 = "./../paymo_output/output1.txt";
		String out2 = "./../paymo_output/output2.txt";
		String out3 = "./../paymo_output/output3.txt";
		int bufferSize = 8*1024;
		try (BufferedReader bReader = new BufferedReader(new FileReader(stream), bufferSize);
				BufferedWriter bWriter1 = new BufferedWriter(new FileWriter(out1), bufferSize);
				BufferedWriter bWriter2 = new BufferedWriter(new FileWriter(out2), bufferSize);
				BufferedWriter bWriter3 = new BufferedWriter(new FileWriter(out3), bufferSize)) {
			String line = bReader.readLine();
			while ((line = bReader.readLine()) != null) {
				Integer[] ids;
				try {
					ids = getUserIds(line);
				} catch (RuntimeException e) {
					continue;
				}
				// Check if there is a brand new user involved in this payment before
				// checking any friendship
				if (!paymo.existingUsers(ids[0], ids[1])) {
					bWriter1.write("unverified");
					bWriter2.write("unverified");
					bWriter3.write("unverified");
				} else {
					if (paymo.isConnection(ids[0], ids[1])) {
						bWriter1.write("trusted");
						bWriter2.write("trusted");
						bWriter3.write("trusted");
					} else {
						bWriter1.write("unverified");
						if (paymo.isFriendOfFriend(ids[0], ids[1])) {
							bWriter2.write("trusted");
							bWriter3.write("trusted");
						} else {
							bWriter2.write("unverified");
							if (paymo.isFourthLevelFriend(ids[0], ids[1])) {
								bWriter3.write("trusted");
							} else {
								bWriter3.write("unverified");
							}
						}
					}
				}
				bWriter1.newLine();
				bWriter2.newLine();
				bWriter3.newLine();
				paymo.addEdge(ids[0], ids[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Extracts the IDs of the two users involved in the payment, and ignore 
	 * everything else.
	 * 
	 * @param inputLine a line from the input file 
	 * @return array of type int containing the IDs of the two users involved in the payment 
	 */
	// Performance - Avoiding the overhead of using the String.split method and the
	// underlying use of Regex, as the input files include millions of input lines.
	private Integer[] getUserIds(String inputLine) {
		int firstComma = inputLine.indexOf(',');
		int secondComma = inputLine.indexOf(',', firstComma + 1);
		int thirdComma = inputLine.indexOf(',', secondComma + 1);
		if (firstComma == -1 || secondComma == -1 || thirdComma == -1) {
			throw new IllegalArgumentException();
		}
		String source = inputLine.substring(firstComma + 1, secondComma).trim();
		String target = inputLine.substring(secondComma + 1, thirdComma).trim();
		Integer[] ids = {Integer.valueOf(source), Integer.valueOf(target)};
		return ids;
	}
	
}

class Graph {
	Map<Integer, Set<Integer>> graph;
	
	/**
	 * Creates a new Graph object
	 */
	public Graph() {
		graph = new HashMap<Integer, Set<Integer>>();
	}
	
	/**
	 * Adds an edge to the graph
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 */
	public void addEdge(Integer id1, Integer id2) {
		if (!graph.containsKey(id1)) {
			addNewVertix(id1);
		}
		if (!graph.containsKey(id2)) {
			addNewVertix(id2);
		}		
		addNewFriend(id1, id2);
		addNewFriend(id2, id1);
	}
	
	/**
	 * Adds a vertex to the graph
	 * 
	 * @param id the user's ID 
	 */
	public void addNewVertix(Integer id) {
		Set<Integer> connections = new HashSet<>();
		graph.put(id, connections);
	}
	
	/**
	 * Adds a friend to the first user
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 */
	public void addNewFriend(Integer id1, Integer id2) {
		Set<Integer> connections = graph.get(id1);
		connections.add(id2);
		graph.put(id1, connections);
	}
	
	/**
	 * Checks if two users already exist in the graph
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 * @return true if both users exist in the graph
	 */
	public boolean existingUsers(Integer id1, Integer id2) {
		return graph.containsKey(id1) && graph.containsKey(id2);
	}
	
	/**
	 * Checks an edge between two users exists in the graph
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 * @return true if the edge exists in the graph
	 */
	public boolean isConnection(Integer id1, Integer id2) {
		// In this specific problem there is NO need to check if both IDs are
		// already in the graph as this method is called only after the check
		// has been performed by the method 'processInput' before calling isConnection(id1, id2).
		// Otherwise, the following if statement would wrap the code below
		// if (graph.containsKey(id1) && graph.containsKey(id2))
		if (graph.get(id1).contains(id2)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if two users are second-level friends (friend of friend)
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 * @return true if one user is a friend of a friend of the other user
	 */
	public boolean isFriendOfFriend(Integer id1, Integer id2) {
		// In this specific problem there is NO need to check if both IDs are
		// already in the graph as this method is called only after the check
		// has been performed by the method 'processInput' before calling isFriendOfFriend(id1, id2).
		// Otherwise, the following if statement would wrap the code below
		// if (graph.containsKey(id1) && graph.containsKey(id2))
		
		// Two users are in a 'Friend of a friend' relation if the
		// intersection of their friends lists (sets) is not empty
		Collection<Integer> connections =  graph.get(id1);
		for (Integer neighbor : connections) {
			if (graph.get(id2).contains(neighbor)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if two users are 4th-level friends
	 * 
	 * @param id1 the first user's ID 
	 * @param id2 the second user's ID
	 * @return true if one user is a 4th-level friend of the other user
	 */
	// BFS-based solution that stops at the fourth level
	public boolean isFourthLevelFriend(Integer id1, Integer id2) {
		// Initiate the queue with the level 1 connections of the first  user
		Queue<Integer> toVisitQueue = new LinkedList<>(graph.get(id1));
		Set<Integer> visited = new HashSet<>();
		visited.add(id1);
		// This special node will be added to the queue to indicate a level (breadth) increase
		Integer specialNode = new Integer(-1);
		toVisitQueue.add(specialNode);
		int level = 1;
		while (level <= 4 && !toVisitQueue.isEmpty()) {
			Integer current = toVisitQueue.remove();
			if (current.equals(id2)) {
				return true;
			}
			if (visited.contains(current)) {
				continue;
			}
			if (current.equals(specialNode)) {
				level++;
				// When we find the special node, we have already iterated
				// through all nodes at the current level and added nodes
				// for the next level. Add the special node after them
				toVisitQueue.add(specialNode);
				continue;
			}
			visited.add(current);
			for (Integer neighbor : graph.get(current)) {
				if (!visited.contains(neighbor)){
					toVisitQueue.add(neighbor);
				}
			}
		}
		return false;
	}
}
