package ar.edu.itba.sds.algos2D;

import ar.edu.itba.sds.objects.AlgorithmType;
import ar.edu.itba.sds.objects.CustomStack;
import ar.edu.itba.sds.objects.Step;
import ar.edu.itba.sds.objects.Vector2D;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

/*
import static ar.edu.itba.sds.objects.Vector2D.*;

public abstract class StepAlgorithm implements Iterator<Step<Vector2D>> {

    protected final CustomStack<Vector2D> pos;
    protected final CustomStack<Vector2D> vel;
    protected final CustomStack<Vector2D> acc;
    protected final BiFunction<Vector2D, Map<Vector2D, Boolean>, Vector2D> f;
    protected final Map<Vector2D, Boolean> staticParticles;
    protected final double mass;
    protected final double deltaT, deltaTSq;
    protected double lastTime;
    protected boolean done;
    protected final double maxX, maxY;
    protected final double dCut;

    public static StepAlgorithm algorithmBuilder(AlgorithmType type, BiFunction<Vector2D, Map<Vector2D, Boolean>, Vector2D> f, double deltaT, double r0, double v0, double mass, double d, int n) {
        switch (type) {
            case BEEMAN:
                return new ar.edu.itba.sds.algos2D.Beeman(f, deltaT, r0, v0, mass, d, n);
            default:
                throw new IllegalArgumentException("Invalid algorithm type");
        }
    }

    protected StepAlgorithm(BiFunction<Vector2D, Map<Vector2D, Boolean>, Vector2D> f, double deltaT, double r0, double v0, double mass, double d, int n) {

        this.f = f;
        this.mass = mass;
        this.deltaT = deltaT;
        this.deltaTSq = deltaT * deltaT;
        this.lastTime = 0.0;
        this.done = false;
        this.maxX = d * n;
        this.maxY = d * (n - 1);
        this.dCut = 0.01 * d;

        this.staticParticles = initStaticParticles(d, n);
        final Vector2D pos0 = new Vector2D(0.0, r0);
        final Vector2D vel0 = new Vector2D(v0, 0.0);
        final Vector2D acc0 = scalar(f.apply(pos0, this.staticParticles), 1 / this.mass);

        final Step<Vector2D> prevStep = eulerPrecedingStep(this.lastTime, pos0, vel0, acc0);

        this.pos = new CustomStack<>();
        pos.push(prevStep.getPos());
        pos.push(pos0);
        this.vel = new CustomStack<>();
        vel.push(prevStep.getVel());
        vel.push(vel0);
        this.acc = new CustomStack<>();
        acc.push(prevStep.getAcc());
        acc.push(acc0);
    }

    public Step<Vector2D> getLastStep() {
        return new Step<>(lastTime, pos.peek(), vel.peek(), acc.peek());
    }

    @Override
    public boolean hasNext() {
        if (lastTime < deltaT) return true;
        if (pos.peek().getX() >= maxX) {
            System.out.println("Algorithm finished. Crossed max X (n * d) position");
            return false;
        }
        if (pos.peek().getX() <= 0.0) {
            System.out.println("Algorithm finished. Crossed min X (0.0) position");
            return false;
        }
        if (pos.peek().getY() >= maxY) {
            System.out.println("Algorithm finished. Crossed max Y ((n - 1) * d) position");
            return false;
        }
        if (pos.peek().getY() <= 0.0) {
            System.out.println("Algorithm finished. Crossed min Y (0.0) position");
            return false;
        }
        if (staticParticles.keySet().stream().anyMatch(v -> dist(pos.peek(), v) < dCut)) {
            System.out.println("Algorithm finished. Close particles");
            return false;
        }
        return true;
    }

    // private auxiliary functions

    private Step<Vector2D> eulerPrecedingStep(double t, Vector2D r, Vector2D v, Vector2D a) {
        final Vector2D prevVel = sub(v, scalar(a, deltaT));
        final Vector2D prevPos = sum(sub(r, scalar(prevVel, deltaT)), scalar(a, deltaTSq / 2.0));
        final Vector2D prevAcc = scalar(f.apply(prevPos, staticParticles), 1.0 / mass);
        return new Step<>(t - deltaT, prevPos, prevVel, prevAcc);
    }

    private Map<Vector2D, Boolean> initStaticParticles(double d, int n) {
        final Map<Vector2D, Boolean> map = new HashMap<>(n * n);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                map.put(new Vector2D((i + 1) * d, j * d), (i + j) % 2 == 0);

        return map;
    }
}*/
