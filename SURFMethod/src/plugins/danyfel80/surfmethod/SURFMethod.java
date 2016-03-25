package plugins.danyfel80.surfmethod;

import java.util.List;

import algorithms.features.surf.SURFFeature;
import algorithms.features.surf.SURFFeatureDetection;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.ezplug.EzGroup;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.adufour.ezplug.EzVarInteger;
import plugins.adufour.ezplug.EzVarSequence;
import plugins.adufour.vars.lang.Var;
import plugins.danyfel80.surfmethod.overlay.SURFFeatureOverlay;
import icy.gui.dialog.MessageDialog;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.type.DataType;

public class SURFMethod extends EzPlug implements Block{

  // Static Variables
  private static final Integer DEFAULT_THRESHOLD = 1000; 
  
  
  // Input Variables
  /**
   * The input image to extract keypoints and descriptors from.
   */
  private EzVarSequence inSequence;
  /**
   * The threshold for the detection of the Hessian.
   */
  private EzVarInteger inHThreshold;
  /**
   * If true an overlay with the keypoints will be shown in the input image.
   */
  private EzVarBoolean inAddOverlay;
  
  
  @Override
  protected void initialize() {
    
    inSequence = new EzVarSequence("Target sequence (a 2D image)");
    inSequence.setToolTipText("The image to extract keypoints and descriptors from.");
    inHThreshold = new EzVarInteger("Hessian Threshold", 1000, 100, 3000, 10);
    inHThreshold.setToolTipText("The threshold for the detection of the Hessian.");
    inHThreshold.setOptional(true);
    EzGroup paramsGroup = new EzGroup("Parameters", inSequence, inHThreshold);
    
    inAddOverlay = new EzVarBoolean("Show results in sequence", true);
    EzGroup presentationGroup = new EzGroup("Presentation", inAddOverlay);
    
    addEzComponent(presentationGroup);
    addEzComponent(paramsGroup);
    
  }

  // Internal variables
  private Sequence seq;
  private Integer threshold;
  /**
   * The found features.
   */
  private List<SURFFeature> features;
  
  @Override
  protected void execute() {
    
    seq = inSequence.getValue();
    if (seq == null || seq.isEmpty()) {
      MessageDialog.showDialog("Input Error", "Please choose a valid sequence to perform the procedure.", MessageDialog.ERROR_MESSAGE);
      return;
    }
    if (seq.getSizeZ() > 1) {
      MessageDialog.showDialog("Dimension Error", "Please choose a sequence with only one slice.", MessageDialog.ERROR_MESSAGE);
      return;
    }
    if (seq.getSizeC() > 1) {
      seq = SequenceUtil.toGray(seq);
    }
    seq = SequenceUtil.convertToType(seq, DataType.DOUBLE, false);
    
    threshold = (inHThreshold.isEnabled())? inHThreshold.getValue(): DEFAULT_THRESHOLD;
    
    
    long startTime = System.nanoTime();
    SURFFeatureDetection featureDetection = new SURFFeatureDetection(seq, threshold);
    features = featureDetection.getFeatures();

    long endTime = System.nanoTime();
    
    if (inAddOverlay.getValue()) {
      SURFFeatureOverlay overlay = new SURFFeatureOverlay(features);
      seq.addOverlay(overlay);
    }
    
    System.out.println("SURF Method has finished in " + ((endTime - startTime)/1000000) + " milliseconds.");
  }

  @Override
  public void clean() {}

  
  // For protocols
  @Override
  public void declareInput(VarList inputMap) {
    inputMap.add(inSequence.name, inSequence.getVariable());
    inputMap.add(inHThreshold.name, inHThreshold.getVariable());
    inputMap.add(inAddOverlay.name, inAddOverlay.getVariable());
  }

  Var<List<SURFFeature>> outFeatures;
  @Override
  public void declareOutput(VarList outputMap) {
    outFeatures = new Var<List<SURFFeature>>("Features", features);
    outFeatures.setValue(features);
    outputMap.add(outFeatures.getName(), outFeatures);
  }
}
