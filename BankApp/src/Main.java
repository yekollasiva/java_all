import java.util.*;

abstract class Account {
    protected String accNo;
    protected String name;
    protected double balance;
    protected ArrayList<String> history;
    private static int count = 0;

    public Account(String name, double amount) {
        this.name = name;
        this.balance = amount;
        this.history = new ArrayList<>();
        Random r = new Random();
        this.accNo =   (1000+r.nextInt(9000)) + "-" + (1000+r.nextInt(9000));
        count++;
        history.add("Created with $" + amount);
    }

    public abstract double interest();

    public void deposit(double amt) {
        if (amt > 0) {
            balance += amt;
            history.add("Deposited $" + amt);
            System.out.println("Deposit successful!");
        }
    }

    public boolean withdraw(double amt) {
        if (amt > 0 && amt <= balance) {
            balance -= amt;
            history.add("Withdrew $" + amt);
            System.out.println("Withdrawal successful!");
            return true;
        }
        System.out.println("Insufficient balance!");
        return false;
    }

    public void display() {
        System.out.println("Number: " + accNo);
        System.out.println("Holder: " + name);
        System.out.println("Balance: $" + balance);
        System.out.println("Transactions: " + history.size());
        for (String s : history) System.out.println("  - " + s);
    }

    public String getAccNo() { return accNo; }
    public double getBalance() { return balance; }
}

class Savings extends Account {
    private double rate;

    public Savings(String name, double amount, double rate) {
        super(name, amount);
        this.rate = rate;
    }

    public double interest() {
        return balance * rate / 100;
    }

    public void addInterest() {
        double i = interest();
        balance += i;
        history.add("Interest added $" + i);
        System.out.println("Interest added: $" + i);
    }
}

class Checking extends Account {
    private double limit;

    public Checking(String name, double amount, double limit) {
        super(name, amount);
        this.limit = limit;
    }

    public double interest() {
        return 0;
    }

    public boolean withdraw(double amt) {
        if (amt > 0 && amt <= balance + limit) {
            balance -= amt;
            history.add("Withdrew $" + amt);
            System.out.println("Withdrawal successful!");
            return true;
        }
        System.out.println("Insufficient balance!");
        return false;
    }
}

public class Main {
    private static ArrayList<Account> accounts = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. View Account");
            System.out.println("5. Add Interest (Savings)");
            System.out.println("6. Exit");
            System.out.print("Choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: createAccount(); break;
                case 2: deposit(); break;
                case 3: withdraw(); break;
                case 4: viewAccount(); break;
                case 5: addInterest(); break;
                case 6: System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void createAccount() {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Initial deposit: $");
        double amt = sc.nextDouble();
        sc.nextLine();
        System.out.print("Type (1=Savings, 2=Checking): ");
        int type = sc.nextInt();
        sc.nextLine();

        Account acc = null;
        if (type == 1) {
            System.out.print("Interest rate (%): ");
            double rate = sc.nextDouble();
            sc.nextLine();
            acc = new Savings(name, amt, rate);
        } else {
            System.out.print("Overdraft limit: $");
            double limit = sc.nextDouble();
            sc.nextLine();
            acc = new Checking(name, amt, limit);
        }
        accounts.add(acc);
        System.out.println("Account created!");
        System.out.println("Number: " + acc.getAccNo());
    }

    private static Account find() {
        System.out.print("Account number: ");
        String no = sc.nextLine();
        for (Account acc : accounts) {
            if (acc.getAccNo().equals(no)) return acc;
        }
        System.out.println("Account not found!");
        return null;
    }

    private static void deposit() {
        Account acc = find();
        if (acc != null) {
            System.out.print("Amount: $");
            acc.deposit(sc.nextDouble());
            sc.nextLine();
        }
    }

    private static void withdraw() {
        Account acc = find();
        if (acc != null) {
            System.out.print("Amount: $");
            acc.withdraw(sc.nextDouble());
            sc.nextLine();
        }
    }

    private static void viewAccount() {
        Account acc = find();
        if (acc != null) acc.display();
    }

    private static void addInterest() {
        Account acc = find();
        if (acc != null && acc instanceof Savings) {
            ((Savings) acc).addInterest();
        } else if (acc != null) {
            System.out.println("Not a savings account!");
        }
    }
}