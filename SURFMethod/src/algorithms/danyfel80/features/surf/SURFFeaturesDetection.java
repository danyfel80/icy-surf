package algorithms.danyfel80.features.surf;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2i;
import javax.vecmath.Point3d;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

/**
 * SURF features detection class. This class holds the main logic for feature 
 * detection using the SURF method.
 * Based on the c++ version developed by Edouard Oyallon and Julien Rabin 
 * (available at http://dx.doi.org/10.5201/ipol.2015.69)
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFFeaturesDetection {

  // Input parameters
  /**
   * 2D Image to extract features from.
   */
  private Sequence sequence;
  /**
   * Threshold for detection of the Hessian.
   */
  private Integer threshold;

  // Internal parameters
  private List<SURFFeature> features;


  /**
   * Constructor taking a sequence and the hessian threshold
   * @param sequence 2D Image to extract features from.
   * @param threshold Threshold for detection of the Hessian.
   */
  public SURFFeaturesDetection(Sequence sequence, Integer threshold) {
    this.sequence = sequence;
    this.threshold = threshold;
  }

  /**
   * Computes the features the first time the method is called.
   * @return The resulting features.
   */
  public List<SURFFeature> findFeatures() {
    if (features == null) {
      features = new ArrayList<SURFFeature>();
      
      Sequence normSeq = NormalSequenceUtil.normalizeSequence(sequence);
      Sequence intSeq = IntegralSequenceUtil.getIntegralSequence(normSeq);

      List<Sequence> hessian = new ArrayList<Sequence>(SURFMethodUtils.MAX_INTERVAL);
      List<Sequence> laplacianSign = new ArrayList<Sequence>(SURFMethodUtils.MAX_INTERVAL);

      // calculate on each octave

      double Dxx, Dxy, Dyy;
      int octave, interval, x, y, w, h, xcoo, ycoo, lp1, l3, mlp1p2, lp1d2, l2p1, pow, sample, l;
      double nxy,nxx;

      for (octave = 0; octave < SURFMethodUtils.MAX_OCTAVE; octave++) {
        pow = (int)Math.round(Math.pow(2, octave+1));
        sample = (int)Math.round(Math.pow(SURFMethodUtils.SAMPLING, octave)); // Sample step
        Point2i wh = NormalSequenceUtil.getSampleSize(normSeq, sample);
        w = wh.x;
        h = wh.y;

        // Memory initialization
        for (interval = 0; interval < SURFMethodUtils.MAX_INTERVAL; interval++)
        {
          hessian.add(new Sequence("hess", new IcyBufferedImage(w, h, 1, DataType.DOUBLE)));
          laplacianSign.add(new Sequence("laps", new IcyBufferedImage(w, h, 1, DataType.DOUBLE)));
        }

        for (interval = 0; interval < SURFMethodUtils.MAX_INTERVAL; interval++) {
          l = pow*(interval + 1) + 1; // L in article

          // These variables are precomputed to allow fast computations.
          // They correspond exactly to the Gamma of the formula given in the article for
          // the second order filters.
          lp1 = -l + 1;
          l3 = 3*l;
          lp1d2 = (-l + 1)/2;
          mlp1p2 = (-l + 1)/2 - l;
          l2p1 = 2*l - 1;

          nxx=Math.sqrt(6*l*(2*l - 1));// Frobenius norm of the xx and yy filters
          nxy=Math.sqrt(4*l*l);// Frobenius of the xy filter.

          double[] hessData = hessian.get(interval).getDataXYAsDouble(0, 0, 0);
          double[] lapsData = laplacianSign.get(interval).getDataXYAsDouble(0, 0, 0);

          // These are the time consuming loops that compute the Hessian at each points.    
          for (y = 0; y < h; y++) {
            for (x = 0; x < w; x++) {
              // Sampling
              xcoo = x*sample;
              ycoo = y*sample;

              // Second order filters
              Dxx = IntegralSequenceUtil.squareConvolutionXY(intSeq, lp1, mlp1p2, l2p1, l3, xcoo, ycoo) - 
                  3*IntegralSequenceUtil.squareConvolutionXY(intSeq, lp1, lp1d2, l2p1, l, xcoo, ycoo);
              Dxx /= nxx;

              Dyy = IntegralSequenceUtil.squareConvolutionXY(intSeq, mlp1p2, lp1, l3, l2p1, xcoo, ycoo) - 
                  3*IntegralSequenceUtil.squareConvolutionXY(intSeq, lp1d2, lp1, l, l2p1, xcoo, ycoo);
              Dyy /= nxx;
              Dxy = IntegralSequenceUtil.squareConvolutionXY(intSeq, 1, 1, l, l, xcoo, ycoo) + 
                  IntegralSequenceUtil.squareConvolutionXY(intSeq, 0, 0, -l, -l, xcoo, ycoo) + 
                  IntegralSequenceUtil.squareConvolutionXY(intSeq, 1, 0, l, -l, xcoo, ycoo) + 
                  IntegralSequenceUtil.squareConvolutionXY(intSeq, 0, 1, -l, l, xcoo, ycoo);

              Dxy /= nxy;

              // Computation of the Hessian and Laplacian
              hessData[x + y*w]= (Dxx*Dyy - 0.8317*(Dxy*Dxy));
              lapsData[x + y*w] = (Dxx + Dyy > 0) ? 1.0: 0.0;
            }
          }
        }

        double x_, y_, s_;

        // Detect keypoints
        for (interval = 1; interval < SURFMethodUtils.MAX_INTERVAL - 1; interval++) {
          l = pow*(interval + 1) + 1;

          // border points are removed
          for (y = 1; y < h - 1; y++) {
            for (x = 1 ; x < w - 1; x++) {
              if (KeyPoint.isMaximum(hessian, x, y, interval, threshold))
              {
                //System.out.println("Maximum (" + x + ", " + y + ")");
                x_ = x*sample;
                y_ = y*sample;
                s_ = 0.4*(pow*(interval + 1) + 2); // box size or scale
                Point3d coord = new Point3d(x_, y_, s_);

                // Affine refinement is performed for a given octave and sampling
                if(KeyPoint.interpolationScaleSpace(hessian, x, y, interval, coord, sample, pow)) {
                  x_ = coord.x;
                  y_ = coord.y;
                  s_ = coord.z;
                  //System.out.println("kp added!");
                  double[] lapSignData= laplacianSign.get(interval).getDataXYAsDouble(0, 0, 0);
                  KeyPoint.addKeyPoint(intSeq, x_, y_, lapSignData[x + (y*w)] == 1.0, s_, features);
                }
              }
            }
          }
        }
      }

      // Compute the descriptors
      SURFFeature.setupDescriptors(intSeq, features);
    }

    return features;
  }
  
  

}
