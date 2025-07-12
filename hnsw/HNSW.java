package hnsw;

import java.util.*;

public class HNSW {
  // class variables I would need
  public Map<Integer, Map<Integer, Set<Integer>>> graph; // main graph
  Map<Integer, Vector> vectors; // vectors just stored in a map
  int maxLevel = 0, entryPoint = 0, lastIndex = 0;

  public HNSW() {
    graph = new HashMap<>();
    vectors = new HashMap<>();
  }

  /* MAIN INSERT METHOD */

  public void insertNode(Vector inputNode) {
    /*
     * STEPS
     * -> generate a random number
     * -> Add number to that many levels
     * -> If currentLevel > maxLevel
     * -> do greedy search from entry point to find the best entry point
     * -> do a priority queue search to get the best n neighbours
     */
    inputNode.index = lastIndex; // storing the index so that it's easy to fetch later
    vectors.put(lastIndex, inputNode);
    int inputIndex = lastIndex;
    lastIndex++;

    // adding new node to n levels
    int currentLevel = generateLevel(3);
    for (int i = 0; i <= currentLevel; i++) {
      graph.putIfAbsent(i, new HashMap<>());
      graph.get(i).put(inputIndex, new HashSet<>());
    }

    // updating the global entrypoint
    if (currentLevel > maxLevel) {
      maxLevel = currentLevel;
      entryPoint = inputIndex;
      return;
    }

    // TODO: Add this to arg list later
    int num = 2;

    PriorityQueue<NodeDist> neighbourList = returnKNeighbours(entryPoint, inputNode, inputIndex, maxLevel, num);
    // make the connections
    for (int i = 0; i <= currentLevel; i++) {
      for (NodeDist node : neighbourList) {
        int index = node.index;
        if (index != inputIndex) {
          graph.get(i).get(inputIndex).add(index);
          pruneNeighborList(graph.get(i).get(inputIndex), inputIndex, i, num);
          if (graph.get(i).containsKey(index)) {
            graph.get(i).get(index).add(inputIndex);
            pruneNeighborList(graph.get(i).get(index), index, i, num);
          }

        }
      }
    }

  }

  public List<Vector> fetchBestKNodes(Vector inputVector, int K) {
    int inputIndex = inputVector.index;
    List<Vector> output = new ArrayList<>();
    PriorityQueue<NodeDist> neighbourList = returnKNeighbours(entryPoint,
        inputVector, inputIndex, maxLevel, K);
    for (NodeDist node : neighbourList) {
      int index = node.index;
      output.add(vectors.get(index));
    }
    return output;
  }

  public int[][] returnPoints() {
    int[][] output = new int[vectors.size()][3];
    for (int i = 0; i < vectors.size(); i++) {
      Vector vec = vectors.get(i);
      output[i][0] = vec.index;
      output[i][1] = (int) vec.x;
      output[i][2] = (int) vec.y;
    }
    return output;
  }

  public int[][] returnConnections() {
    List<int[]> connections = new ArrayList<>();
    Set<Integer> keys = graph.get(0).keySet();

    for (int key : keys) {
      for (int neighbor : graph.get(0).get(key)) {
        // To avoid duplicate edges like (2,3) and (3,2), keep only one direction
        connections.add(new int[] { key, neighbor });

      }
    }

    // Convert List<int[]> to int[][]
    int[][] output = new int[connections.size()][2];
    for (int i = 0; i < connections.size(); i++) {
      output[i] = connections.get(i);
    }

    return output;
  }

  public void printGraph() {
    for (int i = maxLevel; i >= 0; i--) {
      Set<Integer> keys = graph.get(i).keySet();
      System.out.println("Level " + i);
      for (int key : keys) {
        System.out.println("Node: " + key + "->" + graph.get(i).get(key));
      }
      System.out.println();
    }
  }

  public void printGraphRelations() {
    System.out.println();
    System.out.println("Graph at iteration");
    Set<Integer> keys = graph.get(0).keySet();
    for (int key : keys) {
      for (int value : graph.get(0).get(key)) {
        System.out.println(key + " " + value);
      }
    }
  }

  public void printGraphingRelations() {
    System.out.println();
    System.out.println("Graph at iteration");
    Set<Integer> keys = graph.get(0).keySet();
    for (int key : keys) {
      for (int i = 0; i < graph.get(0).get(key).size(); i++) {
        List<Integer> valueList = new ArrayList<>(graph.get(0).get(key));
        double x1 = vectors.get(key).x, y1 = vectors.get(key).y;
        double x2 = vectors.get(valueList.get(i)).x, y2 = vectors.get(valueList.get(i)).y;
        System.out.printf("Vector((%.1f, %.1f), (%.1f, %.1f))\n", x1, y1, x2, y2);
      }
    }
  }

