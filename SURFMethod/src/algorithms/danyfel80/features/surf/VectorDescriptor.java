package algorithms.danyfel80.features.surf;

/**
 * Class of the descriptor for the vector of SURF's detected keypoint.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class VectorDescriptor {
	/**
	 * Sum of the horizontal partial derivatives
	 */
	private double sumDx;
	/**
	 * Sum of the vertical partial derivatives
	 */
	private double sumDy;
	/**
	 * Sum of the absolute horizontal partial derivatives
	 */
	private double sumAbsDx;
	/**
	 * Sum of the absolute vertical partial derivatives
	 */
	private double sumAbsDy;

	/**
	 * Constructor
	 * 
	 * @param sumDx
	 *          Sum of the horizontal partial derivatives
	 * @param sumDy
	 *          Sum of the vertical partial derivatives
	 * @param sumAbsDx
	 *          Sum of the absolute horizontal partial derivatives
	 * @param sumAbsDy
	 *          Sum of the absolute vertical partial derivatives
	 */
	public VectorDescriptor(double sumDx, double sumDy, double sumAbsDx, double sumAbsDy) {
		super();
		this.sumDx = sumDx;
		this.sumDy = sumDy;
		this.sumAbsDx = sumAbsDx;
		this.sumAbsDy = sumAbsDy;
	}

	public double getSumDx() {
		return sumDx;
	}

	public void setSumDx(double sumDx) {
		this.sumDx = sumDx;
	}

	public double getSumDy() {
		return sumDy;
	}

	public void setSumDy(double sumDy) {
		this.sumDy = sumDy;
	}

	public double getSumAbsDx() {
		return sumAbsDx;
	}

	public void setSumAbsDx(double sumAbsDx) {
		this.sumAbsDx = sumAbsDx;
	}

	public double getSumAbsDy() {
		return sumAbsDy;
	}

	public void setSumAbsDy(double sumAbsDy) {
		this.sumAbsDy = sumAbsDy;
	}

}
