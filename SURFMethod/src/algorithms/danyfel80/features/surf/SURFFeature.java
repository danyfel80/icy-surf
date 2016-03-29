package algorithms.danyfel80.features.surf;

import java.util.ArrayList;
import java.util.List;

import icy.sequence.Sequence;

/**
 * Represents a SURF feature with a key point and a list of the descriptors available for the key point.
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFFeature {
  /**
   * The key point.
   */
  private KeyPoint keyPoint;
  /**
   * The descriptor list for each octave-interval combination.
   */
  private List<Descriptor> descriptors;
  
  /**
   * Constructor
   * @param keyPoint The key point of the feature
   * @param descriptors The descriptor list of the feature
   */
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
  
  /**
   * Sets the descriptors of the found features.
   * @param intSeq Integral sequence
   */
  public static void setupDescriptors(Sequence intSeq, List<SURFFeature> features) {
    for (SURFFeature feature : features) {
      KeyPoint kp = feature.getKeyPoint();
      double scale = kp.getScale();
      List<Descriptor> descriptors = new ArrayList<>();
      // Divide in a 4x4 zone the space around the interest point

      // First compute the orientation
      double cosP = Math.cos(kp.getOrientation());
      double sinP = Math.sin(kp.getOrientation());
      double norm = 0, u, v, gauss, responseU, responseV, responseX, responseY;

      // Divide in 16 sectors the space around the interest point.
      for (int i = 0; i < SURFMethodUtils.DESCRIPTOR_SIZE; i++) {
        for (int j = 0; j < SURFMethodUtils.DESCRIPTOR_SIZE; j++) {
          double sumDx = 0, sumDy = 0, sumAbsDx = 0, sumAbsDy = 0;

          // Then each 4x4 is subsampled into a 5x5 zone
          for (int k = 0; k < 5; k++) {
            for (int l = 0; l < 5; l++) {
              // We precompute Haar answers
              u = kp.getX() + scale*(cosP*((i - 2)*5 + k + 0.5) - sinP*((j - 2)*5 + l + 0.5));
              v = kp.getY() + scale*(sinP*((i - 2)*5 + k + 0.5) + cosP*((j - 2)*5 + l + 0.5));

              // (u,v) are already translated of 0.5, which means
              // that there is no round-off to perform: one takes
              // the integer part of the coordinates.
              responseX = IntegralSequenceUtil.haarX(intSeq, (int)u, (int)v, (int)Math.round(scale));
              responseY = IntegralSequenceUtil.haarY(intSeq, (int)u, (int)v, (int)Math.round(scale));

              // Gaussian weight
              gauss = SURFMethodUtils.gaussian(((i - 2)*5 + k + 0.5), ((j - 2)*5 + l + 0.5), 3.3);

              // Rotation of the axis
              responseU = gauss*(responseX*cosP + responseY*sinP);
              responseV = gauss*(-responseX*sinP + responseY*cosP);

              // The descriptors
              sumDx += responseU;
              sumDy += responseV;
              sumAbsDx += Math.abs(responseU);
              sumAbsDy += Math.abs(responseV);
            }
          }

          // Compute the norm of the vector
          norm += sumDx*sumDx + sumDy*sumDy + sumAbsDx*sumAbsDx + sumAbsDy*sumAbsDy;

          descriptors.add(new Descriptor(sumDx, sumDy, sumAbsDx, sumAbsDy));
        }
      }

      norm = Math.sqrt(norm);
      if (norm != 0.0) {
        for (int i = 0; i < SURFMethodUtils.DESCRIPTOR_SIZE*SURFMethodUtils.DESCRIPTOR_SIZE; i++) {
          Descriptor d = descriptors.get(i);
          d.setSumDx(d.getSumDx()/norm);
          d.setSumDy(d.getSumDy()/norm);
          d.setSumAbsDx(d.getSumAbsDx()/norm);
          d.setSumAbsDy(d.getSumAbsDy()/norm);
        }
      }

      feature.setDescriptors(descriptors);
    }
  }
}
