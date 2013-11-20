package com.jm3.tdb.state;

import com.jm3.tdb.control.CreepControl;
import com.jm3.tdb.control.TowerControl;
import com.jm3.tdb.domain.Factory;
import static com.jm3.tdb.state.WorldScreenAppState.logger;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.logging.Level;

public class GameScreenAppState extends AbstractAppState {

    private WorldScreenAppState worldScreenAppState;
    private SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private Node guiNode;
    private AssetManager assetManager;
    private Node playerBaseNode;
    private Node towerNode;
    private Node creepNode;
    private Node beamNode;
    private String score;
    private int health;
    private int budget;
    private float timer_beam;
    private BitmapText hudText;

    public GameScreenAppState(WorldScreenAppState worldScreenAppState) {
        this.worldScreenAppState = worldScreenAppState;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        logger.setLevel(Level.WARNING);

        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.guiNode = this.app.getGuiNode();
        this.assetManager = this.app.getAssetManager();

        this.playerBaseNode = new Node();
        this.towerNode = new Node();
        this.creepNode = new Node();
        this.beamNode = new Node();

        this.budget = 0;
        this.health = 1;

        BitmapFont guiFont = this.assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText infoText = new BitmapText(guiFont, false);
        hudText = new BitmapText(guiFont, false);

        int screenHeight = cam.getHeight();
        float lineHeight = infoText.getLineHeight();

        infoText.setSize(guiFont.getCharSet().getRenderedSize());
        infoText.setColor(ColorRGBA.Blue);
        infoText.setLocalTranslation(0, screenHeight, 0);
        infoText.setText("  Tower Defense Basic");
        guiNode.attachChild(infoText);

        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.Blue);
        hudText.setLocalTranslation(0, screenHeight - lineHeight, 0);
        hudText.setText("");
        guiNode.attachChild(hudText);

        Factory f = new Factory(this.assetManager);

        final Geometry floor = f.createFloor(Vector3f.ZERO);
        final Geometry authorizeZoneLeft = f.createAuthorizeZone(new Vector3f(-6f, 0, 0));
        final Geometry authorizeZoneRight = f.createAuthorizeZone(new Vector3f(6f, 0, 0));
        final Geometry path = f.createPath(new Vector3f(0, 0, -2.5f));
        final Geometry playerBase = f.createPlayerBase(new Vector3f(0, 0, -20));

        this.playerBaseNode.attachChild(floor);
        this.playerBaseNode.attachChild(authorizeZoneLeft);
        this.playerBaseNode.attachChild(authorizeZoneRight);
        this.playerBaseNode.attachChild(path);
        this.playerBaseNode.attachChild(playerBase);

        final Geometry towerOne = f.createTower(new Vector3f(5, 0, 0));
        towerOne.addControl(new TowerControl(this));
        final Geometry towerTwo = f.createTower(new Vector3f(-5, 0, 5));
        towerTwo.addControl(new TowerControl(this));

        this.towerNode.attachChild(towerOne);
        this.towerNode.attachChild(towerTwo);

        for (int index = 0; index < 10; index++) {
            final Geometry creep = f.createCreep(new Vector3f(randRange(-3, 3), 0, randRange(17, 30)));
            creep.addControl(new CreepControl(this));
            this.creepNode.attachChild(creep);
        }

        this.rootNode.attachChild(this.playerBaseNode);
        this.rootNode.attachChild(this.towerNode);
        this.rootNode.attachChild(this.creepNode);
        this.rootNode.attachChild(this.beamNode);
    }

    private float randRange(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    @Override
    public void update(float tpf) {

        timer_beam += tpf;

        if (timer_beam > 0.1f) {
            if (this.beamNode.getQuantity() > 0) {
                this.beamNode.detachAllChildren();
            }
            timer_beam = 0;
        }

        score = String.format("Budget: %d, Health: %d, Creeps: %d [%d]", getBudget(), getHealth(), getCreeps().size(), (getCreeps().size() > 0) ? getCreeps().get(0).getControl(CreepControl.class).getHealth() : 0);

        if (getHealth() <= 0) {
            this.worldScreenAppState.detachGameScreen();
            this.worldScreenAppState.attachStartScreen();
        } else if ((getCreeps().isEmpty()) && getHealth() > 0) {
            hudText.setText(score + "      YOU WIN!");
        } else {
            hudText.setText(score + "      GO! GO! GO!");
        }

        CollisionResults results = new CollisionResults();
        Vector3f origin = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.0f);
        Vector3f direction = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();
        Ray ray = new Ray(origin, direction);

        this.rootNode.collideWith(ray, results);

        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();

            if (closest.getGeometry().getName().equals("authorize")) {
                Material boxMat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                boxMat.setColor("Color", ColorRGBA.Blue);
                closest.getGeometry().setMaterial(boxMat);
            }
        }
    }

    @Override
    public void cleanup() {
        this.rootNode.detachAllChildren();
        this.guiNode.detachAllChildren();

        super.cleanup();
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth() {
        this.health--;
    }

    public int getBudget() {
        return budget;
    }

    public void addBudget() {
        this.budget++;
    }

    public List<Spatial> getCreeps() {
        return this.creepNode.getChildren();
    }

    public List<Spatial> getTowers() {
        return this.towerNode.getChildren();
    }

    public void addBeam(Geometry g) {
        this.beamNode.attachChild(g);
    }

    public AssetManager getAssetManager() {
        return this.assetManager;
    }
}