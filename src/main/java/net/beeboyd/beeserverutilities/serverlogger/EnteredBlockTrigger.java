package net.beeboyd.beeserverutilities.serverlogger;

public class EnteredBlockTrigger {
    public int x;
    public int y;
    public int z;
    public String detailName;

    // No-args constructor for Gson
    public EnteredBlockTrigger() {}

    public EnteredBlockTrigger(int x, int y, int z, String detailName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.detailName = detailName;
    }

    /**
     * Returns true if the given coordinates match this trigger.
     */
    public boolean matches(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public String toString() {
        return detailName + " (" + x + ", " + y + ", " + z + ")";
    }
}
