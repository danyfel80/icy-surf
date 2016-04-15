package plugins.danyfel80.surfmethod;

import java.awt.geom.Point2D;
import java.util.List;

import org.javatuples.Pair;

import algorithms.danyfel80.features.surf.Descriptor;
import algorithms.danyfel80.features.surf.SURFDescriptorsDetection;
import algorithms.danyfel80.features.surf.SURFKeyPointsMatch;
import icy.gui.dialog.MessageDialog;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.type.DataType;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.ezplug.EzGroup;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.adufour.ezplug.EzVarDouble;
import plugins.adufour.ezplug.EzVarSequence;
import plugins.adufour.vars.lang.Var;
import plugins.danyfel80.surfmethod.overlay.SURFDescriptorsOverlay;
import plugins.kernel.roi.roi2d.ROI2DPoint;

/**
 * SURF sequence match. Finds common points between two images using SURF
 * Method.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFSequenceMatch extends EzPlug implements Block {

  // Static Variables
  private static final double DEFAULT_THRESHOLD = 1000;

  // Input Variables
  /**
   * The input image 1 to match.
   */
  private EzVarSequence       inSequence1;
  /**
   * The input image 2 to match.
   */
  private EzVarSequence       inSequence2;
  /**
   * The threshold for the detection of the Hessian.
   */
  private EzVarDouble         inHThreshold;
  /**
   * If true an overlay with the keypoints and the matches will be shown in the
   * input image.
   */
  private EzVarBoolean        inAddOverlay;

  /*
   * (non-Javadoc)
   * 
   * @see plugins.adufour.ezplug.EzPlug#initialize()
   */
  @Override
  protected void initialize() {
    inSequence1 = new EzVarSequence("Sequence 1 (a 2D image)");
    inSequence1.setToolTipText("The image 1 to match.");
    inSequence2 = new EzVarSequence("Sequence 2 (a 2D image)");
    inSequence2.setToolTipText("The image 2 to match.");
    inHThreshold = new EzVarDouble("Hessian Threshold", 1000, 1, 10000000, 10);
    inHThreshold.setToolTipText(
        "The threshold for the detection of the Hessian. (by default "
            + DEFAULT_THRESHOLD + ")");
    inHThreshold.setOptional(true);
    EzGroup paramsGroup = new EzGroup("Parameters", inSequence1, inSequence2,
        inHThreshold);

    inAddOverlay = new EzVarBoolean("Show results in sequence", true);
    EzGroup presentationGroup = new EzGroup("Presentation", inAddOverlay);

    addEzComponent(paramsGroup);
    addEzComponent(presentationGroup);
  }

  // Internal variables
  private Sequence                           seq1;
  private Sequence                           seq2;
  private double                             threshold;

  /**
   * Descriptors found.
   */
  private List<Descriptor>                   descriptors1;
  private List<Descriptor>                   descriptors2;
  /**
   * Matches found
   */
  private List<Pair<Descriptor, Descriptor>> matches;

  /*
   * (non-Javadoc)
   * 
   * @see plugins.adufour.ezplug.EzPlug#execute()
   */
  @Override
  protected void execute() {
    // Input validation
    seq1 = inSequence1.getValue();
    seq2 = inSequence2.getValue();
    if (seq1 == null || seq1.isEmpty() || seq2 == null || seq2.isEmpty()) {
      MessageDialog.showDialog("Input Error",
          "Please choose a valid pair of sequences to perform the procedure.",
          MessageDialog.ERROR_MESSAGE);
      return;
    }
    if (seq1.getSizeZ() > 1 || seq2.getSizeZ() > 1) {
      MessageDialog.showDialog("Dimension Error",
          "Please choose sequences with only one slice.",
          MessageDialog.ERROR_MESSAGE);
      return;
    }

    // Sequence adaptation
    if (seq1.getSizeC() > 1) {
      seq1 = SequenceUtil.toGray(seq1);
    }
    if (seq1.getDataType_() != DataType.DOUBLE) {
      seq1 = SequenceUtil.convertToType(seq1, DataType.DOUBLE, false);
    }
    if (seq2.getSizeC() > 1) {
      seq2 = SequenceUtil.toGray(seq2);
    }
    if (seq2.getDataType_() != DataType.DOUBLE) {
      seq2 = SequenceUtil.convertToType(seq2, DataType.DOUBLE, false);
    }

    threshold = (inHThreshold.isEnabled())? inHThreshold.getValue()
        : DEFAULT_THRESHOLD;

    // Descriptors extraction
    long startTime = System.nanoTime();
    SURFDescriptorsDetection descriptorDetection1 = new SURFDescriptorsDetection(
        seq1, threshold);
    descriptors1 = descriptorDetection1.findDescriptors();
    SURFDescriptorsDetection descriptorDetection2 = new SURFDescriptorsDetection(
        seq2, threshold);
    descriptors2 = descriptorDetection2.findDescriptors();

    long endTime = System.nanoTime();

    if (inAddOverlay.getValue()) {
      SURFDescriptorsOverlay overlay1 = new SURFDescriptorsOverlay(
          descriptors1);
      inSequence1.getValue().addOverlay(overlay1);
      SURFDescriptorsOverlay overlay2 = new SURFDescriptorsOverlay(
          descriptors2);
      inSequence2.getValue().addOverlay(overlay2);
    }
    System.out.println(
        "Found " + descriptors1.size() + " descriptors in sequence 1.");
    System.out.println(
        "Found " + descriptors2.size() + " descriptors in sequence 2.");
    System.out.println("Descriptors computed in has finished in "
        + ((endTime - startTime) / 1000000) + " msecs.");

    // Match descriptors
    long startTime1 = System.nanoTime();
    SURFKeyPointsMatch matcher = new SURFKeyPointsMatch(seq1, seq2,
        descriptors1, descriptors2);
    matcher.matchDescriptors();
    matches = matcher.getMatches();
    endTime = System.nanoTime();
    System.out.println("Found " + matches.size() + " matches.");
    if (inAddOverlay.getValue()) {
      for (int i = 0; i < matcher.getMatches().size(); i++) {
        Descriptor d1 = matches.get(i).getValue0();
        Descriptor d2 = matches.get(i).getValue1();
        ROI r1 = new ROI2DPoint(new Point2D.Double(d1.getKeyPoint().getX(),
            d1.getKeyPoint().getY()));
        r1.setName("" + (i + 1));
        r1.setShowName(true);
        ROI r2 = new ROI2DPoint(new Point2D.Double(d2.getKeyPoint().getX(),
            d2.getKeyPoint().getY()));
        r2.setName("" + (i + 1));
        r2.setShowName(true);
        inSequence1.getValue().addROI(r1);
        inSequence2.getValue().addROI(r2);
      }
    }

    System.out.println("Matches computed in has finished in "
        + ((endTime - startTime1) / 1000000) + " msec.");
    System.out.println("SURF Method has finished in "
        + ((endTime - startTime) / 1000000) + " msec.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see plugins.adufour.ezplug.EzPlug#clean()
   */
  @Override
  public void clean() {}

  // For protocols
  @Override
  public void declareInput(VarList inputMap) {
    inputMap.add(inSequence1.name, inSequence1.getVariable());
    inputMap.add(inSequence2.name, inSequence2.getVariable());
    inputMap.add(inHThreshold.name, inHThreshold.getVariable());
    inputMap.add(inAddOverlay.name, inAddOverlay.getVariable());
  }

  Var<List<Descriptor>>                   outDescriptors1;
  Var<List<Descriptor>>                   outDescriptors2;
  Var<List<Pair<Descriptor, Descriptor>>> outMatches;

  @Override
  public void declareOutput(VarList outputMap) {
    outDescriptors1 = new Var<List<Descriptor>>("Descriptors 1", descriptors1);
    outDescriptors1.setValue(descriptors1);
    outputMap.add(outDescriptors1.getName(), outDescriptors1);
    outDescriptors2 = new Var<List<Descriptor>>("Descriptors 2", descriptors2);
    outDescriptors2.setValue(descriptors2);
    outputMap.add(outDescriptors2.getName(), outDescriptors2);

    outMatches = new Var<List<Pair<Descriptor, Descriptor>>>("Matches",
        matches);
    outMatches.setValue(matches);
    outputMap.add(outMatches.getName(), outMatches);
  }

}
