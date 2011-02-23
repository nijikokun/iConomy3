package com.nijikokun.bukkit.iConomy;

import java.util.Map;
import java.util.TreeMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * iConomy v1.x
 * Copyright (C) 2010  Nijikokun <nijikokun@gmail.com>
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
/**
 * iListen.java
 * <br /><br />
 * Listens for calls from hMod, and reacts accordingly.
 * 
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class iListen extends PlayerListener {

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public Misc Misc = new Misc();
    public static iConomy plugin;

    public iListen(iConomy instance) {
        plugin = instance;
    }

    /**
     * Sends simple condensed help lines to the current player
     */
    private void showSimpleHelp() {
        Messaging.send("&e-----------------------------------------------------");
        Messaging.send("&f " + plugin.name + " (&c" + plugin.codename + "&f)           ");
        Messaging.send("&e-----------------------------------------------------");
        Messaging.send("&f [] Required, () Optional                            ");
        Messaging.send("&e-----------------------------------------------------");
        Messaging.send("&f /money &6-&e Check your balance                     ");
        Messaging.send("&f /money ? &6-&e For help & Information               ");
        Messaging.send("&f /money rank (player) &6-&e Rank on the topcharts.   ");
        Messaging.send("&f /money top (amount) &6-&e Richest players listing.  ");
        Messaging.send("&f /money pay [player] [amount] &6-&e Send money to another player.");
        Messaging.send("&e-----------------------------------------------------");
        Messaging.send("&f Admin Commands:                                     ");
        Messaging.send("&e-----------------------------------------------------");
        Messaging.send("&f /money withdraw [player] (amount) &6-&e Take money from a player.");
        Messaging.send("&f /money deposit [player] (amount) &6-&e Give money to a player.");
        Messaging.send("&f /money reset [player] &6-&e Puts a players account at initial.");
        Messaging.send("&e-----------------------------------------------------");
    }

    /**
     * Shows the balance to the requesting player.
     *
     * @param name The name of the player we are viewing
     * @param viewing The player who is viewing the account
     * @param mine Is it the player who is trying to view?
     */
    public void showBalance(String name, Player viewing, boolean mine) {
        if (mine) {
            Messaging.send(viewing, iConomy.template.color("tag") + iConomy.template.parse("personal-balance", new String[]{"+balance,+b"}, new String[]{iConomy.Bank.format(viewing.getName())}));
        } else {
            Messaging.send(viewing, iConomy.template.color("tag") + iConomy.template.parse("player-balance", new String[]{"+balance,+b", "+name,+n"}, new String[]{iConomy.Bank.format(name), name}));
        }
    }

    /*
     * Reset a players account easily.
     *
     * @param resetting The player being reset. Cannot be null.
     * @param by The player resetting the account. Cannot be null.
     * @param notify Do we want to show the updates to each player?
     */
    public void showPayment(String from, String to, double amount) {
        Player paymentFrom = Misc.player(from);
        Player paymentTo = Misc.player(to);
        Account balanceFrom = iConomy.Bank.getAccount(from);
        Account balanceTo = iConomy.Bank.getAccount(to);

        if (from.equals(to)) {
            if (paymentFrom != null) {
                Messaging.send(paymentFrom, iConomy.template.color("pay-self"));
            }

            iConomy.Info.write("{'action': 'self', 'player': '" + from + "', 'local': '" + to + "', 'executed': 0, 'data': { 'amount': " + amount + " }}");
        } else if (balanceFrom.hasEnough(amount)) {
            if (paymentFrom != null) {
                Messaging.send(paymentFrom, iConomy.template.color("no-funds"));
            }

            iConomy.Info.write("{'action': 'low balance', 'player': '" + from + "', 'local': '" + to + "', 'executed': 0, 'data': { 'balance': " + balanceFrom.toString() + ", 'amount': " + amount + " }}");
        } else {
            balanceFrom.subtract(amount); balanceFrom.save();
            balanceTo.add(amount); balanceTo.save();

            if (paymentFrom != null) {
                Messaging.send(
                    paymentFrom,
                    iConomy.template.color("tag")
                    + iConomy.template.parse(
                        "payment-to",
                        new String[]{"+name,+n", "+amount,+a"},
                        new String[]{from, iConomy.Bank.format(amount) }
                    )
                );
            }

            if (paymentTo != null) {
                Messaging.send(
                    paymentTo,
                    iConomy.template.color("tag")
                    + iConomy.template.parse(
                        "payment-from",
                        new String[]{"+name,+n", "+amount,+a"},
                        new String[]{from, iConomy.Bank.format(amount) }
                    )
                );
            }

            iConomy.Info.write("{'action': 'success', 'player': '" + from + "', 'local': '" + to + "', 'executed': 1, 'data': { 'player_balance_before': " + balanceFrom.toString() + " 'player_balance_after': " + balanceFrom.toString() + ", 'local_balance_before': " + balanceTo.toString() + " 'local_balance_after': " + balanceTo.toString() + ", 'amount': " + amount + ", }}");

            if (paymentFrom != null) {
                showBalance(from, paymentFrom, true);
            }

            if (paymentTo != null) {
                showBalance(to, paymentTo, true);
            }
        }
    }

    /**
     * Reset a players account, accessable via Console & In-Game
     *
     * @param account The account we are resetting.
     * @param controller If set to null, won't display messages.
     * @param console Is it sent via console?
     */
    public void showReset(String account, Player controller, boolean console) {
        Player player = Misc.player(account);
        iConomy.Bank.resetAccount(account);

        if (player != null) {
            Messaging.send(player, iConomy.template.color("personal-reset"));
        }

        if (controller != null) {
            Messaging.send(
                iConomy.template.parse(
                    "player-reset",
                    new String[]{ "+name,+n" },
                    new String[]{ account }
                )
            );
        }

        if (console) {
            iConomy.log.info("Player " + account + "'s account has been reset.");
        } else {
            iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account has been reset by " + controller.getName() + ".");
        }
    }

    /**
     *
     * @param account
     * @param controller If set to null, won't display messages.
     * @param amount
     * @param console Is it sent via console?
     */
    public void showWithdraw(String account, Player controller, double amount, boolean console) {
        Player player = Misc.player(account);
        Account withdrawing = iConomy.Bank.getAccount(account);

        withdrawing.subtract(amount);
        withdrawing.save();

        if (player != null) {
            Messaging.send(player,
                iConomy.template.color("tag")
                + iConomy.template.parse(
                    "personal-withdraw",
                    new String[]{"+by", "+amount,+a"},
                    new String[]{ (console) ? "console" : controller.getName(), iConomy.Bank.format(account) }
                )
            );

            showBalance(account, player, true);
        }

        if (controller != null) {
            Messaging.send(
                iConomy.template.color("tag")
                + iConomy.template.parse(
                    "player-withdraw",
                    new String[]{ "+name,+n", "+amount,+a" },
                    new String[]{ account, iConomy.Bank.format(amount) }
                )
            );
        }

        if (console) {
            iConomy.log.info("Player " + account + "'s account had " + iConomy.Bank.format(amount) + " withdrawn.");
        } else {
            iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account had " + iConomy.Bank.format(amount) + " withdrawn by " + controller.getName() + ".");
        }
    }

    /**
     *
     * @param account
     * @param controller If set to null, won't display messages.
     * @param amount
     * @param console Is it sent via console?
     */
    public void showDeposit(String account, Player controller, int amount, boolean console) {
        Player online = Misc.player(account);
        int balance = iConomy.database.get_balance(account);
        balance += amount;
        iConomy.database.set_balance(account, balance);

        if (online != null) {
            Messaging.send(online,
                    iConomy.template.color("tag")
                    + iConomy.template.parse(
                    "personal-deposited",
                    new String[]{"+by", "+amount,+a"},
                    new String[]{(console) ? "console" : controller.getName(), Misc.formatCurrency(amount, iConomy.currency)}));

            showBalance(account, online, true);
        }

        if (controller != null) {
            Messaging.send(
                    iConomy.template.color("tag")
                    + iConomy.template.parse(
                    "player-deposited",
                    new String[]{"+name,+n", "+amount,+a"},
                    new String[]{account, Misc.formatCurrency(amount, iConomy.currency)}));
        }

        if (console) {
            iConomy.log.info("Player " + account + "'s account had " + amount + " deposited into it.");
        } else {
            iConomy.log.info(Messaging.bracketize(iConomy.name) + "Player " + account + "'s account had " + amount + " deposited into it by " + controller.getName() + ".");
        }
    }

    /**
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
        Messaging.save(player);
        String base = split[0];


        if (Misc.is(base, "/money")) {
            if (Misc.arguments(split, 0)) {
                showBalance("", player, true);
                return;
            }

            if (Misc.arguments(split, 1)) {
                if (Misc.isEither(split[1], "rank", "-r")) {
                    if (!iConomy.Permissions.has(player, "iConomy.rank")) {
                        return;
                    }

                    iConomy.database.get_ranked(player.getName(), player, true);
                    return;
                }

                if (Misc.isEither(split[1], "top", "-t")) {
                    if (!iConomy.Permissions.has(player, "iConomy.list")) {
                        return;
                    }

                    iConomy.database.get_top(5);
                    return;
                }

                if (Misc.isEither(split[1], "help", "?")
                        || Misc.isEither(split[1], "deposit", "-d")
                        || Misc.isEither(split[1], "credit", "-c")
                        || Misc.isEither(split[1], "withdraw", "-w")
                        || Misc.isEither(split[1], "debit", "-b")
                        || Misc.isEither(split[1], "pay", "-p")) {
                    showSimpleHelp();
                    return;
                }

                // Check another players account
                if (!iConomy.Permissions.has(player, "iConomy.access")) {
                    return;
                }

                Player viewable = Misc.playerMatch(split[1]);

                if (viewable == null) {
                    if (iConomy.getDatabase().hasBalance(split[1])) {
                        showBalance(split[1], player, false);
                    } else {
                        Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[1]}));
                    }
                    return;

                } else {
                    showBalance(viewable.getName(), player, false);
                    return;
                }
            }

            if (Misc.arguments(split, 2)) {
                if (Misc.isEither(split[1], "reset", "-x")) {
                    if (!iConomy.Permissions.has(player, "iConomy.reset")) {
                        return;
                    }

                    Player viewable = Misc.player(split[2]);

                    if (viewable == null) {
                        if (iConomy.getDatabase().hasBalance(split[2])) {
                            showReset(split[2], player, false);
                        } else {
                            Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
                        }
                        return;

                    } else {
                        showReset(viewable.getName(), player, false);
                        return;
                    }
                }

                if (Misc.isEither(split[1], "rank", "-r")) {
                    if (!iConomy.Permissions.has(player, "iConomy.rank")) {
                        return;
                    }

                    Player viewable = Misc.playerMatch(split[2]);

                    if (viewable == null) {
                        if (iConomy.getDatabase().hasBalance(split[2])) {
                            iConomy.database.get_ranked(split[2], player, false);
                        } else {
                            Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
                        }
                        return;

                    } else {
                        iConomy.database.get_ranked(viewable.getName(), player, false);
                        return;
                    }
                }

                if (Misc.isEither(split[1], "top", "-t")) {
                    if (!iConomy.Permissions.has(player, "iConomy.list")) {
                        return;
                    }

                    iConomy.database.get_top((Integer.parseInt(split[2]) < 0) ? 5 : Integer.parseInt(split[2]));
                    return;
                }

                showSimpleHelp();
                return;
            }

            if (Misc.arguments(split, 3)) {
                if (Misc.isEither(split[1], "pay", "-p")) {
                    if (!iConomy.Permissions.has(player, "iConomy.payment")) {
                        return;
                    }

                    Player viewable = Misc.playerMatch(split[2]);
                    String name = "";
                    int amount = 0;

                    if (viewable == null) {
                        if (iConomy.getDatabase().hasBalance(split[2])) {
                            name = split[2];
                        } else {
                            Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
                            return;
                        }
                    } else {
                        name = viewable.getName();
                    }

                    try {
                        amount = Integer.parseInt(split[3]);

                        if (amount < 1) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        Messaging.send("&cInvalid amount: &f" + amount);
                        Messaging.send("&cUsage: &f/money &c[&f-p&c|&fpay&c] <&fplayer&c> &c<&famount&c>");
                        return;
                    }

                    // Pay amount
                    showPayment(player.getName(), name, amount);
                    return;
                }

                if (Misc.isEither(split[1], "deposit", "-d") || Misc.isEither(split[1], "credit", "-c")) {
                    if (!iConomy.Permissions.has(player, "iConomy.deposit")) {
                        return;
                    }

                    Player viewable = Misc.player(split[2]);

                    if (viewable == null) {
                        if (iConomy.getDatabase().hasBalance(split[2])) {
                            showDeposit(split[2], player, Integer.valueOf(split[3]), true);
                        } else {
                            Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
                        }
                        return;

                    } else {
                        showDeposit(viewable.getName(), player, Integer.valueOf(split[3]), false);
                        return;
                    }
                }

                if (Misc.isEither(split[1], "withdraw", "-w") || Misc.isEither(split[1], "debit", "-b")) {
                    if (!iConomy.Permissions.has(player, "iConomy.withdraw")) {
                        return;
                    }

                    Player viewable = Misc.player(split[2]);

                    if (viewable == null) {
                        if (iConomy.getDatabase().hasBalance(split[2])) {
                            showWithdraw(split[2], player, Integer.valueOf(split[3]), true);
                        } else {
                            Messaging.send(iConomy.template.parse("no-account", new String[]{"+name,+n"}, new String[]{split[2]}));
                        }
                        return;
                    } else {
                        showWithdraw(viewable.getName(), player, Integer.valueOf(split[3]), false);
                        return;
                    }
                }

                showSimpleHelp();
                return;
            }
        }

        return;
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        Player player = event.getPlayer();

        if (iConomy.getDatabase().hasBalance(player.getName())) {
            iConomy.getDatabase().setBalance(player.getName(), iConomy.initialBalance);
        }
    }
}
