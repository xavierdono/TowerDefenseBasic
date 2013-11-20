package com.jm3.tdb.state;

import com.jm3.tdb.control.CreepControl;
import com.jm3.tdb.control.TowerControl;
import com.jm3.tdb.domain.Circle3d;
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
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
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

        createFloor();
        createPath();

        createPlayerBase(new Vector3f(0, 0, -20));

        createTower(new Vector3f(5, 0, 0));
        createTower(new Vector3f(-5, 0, 5));

        createCreep(new Vector3f(1, 0, 20));
        createCreep(new Vector3f(3, 0, 25));
        createCreep(new Vector3f(0, 0, 30));
        createCreep(new Vector3f(2, 0, 16));

        this.rootNode.attachChild(playerBaseNode);
        this.rootNode.attachChild(towerNode);
        this.rootNode.attachChild(creepNode);
        this.rootNode.attachChild(beamNode);
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

//        CollisionResults results = new CollisionResults();
//        Vector3f origin = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.0f);
//        Vector3f direction = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.3f);
//        direction.subtractLocal(origin).normalizeLocal();
//        Ray ray = new Ray(origin, direction);
//
//        this.rootNode.collideWith(ray, results);
//
//        if (results.size() > 0) {
//            CollisionResult closest = results.getClosestCollision();
//            logger.log(Level.WARNING, closest.getGeometry().getName());
//        }
    }

    @Override
    public void cleanup() {
        this.rootNode.detachAllChildren();
        this.guiNode.detachAllChildren();

        super.cleanup();
    }

    private void createFloor() {

        Box boxMesh = new Box(30f, 0f, -30f);
        Geometry boxFloor = new Geometry("floor", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Orange);
        boxFloor.setMaterial(boxMat);

        this.rootNode.attachChild(boxFloor);

        // Line
        Line line = new Line(new Vector3f(-30f, 0f, 15), new Vector3f(30f, 0.1f, 15));
        Geometry lineGeo = new Geometry("beam", line);
        Material lineMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMat.setColor("Color", ColorRGBA.Red);
        lineGeo.setMaterial(lineMat);
        this.rootNode.attachChild(lineGeo);
    }

    private void createPath() {

        Box boxMesh = new Box(4f, 0.1f, 17.5f);
        Geometry boxPath = new Geometry("path", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Brown);
        boxPath.setLocalTranslation(0, 0, -2.5f);
        boxPath.setMaterial(boxMat);

        this.rootNode.attachChild(boxPath);

    }

    private void createPlayerBase(Vector3f location) {

        Box boxMesh = new Box(30f, 1f, 0f);
        Geometry boxBase = new Geometry("base", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Yellow);
        boxBase.setMaterial(boxMat);
        boxBase.setLocalTranslation(location.addLocal(0, boxMesh.getYExtent(), 0));
        this.playerBaseNode.attachChild(boxBase);

    }

    private void createTower(Vector3f location) {

        Box boxMesh = new Box(1f, 4f, 1f);
        Geometry boxTower = new Geometry("tower", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Green);
        boxTower.setMaterial(boxMat);
        boxTower.setLocalTranslation(location.addLocal(0, boxMesh.getYExtent(), 0));
        boxTower.setUserData("index", 0);
        boxTower.addControl(new TowerControl(this));

        // Perimetre
        Circle3d circle = new Circle3d(Vector3f.ZERO, 5f, 32);

        Geometry geom = new Geometry("circle", circle);
        geom.updateModelBound();

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        geom.setMaterial(mat);
        geom.setLocalTranslation(location.addLocal(0, -boxMesh.getYExtent() + 0.1f, 0));
        rootNode.attachChild(geom);

        this.towerNode.attachChild(boxTower);

    }

    private void createCreep(Vector3f location) {

        Box boxMesh = new Box(1f, 1f, 1f);
        Geometry boxCreep = new Geometry("creep", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Black);
        boxCreep.setMaterial(boxMat);
        boxCreep.setLocalTranslation(location.addLocal(0, boxMesh.getYExtent(), 0));
        boxCreep.setUserData("index", 0);
        boxCreep.setUserData("health", Integer.valueOf(50));
        boxCreep.addControl(new CreepControl(this));
        this.creepNode.attachChild(boxCreep);

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