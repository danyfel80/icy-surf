/**
 * 
 */
package algorithms.danyfel80.features.surf;

import javax.vecmath.Point2i;

import icy.sequence.Sequence;

/**
 * Normal sequence utility algorithms.
 * @author Daniel Felipe Gonzalez Obando
 */
public class NormalSequenceUtil {
  /**
   * Computes the size of sampling.
   * @param seq the sequence to sample.
   * @param sample the amount of samples to obtain in each direction.
   * @return The size of sampling
   */
  public static Point2i getSampleSize(Sequence seq, int sample) {
    return new Point2i(seq.getWidth()/sample, seq.getHeight()/sample); 
  }

  /**
   * Normalizes the sequence using the minimum and maximum values in the sequence.
   * @param seq The sequence to normalize.
   * @return The sequence normalized.
   */
  public static Sequence normalizeSequence(Sequence seq) {
    double minVal = seq.getChannelMax(0);
    double maxVal = seq.getChannelMax(0);

    Sequence resSeq = new Sequence();
    resSeq.copyFrom(seq, true);
    resSeq.beginUpdate();
    double[] resData = resSeq.getDataCopyXYAsDouble(0, 0, 0);
    for (int i = 0; i < resData.length; i++) {
      resData[i] = 255.0 * ((resData[i] - minVal)/(maxVal-minVal));
    }
    resSeq.dataChanged();
    resSeq.endUpdate();

    return resSeq;
  }
}
