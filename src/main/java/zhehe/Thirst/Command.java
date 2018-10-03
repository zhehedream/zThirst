package zhehe.Thirst;

import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.biome.BiomeType;

public class Command {
    public static void register_command() {
		CommandSpec thirstStatusCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the thirst status."))
			    .permission("zthirst.status")
			    .executor(new Command.ThirstStatus())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstStatusCommandSpec, "thirststatus", "thirstst");
		
		CommandSpec thirstStatusOfCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the thirst status of player."))
			    .permission("zthirst.status_of_other")
			    .arguments(
			    		GenericArguments.onlyOne(GenericArguments.player(Text.of("player")))
			    )
			    .executor(new Command.ThirstStatusOf())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstStatusOfCommandSpec, "thirststatusof", "thirststatof");
		
		CommandSpec thirstSetCommandSpec = CommandSpec.builder()
			    .description(Text.of("Set the thirst status of player."))
			    .permission("zthirst.set")
			    .arguments(
			    		GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
			    		GenericArguments.onlyOne(GenericArguments.integer(Text.of("thirst")))
			    )
			    .executor(new Command.ThirstSet())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstSetCommandSpec, "thirstset");
		
		CommandSpec thirstCurrentBiomeCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the biome name of the position of player."))
			    .permission("zthirst.getbiome")
			    .executor(new Command.ThirstCurrentBiome())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstCurrentBiomeCommandSpec, "thirstbiome");
		
		CommandSpec thirstCurrentWorldCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the world name of the position of player."))
			    .permission("zthirst.getworld")
			    .executor(new Command.ThirstCurrentWorld())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstCurrentWorldCommandSpec, "thirstworld");
		
		CommandSpec thirstWorldListCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the world config list."))
			    .permission("zthirst.worldlist")
			    .executor(new Command.ThirstWorldList())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstWorldListCommandSpec, "thirstworldlist", "thirstwl");
		
		CommandSpec thirstBiomeListCommandSpec = CommandSpec.builder()
			    .description(Text.of("Get the biome list."))
			    .permission("zthirst.biomelist")
			    .executor(new Command.ThirstBiomeList())
			    .build();

		Sponge.getCommandManager().register(ZThirst.getZThirst(), thirstBiomeListCommandSpec, "thirstbiomelist", "thirstbl");

    }

	public static class ThirstStatusOf implements CommandExecutor {

	    @Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    	Player player = args.<Player>getOne("player").get();
	    	int thirst = Waterbar.getWaterbar().getPlayerThirst(player);
	    	src.sendMessage(Text.of("Thirst: " + String.valueOf(thirst)));
	    	
	        return CommandResult.success();
	    }	    
	}
	
	public static class ThirstStatus implements CommandExecutor {

	    @Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    	if (src instanceof Player) {
	    		Player player = (Player) src;
	    		if(Config.getConfig().ignore_createmode() && player.gameMode().get() == GameModes.CREATIVE) {
	    			src.sendMessage(Text.of("You are in CREATIVE, so your thirst value will never change."));
	    		}
	    		int thirst = Waterbar.getWaterbar().getPlayerThirst(player);
	    		src.sendMessage(Text.of("Thirst: " + String.valueOf(thirst)));
	    	} else {
	    		src.sendMessage(Text.of("Sorry, but only player can use that."));
	    	}
	    	
	        return CommandResult.success();
	    }
	}
	
	public static class ThirstSet implements CommandExecutor {

	    @Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
	    	Player p = args.<Player>getOne("player").get();
	    	int thirst = args.<Integer>getOne("thirst").get();
	    	
	    	Waterbar.getWaterbar().thirstSet(p, thirst);
	    	src.sendMessage(Text.of("Done."));
	    	
	        return CommandResult.success();
	    }
	}
	
	public static class ThirstCurrentBiome implements CommandExecutor {
		
		@Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
	    		Player player = (Player) src;
	    		String biome = Config.getConfig().BiomeTypeToString(player.getLocation().getBiome());
	    		src.sendMessage(Text.of(biome));
			} else {
				src.sendMessage(Text.of("Sorry, but only player can use that."));
			}
	    	
	        return CommandResult.success();
	    }
	}
	
public static class ThirstBiomeList implements CommandExecutor {
		
		@Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			StringBuilder sb = new StringBuilder();
			for (BiomeType biome : Sponge.getGame().getRegistry().getAllOf(BiomeType.class)) {
	            //list.add(new BaseBiome(IDHelper.resolve(biome)));
				sb.append(" #");
				sb.append(Config.getConfig().BiomeTypeToString(biome));
	        }
			src.sendMessage(Text.of(sb.toString()));
	    	
	        return CommandResult.success();
	    }
	}
	
	public static class ThirstCurrentWorld implements CommandExecutor {
		
		@Override
	    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
	    		Player player = (Player) src;
	    		src.sendMessage(Text.of(player.getWorld().getName()));
			} else {
				src.sendMessage(Text.of("Sorry, but only player can use that."));
			}
	    	
	        return CommandResult.success();
	    }
	}
	
	public static class ThirstWorldList implements CommandExecutor {
		
		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			Map<String, Float> worlds = Config.getConfig().world_list;
			src.sendMessage(Text.of("world\t\tvalue"));
			for(Map.Entry<String, Float> entry : worlds.entrySet()) {
				String temp = entry.getKey() + "\t\t" + Float.toString(entry.getValue());
				src.sendMessage(Text.of(temp));
			}
			src.sendMessage(Text.of("========"));
	        return CommandResult.success();
	    }
	}
	
	

}
