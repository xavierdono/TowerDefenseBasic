package com.jm3.tdb.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldScreenAppState extends AbstractAppState {

    private SimpleApplication app;
    private InputManager inputManager;
    private AppStateManager stateManager;
    private GameScreenAppState gameScreen;
    private StartScreenAppState startScreen;
    public static final Logger logger = Logger.getLogger("");
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String mapping, boolean keyDown, float tpf) {
            if (stateManager.hasState(gameScreen)) {
                if (mapping.equals("select") && !keyDown) {

                    enhance();

                    logger.log(Level.WARNING, "select");
                }
            } else {
                if (mapping.equals("start") && !keyDown) {
                    startGame();
                }
            }

            if (mapping.equals("quit") && !keyDown) {

                stopGame();
            }
        }
    };

    private void enhance() {
        CollisionResults results = new CollisionResults();
        Vector3f origin = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.0f);
        Vector3f direction = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();
        Ray ray = new Ray(origin, direction);

        this.app.getRootNode().collideWith(ray, results);

        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            if (closest.getGeometry().getName().equals("tower")) {
                Material boxMat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                boxMat.setColor("Color", ColorRGBA.Green);

                AmbientLight ambient = new AmbientLight();
                ambient.setColor(new ColorRGBA(1, 1, 1, 1));
                closest.getGeometry().addLight(ambient);

                closest.getGeometry().setMaterial(boxMat);
            }
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        logger.setLevel(Level.WARNING);

        this.app = (SimpleApplication) app;
        this.inputManager = this.app.getInputManager();
        this.stateManager = stateManager;

        this.inputManager.addMapping("start", new KeyTrigger(KeyInput.KEY_RETURN));
        this.inputManager.addMapping("quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        this.inputManager.addMapping("select", new MouseButtonTrigger(0));
        this.inputManager.addListener(actionListener, "start", "quit", "select");

        gameScreen = new GameScreenAppState(this);

        startScreen = new StartScreenAppState(this);
        stateManager.attach(startScreen);
    }

    public void attachStartScreen() {
        stateManager.attach(startScreen);
    }

    public void attachGameScreen() {
        stateManager.attach(gameScreen);
    }

    public void detachStartScreen() {
        stateManager.detach(startScreen);
    }

    public void detachGameScreen() {
        stateManager.detach(gameScreen);
    }

    private void startGame() {
        detachStartScreen();
        attachGameScreen();
    }

    private void stopGame() {
        detachGameScreen();
        detachStartScreen();

        this.app.stop();
    }
}