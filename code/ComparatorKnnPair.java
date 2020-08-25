

import java.util.Comparator;



public class ComparatorKnnPair implements Comparator<KnnPair>{
  @Override
  public int compare(KnnPair kp1, KnnPair kp2) {
    // TODO Auto-generated method stub
    double distance1 = kp1.getDistance();
    double distance2 = kp2.getDistance();
    
    if (distance1 > distance2) {
      return 1;
    } else if (distance1 < distance2) {
      return -1;
    }
    
    return 0;
  }

}
