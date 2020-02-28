package io.helidon.data.examples;

public class OrderServiceCPUStress {
    boolean isStressOn = false;

    public void start() {
        for (int thread = 0; thread < 4; thread++) {
            new CPUStressThread().start();
        }
    }

    public void stop() {
        isStressOn = false;
    }

    private class CPUStressThread extends Thread {
        public void run() {
            try {
                while (isStressOn) {
                    if (System.currentTimeMillis() % 100 == 0) {
                        Thread.sleep((long) Math.floor((.2) * 100));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
