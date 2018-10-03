package zhehe.Thirst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

@Plugin(id = ZThirst.PLUGIN_ID, name = ZThirst.PLUGIN_NAME, version = ZThirst.PLUGIN_VERSION, authors = "zhehe")
public class ZThirst {
	public static final String PLUGIN_ID = "zthirst";
	public static final String PLUGIN_NAME = "ZThirst";
	public static final String PLUGIN_VERSION = "1.0";
	
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Inject
	private Logger logger;
	
	@Inject
	private static ZThirst instance;
	
    public Path getconfigDir() {
    	return configDir;
    }
    
    public void SendTerminalMessage(String str) {
    	logger.info(str);
    }
    
    public static ZThirst getZThirst() {
    	return instance;
    }
    
    @Listener
	public void onGamePreInitializationEvent(GamePreInitializationEvent e) {
		instance = this;
		Config.getConfig().init(instance);
		
		logger.info("zhehe's Thirst good to go!");
		// Create Configuration Directory for CustomPlayerCount
		if (!Files.exists(configDir)) {
			try {
				Files.createDirectories(configDir);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		Config.getConfig().init_config();
		logger.info("[ZThirst] Config has been loaded.");
		
		ThirstDB.getThirstDB().init(instance);
		try {
			ThirstDB.getThirstDB().initDB();
		} catch (Exception ex) {
			//ex.printStackTrace();
			logger.info(ex.toString());
		}
		
		//Waterbar.getWaterbar().reload();
		
		Tasker.getTasker().init();
		
		Command.register_command();
		
		Sponge.getEventManager().registerListeners(instance, new Event());
    }    
}
