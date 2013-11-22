package com.jm3.tdb;

import com.jm3.tdb.state.StartScreenAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {

    public static void main(String[] args) {

        AppSettings s = new AppSettings(true);
        s.setTitle("  Tower Defense Basic");
        s.setResolution(1024, 768);

        Main app = new Main();
        app.setSettings(s);
        app.setShowSettings(false);
        app.start();

    }

    @Override
    public void simpleInitApp() {

        setDisplayStatView(false);
        setDisplayFps(false);

        flyCam.setMoveSpeed(100);
        flyCam.setEnabled(false);
        flyCam.setDragToRotate(false);
        inputManager.setCursorVisible(true);

        cam.setLocation(new Vector3f(0f, 50f, 50f));
        cam.lookAt(new Vector3f(0, 0, 8f), Vector3f.UNIT_Y);

        StartScreenAppState startScreenAppState = new StartScreenAppState();
        stateManager.attach(startScreenAppState);
    }
}