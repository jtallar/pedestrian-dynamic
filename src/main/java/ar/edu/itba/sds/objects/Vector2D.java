package ar.edu.itba.sds.objects;

import static java.lang.Math.*;

public class Vector2D {
    private double x;
    private double y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static double deltaX(Vector2D v1, Vector2D v2) {
        return v1.getX() - v2.getX();
    }

    public static double deltaY(Vector2D v1, Vector2D v2) {
        return v1.getY() - v2.getY();
    }

    public static Vector2D sum(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.getX() + v2.getX(), v1.getY() + v2.getY());
    }

    public static Vector2D sub(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.getX() - v2.getX(), v1.getY() - v2.getY());
    }

    public static Vector2D scalar(Vector2D v, double scalar) {
        return new Vector2D(v.getX() * scalar, v.getY() * scalar);
    }


    public static double mod(Vector2D v1, Vector2D v2) {
        return sqrt(pow(v1.getX() - v2.getX(), 2.0) + pow(v1.getY() - v2.getY(), 2.0));
    }

    public static Vector2D force(Vector2D v1, Vector2D v2, double k, double q, boolean pos) {
        final double mod = mod(v1, v2);
        final double fn = (k * q * q * ((pos)? 1 : -1 )) / pow(mod, 2.0);
        return new Vector2D(fn * deltaX(v1, v2) / mod, fn * deltaY(v1, v2) / mod);
    }

    @Override
    public String toString() {
        return "Vector2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2D)) return false;
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(vector2D.x, x) == 0 && Double.compare(vector2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
}
