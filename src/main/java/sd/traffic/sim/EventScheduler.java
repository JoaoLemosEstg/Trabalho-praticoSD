package sd.traffic.sim;

import java.util.PriorityQueue;

public class EventScheduler {

    private long clock = 0;
    private final PriorityQueue<Event> agenda = new PriorityQueue<>();
    private volatile boolean running = true;

    public synchronized void schedule(Event e) {
        agenda.add(e);
    }

    public void stop() { running = false; }

    public void runLoop(EventProcessor processor) {
        while (running && !agenda.isEmpty()) {
            Event e;
            synchronized (this) {
                e = agenda.poll();
            }
            if (e == null) break;
            clock = e.getTime();
            processor.process(e, clock);
        }
    }

    public interface EventProcessor {
        void process(Event e, long currentTime);
    }
}
