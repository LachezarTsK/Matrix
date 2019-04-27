import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The program applies Disjoint Union Sets and a modification of Kruskal MST
 * algorithm to find the minimum time for disconnecting machines contained in a
 * graph, so that no machine can be reached from any other machine.
 */
public class Solution {

	private static DisjointUnionSet disjointUnionSet;
	private static Edge[] edges;

	public static void main(String[] args) throws IOException {

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
		int numberOfCities = Integer.parseInt(stringTokenizer.nextToken());
		int numberOfMachines = Integer.parseInt(stringTokenizer.nextToken());
		disjointUnionSet = new DisjointUnionSet(numberOfCities);
		edges = new Edge[numberOfCities - 1];
		CorrectInput checkInput = new CorrectInput();

		for (int i = 0; i < numberOfCities - 1; i++) {
			String inputLine = bufferedReader.readLine();

			if (checkInput.input_is_threeIntegersSeparatedBySpace(inputLine)) {
				stringTokenizer = new StringTokenizer(inputLine);
				int cityOne = Integer.parseInt(stringTokenizer.nextToken());
				int cityTwo = Integer.parseInt(stringTokenizer.nextToken());
				int time = Integer.parseInt(stringTokenizer.nextToken());
				edges[i] = new Edge(cityOne, cityTwo, time);
			} else {
				i--;
			}
		}

		Set<Integer> machines = new HashSet<Integer>();

		for (int i = 0; i < numberOfMachines; i++) {
			String inputLine = bufferedReader.readLine();
			if (checkInput.input_is_oneInteger(inputLine)) {
				stringTokenizer = new StringTokenizer(inputLine);
				machines.add(Integer.parseInt(stringTokenizer.nextToken()));
			} else {
				break;
			}
		}

		bufferedReader.close();
		System.out.println(calculateMinimumTime(machines));
	}

	/**
	 * @return minimum time necessary to disrupt the connections among all machines.
	 */
	private static int calculateMinimumTime(Set<Integer> machines) {
		/**
		 * The set stores the nodes that are connected, directly or indirectly, to a
		 * machine.
		 */
		Set<Integer> rootsLeadingToMachines = new HashSet<Integer>();
		int minimumTime = 0;
		Arrays.sort(edges);

		for (int i = 0; i < edges.length; i++) {
			int rootOne = disjointUnionSet.find(edges[i].from);
			int rootTwo = disjointUnionSet.find(edges[i].to);

			if (edgeConnectsTwoMachines(rootOne, rootTwo, machines, rootsLeadingToMachines)) {
				minimumTime += edges[i].time;
			} else {
				disjointUnionSet.union(rootOne, rootTwo, machines, rootsLeadingToMachines);
			}
		}
		return minimumTime;
	}

	/**
	 * Checks whether the edge, directly or indirectly, connects two machines.
	 */
	private static boolean edgeConnectsTwoMachines(int rootOne, int rootTwo, Set<Integer> machines,
			Set<Integer> rootsLeadingToMachines) {
		boolean rootOne_leadsToMachine = rootsLeadingToMachines.contains(rootOne);
		boolean rootTwo_leadsToMachine = rootsLeadingToMachines.contains(rootTwo);
		boolean rootOne_isMachine = machines.contains(rootOne);
		boolean rootTwo_isMachine = machines.contains(rootTwo);

		return (rootOne_leadsToMachine || rootOne_isMachine) && (rootTwo_leadsToMachine || rootTwo_isMachine);
	}
}

/**
 * The class contains methods that check for the correctness of the input of
 * edges and machines.
 * 
 * Although it is not explicitly mentioned in the conditions of the task, it
 * could be inferred from Test Case 11 (if it is not a typographical error)
 * that:
 *
 * 1. Input of Edges:
 * 
 * When there is an incorrect input for an edge i.e. the line does not contain
 * three integers separated by space, beside preventing a crash due to
 * IOException, the algorithm does not count the line as an input. Thus the
 * index counter does not increase unless there is a correct input for an edge.
 * The loop will continue until the number of correct input for the edges equals
 * the number of edges.
 * 
 * 2. Input of Machines:
 * 
 * When there is an incorrect input for a machine i.e. the line does not contain
 * one integer, the loop ends regardless of whether the number of correct input
 * is equal to the number of machines .
 * 
 * In the mentioned Test Case 11, the loop for the input for the machines ends
 * after a null input. Thus, one possible interpretation is that, if there is
 * any input other than null, then the correctness of the input is guaranteed
 * and only if there is a null input, the loop ends - even if number of correct
 * input is less than the number of machines.
 * 
 * However, the algorithm in this program takes a broader perspective: any form
 * of incorrect input is possible, and if the input does not consist of an
 * integer (therefore, is null or something else), then the loop ends,
 * regardless of whether the number of correct input equals the number of
 * machines.
 */
