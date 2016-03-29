package algorithms.danyfel80.features.surf;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

/**
 * Utility class for Integral sequences, specially concerning the padding-dependent coordinates.
 * @author Daniel Felipe Gonzalez Obando
 */
public class IntegralSequenceUtil {

  public static int padding = 312; // sizeDescriptor * maxSizeL = 4*0.4*195;

  /**
   * Creates an integral sequence, with the padding and mirroring.
   * @param seq Base sequence
   * @return Integral sequence
   */
  public static Sequence getIntegralSequence(Sequence seq) {
    Sequence padSeq = new Sequence(seq.getName() + "_Padded");
    padSequence(seq, padSeq, padding);

    Sequence intSeq = new Sequence(seq.getName() + "_Integral");
    computeIntegralImage(padSeq, intSeq);

    return intSeq;
  }

  /**
   * Transforms a x,y coordinate to a valid single-number coordinate in the integral sequence.
   * @param intSeq Integral sequence
   * @param x x-coordinate to transform
   * @param y y-coordinate to transform
   * @return single-number coordinate in the integral sequence
   */
  public static int getIntegralCoordinates(Sequence intSeq, int x, int y) {
    return (x + padding) + (y+padding) * intSeq.getWidth();
  }

  /**
   * Creates the mirrored and padded image to perform the integration.
   * @param seq Base sequence
   * @param padSeq Sequence to stock mirrored and padded image. 
   * @param padding
   */
  private static void padSequence(Sequence seq, Sequence padSeq, int padding) {

    double[] seqData = seq.getDataXYAsDouble(0, 0, 0);


    int sizeX = seq.getSizeX();
    int sizeY = seq.getSizeY();

    int sizePX = sizeX + 2*padding;
    int sizePY = sizeY + 2*padding;

    IcyBufferedImage intBI = new IcyBufferedImage(
        sizePX, sizePY, 1, DataType.DOUBLE);

    padSeq.beginUpdate();
    {
      padSeq.setImage(0, 0, intBI);
      double[] padData = padSeq.getDataXYAsDouble(0, 0, 0);

      int x0, y0;
      for (int x = -padding; x < sizeX + padding; x++) {
        for (int y = -padding; y < sizeY + padding; y++) {
          x0 = x;
          if (x0 < 0)
            x0 = -x0;
          x0 = x0 % (2*sizeX);
          if (x0 >= sizeX)
            x0 = 2*sizeX -x0 -1;

          y0 = y;
          if (y0 < 0)
            y0 = -y0;

          y0 = y0 % (2*sizeY);
          if (y0 >= sizeY)
            y0 = 2*sizeY -y0 -1;

          padData[(x+padding) + ((y+padding)*sizePX)] = seqData[x0 + (y0*sizeX)];
        }
      }

      padSeq.dataChanged();
    }
    padSeq.endUpdate();
  }
  
  /**
   * Computes the integration of the input sequence
   * @param padSeq Padded and mirrored sequence
   * @param intSeq The sequence where the integration result is stocked.
   */
  private static void computeIntegralImage(Sequence padSeq, Sequence intSeq) {
    double[] padData = padSeq.getDataXYAsDouble(0, 0, 0);
    int sizeX = padSeq.getSizeX();
    int sizeY = padSeq.getSizeY();

    intSeq.beginUpdate();
    {
      intSeq.setImage(0, 0, new IcyBufferedImage(sizeX, sizeY, 1, DataType.DOUBLE));
      double[] intData = intSeq.getDataXYAsDouble(0, 0, 0);

      // Initialization
      intData[0] = padData[0];

      // First row
      for (int x = 1; x < sizeX; x++) {
        intData[x] = intData[x-1] + padData[x];
      }

      // Recursion
      for (int y = 1; y < sizeY; y++) {
        double h = 0.0;
        for (int x = 0; x < sizeX; x++) {
          h += padData[x + y*sizeX];
          intData[x + y*sizeX] = intData[x + (y-1)*sizeX] + h;
        }
      }
      intSeq.dataChanged();
    }
    intSeq.endUpdate();
  }

  /**
   * Convolution by a square defined by the bottom-left (a,b) and 
   * top-right (c,d).
   * Note: No L2-normalization is performed here.
   * @param intSeq Integral sequence.
   * @param a x-coordinate of the bottom-left square.
   * @param b y-coordinate of the bottom-left square.
   * @param c x-coordinate of the top-right square.
   * @param d y-coordinate of the top-right square.
   * @param x x-coordinate of center of convolution. 
   * @param y y-coordinate of center of convolution.
   * @return
   */
  public static double squareConvolutionXY(Sequence intSeq, int a, int b, int c,
      int d, int x, int y) {
    int a1 = x - a;
    int a2 = y - b;
    int b1 = a1 - c;
    int b2 = a2 - d;


    double[] intData = intSeq.getDataXYAsDouble(0, 0, 0);
    return intData[getIntegralCoordinates(intSeq, b1, b2)] + 
        intData[getIntegralCoordinates(intSeq, a1, a2)] - 
        intData[getIntegralCoordinates(intSeq, b1, a2)] - 
        intData[getIntegralCoordinates(intSeq, a1, b2)];
    // Note: No L2-normalization is performed here.
  }

  /**
   * Convolution by a box [-1,+1]
   * @param intSeq sequence where the convolution is performed
   * @param x x coordinate
   * @param y y coordinate
   * @param lambda lambda value
   * @return result of convolution
   */
  public static int haarX(Sequence intSeq, int x, int y, int lambda) {
    return -((int)squareConvolutionXY(intSeq, 1, -lambda - 1, -lambda - 1, lambda*2 + 1, x, y) + 
        (int)squareConvolutionXY(intSeq, 0, -lambda - 1, lambda + 1, lambda*2 + 1, x, y));
  }

  /**
   * Convolution by a box [-1;+1]
   * @param intSeq sequence where the convolution is performed
   * @param x x coordinate
   * @param y y coordinate
   * @param lambda lambda value
   * @return result of convolution
   */
  public static int haarY(Sequence intSeq, int x, int y, int lambda) {
    return -((int)squareConvolutionXY(intSeq, -lambda - 1, 1, lambda*2 + 1, -lambda - 1, x, y) + 
        (int)squareConvolutionXY(intSeq, -lambda - 1, 0, lambda*2 + 1, lambda + 1, x, y));
  }
}
