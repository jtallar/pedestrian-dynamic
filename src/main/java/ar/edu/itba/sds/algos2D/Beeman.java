package ar.edu.itba.sds.algos2D;

/*
import ar.edu.itba.sds.objects.Step;
import ar.edu.itba.sds.objects.Vector2D;

import java.util.Map;
import java.util.function.BiFunction;

import static ar.edu.itba.sds.objects.Vector2D.*;

public class Beeman extends StepAlgorithm {

    public Beeman(BiFunction<Vector2D, Map<Vector2D, Boolean>, Vector2D> f, double deltaT, double r0, double v0, double mass, double d, int n) {
        super(f, deltaT, r0, v0, mass, d, n);
    }

    @Override
    public Step<Vector2D> next() {
        if (!hasNext()) throw new IndexOutOfBoundsException("No more time steps!");

        // Calculate x(t+dt)
        final Vector2D nextPos = sub(sum(sum(pos.peek(), scalar(vel.peek(), deltaT)), scalar(acc.peek(),2.0 / 3.0 * deltaTSq)), scalar(acc.peekPeek(), 1.0 / 6.0 * deltaTSq));

        // Calculate a(t+dt) using x(t+dt)
        final Vector2D nextAcc = scalar(f.apply(nextPos, staticParticles), 1 / mass);

        // Calculate v(t+dt)
        final Vector2D nextVel = sub(sum(sum(vel.peek(), scalar(nextAcc, 1.0 / 3.0 * deltaT)), scalar(acc.peek(), 5.0 / 6.0 * deltaT)), scalar(acc.peekPeek(), 1.0 / 6.0 * deltaT));

        // Update lastTime and lastIndex
        lastTime += deltaT;
        pos.push(nextPos);
        vel.push(nextVel);
        acc.push(nextAcc);

        return new Step<>(lastTime, nextPos, nextVel, nextAcc);
    }
}
*/
