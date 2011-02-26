package com.nijiko.coelho.iConomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.bukkit.event.Event;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.coelho.iConomy.entity.iPlayerListener;
import com.nijiko.coelho.iConomy.entity.iPluginListener;
import com.nijiko.coelho.iConomy.net.iDatabase;
import com.nijiko.coelho.iConomy.system.Bank;
import com.nijiko.coelho.iConomy.util.Constants;
import com.nijiko.coelho.iConomy.system.Transactions;
import com.nijiko.coelho.iConomy.util.FileManager;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class iConomy extends JavaPlugin {

	private static Server Server = null;
	private static Bank Bank = null;
	private static iDatabase iDatabase = null;
	private static Permissions Permissions = null;
	private static iPlayerListener playerListener = null;
	private static iPluginListener pluginListener = null;
	private static Transactions Transactions = null;
	

	@Override
	public void onEnable() {
        // Create iConomy Directory
        getDataFolder().mkdir();

		// Get the server
		Server = getServer();

        // Directory
        Constants.Plugin_Directory = getDataFolder().getPath();
		
		// Grab plugin details..
		PluginManager pm = Server.getPluginManager();
		PluginDescriptionFile pdfFile = this.getDescription();
		
		// Versioning File
		FileManager file = new FileManager(getDataFolder().getPath(), "VERSION", false);
		
		// Default Files
		setupDefaultFile("iConomy.yml");
		setupDefaultFile("Messages.tpl");
		
		// Configuration
		try {
			Constants.load(new Configuration(new File(getDataFolder(), "iConomy.yml")));
		} catch(Exception e) {
			this.getServer().getPluginManager().disablePlugin(this);
			System.out.println("[iConomy] Failed to retrieve configuration from directory.");
			System.out.println("[iConomy] Please back up your current settings and let iConomy recreate it.");
			return;
		}

        // Create flatfile database
        if(Constants.Database_Type.equalsIgnoreCase("flatfile")) {
            setupDefaultFile("iConomy.flatfile");
        }

		// Load the database
		try {
			iDatabase = new iDatabase();
		} catch(Exception e) {
			this.getServer().getPluginManager().disablePlugin(this);
			System.out.println("[iConomy] Failed to connect to database: " + e.getMessage());
			return; 
		}

		// File Logger
		Transactions = new Transactions();

        try {
            Transactions.load();
        } catch (Exception ex) {
            System.out.println("[iConomy] Could not load transaction logger.");
        }
		
		// Check version details before the system loads
		upgrade(file, Double.valueOf(pdfFile.getVersion()));
		
		// Load the bank system
		try {
			Bank = new Bank();
			Bank.load();
		} catch(Exception e) {
			this.getServer().getPluginManager().disablePlugin(this);
			System.out.println("[iConomy] Failed to load accounts from database: " + e.getMessage());
			return; 
		}

		// Initializing Listeners
		pluginListener = new iPluginListener();
		playerListener = new iPlayerListener(getDataFolder().getPath());
		
		// Event Registration
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);

		// Console Detail
		System.out.println("[iConomy] v" + pdfFile.getVersion() + " ("+ Constants.Codename + ") loaded.");
		System.out.println("[iConomy] Developed By: " + pdfFile.getAuthors());
	}

	@Override
	public void onDisable() {
        for(String account : Bank.getAccounts().keySet()) {
            Bank.getAccount(account).save();
        }

		System.out.println("[iConomy] saved accounts.");
        System.out.println("[iConomy] Has been disabled.");
	}

    private void upgrade(FileManager file, double version) {
		if(file.exists()) {
			file.read();

			try {
				double current = Double.parseDouble(file.getSource());

				if(current != version) {
					file.write(version);
				}
			} catch(Exception e) {
				System.out.println("[iConomy] Invalid version file, deleting to be re-created on next load.");
                file.delete();
			}
		} else {
            // New Version, check for older databases
            if (Constants.Database_Type.equalsIgnoreCase("flatfile")) {

            } else {
                String[] SQL = {};

                String[] MySQL = {
                    "RENAME TABLE ibalances TO " + Constants.SQL_Table + ";",
                    "ALTER TABLE " + Constants.SQL_Table + " CHANGE  player  username TEXT NOT NULL, CHANGE balance balance DECIMAL(65, 2) NOT NULL;"
                };

                String[] SQLite = {
                    "CREATE TABLE '" + Constants.SQL_Table + "' ('id' INT ( 10 ) PRIMARY KEY , 'username' TEXT , 'balance' DECIMAL ( 65 , 2 ));",
                    "INSERT INTO " + Constants.SQL_Table + "(id, username, balance) SELECT id, player, balance FROM ibalances;",
                    "DROP TABLE ibalances;"
                };

                try {
                    DatabaseMetaData dbm = iDatabase.getConnection().getMetaData();
                    ResultSet rs = dbm.getTables(null, null, "ibalances", null);

                    if(rs.next()) {
                        System.out.println(" - Updating " + Constants.Database_Type + " Database for latest iConomy");

                        int i = 1;
                        SQL = (Constants.Database_Type.equalsIgnoreCase("mysql")) ? MySQL : SQLite;

                        for(String Query : SQL) {
                            iDatabase.executeQuery(Query);

                            System.out.println("   Executing SQL Query #" + i + " of " + (SQL.length));
                            ++i;
                        }

                        System.out.println(" + Database Update Complete.");
                    }
                } catch (SQLException e) {
                    System.out.println("[iConomy] Error updating database: " + e);
                }
            }

            // Create file
			file.create();
			file.write(version);
		}
    }
	
    private void setupDefaultFile(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            
            InputStream input = this.getClass().getResourceAsStream("/default/" + name);
            if (input != null) {
                FileOutputStream output = null;
                
                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    
                    System.out.println("[iConomy] Default setup file written: " + name);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (Exception e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (Exception e) {
                    	
                    }
                }
            }
        }
    }

	public static Bank getBank() {
		return Bank;
	}
	
	public static iDatabase getDatabase() {
		return iDatabase;
	}
	
	public static Transactions getTransactions() {
		return Transactions;
	}

	@SuppressWarnings("static-access")
	public static PermissionHandler getPermissions() {
		return Permissions.Security;
	}
	
	public static boolean setPermissions(Permissions permissions) {
		if(Permissions == null)
			Permissions = permissions;
		else
			return false;
		return true;
	}
	
	public static Server getBukkitServer() {
		return Server;
	}

}
