package algorithms.features.surf;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2i;

public class SURFFeatureDetection {

  // Static values
  public static final int MAX_INTERVAL = 4;
  public static final int MAX_OCTAVE = 4;
  public static final int SAMPLING = 2;
  
  
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
   * @param sequence 2D Image to extract features from.
   * @param threshold Threshold for detection of the Hessian.
   */
  public SURFFeatureDetection(Sequence sequence, Integer threshold) {
    this.sequence = sequence;
  }

  public List<SURFFeature> getFeatures() {
    if (features == null) {
      Sequence normSeq = normalizeSequence(sequence);
      Sequence intSeq = IntegralSequenceUtil.getIntegralSequence(normSeq);
      
      List<Sequence> hessian = new ArrayList<Sequence>(MAX_INTERVAL);
      List<Sequence> laplacianSign = new ArrayList<Sequence>(MAX_INTERVAL);
      
      // calculate on each octave
      
      double Dxx, Dxy, Dyy;
      int octave, interval, x, y, w, h, xcoo, ycoo, lp1, l3, mlp1p2, lp1d2, l2p1, pow, sample, l;
      double nxy,nxx;
      
      for (octave = 0; octave < MAX_OCTAVE; octave++) {
        pow = (int)Math.round(Math.pow(2, octave+1));
        sample = (int)Math.round(Math.pow(SAMPLING, octave)); // Sample step
        Point2i wh = getSampleSize(sequence, sample);
        w = wh.x;
        h = wh.y;
        
        // Memory initialization
        for (interval = 0; interval < MAX_INTERVAL; interval++)
        {
          hessian.add(new Sequence("hess", new IcyBufferedImage(w, h, 1, DataType.DOUBLE)));
          laplacianSign.add(new Sequence("laps", new IcyBufferedImage(w, h, 1, DataType.DOUBLE)));
        }
        
        for (interval = 0; interval < MAX_INTERVAL; interval++) {
          l = pow*(interval + 1) + 1; // L in article
          
          // These variables are precomputed to allow fast computations.
          // They correspond exactly to the Gamma of the formula given in the article for
          // the second order filters.
          lp1 = - l + 1;
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
              lapsData[x + y*w] = (Dxx + Dyy > 0) ? 1: 0;
            }
          }
        }
      }
      // TODO continue here
//      int x_,y_,s_;
//      
//      // Detect keypoints
//      for (interval = 1; interval < MAX_INTERVAL - 1; interval++) {
//        l = (pow*(interval + 1) + 1);
//        
//        // border points are removed
//        for (y = 1; y < h - 1; y++)
//          for (x=1 ; x<w-1 ; x++)
//            if (isMaximum(hessian, x, y, interval, threshold))
//            {
//              x_ = x*sample;
//              y_ = y*sample;
//              s_ = 0.4*(pow*(interval + 1) + 2); // box size or scale
//              // Affine refinement is performed for a given octave and sampling
//              if( interpolationScaleSpace(hessian, x, y, interval, x_, y_, s_, sample, pow) )
//                addKeyPoint(imgInt, x_, y_, (*(signLaplacian[interval]))(x, y), s_, lKP);
//            }
//      }
//
//      /* MemCheck*/
//      for(int j = 0; j < INTERVAL; j++)
//      {
//        delete hessian[j];
//        delete signLaplacian[j];
//      }
//    }
//
//    // Compute the descriptors
//    return getDescriptor(imgInt, lKP);
    }
    return null;
    //return features;
  }

  

  private Point2i getSampleSize(Sequence seq, int sample) {
    return new Point2i(seq.getWidth()/sample, seq.getHeight()/sample); 
  }

  private Sequence normalizeSequence(Sequence seq) {
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
