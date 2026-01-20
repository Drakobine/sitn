package sitn.smthinthenight;

public class PsychosisData {

    public static final float MAX = 100f;

    private static float value = 0f;
    private static boolean permanent = false;

    private static boolean inHomeClient = false;

    private static int calmTicks = 0;

    /* ================= SERVER ================= */

    public static void increase(float amount) {
        if (permanent) return;
        value = Math.min(MAX, value + amount);
        if (value >= MAX) {
            value = MAX;
            permanent = true;
        }
    }

    public static void decrease(float amount) {
        if (permanent) return;
        value = Math.max(0f, value - amount);
    }

    /* ================= CALM NIGHT ================= */

    public static void applyCalmNight() {
        calmTicks = 9600;
    }

    public static boolean hasCalmNight() {
        return calmTicks > 0;
    }

    public static void tickCalmNight() {
        if (calmTicks > 0) calmTicks--;
    }

    /* ================= GET ================= */

    public static float get() {
        return value;
    }

    public static boolean isMax() {
        return permanent;
    }

    public static boolean isInHome() {
        return inHomeClient;
    }

    /* ================= CLIENT ================= */

    public static void setClient(float v, boolean inHome) {
        value = v;
        inHomeClient = inHome;
    }

    /* ================= ITEMS ================= */

    public static void drinkTea() {
        value = 0f;
        permanent = false;
    }

    public static void overdose() {
        value = MAX;
        permanent = true;
    }

    /* ================= DETECTOR (CLIENT) ================= */

    private static boolean detectorEquippedClient = false;
    private static boolean detectorBootingClient = false;
    private static int detectorBootPercent = 0;

    public static void setDetectorEquippedClient(boolean value) {
        detectorEquippedClient = value;
    }

    public static boolean isDetectorEquippedClient() {
        return detectorEquippedClient;
    }

    public static void setDetectorBootingClient(boolean value) {
        detectorBootingClient = value;
        if (!value) detectorBootPercent = 100;
    }

    public static boolean isDetectorBootingClient() {
        return detectorBootingClient;
    }

    public static void setDetectorBootPercent(int percent) {
        detectorBootPercent = Math.max(0, Math.min(100, percent));
    }

    public static int getDetectorBootPercent() {
        return detectorBootPercent;
    }
}
