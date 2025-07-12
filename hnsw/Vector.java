package hnsw;

public class Vector {
  public double x, y;
  public int index;

  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  // method finds the euclidean distance
  double vectorDistance(Vector vec) {
    return Math.hypot(this.x - vec.x, this.y - vec.y); // O(1)
  }
}
