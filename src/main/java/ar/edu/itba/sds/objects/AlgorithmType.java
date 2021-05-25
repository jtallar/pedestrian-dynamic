package ar.edu.itba.sds.objects;

public enum AlgorithmType {
    BEEMAN("Beeman"),
    VERLET("Verlet"),
    GEAR("Gear");

    private final String key;

    AlgorithmType(String key) {
        this.key = key;
    }

    public static AlgorithmType of(String key) {
        for (AlgorithmType op : AlgorithmType.values()) {
            if (op.key.equals(key)) return op;
        }
        return null;
    }
}
