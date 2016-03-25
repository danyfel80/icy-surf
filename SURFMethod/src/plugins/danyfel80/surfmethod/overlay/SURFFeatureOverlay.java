/**
 * 
 */
package plugins.danyfel80.surfmethod.overlay;

import icy.canvas.IcyCanvas;
import icy.painter.Overlay;
import icy.sequence.Sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import algorithms.features.surf.KeyPoint;
import algorithms.features.surf.SURFFeature;

/**
 * SURF Feature Overlay
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFFeatureOverlay extends Overlay {
  private List<SURFFeature> features;
  
  private SURFFeatureOverlay() {
    super("SURF features");
  }
  
  public SURFFeatureOverlay(List<SURFFeature> features) {
    super("SURF features");
    this.features = features;
  }

  @Override
  public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
    if (g != null && features != null) {
      g.setColor(Color.GREEN);
      g.setStroke(new BasicStroke());
      
      SURFFeature f;
      KeyPoint fkp;
      for (int i = 0; i < features.size(); i++) {
        f = features.get(i);
        fkp = f.getKeyPoint();
        g.drawOval((int)(fkp.getX() - fkp.getScale())+1,
            (int)(fkp.getY() - fkp.getScale())+1,
            (int)(2*fkp.getScale()), (int)(2*fkp.getScale()));
        
        int x2 = (int) (fkp.getX() + (fkp.getScale() * Math.cos(fkp.getOrientation())));
        int y2 = (int) (fkp.getY() + (fkp.getScale() * Math.sin(fkp.getOrientation())));
        
        g.drawLine((int)fkp.getX(), (int)fkp.getY(), x2, y2);
      }
    }
  }
}
