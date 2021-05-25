package ar.edu.itba.sds.objects;

public class Step <T> {
    private final double time;
    private final T pos;
    private final T vel;
    private final T acc;

    public Step(double time, T pos, T vel, T acc) {
        this.time = time;
        this.pos = pos;
        this.vel = vel;
        this.acc = acc;
    }

    public double getTime() {
        return time;
    }

    public T getPos() {
        return pos;
    }

    public T getVel() {
        return vel;
    }

    public T getAcc() {
        return acc;
    }
}
