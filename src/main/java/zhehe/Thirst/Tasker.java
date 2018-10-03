package zhehe.Thirst;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.scheduler.Task;

public class Tasker {
	private Task waterbar_task;
	private Task Reg_task;
	private Task Consume_task;
	private Task DB_task;
	private Task TooThirst_task;
	
	private static Tasker tasker = new Tasker();
	private Tasker() {
		;
	}
	
	public static Tasker getTasker() {
		return tasker;
	}
	
	public void init() {
		long tick1 = Config.getConfig().getWaterBarRefreshInterval();
		waterbar_task = Task.builder().execute(() -> executeWaterBarTask())
			    .intervalTicks(tick1)
			    .name("WaterBar Updater").submit(ZThirst.getZThirst());
		
		long time2 = Config.getConfig().getDataBaseRefreshInterval();
		Reg_task = Task.builder().execute(() -> executeRegTask())
			    .interval(time2, TimeUnit.MICROSECONDS).async()
			    .name("DataBase Updater").submit(ZThirst.getZThirst());
		
		//long tick3 = Config.getConfig().getConsumeInterval();
		Consume_task = Task.builder().execute(() -> executeWaterConsumeTask())
			    .intervalTicks(20)
			    .name("Water Consume Task").submit(ZThirst.getZThirst());
		
		long time4 = Config.getConfig().getDataBaseRefreshInterval();
		DB_task = Task.builder().execute(() -> executeDBTask())
			    .interval(time4, TimeUnit.SECONDS).async()
			    .name("Water Consume Task").submit(ZThirst.getZThirst());
		
		int time5 = Config.getConfig().getPunishmentInterval();
		TooThirst_task = Task.builder().execute(() -> executePunishment())
			    .interval(time5, TimeUnit.SECONDS)
			    .name("Punishment Task").submit(ZThirst.getZThirst());
	}
	
	void reload() {
		if(waterbar_task != null) waterbar_task.cancel();
		if(Reg_task != null) Reg_task.cancel();
		if(Consume_task != null) Consume_task.cancel();
		if(DB_task != null) DB_task.cancel();
		if(TooThirst_task != null) TooThirst_task.cancel();
		init();
	}
	
	private void executeWaterBarTask() {
		Waterbar.getWaterbar().waterbarUpdate();
	}
	
	private void executeRegTask() {
		Waterbar.getWaterbar().prepare();
	}
	
	private void executeWaterConsumeTask() {
		Waterbar.getWaterbar().playerWaterConsume();
	}
	
	private void executeDBTask() {
		Waterbar.getWaterbar().writeDB();
	}
	private void executePunishment() {
		Waterbar.getWaterbar().PunishmentAll();
	}
}
