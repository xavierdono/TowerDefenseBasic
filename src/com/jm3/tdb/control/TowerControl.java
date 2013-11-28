package com.jm3.tdb.control;

import com.jm3.tdb.state.GameScreenAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Line;
import java.util.ArrayList;
import java.util.List;

public class TowerControl extends AbstractControl {

    private GameScreenAppState app;

    public TowerControl(GameScreenAppState app) {
        this.app = app;
    }

    @Override
    protected void controlUpdate(float tpf) {
        List<CreepControl> reachable = new ArrayList<CreepControl>();
        
        List<Spatial> creeps = this.app.getCreeps();
        
        for (Spatial creep_geo : creeps) {
            CreepControl creep = creep_geo.getControl(CreepControl.class);
            if (creep.isAlive()
                    && getTowerTop().distance(creep.getLoc()) <= FastMath.sqrt(74)) {
                reachable.add(creep);
            }
        }

        if (reachable.size() > 0) {
            Line beam = null;
            
            for (CreepControl creep : reachable) {
                Vector3f hit = creep.getLoc();
                
                beam = new Line(
                        getTowerTop(),
                        new Vector3f(
                        hit.x + FastMath.rand.nextFloat() / 10f,
                        hit.y + FastMath.rand.nextFloat() / 10f,
                        hit.z + FastMath.rand.nextFloat() / 10f));

                Geometry beam_geo = new Geometry("beam", beam);

                Material boxMat = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                boxMat.setColor("Color", ColorRGBA.Red);
                beam_geo.setMaterial(boxMat);
                this.app.addBeam(beam_geo);
                creep.decreaseCreepHealth();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Vector3f getTowerTop() {
        Vector3f loc = spatial.getLocalTranslation();
        return new Vector3f(loc.x, loc.y + 4, loc.z);
    }
}