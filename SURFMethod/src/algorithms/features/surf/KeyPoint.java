package algorithms.features.surf;

public class KeyPoint {
  private double x;
  private double y;
  private double scale;
  private double orientation;
  private boolean signLaplacian;
  
  public KeyPoint(double x, double y, double scale, double orientation,
      boolean signLaplacian) {
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
  
  
}
