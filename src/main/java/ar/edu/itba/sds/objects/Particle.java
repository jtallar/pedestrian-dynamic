package ar.edu.itba.sds.objects;

public class Particle implements Comparable<Particle> {
    private static final double DOOR_MARGIN = 0.2;
    private static final double DOOR_TARGET_Y = 0;
    private static final double FAR_TARGET_Y = -10;
    private static final double FAR_TARGET_WIDTH = 3;

    private static Double l = null, d = null;

    private final int id;
    private Vector2D pos;
    private Vector2D vel;
    private double r;
    private Vector2D target;
    private Vector2D eij, escapeVel, targetVel;
    private boolean doorCrossed;

    public static void setSide(double l) {
        Particle.l = l;
    }

    public static void setDoorWidth(double d) {
        Particle.d = d;
    }

    /**
     * Create a particle with radius
     * @param id particle id
     * @param pos particle initial position vector
     * @param vel particle initial velocity vector
     * @param r particle radius
     * @throws IllegalStateException if l and d were not set before this method call.
     */
    public Particle(int id, Vector2D pos, Vector2D vel, double r) {
        if (l == null || d == null) throw new IllegalStateException("You should set l and d before instantiating Particles!");

        this.id = id;
        this.pos = pos;
        this.vel = vel;
        this.r = r;
        this.targetVel = this.escapeVel = null;
        this.eij = new Vector2D();
        this.doorCrossed = false;
        this.target = new Vector2D();
        updateTarget();
    }

    // TODO: Check if we should do this or what paper says: take a random point in door instead of closest one
    private void updateTarget() {
        // TODO: Mepa que se puede poner que y < 0, total si hay colision probablemente sea antes, y sino aparece escapeVel
        double targetY, targetWidth, targetMargin;
        if (doorCrossed) {
            targetY = FAR_TARGET_Y;
            targetWidth = FAR_TARGET_WIDTH;
            targetMargin = 0;
        } else {
            targetY = DOOR_TARGET_Y;
            targetWidth = d;
            targetMargin = DOOR_MARGIN;
        }

        double leftTargetX = (l - targetWidth + targetMargin) / 2;
        double rightTargetX = (l + targetWidth - targetMargin) / 2;

        if (pos.getX() < leftTargetX) target.setCoordinates(leftTargetX, targetY);
        else target.setCoordinates(Math.min(pos.getX(), rightTargetX), targetY);
    }

    // Accumulate collision with otherPos in eij
    public void addCollision(Vector2D otherPos) {
        this.eij = Vector2D.sum(eij, Vector2D.getProjection(pos, otherPos));
    }

    public void updateEscapeVel(double veMod) {
        // TODO: Check si puede pasar que eij = (0,0) Habiendo habido colisiones
        if (this.eij.equals(new Vector2D())) {
            this.escapeVel = null;
            return;
        }

        this.escapeVel = Vector2D.scalar(this.eij, veMod / this.eij.mod());
        this.eij.setCoordinates(0, 0);
    }

    public void updateRadius(double rmin, double rmax, double tau, double dt) {
        // If there were collisions, contract
        if (this.escapeVel != null) {
            this.r = rmin;
            return;
        }
        // Update radius
        this.r += rmax / (tau / dt);

        // If max radius reached, keep it there
        if (this.r >= rmax) this.r = rmax;
    }

    public void updateTargetVel(double vdmax, double rmin, double rmax, double beta) {
        double vMod = vdmax * Math.pow((r - rmin) / (rmax - rmin), beta);
        Vector2D dif = Vector2D.sub(target, pos);
        Vector2D vDir = Vector2D.scalar(dif, 1 / dif.mod());
        this.targetVel = Vector2D.scalar(vDir, vMod);
    }

    public Step<Vector2D> updateState(double curTime, double dt) {
        // If escape vel is not null -> r = rmin -> collision
        // Update velocity
        this.vel = (this.escapeVel != null) ? this.escapeVel : this.targetVel;
        // Update position
        this.pos = Vector2D.sum(this.pos, Vector2D.scalar(this.vel, dt));
        // Build Step
        return new Step<>(curTime, this.r, this.pos, this.vel);
    }

    public boolean reachedGoal() {
        return pos.getY() < FAR_TARGET_Y;
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

    public void setEscapeVel(Vector2D escapeVel) {
        this.escapeVel = escapeVel;
    }

    public Vector2D getPos() {
        return pos;
    }

    public double getR() {
        return r;
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

    @Override
    public int compareTo(Particle o) {
        return id - o.id;
    }

    @Override
    public String toString() {
        return "Particle{" +
                "id=" + id +
                ", pos=" + pos +
                ", vel=" + vel +
                ", r=" + r +
                '}';
    }
}