package algorithms.features.surf;

import java.util.List;

public class SURFFeature {
  private KeyPoint keyPoint;
  private List<Descriptor> descriptors;
  
  
  public SURFFeature(KeyPoint keyPoint, List<Descriptor> descriptors) {
    super();
    this.keyPoint = keyPoint;
    this.descriptors = descriptors;
  }

  public KeyPoint getKeyPoint() {
    return keyPoint;
  }
  public void setKeyPoint(KeyPoint keyPoint) {
    this.keyPoint = keyPoint;
  }

  public List<Descriptor> getDescriptors() {
    return descriptors;
  }
  public void setDescriptors(List<Descriptor> descriptors) {
    this.descriptors = descriptors;
  }
  
  
}
