package ar.edu.itba.sds.objects;

public class Particle {
    private final int id;
    private Vector2D pos;
    private Vector2D vel;
    private double r;

    /**
     * Create a particle with radius
     * @param id particle id
     * @param pos particle initial position vector
     * @param vel particle initial velocity vector
     * @param r particle radius
     */
    public Particle(int id, Vector2D pos, Vector2D vel, double r) {
        this.id = id;
        this.pos = pos;
        this.vel = vel;
        this.r = r;
    }

    public double centerDistance(Particle other) {
        return Math.sqrt(Math.pow(Vector2D.deltaX(pos, other.pos), 2) + Math.pow(Vector2D.deltaY(pos, other.pos), 2));
    }

    public double borderDistance(Particle other) {
        return centerDistance(other) - r - other.r;
    }

    private void setVelocity(Vector2D vel) {
        this.vel = vel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle)) return false;
        Particle particle = (Particle) o;
        return id == particle.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}