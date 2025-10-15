package com.github.standobyte.jojo.mechanics.entitycontrol.client.mob;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.hud.VanillaHudSprites;
import com.github.standobyte.jojo.mechanics.entitycontrol.client.ClientEntityController;
import com.github.standobyte.jojo.mechanics.entitycontrol.client.stand.StandHudElements;
import com.github.standobyte.jojo.mechanics.entitycontrol.client.stand.StandHudElements.HealthHudTracker;
import com.github.standobyte.jojo.mechanics.entitycontrol.tmp.ClQuitControllerPacket;
import com.github.standobyte.jojo.mixin.entitycontrol.client.GuiAccessor;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientMobController extends ClientEntityController {

	public ClientMobController(Entity entity) {
		super(entity);
	}


	@Override
	public void onSet() {
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void onUnset() {
		PacketDistributor.sendToServer(ClQuitControllerPacket.packet());
		NeoForge.EVENT_BUS.unregister(this);
	}

	protected boolean prevShift = true;
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInputUpdate(MovementInputUpdateEvent event) {
		Input clientInput = event.getInput();
		if (!prevShift && clientInput.shiftKeyDown) {
			setInstance(null);
			return;
		}
		prevShift = clientInput.shiftKeyDown;
		
		Mob controlledMob = MobControlUtil.getMobOrMobVehicle(entityAsLiving);
		
		// LookControl tick is being cancelled in com.github.standobyte.jojo.mixin.entitycontrol.mob.MobAILookMixin
		
		MoveControl moveControl = controlledMob.getMoveControl();
		moveControl.strafe(clientInput.forwardImpulse, clientInput.leftImpulse);
		
		if (clientInput.jumping) {
			JumpControl jumpControl = controlledMob.getJumpControl();
			jumpControl.jump();
		}
        
		UtilFunctions.wrapYRotationAngles(entityAsLiving);
	}

	@Override
	public void tick() {
		PacketDistributor.sendToServer(new ClMobControlMovementPacket(entity.getId(), 
				entity.getX(), entity.getY(), entity.getZ(), 
				entity.getXRot(), entity.getYRot(), entity.onGround()));
	}
	
	@Override
	public boolean turn(double yRot, double xRot) {
		super.turn(yRot, xRot);
		Mob vehicle = MobControlUtil.getMobOrMobVehicle(entityAsLiving);
		if (vehicle != this.entity) {
			vehicle.setYRot(this.entity.getYRot());
		}
		return true;
	}

	@Override
	public boolean isBeingControlled(Entity entity) {
		return super.isBeingControlled(entity) || MobControlUtil.getMobOrMobVehicle(this.entity) == entity;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearInput(MovementInputUpdateEvent event) {
		Input clientInput = event.getInput();
		clearInput(clientInput);
	}



	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void removeHudElements(RenderGuiLayerEvent.Pre event) {
		ResourceLocation layerName = event.getName();
		if (layerName.equals(VanillaGuiLayers.EXPERIENCE_BAR) || layerName.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
			event.setCanceled(true);
		}
	}
	
	protected HealthHudTracker healthHudTracker = new HealthHudTracker();
	@SubscribeEvent(priority = EventPriority.LOW)
	public void renderHudElements(RenderGuiLayerEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
//		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
			StandHudElements.renderHealth(entityAsLiving, guiGraphics, gui, mc, healthHudTracker);
		}
		if (layerName.equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
			renderVehicleHealth(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			StandHudElements.renderArmor(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.AIR_LEVEL)) {
			StandHudElements.renderAir(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.EFFECTS)) {
			StandHudElements.renderStatusEffects(entityAsLiving, guiGraphics, gui, mc, false);
		}
//		else if (layerName.equals(VanillaGuiLayers.HOTBAR)) { // XXX witch potions hotbar
//			int center = guiGraphics.guiWidth() / 2;
//			int xLeft = center;
//			int xRight = center;
//			renderStandHeldItems(stand, guiGraphics, gui, deltaTracker, mc, xLeft, xRight, true);
//		}
//		else if (layerName.equals(VanillaGuiLayers.CROSSHAIR)) {
//			
//		}
	}

	public static void renderVehicleHealth(LivingEntity entity, GuiGraphics guiGraphics, GuiAccessor gui, Minecraft mc) {
		LivingEntity vehicle = getVehicleWithHealth(entity);
		if (vehicle != null) {
			int maxHealth = (int)(vehicle.getMaxHealth() + 0.5F) / 2;
			if (maxHealth > 30) {
				maxHealth = 30;
			}

			if (maxHealth != 0) {
				int j = (int)Math.ceil((double)vehicle.getHealth());
				mc.getProfiler().popPush("mountHealth");
				int k = guiGraphics.guiHeight() - mc.gui.rightHeight;
				int l = guiGraphics.guiWidth() / 2 + 91;
				int i1 = k;
				int j1 = 0;
				RenderSystem.enableBlend();

				while (maxHealth > 0) {
					int k1 = Math.min(maxHealth, 10);
					maxHealth -= k1;

					for (int l1 = 0; l1 < k1; l1++) {
						int i2 = l - l1 * 8 - 9;
						guiGraphics.blitSprite(VanillaHudSprites.HEART_VEHICLE_CONTAINER_SPRITE, i2, i1, 9, 9);
						if (l1 * 2 + 1 + j1 < j) {
							guiGraphics.blitSprite(VanillaHudSprites.HEART_VEHICLE_FULL_SPRITE, i2, i1, 9, 9);
						}

						if (l1 * 2 + 1 + j1 == j) {
							guiGraphics.blitSprite(VanillaHudSprites.HEART_VEHICLE_HALF_SPRITE, i2, i1, 9, 9);
						}
					}

					i1 -= 10;
					mc.gui.rightHeight += 10;
					j1 += 20;
				}

				RenderSystem.disableBlend();
			}
		}
	}

	@Nullable
	public static LivingEntity getVehicleWithHealth(LivingEntity entity) {
		return entity.getVehicle() instanceof LivingEntity vehicle && vehicle.showVehicleHealth() ? vehicle : null;
	}

}
