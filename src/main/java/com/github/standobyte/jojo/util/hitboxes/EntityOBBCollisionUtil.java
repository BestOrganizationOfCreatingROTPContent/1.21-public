package com.github.standobyte.jojo.util.hitboxes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EntityOBBCollisionUtil {

    public static List<? extends Entity> getEntitiesInOBB(Level level, OrientedBoundingBox obb, Predicate<? super Entity> predicate){
        List<Entity> output = new ArrayList<>();
        for (Entity entity : getEntities(level)){
            if (predicate.test(entity)){
                AABB aabb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                if (obb.intersects(aabb) || obb.contains(entity.position().add(0, entity.getBbHeight() / 2F, 0))){
                    output.add(entity);
                }
            }
        }
        return output;
    }

    public static Iterable<Entity> getEntities(Level level){
        if (level.isClientSide()){
            return ((ClientLevel) level).entitiesForRendering();
        }
        else {
            return ((ServerLevel) level).getAllEntities();
        }
    }
}
