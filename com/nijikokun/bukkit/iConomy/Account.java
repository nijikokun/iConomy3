/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nijikokun.bukkit.iConomy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Nijiko
 */
public class Account {
    private String name;
    private double balance;

    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Sets the balance to ZERO. Not the initial.
     */
    public void resetBalance() {
        this.setBalance(0);
        this.save();
    }

    public boolean hasEnough(double amount) {
        return amount <= this.balance;
    }

    public boolean hasOver(double amount) {
        return amount < this.balance;
    }

    public boolean isNegative() {
        return this.balance < 0.0;
    }
    
    public void add(double amount) {
        this.balance = this.balance+amount;
    } 
    
    public void multiply(double amount) {
        this.balance = this.balance*amount;
    } 
    
    public void divide(double amount) {
        this.balance = this.balance/amount;
    }

    public void subtract(double amount) {
        this.balance = this.balance-amount;
    }

    public void remove() {
        iConomy.getDatabase().removeBalance(name);
    }

    public void save() {
        iConomy.getDatabase().setBalance(this.name, this.balance);
    }

   /**
     * Formats the balance in a human readable form without currency attached:<br /><br />
     * 20000.53 = 20,000.53<br />
     * 20000.00 = 20,000
    *
     * @return String
     */
    @Override
    public String toString() {
        NumberFormat formatter = new DecimalFormat("#,##0.##");
        String formatted = formatter.format(this.balance);

        if(formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length()-1);
        }

        return formatted;
    }

}
