package com.github.standobyte.jojo.mechanics.character.mob;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.core.ModEntityDataSerializers;
import com.github.standobyte.jojo.mechanics.inheritancesucks.EntityAsPlayerWrapper;
import com.github.standobyte.jojo.mechanics.inheritancesucks.RemoteClientPlayerLivingWrapper;
import com.github.standobyte.jojo.mechanics.inheritancesucks.ServerPlayerLivingWrapper;
import com.github.standobyte.jojo.util.NBTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.XpOrbTargetingEvent;

// TODO (character mob) player mechanics
/*
 * inventory
 * food data
 * picking up items
 * xp
 * ...
 * swimming, crawling, sneaking
 * ...
 * ender chest
 * elytra flight, mace (their code fucking sucks)
 * ...
 * using items and containers...
⣀⣠⣤⣤⣤⣤⢤⣤⣄⣀⣀⣀⣀⡀⡀⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄
⠄⠉⠹⣾⣿⣛⣿⣿⣞⣿⣛⣺⣻⢾⣾⣿⣿⣿⣶⣶⣶⣄⡀⠄⠄⠄
⠄⠄⠠⣿⣷⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣿⣿⣿⣿⣿⣿⣆⠄⠄
⠄⠄⠘⠛⠛⠛⠛⠋⠿⣷⣿⣿⡿⣿⢿⠟⠟⠟⠻⠻⣿⣿⣿⣿⡀⠄
⠄⢀⠄⠄⠄⠄⠄⠄⠄⠄⢛⣿⣁⠄⠄⠒⠂⠄⠄⣀⣰⣿⣿⣿⣿⡀
⠄⠉⠛⠺⢶⣷⡶⠃⠄⠄⠨⣿⣿⡇⠄⡺⣾⣾⣾⣿⣿⣿⣿⣽⣿⣿
⠄⠄⠄⠄⠄⠛⠁⠄⠄⠄⢀⣿⣿⣧⡀⠄⠹⣿⣿⣿⣿⣿⡿⣿⣻⣿
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠉⠛⠟⠇⢀⢰⣿⣿⣿⣏⠉⢿⣽⢿⡏
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠠⠤⣤⣴⣾⣿⣿⣾⣿⣿⣦⠄⢹⡿⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠒⣳⣶⣤⣤⣄⣀⣀⡈⣀⢁⢁⢁⣈⣄⢐⠃⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⣰⣿⣛⣻⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⣬⣽⣿⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠁⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⢘⣿⣿⣻⣛⣿⡿⣟⣻⣿⣿⣿⣿⡟⠄⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠛⢛⢿⣿⣿⣿⣿⣿⣿⣷⡿⠁⠄⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠉⠉⠉⠉⠈⠄⠄⠄⠄⠄⠄
 */
public class PowerUserMobEntity extends Mob {
	public static final EntityDataAccessor<Optional<ResolvableProfile>> DATA_PROFILE = SynchedEntityData.defineId(PowerUserMobEntity.class, 
			ModEntityDataSerializers.RESOLVABLE_PROFILE_OPTIONAL.get());
	public ClientHumanoidCharacterStuff clientStuff;
	public EntityAsPlayerWrapper playerWrapper;

	public PowerUserMobEntity(EntityType<? extends PowerUserMobEntity> entityType, Level level) {
		super(entityType, level);
		if (!level.isClientSide()) {
			this.playerWrapper = ServerPlayerLivingWrapper.create(this, null);
		}
		else {
			this.playerWrapper = RemoteClientPlayerLivingWrapper.create(this);
			this.clientStuff = level.isClientSide() ? new ClientHumanoidCharacterStuff() : null;
		}
		setPersistenceRequired();
	}

	public PowerUserMobEntity(Level level) {
		this(ModEntityTypes.CHARACTER.get(), level);
	}
	
	
	public static boolean isMobPlayerLike(Entity entity) {
		return entity.getType() == ModEntityTypes.CHARACTER.get();
	}
	
