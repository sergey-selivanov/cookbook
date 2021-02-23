package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.sergeys.cookbook.logic.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResearchTest {

    static {
        // init logging
        SettingsManager.getInstance();
    }

    static final Logger log = LoggerFactory.getLogger(ResearchTest.class);

    class DurationSleeper {

        // https://stackoverflow.com/questions/31032636/how-to-change-the-duration-of-a-thread-sleep-already-sleeping

        private final Object monitor = new Object();
        private long durationMillis = 0;

        public DurationSleeper(long duration, TimeUnit timeUnit) {
            setDuration(duration, timeUnit);
        }

        public void sleep() {
            long millisSlept = 0;

            while (true) {
                synchronized (monitor) {
                    try {
                        long millisToSleep = durationMillis - millisSlept;
                        if (millisToSleep <= 0) {
                            return;
                        }
                        long sleepStartedInNanos = System.nanoTime(); // Not using System.currentTimeMillis - it depends on OS time, and may be changed at any moment (e.g. by daylight saving time)
                        log.debug("waiting for {} millis...", millisToSleep);
                        monitor.wait(millisToSleep);
                        millisSlept += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sleepStartedInNanos);
                        log.debug("millis slept: {}", millisSlept);
                    } catch (InterruptedException e) {
                        log.debug("interrupted");
                        throw new RuntimeException("Execution interrupted.", e);
                    }
                }
            }
        }

        public void setDuration(long newDuration, TimeUnit timeUnit) {
            synchronized (monitor) {

                this.durationMillis = timeUnit.toMillis(newDuration);
                log.debug("new duration: {} millis", durationMillis);
                monitor.notifyAll();
            }
        }
    }

    @Test
    void testProlongedWait() {

        DurationSleeper slp = new DurationSleeper(5, TimeUnit.SECONDS);

        Thread sleeper = new Thread(() -> {
            slp.sleep();
            log.debug("sleep thread is over");
        });

        try {
            Instant start = Instant.now();

            sleeper.start();

            Thread.sleep(100);
            slp.setDuration(7, TimeUnit.SECONDS);

            Thread.sleep(100);
            slp.setDuration(8, TimeUnit.SECONDS);

            sleeper.join();

            log.debug("duration: {}", Duration.between(start, Instant.now()));
        } catch (InterruptedException e) {
            log.debug("interrupted", e);
        }

        //fail("Not yet implemented");
    }


    @Test
    void testWinVersion() {
        String ver = "0.0.5-dev.0.11+20210113T101703Z";
        String fix = ver.replaceAll("-dev.*", ".10113");
        System.out.println(fix);

        ver = "0.0.6";
        fix = ver.replaceAll("-dev.*", ".10113");
        System.out.println(fix);


        System.out.println(System.getProperty("user.home"));

        Properties p = new Properties();
        p.put("test", "{{HOME_DIR}}");

        //System.out.println(p.getProperty("test"));

        p.forEach((k, v) -> {
            System.out.println(v);
            //p.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", System.getProperty("user.home")));
            //p.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", "ololo"));
            p.replace(k, v.toString().replace("{{HOME_DIR}}", System.getProperty("user.home")));
            System.out.println(p.getProperty((String) k));
        });
    }

}
