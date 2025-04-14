package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.packet.fromserver.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.core.packet.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.network.PacketDistributor2;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandEntity extends LivingEntity implements SummonedStand, IEntityWithComplexSpawn {
	private ResourceLocation standId;
	private static final EntityDataAccessor<Integer> USER_ID = SynchedEntityData.defineId(StandEntity.class, EntityDataSerializers.INT);
	private WeakReference<LivingEntity> userRef = new WeakReference<LivingEntity>(null);
	private StandPower userPower;
	private EntityActionInstance curAction;

	public StandEntity(EntityType<? extends StandEntity> type, Level level) {
		super(type, level);
	}
	
	public StandEntity withStandId(ResourceLocation standId) {
		if (isAddedToLevel()) throw new IllegalStateException();
		this.standId = standId;
		return this;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(USER_ID, -1);
	}
	
	
	@Override
	public void tick() {
		fallDistance = 0;
		super.tick();
		LivingEntity user = getUser();
		if (user != null) {
			updatePosition(user);
		}
		if (curAction != null && userPower != null) {
			if (tickAction(curAction)) {
				setStandAction(null, false);
			}
		}
		
		// TODO stand entity sounds
		// this below was just me testing sounds in stand skins
//		if (level().isClientSide() && tickCount == 2) {
//			Minecraft mc = Minecraft.getInstance();
//			
//			double x = this.getX() + 0.5;
//			double y = this.getY() + 0.5;
//			double z = this.getZ() + 0.5;
//			SoundEvent soundEvent = ModSoundEvents.STAND_SUMMON.get();
//			SoundSource source = this.getSoundSource();
//			float volume = 1.0F;
//			float pitch = 1;
//			boolean distanceDelay = false;
//			long seed = this.random.nextLong();
//			
//			
//			double d0 = mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(x, y, z);
//			SimpleSoundInstance sound = new SimpleSoundInstance(
//				soundEvent, source, volume, pitch, RandomSource.create(seed), x, y, z
//			);
//			((SoundInstanceWithStandSkin) sound).setStandSkin(standId, standSkin);
//			if (distanceDelay && d0 > 100.0) {
//				double d1 = Math.sqrt(d0) / 40.0;
//				mc.getSoundManager().playDelayed(sound, (int)(d1 * 20.0));
//			} else {
//				mc.getSoundManager().play(sound);
//			}
//		}
	
	}
	
	public void updatePosition(LivingEntity user) {
		if (user == null) return;
		
		Vec3 relativeOffset = new Vec3(-0.75, 0.2, -0.75);
		Vec3 offset = relativeOffset.yRot(-user.yBodyRot * MathUtil.DEG_TO_RAD);
		Vec3 pos = user.position().add(offset);
		setPos(pos.x, pos.y, pos.z);
		
		this.setYRot(user.getYRot());
		this.setXRot(user.getXRot());
		this.yRotO = user.yRotO;
		this.yBodyRot = user.yBodyRot;
		this.yBodyRotO = user.yBodyRotO;
		this.yHeadRot = user.yHeadRot;
		this.yHeadRotO = user.yHeadRotO;
	}
	
	public boolean isFollowingUser() {
		return true;
	}
	
	// TODO make the stand entity not push the user on summon (this isn't enough for whatever f-ing reason)
	@Override
	public void push(Entity entity) {}
	
	@Override
	public boolean isPushable() { return false; }
	
	
	public ResourceLocation getStandId() {
		return standId;
	}
	
	@Override
	public void setUserAndPower(LivingEntity user, StandPower power) {
		if (!level().isClientSide()) {
			entityData.set(USER_ID, user.getId());
		}
		this.userPower = power;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
		super.onSyncedDataUpdated(dataParameter);
		if (USER_ID.equals(dataParameter)) {
			updateUserFromNetwork(entityData.get(USER_ID));
		}
	}
	
	@Override
	public void tickStand(LivingEntity user, StandPower userStand) {
		if (!user.level().isClientSide() && this.isRemoved()) {
			userStand.setSummonedStand(null);
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrSetStandEntityPacket(user.getId(), 0));
		}
	}
	
	/**
	 * Careful - the user's entity might not always be loaded on client in case of long-ranged Stands.
	 */
	@Nullable
	public LivingEntity getUser() {
		if (hasUser()) {
			return userRef == null ? null : userRef.get();
		}
		return null;
	}

	@Nullable
	public StandPower getUserPower() {
		if (userPower == null && hasUser()) {
			LivingEntity user = getUser();
			if (user != null) {
				userPower = StandPower.get(user);
			}
		}
		return userPower;
	}

	protected final boolean hasUser() {
		return entityData.get(USER_ID) >= 0;
	}
	
	// XXX left-side stand pos config
	private void updateUserFromNetwork(int userId) {
		userRef = lookupUser(userId);
		LivingEntity user = getUser();
		if (user != null) {
//			if (user instanceof Player) {
//				playerSettings = PlayerClientBroadcastedSettings.getPlayerSettings((Player) user);
//			}
			if (level().isClientSide()) {
				StandPower standPower = StandPower.get(user);
				if (standPower != null && standPower.getSummonedStand() != this) {
					standPower.setSummonedStand(this);
				}
			}
		}
	}

	@Nullable
	private WeakReference<LivingEntity> lookupUser(int userId) {
		Entity user = level().getEntity(userId);
		if (user instanceof LivingEntity) {
			return new WeakReference<LivingEntity>((LivingEntity) user);
		}
		return null;
	}
	
	
	@Nullable
	public EntityActionInstance getStandAction() {
		return curAction;
	}
	
	public void setStandAction(@Nullable EntityActionInstance action, boolean sync) {
		if (this.curAction != null) {
			this.curAction.onActionCleared();
		}
		this.curAction = action;
		if (action != null) {
			action.onActionSet(this, getUser());
		}
		
		if (sync && !level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(this, new TrEntityActionInstancePacket(this.getId(), action, true));
		}
	}

	@ApiStatus.Internal // called in StandEntityAbility
	public void setStandAction(@Nullable EntityActionInstance action, Stream<ServerPlayer> syncTo /* in case it's a long-ranged stand, the user might actually be outside of render distance for some players */) {
		setStandAction(action, false);
		
		if (!level().isClientSide()) {
			PacketDistributor2.sendToPlayers(this, syncTo, false, new TrEntityActionInstancePacket(this.getId(), action, true));
		}
	}
	
	protected boolean tickAction(EntityActionInstance action) {
		return action.tickAction();
	}
	// TODO (entity action 2) sync on load
	// TODO (entity action 2) sync already existing action with tracking
	
	
	// TODO StandEntity attributes
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_SPEED)
			.add(Attributes.LUCK)
			.add(Attributes.BLOCK_INTERACTION_RANGE, 4.5)
			.add(Attributes.ENTITY_INTERACTION_RANGE, 3.0)
			.add(Attributes.BLOCK_BREAK_SPEED)
			.add(Attributes.SUBMERGED_MINING_SPEED)
			.add(Attributes.SNEAKING_SPEED)
			.add(Attributes.MINING_EFFICIENCY)
			.add(Attributes.SWEEPING_DAMAGE_RATIO);
	}
	
	
	protected Optional<ResourceLocation> standSkin;
	@Override
	public void setSelectedSkin(Optional<ResourceLocation> standSkin) {
		this.standSkin = standSkin;
	}
	
	public Optional<ResourceLocation> getStandSkin() {
		return standSkin;
	}
	
	
	/**
	 * Apparently we have to do this to make sure the user's id is read before the EntityJoinLevelEvent fires on client side.
	 */
	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity trackedEntity) {
		return new ClientboundAddEntityPacket(this, trackedEntity, entityData.get(USER_ID));
	}
	
	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket packet) {
		super.recreateFromPacket(packet);
		entityData.set(USER_ID, packet.getData());
	}
	
	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		ResourceLocation.STREAM_CODEC.encode(buffer, standId);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		standId = ResourceLocation.STREAM_CODEC.decode(additionalData);
	}
	
	
	// TODO figure out these (abstract methods from LivingEntity)
	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	
	public int nonIdlePoseTimeStamp;
}
