

public class KnnPair {
  private double distance;
  private int index;
  
  public KnnPair(double distance, int index) {
    setDistance(distance);
    setIndex(index);
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
