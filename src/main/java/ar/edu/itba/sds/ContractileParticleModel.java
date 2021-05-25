package ar.edu.itba.sds;

import ar.edu.itba.sds.algos2D.StepAlgorithm;
import ar.edu.itba.sds.objects.AlgorithmType;
import ar.edu.itba.sds.objects.Step;
import ar.edu.itba.sds.objects.Vector2D;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContractileParticleModel {
    private static final String DEFAULT_CONFIG = "config.json";
    private static final String CONFIG_PARAM = "config";

    private static final String DELTA_T_PARAM = "dt";
    private static final String ALGORITHM_PARAM = "algo";
    private static final String DYNAMIC_FILE_PARAM = "dynamicSuf";
    private static final String V0_PARAM = "v0";

    private static final String DYNAMIC_CONFIG_KEY = "dynamic_file";

    private static final String DELTA_T_SIM_CONFIG_KEY = "delta_t_sim";
    private static final String DELTA_T_PRINT_CONFIG_KEY = "delta_t_print";

    private static final String RAD_OBJECT_CONFIG_KEY = "rad";
    private static final String RAD_ALGO_CONFIG_KEY = "algo";
    private static final String RAD_MASS_CONFIG_KEY = "mass";
    private static final String RAD_K_CONFIG_KEY = "k";
    private static final String RAD_N_CONFIG_KEY = "N";
    private static final String RAD_D_CONFIG_KEY = "D";
    private static final String RAD_Q_CONFIG_KEY = "Q";
    private static final String RAD_V0_CONFIG_KEY = "v0";
    private static final String RAD_USE_SEED_CONFIG_KEY = "use_seed";
    private static final String RAD_SEED_CONFIG_KEY = "seed";


    private static final double FLOAT_EPS = 1e-6;

    private static final int ERROR_STATUS = 1;

    private static String dynamicFilename;
    private static AlgorithmType algorithmType;
    private static double mass, k, d, q;
    private static double r0;
    private static double deltaTimeSim, deltaTimePrint;
    private static int n, v0;
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

        // force function
        final BiFunction<Vector2D, Map<Vector2D, Boolean>, Vector2D> f = (pi, map) ->
                map.entrySet()
                .stream()
                .map(e -> Vector2D.force(pi, e.getKey(), k, q, e.getValue()))
                .reduce(new Vector2D(), Vector2D::sum);
        final Random r = new Random(seed);
        final double rangeMin = (n - 1) * d / 2 - d;
        final double rangeMax = (n - 1) * d / 2 + d;
        r0 = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        // Delete dynamicFile if already exists
        try {
            Files.deleteIfExists(Paths.get(dynamicFilename));
        } catch (IOException e) {
            System.err.printf("Could not delete %s\n", dynamicFilename);
            System.exit(ERROR_STATUS);
            return;
        }

        System.out.printf("Running %s with dt_sim=%.3E and dt_print=%.3E. \nOutput to ", algorithmType.name(), deltaTimeSim, deltaTimePrint);
        System.err.printf("%s", dynamicFilename);
        System.out.print("\n\n");

        // Measure simulation time
        long startTime = System.currentTimeMillis();

        // Simulation
        final StepAlgorithm algorithm = StepAlgorithm.algorithmBuilder(algorithmType, f, deltaTimeSim, r0, v0, mass, d, n);
        Step<Vector2D> curStep = algorithm.getLastStep();
        printStep(curStep);
        while (algorithm.hasNext()) {
            curStep = algorithm.next();
            if (doubleMultiple(curStep.getTime(), deltaTimePrint)) {
                printStep(curStep);
            }
        }
        System.out.println("Done");

        // Print simulation time
        long endTime = System.currentTimeMillis();
        System.out.printf("Simulation time \t\t ⏱  %g seconds\n", (endTime - startTime) / 1000.0);
    }

    private static void printStep(Step<Vector2D> step) {
        try {
            appendToFile(dynamicFilename, String.format("%.30E\n%.30E %.30E %.30E %.30E\n*\n",
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
            deltaTimeSim = getConfigDouble(config, DELTA_T_SIM_CONFIG_KEY, v -> v > 0);
            deltaTimePrint = getConfigDouble(config, DELTA_T_PRINT_CONFIG_KEY, v -> v > 0 && doubleMultiple(v, deltaTimeSim));

            final JSONObject radObject = config.getJSONObject(RAD_OBJECT_CONFIG_KEY);
            algorithmType = AlgorithmType.of(radObject.getString(RAD_ALGO_CONFIG_KEY));
            if (algorithmType == null) throw new ArgumentException("Invalid algorithm name");

            // get double params
            mass = getConfigDouble(radObject, RAD_MASS_CONFIG_KEY, v -> v > 0);
            k = getConfigDouble(radObject, RAD_K_CONFIG_KEY, v -> v > 0);
            d = getConfigDouble(radObject, RAD_D_CONFIG_KEY, v -> v > 0);
            q = getConfigDouble(radObject, RAD_Q_CONFIG_KEY, v -> v > 0);

            // get int params
            n = getConfigInt(radObject, RAD_N_CONFIG_KEY, v -> v > 0);
            v0 = getConfigInt(radObject, RAD_V0_CONFIG_KEY, v -> v > 0);
            final boolean useSeed = radObject.getBoolean(RAD_USE_SEED_CONFIG_KEY);
            seed = (useSeed)? getConfigInt(radObject, RAD_SEED_CONFIG_KEY, v -> v > 0) : System.nanoTime();

        } catch (FileNotFoundException e) {
            throw new ArgumentException(String.format("Config file %s not found", configFilename));
        } catch (IOException e) {
            throw new ArgumentException("Error parsing config file");
        } catch (JSONException e) {
            throw new ArgumentException(e.getMessage());
        }

        // Check properties to override parameters for faster simulation repetition
        String algorithmName = properties.getProperty(ALGORITHM_PARAM);
        if (algorithmName != null) {
            algorithmType = AlgorithmType.of(algorithmName);
            if (algorithmType == null) throw new ArgumentException("Invalid algorithm name");
        }

        String dynamicSuffix = properties.getProperty(DYNAMIC_FILE_PARAM, "");

        String deltaTimeProp = properties.getProperty(DELTA_T_PARAM);
        if (deltaTimeProp != null) {
            double value;
            try {
                value = Double.parseDouble(deltaTimeProp);
                if (value <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new ArgumentException(String.format("Invalid %s param", DELTA_T_PARAM));
            }
            deltaTimeSim = deltaTimePrint = value;
        }

        String voProp = properties.getProperty(V0_PARAM);
        if (voProp != null) {
            int value;
            try {
                value = (int) Double.parseDouble(voProp);
                if (value <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new ArgumentException(String.format("Invalid %s param", V0_PARAM));
            }
            v0 = value;
        }

        // If dt, algo or dt were set by param and dynamic name wasn't, rename dynamic file with algorithm and dt
        if (algorithmName != null || deltaTimeProp != null || voProp != null) {
            dynamicFilename = String.format("%s_%.5E_%d%s.txt", algorithmType.name(), deltaTimeSim, v0, dynamicSuffix);
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
