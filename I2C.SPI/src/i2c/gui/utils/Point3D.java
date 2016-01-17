package i2c.gui.utils;

public class Point3D
{
  private double x, y, z;

  public double getX()
  {
    return x;
  }

  public double getY()
  {
    return y;
  }

  public double getZ()
  {
    return z;
  }

  public Point3D(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  
  /**
   * Rotates the point around the X axis by the given angle in degrees.
   * @param angle in degrees
   * @return
   */
  public Point3D rotateX(double angle)
  {
    double rad = Math.toRadians(angle);
    double cosa = Math.cos(rad);
    double sina = Math.sin(rad);
    double y = this.y * cosa - this.z * sina;
    double z = this.y * sina + this.z * cosa;
    return new Point3D(this.x, y, z);    
  }

  /**
   * Rotates the point around the Y axis by the given angle in degrees.
   * @param angle in degrees
   * @return
   */
  public Point3D rotateY(double angle)
  {
    double rad = Math.toRadians(angle);
    double cosa = Math.cos(rad);
    double sina = Math.sin(rad);
    double z = this.z * cosa - this.x * sina;
    double x = this.z * sina + this.x * cosa;
    return new Point3D(x, this.y, z);
  }

  /**
   * Rotates the point around the Z axis by the given angle in degrees.
   * @param angle in degrees
   * @return
   */
  public Point3D rotateZ(double angle)
  {
    double rad = Math.toRadians(angle);
    double cosa = Math.cos(rad);
    double sina = Math.sin(rad);
    double x = this.x * cosa - this.y * sina;
    double y = this.x * sina + this.y * cosa;
    return new Point3D(x, y, this.z);
  }

  /*
   * Transforms this 3D point to 2D using a perspective projection.
   */
  public Point3D project(int winWidth, int winHeight, double fieldOfView, double viewerDistance)
  {
    double factor = fieldOfView / (viewerDistance + this.z);
    double x =  this.x * factor + winWidth / 2;
    double y = -this.y * factor + winHeight / 2;
    return new Point3D(x, y, 1);
  }
}
