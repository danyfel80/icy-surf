package algorithms.danyfel80.features.surf;

import java.util.List;

import javax.vecmath.Point3d;

/**
 * This class represents a key point found with the surf method
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class KeyPoint {
	private double x;
	private double y;
	private double scale;
	private double orientation;
	private boolean signLaplacian;

	/**
	 * Constructor
	 * 
	 * @param x
	 *          x-coordinate of the key point.
	 * @param y
	 *          y-coordinate of the key point.
	 * @param scale
	 *          octave scale of the key point.
	 * @param orientation
	 *          orientation of the key point.
	 * @param signLaplacian
	 *          the sign of the laplacian at the key point.
	 */
	public KeyPoint(double x, double y, double scale, double orientation, boolean signLaplacian) {
		super();
		this.x = x;
		this.y = y;
		this.scale = scale;
		this.orientation = orientation;
		this.signLaplacian = signLaplacian;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getOrientation() {
		return orientation;
	}

	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	public boolean isSignLaplacian() {
		return signLaplacian;
	}

	public void setSignLaplacian(boolean signLaplacian) {
		this.signLaplacian = signLaplacian;
	}

	/**
	 * Adds a key point to the feature list.
	 * 
	 * @param intSeq
	 *          Integral sequence.
	 * @param i
	 *          The x-coordinate of the key point.
	 * @param j
	 *          The y-coordinate of the key point.
	 * @param sign
	 *          The laplacian sign of the feature at the key point in the integral
	 *          sequence.
	 * @param scale
	 *          The scale at which the key point was detected.
	 */
	public static void addKeyPoint(IntegralSequence intSeq, double i, double j, boolean sign, double scale,
	    List<Descriptor> features) {
		KeyPoint pt = new KeyPoint(i, j, scale,
		    getOrientation(intSeq, (int) Math.round(i), (int) Math.round(j), SURFMethodUtils.ANGULAR_REGIONS, scale), sign);
		features.add(new Descriptor(pt, null));
	}

	/**
	 * Computes the orientation of a given key point
	 * 
	 * @param intSeq
	 *          The integral sequence.
	 * @param x
	 *          The x-coordinate of the key point.
	 * @param y
	 *          The y-coordinate of the key point.
	 * @param sectors
	 *          The amount of sectors at the current scale.
	 * @param scale
	 *          The scale at which the key point was found.
	 * @return The orientation of the key point.
	 */
	public static double getOrientation(IntegralSequence intSeq, int x, int y, int sectors, double scale) {

		double[] haarResponseX = new double[sectors];
		double[] haarResponseY = new double[sectors];
		double[] haarResponseSectorX = new double[sectors];
		double[] haarResponseSectorY = new double[sectors];
		long answerX, answerY;
		double gauss;

		int theta;

		// Computation of the contribution of each angular sectors.
		for (int i = -6; i <= 6; i++) {
			for (int j = -6; j <= 6; j++) {
				if (i * i + j * j <= 36) {

					answerX = intSeq.haarX(x + (int) (i * scale), y + (int) (j * scale), (int) Math.round(2.0 * scale));
					answerY = intSeq.haarY(x + (int) (i * scale), y + (int) (j * scale), (int) Math.round(2.0 * scale));

					// Associated angle
					theta = (int) (Math.atan2(answerY, answerX) * sectors / (2.0 * Math.PI));
					theta = (theta >= 0) ? theta : (theta + sectors);

					// Gaussian weight
					gauss = SURFMethodUtils.gaussian(i, j, 2);

					// Cumulative answers
					haarResponseSectorX[theta] += answerX * gauss;
					haarResponseSectorY[theta] += answerY * gauss;
				}
			}
		}

		// Compute a windowed answer
		for (int i = 0; i < sectors; i++) {
			for (int j = -sectors / 12; j <= sectors / 12; j++) {
				if (0 <= i + j && i + j < sectors) {
					haarResponseX[i] += haarResponseSectorX[i + j];
					haarResponseY[i] += haarResponseSectorY[i + j];
				}
				// The answer can be on any cadrant of the unit circle
				else if (i + j < 0) {
					haarResponseX[i] += haarResponseSectorX[sectors + i + j];
					haarResponseY[i] += haarResponseSectorY[i + j + sectors];
				} else {
					haarResponseX[i] += haarResponseSectorX[i + j - sectors];
					haarResponseY[i] += haarResponseSectorY[i + j - sectors];
				}
			}
		}

		// Find out the maximum answer
		double max = haarResponseX[0] * haarResponseX[0] + haarResponseY[0] * haarResponseY[0];

		int t = 0;
		for (int i = 1; i < sectors; i++) {
			double norm = haarResponseX[i] * haarResponseX[i] + haarResponseY[i] * haarResponseY[i];
			t = (max < norm) ? i : t;
			max = (max < norm) ? norm : max;
		}

		// Return the angle ; better than atan which is not defined in pi/2
		return Math.atan2(haarResponseY[t], haarResponseX[t]);
	}

	/**
	 * Performs the interpolation and finds out if the interpolation is stable
	 * Reject or interpolate the coordinate of a keypoint. This is necessary since
	 * there was a subsampling of the image.
	 * 
	 * @param hessian
	 * @param x
	 *          The x-coordinate of the point.
	 * @param y
	 *          The y-coordinate of the point.
	 * @param i
	 *          The interval of the point.
	 * @param coord
	 *          the x, y, scale of the point to interpolate.
	 * @param sample
	 *          The sample size of the point.
	 * @param octave
	 *          The octave at which the point is processed.
	 * @return True if the point is stable with the given interpolation
	 *         parameters.
	 */
	public static boolean interpolationScaleSpace(List<RegularSequence> hessian, int x, int y, int i, Point3d coord,
	    int sample, int octave) {
		RegularSequence hs = hessian.get(i);
		// If we are outside the image...
		if (x <= 0 || y <= 0 || x >= hs.getWidth() - 2 || y >= hs.getHeight() - 2)
			return false;

		double mx, my, mi, dx, dy, di, dxx, dyy, dii, dxy, dxi, dyi;
		RegularSequence hsPrev = hessian.get(i - 1);
		RegularSequence hsNext = hessian.get(i + 1);

		// Nabla X
		dx = ((hs.getValue(x + 1, y) - hs.getValue(x - 1, y)) / 2.0);
		dy = ((hs.getValue(x, y + 1) - hs.getValue(x, y - 1)) / 2.0);
		di = ((hs.getValue(x, y) - hs.getValue(x, y)) / 2.0);

		// Hessian X
		double a = hs.getValue(x, y);
		dxx = hs.getValue(x + 1, y) + hs.getValue(x - 1, y) - 2.0 * a;
		dyy = hs.getValue(x, y + 1) + hs.getValue(x, y + 1) - 2.0 * a;
		dii = hsPrev.getValue(x, y) + hsNext.getValue(x, y) - 2.0 * a;

		dxy = (hs.getValue(x + 1, y + 1) - hs.getValue(x + 1, y - 1) - hs.getValue(x - 1, y + 1)
		    + hs.getValue(x - 1, y - 1)) / 4.0;
		dxi = (hsNext.getValue(x + 1, y) - hsNext.getValue(x - 1, y) - hsPrev.getValue(x + 1, y)
		    + hsPrev.getValue(x - 1, y)) / 4.0;
		dyi = (hsNext.getValue(x, y + 1) - hsNext.getValue(x, y - 1) - hsPrev.getValue(x, y + 1)
		    + hsPrev.getValue(x, y - 1)) / 4.0;

		// Det
		double det = dxx * dyy * dii - dxx * dyi * dyi - dyy * dxi * dxi + 2 * dxi * dyi * dxy - dii * dxy * dxy;

		if (det != 0.0) // Matrix must be inversible - maybe useless.
		{
			mx = -1.0 / det * (dx * (dyy * dii - dyi * dyi) + dy * (dxi * dyi - dii * dxy) + di * (dxy * dyi - dyy * dxi));
			my = -1.0 / det * (dx * (dxi * dyi - dii * dxy) + dy * (dxx * dii - dxi * dxi) + di * (dxy * dxi - dxx * dyi));
			mi = -1.0 / det * (dx * (dxy * dyi - dyy * dxi) + dy * (dxy * dxi - dxx * dyi) + di * (dxx * dyy - dxy * dxy));

			// If the point is stable
			// System.out.println("det=" + det + ", mx=" + mx + ", my=" + my + ", mi="
			// + mi);
			if (Math.abs(mx) < 1.0 && Math.abs(my) < 1.0 && Math.abs(mi) < 1.0) {
				coord.x = sample * (x + mx) + 0.5;// Center the pixels value
				coord.y = sample * (y + my) + 0.5;
				coord.z = 0.4 * (1.0 + octave * (i + mi + 1.0));
				return true;
			}

		}
		return false;
	}

	/**
	 * Finds out if the point at the image of a given scale is the maximum
	 * compared with neighbor scales and taking into account the Hessian
	 * threshold. Check if a point is a local maximum or not, and more than a
	 * given threshold.
	 * 
	 * @param imageStamp
	 * @param x
	 * @param y
	 * @param scale
	 * @return
	 */
	public static boolean isMaximum(List<RegularSequence> imageStamp, int x, int y, int scale, double threshold) {
		RegularSequence iStp = imageStamp.get(scale);
		RegularSequence iStpPrev = imageStamp.get(scale - 1);
		RegularSequence iStpNext = imageStamp.get(scale + 1);

		double tmp = iStp.getValue(x, y);
		if (tmp > threshold) {
			for (int j = -1 + y; j < 2 + y; j++) {
				for (int i = -1 + x; i < 2 + x; i++) {
					if (iStpPrev.getValue(i, j) >= tmp)
						return false;
					if (iStpNext.getValue(i, j) >= tmp)
						return false;
					if ((x != i || y != j) && iStp.getValue(i, j) >= tmp)
						return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
