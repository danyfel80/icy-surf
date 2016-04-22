package algorithms.danyfel80.features.surf;

/**
 * Utility class for the SURF Method.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class SURFMethodUtils {

	// Static values
	/**
	 * Maximum amount of intervals for each octave (4)
	 */
	public static final int MAX_INTERVAL = 4;
	/**
	 * Maximum amount of octaves (4)
	 */
	public static final int MAX_OCTAVE = 4;
	/**
	 * Amount of samples at processed interval.
	 */
	public static final int SAMPLING = 2;
	/**
	 * Amount of considered angular regions
	 */
	public static final int ANGULAR_REGIONS = 20;
	/**
	 * Descriptor size
	 */
	public static final int DESCRIPTOR_SIZE = 4;
	/**
	 * Ratio between two matches
	 */
	public static final float RATE = 0.6f;

	/**
	 * Performs the gaussian with the given coordinate and sigma values.
	 * 
	 * @param x
	 *          The x-coordinate of the key point.
	 * @param y
	 *          The y-coordinate of the key point.
	 * @param sigma
	 *          The sigma of the gaussian.
	 * @return The gaussian.
	 */
	public static double gaussian(double x, double y, double sigma) {
		return 1.0 / (2.0 * Math.PI * sigma * sigma) * Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
	}
}
