package zhehe.Thirst;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Waterbar {
	
	private class WaterbarStruct {
		public ServerBossBar bar;
		public boolean show;
		public long unix_time;
		public WaterbarStruct(ServerBossBar i) {
			bar = i;
			show = false;
			unix_time = 0;
		}
	}
	
	private Map<Player, WaterbarStruct> waterbar = new ConcurrentHashMap<>();
	private Map<Player, Integer> thirst = new ConcurrentHashMap<>();
	private Map<Player, Integer> thirst_small = new ConcurrentHashMap<>();
	private Queue<Player> need_register = new ConcurrentLinkedQueue<>();
	private Queue<Player> need_remove = new ConcurrentLinkedQueue<>();
	
	private Map<Player, Integer> online_player = new ConcurrentHashMap<>();
	
	//private int init_thirst = 10000;
	
	public int getPlayerThirst(Player p) {
		if(thirst.containsKey(p)) return thirst.get(p);
		else return -1;
	}
	
	//public void reload() {
		//init_thirst = Config.getConfig().getInitialThirst();
	//}
    
    private Waterbar() {
    	;
    }
    
    private static Waterbar instance = new Waterbar();
    
    public static Waterbar getWaterbar() {
    	return instance;
    }
    
    public void removePlayer(Player p) {
    	//waterbar.remove(p);
    	//thirst.remove(p);
    	//thirst_small.remove(p);
    	
    	//online_player.remove(p);
    	
    	need_remove.offer(p);
    }
    
    public void registerPlayer(Player p, int init_thirst) {
    	if(!online_player.containsKey(p)) return;
    	ThirstDB db = ThirstDB.getThirstDB();
    	try {
    		int[] thirstdb = db.register_Player_Thirst(p, init_thirst);
    		float percent = thirstdb[0] / 10000f;
    		ServerBossBar bossbar = ServerBossBar.builder()
					.name(Text.of(TextColors.DARK_AQUA, "Thirst"))
					.color(BossBarColors.BLUE)
		    		.overlay(BossBarOverlays.NOTCHED_12)
		    		.percent(percent)
		    		.build();
    		waterbar.put(p, new WaterbarStruct(bossbar));
    		
    		thirst.put(p, thirstdb[0]);
    		thirst_small.put(p, thirstdb[1]);
    		
    	} catch (Exception ex) {
    		ZThirst.getZThirst().SendTerminalMessage("ERROR: Cannot register player: " + p.getName());
    	}
    }
    
    public void addPlayer(Player p) {
    	need_register.offer(p);
    	online_player.put(p, 0);
    }
    
    private void registerAll() {
    	int init_thirst = Config.getConfig().getInitialThirst();
    	while(!need_register.isEmpty()) {
    		Player p = need_register.poll();
    		registerPlayer(p, init_thirst);
    	}
    }
    
    private void gcAll() {
    	while(!need_remove.isEmpty()) {
    		Player p = need_remove.poll();
    		waterbar.remove(p);
        	thirst.remove(p);
        	thirst_small.remove(p);
    	}
    }
    
    public void prepare() { //async, 500ms
    	registerAll();
    	gcAll();
    }
    
    public void waterbarUpdate() { //sync, user define
    	for(Map.Entry<Player, WaterbarStruct> entry: waterbar.entrySet()) {
			int thirst_t = thirst.get(entry.getKey());
			float percent = thirst_t / 10000f;
			WaterbarStruct temp = entry.getValue();
			temp.bar.setPercent(percent);
			if(!temp.show) {
				temp.show = true;
				temp.bar.addPlayer(entry.getKey());
			}
		}
    }
    
    public void writeDB() { //async, user define
    	for(Map.Entry<Player, Integer> entry: thirst.entrySet()) {
    		Player p = entry.getKey();
    		int t1 = entry.getValue(), t2 = thirst_small.get(p);
    		ThirstDB.getThirstDB().set_Player_Thirst(p, t1, t2);
    	}
    }
    
    public void writeDBPlayer(Player p) {
    	if(!thirst.containsKey(p)) return;
		int t1 = thirst.get(p), t2 = thirst_small.get(p);
		ThirstDB.getThirstDB().set_Player_Thirst(p, t1, t2);
    }
    
    public void playerWaterConsume() { //sync, every 1s
    	HashMap<String, Float> biomes = Config.getConfig().biome_list;
    	HashMap<String, Float> worlds = Config.getConfig().world_list;
    	int tick = Config.getConfig().getConsumeInterval();
    	int waterminus = Config.getConfig().getConsumeValue();
    	boolean ignore_admin = Config.getConfig().ignore_createmode();
    	for(Map.Entry<Player, Integer> entry: thirst.entrySet()) {
    		Player p = entry.getKey();
    		if(ignore_admin && p.gameMode().get() == GameModes.CREATIVE) {
    			continue;
    		}
    		String biome = Config.getConfig().BiomeTypeToString(p.getLocation().getBiome());
    		String world = p.getWorld().getName();
    		    		
    		int thirst_small_int = thirst_small.get(p);
    		thirst_small_int = thirst_small_int + 1;
    		
    		if(thirst_small_int > tick) {
        		float mult = 1f;
        		if(biomes.containsKey(biome)) mult = mult * biomes.get(biome);
        		if(worlds.containsKey(world)) mult = mult * worlds.get(world);
        		
        		int thirst_int = thirst.get(p);
        		thirst_int = thirst_int - ( (int) (waterminus * mult) ) * ( thirst_small_int / tick );
        		thirst_small_int = thirst_small_int % tick;
        		
        		thirst.put(p, thirst_int);
    		}
    		
    		thirst_small.put(p, thirst_small_int);
    	}
    }
    
    public void thirstSet(Player p, int t) {
    	ThirstDB.getThirstDB().set_Player_Thirst(p, t, 0);
    	
    	if(thirst.containsKey(p)) {
    		thirst.put(p, t);
    	}
    	if(thirst_small.containsKey(p)) {
    		thirst_small.put(p, 0);
    	}
    	if(waterbar.containsKey(p)) {
    		waterbar.get(p).bar.setPercent(t / (float) 10000);
    	}
    	
    	ThirstDB.getThirstDB().set_Player_Thirst(p, t, 0);
    }
    
    public boolean addThirst(Player p, int t) {
    	if(thirst.containsKey(p)) {
    		//p.sendMessage(Text.of("add thirst"));
    		int thirst_int = thirst.get(p);
    		thirst_int += t;
    		thirst_int = (thirst_int < 10000) ? thirst_int : 10000;
    		thirst.put(p, thirst_int);
    		waterbar.get(p).bar.setPercent(thirst_int / 10000f);
    		if(thirst_int >= 10000) return true;
    		else return false;
    	} else return false;
    }
    
    public void addEffect(Player p) {
    	if(!waterbar.containsKey(p)) return;
    	if(Config.getConfig().ignore_createmode() && p.gameMode().get() == GameModes.CREATIVE) return;
    	long current_time = System.currentTimeMillis();
    	long unix = waterbar.get(p).unix_time;
    	if(current_time - unix >= Config.getConfig().getBonusCoolDown()) {
    		ThirstPotionEffect.getThirstPotionEffect().addPositiveEffect(p);
    		waterbar.get(p).unix_time = current_time;
    	}
    }
    
    public void PunishmentAll() {
    	boolean ignore_creativemode = Config.getConfig().ignore_createmode();
    	int threshold = Config.getConfig().getPunishmentThirstThreshold();
    	Text warning = Config.getConfig().getPunishmentMessage();
    	ThirstPotionEffect tpe = ThirstPotionEffect.getThirstPotionEffect();
    	for(Map.Entry<Player, Integer> entry: thirst.entrySet()) {
    		Player p = entry.getKey();
    		if(ignore_creativemode && p.gameMode().get() == GameModes.CREATIVE) continue;
    		int temp = entry.getValue();
    		if(temp < threshold) {
    			tpe.addNegativeEffect(p);
    			p.sendMessage(warning);
    		}
    	}
    }
}
