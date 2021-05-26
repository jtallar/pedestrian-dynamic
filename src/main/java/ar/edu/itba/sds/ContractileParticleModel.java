package ar.edu.itba.sds;

import ar.edu.itba.sds.algos2D.StepAlgorithm;
import ar.edu.itba.sds.objects.AlgorithmType;
import ar.edu.itba.sds.objects.Particle;
import ar.edu.itba.sds.objects.Step;
import ar.edu.itba.sds.objects.Vector2D;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContractileParticleModel {
    private static final String DEFAULT_CONFIG = "config.json";
    private static final String CONFIG_PARAM = "config";

    private static final String DYNAMIC_FILE_PARAM = "dynamicSuf";
    private static final String D_PARAM = "d";
    private static final String N_PARAM = "n";

    private static final String DYNAMIC_CONFIG_KEY = "dynamic_file";

    private static final String N_CONFIG_KEY = "N";
    private static final String L_CONFIG_KEY = "L";
    private static final String D_CONFIG_KEY = "d";

    private static final String R_MIN_CONFIG_KEY = "rmin";
    private static final String R_MAX_CONFIG_KEY = "rmax";
    private static final String VD_MAX_CONFIG_KEY = "vdmax";
    private static final String TAU_CONFIG_KEY = "tau";
    private static final String BETA_CONFIG_KEY = "beta";

    // TODO: Ver si aca hay algo aleatorio o no hace falta leer este seed
    private static final String USE_SEED_CONFIG_KEY = "use_seed";
    private static final String SEED_CONFIG_KEY = "seed";

    private static final String DT_MULT_CONFIG_KEY = "dt_print_mult";

    private static final double FLOAT_EPS = 1e-6;

    private static final int ERROR_STATUS = 1;

    private static String dynamicFilename;
    private static int n;
    private static double l, d;
    private static double rmin, rmax, vdmax, tau, beta;
    private static double time, deltaTimeSim;
    private static int deltaTimePrintMult;
    private static long seed;

    public static void main(String[] args) {
        // Get simulation params
        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        deltaTimeSim = rmin / (2 * vdmax);

        final Random rand = new Random(seed);

        // Parse dynamic file to initialize particle list
        List<Particle> particles;
        try(BufferedReader reader = new BufferedReader(new FileReader(dynamicFilename))) {
            // Set initial time
            time = Double.parseDouble(reader.readLine());
            // Create particle list
            particles = createParticleList(reader, n, rmin);
        } catch (FileNotFoundException e) {
            System.err.println("Dynamic file not found");
            System.exit(ERROR_STATUS);
            return;
        } catch (IOException e) {
            System.err.println("Error reading dynamic file");
            System.exit(ERROR_STATUS);
            return;
        }

        System.out.printf("Running with N=%d and d=%.3E. \nOutput to ", n, d);
        System.err.printf("%s", dynamicFilename);
        System.out.print("\n\n");

        // Measure simulation time
        long startTime = System.currentTimeMillis();

        // Simulation
//        final StepAlgorithm algorithm = StepAlgorithm.algorithmBuilder(algorithmType, f, deltaTimeSim, r0, v0, mass, d, n);
//        Step<Vector2D> curStep = algorithm.getLastStep();
//        printStep(curStep);
//        while (algorithm.hasNext()) {
//            curStep = algorithm.next();
//            if (doubleMultiple(curStep.getTime(), deltaTimePrint)) {
//                printStep(curStep);
//            }
//        }

        // Print simulation time
        long endTime = System.currentTimeMillis();
        System.out.printf("Simulation time \t\t ⏱  %g seconds\n", (endTime - startTime) / 1000.0);
    }

    private static List<Particle> createParticleList(BufferedReader reader, int n, double radius)
            throws IOException {

        List<Particle> particles = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String line = reader.readLine();
            if (line == null) throw new IOException();

            // Values has format (x, y, vx, vy)
            final String[] values = line.split(" ");
            double x = Double.parseDouble(values[0]), y = Double.parseDouble(values[1]);
            double vx = Double.parseDouble(values[2]), vy = Double.parseDouble(values[3]);

            particles.add(new Particle(i, new Vector2D(x, y), new Vector2D(vx, vy), radius));
        }

        // Check that there are no lines remaining
        if (reader.readLine() != null) throw new IOException();

        return particles;
    }

    private static void printStep(Step<Vector2D> step) {
        try {
            // TODO: Check que precision es necesaria aca, si con 7E va bien
            appendToFile(dynamicFilename, String.format("%.7E\n%.7E %.7E %.7E %.7E\n*\n",
                    step.getTime(), step.getPos().getX(), step.getPos().getY(), step.getVel().getX(), step.getVel().getY()));
        } catch (IOException e) {
            System.err.println("Error writing dynamic file");
            System.exit(ERROR_STATUS);
        }
    }

    private static void appendToFile(String filename, String s) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(s);
        }
    }

    private static void argumentParsing() throws ArgumentException {
        Properties properties = System.getProperties();
        String configFilename = properties.getProperty(CONFIG_PARAM, DEFAULT_CONFIG);

        try(BufferedReader reader = new BufferedReader(new FileReader(configFilename))) {
            JSONObject config = new JSONObject(reader.lines().collect(Collectors.joining()));
            dynamicFilename = config.getString(DYNAMIC_CONFIG_KEY);

            // get int params
            n = getConfigInt(config, N_CONFIG_KEY, v -> v > 0);

            // get double params
            l = getConfigDouble(config, L_CONFIG_KEY, v -> v > 0);
            d = getConfigDouble(config, D_CONFIG_KEY, v -> v > 0);

            rmin = getConfigDouble(config, R_MIN_CONFIG_KEY, v -> v >= 0);
            rmax = getConfigDouble(config, R_MAX_CONFIG_KEY, v -> v >= rmin);
            vdmax = getConfigDouble(config, VD_MAX_CONFIG_KEY, v -> v > 0);
            tau = getConfigDouble(config, TAU_CONFIG_KEY, v -> v > 0);
            beta = getConfigDouble(config, BETA_CONFIG_KEY, v -> v > 0);

            final boolean useSeed = config.getBoolean(USE_SEED_CONFIG_KEY);
            seed = (useSeed) ? getConfigInt(config, SEED_CONFIG_KEY, v -> v > 0) : System.nanoTime();

            deltaTimePrintMult = getConfigInt(config, DT_MULT_CONFIG_KEY, v -> v > 0);

        } catch (FileNotFoundException e) {
            throw new ArgumentException(String.format("Config file %s not found", configFilename));
        } catch (IOException e) {
            throw new ArgumentException("Error parsing config file");
        } catch (JSONException e) {
            throw new ArgumentException(e.getMessage());
        }

        // Check properties to override parameters for faster simulation repetition
        String dynamicSuffix = properties.getProperty(DYNAMIC_FILE_PARAM, "");

        String dProp = properties.getProperty(D_PARAM);
        if (dProp != null) {
            double value;
            try {
                value = Double.parseDouble(dProp);
                if (value <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new ArgumentException(String.format("Invalid %s param", D_PARAM));
            }
            d = value;
        }

        String nProp = properties.getProperty(N_PARAM);
        if (nProp != null) {
            int value;
            try {
                value = (int) Double.parseDouble(nProp);
                if (value <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new ArgumentException(String.format("Invalid %s param", N_PARAM));
            }
            n = value;
        }

        // If d or n were set by param and dynamic name wasn't, rename dynamic file with d and n
        if (dProp != null || nProp != null ) {
            dynamicFilename = String.format("dynamic_%.5E_%d%s.txt", d, n, dynamicSuffix);
        }
    }


    /**
     * @param value double to check if valid
     * @param k factor to be multiple of
     * @return true if value ~= k * integer
     */
    private static boolean doubleMultiple(double value, double k) {
        if (value % k < FLOAT_EPS) return true;
        return Math.abs(value % k - k) < FLOAT_EPS;
    }

    private static double getConfigDouble(JSONObject config, String key, Predicate<Double> validator) throws ArgumentException {
        double value;
        try {
            value = config.getDouble(key);
            if (!validator.test(value)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ArgumentException(String.format("Invalid %s number", key));
        }
        return value;
    }

    private static int getConfigInt(JSONObject config, String key, Predicate<Integer> validator) throws ArgumentException {
        int value;
        try {
            value = config.getInt(key);
            if (!validator.test(value)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ArgumentException(String.format("Invalid %s number", key));
        }
        return value;
    }
}
