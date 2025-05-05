package com.github.standobyte.jojo.powersystem.ability.config;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@ApiStatus.Internal
public class ConfigAbilityFactory<A extends Ability> {
	private final AbilityType<A> abilityType;
	private final AbilityConfigComponent<A>[] abilityConfig;
	
	public ConfigAbilityFactory(AbilityType<A> abilityType, AbilityConfigComponent<A>[] abilityConfig) {
		this.abilityType = abilityType;
		this.abilityConfig = abilityConfig;
	}
	
	public A makeAbility(AbilityId abilityId) {
		A ability = abilityType.createInstance(a -> {
			if (abilityConfig != null) {
				for (var config : abilityConfig) {
					config.accept(a);
				}
			}
		}, abilityId);
		return ability;
	}
	
	
	// TODO ability configs
//	@SuppressWarnings("rawtypes")
//	public static final Codec<AbilityConfig> CONFIG_CODEC_PLACEHOLDER = CodecUtil.placeholderCodec(null);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Codec<ConfigAbilityFactory<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			JojoRegistries.ABILITY_TYPES_REG.byNameCodec().fieldOf("type").forGetter(factory -> factory.abilityType)//,
//			CONFIG_CODEC_PLACEHOLDER.optionalFieldOf("config").forGetter(factory -> Optional.ofNullable(factory.abilityConfig))
		).apply(instance, 
			(AbilityType<?> abilityType/*, Optional<AbilityConfig> abilityConfig*/) -> {
				return new ConfigAbilityFactory(abilityType, null);
			}
		));

}
