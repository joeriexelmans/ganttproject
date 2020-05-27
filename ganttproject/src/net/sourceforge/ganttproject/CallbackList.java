package net.sourceforge.ganttproject;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;

public class CallbackList {
    private final ArrayList<Runnable> callbacks = new ArrayList<>();

    public void add(Runnable r) {
        callbacks.add(r);
    }

    public void remove(Runnable r) {
        callbacks.remove(r);
    }

    public void runAll() {
        for (Runnable r : callbacks) {
            r.run();
        }
    }
}
