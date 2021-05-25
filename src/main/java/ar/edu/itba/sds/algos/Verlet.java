package ar.edu.itba.sds.algos;

import ar.edu.itba.sds.objects.Step;

import java.util.function.BiFunction;

public class Verlet extends StepAlgorithm {

    public Verlet(BiFunction<Double, Double, Double> f, double deltaT, double tf, double r0, double v0, double mass) {
        super(f, deltaT, tf, r0, v0, mass);
    }

    @Override
    public Step<Double> next() {
        if (!hasNext()) throw new IndexOutOfBoundsException("No more timesteps!");

        // Calculate x(t+dt)
        pos[lastIndex + 1] = 2 * pos[lastIndex] - pos[lastIndex - 1] + deltaTSq * acc[lastIndex];

        // Calculate v(t+dt)
        vel[lastIndex + 1] = (pos[lastIndex + 1] - pos[lastIndex - 1]) / (2 * deltaT);

        // Estimate a(t+dt) using x(t+dt) and predicted v(t+dt)
        acc[lastIndex + 1] = f.apply(pos[lastIndex + 1], vel[lastIndex + 1]) / mass;

        // Update lastTime and lastIndex
        lastTime += deltaT;
        lastIndex++;

        return new Step<>(lastTime, pos[lastIndex], vel[lastIndex], acc[lastIndex]);
    }
}
