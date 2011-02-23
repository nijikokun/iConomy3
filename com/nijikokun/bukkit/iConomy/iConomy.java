package com.nijikokun.bukkit.iConomy;

import com.nijiko.iConomy.configuration.PropertyHandler;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * iConomy v2.0 - Official `LightWeight` Version
 * Copyright (C) 2011  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class iConomy extends JavaPlugin {
    /*
     * Loggery Foggery
     */
    public static final Logger log = Logger.getLogger("Minecraft");

    /*
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "iConomy";
    public static String codename = "Shiva";
    public static String version = "3.0";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static PermissionHandler Permissions;

    /*
     * Data locations
     */
    public static String temp_directory = "templates" + File.separator;
    public static String main_directory = "iConomy" + File.separator;
    public static String log_directory = "logs" + File.separator;

    /**
     * JSON Parser, Information Logging.
     */
    public static JSON Info;

    /**
     * Internal Properties controllers
     */
    public static PropertyHandler Settings, Logging;

    /**
     * Template object
     */
    public static Template template;

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /*
     * Variables
     */
    public static String currency;
    public static double initialBalance, takeAmount, giveAmount;
    public static int takeInterval, giveInterval;
    private boolean debugging;
    public static Timer give = null, take = null;
    public static Server Server = null;

    /*
     * Databases
     */
    private String database_type = "flatfile";
    public static String flatfile = main_directory + "accounts.flat";
    public static String sqlite = "jdbc:sqlite:" + main_directory + "iConomy.sqlite";
    public static String mysql = "jdbc:mysql://localhost:3306/minecraft";
    public static String mysql_user = "root";
    public static String mysql_pass = "pass";

    /*
     * Database connection
     */
    public static Database database = null;
    public static Bank Bank = null;

    public iConomy(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onDisable() {
        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") un-loaded");
    }

    public void onEnable() {
        registerEvents();
        grabServer();
        setup();
        setupTemplate();
        setupTimers();

        log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") loaded");
    }

    private void grabServer() {
        Server = this.getServer();
    }

    private Listener Listener = new Listener();

    private class Listener extends ServerListener {

        public Listener() {
        }

        @Override
        public void onPluginEnabled(PluginEvent event) {
            if(event.getPlugin().getDescription().getName().equals("Permissions")) {
                iConomy.Permissions = ((Permissions)event.getPlugin()).Security;
                log.info(Messaging.bracketize(name) + " Attached plugin to Permissions. Enjoy~");
            }
        }
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, Listener, Priority.Monitor, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, l, Priority.Normal, this);
    }

    private void setup() {
        // Create directory if it doesn't exist.
        (new File(main_directory)).mkdir();
        (new File(main_directory + log_directory)).mkdir();
        (new File(main_directory + temp_directory)).mkdir();

        // File Data
        (new File(main_directory + "settings.properties")).renameTo(new File(main_directory + "iConomy.settings"));
        (new File(main_directory + "balances.properties")).renameTo(new File(main_directory + "accounts.flat"));
        Settings = new PropertyHandler(main_directory + "iConomy.settings");
        Logging = new PropertyHandler(main_directory + "logging.settings");

        // Templating
        template = new Template("money.tpl");

        // Logging
        Info = new JSON(main_directory + log_directory, "pay.log", Logging.getBoolean("log-pay", false));

        // Money Settings
        currency = Settings.getString("money-name", "Coin");
        initialBalance = Settings.getInt("starting-balance", 0);

        // Timer Settings
        takeInterval = Settings.getInt("money-take-interval", 0);
        giveInterval = Settings.getInt("money-give-interval", 300);
        takeAmount = Settings.getInt("money-take-amount", 0);
        giveAmount = Settings.getInt("money-give-amount", 1);

        // Boolean Variables
        debugging = Settings.getBoolean("debugging", false);

        // Database
        database_type = Settings.getString("database-type", "flatfile");

        // Connect & Create
        if (database_type.equalsIgnoreCase("mysql")) {
            mysql = Settings.getString("mysql-db", mysql);
            mysql_user = Settings.getString("mysql-user", mysql_user);
            mysql_pass = Settings.getString("mysql-pass", mysql_pass);

            database = new Database(Database.Type.MYSQL);
        } else if (database_type.equalsIgnoreCase("sqlite")) {
            database = new Database(Database.Type.SQLITE);
        } else {
            database = new Database(Database.Type.FLATFILE);
        }

        Bank = new Bank(currency, initialBalance, giveAmount, takeAmount, giveInterval, takeInterval, database.setupBank());
    }

    private void setupTemplate() {
        template.raw("tag", "<green>[<white>Money<green>] ");
        template.raw("personal-balance", "<green>Balance: <white>+balance");
        template.raw("player-balance", "<green>+name's Balance: <white>+balance");
        template.raw("personal-rank", "<green>Current rank: <white>+rank");
        template.raw("player-rank", "<green>+name's rank: <white>+rank");
        template.raw("top-opening", "<green>Top <white>+amount<green> Richest Players:");
        template.raw("top-line", "<white>   +i.<green> +name <white>(<green>+balance<white>)");
        template.raw("top-empty", "<white>   Nobody yet!");
        template.raw("payment-to", "<green>You have sent <white>+amount<green> to <white>+name<green>.");
        template.raw("payment-from", "<white>+name<green> has sent you <white>+amount<green>.");
        template.raw("personal-reset", "<rose>Your account has been reset.");
        template.raw("player-reset", "<white>+name's <rose>account has been reset.");
        template.raw("personal-withdraw", "<rose>Your account had <white>+amount<rose> withdrawn.");
        template.raw("player-withdraw", "<white>+name's <rose>account had <white>+amount<rose> withdrawn.");
        template.raw("personal-deposited", "<white>+amount<green> was deposited into your account.");
        template.raw("player-deposited", "<white>+name's <green>account had <white>+amount<green> deposited into it.");
        template.raw("payment-self", "<rose>Sorry, you cannot send money to yourself.");
        template.raw("no-funds", "<rose>Sorry, you do not have enough funds to do that.");
        template.raw("no-account", "<rose>Player does not have account: <white>+name");
    }

    private void setupTimers() {
        if (giveInterval != 0) {
            give = new Timer();
            give.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                    for (Player player : Server.getOnlinePlayers()) {
                        String name = player.getName();

                        if(Bank.hasAccount(name)) {
                            Account modifying = Bank.getAccount(name);
                            modifying.add(Bank.getIncremental_give());
                            modifying.save();
                        } else {
                            Bank.addAccount(name);

                            Account modifying = Bank.getAccount(name);
                            modifying.add(Bank.getIncremental_give());
                            modifying.save();
                        }
                    }
                }
            }, 0L, (Bank.getIncremental_give_time() * 1000L));
        }

        if (takeInterval != 0) {
            take = new Timer();
            take.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                    for (Player player : Server.getOnlinePlayers()) {
                        String name = player.getName();

                        if(Bank.hasAccount(name)) {
                            Account modifying = Bank.getAccount(name);
                            modifying.add(Bank.getIncremental_take());
                            modifying.save();
                        } else {
                            Bank.addAccount(name);

                            Account modifying = Bank.getAccount(name);
                            modifying.add(Bank.getIncremental_take());
                            modifying.save();
                        }
                    }
                }
            }, 0L, (Bank.getIncremental_take_time() * 1000L));
        }
    }

    public static Database getDatabase() {
        return database;
    }
}
