package algorithms.features.surf;

public class Descriptor {
  private double sumDx;
  private double sumDy;
  private double sumAbsDx;
  private double sumAbsDy;
  
  
  public Descriptor(double sumDx, double sumDy, double sumAbsDx, double sumAbsDy) {
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
