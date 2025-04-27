package com.github.standobyte.jojo.powersystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityConfig;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

@ApiStatus.NonExtendable
public class Moveset {
	protected final Map<String, Ability> abilities;
	
	protected Moveset(Moveset.Builder builder, PowerClass<?> powerClass, ResourceLocation powerTypeId) {
		this.abilities = builder.allAbilities.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey, entry -> entry.getValue().makeAbility(new AbilityId(powerClass, powerTypeId, entry.getKey()))));
	}
	
	public Ability getAbility(String name) {
		return abilities.get(name);
	}
	
	
	public static class Builder {
		protected final Map<String, ConfigAbilityFactory<?>> allAbilities = new HashMap<>();
		
		public <A extends Ability> Builder addAbility(String abilityName, AbilityType<A> abilityType) {
			return addAbility(abilityName, abilityType, null);
		}
		
		public <A extends Ability> Builder addAbility(String abilityName, AbilityType<A> abilityType, @Nullable AbilityConfig<A> setParameters) {
			allAbilities.put(abilityName, new ConfigAbilityFactory<>(abilityType, setParameters));
			return this;
		}
		
		public <A extends Ability> Builder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType) {
			return addAbility(abilityName, abilityType.get());
		}
		
		public <A extends Ability> Builder addAbility(String abilityName, Supplier<? extends AbilityType<A>> abilityType, @Nullable AbilityConfig<A> setParameters) {
			return addAbility(abilityName, abilityType.get(), setParameters);
		}
		
		public Builder removeAbility(String abilityName) {
			allAbilities.remove(abilityName);
			return this;
		}
		
		
		/**
		 * @deprecated Prefer using the methods that explicitly take an abilityName parameter, this method is only here if you're feeling lazy to type ability ids.
		 */
		@Deprecated
		public Builder addAbility(DeferredHolder<AbilityType<?>, ? extends AbilityType<? extends Ability>> abilityType) {
			return addAbility(getLocalId(abilityType), abilityType.get(), null);
		}
		
		@Deprecated
		public <A extends Ability> Builder addAbility(DeferredHolder<AbilityType<?>, ? extends AbilityType<A>> abilityType, @Nullable AbilityConfig<A> setParameters) {
			return addAbility(getLocalId(abilityType), abilityType.get(), setParameters);
		}
		
		@SafeVarargs
		public final Builder addAbilities(DeferredHolder<AbilityType<?>, ? extends AbilityType<? extends Ability>>... abilityTypes) {
			for (var abilityType : abilityTypes) {
				addAbility(getLocalId(abilityType), abilityType.get());
			}
			return this;
		}
		
		private static String getLocalId(DeferredHolder<AbilityType<?>, ? extends AbilityType<?>> abilityType) {
			String path = abilityType.getKey().location().getPath();
			if (path.startsWith("stand_")) path = path.substring(6);
			return path;
		}
		
		
		public Moveset.Builder copy() {
			Moveset.Builder copy = new Moveset.Builder();
			for (var ability : this.allAbilities.entrySet()) {
				copy.allAbilities.put(ability.getKey(), ability.getValue().copy());
			}
			return copy;
		}
		
		public void merge(Moveset.Builder edits) {
			for (var editEntry : edits.allAbilities.entrySet()) {
				String abilityName = editEntry.getKey();
				var curAbility = this.allAbilities.get(abilityName);
				if (curAbility == null || curAbility.abilityType != editEntry.getValue().abilityType) {
					this.allAbilities.put(abilityName, editEntry.getValue());
				}
				else {
					curAbility.abilityConfig.merge(editEntry.getValue().abilityConfig);
				}
			}
		}
		
		public Moveset build(PowerClass<?> powerClass, ResourceLocation powerTypeId) {
			return new Moveset(this, powerClass, powerTypeId);
		}
	}
	
	public static class ConfigAbilityFactory<A extends Ability> {
		private final AbilityType<A> abilityType;
		private final AbilityConfig<A> abilityConfig;
		
		public ConfigAbilityFactory(AbilityType<A> abilityType, @Nullable AbilityConfig<A> abilityConfig) {
			this.abilityType = abilityType;
			this.abilityConfig = abilityConfig;
		}
		
		protected ConfigAbilityFactory<A> copy() {
			return new ConfigAbilityFactory<>(this.abilityType, this.abilityConfig != null ? this.abilityConfig.copy() : null);
		}
		
		protected A makeAbility(AbilityId abilityId) {
			A ability = abilityType.createInstance(abilityConfig, abilityId);
			return ability;
		}
		
		
		// TODO ability configs
		@SuppressWarnings("rawtypes")
		protected static final Codec<AbilityConfig> CONFIG_CODEC_PLACEHOLDER = CodecUtil.placeholderCodec(null);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected static final Codec<ConfigAbilityFactory<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				JojoRegistries.ABILITY_TYPES_REG.byNameCodec().fieldOf("type").forGetter(factory -> factory.abilityType),
				CONFIG_CODEC_PLACEHOLDER.optionalFieldOf("config").forGetter(factory -> Optional.ofNullable(factory.abilityConfig))
			).apply(instance, 
				(AbilityType<?> abilityType, Optional<AbilityConfig> abilityConfig) -> {
					return new ConfigAbilityFactory(abilityType, abilityConfig.orElse(null));
				}
			));
	}
	
	
	public static Codec<Moveset.Builder> builderCodec() {
		return BUILDER_CODEC;
	}
	
	// XXX deserialize default control schemes
	protected static final Codec<Moveset.Builder> BUILDER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(Codec.STRING, ConfigAbilityFactory.CODEC, null).codec().fieldOf("abilities").forGetter(moveset -> moveset.allAbilities))
			.apply(instance, (Map<String, ConfigAbilityFactory<?>> abilities) -> {
				Moveset.Builder moveset = new Moveset.Builder();
				for (var ability : abilities.entrySet()) {
					moveset.allAbilities.put(ability.getKey(), ability.getValue());
				}
				return moveset;
			}));
	
	
	protected static final Moveset EMPTY = new Moveset(new Moveset.Builder(), null, null);
	
	public static Moveset empty() {
		return EMPTY;
	}
	
}
