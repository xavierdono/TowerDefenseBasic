package com.jm3.tdb.state;

import com.jm3.tdb.control.CreepControl;
import com.jm3.tdb.control.TowerControl;
import com.jm3.tdb.domain.Factory;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.List;

public class GameScreenAppState extends AbstractAppState implements ScreenController {
    
    private SimpleApplication app;
    private Camera cam;
    private Node pickableNode;
    private Node rootNode;
    private Node guiNode;
    private Factory f;
    private AssetManager assetManager;
    private InputManager inputManager;
    private AppStateManager stateManager;
    private Node playerBaseNode;
    private Node towerNode;
    private Node creepNode;
    private Node beamNode;
    private String score;
    private int health;
    private int budget;
    private int level;
    private int numberOfCreeps;
    private int healthOfCreeps;
    private int timeBeforeAttack;
    private int numberOfTowerAvailable;
    private Geometry p;
    private float timer_beam;
    private BitmapText hudText;
    private BitmapText timeText;
    private Boolean isPickable;
    private Boolean swapView = true;
    private long currTime;
    private int updateTimeElapsed;
    private int systemTimeElapsed;
    private long prevUpdate;
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String mapping, boolean keyDown, float tpf) {
            if (mapping.equals("select") && !keyDown) {
                if (isPickable) {
                    addTower();
                }
            }
            
            if (mapping.equals("move") && !keyDown) {
                moveCamera();
            }
            
            if (mapping.equals("restart") && !keyDown) {
                reStartGame();
            }
            
            if (mapping.equals("quit") && !keyDown) {
                stopGame();
            }
        }
    };
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.guiNode = this.app.getGuiNode();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.stateManager = stateManager;
        
        this.playerBaseNode = new Node();
        this.towerNode = new Node();
        this.creepNode = new Node();
        this.pickableNode = new Node();
        this.beamNode = new Node();
        
        this.inputManager.clearMappings();
        this.inputManager.removeListener(actionListener);
        this.inputManager.addMapping("quit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        this.inputManager.addMapping("restart", new KeyTrigger(KeyInput.KEY_R));
        this.inputManager.addMapping("select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        this.inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        this.inputManager.addListener(actionListener, "select", "quit", "move", "restart");
        
        this.budget = 0;
        this.health = 1;
        this.numberOfTowerAvailable = 2;
        this.timeBeforeAttack = 15;
        this.healthOfCreeps = 40;
        this.numberOfCreeps = 10;
        this.updateTimeElapsed = 0;
        this.systemTimeElapsed = 0;
        this.prevUpdate = -1;
        this.level = 1;
        
        BitmapFont guiFont = this.assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText infoText = new BitmapText(guiFont, false);
        hudText = new BitmapText(guiFont, false);
        timeText = new BitmapText(guiFont, false);
        
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
        
        timeText.setSize(guiFont.getCharSet().getRenderedSize());
        timeText.setColor(ColorRGBA.Blue);
        timeText.setLocalTranslation(cam.getWidth() / 2, 50, 0);
        timeText.setText(String.format("Attack in %s s", this.timeBeforeAttack));
        guiNode.attachChild(timeText);
        
        f = new Factory(this.assetManager);
        f.setCreepHealth(getHealthOfCreeps());
        
        final Geometry floor = f.createFloor(Vector3f.ZERO);
        final Geometry authorizeZoneLeft = f.createAuthorizeZone(new Vector3f(-6f, 0, 0));
        final Geometry authorizeZoneRight = f.createAuthorizeZone(new Vector3f(6f, 0, 0));
        final Geometry path = f.createPath(new Vector3f(0, 0, -2.5f));
        final Geometry playerBase = f.createPlayerBase(new Vector3f(0, 0, -20));
        final Geometry line = f.createLine(Vector3f.ZERO);
        
        this.playerBaseNode.attachChild(floor);
        this.playerBaseNode.attachChild(line);
        this.playerBaseNode.attachChild(authorizeZoneLeft);
        this.playerBaseNode.attachChild(authorizeZoneRight);
        this.playerBaseNode.attachChild(path);
        this.playerBaseNode.attachChild(playerBase);
        
        for (int index = 0; index < getNumberOfCreeps(); index++) {
            final Geometry creep = f.createCreep(new Vector3f(randRange(-3, 3), 0, randRange(17, 30)));
            creep.addControl(new CreepControl(this));
            this.creepNode.attachChild(creep);
        }
        
        p = f.createTower(new Vector3f(0, 0, 0));
        this.pickableNode.attachChild(p);
        
        this.rootNode.attachChild(this.playerBaseNode);
        this.rootNode.attachChild(this.towerNode);
        this.rootNode.attachChild(this.beamNode);
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(this.assetManager,
                this.inputManager,
                this.app.getAudioRenderer(),
                this.app.getGuiViewPort());
        Nifty nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/gameUI.xml", "start");
        this.app.getGuiViewPort().addProcessor(niftyDisplay);        
    }
    
    private float randRange(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }
    
    @Override
    public void update(float tpf) {
        currTime = System.currentTimeMillis();
        
        if (prevUpdate != -1) {
            updateTimeElapsed += (int) (tpf * 1000f);
            systemTimeElapsed += (currTime - prevUpdate);
        }
        
        prevUpdate = currTime;
        
        timer_beam += tpf;
        
        if (timer_beam > 0.1f) {
            if (this.beamNode.getQuantity() > 0) {
                this.beamNode.detachAllChildren();
            }
            timer_beam = 0;
        }
        
        timeText.setText(String.format("Attack in %s s", this.timeBeforeAttack - (int) systemTimeElapsed / 1000));
        
        if (this.timeBeforeAttack - (int) systemTimeElapsed / 1000 < 0) {
            timeText.setText("Go Creeps!");
            this.rootNode.attachChild(this.creepNode);
        }
        
        score = String.format("Level %s, Budget: %d, Health: %d, Creeps: %d, Creeps health: %d", getLevel(), getBudget(), getHealth(), getCreeps().size(), getCreeps().isEmpty() ? 0 : getCreeps().get(0).getControl(CreepControl.class).getHealth());
        
        if (getHealth() <= 0) {
            // TODO : Afficher un ecran perdu
            hudText.setText(score + "  YOU LOOSE");
        } else if ((getCreeps().isEmpty()) && getHealth() > 0) {
            addLevel();
            
            this.rootNode.detachChild(this.creepNode);
            
            this.timeBeforeAttack = 5;
            this.prevUpdate = -1;
            this.updateTimeElapsed = 0;
            this.systemTimeElapsed = 0;
            
            for (int index = 0; index < getNumberOfCreeps(); index++) {
                final Geometry creep = f.createCreep(new Vector3f(randRange(-3, 3), 0, randRange(17, 30)));
                creep.addControl(new CreepControl(this));
                
                this.creepNode.attachChild(creep);
            }
        } else {
            hudText.setText(score);
        }
        
        if (this.numberOfTowerAvailable != 0) {
            CollisionResults results = new CollisionResults();
            Vector3f origin = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.0f);
            Vector3f direction = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.3f);
            direction.subtractLocal(origin).normalizeLocal();
            Ray ray = new Ray(origin, direction);
            
            this.app.getRootNode().collideWith(ray, results);
            
            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                if (closest.getGeometry().getName().equals("authorize")) {
                    isPickable = true;
                    
                    p.setLocalTranslation(closest.getContactPoint().x, closest.getContactPoint().y + 4, closest.getContactPoint().z);
                    this.rootNode.attachChild(this.pickableNode);
                    
                } else {
                    isPickable = false;
                    this.rootNode.detachChild(this.pickableNode);
                }
            } else {
                this.rootNode.detachChild(this.pickableNode);
            }
        }
    }
    
    @Override
    public void cleanup() {
        this.rootNode.detachAllChildren();
        this.guiNode.detachAllChildren();
        
        super.cleanup();
    }
    
    private int getHealth() {
        return health;
    }
    
    public void decreaseHealth() {
        this.health--;
    }
    
    private int getBudget() {
        return budget;
    }
    
    public void addBudget() {
        this.budget++;
    }
    
    public List<Spatial> getCreeps() {
        return this.creepNode.getChildren();
    }
    
    public void addBeam(Geometry g) {
        this.beamNode.attachChild(g);
    }
    
    public AssetManager getAssetManager() {
        return this.assetManager;
    }
    
    private void stopGame() {
        stateManager.detach(this);
        this.app.stop();
    }
    
    private void reStartGame() {
        this.stateManager.detach(this);
        this.stateManager.attach(this);
    }
    
    private void addTower() {
        if (this.numberOfTowerAvailable != 0) {
            CollisionResults results = new CollisionResults();
            Vector3f origin = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.0f);
            Vector3f direction = this.app.getCamera().getWorldCoordinates(this.app.getInputManager().getCursorPosition(), 0.3f);
            direction.subtractLocal(origin).normalizeLocal();
            Ray ray = new Ray(origin, direction);
            
            this.app.getRootNode().collideWith(ray, results);
            
            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                Geometry tower = f.createTower(closest.getContactPoint());
                tower.addControl(new TowerControl(this));
                this.towerNode.attachChild(tower);
                this.numberOfTowerAvailable--;
                this.rootNode.detachChild(this.pickableNode);
            }
        }
    }
    
    private int getLevel() {
        return level;
    }
    
    private void addLevel() {
        this.level++;
        addNumberOfCreeps();
        f.setCreepHealth(getHealthOfCreeps());
    }
    
    private void addNumberOfCreeps() {
        this.numberOfCreeps += 10;
        this.healthOfCreeps += 20;
    }
    
    private int getNumberOfCreeps() {
        return numberOfCreeps;
    }
    
    public int getHealthOfCreeps() {
        return healthOfCreeps;
    }
    
    private void moveCamera() {
        
        if (swapView) {
            // Vue de dessus
            cam.setLocation(new Vector3f(-15f, 80f, 0f));
            cam.lookAt(new Vector3f(-15f, 0f, 0f), Vector3f.UNIT_Y);
        } else {
            // View Normal
            cam.setLocation(new Vector3f(15f, 50f, 50f));
            cam.lookAt(new Vector3f(15f, 0f, 10f), Vector3f.UNIT_Y);
        }
        
        swapView = !swapView;
    }
    
    public void bind(Nifty nifty, Screen screen) {
    }
    
    public void onStartScreen() {
    }
    
    public void onEndScreen() {
    }
}