package net.sourceforge.ganttproject;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;

public class ObservableImpl implements Observable {
    private final ArrayList<InvalidationListener> myListeners = new ArrayList<>();

    @Override
    public void addListener(InvalidationListener l) {
        myListeners.add(l);
    }

    @Override
    public void removeListener(InvalidationListener l) {
        myListeners.remove(l);
    }

    protected void notifyListeners() {
        for (InvalidationListener l : myListeners) {
            l.invalidated(this);
        }
    }

//    protected void notifyListeners(Observable o) {
//        for (InvalidationListener l: myListeners) {
//            l.invalidated(o);
//        }
//    }
}
