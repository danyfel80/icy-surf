/**
 * 
 */
package algorithms.danyfel80.features.surf;

import javax.vecmath.Point2i;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

/**
 * Class that handles any 2D image except the integral images
 * @author Daniel Felipe Gonzalez Obando
 */
public class RegularSequence {

  private int sx, sy;
  private Sequence sequence;
  private double[] sequenceData;

  /**
   * Constructor based on an existing regular sequence. No data copy.
   * @param seq
   */
  public RegularSequence(RegularSequence seq) {
    sx = seq.getWidth();
    sy = seq.getHeight();
    sequence = new Sequence(new IcyBufferedImage(sx, sy, 1, DataType.DOUBLE));
    sequenceData = sequence.getDataXYAsDouble(0, 0, 0);
  }
  
  /**
   * Constructor based on an existing sequence. No cloning of data.
   * @param sequence
   */
  public RegularSequence(Sequence sequence) {
    this.sequence = sequence;
    sequenceData = this.sequence.getDataXYAsDouble(0, 0, 0);
    sx = this.sequence.getWidth();
    sy = this.sequence.getHeight();
  }
  
  /**
   * Constructor based on array of values.
   * @param width
   * @param height
   * @param data
   */
  public RegularSequence(int width, int height, double[] data) {
    this.sequence = new Sequence(new IcyBufferedImage(width, height, 1, DataType.DOUBLE));
    sequenceData = this.sequence.getDataXYAsDouble(0, 0, 0);
    sx = width;
    sy = height;
    
    for (int x = 0; x < sx; x++) {
      for (int y = 0; y < sy; y++) {
        setValue(x, y, data[x + y*sx]);
      }
    }
    sequence.dataChanged();
  }

  /**
   * Constructor that creates an sequence the size of the specified parameters. 
   * @param width
   * @param height
   */
  public RegularSequence(int width, int height) {
    sequence = new Sequence(new IcyBufferedImage(width, height, 1, DataType.DOUBLE));
    sequenceData = sequence.getDataXYAsDouble(0, 0, 0);
    sx = width;
    sy = height;
  }

  /**
   * Linearly sets the min and max value of an image to be 0 dans 255
   */
  void normalizeImage() {
    double min = getValue(0, 0), max = getValue(0, 0);
    int i, j;
    for (i = 0; i < sx; i++) {
      for(j = 0; j < sy; j++) {
        min = (getValue(i, j) < min)? getValue(i, j): min;
        max = (getValue(i, j) > max)? getValue(i, j): max;
      }
    }

    for (i = 0; i < sx; i++) {
      for (j = 0; j < sy; j++) {
        setValue(i, j, 255.0*((getValue(i, j) - min)/(max - min)));
      }
    }
    sequence.dataChanged();
  }

  /**
   * Returns the sample image size depending on the amount of samples required.
   * @param sampleNum Amount of samples required in each direction.
   * @return Sample size
   */
  public Point2i getSampleSize(int sampleNum) {
    return new Point2i(sx/sampleNum, sy/sampleNum);
  }

  /**
   * Pad an image, and allocate some memory according to padding
   * @param inputSequence
   * @param padding
   * @return padded image
   */
  public RegularSequence padImage(int padding) {
    RegularSequence paddedSequence = new RegularSequence(
            sx + 2*padding, sy + 2*padding);
    
    int ix, iy;
    for (int ox = -padding; ox < sx + padding; ox++) {
      for (int oy = -padding; oy < sy + padding; oy++) {
        ix = ox;
        iy = oy;

        if (ix < 0) {
          ix = -ix;
        }
        if (iy < 0) {
          iy = -iy;
        }

        ix %= 2*sx;
        iy %= 2*sy;

        if (ix >= sx) {
          ix = 2*sx -ix - 1;
        }
        if (iy >= sy) {
          iy = 2*sy -iy - 1;
        }

        paddedSequence.setValue(ox + padding, oy + padding, getValue(ix, iy));
      }
    }
    paddedSequence.dataChanged();
    return paddedSequence;
  }

  /**
   * Setter
   * @param x
   * @param y
   * @param value
   */
  public void setValue(int x, int y, double value) {
    sequenceData[x + y*sx] = value;
  }

  /**
   * Use when finishing an update in the sequence.
   */
  public void dataChanged() {
    sequence.dataChanged();
  }

  /**
   * Getter
   * @param x
   * @param y
   * @return value at (x, y) of image.
   */
  public double getValue(int x, int y) {
    return sequenceData[x + y*sx];
  }

  public int getWidth() {
    return sx;
  }

  public int getHeight() {
    return sy;
  }

  public Sequence getSequence() {
    return sequence;
  }

  public double[] getData() {
    return this.sequenceData;
  }
}
