package nathan.banking;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Account implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3683425652059594260L;
	private ArrayList<User> connectedUsers;
	private long balance;
	private LocalDateTime lastTransferUpdate;
	private long totalDailyTransfer;
	private boolean isApproved = false;
	private User approvedBy = null;
	private String status = "pending";
	private int accountNumber;
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
			u.addConnectedAccount(this);
		}
		accountNumber = DataIO.accounts.size();
	}
	public static Account create(ArrayList<User> users)
	{
		Account a = new Account(users);
		DataIO.accounts.add(a);
		DataIO.WriteAll();
		return a;
	}
	public long getBalance() {
		return balance;
	}
	public LocalDateTime getLastTransferUpdate() {
		return lastTransferUpdate;
	}
	public long getTotalDailyTransfer() {
		return totalDailyTransfer;
	}
	public boolean isApproved() {
		return isApproved;
	}
	public void approveBy(User a) throws PermissionException
	{
		if(!(DataIO.loggedIn && DataIO.currentUser.canApproveAccounts()))
		{
			throw new PermissionException();
		}
		isApproved = true;
		status = "active";
		approvedBy = a;
	}
	public User getApprovedBy() {
		return approvedBy;
	}
	private boolean updateTransferLimitTime(long amount) throws TransferDateException
	{
		if(ChronoUnit.DAYS.between(lastTransferUpdate, LocalDateTime.now()) > 0)
		{
			totalDailyTransfer = 0;
			return true;
		}
		else if(totalDailyTransfer + amount > Admin.getTransferLimit())
		{
			throw new TransferDateException();
		}
		return false;
	}
	/**
	 * 
	 * @param a the account to transfer to
	 * @param amount the amount to transfer
	 */
	public void transfer(Account a, long amount) throws AccountInvalidException, TransferDateException, OverdrawnException
	{
		if(this.totalDailyTransfer > Admin.getTransferLimit() || a.totalDailyTransfer > Admin.getTransferLimit())
		{
			throw new TransferDateException();
		}
		if(!(this.isApproved) || !(a.isApproved))
		{
			throw new AccountInvalidException();
		}
		this.updateTransferLimitTime(amount);
		a.updateTransferLimitTime(amount);
		if (amount > this.balance)
		{
			throw new OverdrawnException();
		}
		this.balance -= amount;
		a.balance += amount;
		this.totalDailyTransfer += amount;
		a.totalDailyTransfer += amount;
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
	public void deny()
	{
		this.status = "denied";
	}
	public String getStatus()
	{
		return this.status;
	}
	public boolean currentUserCanCheckAccount()
	{
		return (DataIO.currentUser.canViewUserData() || this.connectedUsers.contains(DataIO.currentUser));
	}
	public boolean isJoint()
	{
		return connectedUsers.size() > 1;	
	}
}
