package com.jm3.tdb.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class StartScreenAppState extends AbstractAppState {

    private WorldScreenAppState worldScreenAppState;
    private SimpleApplication app;
    private Node guiNode;

    public StartScreenAppState(WorldScreenAppState worldScreenAppState) {
        this.worldScreenAppState = worldScreenAppState;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        Camera cam = this.app.getCamera();

        BitmapFont guiFont = this.app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        BitmapText startText = new BitmapText(guiFont, false);
        startText.setSize(guiFont.getCharSet().getRenderedSize());
        startText.setColor(ColorRGBA.Blue);
        startText.setText("Press ENTER to start playing");
        startText.setLocalTranslation(cam.getWidth() / 2 - startText.getLineWidth() / 2, cam.getHeight() / 2 + startText.getLineHeight() / 2, 0);
        startText.attachChild(startText);

        this.guiNode.attachChild(startText);
    }

    @Override
    public void update(float tpf) {
    }

    @Override
    public void cleanup() {
        this.guiNode.detachAllChildren();
        this.worldScreenAppState.detachStartScreen();
        
        super.cleanup();
    }
}