package ar.edu.itba.sds;

import ar.edu.itba.sds.objects.Particle;
import ar.edu.itba.sds.objects.Step;
import ar.edu.itba.sds.objects.Vector2D;
import ar.edu.itba.sds.objects.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ContractileParticleModel {
    private static final String DEFAULT_CONFIG = "config.json";
    private static final String CONFIG_PARAM = "config";

    private static final String DYNAMIC_FILE_PARAM = "dynamic";
    private static final String EXIT_FILE_PARAM = "exit";
    private static final String D_PARAM = "d";
    private static final String N_PARAM = "n";

    private static final String DYNAMIC_CONFIG_KEY = "dynamic_file";
    private static final String EXIT_CONFIG_KEY = "exit_file";

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

    private static String dynamicFilename, exitFilename;
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

        System.out.printf("Running with N=%d and d=%.3E. \nOutput to ", n, d);
        System.err.printf("%s %s", dynamicFilename, exitFilename);
        System.out.print("\n\n");


        // Cell index method variables
        int cimM = (int) (l / (2 * rmax));
        double cimCellWidth = l / cimM;
        Map<Pair<Integer, Integer>, Set<Particle>> cellMatrix = new HashMap<>();

        // Delete exitFile if already exists
        try {
            Files.deleteIfExists(Paths.get(exitFilename));
        } catch (IOException e) {
            System.err.printf("Could not delete %s\n", exitFilename);
            System.exit(ERROR_STATUS);
            return;
        }

        // Parse dynamic file to initialize particle list
        List<Particle> particles;
        try(BufferedReader reader = new BufferedReader(new FileReader(dynamicFilename))) {
            // Set initial time
            time = Double.parseDouble(reader.readLine());
            // Set particle class properties
            Particle.setSide(l);
            Particle.setDoorWidth(d);
            // Create particle list
            particles = createParticleList(reader, n, cellMatrix, cimM, cimCellWidth);
        } catch (FileNotFoundException e) {
            System.err.println("Dynamic file not found");
            System.exit(ERROR_STATUS);
            return;
        } catch (IOException e) {
            System.err.println("Error reading dynamic file");
            System.exit(ERROR_STATUS);
            return;
        }
        // Measure simulation time
        long startTime = System.currentTimeMillis();

        // Simulation
        Set<Integer> exitedRoomIds = new HashSet<>();
        long printCount = 0;
        while (!particles.isEmpty()) {
            // Instantiate new String Builders
            StringBuilder exitStr = new StringBuilder();
            StringBuilder dynamicStr = new StringBuilder();

            // Update time
            time += deltaTimeSim;
            // Update print count
            printCount++;
            // Save time to print if needed
            if (printCount == deltaTimePrintMult) dynamicStr.append(getTimePrint(time));

            // Find contacts
            findCollisions(cellMatrix, cimM);

            List<Particle> nextParticleList = new ArrayList<>();
            // Iterate over particle list
            for (Particle p : particles) {
                // Compute escape vel
                p.updateEscapeVel(vdmax);
                // Update radius
                p.updateRadius(rmin, rmax, tau, deltaTimeSim);
                // Compute target vel
                p.updateTargetVel(vdmax, rmin, rmax, beta);
                // Save previous rowCol for particle in cell matrix
                final Pair<Integer, Integer> curRowCol = getRowColPair(p.getPos().getX(), p.getPos().getY(), cimM, cimCellWidth);
                // Update speed and position
                final Step<Vector2D> pStep = p.updateState(time, deltaTimeSim);
                // If door crossed and just crossed, print to exitStr
                if (p.doorCrossed() && !exitedRoomIds.contains(p.getId())) {
                    exitStr.append(getTimePrint(time));
                    exitedRoomIds.add(p.getId());
                }
                // If goal reached, remove particle from particle list and cell matrix
                if (p.reachedGoal()) mapSetRemove(cellMatrix, curRowCol, p);
                else {
                    nextParticleList.add(p);
                    // Calculate new rowCol for particle in cell matrix
                    final Pair<Integer, Integer> newRowCol = getRowColPair(p.getPos().getX(), p.getPos().getY(), cimM, cimCellWidth);
                    // Update particle in cell matrix if needed
                    if (!newRowCol.equals(curRowCol)) {
                        mapSetRemove(cellMatrix, curRowCol, p);
                        mapSetAdd(cellMatrix, newRowCol, p);
                    }
                }
                // Save pStep to print if needed
                if (printCount == deltaTimePrintMult) dynamicStr.append(getStepPrint(p.getId(), pStep));
            }
            // Update particle list
            particles = nextParticleList;
            // Append to files
            if (exitStr.length() != 0) appendToFile(exitFilename, exitStr.toString());
            if (dynamicStr.length() != 0) {
                dynamicStr.append("*\n");
                appendToFile(dynamicFilename, dynamicStr.toString());
                printCount = 0;
            }
        }

        // Print simulation time
        long endTime = System.currentTimeMillis();
        System.out.printf("Simulation time \t\t ⏱  %g seconds\n", (endTime - startTime) / 1000.0);
    }

    private static String getTimePrint(double time) {
        // TODO: Check que precision es necesaria aca, si con 7E va bien
        return String.format("%.7E\n", time);
    }

    private static String getStepPrint(int id, Step<Vector2D> step) {
        // TODO: Check que precision es necesaria aca, si con 7E va bien
        return String.format("%d %.7E %.7E %.7E %.7E %.7E\n",
                id, step.getPos().getX(), step.getPos().getY(), step.getVel().getX(), step.getVel().getY(), step.getRadius());
    }

    private static void appendToFile(String filename, String s) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(s);
        } catch (IOException e) {
            System.err.printf("Error writing %s file\n", filename);
            System.exit(ERROR_STATUS);
        }
    }

    private static void mapSetRemove(Map<Pair<Integer, Integer>, Set<Particle>> cellMatrix, Pair<Integer, Integer> key, Particle p) {
        final Set<Particle> set = cellMatrix.getOrDefault(key, new HashSet<>());
        set.remove(p);
        cellMatrix.put(key, set);
    }

    private static void mapSetAdd(Map<Pair<Integer, Integer>, Set<Particle>> cellMatrix, Pair<Integer, Integer> key, Particle p) {
        final Set<Particle> set = cellMatrix.getOrDefault(key, new HashSet<>());
        set.add(p);
        cellMatrix.put(key, set);
    }

    private static void findCollisions(Map<Pair<Integer, Integer>, Set<Particle>> cellMatrix, int M) {
        for (int cell = -M * (int) Math.ceil(M / 2.0); cell < M * M; cell++) {
            Pair<Integer, Integer> rowColPair = integerDivision(cell, M);
            int row = rowColPair.getKey(), col = rowColPair.getValue();
            // Iterate cell over itself
            findSetCollision(cellMatrix.getOrDefault(new Pair<>(row, col), new HashSet<>()));

            // Iterate over 4 neighbours --> L` --> DOWN, DOWN-LEFT, LEFT, UP-LEFT
            List<Pair<Integer, Integer>> visitRowCol = Arrays.asList(new Pair<>(row - 1, col),
                    new Pair<>(row - 1, col - 1), new Pair<>(row, col - 1), new Pair<>(row + 1, col - 1));
            for (Pair<Integer, Integer> pair : visitRowCol) {
                findSetCollision(cellMatrix.getOrDefault(new Pair<>(row, col), new HashSet<>()), cellMatrix.getOrDefault(pair, new HashSet<>()));
            }
        }
    }

    // Collisions in the same set
    private static void findSetCollision(Set<Particle> set) {
        final Vector2D leftVertex = new Vector2D((l - d) / 2, 0);
        final Vector2D rightVertex = new Vector2D((l + d) / 2, 0);

        for (Particle p1 : set) {
            for (Particle p2 : set) {
                int c = p1.compareTo(p2);
                if (c == 0) {
                    final Vector2D pos = p1.getPos();
                    double radius = p1.getR();
                    // Left wall collision
                    if (pos.getX() < radius) p1.addCollision(new Vector2D(0, pos.getY()));
                    // Right wall collision
                    else if (pos.getX() > l - radius) p1.addCollision(new Vector2D(l, pos.getY()));

                    // Top wall collision
                    if (pos.getY() > l - radius) p1.addCollision(new Vector2D(pos.getX(), l));
                    // Bottom wall collision
                    else if ((pos.getX() < leftVertex.getX() || pos.getX() > rightVertex.getX()) &&
                            (-radius < pos.getY() && pos.getY() < radius)) p1.addCollision(new Vector2D(pos.getX(), 0));
                    // Left vertex collision
                    else if (Vector2D.dist(pos, leftVertex) < radius) p1.addCollision(leftVertex);
                    // Right vertex collision
                    else if (Vector2D.dist(pos, rightVertex) < radius) p1.addCollision(rightVertex);
                } else if (c > 0) {
                    // Check for particle collision
                    if (p1.borderDistance(p2) < 0) {
                        p1.addCollision(p2.getPos());
                        p2.addCollision(p1.getPos());
                    }
                }
            }
        }
    }

    // Collisions between different sets
    private static void findSetCollision(Set<Particle> set1, Set<Particle> set2) {
        for (Particle p1 : set1) {
            for (Particle p2 : set2) {
                // Check for particle collision
                if (p1.borderDistance(p2) < 0) {
                    p1.addCollision(p2.getPos());
                    p2.addCollision(p1.getPos());
                }
            }
        }
    }

    private static List<Particle> createParticleList(BufferedReader reader, int n,
                                                     Map<Pair<Integer, Integer>, Set<Particle>> cellMatrix, int M, double cellWidth)
            throws IOException {

        List<Particle> particles = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String line = reader.readLine();
            if (line == null) throw new IOException();

            // Values has format (x, y, vx, vy)
            final String[] values = line.split(" ");
            int id = Integer.parseInt(values[0]);
            double x = Double.parseDouble(values[1]), y = Double.parseDouble(values[2]);
            double vx = Double.parseDouble(values[3]), vy = Double.parseDouble(values[4]);
            double radius = Double.parseDouble(values[5]);

            Particle p = new Particle(id, new Vector2D(x, y), new Vector2D(vx, vy), radius);
            particles.add(p);

            // Add particle to CIM matrix
            Pair<Integer, Integer> rowColPair = getRowColPair(x, y, M, cellWidth);
            mapSetAdd(cellMatrix, rowColPair, p);
        }

        // Parse * separator
        String line = reader.readLine();
        if (line == null || !line.equals("*")) throw new IOException();

        // Check that there are no lines remaining
        if (reader.readLine() != null) throw new IOException();

        return particles;
    }

    private static Pair<Integer, Integer> getRowColPair(double x, double y, int M, double cellWidth) {
        int cellIndex = (int) Math.floor(x / cellWidth) + (int) Math.floor(y / cellWidth) * M;
        return integerDivision(cellIndex, M);
    }

    private static Pair<Integer, Integer> integerDivision(int y, int d) {
        int q = (int) Math.floor((double) y / d);
        return new Pair<>(q, y - d * q);
    }

    private static void argumentParsing() throws ArgumentException {
        Properties properties = System.getProperties();
        String configFilename = properties.getProperty(CONFIG_PARAM, DEFAULT_CONFIG);

        try(BufferedReader reader = new BufferedReader(new FileReader(configFilename))) {
            JSONObject config = new JSONObject(reader.lines().collect(Collectors.joining()));
            dynamicFilename = config.getString(DYNAMIC_CONFIG_KEY);
            exitFilename = config.getString(EXIT_CONFIG_KEY);

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
        String dynamicFilenameProp = properties.getProperty(DYNAMIC_FILE_PARAM);
        if (dynamicFilenameProp != null) {
            dynamicFilename = dynamicFilenameProp;
        }

        String exitFilenameProp = properties.getProperty(EXIT_FILE_PARAM);
        if (exitFilenameProp != null) {
            exitFilename = exitFilenameProp;
        }

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
