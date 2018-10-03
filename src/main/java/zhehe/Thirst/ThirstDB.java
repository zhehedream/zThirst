package zhehe.Thirst;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class ThirstDB {
	private static ThirstDB db = new ThirstDB();
	private static ZThirst zthirst;
	
	public static String TABLE_NAME = "ZThirst_Player_Thirst";
	
	private Path dbFile;
	
	private ThirstDB() {
		;
	}
	
	public static ThirstDB getThirstDB() {
		return db;
	}
	
	private SqlService sql;
	
	public DataSource getDataSource(String jdbcUrl) throws SQLException {
	    if (sql == null) {
	        sql = Sponge.getServiceManager().provide(SqlService.class).get();
	    }
	    return sql.getDataSource(jdbcUrl);
	}
	
	String uri ="";
	
	public void init(ZThirst in) {
		zthirst = in;
		Path configDir = zthirst.getconfigDir();
		dbFile = Paths.get(configDir + "/player_thirst");
	}
	
	public void initDB() throws SQLException {
		zthirst.SendTerminalMessage("[ZThirst] Connecting to the Thirst DataBase...");
		uri = "jdbc:h2:" + dbFile.toString();
		zthirst.SendTerminalMessage("[ZThirst] H2 DB Path: " + uri);
		try (
			Connection conn = getDataSource(uri).getConnection();
			PreparedStatement stmt = conn.prepareStatement("create table IF NOT EXISTS ZTHIRST ( `id` bigint(20) NOT NULL AUTO_INCREMENT, `user` varchar(255), `thirst` int(10), `water` int(10), PRIMARY KEY (`id`), UNIQUE KEY `user` (`user`))");
			) {
			stmt.executeUpdate();
		} catch (Exception e) {
			zthirst.SendTerminalMessage("[ZThirst] Cannot connect to database. ZThirst is disabled.");
			zthirst.SendTerminalMessage(e.toString());
		}
	}
	
	public int[] register_Player_Thirst(Player p, int init_thirst) throws SQLException {
		String uuid = p.getUniqueId().toString();
		int[] result = {10000, 0};
		try (
			Connection conn = getDataSource(uri).getConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT IFNULL((select 1 from ZTHIRST where `user`='" + uuid + "' limit 1),0)");
			) {
			ResultSet rs = stmt.executeQuery();
			rs.next();
			if(rs.getInt(1) == 1) {
				PreparedStatement stmt1 = conn.prepareStatement("SELECT * FROM ZTHIRST WHERE `user` = '" + uuid + "'");
				rs = stmt1.executeQuery();
				rs.next();
				result[0] = rs.getInt(3);
				result[1] = rs.getInt(4);
			} else {
				PreparedStatement stmt2 = conn.prepareStatement("insert into ZTHIRST (`user`, `thirst`, `water`) VALUES ('" + uuid + "'," + String.valueOf(init_thirst) + ", 0)");
				stmt2.execute();
				result[0] = init_thirst;
				result[1] = 0;
			}
		} catch (Exception e) {
			zthirst.SendTerminalMessage("ERROR @ register_Player_Thirst with Player: " + p.getName());
			zthirst.SendTerminalMessage(e.toString());
			result[0] = 10000;
			result[1] = 0;
		}
		return result;
	}
	
	public void set_Player_Thirst(Player p, int thirst, int water) {
		String uuid = p.getUniqueId().toString();
		try (
			Connection conn = getDataSource(uri).getConnection();
			PreparedStatement stmt = conn.prepareStatement("UPDATE ZTHIRST SET `thirst` = " + String.valueOf(thirst) + ", `water` = " + String.valueOf(water) + " WHERE `user` = '" + uuid + "'");
			) {
				stmt.execute();
		} catch (Exception e) {
			zthirst.SendTerminalMessage("ERROR @ update_Player_Thirst");
			zthirst.SendTerminalMessage(e.toString());
		}
	}
}
