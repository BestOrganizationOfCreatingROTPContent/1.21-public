package com.github.standobyte.jojo.util.hitboxes;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExtendableOBB {

    public ExtendableOBB(OrientedBoundingBox obb, float movementSpeed, int lifeSpan, int timeAtFullLength, Vec3 offset){
        this.obb = obb.updateVertex();
        this.movementSpeed = movementSpeed;
        this.maxLifeSpan = lifeSpan;
        this.lifeSpan = lifeSpan;
        this.timeAtFullLength = timeAtFullLength;
        this.offset = offset;
    }

    private OrientedBoundingBox obb;

    protected boolean isMovingForward = true;
    protected boolean isRetracting;
    protected int lifeSpan;
    protected int maxLifeSpan;
    private float length;
    private float movementSpeed;
    private final int timeAtFullLength;
    private int tickCount;
    private double maxDistance;
    private Vec3 offset;


    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public OrientedBoundingBox rotatableHitbox(){
        return obb;
    }

    public void updateOBB(){
        this.obb = obb.updateVertex();
    }

    protected float getMovementSpeed(){
        return movementSpeed;
    }

    protected int timeAtFullLength() {
        return timeAtFullLength;
    }

    public float getLength(){
        return length;
    }

    protected float retractSpeed() {
        return getMovementSpeed();
    }

    public void setIsMovingForward(boolean isMovingForward) {
        this.isMovingForward = isMovingForward;
    }

    public boolean isMovingForward() {
        return isMovingForward;
    }

    public void setIsRetracting(boolean isRetracting) {
        this.isRetracting = isRetracting;
    }

    public boolean isRetracting() {
        return isRetracting;
    }

    public boolean isRetracted(){
        return isRetracting() && getLength() <= 0;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public int ticksLifespan() {
        return lifeSpan;
    }

    protected void updateMotionFlags() {
        if (isMovingForward() && lifeSpan <= (timeAtFullLength())) {
            setIsMovingForward(false);
        }
        if (!isRetracting() && lifeSpan <= 0) {
            setIsRetracting(true);
        }
    }

    protected void updateHitboxExtension(){
        updateMotionFlags();
        if (isMovingForward()){
            obb.extent = obb.extent.add(0, 0, getMovementSpeed());
            length += getMovementSpeed()*5.5F;
        }
        else if (isRetracting()){
            obb.extent = obb.extent.add(0, 0, -retractSpeed());
            length -= retractSpeed()*5.5F;
        }
    }

    public void tick() {
        if (isRetracted() || lifeSpan <= 0) return;
        tickCount ++;
        updateHitboxExtension();
        updateOBB();
        lifeSpan --;
    }

    public void updatePosition(Level level, Vec3 pos, Vec3 offset, float xRot, float yRot){
        if (!level.isClientSide()){
            obb.center = pos.add(offset);
        }
        else {
            obb.center = offset;
        }
        obb = rotatableHitbox().setRotation(yRot, xRot).updateVertex();
    }
}
