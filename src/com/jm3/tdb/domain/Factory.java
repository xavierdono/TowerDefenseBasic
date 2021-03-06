package com.jm3.tdb.domain;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;

public class Factory {

    private int creepHealth;
    private AssetManager assetManager;

    public Factory(AssetManager as) {
        this.assetManager = as;
    }

    public Spatial createTower(Vector3f location, String index) {

        Spatial spatial = assetManager.loadModel("Models/Tree/Tree.mesh.j3o");
        spatial.setName(index);
        spatial.scale(2);
        spatial.setLocalTranslation(location.addLocal(0, spatial.getLocalTransform().getTranslation().getY(), 0));

        return spatial;
    }

    public Geometry createCreep(Vector3f location) {

        Box boxMesh = new Box(1f, 1f, 1f);
        Geometry boxCreep = new Geometry("creep", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Black);
        boxCreep.setMaterial(boxMat);
        boxCreep.setLocalTranslation(location.addLocal(0, boxMesh.getYExtent(), 0));
        boxCreep.setUserData("health", this.creepHealth);

        return boxCreep;
    }

    public void setCreepHealth(int creepHealth) {
        this.creepHealth = creepHealth;
    }

    public Geometry createPlayerBase(Vector3f location) {

        Box boxMesh = new Box(20f, 1f, 0f);
        Geometry boxBase = new Geometry("base", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Yellow);
        boxBase.setMaterial(boxMat);
        boxBase.setLocalTranslation(location.addLocal(0, boxMesh.getYExtent(), 0));

        return boxBase;
    }

    public Geometry createPath(Vector3f location) {

        Box boxMesh = new Box(4f, 0.1f, 17.5f);
        Geometry boxPath = new Geometry("path", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Brown);
        boxPath.setLocalTranslation(location);
        boxPath.setMaterial(boxMat);

        return boxPath;
    }

    public Geometry createFloor(Vector3f location) {

        Box boxMesh = new Box(20f, 0f, -30f);
        Geometry boxFloor = new Geometry("floor", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Orange);
        boxFloor.setMaterial(boxMat);

        return boxFloor;
    }

    public Geometry createLine(Vector3f location) {
        Line line = new Line(new Vector3f(-20f, 0f, 15), new Vector3f(20f, 0.1f, 15));
        Geometry lineGeo = new Geometry("beam", line);
        Material lineMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        lineMat.setColor("Color", ColorRGBA.Red);
        lineGeo.setMaterial(lineMat);

        return lineGeo;
    }

    public Geometry createAuthorizeZone(Vector3f location) {
        Box boxMesh = new Box(1f, 0.1f, 10f);
        Geometry boxFloor = new Geometry("authorize", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.White);
        boxFloor.setMaterial(boxMat);
        boxFloor.setLocalTranslation(location);

        return boxFloor;
    }
}