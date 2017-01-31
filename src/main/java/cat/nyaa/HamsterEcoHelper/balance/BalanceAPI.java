package cat.nyaa.HamsterEcoHelper.balance;


import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;

public class BalanceAPI {
    private final HamsterEcoHelper plugin;

    public BalanceAPI(HamsterEcoHelper pl) {
        plugin = pl;
    }
    
    public boolean isEnabled(){
        return plugin.config.enable_balance;
    }

    public double getBalance() {
        return plugin.config.variablesConfig.balance;
    }

    public double setBalance(double money) {
        return plugin.config.variablesConfig.balance = money;
    }

    public void withdraw(double money) {
        plugin.config.variablesConfig.balance -= money;
    }

    public void deposit(double money) {
        plugin.config.variablesConfig.balance += money;
    }
}
