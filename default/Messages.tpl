##
# Messaging Template
##
# Variables
#   These are denoted by the + signs before them;
#   Not every line contains the same variables. 
#   To prevent any errors or issues, we have included every variable for each line.
##
# Colors
#   Previously you would use a utf-8 character to use colors, this has been replaced
#   with &[code] or the easier alternative html color brackets: <[color]>
##

# Money Tag [Money] 
tag: "<green>[<white>Money<green>] "

# Personal Account settings
personal:
    balance: "<green>Balance: <white>+balance"
    reset: "<rose>Your account has been reset."
    rank: "<green>Current rank: <white>+rank"
    set: "<green>Your balance has been changed to <white>+amount"
    debit: "<rose>Your account had <white>+amount<rose> debited."
    credit: "<white>+amount<green> was credited into your account."

##
# Player message settings
##
player:
    balance: "<green>+name's Balance: <white>+balance"
    rank: "<green>+name's rank: <white>+rank"
    reset: "<white>+name's <rose>account has been reset."
    set: "<green>+name's balance has been changed to <white>+amount"
    credit: "<white>+name's <green>account had <white>+amount<green> credited."
    debit: "<white>+name's <rose>account had <white>+amount<rose> debited."

##
# Payment messages
##
payment:
    self: "<rose>Sorry, you cannot send money to yourself."
    to: "<green>You have sent <white>+amount<green> to <white>+name<green>."
    from: "<white>+name<green> has sent you <white>+amount<green>."
    
statistics:
    opening: "<green>-----[ <white>iConomy Stats <green>]-----"
    total:  "<gray>Total +currency: <white>+amount"
    average: "<gray>Average +currency: <white>+amount"
    accounts: "<gray>Total Accounts: <white>+amount"

##
# Top-list Ranking
##
top:
    opening: "<green>Top <white>+amount<green> Richest Players:"
    empty: "<white>   Nobody yet!"
    line: "<white>   +i.<green> +name <white>(<green>+balance<white>)"

##
# Errors
##
no:
    account: "<rose>Player does not have account: <white>+name"
    funds: "<rose>Sorry, you do not have enough funds to do that."