  // helper functions
  int generateLevel(int numLevel) {
    int level = 0;
    double num = 1 / Math.E;

    while (Math.random() < num && level < numLevel - 1) {
      level++;
    }
    return level;
  }

  int greedySearch(int nodeIndex, Vector inputNode, int level) {
    Vector node = vectors.get(nodeIndex); // O(1)
    double smallestDistance = node.vectorDistance(inputNode); // find the parent distance
    int smallestNode = nodeIndex;

    Set<Integer> neighbours = graph.get(level).get(nodeIndex);
    // if the node doesn't have any neighbours
    if (neighbours == null || neighbours.isEmpty())
      return nodeIndex;

    // check the distances of all the neighbours of the node
    for (int neigh : neighbours) {
      Vector neigh_node = vectors.get(neigh);
      double dist = neigh_node.vectorDistance(inputNode);
      if (dist < smallestDistance) {
        smallestDistance = dist;
        smallestNode = neigh;
      }
    }
    if (smallestNode == nodeIndex)
      return nodeIndex;
    else
      return greedySearch(smallestNode, inputNode, level);
  }

  void pruneNeighborList(Set<Integer> neighborSet, int baseIndex, int level, int M) {
    if (neighborSet.size() <= M)
      return;

    Vector baseVector = vectors.get(baseIndex);
    List<Integer> neighborList = new ArrayList<>(neighborSet);
    neighborList.sort((i1, i2) -> Double.compare(
        baseVector.vectorDistance(vectors.get(i1)),
        baseVector.vectorDistance(vectors.get(i2))));

    neighborSet.clear();
    for (int i = 0; i < M; i++) {
      neighborSet.add(neighborList.get(i));
    }
  }

  PriorityQueue<NodeDist> returnKNeighbours(int entryPoint, Vector inputNode, int inputIndex, int maxLevel,
      int K) {
    // finding best local entrypoint greedy search
    int bestEntryPoint = entryPoint;
    for (int i = maxLevel; i > 0; i--) {
      bestEntryPoint = greedySearch(entryPoint, inputNode, i);
    }
    double entryPointDist = vectors.get(bestEntryPoint).vectorDistance(inputNode);

    // create a new openList
    PriorityQueue<NodeDist> openList = new PriorityQueue<>((a, b) -> Double.compare(a.distance, b.distance));
    PriorityQueue<NodeDist> closedList = new PriorityQueue<>((a, b) -> Double.compare(b.distance, a.distance));
    Set<Integer> seen = new HashSet<>();
    seen.add(inputIndex);

    /*
     * steps
     * 1. add to open list
     * 2. while open list is not empty
     * -> extract best item from open
     * -> if(best << worst) break;
     * -> get children of best and add to open list
     * -> if closed < n -> add best to closed list
     * -> if best >> worst -> remove worst and add best
     */
    openList.add(new NodeDist(bestEntryPoint, entryPointDist));
    while (!openList.isEmpty()) {
      NodeDist bestNode = openList.poll(); // extracting the best node
      NodeDist worstNode = closedList.peek();

      // add all items to the openList
      for (int neigh : graph.get(0).get(bestNode.index)) {
        if (!seen.contains(neigh)) {
          seen.add(neigh);
          double neighDist = vectors.get(neigh).vectorDistance(inputNode);
          openList.add(new NodeDist(neigh, neighDist));
        }
      }
      // Avoid adding the input node itself
      if (bestNode.index == inputIndex)
        continue;

      // Avoid duplicate entries in closedList
      boolean alreadyInClosed = false;
      for (NodeDist n : closedList) {
        if (n.index == bestNode.index) {
          alreadyInClosed = true;
          break;
        }
      }
      if (alreadyInClosed)
        continue;

      if (closedList.size() < K) {
        closedList.add(bestNode);
      } else if (bestNode.distance < worstNode.distance) {
        closedList.poll();
        closedList.add(bestNode);
      }

    }
    return closedList;
  }
}

// creating a 2d vector class

class NodeDist {
  int index;
  double distance;

  NodeDist(int index, double distance) {
    this.index = index;
    this.distance = distance;
  }
}