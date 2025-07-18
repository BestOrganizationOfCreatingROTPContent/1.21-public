package com.github.standobyte.jojo.client.entitycontrol.stand;

import com.github.standobyte.jojo.client.entitycontrol.ClientEntityController;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientStandController extends ClientEntityController {
	protected StandControlHud hud;
	
	public ClientStandController(LivingEntity entity) {
		super(entity);
		this.hud = new StandControlHud();
	}


	@Override
	public void onSet() {
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void onUnset() {
		NeoForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInputUpdate(MovementInputUpdateEvent event) {
		ClientInput input = event.getInput();
		moveStandManually(entityAsLiving, input.leftImpulse, input.forwardImpulse, 
				input.keyPresses.jump(), input.keyPresses.shift());
		// FIXME (1.16) (stand manual control) do not reset deltaMovement in manual control
		PacketDistributor.sendToServer(new ClStandManualMovementPacket(
				entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), prevTickInput));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearInput(MovementInputUpdateEvent event) { // prevents the player from sneaking on shift, and flying in creative on double space
		ClientInput input = event.getInput();
		input.keyPresses = Input.EMPTY;
		input.forwardImpulse = 0;
		input.leftImpulse = 0;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		manualMovementSpeed = Mth.clamp(manualMovementSpeed + 0.025f * (float) event.getScrollDeltaY(), 0, 1);
		hud.movementSpeedBarTranslucency.reset();
		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void cancelPlayerHandRender(RenderHandEvent event) {
		event.setCanceled(true);
	}

	@Override
	public boolean shouldRenderBlockOutline() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.mayBuild()) { // either adventure mode or spectator
			ItemStack heldItem = ((LivingEntity) entity).getMainHandItem();
			HitResult vanillaAim = mc.hitResult;
			if (vanillaAim != null && vanillaAim.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult) vanillaAim).getBlockPos();
				BlockState blockState = mc.level.getBlockState(blockPos);
				if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
					return blockState.getMenuProvider(mc.level, blockPos) != null;
				} else {
					BlockInWorld blockInWorld = new BlockInWorld(mc.level, blockPos, false);
					return !heldItem.isEmpty() && (heldItem.canBreakBlockInAdventureMode(blockInWorld) || heldItem.canPlaceOnBlockInAdventureMode(blockInWorld));
				}
			}
		}

		return true;
	}


	static float manualMovementSpeed = 1;
	private boolean prevTickInput = false;
	public void moveStandManually(LivingEntity standEntity, float strafe, float forward, boolean jumping, boolean sneaking) {
		boolean canStandMoveManually = true;
		if (canStandMoveManually) {
			boolean input = jumping || sneaking || forward != 0 || strafe != 0;
			if (input) {
				double speed = standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
				double y = jumping ? speed : 0;
				if (sneaking) {
					y -= speed;
					strafe *= 0.5;
					forward *= 0.5;
				}
				if (!prevTickInput) {
					standEntity.setDeltaMovement(Vec3.ZERO);
				}
				else {
					float actionWalkSpeed = 1;
					EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(standEntity);
					if (curAction != null) {
						actionWalkSpeed = curAction.userWalkSpeed;
					}
					Vec3 motion = getAbsoluteMotion(new Vec3((double)strafe, y, (double)forward), speed, standEntity.getYRot())
							.scale(actionWalkSpeed * manualMovementSpeed);
					standEntity.setDeltaMovement(motion);
				}
			}
			else if (prevTickInput) {
				standEntity.setDeltaMovement(Vec3.ZERO);
			}
			prevTickInput = input;
		}
	}

	private static Vec3 getAbsoluteMotion(Vec3 relative, double speed, float facingYRot) {
		double d0 = relative.lengthSqr();
		if (d0 < 1.0E-7D) {
			return Vec3.ZERO;
		} else {
			Vec3 vec3d = relative.normalize().scale(speed);
			float yRotSin = Mth.sin(facingYRot * ((float)Math.PI / 180F));
			float yRotCos = Mth.cos(facingYRot * ((float)Math.PI / 180F));
			return new Vec3(vec3d.x * (double)yRotCos - vec3d.z * (double)yRotSin, vec3d.y, vec3d.z * (double)yRotCos + vec3d.x * (double)yRotSin);
		}
	}


	@SubscribeEvent(priority = EventPriority.LOW)
	public void renderHudElements(RenderGuiLayerEvent.Pre event) {
		hud.renderHudElements(event, entityAsLiving);
	}
	
	@Override
	public void renderExtraHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		hud.renderExtraHud(guiGraphics, deltaTracker);
	}

}
