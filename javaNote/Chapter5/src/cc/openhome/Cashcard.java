package cc.openhome;


class CashCard  {
    private String number;
    private int balance;
    private int bonus;
    CashCard(String number, int balance, int bonus)  {
        this.number = number;
        this.balance = balance;
        this.bonus = bonus;
    }
    
    void store(int money)   {
        if(money >0)    {
            this.balance += money;
            if(money >= 1000)   {
                this.bonus++;
            }
        }
        else    {
            System.out.println("Wrong input");
        }
    }
    
    void charge(int money)  {
        if(money > 0)   {
            this.balance -= money;
        }
        else    {
            System.out.println("Wrong charge value");
        }
    }
    
    int exchange(int bonus) {
        if(bonus > 0)   {
            this.bonus = bonus;
        }
        return this.bonus;
    }
    
    int getBalance()    {
        return balance;
    }
    
    String getNumber() {
        return number;
    }
    
    int getBonus()  {
        return bonus;
    }
}
