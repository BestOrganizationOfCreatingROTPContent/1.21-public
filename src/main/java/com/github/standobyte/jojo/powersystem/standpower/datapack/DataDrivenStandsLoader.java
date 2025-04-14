package com.github.standobyte.jojo.powersystem.standpower.datapack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.core.packet.fromserver.DatapackStandsPacket;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.JSONUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.network.PacketDistributor;

public class DataDrivenStandsLoader {
	private static DatapackStandsMap clientReceivedStands;
	private static DataPackStandsServerManager serverLoadedStands;
	
	public static PreparableReloadListener getDatapackStandsLoader() {
		if (serverLoadedStands == null) {
			serverLoadedStands = new DataPackStandsServerManager();
		}
		return (PreparableReloadListener) serverLoadedStands;
	}
	
	
	@Nullable
	public static StandType getDatapackStand(ResourceLocation standId) {
		StandType stand = standsMap().datapackStands().get(standId);
		if (stand != null && !stand.isEnabled()) {
			stand = null;
		}
		return stand;
	}
	
	@Nullable
	public static Stream<StandType> getAllDatapackStands() {
		return standsMap().datapackStands().entrySet().stream()
				.filter(entry -> entry.getValue().isEnabled()).map(entry -> entry.getValue());
	}
	
	@Nonnull
	private static DatapackStandsMap standsMap() {
		if (clientReceivedStands != null) return clientReceivedStands;
		// if we haven't received a clientbound packet with a map, we're a dedicated server
		if (serverLoadedStands != null) return serverLoadedStands;
		throw new IllegalStateException();
	}
	
	protected static interface DatapackStandsMap {
		Map<ResourceLocation, StandType> datapackStands();
	}
	
	// Data resource loading
	
	public static final String DIRECTORY = "jojostandpowers";
	public static final String FILE_EXT = ".json";
	public static class DataPackStandsServerManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> implements DatapackStandsMap {
		private final Map<ResourceLocation, StandType> datapackStands = new HashMap<>();
		private Map<ResourceLocation, JsonObject> loadedConfigsData = new HashMap<>();

		@Override
		protected Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
			Map<ResourceLocation, JsonObject> map = new TreeMap<>();
			
			Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIRECTORY, rl -> rl.getPath().endsWith(FILE_EXT));
			Map<ResourceLocation, StitchedStandDataFiles> grouped = StitchedStandDataFiles.groupResources(resources);
			
			for (var entry : grouped.entrySet()) {
				ResourceLocation standId = entry.getKey();
				StitchedStandDataFiles resource = entry.getValue();
				try {
					JsonObject fullJson = resource.stitchJson(standId);
					if (fullJson != null) {
						map.put(standId, fullJson);
					}
				} catch (Exception e) {
					JojoMod.getLogger().error("Failed to parse JSON data for Stand {}", standId, e);
				}
			}
			
