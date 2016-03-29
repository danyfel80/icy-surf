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

import algorithms.danyfel80.features.surf.KeyPoint;
import algorithms.danyfel80.features.surf.SURFFeature;

/**
 * SURF Feature Overlay
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFFeaturesOverlay extends Overlay {
  private List<SURFFeature> features;
  
  private SURFFeaturesOverlay() {
    super("SURF features");
  }
  
  public SURFFeaturesOverlay(List<SURFFeature> features) {
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
        
        int dx = (int)(fkp.getScale() * Math.cos(fkp.getOrientation()));
        int dy = (int)(fkp.getScale() * Math.sin(fkp.getOrientation()));
        int x2 = (int)fkp.getX() + (fkp.isSignLaplacian()? dx: -dx);
        int y2 = (int)fkp.getY() + (fkp.isSignLaplacian()? dy: -dy);
        
        g.drawLine((int)fkp.getX(), (int)fkp.getY(), x2, y2);
      }
    }
  }
}
