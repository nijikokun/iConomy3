/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nijikokun.bukkit.iConomy;

import com.nijiko.iConomy.configuration.PropertyHandler;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author Nijiko
 */
public class Database {

    public enum Type {
        SQLITE,
        MYSQL,
        FLATFILE;
    };

    public Type database = null;

    /*
     * Tip array for less database usage
     */
    public static PropertyHandler accounts;
    public int i = 0;

    public Database(Type database) {
        this.database = database;
        this.initialize();
    }

    private void initialize() {
        if (this.database.equals(database.FLATFILE)) {
            (new File(iConomy.main_directory + "balances.properties")).renameTo(new File(iConomy.flatfile));
            this.accounts = new PropertyHandler(iConomy.flatfile);
        } else {
            if (!checkTable()) {
                iConomy.log.info("[iConomy] Creating database.");
                createTable();
            }
        }
    }

    public HashMap<String, Account> setupBank() {
        HashMap<String, Account> bank = new HashMap<String, Account>();

        if (this.database.equals(database.FLATFILE)) {
            try {
                for (String account : this.accounts.returnMap().keySet()) {
                    Account initialized = new Account(account, this.accounts.getDouble(account));
                    bank.put(account, initialized);
                }
            } catch (Exception ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = this.connection();
                ps = conn.prepareStatement("SELECT * FROM iBalances");
                rs = ps.executeQuery();

                while(rs.next()) {
                    Account initialized = new Account(rs.getString("player"), rs.getDouble("balance"));
                    bank.put(rs.getString("player"), initialized);
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not check balance for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    iConomy.log.severe("[iConomy]: Failed to close connection");
                }
            }
        }

        return bank;
    }

    private Connection connection() throws ClassNotFoundException, SQLException {
        if (this.database.equals(database.SQLITE)) {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(iConomy.sqlite);
        } else {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(iConomy.mysql, iConomy.mysql_user, iConomy.mysql_pass);
        }
    }

