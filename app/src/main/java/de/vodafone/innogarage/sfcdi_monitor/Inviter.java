package de.vodafone.innogarage.sfcdi_monitor;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Innovation on 09.03.2017.
 */
// TODO: check if this works
public class Inviter extends Thread {
    public void run(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                new Broadcaster().start();
            }
        };
        timer.schedule(timerTask, 0, 25000);
    }
}
