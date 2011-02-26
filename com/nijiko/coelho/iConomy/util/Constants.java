package com.nijiko.coelho.iConomy.util;

import java.io.File;

import org.bukkit.util.config.Configuration;

public class Constants {
	public static final String Codename = "Kristen";
	
	// Configuration File
	public static File Configuration;

    // Plugin Directory
    // I don't know why I didn't think of this before.
    public static String Plugin_Directory;
	
	// System logging
	public static boolean Log_Data = false;

	// System Data
	public static String Currency = "Coin";
	public static double Initial_Balance = 45.0;

	// Database Type
	public static String Database_Type = "MySQL";

	// Relational SQL Generics
	public static String SQL_Hostname = "localhost";
	public static String SQL_Port = "3306";
	public static String SQL_Username = "root";
	public static String SQL_Password = "";

	// SQL Generics
	public static String SQL_Database = "minecraft";
	public static String SQL_Table = "iConomy";


	public static void load(Configuration config) {
		config.load();
		
		// System Logging
		Log_Data = config.getBoolean("System.Logging.Enabled", Log_Data);

		// System Configuration
		Currency = config.getString("System.Currency", Currency);
		Initial_Balance = config.getDouble("System.Initial_Balance", Initial_Balance);

		// Database Configuration
		Database_Type = config.getString("System.Database.Type", Database_Type);

		// MySQL
		SQL_Hostname = config.getString("System.Database.MySQL.Hostname", SQL_Hostname);
		SQL_Port = config.getString("System.Database.MySQL.Port", SQL_Port);
		SQL_Username = config.getString("System.Database.MySQL.Username", SQL_Username);
		SQL_Password = config.getString("System.Database.MySQL.Password", SQL_Password);

		// SQLite
		SQL_Database = config.getString("System.Database.Name", SQL_Database);
		SQL_Table = config.getString("System.Database.Table", SQL_Table);
	}
}
