package ar.edu.itba.sds.objects;

public class Step <T> {
    private final double time;
    private final double radius;
    private final T pos;
    private final T vel;

    public Step(double time, double radius, T pos, T vel) {
        this.time = time;
        this.radius = radius;
        this.pos = pos;
        this.vel = vel;
    }

    public double getTime() {
        return time;
    }

    public double getRadius() {
        return radius;
    }

    public T getPos() {
        return pos;
    }

    public T getVel() {
        return vel;
    }
}