    private boolean checkTable() {
        Connection conn = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            conn = this.connection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "iBalances", null);
            result = rs.next();
        } catch (SQLException ex) {
            iConomy.log.severe("[iConomy]: Table check for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + " Failed: " + ex);
            return false;
        } catch (ClassNotFoundException e) {
            iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Failed to close connection");
            }
        }

        return result;
    }

    private void createTable() {
        Connection conn = null;
        Statement st = null;

        try {
            conn = this.connection();
            st = conn.createStatement();

            if (this.database.equals(database.SQLITE)) {
                st.executeUpdate("CREATE TABLE `iBalances` ( `id` INT ( 255 ) PRIMARY KEY , `player` TEXT , `balance` DECIMAL( 255,5 ) NOT NULL); CREATE INDEX playerIndex on iBalances (player);CREATE INDEX balanceIndex on iBalances (balance);");
            } else {
                st.executeUpdate("CREATE TABLE `iBalances` ( `id` INT( 255 ) NOT NULL AUTO_INCREMENT, `player` TEXT NOT NULL ,`balance` DECIMAL( 254,5 ) NOT NULL, PRIMARY KEY ( `id` ), INDEX ( `balance` )) ENGINE = MYISAM;");
            }
        } catch (SQLException ex) {
            iConomy.log.severe("[iConomy]: Could not create table for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
            return;
        } catch (ClassNotFoundException e) {
            iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
            return;
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Failed to close connection");
            }
        }
    }

    @Deprecated
    public boolean has_balance(String name) {
        return hasBalance(name);
    }

    @Deprecated
    public int get_balance(String name) {
        return (int)getBalance(name);
    }

    @Deprecated
    public void set_balance(String name, int balance) {
        setBalance(name, (double)balance);
    }

    @Deprecated
    public void remove_balance(String name) {
        removeBalance(name);
    }

    public boolean hasBalance(String name) {
        boolean has = false;

        if (this.database.equals(database.FLATFILE)) {
            return (this.accounts.getInt(name) != 0) ? true : false;
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = this.connection();
                ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
                ps.setString(1, name);
                rs = ps.executeQuery();
                has = (rs.next()) ? true : false;
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not check balance for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return false;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return false;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    iConomy.log.severe("[iConomy]: Failed to close connection");
                }
            }

            return has;
        }
    }


    public double getBalance(String name) {
        int balance = 0;

        if (this.database.equals(database.FLATFILE)) {
            // To work with plugins we must do this.
            this.accounts.load();

            // Return the balance
            return (has_balance(name)) ? this.accounts.getInt(name) : this.accounts.getDouble(name, iConomy.Bank.getInitial());
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = this.connection();
                ps = conn.prepareStatement("SELECT balance FROM iBalances WHERE player = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
                ps.setString(1, name);
                rs = ps.executeQuery();

                if (rs.next()) {
                    balance = rs.getInt("balance");
                } else {
                    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
                    ps.setString(1, name);
                    ps.setInt(2, balance);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not check balance for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return 0;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return 0;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    iConomy.log.severe("[iConomy]: Failed to close connection");
                }
            }

            return balance;
        }
    }

    public void setBalance(String name, double balance) {
        if (this.database.equals(database.FLATFILE)) {
            this.accounts.getDouble(name, balance);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = this.connection();

                if (has_balance(name)) {
                    ps = conn.prepareStatement("UPDATE iBalances SET balance = ? WHERE player = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
                    ps.setDouble(1, balance);
                    ps.setString(2, name);
                    ps.executeUpdate();
                } else {
                    ps = conn.prepareStatement("INSERT INTO iBalances (player, balance) VALUES(?,?)");
                    ps.setString(1, name);
                    ps.setDouble(2, balance);
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not set balance for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    iConomy.log.severe("[iConomy]: Failed to close connection");
                }
            }
        }
    }

    public void removeBalance(String name) {
        boolean has = false;

        if (this.database.equals(database.FLATFILE)) {
            this.accounts.removeKey(name);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = this.connection();

                ps = conn.prepareStatement("DELETE FROM iBalances WHERE player = ?" + (this.database.equals(database.SQLITE) ? "" : " LIMIT 1"));
                ps.setString(1, name);
                ps.executeUpdate();
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not remove balance for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    iConomy.log.severe("[iConomy]: Failed to close connection");
                }
            }
        }
    }

    public void get_ranked(String checking, Player viewing, boolean mine) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int i = 1;
        if (this.database.equals(database.FLATFILE)) {
            Map balances;
            TreeMap<String, Integer> sorted = null;
            ValueComparator bvc = null;

            try {
                balances = this.accounts.returnMap();
                bvc = new ValueComparator(balances);
                sorted = new TreeMap(bvc);
                sorted.putAll(balances);
            } catch (Exception ex) {
                iConomy.log.info(Messaging.bracketize(iConomy.name + " FlatFile") + " Exception while mapping accounts during ranking: " + ex);
            }

            for (Object key : sorted.keySet()) {
                String name = (String) key;

                if (mine) {
                    if (name.equalsIgnoreCase(checking)) {
                        Messaging.send(
                                viewing,
                                iConomy.template.color("tag")
                                + iConomy.template.parse(
                                "personal-rank",
                                new String[]{"+name,+n", "+rank,+r"},
                                new String[]{checking, Misc.string(i)}));
                        break;
                    }
                } else {
                    if (name.equalsIgnoreCase(checking)) {
                        Messaging.send(
                                viewing,
                                iConomy.template.color("tag")
                                + iConomy.template.parse(
                                "player-rank",
                                new String[]{"+name,+n", "+rank,+r"},
                                new String[]{checking, Misc.string(i)}));

                        break;
                    }
                }

                i++;
            }
        } else {
            try {
                conn = this.connection();

                ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC");
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (mine) {
                        if (rs.getString("player").equalsIgnoreCase(checking)) {
                            Messaging.send(
                                    viewing,
                                    iConomy.template.color("tag")
                                    + iConomy.template.parse(
                                    "personal-rank",
                                    new String[]{"+name,+n", "+rank,+r"},
                                    new String[]{checking, Misc.string(i)}));

                            break;
                        }
                    } else {
                        if (rs.getString("player").equalsIgnoreCase(checking)) {
                            Messaging.send(
                                    viewing,
                                    iConomy.template.color("tag")
                                    + iConomy.template.parse(
                                    "player-rank",
                                    new String[]{"+name,+n", "+rank,+r"},
                                    new String[]{checking, Misc.string(i)}));

                            break;
                        }
                    }

                    i++;
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not rank  for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return;
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }

                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                }
            }
        }
        return;
    }

    public void get_top(int amount) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int i = 1;

        if (this.database.equals(database.FLATFILE)) {
            Map balances;
            TreeMap<String, Integer> sorted = null;
            ValueComparator bvc = null;

            try {
                balances = this.accounts.returnMap();
                bvc = new ValueComparator(balances);
                sorted = new TreeMap(bvc);
                sorted.putAll(balances);
            } catch (Exception ex) {
                iConomy.log.info(Messaging.bracketize(iConomy.name + " FlatFile") + " Exception while mapping accounts during ranking: " + ex);
            }

            Messaging.send(
                    iConomy.template.parse(
                    "top-opening",
                    new String[]{"+amount,+a"},
                    new String[]{Misc.string(amount)}));

            if (sorted.size() < 1) {
                Messaging.send(iConomy.template.color("top-empty"));
                return;
            }

            if (amount > sorted.size()) {
                amount = sorted.size();
            }

            for (Object key : sorted.keySet()) {
                String name = (String) key;
                int balance = Integer.valueOf("" + sorted.get(name));

                if (i <= amount) {
                    Messaging.send(
                            iConomy.template.parse(
                            "top-line",
                            new String[]{"+i,+number", "+name,+n", "+balance,+b"},
                            new String[]{Misc.string(i), name, Misc.formatCurrency(balance, iConomy.currency)}));
                } else {
                    break;
                }

                i++;
            }
        } else {
            try {
                conn = this.connection();

                ps = conn.prepareStatement("SELECT player,balance FROM iBalances ORDER BY balance DESC LIMIT 0,?");
                ps.setInt(1, amount);
                rs = ps.executeQuery();

                Messaging.send(
                        iConomy.template.parse(
                        "top-opening",
                        new String[]{"+amount,+a"},
                        new String[]{Misc.string(amount)}));

                if (rs != null) {
                    while (rs.next()) {
                        Messaging.send(
                                iConomy.template.parse(
                                "top-line",
                                new String[]{"+i,+number", "+name,+n", "+balance,+b"},
                                new String[]{Misc.string(i), rs.getString("player"), iConomy.Misc.formatCurrency(rs.getInt("balance"), iConomy.currency)}));

                        i++;
                    }
                } else {
                    Messaging.send(iConomy.template.color("top-empty"));
                }
            } catch (SQLException ex) {
                iConomy.log.severe("[iConomy]: Could not rank for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + ex);
                return;
            } catch (ClassNotFoundException e) {
                iConomy.log.severe("[iConomy]: Database connector not found for " + (this.database.equals(database.SQLITE) ? "sqlite" : "mysql") + ": " + e);
                return;
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }

                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                }
            }
        }

        return;
    }
}
