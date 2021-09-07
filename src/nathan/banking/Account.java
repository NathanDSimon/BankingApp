package nathan.banking;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class Account implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -343454477079153095L;
	private ArrayList<User> connectedUsers;
	private double balance;
	private LocalDateTime lastTransferUpdate;
	private double totalDailyTransfer;
	private boolean isApproved = false;
	private User approvedOrDeniedBy = null;
	private String status = "pending";
	private int accountNumber;
	public Account()
	{
		
	}
	public int getAccountNumber()
	{
		return accountNumber;
	}
	private Account(ArrayList<User> users)
	{
		connectedUsers = new ArrayList<User>();
		balance = 0;
		totalDailyTransfer = 0;
		lastTransferUpdate = LocalDateTime.now();
		for(User u: users)
		{
			connectedUsers.add(u);
		}
		accountNumber = DataIO.accounts.size();
	}
	public static Account create(ArrayList<User> users)
	{
		Account a = new Account(users);
		for(User u : users)
		{
			u.addConnectedAccount(a);
		}
		DataIO.accounts.add(a);
		DataIO.WriteAll();
		return a;
	}
	public double getBalance() {
		return balance;
	}
	public LocalDateTime getLastTransferUpdate() {
		return lastTransferUpdate;
	}
	public double getTotalDailyTransfer() {
		return totalDailyTransfer;
	}
	public boolean isApproved() {
		return isApproved;
	}
	public void approve() throws PermissionException
	{
		if(!(DataIO.loggedIn && DataIO.currentUser.canApproveAccounts()))
		{
			throw new PermissionException();
		}
		isApproved = true;
		status = "active";
		approvedOrDeniedBy = DataIO.currentUser;
	}
	public User getApprovedOrDeniedBy() {
		return approvedOrDeniedBy;
	}
	private boolean updateTransferLimitTime(double d) throws TransferDateException
	{
		if(ChronoUnit.DAYS.between(lastTransferUpdate, LocalDateTime.now()) > 0)
		{
			totalDailyTransfer = 0;
			return true;
		}
		else if(totalDailyTransfer + d > Admin.getTransferLimit())
		{
			throw new TransferDateException();
		}
		return false;
	}
	/**
	 * 
	 * @param a the account to transfer to
	 * @param d the amount to transfer
	 */
	public void transfer(Account a, double d) throws AccountInvalidException, TransferDateException, OverdrawnException
	{
		if(d < 0)
		{
			System.out.println("Cannot transfer a negative amount.");
			return;
		}
		if(this.totalDailyTransfer > Admin.getTransferLimit() || a.totalDailyTransfer > Admin.getTransferLimit())
		{
			throw new TransferDateException();
		}
		if(!(this.isApproved) || !(a.isApproved))
		{
			throw new AccountInvalidException();
		}
		this.updateTransferLimitTime(d);
		a.updateTransferLimitTime(d);
		if (d > this.balance)
		{
			throw new OverdrawnException();
		}
		this.balance -= d;
		a.balance += d;
		this.totalDailyTransfer += d;
		a.totalDailyTransfer += d;
	}
	public void cancel() throws PermissionException
	{
		if((DataIO.loggedIn && DataIO.currentUser.canSetUserData()))
		{
			this.isApproved = false;
			this.status = "cancelled";
		}
		else
		{
			throw new PermissionException();
		}
	}
	public void printConnectedUsers(boolean ignoreCurrent)
	{
		for(User u : this.connectedUsers)
		{
			if(u.equals(DataIO.currentUser) && ignoreCurrent) continue;
			System.out.println("\t\t" + u.username);
		}
	}
	public void deny() throws PermissionException
	{
		if(!(DataIO.loggedIn && DataIO.currentUser.canApproveAccounts()))
		{
			throw new PermissionException();
		}
		this.isApproved = false;
		approvedOrDeniedBy = DataIO.currentUser;
		this.status = "denied";
	}
	public String getStatus()
	{
		return this.status;
	}
	public boolean currentUserCanCheckAccount()
	{
		return DataIO.loggedIn && (DataIO.currentUser.canViewUserData() || this.connectedUsers.contains(DataIO.currentUser));
	}
	public boolean isJoint()
	{
		return connectedUsers.size() > 1;	
	}
	@BeforeClass
	public static void beforeAll()
	{
		User dummyAdmin = Admin.createNew("DummyAdmin", "DumbPass", new HashMap<String, String>());
		DataIO.login("DummyAdmin", "DumbPass");
		User dummyCust = null;
		User dummyEmployee = null;
		try {
			dummyEmployee = DataIO.createUser("DummyEmployee", "DumbPass", "John", "Doe", "no", "1-1-00", "Everywhere", 1);
			dummyCust = DataIO.createUser("DummyCust", "DumbPass", "John", "Smith", "yes", "0-0-00", "Nowhere", 0);
		} catch (PermissionException e) {
			e.printStackTrace();
		}
		ArrayList<User> list1 = new ArrayList<User>();
		list1.add(dummyCust);
		ArrayList<User> list2 = new ArrayList<User>();
		list2.add(dummyEmployee);
		list2.add(dummyCust);
		ArrayList<User> list3 = new ArrayList<User>();
		list3.add(dummyEmployee);
		list3.add(dummyCust);
		list3.add(dummyAdmin);
	    Account DumbAccount1 = create(list1);
	    DumbAccount1.balance = 1000000.1;
	    DumbAccount1.accountNumber = 1337;
		Account DumbAccount2 = create(list2);
		DumbAccount2.balance = 34500.5;
		DumbAccount2.accountNumber = 1338;
		Account DumbAccount3 = create(list3);
		DumbAccount3.balance = 45010202.25;
		DumbAccount3.accountNumber = 1339;
		try {
			Admin.setTransferLimit(10000000000.0);
		} catch (PermissionException e) {
			e.printStackTrace();
		}
	}
	@Before
	public void beforeTest()
	{
		for(Account a : DataIO.accounts)
		{
			a.lastTransferUpdate = LocalDateTime.now();
			a.totalDailyTransfer = 0;
			a.isApproved = true;
		}
		 Account DumbAccount1 = DataIO.getAccountByID(1337);
		    DumbAccount1.balance = 1000000.1;
		    DumbAccount1.accountNumber = 1337;
			Account DumbAccount2 = DataIO.getAccountByID(1338);
			DumbAccount2.balance = 34500.5;
			DumbAccount2.accountNumber = 1338;
			Account DumbAccount3 = DataIO.getAccountByID(1339);
			DumbAccount3.balance = 45010202.25;
			DumbAccount3.accountNumber = 1339;
	}
	@Test
	public void testAccountTransferValid()
	{
		Account a = DataIO.getAccountByID(1337);
		Account b = DataIO.getAccountByID(1338);
		try {
			a.transfer(b, 666.66);
		} catch (AccountInvalidException | TransferDateException | OverdrawnException e) {
			e.printStackTrace();
		}
	}
	@Test (expected = AccountInvalidException.class)
	public void testAccountTransferUnapproved() throws AccountInvalidException
	{
		Account a = DataIO.getAccountByID(1337);
		a.isApproved = false;
		Account b = DataIO.getAccountByID(1338);
		try {
			a.transfer(b, 7);
		} catch (TransferDateException | OverdrawnException e) {
			e.printStackTrace();
		}
	}
	@Test (expected = TransferDateException.class) 
	public void testAccountTransferTooLarge() throws TransferDateException
	{
		Account a = DataIO.getAccountByID(1337);
		Account b = DataIO.getAccountByID(1338);
		a.balance = 20000000000.0;
		
		try {
			a.transfer(b, 10000000001.0);
		} catch (AccountInvalidException | OverdrawnException e) {
			e.printStackTrace();
		}
	}
	@Test (expected = TransferDateException.class)
	public void testAccountTooManyTransfers() throws TransferDateException
	{
		Account a = DataIO.getAccountByID(1337);
		Account b = DataIO.getAccountByID(1338);
		a.totalDailyTransfer = 10000000000.0;
		try {
			a.transfer(b, 20);
		} catch (AccountInvalidException | OverdrawnException e) {
			e.printStackTrace();
		}
	}
	@Test (expected = OverdrawnException.class)
	public void testAccountOverdraw() throws OverdrawnException
	{
		Account a = DataIO.getAccountByID(1337);
		Account b = DataIO.getAccountByID(1338);
		a.totalDailyTransfer = 0;
		b.totalDailyTransfer = 0;
		System.out.println(Admin.getTransferLimit());
		try {
			a.transfer(b, 900000000.0);
		} catch (AccountInvalidException | TransferDateException e) {
			e.printStackTrace();
		}
	}
	@After
	public void afterTest()
	{
		for(Account a : DataIO.accounts)
		{
			a.isApproved = true;
		}
		try {
			Admin.setTransferLimit(10000000000.0);
		} catch (PermissionException e) {
			e.printStackTrace();
		}
	}
	@AfterClass
	public static void afterAll()
	{
		File settingsFile = new File("data/settings.cfg");
		File usersFile = new File("data/users.cfg");
		File accountsFile = new File("data/accounts.cfg");
		int n = 0;
		int o = 0;
		int p = 0;
		File newSettings = new File("data/settings_test" + n + ".cfg");
		while(newSettings.exists())
		{
			n++;
			newSettings = new File("data/settings_test" + n + ".cfg");
		}
		File newAccounts = new File("data/settings_test" + o + ".cfg");
		while(newAccounts.exists())
		{
			o++;
			newAccounts = new File("data/accounts_test" + o + ".cfg");
		}
		File newUsers = new File("data/users_test" + p + ".cfg");
		while(newUsers.exists())
		{
			p++;
			newUsers = new File("data/users_test" + p + ".cfg");
		}
		settingsFile.renameTo(newSettings);
		usersFile.renameTo(newUsers);
		accountsFile.renameTo(newAccounts);
	}
}
