package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.packet.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.PrevRotations;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandEntity extends LivingEntity implements SummonedStand, IEntityWithComplexSpawn, LivingReactToNewAction {
	protected ResourceLocation standId;
	private static final EntityDataAccessor<Integer> USER_ID = SynchedEntityData.defineId(StandEntity.class, EntityDataSerializers.INT);
	private WeakReference<LivingEntity> userRef = new WeakReference<LivingEntity>(null);
	protected StandPower userPower;
	protected final LivingComponentAction standAction;
	
	public static final double Y_OFFSET = 0.2;
	protected static final Vec3 DEFAULT_USER_OFFSET = new Vec3(0.75, Y_OFFSET, -0.75);
	public StandOffsetFromUser offsetFromUser;
	
	public ClientStandEntityStuff clientStuff;

	public StandEntity(EntityType<? extends StandEntity> type, Level level) {
		super(type, level);
		this.standAction = LivingComponentAction.getComponent(this);
		this.offsetFromUser = new StandOffsetFromUser(this, DEFAULT_USER_OFFSET, StandOffsetFromUser.OffsetMode.BODY);
		this.noPhysics = true;
		setNoGravity(true);
		if (level.isClientSide()) {
			this.clientStuff = new ClientStandEntityStuff();
		}
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
	

	protected PrevRotations rotO = new PrevRotations();
	@Override
	public void tick() {
		fallDistance = 0;
		rotO.rememberAngles(this);
		super.tick();
		LivingEntity user = getUser();
		if (user != null) {
			updatePosition(user);
			if (!level().isClientSide()) {
				tickHealth(user);
			}
		}
	}
	
	public void updatePosition(LivingEntity user) {
		if (user != null) {
			Vec3 pos = offsetFromUser.getPosition(user);
			setPos(pos.x, pos.y, pos.z);
			copyStandUserRotation(user);
		}
		lookAtCurTarget(rotO);
	}
	
	public void copyStandUserRotation(LivingEntity user) {
		offsetFromUser.copyRotation(user, level().isClientSide());
	}
	
	protected boolean lookAtCurTarget(PrevRotations rotO) {
		ActionTarget lookTarget;
		EntityActionInstance curAction = standAction.getAction();
		boolean fullyRotateBody = curAction != null;
		if (curAction != null) {
			lookTarget = curAction.standRotationTarget;
			if (lookTarget == null) {
				lookTarget = ActionTarget.EMPTY;
			}
			else if (lookTarget.isEmpty(level())) {
				curAction.standRotationTarget = null;
				lookTarget = ActionTarget.EMPTY;
			}
		}
		else {
			ActionTarget crosshairTarget = standAction.entityAim.getTarget();
			if (crosshairTarget.getType() == TargetType.ENTITY) {
				lookTarget = crosshairTarget;
			}
			else {
				lookTarget = ActionTarget.EMPTY;
			}
		}
		
		Vec3 targetPos = switch (lookTarget.getType()) {
			case ENTITY -> {
				Entity targetEntity = lookTarget.getEntity();
				// TODO (stand aiming) look closer to where the user is looking (legs/head aiming)
				double y = targetEntity instanceof LivingEntity ? 
						targetEntity.getEyeY() : 
						(targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0;
				yield new Vec3(targetEntity.getX(), y, targetEntity.getZ());
			}
			case BLOCK -> {
				yield Vec3.atCenterOf(lookTarget.getBlockPos());
			}
			default -> null;
		};
		
		if (targetPos != null) {
			Vec2 rotations = MathUtil.lookAnglesTowards(targetPos, this, EntityAnchorArgument.Anchor.EYES);
			this.setXRot(rotations.x);
			this.setYRot(rotations.y);
			if (fullyRotateBody) {
				this.setYHeadRot(this.getYRot());
				this.setYBodyRot(this.getYRot());
			}
			else {
				float maxHeadYRot = 37.5f;
		    	float f2 = Mth.wrapDegrees(yBodyRot - this.getYRot());
		    	float f3 = Mth.clamp(f2, -maxHeadYRot, maxHeadYRot);
				this.setYHeadRot(this.getYRot() + f2 - f3);
			}
			this.xRotO = rotO.xRot;
			this.yRotO = rotO.yRot;
			this.yHeadRotO = rotO.yHeadRot;
			this.yBodyRotO = rotO.yBodyRot;
			return true;
		}
		return false;
	}
	
	public boolean isFollowingUser() {
		return true;
	}
	
	@Override
	public void push(Entity entity) {}
	
	@Override
	public boolean isPushable() { return false; }
	
	@Override
	public void pushEntities() {}

	@Override
	public boolean isPickable() {
		if (level().isClientSide()) {
			Player clientPlayer = ClientProxy.getClientPlayer();
			if (clientPlayer != null && this.is(ClientGlobals.playerStandEntity)) {
				return false;
			}
		}
		return super.isPickable();
	}

	
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
	
	@Override
	public StandEntity getStandEntity() {
		return this;
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
	public EntityActionInstance getCurStandAction() {
		return standAction.getAction();
	}
	
	@Nonnull
	public LivingComponentAction getStandActionComponent() {
		return standAction;
	}
	
	@Override
	public boolean onActionSet(EntityActionInstance action) {
		if (action == null) {
			offsetFromUser.resetToIdle(getUser());
		}
		return false;
	}
	// TODO (entity action 2) sync on load
	// TODO (entity action 2) sync already existing action with tracking
	
	
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.ATTACK_DAMAGE, 1.0)
			.add(Attributes.MOVEMENT_SPEED, 0.1F)
			.add(Attributes.ATTACK_SPEED)
			.add(Attributes.LUCK)
			.add(Attributes.BLOCK_INTERACTION_RANGE, 4.0)
			.add(Attributes.ENTITY_INTERACTION_RANGE, 4.0)
			.add(Attributes.BLOCK_BREAK_SPEED)
			.add(Attributes.SUBMERGED_MINING_SPEED)
			.add(Attributes.SNEAKING_SPEED)
			.add(Attributes.MINING_EFFICIENCY)
			.add(Attributes.SWEEPING_DAMAGE_RATIO);
	}

	// TODO StandEntity stat attributes
	public double getPrecision() {
		return 20;
	}
	
	
	protected Optional<ResourceLocation> standSkin;
	@Override
	public void setSelectedSkin(Optional<ResourceLocation> standSkin) {
		this.standSkin = standSkin;
	}
	
	public Optional<ResourceLocation> getStandSkin() {
		return standSkin;
	}
	
	
	public boolean onlyVisibleToStandUsers() {
		return true;
	}

	
	@Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
		LivingEntity user = getUser();
		return user != null && (
					user.isInvulnerableTo(level, damageSource)
					|| user instanceof Player player && player.getAbilities().invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
				|| !DamageUtil.canHurtStands(damageSource)
				|| super.isInvulnerableTo(level, damageSource);
	}
	
	protected void tickHealth(LivingEntity user) {
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(user.getMaxHealth());
		setHealth(user.getHealth());
	}


	@Deprecated
	@Override
	public boolean canAttack(LivingEntity entity) {
		if (entity.is(this) || !super.canAttack(entity)) return false;

		LivingEntity user = getUser();
		if (user != null) {
			boolean canHarm = DamageUtil.isNotFriendlyFire(user, entity);
			if (canHarm && entity instanceof Animal) {
				canHarm &= !entity.isPassengerOfSameVehicle(user);
				if (canHarm && entity instanceof TamableAnimal) {
					LivingEntity tameableOwner = ((TamableAnimal) entity).getOwner();
					canHarm &= !(tameableOwner != null && tameableOwner == user);
				}
			}
			return canHarm;
		}

		return true;
	}

	public boolean canAttackEntity(Entity target) {
		if (target instanceof LivingEntity) {
			return canAttack((LivingEntity) target);
		}
		LivingEntity user = getUser();
		if (target instanceof Projectile) {
			Entity owner = ((Projectile) target).getOwner();
			if (owner != null && (owner.is(this) || owner.is(user))) {
				// TODO mod projectiles that can hit the owner
//				return target instanceof DamagingEntity && ((DamagingEntity) target).canHitOwner();
				return false;
			}
		}
		if (user != null && target.getControllingPassenger() == user) {
			return false;
		}
		return true;
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
		buffer.writeFloat(yBodyRot);
		buffer.writeVarInt(tickCount);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		standId = ResourceLocation.STREAM_CODEC.decode(additionalData);
		yBodyRot = additionalData.readFloat();
		yBodyRotO = yBodyRot;
		tickCount = additionalData.readVarInt();
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