	public static Player getWrapperFakePlayer(Entity actualEntity) {
		if (actualEntity instanceof PowerUserMobEntity characterMob) {
			return characterMob.playerWrapper.asPlayer();
		}
		return null;
	}
	

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PROFILE, Optional.empty());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_PROFILE) {
			if (level().isClientSide()) {
				clientStuff.onSetSkinSourceProfile(this.entityData.get(DATA_PROFILE), this);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (playerWrapper != null) {
			Player asPlayer = playerWrapper.asPlayer();
			if (asPlayer instanceof ServerPlayer asServerPlayer) {
				asServerPlayer.doTick();
			}
			else {
				asPlayer.tick();
			}
			ServerPlayerLivingWrapper.copyData(this, asPlayer);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		if (playerWrapper != null) {
			Player player = playerWrapper.asPlayer();
			
			CompoundTag playerData = new CompoundTag();
			playerData.putFloat("XpP", player.experienceProgress);
			playerData.putInt("XpLevel", player.experienceLevel);
			playerData.putInt("XpTotal", player.totalExperience);
//			playerData.putInt("XpSeed", player.enchantmentSeed);
			
			compound.put("Player", playerData);
		}
		
		entityData.get(DATA_PROFILE).ifPresent(profile -> {
			ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).ifSuccess(profileNbt -> {
				compound.put("Skin", profileNbt);
			});
		});
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		if (playerWrapper != null) {
			Player player = playerWrapper.asPlayer();
			
			CompoundTag playerData = NBTUtil.getCompoundOptional(compound, "Player").orElse(null);
			if (playerData != null) {
				player.experienceProgress = playerData.getFloat("XpP");
				player.experienceLevel = playerData.getInt("XpLevel");
				player.totalExperience = playerData.getInt("XpTotal");
//				player.enchantmentSeed = playerData.getInt("XpSeed");
			}
		}
		
		NBTUtil.getCompoundOptional(compound, "Skin").ifPresent(profileNbt -> {
			ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, profileNbt).ifSuccess(profile -> {
				entityData.set(DATA_PROFILE, Optional.of(profile));
			});
		});
	}


	// Disabling any form of despawning

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean requiresCustomPersistence() {
		return true;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public void checkDespawn() {}


	// XP stuff

	@Override
	protected int getBaseExperienceReward() {
		if (playerWrapper != null) {
			int i = playerWrapper.asPlayer().experienceLevel * 7;
			return i > 100 ? 100 : i;
		}
		return 0;
	}
	
	@Override
    protected boolean isAlwaysExperienceDropper() {
    	return true;
    }
	
	// XP orbs themselves are picked up when we call ServerPlayer#doTick() in this entity's tick method

	public static void attractXpToCharacter(XpOrbTargetingEvent event) {
		ExperienceOrb xpOrb = event.getXpOrb();
		Vec3 pos = xpOrb.position();
		double maxDist = event.getScanDistance();
		Level level = xpOrb.level();
		AABB searchBox = new AABB(
				pos.x - maxDist, pos.y - maxDist, pos.z - maxDist, 
				pos.x + maxDist, pos.y + maxDist, pos.z + maxDist);
		List<Entity> mobCharacters = level.getEntities((Entity) null, searchBox, entity -> PowerUserMobEntity.isMobPlayerLike(entity));
		if (!mobCharacters.isEmpty()) {
			Entity nearestMob = null;
			double nearestMobDistSqr = -1;
			double maxDistSqr = maxDist * maxDist;
			for (Entity mob : mobCharacters) {
				double distSqr = mob.distanceToSqr(xpOrb);
				if ((distSqr < 0.0 || distSqr < maxDistSqr)
						&& (nearestMobDistSqr == -1.0 || distSqr < nearestMobDistSqr)) {
					nearestMob = mob;
					nearestMobDistSqr = distSqr;
				}
			}
			
			if (nearestMob != null) {
				Player evenNearerPlayer = level.getNearestPlayer(pos.x, pos.y, pos.z, 
						Math.sqrt(nearestMobDistSqr), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
				event.setFollowingPlayer(evenNearerPlayer != null ? evenNearerPlayer : PowerUserMobEntity.getWrapperFakePlayer(nearestMob));
			}
		}
	}
	
	// Item dropping

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
//		PlayerLikeMobData data = ComponentUtil.getExistingDataOrNull(this, ModDataAttachmentTypes.PLAYER_LIKE_DATA);
//		if (data != null && data.inventory != null) {
//			CharacterMobInventory.destroyVanishingCursedItems(data.inventory);
//			data.inventory.dropAll();
//		}
	}


	@Override
	public boolean canUseSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.BODY;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean canAttackType(EntityType<?> type) {
		return true;
	}


}
