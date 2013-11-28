package com.jm3.tdb.control;

import com.jm3.tdb.state.GameScreenAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CreepControl extends AbstractControl {

    private final float speed_min = 10f;
    private GameScreenAppState app;

    public CreepControl(GameScreenAppState app) {
        this.app = app;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (isAlive()) {
            Vector3f newloc = new Vector3f(
                    spatial.getLocalTranslation().getX(),
                    spatial.getLocalTranslation().getY(),
                    spatial.getLocalTranslation().getZ()
                    - (speed_min * tpf * FastMath.rand.nextFloat()));

            if (newloc.z > -19f) {
                spatial.setLocalTranslation(newloc);
            } else {
                this.app.decreaseHealth();

                spatial.removeFromParent();
                spatial.removeControl(this);
            }
        } else {
            this.app.addBudget();

            if (getHealth() == 0) {
                spatial.removeFromParent();
                spatial.removeControl(this);
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Boolean isAlive() {
        return getHealth() > 0f;
    }

    public Vector3f getLoc() {
        Vector3f loc = spatial.getLocalTranslation();
        return new Vector3f(loc.x, loc.y + 1, loc.z);
    }
    
    public void decreaseCreepHealth() {
        spatial.setUserData("health", getHealth() - 1);
    }

    public int getHealth() {
        return Integer.valueOf(spatial.getUserData("health").toString());
    }
}