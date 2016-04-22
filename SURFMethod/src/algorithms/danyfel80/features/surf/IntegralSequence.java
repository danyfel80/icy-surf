package algorithms.danyfel80.features.surf;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.TypeUtil;

/**
 * Special class for integral images
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class IntegralSequence {

	private int sx, sy, padding;
	private Sequence sequence;
	private double[] sequenceData;

	/**
	 * Constructor which computes the integral image.
	 * 
	 * @param sequence
	 *          Sequence to convert
	 */
	public IntegralSequence(RegularSequence sequence) {
		padding = 312;// size descriptor * max size L = 4*0.4*195;

		RegularSequence paddedSequence = sequence.padImage(padding); // Pad the
		                                                             // image
		computeIntegralImage(paddedSequence);
	}

	/**
	 * Setter
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public void setValue(int x, int y, long value) {
		sequenceData[(x + padding) + (y + padding) * sx] = TypeUtil.toDouble(value, true);
	}

	/**
	 * Use when finishing an update in the sequence.
	 */
	public void dataChanged() {
		sequence.dataChanged();
	}

	/**
	 * Getter
	 * 
	 * @param x
	 * @param y
	 * @return value at (x, y) of integral image.
	 */
	public long getValue(int x, int y) {
		return TypeUtil.toLong(sequenceData[(x + padding) + (y + padding) * sx]);
	}

	/**
	 * @return Size x
	 */
	public int getWidth() {
		return sx;
	}

	/**
	 * @return Size y
	 */
	public int getHeight() {
		return sy;
	}

	/**
	 * @return Internal sequence
	 */
	public Sequence getSequence() {
		return sequence;
	}

	/**
	 * Use TypeUtil.toLong() to convert to long.
	 * 
	 * @return Internal sequence data
	 */
	public double[] getData() {
		return this.sequenceData;
	}

	/**
	 * Computes the integral image. In order to avoid border effects, the image is
	 * firstly periodized by padding the image and mirroring on image borders.
	 * 
	 * @param paddedSequence
	 *          Padded sequence
	 */
	private void computeIntegralImage(RegularSequence paddedSequence) {

		sx = paddedSequence.getWidth();
		sy = paddedSequence.getHeight();
		sequence = new Sequence(new IcyBufferedImage(sx, sy, 1, DataType.DOUBLE));
		sequence.setName(paddedSequence.getSequence().getName() + "(Integral)");
		sequenceData = sequence.getDataXYAsDouble(0, 0, 0);

		// Intialization
		setValue(-padding, -padding, 0);

		// First row
		for (int x = 1; x < sx; x++) {
			setValue(x - padding, -padding, getValue(x - padding - 1, -padding) + (long) paddedSequence.getValue(x, 0));
		}

		// Recursion
		for (int y = 1; y < sy; y++) {
			long h = 0;
			for (int x = 0; x < sx; x++) {
				h += paddedSequence.getValue(x, y);
				setValue(x - padding, y - padding, getValue(x - padding, y - padding - 1) + h);
			}
		}
		dataChanged();
	}

	/**
	 * Convolution by a square defined by the bottom-left (a,b) and top-right
	 * (c,d)
	 * 
	 * @param a
	 *          Left
	 * @param b
	 *          Bottom
	 * @param c
	 *          Right
	 * @param d
	 *          Top
	 * @param x
	 *          X center
	 * @param y
	 *          Y center
	 * @return computed value
	 */
	public double squareConvolutionXY(int a, int b, int c, int d, int x, int y) {
		int a1 = x - a;
		int a2 = y - b;
		int b1 = a1 - c;
		int b2 = a2 - d;
		// Note: No L2-normalization is performed here.
		return (getValue(b1, b2) + getValue(a1, a2) - getValue(b1, a2) - getValue(a1, b2));
	}

	/**
	 * Convolution by a box [-1,+1]
	 * 
	 * @param x
	 *          X center
	 * @param y
	 *          Y center
	 * @param lambda
	 *          Lambda value
	 * @return
	 */
	public long haarX(int x, int y, int lambda) {
		return (long) -(squareConvolutionXY(1, -lambda - 1, -lambda - 1, lambda * 2 + 1, x, y)
		    + squareConvolutionXY(0, -lambda - 1, lambda + 1, lambda * 2 + 1, x, y));
	}

	/**
	 * Convolution by a box [-1;+1]
	 * 
	 * @param x
	 *          X center
	 * @param y
	 *          Y center
	 * @param lambda
	 *          Lambda value
	 * @return
	 */
	long haarY(int x, int y, int lambda) {
		return (long) -(squareConvolutionXY(-lambda - 1, 1, 2 * lambda + 1, -lambda - 1, x, y)
		    + squareConvolutionXY(-lambda - 1, 0, 2 * lambda + 1, lambda + 1, x, y));
	}
}