			return map;
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonObject> object, ResourceManager resourceManager, ProfilerFiller profiler) {
			this.loadedConfigsData = object;
			applyData(this.datapackStands, this.loadedConfigsData.entrySet());
			
			JojoMod.getLogger().info("Loaded {} Stand data pack entries (created {} new Stands)", 
					object.size(), datapackStands.values().stream().filter(StandType::isEnabled).count());
		}
		
		
		@Override
		public Map<ResourceLocation, StandType> datapackStands() {
			return datapackStands;
		}

	}
	
	// Apply the datapack (configure existing Stands, create new ones)
	
	private static void applyData(Map<ResourceLocation, StandType> datapackStands, Collection<Map.Entry<ResourceLocation, JsonObject>> loadedConfigsData) {
		// If the stand still exists in the datapack, it will be re-enabled.
		// It's made so that, if a player has a Stand of that type already, the reference points to the same object after datapack reload.
		datapackStands.values().forEach(stand -> stand.setEnabled(false));
		
		Registry<StandType> hardcodedStands = JojoRegistries.DEFAULT_STANDS_REG;
		hardcodedStands.entrySet().forEach(entry -> entry.getValue().restoreDefaults());
		
		for (var dataEntry : loadedConfigsData) {
			ResourceLocation standId = dataEntry.getKey();
			JsonObject json = dataEntry.getValue();
			if (hardcodedStands.containsKey(standId)) {
				// Configure one of the registered Stands.
				StandType stand = hardcodedStands.getValue(standId);
				stand.applyConfig(json);
			}
			else {
				// Create a new data-driven Stand
				StandType newDatapackStand = null;
				ResourceLocation baseStandId = Optional.ofNullable(json.get("baseStand")).map(JsonElement::getAsString).map(ResourceLocation::parse).orElse(null);
				if (baseStandId != null) {
					// Use one of the existing Stands as a base.
					// Another json to configure the new Stand is created based on the baseStand's values.
					StandType baseStand = hardcodedStands.getValue(baseStandId);
					if (baseStand != null) {
						try {
							StandTypeClass<?> standTypeClass = StandTypeClass.byClass(baseStand.getClass());
							StandStats stats = baseStand.getStandStats().defaultToBuilder().build();
							Moveset.Builder<StandPower> moveset = baseStand.copyDefaultMoveset();
							newDatapackStand = standTypeClass.createStand(stats, moveset, standId);
						} catch (Exception e) {
							JojoMod.getLogger().error("Failed to create Stand {} from data pack (Stand {} can't be used as a base)", standId, baseStandId);
							continue;
						}
						JsonObject baseStandConfig = baseStand.makeConfigTemplate();
						JSONUtil.merge(baseStandConfig, json);
						json = baseStandConfig;
					}
					else {
						JojoMod.getLogger().error("Failed to create Stand {} from data pack (base Stand {} is not present)", standId, baseStandId);
					}
				}
				else {
					// Create the new Stand from scratch using all the JSON data.
					StandStats stats = Optional.ofNullable(json.getAsJsonObject("stats"))
							.map(StandStats::fromJson)
							.orElse(new StandStats(0, 0, 0, 0, 0, 0));
					
					Codec<Moveset.Builder<StandPower>> javaZaebala = Moveset.builderCodec();
					Moveset.Builder<StandPower> moveset = Optional.ofNullable(json.getAsJsonObject("moveset"))
							.flatMap(movesetJson -> javaZaebala.decode(JsonOps.INSTANCE, movesetJson).result())
							.map(Pair::getFirst)
							.orElse(new Moveset.Builder<>());
					
					String standClassAlias = StandTypeClass.getStandClassAlias(json);
					try {
						StandTypeClass<?> standTypeClass = standClassAlias != null ? StandTypeClass.byName(standClassAlias) : StandTypeClass.DEFAULT_CLASS;
						newDatapackStand = standTypeClass.createStand(stats, moveset, standId);
					} catch (Exception e) {
						JojoMod.getLogger().error("Failed to create Stand {} from data pack (Stand class {} does not exist or can't be instantiated)", standId, standClassAlias);
						continue;
					}
				}
				
				if (newDatapackStand != null) {
					StandType oldDatapackStand = datapackStands.get(standId);
					if (oldDatapackStand != null && oldDatapackStand.getClass() == newDatapackStand.getClass()) {
						oldDatapackStand.setEnabled(true);
						oldDatapackStand.restoreDefaults();
						oldDatapackStand.applyConfig(json);
					}
					else {
						newDatapackStand.applyConfig(json);
						datapackStands.put(standId, newDatapackStand);
					}
				}
			}
		}
	}
	
	// Sync to clients
	
	public static void syncDatapackTo(Stream<ServerPlayer> players) {
		Collection<Map.Entry<ResourceLocation, JsonObject>> standEntries = null;
		if (serverLoadedStands != null) {
			var standsMap = serverLoadedStands.loadedConfigsData;
			if (standsMap != null) {
				standEntries = standsMap.entrySet();
			}
		}
		var packet = new DatapackStandsPacket(standEntries != null ? standEntries : Collections.emptySet());
		players.forEach(player -> PacketDistributor.sendToPlayer(player, packet));
	}
	
	public static void receivePacket(DatapackStandsPacket packet) {
		if (clientReceivedStands == null) {
			clientReceivedStands = new ClientsideWrapper();
		}
		applyData(clientReceivedStands.datapackStands(), packet.standData());
	}

	@ApiStatus.Internal
	public static class ClientsideWrapper implements DatapackStandsMap {
		private final Map<ResourceLocation, StandType> stands = new HashMap<>();

		@Override
		public Map<ResourceLocation, StandType> datapackStands() {
			return stands;
		}
		
	}
	
	// Generate the Stand config template
	
	// XXX (data-driven stands) a way generate the stand jsons (or only a part of them) on demand of the server admin (/jojoconfig)
	public static Map<String, JsonElement> standTypeToJsons(StandType standType) {
		JsonObject fullJson = standType.makeConfigTemplate();
		Map<String, JsonElement> separateFiles = new HashMap<>();
		separateFiles.put("stand", fullJson);
		return separateFiles;
	}
	
}