class CorrectInput {
	public boolean input_is_threeIntegersSeparatedBySpace(String inputLine) {
		if (inputLine == null) {
			return false;
		}
		String regex = "((([0-9]+)(\\s+)){2})([0-9]+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputLine.trim());
		return matcher.matches();
	}

	public boolean input_is_oneInteger(String inputLine) {
		if (inputLine == null) {
			return false;
		}
		String regex = "([0-9]+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputLine.trim());
		return matcher.matches();
	}
}

class Edge implements Comparable<Edge> {
	int from;
	int to;
	int time;

	public Edge(int from, int to, int time) {
		this.from = from;
		this.to = to;
		this.time = time;
	}

	/**
	 * Sort edges as per their time value, in non-increasing order.
	 */
	@Override
	public int compareTo(Edge arg0) {
		return arg0.time - this.time;
	}

}

class DisjointUnionSet {
	private int[] parent;
	private int[] rank;
	private int numberOfCities;

	public DisjointUnionSet(int numberOfCities) {
		this.numberOfCities = numberOfCities;
		parent = new int[numberOfCities];
		rank = new int[numberOfCities];
		makeSet();
	}

	private void makeSet() {
		for (int i = 0; i < numberOfCities; i++) {
			parent[i] = i;
		}
	}

	public int find(int i) {
		if (parent[i] != i) {
			parent[i] = find(parent[i]);
		}
		return parent[i];
	}

	/**
	 * When joining a node that is a machine (or a set of nodes containing a
	 * machine) with a node that is not a machine (or a set of nodes not containing
	 * a machine), it is possible to make the root of the first as the root of the
	 * latter.
	 * 
	 * Although it will spare the creation of "Set<Integer> rootsLeadingToMachines",
	 * depending on the actual input, there is no guarantee that the principle of
	 * joining a subtree with a lower rank to that of higher rank will be preserved.
	 * This could negatively affect the time for search.
	 */
	public void union(int rootOne, int rootTwo, Set<Integer> machines, Set<Integer> rootsLeadingToMachines) {

		if (rootOne == rootTwo) {
			return;
		}

		if (rank[rootOne] < rank[rootTwo]) {
			if (isMachine_or_hasRootLeadingToMachine(rootOne, machines, rootsLeadingToMachines)) {
				rootsLeadingToMachines.add(rootTwo);
				rootsLeadingToMachines.remove(rootOne);
			}
			parent[rootOne] = rootTwo;
		} else if (rank[rootOne] > rank[rootTwo]) {
			if (isMachine_or_hasRootLeadingToMachine(rootTwo, machines, rootsLeadingToMachines)) {
				rootsLeadingToMachines.add(rootOne);
				rootsLeadingToMachines.remove(rootTwo);
			}
			parent[rootTwo] = rootOne;
		} else {
			if (isMachine_or_hasRootLeadingToMachine(rootTwo, machines, rootsLeadingToMachines)) {
				rootsLeadingToMachines.add(rootOne);
				rootsLeadingToMachines.remove(rootTwo);
			}
			parent[rootTwo] = rootOne;
			rank[rootOne] = rank[rootOne] + 1;
		}
	}

	/**
	 * Checks whether a node is a machine or is connected, directly or indirectly,
	 * to a machine.
	 */
	public boolean isMachine_or_hasRootLeadingToMachine(int root, Set<Integer> machines,
			Set<Integer> rootsLeadingToMachines) {
		if (rootsLeadingToMachines.contains(root) || machines.contains(root)) {
			return true;
		}
		return false;
	}
}
