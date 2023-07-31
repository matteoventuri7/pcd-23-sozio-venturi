public class Cron {

    private boolean running;
    private long startTime;

    public Cron(){
        running = false;
    }

    public synchronized void start(){
        running = true;
        startTime = System.currentTimeMillis();
    }

    public synchronized void stop(){
        startTime = getTime();
        running = false;
    }

    public synchronized long getTime(){
        if (running){
            return 	System.currentTimeMillis() - startTime;
        } else {
            return startTime;
        }
    }
}