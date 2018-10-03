package zhehe.Thirst;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.living.player.Player;

public class ThirstPotionEffect {
	private ThirstPotionEffect() {
		;
	}
	
	private static ThirstPotionEffect pe = new ThirstPotionEffect();
	
	public static ThirstPotionEffect getThirstPotionEffect() {
		return pe;
	}
	
	public void addPositiveEffect(Player p) {
		PotionEffectData effectdata = p.getOrCreate(PotionEffectData.class).get();
		
		ArrayList<PotionEffectType> positive_pe = Config.getConfig().positive_p;
		ArrayList<Integer> duration = Config.getConfig().positive_d;
		ArrayList<Integer> amplifier = Config.getConfig().positive_a;
		int len = positive_pe.size();
		
		for(int i = 0; i < len; i++) {
			PotionEffectType effect = positive_pe.get(i);
			PotionEffect potion = PotionEffect.builder()
					.potionType(effect)
					.duration(duration.get(i) * 20)
					.amplifier(amplifier.get(i))
					.build();
			effectdata.addElement(potion);
		}
		
		p.offer(effectdata);
	}
	
	public void addNegativeEffect(Player p) {
		PotionEffectData effectdata = p.getOrCreate(PotionEffectData.class).get();
		
		ArrayList<PotionEffectType> negative_pe = Config.getConfig().negative_p;
		ArrayList<Integer> duration = Config.getConfig().negative_d;
		ArrayList<Integer> amplifier = Config.getConfig().negative_a;
		int len = negative_pe.size();
		
		for(int i = 0; i < len; i++) {
			//p.sendMessage(Text.of("Effect: " + Config.PotionEffectTypeToString(negative_pe.get(i))));
			PotionEffectType effect = negative_pe.get(i);
			PotionEffect potion = PotionEffect.builder()
					.potionType(effect)
					.duration(duration.get(i) * 20)
					.amplifier(amplifier.get(i))
					.build();
			effectdata.addElement(potion);
		}
		
		p.offer(effectdata);
	}
	
	public ArrayList<String> getPlayerEffectList(Player p) {
		PotionEffectData effectdata = p.getOrCreate(PotionEffectData.class).get();
		
		List<PotionEffect> temp = effectdata.asList();
		int len = temp.size();
		
		ArrayList<String> result = new ArrayList<>(len);
		
		for(int i = 0; i < len; i++) {
			Config.getConfig();
			result.add(Config.PotionEffectTypeToString(temp.get(i).getType()));
		}
		
		return result;
	}
}
