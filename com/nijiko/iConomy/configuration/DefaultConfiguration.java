package com.nijiko.iConomy.configuration;

/**
 * Basic configuration loader.
 * 
 * @author Nijiko
 */
public abstract class DefaultConfiguration {
  public String database = "";
  public String user = "";
  public String pass = "";
  public String type = "sqlite";
  public String flatfile = "accounts.flat";

  public String permissions = "Permissions";



  public abstract void load();
}
