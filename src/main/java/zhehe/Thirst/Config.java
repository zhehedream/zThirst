package zhehe.Thirst;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class Config {
	
	private static Config config = new Config();
	
	private Config() {
		;
	}
	
	public static Config getConfig() {
		return config;
	}
	
	private ZThirst zthirst;
	
	private Path configFile;

	ConfigurationLoader<CommentedConfigurationNode> loader;
	CommentedConfigurationNode rootNode;
	
	public HashMap<String,Float> item = null;
	public ArrayList<PotionEffectType> positive_p = null;
	public ArrayList<Integer> positive_d = null;
	public ArrayList<Integer> positive_a = null;
	public ArrayList<PotionEffectType> negative_p = null;
	public ArrayList<Integer> negative_d = null;
	public ArrayList<Integer> negative_a = null;
	public HashMap<String, Float> biome_list = null;
	public HashMap<String, Float> world_list = null;
		
	public void init(ZThirst in) {
		zthirst = in;
		Path configDir = zthirst.getconfigDir();
		configFile = Paths.get(configDir + "/config.txt");
		loader = HoconConfigurationLoader.builder().setPath(configFile).build();
	}
	
	private void init_data() {
		item = StringToHashMap(rootNode.getNode("Details", Constant.quenching_items).getString());
		PotionEffectList pel;
		pel = Potion_StringToArrayList(rootNode.getNode("Details", Constant.bonus_effets).getString());
		positive_p = pel.ptype;
		positive_d = pel.duration;
		positive_a = pel.amplifier;
		
		ZThirst.getZThirst().SendTerminalMessage(positive_p.toString());
		
		pel = Potion_StringToArrayList(rootNode.getNode("Details", Constant.punishment_effects).getString());

		negative_p = pel.ptype;
		negative_d = pel.duration;
		negative_a = pel.amplifier;
		
		biome_list = StringToHashMap(rootNode.getNode("Other", Constant.biome_setting).getString());
		
		world_list = StringToHashMap(rootNode.getNode("Other", Constant.world_setting).getString());
	}
	
	public void reload() {
		init_config();
	}
	
	public void init_config() {
		zthirst.SendTerminalMessage("[ZRecipe] Try to read the ZRecipe config file.");
		if (!Files.exists(configFile)) {
			try {
				zthirst.SendTerminalMessage("[ZRecipe] Could not find a valid config file, so will create one.");
				Files.createFile(configFile);
				load_config();
				build();
				save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			load_config();
		}
		init_data();
	}
	
	private void load_config() {
		try {
			rootNode = loader.load();

			switch (rootNode.getNode("ConfigVersion").getInt()) {
				case 1: {
					// current version
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getInitialThirst() {
		int temp = rootNode.getNode("Value", Constant.initial_thirst).getInt();
		return (temp > 10000) ? 10000 : temp;
	}
	
	public long getWaterBarRefreshInterval() {
		long temp = rootNode.getNode("System", Constant.waterbar_refresh_interval).getLong();
		return (temp < 1) ? 1 : temp;
	}
	
	public long getDataBaseRefreshInterval() {
		//return rootNode.getNode("System", "Database ASync Refresh Interval(Tick)").getLong();
		return 500;
	}
	
	public int getConsumeInterval() {
		int temp = rootNode.getNode("Value", Constant.thirst_consume_interval).getInt();
		return (temp < 1) ? 1 : temp;
	}
	
	public int getConsumeValue() {
		int temp = rootNode.getNode("Value", Constant.thirst_consume_value).getInt();
		return (temp < 1) ? 1 : temp;
	}
	
	public boolean ignore_createmode() {
		return rootNode.getNode("System", Constant.ignore_createmode).getBoolean();
	}
	
	public int getBonusCoolDown() {
		int temp = rootNode.getNode("Details", Constant.bonus_cooldown).getInt();
		return (temp < 1) ? 1 : temp;
	}
	public int getPunishmentInterval() {
		int temp = rootNode.getNode("Details", Constant.punishment_interval).getInt();
		return (temp < 1) ? 1 : temp;
	}
	public int getPunishmentThirstThreshold() {
		int temp = rootNode.getNode("Details", Constant.punishment_thirst_threshold).getInt();
		return (temp < 1) ? 1 : temp;
	}
	public Text getPunishmentMessage() {
		return Text.of(rootNode.getNode("Details", Constant.punishment_message).getString());
	}
	
	/*public int getDatabaseRefreshInterval() {
		return rootNode.getNode("System", "Database ASync Refresh Interval (s)").getInt();
	}*/
	
	private void build() {
		rootNode.getNode("ConfigVersion").setValue(1).setComment("Config file version. Do not change it!!");
		rootNode.getNode("System", Constant.ignore_createmode).setValue(true)
			    .setComment("If true, anyone in creative mode will not be affected by Thirst");
		rootNode.getNode("System", Constant.waterbar_refresh_interval).setValue(10);
		//rootNode.getNode("System", "Database ASync Refresh Interval (s)").setValue(10);
		
		rootNode.getNode("Value", Constant.initial_thirst).setValue(10000)
				.setComment("Maximum value is 10000 (100%).");
		rootNode.getNode("Value", Constant.thirst_consume_interval).setValue(72);
		rootNode.getNode("Value", Constant.thirst_consume_value).setValue(100);
		
		HashMap<String, Float> quenching = new HashMap<>();
		quenching.put(ItemTypeToString(ItemTypes.APPLE), 1000f);
		quenching.put(ItemTypeToString(ItemTypes.MELON), 2000f);
		quenching.put(ItemTypeToString(ItemTypes.POTION), 3000f);
		quenching.put(ItemTypeToString(ItemTypes.GOLDEN_APPLE), 3000f);
		quenching.put(ItemTypeToString(ItemTypes.MILK_BUCKET), 4000f);
		rootNode.getNode("Details", Constant.quenching_items).setValue(HashMapToString(quenching));
		
		ArrayList<PotionEffectType> p = new ArrayList<>();
		ArrayList<Integer> d = new ArrayList<>();
		ArrayList<Integer> a = new ArrayList<>();
		
		p.add(PotionEffectTypes.LUCK);
		d.add(5);
		a.add(1);
		
		p.add(PotionEffectTypes.SPEED);
		d.add(5);
		a.add(3);
		
		p.add(PotionEffectTypes.RESISTANCE);
		d.add(5);
		a.add(3);
		
		rootNode.getNode("Details", Constant.bonus_effets).setValue(Potion_ArrayListToString(p,d,a))
			    .setComment("Effect when thirst is full");
		rootNode.getNode("Details", Constant.bonus_cooldown).setValue(150);
		rootNode.getNode("Details", Constant.punishment_thirst_threshold).setValue(3000);
		
		p.clear();
		d.clear();
		a.clear();
		
		p.add(PotionEffectTypes.BLINDNESS);
		d.add(5);
		a.add(1);
		
		p.add(PotionEffectTypes.HUNGER);
		d.add(5);
		a.add(3);
		
		p.add(PotionEffectTypes.SLOWNESS);
		d.add(5);
		a.add(3);
		
		rootNode.getNode("Details", Constant.punishment_effects).setValue(Potion_ArrayListToString(p,d,a))
	    .setComment("Effect when thirst is lower than threshold");
		rootNode.getNode("Details", Constant.punishment_interval).setValue(15);
		rootNode.getNode("Details", Constant.punishment_message).setValue("You are too thirsty now. Drink some water please.");
		
		HashMap<String, Float> biome_map = new HashMap<>();
		
		biome_map.put(BiomeTypeToString(BiomeTypes.BEACH), 1.4f);
		biome_map.put(BiomeTypeToString(BiomeTypes.DESERT), 2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.DESERT_HILLS), 2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.DESERT_MOUNTAINS), 2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.RIVER), 0.8f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SWAMPLAND), 0.9f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SWAMPLAND_MOUNTAINS), 0.9f);
		biome_map.put(BiomeTypeToString(BiomeTypes.JUNGLE), 1.2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.JUNGLE_EDGE), 1.2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.JUNGLE_EDGE_MOUNTAINS), 1.2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.JUNGLE_HILLS), 1.2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.JUNGLE_MOUNTAINS), 1.2f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SAVANNA), 1.7f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SAVANNA_MOUNTAINS), 1.7f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SAVANNA_PLATEAU), 1.7f);
		biome_map.put(BiomeTypeToString(BiomeTypes.SAVANNA_PLATEAU_MOUNTAINS), 1.7f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA), 1.6f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA_BRYCE), 1.6f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA_PLATEAU), 1.6f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA_PLATEAU_FOREST), 1.6f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA_PLATEAU_FOREST_MOUNTAINS), 1.6f);
		biome_map.put(BiomeTypeToString(BiomeTypes.MESA_PLATEAU_MOUNTAINS), 1.6f);
		
		rootNode.getNode("Other", Constant.biome_setting).setValue(HashMapToString(biome_map));
		
		HashMap<String, Float> world_map = new HashMap<>();
		
		world_map.put("DIM-1", 1.5f);
		world_map.put("DIM1", 2f);
		
		rootNode.getNode("Other", Constant.world_setting).setValue(HashMapToString(world_map));
	}
	
	private void save() {
		try {
			loader.save(rootNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, Float> StringToHashMap(String str) {
		HashMap<String, Float> map = new HashMap<>();
		String[] lines = str.split(";");
		int len = lines.length;
		for(int i = 0; i < len; i++) {
			String[] temp = lines[i].split(" ");
			if(temp.length != 2) continue;
			try {
				map.put(temp[0], Float.parseFloat(temp[1]));
			} catch(Exception e) {
				//e.printStackTrace();
				ZThirst.getZThirst().SendTerminalMessage("Error in ZThirst config:");
				ZThirst.getZThirst().SendTerminalMessage(lines[i]);
			}
		}
		return map;
	}
	
	public static String HashMapToString(HashMap<String, Float> map) {
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Float> entry: map.entrySet())
        {
			sb.append(entry.getKey() + " " + String.valueOf(entry.getValue()));
			sb.append(';');
        }
		return sb.toString().replaceAll("\"", "");
	}
	
	public String BiomeTypeToString(BiomeType biome) {
		try {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader file = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
			ConfigurationNode node = file.createEmptyNode();
			node.setValue(TypeToken.of(BiomeType.class), biome);
			file.save(node);
			return sink.toString().replaceAll(System.getProperty("line.separator"),"");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	class PotionEffectList {
		ArrayList<PotionEffectType> ptype;
		ArrayList<Integer> duration;
		ArrayList<Integer> amplifier;
		
		public PotionEffectList(ArrayList<PotionEffectType> p, ArrayList<Integer> d, ArrayList<Integer> a) {
			ptype = p;
			duration = d;
			amplifier = a;
		}
	}
	
	public static PotionEffectType StringToPotionEffectType(String str) {
		return Sponge.getRegistry().getType(PotionEffectType.class, str).get();
	}
	
	public PotionEffectList Potion_StringToArrayList(String str) {
		String[] lines = str.replace('\'', '"').split(";");;
		int len = lines.length;
				
		ArrayList<PotionEffectType> ptype = new ArrayList<>(len);
		ArrayList<Integer> duration = new ArrayList<>(len);
		ArrayList<Integer> amplifier = new ArrayList<>(len);
				
		for(int i = 0; i < len; i++) {
			String[] temp = lines[i].split(" ");
			if(temp.length != 3) continue;
			try {
				ptype.add(StringToPotionEffectType(temp[0]));
				duration.add(Integer.parseInt(temp[1]));
				amplifier.add(Integer.parseInt(temp[2]));
			} catch (Exception e) {
				ZThirst.getZThirst().SendTerminalMessage("Error in ZThirst config:");
				ZThirst.getZThirst().SendTerminalMessage(lines[i]);
				//e.printStackTrace();
			}
		}
		
		return new PotionEffectList(ptype, duration, amplifier);
	}
	
	public static String Potion_ArrayListToString(ArrayList<PotionEffectType> p, ArrayList<Integer> d, ArrayList<Integer> a) {
		StringBuilder sb = new StringBuilder();
		int len = p.size();
		
		for(int i = 0; i < len; i++) {
			sb.append(PotionEffectTypeToString(p.get(i)));
			sb.append(' ');
			sb.append(d.get(i));
			sb.append(' ');
			sb.append(a.get(i));
			sb.append(';');
		}
		
		return sb.toString().replace('"', '\'');
	}
	
	
	public static String PotionEffectTypeToString(PotionEffectType ptype) {
		try {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader file = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
			ConfigurationNode node = file.createEmptyNode();
			node.setValue(TypeToken.of(PotionEffectType.class), ptype);
			file.save(node);
			return sink.toString().replaceAll(System.getProperty("line.separator"),"").replaceAll("\"", "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String ItemTypeToString(ItemType itemtype) {
		try {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader file = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
			ConfigurationNode node = file.createEmptyNode();
			node.setValue(TypeToken.of(ItemType.class), itemtype);
			file.save(node);
			return sink.toString().replaceAll(System.getProperty("line.separator"),"").replaceAll("\"", "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
