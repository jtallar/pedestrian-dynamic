package ar.edu.itba.sds;

import ar.edu.itba.sds.algos.StepAlgorithm;
import ar.edu.itba.sds.objects.AlgorithmType;
import ar.edu.itba.sds.objects.Step;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DampedOscillation {
    private static final String DEFAULT_CONFIG = "config.json";
    private static final String CONFIG_PARAM = "config";

    private static final String DELTA_T_PARAM = "dt";
    private static final String ALGORITHM_PARAM = "algo";

    private static final String DYNAMIC_CONFIG_KEY = "dynamic_file";

    private static final String DELTA_T_SIM_CONFIG_KEY = "delta_t_sim";
    private static final String DELTA_T_PRINT_CONFIG_KEY = "delta_t_print";

    private static final String OSC_OBJECT_CONFIG_KEY = "osc";
    private static final String OSC_ALGO_CONFIG_KEY = "algo";
    private static final String OSC_MASS_CONFIG_KEY = "mass";
    private static final String OSC_K_CONFIG_KEY = "k";
    private static final String OSC_GAMMA_CONFIG_KEY = "gamma";
    private static final String OSC_TF_CONFIG_KEY = "tf";
    private static final String OSC_R0_CONFIG_KEY = "r0";
    private static final String OSC_A_CONFIG_KEY = "A";

    private static final double FLOAT_EPS = 1e-6;

    private static final int ERROR_STATUS = 1;

    private static String dynamicFilename;
    private static AlgorithmType algorithmType;
    private static double mass, k, gamma, amp;
    private static double r0, v0;
    private static double time = 0.0, timeFinal;
    private static double deltaTimeSim, deltaTimePrint;

    public static void main(String[] args) {
        // Get simulation params
        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }
        v0 = -amp * gamma  / (2.0 * mass);
        final BiFunction<Double, Double, Double> f = (r, v) -> -k * r - gamma * v;

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
        final StepAlgorithm algorithm = StepAlgorithm.algorithmBuilder(algorithmType, f, deltaTimeSim, timeFinal, r0, v0, mass);
        Step<Double> curStep = algorithm.getLastStep();
        printStep(curStep);
        while (algorithm.hasNext()) {
            curStep = algorithm.next();
            if (doubleMultiple(curStep.getTime(), deltaTimePrint)) {
                printStep(curStep);
            }
        }

        // Print simulation time
        long endTime = System.currentTimeMillis();
        System.out.printf("Simulation time \t\t ⏱  %g seconds\n", (endTime - startTime) / 1000.0);
    }

    private static void printStep(Step<Double> step) {
        try {
            appendToFile(dynamicFilename, String.format("%.30E\n%.30E %.30E\n*\n", step.getTime(), step.getPos(), step.getVel()));
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

            final JSONObject oscObject = config.getJSONObject(OSC_OBJECT_CONFIG_KEY);
            algorithmType = AlgorithmType.of(oscObject.getString(OSC_ALGO_CONFIG_KEY));
            if (algorithmType == null) throw new ArgumentException("Invalid algorithm name");

            mass = getConfigDouble(oscObject, OSC_MASS_CONFIG_KEY, v -> v > 0);
            k = getConfigDouble(oscObject, OSC_K_CONFIG_KEY, v -> v > 0);
            gamma = getConfigDouble(oscObject, OSC_GAMMA_CONFIG_KEY, v -> v > 0);
            timeFinal = getConfigDouble(oscObject, OSC_TF_CONFIG_KEY, v -> v > 0 &&  doubleMultiple(v, deltaTimeSim));
            r0 = getConfigDouble(oscObject, OSC_R0_CONFIG_KEY, v -> true);
            amp = getConfigDouble(oscObject, OSC_A_CONFIG_KEY, v -> v > 0);
        } catch (FileNotFoundException e) {
            throw new ArgumentException(String.format("Config file %s not found", configFilename));
        } catch (IOException e) {
            throw new ArgumentException("Error parsing config file");
        } catch (JSONException e) {
            throw new ArgumentException("Missing configurations in config file. Must define \"dynamic_file\" and \"osc\".");
        }

        // Check properties to override parameters for faster simulation repetition
        String algorithmName = properties.getProperty(ALGORITHM_PARAM);
        if (algorithmName != null) {
            algorithmType = AlgorithmType.of(algorithmName);
            if (algorithmType == null) throw new ArgumentException("Invalid algorithm name");
        }
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
        // If dt or algo were set by param, rename dynamic file with algorithm and dt
        if (algorithmName != null || deltaTimeProp != null) {
            dynamicFilename = String.format("%s-%.10E.txt", algorithmType.name(), deltaTimeSim);
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
}
