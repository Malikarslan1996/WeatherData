public class LamportClock {
    private int time = 0;


    public synchronized void tick() {
        time++;
    }

    public synchronized void update(int otherTime) {
        time = Math.max(time, otherTime) + 1;
    }

    public synchronized int getTime() {
        return time;
    }
}