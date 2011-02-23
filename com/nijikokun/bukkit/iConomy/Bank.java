/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nijikokun.bukkit.iConomy;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Nijiko
 */
public class Bank {
    private String currency;
    private double initial;
    private double incremental_give, incremental_take;
    private int incremental_give_time, incremental_take_time;
    private HashMap<String, Account> accounts;

    public Bank(String currency, double initial, double incremental_give, double incremental_take, int incremental_give_time, int incremental_take_time, HashMap<String, Account> accounts) {
        this.currency = currency;
        this.initial = initial;
        this.incremental_give = incremental_give;
        this.incremental_take = incremental_take;
        this.incremental_give_time = incremental_give_time;
        this.incremental_take_time = incremental_take_time;
        this.accounts = accounts;
    }
    
    /**
     * Formats the balance in a human readable form with the currency attached:<br /><br />
     * 20000.53 = 20,000.53 Coin<br />
     * 20000.00 = 20,000 Coin
     * 
     * @param account The name of the account you wish to be formatted
     * @return String
     */
    public String format(String account) {
        return this.getAccount(account).toString() + " " + this.currency;
    }

    /**
     * Formats the money in a human readable form with the currency attached:<br /><br />
     * 20000.53 = 20,000.53 Coin<br />
     * 20000.00 = 20,000 Coin
     *
     * @param amount double
     * @return String
     */
    public String format(double amount) {
        NumberFormat formatter = new DecimalFormat("#,##0.##");
        String formatted = formatter.format(amount);

        if(formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length()-1);
        }

        return formatted + " " + this.currency;
    }

    /**
     * Grab the hashmap of all accounts relationed with account names.
     *
     * @return HashMap - All accounts existing in the bank.
     */
    public HashMap<String, Account> getAccounts() {
        return accounts;
    }

    /**
     * Does the bank have record of the account in question?
     *
     * @param account The account in question
     * @return boolean - Does the account exist?
     */
    public boolean hasAccount(String account) {
        return accounts.containsKey(account);
    }

    /**
     * Fetch the account, Does not check for existance.
     * Do that prior to using this to prevent null errors or any other issues.
     *
     * @param account The account to grab
     * @return Account - Child object of bank
     */
    public Account getAccount(String accountName) {
        return accounts.get(accountName);
    }

    /**
     * Get the current currency name.
     * 
     * @return String - Currency name
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Get the incremental give amount (Money)
     * 
     * @return double
     */
    public double getIncremental_give() {
        return incremental_give;
    }

    /**
     * Get the incremental give time in-between each deposit (Time)
     * 
     * @return int
     */
    public int getIncremental_give_time() {
        return incremental_give_time;
    }

    /**
     * Get the incremental take amount (Money)
     * 
     * @return double
     */
    public double getIncremental_take() {
        return incremental_take;
    }

    /**
     * Get the incremental take time in-between each withdraw (Time)
     * 
     * @return int
     */
    public int getIncremental_take_time() {
        return incremental_take_time;
    }

    /**
     * Get the initial balance amount upon creation of new accounts.
     * 
     * @return double
     */
    public double getInitial() {
        return initial;
    }

    /**
     * Set the entire accounts hashmap. 
     * Warning, only use if you are an advanced java user.
     * This could alter many... many... things.
     * 
     * @param accounts
     */
    public void setAccounts(HashMap<String, Account> accounts) {
        this.accounts = accounts;
    }

    /**
     * Set the currency name, only do so if the server owner is knowingly allowing you to do this.
     * 
     * @param currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Changes the incremental give amount (Money)
     * 
     * @param incremental_give double The amount of money given per interval
     */
    public void setIncremental_give(double incremental_give) {
        this.incremental_give = incremental_give;
    }

    /**
     * Changes the incremental give time in-between each deposit (Time in Seconds)
     * 
     * @param incremental_give_time int Time in seconds
     */
    public void setIncremental_give_time(int incremental_give_time) {
        this.incremental_give_time = incremental_give_time;
    }

    /**
     * Changes the incremental take amount (Money)
     * 
     * @param incremental_take double The amount of money withdrawn per interval
     */
    public void setIncremental_take(double incremental_take) {
        this.incremental_take = incremental_take;
    }
    
    /**
     * Changes the incremental take time in-between each withdraw (Time in Seconds)
     * 
     * @param incremental_take_time int Time in seconds
     */
    public void setIncremental_take_time(int incremental_take_time) {
        this.incremental_take_time = incremental_take_time;
    }

    /**
     * Changes the initial balance amount upon account creation.
     * 
     * @param initial
     */
    public void setInitial(double initial) {
        this.initial = initial;
    }
    
    /**
     * Add an account to the bank, if it already exists it updates the balance.
     * This does not utilize the initial balance setting. If you want to do that make sure you
     * grab the initial balance and put it as the second parameter.
     * 
     * @param account
     * @param balance
     */
    public void addAccount(String account) {
        if(!this.hasAccount(account)) {
            Account initialized = new Account(account, this.initial);
            iConomy.getDatabase().setBalance(account, this.initial);
            this.accounts.put(account, initialized);
        } else {
            this.getAccount(account).setBalance(initial);
            this.getAccount(account).save();
        }
    }
    
    /**
     * Add an account to the bank, if it already exists it updates the balance.
     * This does not utilize the initial balance setting. If you want to do that make sure you
     * grab the initial balance and put it as the second parameter.
     * 
     * @param account
     * @param balance
     */
    public void addAccount(String account, double balance) {
        if(!this.hasAccount(account)) {
            Account initialized = new Account(account, balance);
            iConomy.getDatabase().setBalance(account, balance);
            this.accounts.put(account, initialized);
        } else {
            this.getAccount(account).setBalance(balance);
            this.getAccount(account).save();
        }
    }
    
    /**
     * Subtract an amount from an account balance, this returns a boolean saying if the account exists or not to prevent failure.
     * 
     * @param account The account in question
     * @param amount The amount in double form that you wish to withdraw
     * @return boolean
     */
    public boolean subtractFromAccount(String account, double amount) {
        if(this.hasAccount(account)) {
            Account updating = this.getAccount(account);
            updating.subtract(amount);
            updating.save();
            return true;
        }
        
        return false;
    }

    /**
     * Adds an amount from an account balance, this returns a boolean saying if the account exists or not to prevent failure.
     *
     * @param account The account in question
     * @param amount The amount in double form that you wish to deposit
     * @return boolean
     */
    public boolean addToAccount(String account, double amount) {
        if(this.hasAccount(account)) {
            Account updating = this.getAccount(account);
            updating.add(amount);
            updating.save();
            return true;
        }
        
        return false;
    }

    
    public void updateAccount(String account, double amount) {
        if(this.hasAccount(account)) {
            Account updating = this.getAccount(account);
            updating.setBalance(amount);
            updating.save();
        } else {
            this.addAccount(account, amount);
        }
    }
    
    public void resetAccount(String account) {
        if(this.hasAccount(account)) {
            Account updating = this.getAccount(account);
            updating.setBalance(this.initial);
            updating.save();
        } else {
            this.addAccount(account, initial);
        }
    }
    
    public void removeAccount(String account) {
        if(this.hasAccount(account)) {
            this.getAccount(account).remove();
            this.accounts.remove(account);
        } else if(iConomy.getDatabase().hasBalance(account)) {
            iConomy.getDatabase().removeBalance(account);
        }
    }
}
