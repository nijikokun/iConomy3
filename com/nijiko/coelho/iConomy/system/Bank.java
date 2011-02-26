package com.nijiko.coelho.iConomy.system;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.util.Constants;
import org.bukkit.util.config.Configuration;

public class Bank {
	private HashMap<String, Account> accounts;
	private String currency;
	private double initial;

    /**
     * Loads all bank accounts into a hashmap.
     *
     * @throws Exception
     */
	public void load() throws Exception {
		this.currency = Constants.Currency;
		this.initial = Constants.Initial_Balance;

		HashMap<String, Account> accounts = new HashMap<String, Account>();

		if (Constants.Database_Type.equalsIgnoreCase("flatfile")) {
            Configuration Flatfile = iConomy.getDatabase().getFlatfile();
            List<String> balances = Flatfile.getStringList("accounts", null);

            if(balances != null) {
                for (String account : Flatfile.getStringList("accounts", null)) {
                    Account initialized = new Account(account, Flatfile.getDouble(account, Constants.Initial_Balance));
                    accounts.put(account.toLowerCase(), initialized);
                }
            }
		} else {
			DatabaseMetaData dbm = iConomy.getDatabase().getConnection().getMetaData();
			ResultSet rs = dbm.getTables(null, null, Constants.SQL_Table, null);

			if(!rs.next()) {
                if(Constants.Database_Type.equalsIgnoreCase("mysql")) {
                    iConomy.getDatabase().executeQuery("CREATE TABLE " + Constants.SQL_Table + " (`id` INT(10) NOT NULL AUTO_INCREMENT, `username` TEXT NOT NULL, `balance` DECIMAL(65, 2) NOT NULL, PRIMARY KEY (`id`))");
                } else if(Constants.Database_Type.equalsIgnoreCase("sqlite")) {
                    iConomy.getDatabase().executeQuery("CREATE TABLE '" + Constants.SQL_Table + "' ('id' INT ( 10 ) PRIMARY KEY , 'username' TEXT , 'balance' DECIMAL ( 65 , 2 ));");
                }
            } else {
                rs = iConomy.getDatabase().resultQuery("SELECT * FROM " + Constants.SQL_Table + "");

                while(rs.next()) {
                    Account initialized = new Account(rs.getString("username"), rs.getDouble("balance"));
                    accounts.put(rs.getString("username").toLowerCase(), initialized);
                }
            }
		}

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
		DecimalFormat formatter = new DecimalFormat("#,##0.##");
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
		return this.accounts;
	}

    /**
     * Private :)
     *
     * @return
     */
	private HashMap<String, Double> getDirectAccounts() {
		HashMap<String, Double> result = new HashMap<String, Double>();
		for(String name : this.accounts.keySet())
			result.put(name, accounts.get(name).getBalance());
		return result;
	}

	/**
	 * Does the bank have record of the account in question?
	 *
	 * @param account The account in question
	 * @return boolean - Does the account exist?
	 */
	public boolean hasAccount(String account) {
		return accounts.containsKey(account.toLowerCase());
	}

	/**
	 * Fetch the account, Does not check for existance.
	 * Do that prior to using this to prevent null errors or any other issues.
	 *
	 * @param account The account to grab
	 * @return Account - Child object of bank
	 */
	public Account getAccount(String accountName) {
		return accounts.get(accountName.toLowerCase());
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
	 * Get the initial balance amount upon creation of new accounts.
	 * 
	 * @return double
	 */
	public double getInitial() {
		return initial;
	}

    /**
     *  Grabs the account ranks based on input amount
     *
     * @param output
     * @return Arraylist of account names
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<String> getAccountRanks(int output) {
		List list = new LinkedList(getDirectAccounts().entrySet());
	    Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
	              return ((Comparable) ((Map.Entry) (o1)).getValue())
	             .compareTo(((Map.Entry) (o2)).getValue());
	         }
	    });
	    Collections.reverse(list);

	    HashMap sorted = new HashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry) it.next();
	        sorted.put(entry.getKey(), entry.getValue());
	    }

		if (sorted.size() < 1) {
			return new ArrayList<String>();
		}

		if (output > sorted.size()) {
			output = sorted.size();
		}
		
		int i = 0;
		ArrayList<String> outputArray = new ArrayList<String>(output);
		
		for (Object key : sorted.keySet()) {
            if(i >= output) { break; }

			String name = (String) key;
			outputArray.add(name);
			i++;
		}
		
		return outputArray;
	}

    /**
     * Returns the ranking number of an account
     *
     * @param name
     * @return Integer
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int getAccountRank(String name) {
		List list = new LinkedList(getDirectAccounts().entrySet());
	    Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
	              return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
	         }
	    });

	    Collections.reverse(list);
	    HashMap sorted = new HashMap();

	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry) it.next();
	        sorted.put(entry.getKey(), entry.getValue());
	    }

		if (sorted.size() < 1) {
			return 1;
		}
		
		int i = 1;
		
		for (Object key : sorted.keySet()) {
			String username = (String) key;

			if(name.equals(username))
				return i;

			i++;
		}
		
		return 1;
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
			this.accounts.put(account.toLowerCase(), initialized);
			this.getAccount(account).save();
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
			initialized.save();
			this.accounts.put(account.toLowerCase(), initialized);
		} else {
			this.getAccount(account).setBalance(balance);
			this.getAccount(account).save();
		}
	}

    /**
     * Update an account or create one if it doesn't exist.
     *
     * @param account
     * @param amount
     */
	public void updateAccount(String account, double amount) {
		if(this.hasAccount(account)) {
			Account updating = this.getAccount(account);
			updating.setBalance(amount);
			updating.save();
		} else {
			this.addAccount(account, amount);
		}
	}

    /**
     * Reset account or create a new one if it doesn't exist.
     *
     * @param account
     */
	public void resetAccount(String account) {
		if(this.hasAccount(account)) {
			Account updating = this.getAccount(account);
			updating.setBalance(this.initial);
			updating.save();
		} else {
			this.addAccount(account, initial);
		}
	}

    /**
     * Completely remove an account from the bank and the database.
     *
     * @param account
     */
	public void removeAccount(String account) {
		if (Constants.Database_Type.equalsIgnoreCase("flatfile")) {
            if(this.hasAccount(account)) {
                this.getAccount(account).remove();
                this.accounts.remove(account);
            }
		} else {
            ResultSet rs = iConomy.getDatabase().resultQuery(
                    "SELECT * FROM `" + Constants.SQL_Table + "` WHERE username = ?",
                            new Object[]{ account }
            );

            try {
                if(this.hasAccount(account)) {
                    this.getAccount(account).remove();
                    this.accounts.remove(account);
                } else if(rs.next()) {
                    iConomy.getDatabase().executeQuery(
                            "DELETE FROM `" + Constants.SQL_Table + "` WHERE username = ?",
                            new Object[]{ account }
                    );
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
	}
}