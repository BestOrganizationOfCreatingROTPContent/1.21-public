package com.github.standobyte.jojo.mechanics.entity_like_player.playerwrapper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface EntityAsPlayerWrapper {
	Entity getEntity();
	default Player asPlayer() { return (Player) this; }
}
