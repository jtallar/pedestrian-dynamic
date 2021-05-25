package ar.edu.itba.sds.algos;

import ar.edu.itba.sds.objects.AlgorithmType;
import ar.edu.itba.sds.objects.Step;

import java.util.Iterator;
import java.util.function.BiFunction;

public abstract class StepAlgorithm implements Iterator<Step<Double>> {
    protected final double[] pos;
    protected final double[] vel;
    protected final double[] acc;
    protected final BiFunction<Double, Double, Double> f;
    protected final double mass;
    protected final double deltaT, deltaTSq;
    protected double lastTime;
    protected int lastIndex;

    public static StepAlgorithm algorithmBuilder(AlgorithmType type, BiFunction<Double, Double, Double> f, double deltaT, double tf, double r0, double v0, double mass) {
        switch (type) {
            case BEEMAN:
                return new Beeman(f, deltaT, tf, r0, v0, mass);
            case GEAR:
                return new Gear(f, deltaT, tf, r0, v0, mass);
            case VERLET:
                return new Verlet(f, deltaT, tf, r0, v0, mass);
            default:
                throw new IllegalArgumentException("Invalid algorithm type");
        }
    }

    protected StepAlgorithm(BiFunction<Double, Double, Double> f, double deltaT, double tf, double r0, double v0, double mass) {
        int size = (int) Math.round(tf / deltaT) + 2; // [-dt; tf] including both ends

        this.f = f;
        this.mass = mass;
        this.deltaT = deltaT;
        this.deltaTSq = deltaT * deltaT;
        this.lastTime = 0.0;

        this.pos = new double[size];
        this.pos[1] = r0;
        this.vel = new double[size];
        this.vel[1] = v0;
        this.acc = new double[size];
        this.acc[1] = f.apply(r0, v0) / this.mass;

        final Step<Double> prevStep = eulerPrecedingStep(this.lastTime, this.pos[1], this.vel[1], this.acc[1]);
        this.pos[0] = prevStep.getPos();
        this.vel[0] = prevStep.getVel();
        this.acc[0] = prevStep.getAcc();

        this.lastIndex = 1;
    }

    private Step<Double> eulerPrecedingStep(double t, double r, double v, double a) {
        double vPrev = v - deltaT * a;
        double rPrev = r - deltaT * vPrev + deltaTSq * a / 2.0;
        double aPrev = f.apply(rPrev, vPrev) / mass;

        return new Step<>(t - deltaT, rPrev, vPrev, aPrev);
    }

    public Step<Double> getLastStep() {
        return new Step<>(lastTime, pos[lastIndex], vel[lastIndex], acc[lastIndex]);
    }

    @Override
    public boolean hasNext() {
        return lastIndex + 1 < pos.length;
    }
}
