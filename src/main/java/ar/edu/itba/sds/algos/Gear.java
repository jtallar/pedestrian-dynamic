package ar.edu.itba.sds.algos;

import ar.edu.itba.sds.objects.Step;

import java.util.function.BiFunction;

public class Gear extends StepAlgorithm {
    private final double[] r3;
    private final double[] r4;
    private final double[] r5;
    private final double[] alpha;

    // CAN ONLY BE USED FOR f = (r, v) -> -k * r - gamma * v;
    // OR SIMILAR SO THAT df/dt = f(dr/dt, dv/dt)
    public Gear(BiFunction<Double, Double, Double> f, double deltaT, double tf, double r0, double v0, double mass) {
        super(f, deltaT, tf, r0, v0, mass);
        this.alpha = new double[]{(3.0 / 16.0), (251.0 / 360), 1.0, (11.0 / 18.0), (1.0 / 6.0), (1.0 / 60.0)};
        this.r3 = new double[pos.length];
        this.r3[1] = f.apply(vel[1], acc[1]) / mass;
        this.r4 = new double[pos.length];
        this.r4[1] = f.apply(acc[1], r3[1]) / mass;
        this.r5 = new double[pos.length];
        this.r5[1] = f.apply(r3[1], r4[1]) / mass;
    }

    @Override
    public Step<Double> next() {
        if (!hasNext()) throw new IndexOutOfBoundsException("No more timesteps!");

        double [][] r = {pos, vel, acc, r3, r4, r5};

        // Predict each r
        for(int j = 0; j < r.length; j++)
            for (int i = j; i < r.length; i++)
                r[j][lastIndex + 1] += r[i][lastIndex] * factor(deltaT,i - j);

        // Estimation Delta R2
        double deltaR2 = ((f.apply(pos[lastIndex + 1], vel[lastIndex + 1]) / mass ) - acc[lastIndex + 1])
                        * factor(deltaT, 2);

        // Correct every r
        for(int i = 0; i < r.length; i++)
            r[i][lastIndex + 1] = r[i][lastIndex + 1] + alpha[i] * deltaR2 / factor(deltaT, i);

        // Update lastTime and lastIndex
        lastTime += deltaT;
        lastIndex++;

        return new Step<>(lastTime, pos[lastIndex], vel[lastIndex], acc[lastIndex]);
    }

    private double factor(double deltaT, int k){
        if(k == 0) return 1;
        return Math.pow(deltaT, k) / factorial(k);
    }

    public int factorial (double n) {
        if (n == 0 || n == 1)
            return 1;
        return (int) (n * factorial(n - 1));
    }
}
