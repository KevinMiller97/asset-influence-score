package com.millerk97.ais.controller;

public abstract class ThreadWithOnFinished extends Thread {

    private Runnable onFinished;

    @Override
    public final void run() {
        try {
            doRun();
        } finally {
            if (onFinished != null)
                onFinished.run();
        }
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public abstract void doRun();
}