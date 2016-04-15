/**
 * 
 */
package algorithms.danyfel80.features.surf;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import icy.sequence.Sequence;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFKeyPointsMatch {

  Sequence                           sequence1;
  Sequence                           sequence2;
  List<Descriptor>                   descriptors1;
  List<Descriptor>                   descriptors2;

  List<Pair<Descriptor, Descriptor>> matches;

  /**
   * Constructor
   */
  public SURFKeyPointsMatch(Sequence seq1, Sequence seq2, List<Descriptor> des1,
      List<Descriptor> des2) {
    this.sequence1 = seq1;
    this.sequence2 = seq2;
    this.descriptors1 = des1;
    this.descriptors2 = des2;
  }

  /**
   * Finds matches between the given sets of descriptors
   */
  public void matchDescriptors() {
    matches = findMatches(descriptors1, descriptors2);
    cleanMatches(matches);
  }

  private List<Pair<Descriptor, Descriptor>> findMatches(
      List<Descriptor> descriptors1, List<Descriptor> descriptors2) {
    List<Pair<Descriptor, Descriptor>> matches = new ArrayList<>();

    // The match uses a ratio between a selected descriptor of l1 and the
    // two closest descriptors of l2.
    float thrm = SURFMethodUtils.RATE * SURFMethodUtils.RATE;

    // Matching is not symmetric.
    int i, j, position;
    float d1, d2, d;
    for (i = 0; i < descriptors1.size(); i++) {
      position = -1;
      d1 = 3;
      d2 = 3;

      for (j = 0; j < descriptors2.size(); j++) {
        d = euclideanDistance(descriptors1.get(i), descriptors2.get(j));
        // We select the two closes descriptors
        if (descriptors1.get(i).getKeyPoint().isSignLaplacian() == descriptors2
            .get(j).getKeyPoint().isSignLaplacian()) {
          d2 = (d2 > d)? d: d2;
          if (d1 > d) {
            position = j;
            d2 = d1;
            d1 = d;
          }
        }
      }

      // Try to match it
      if (position >= 0 && thrm * d2 > d1) {
        Pair<Descriptor, Descriptor> match = new Pair<Descriptor, Descriptor>(
            descriptors1.get(i), descriptors2.get(position));
        matches.add(match);
      }
    }
    return matches;
  }

  private float euclideanDistance(Descriptor descriptor1,
      Descriptor descriptor2) {
    float sum = 0;
    for (int i = 0; i < 16; i++) {
      sum += (descriptor1.getVectorDescriptors().get(i).getSumDx()
          - descriptor2.getVectorDescriptors().get(i).getSumDx())
          * (descriptor1.getVectorDescriptors().get(i).getSumDx()
              - descriptor2.getVectorDescriptors().get(i).getSumDx())
          + (descriptor1.getVectorDescriptors().get(i).getSumDy()
              - descriptor2.getVectorDescriptors().get(i).getSumDy())
              * (descriptor1.getVectorDescriptors().get(i).getSumDy()
                  - descriptor2.getVectorDescriptors().get(i).getSumDy())
          + (descriptor1.getVectorDescriptors().get(i).getSumAbsDy()
              - descriptor2.getVectorDescriptors().get(i).getSumAbsDy())
              * (descriptor1.getVectorDescriptors().get(i).getSumAbsDy()
                  - descriptor2.getVectorDescriptors().get(i).getSumAbsDy())
          + (descriptor1.getVectorDescriptors().get(i).getSumAbsDx()
              - descriptor2.getVectorDescriptors().get(i).getSumAbsDx())
              * (descriptor1.getVectorDescriptors().get(i).getSumAbsDx()
                  - descriptor2.getVectorDescriptors().get(i).getSumAbsDx());
    }
    return sum;
  }

  /**
   * Cleans the multiple-to-one in SURF.
   */
  private void cleanMatches(List<Pair<Descriptor, Descriptor>> matches) {
    boolean[] toRemove = new boolean[matches.size()];

    int i, j, x, y, x_, y_;
    for (i = 0; i < matches.size(); i++) {
      x = (int) matches.get(i).getValue1().getKeyPoint().getX();
      y = (int) matches.get(i).getValue1().getKeyPoint().getY();
      if (!toRemove[i]) {
        for (j = i + 1; j < matches.size(); j++) {
          // Check if i is j
          x_ = (int) matches.get(j).getValue1().getKeyPoint().getX();
          y_ = (int) matches.get(j).getValue1().getKeyPoint().getY();

          if (x_ == x && y == y_) {
            toRemove[i] = true;
            toRemove[j] = true;
          }
        }
      }
    }

    for (i = matches.size() - 1; i >= 0; i--) {
      if (toRemove[i]) {
        matches.remove(i);
      }
    }
  }

  public List<Pair<Descriptor, Descriptor>> getMatches() {
    return matches;
  }

}
