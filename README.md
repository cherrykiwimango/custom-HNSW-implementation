# Custom HNSW implementation using Java

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

An implementation of Hierarchical Navigable Small Graphs with insert and fetch operations. Handles 2d vectors of the form of a custom class.

<img width="350" height="336" alt="hnswgraph (1)" src="https://github.com/user-attachments/assets/4b5d8f1b-b7b4-4346-9ba1-84ef62830a4c" />


## Demo Video

https://github.com/user-attachments/assets/1d79d7fd-a73e-41a6-b67a-4a7d0880d593


## Features

### Insertion into the graph

- Uses greedy search and best-first search algorithms to find the best n nodes to connect to
- Assigns a random level to each node using a probabilistic random number generator (As the levels increase the probability of generation decreases)
- Uses a Map to store the vectors index in the form of adjacency lists representing the graph

### Fetching N closest nodes

- Uses the same greedy search and best-first search approach to find the best n nodes
- Conducts the search from the sparse top level, down, quickly avoiding far nodes improving efficiency
- Output can be fetched in many forms suitable for graph generation, plotting on a 2d plane or simply the vector information

## Get Started

### 1. To use the class

### Use the SimpleVisualizer class 
- Write your implementation inside the simple visualizer class which imports both the HNSW and the Vector packages
- Comment out the existing code and write your own functions

or 

### Use your own class
- Make sure to add the following imports to your file
```bash
import hnsw.*;
```

### 2. Vector format
- Initialize an object of the Vector class that takes 2 double values. Follow the below example:
```bash
Vector object = new Vector(1.2, 2.3)
```
- There is an id property that is used to index the vectors internally
- The vector supported is of a form as defined below:
```bash
public class Vector{
  public double x, y;
  public int index;

  public Vector(double x, double y){
    this.x = x;
    this.y = y;
  }
```


