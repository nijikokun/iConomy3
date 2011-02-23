package com.nijiko.iConomy.configuration;

import org.bukkit.util.config.Configuration;

/**
 * Handles default configuration and loads data.
 * 
 * @author Nijiko
 */
public class ConfigurationHandler extends DefaultConfiguration {
    private Configuration config;

    public ConfigurationHandler(Configuration config, String database) {
	this.config = config;
	this.database = database;
    }

    public void load() {
	// Database Support
	this.type = this.config.getString("storage.type", this.type);
	this.database = this.config.getString("storage.database", this.database);
	this.user = this.config.getString("storage.user", this.user);
	this.pass = this.config.getString("storage.pass", this.pass);

	// GroupUser Support
	//this.groupusers = this.config.getBoolean("plugin.permissions.use-group-users", this.groupusers);
    }
}
