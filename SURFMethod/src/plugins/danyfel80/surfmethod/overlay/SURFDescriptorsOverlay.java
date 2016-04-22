package plugins.danyfel80.surfmethod.overlay;

import icy.canvas.IcyCanvas;
import icy.painter.Overlay;
import icy.sequence.Sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import algorithms.danyfel80.features.surf.KeyPoint;
import algorithms.danyfel80.features.surf.Descriptor;

/**
 * SURF Feature Overlay
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFDescriptorsOverlay extends Overlay {
	private List<Descriptor> descriptors;

	private SURFDescriptorsOverlay() {
		super("SURF descriptors");
	}

	public SURFDescriptorsOverlay(List<Descriptor> descriptors) {
		super("SURF descriptors");
		this.descriptors = descriptors;
	}

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		if (g != null && descriptors != null) {
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke());

			Descriptor d;
			KeyPoint kp;

			int i, dx, dy, x2, y2;

			for (i = 0; i < descriptors.size(); i++) {
				d = descriptors.get(i);
				kp = d.getKeyPoint();
				g.drawOval((int) (kp.getX() - kp.getScale()) + 1, (int) (kp.getY() - kp.getScale()) + 1,
				    (int) (2 * kp.getScale()), (int) (2 * kp.getScale()));

				dx = (int) (kp.getScale() * Math.cos(kp.getOrientation()));
				dy = (int) (kp.getScale() * Math.sin(kp.getOrientation()));
				x2 = (int) kp.getX() + (kp.isSignLaplacian() ? dx : -dx);
				y2 = (int) kp.getY() + (kp.isSignLaplacian() ? dy : -dy);

				g.drawLine((int) kp.getX(), (int) kp.getY(), x2, y2);
			}
		}
	}
}
