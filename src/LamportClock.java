public class LamportClock {
    private int time = 0;

    // Increment the clock value
    public synchronized void tick() {
        time++;
    }

    // When receiving a message, update the clock value
    public synchronized void update(int otherTime) {
        time = Math.max(time, otherTime) + 1;
    }

    public synchronized int getTime() {
        return time;
    }
}