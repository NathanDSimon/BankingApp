package nathan.banking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -343937814567666796L;
	protected String username;
	protected String password;
	protected HashMap<String, String> personalInfo;
	protected ArrayList<Account> connectedAccounts;
	protected User() 
	{
		
	}
	public void addConnectedAccount(Account a)
	{
		this.connectedAccounts.add(a);
	}
	public String getUsername() 
	{
		return username;
	}
	public boolean checkPassword(String pass)
	{
		return this.password.compareTo(pass) == 0;
	}
	public void setPassword(String oldPass, String newPass)
	{
		if((this.checkPassword(oldPass) && this.equals(DataIO.currentUser)) || DataIO.currentUser.isAdmin())
		{
			password = newPass;
		}
		else
		{
			System.out.println("Password change failed.");
			if(!(this.checkPassword(oldPass)))
			{
				System.out.println("Password entered was incorrect.");
			}
		}
	}
	protected String getPersonalInfoBit(String key)
	{
		return this.personalInfo.get(key);
	}
	public abstract boolean canViewUserData();
	public abstract boolean canSetUserData();
	public abstract boolean canApproveAccounts();
	public abstract boolean isAdmin();
	public void listAccounts()
	{
		for(Account a : connectedAccounts)
		{
			System.out.print("\tAccount number " + a.getAccountNumber());
			if(a.isJoint())
			{
				System.out.print(" joint with users:");
				a.printConnectedUsers(true);
			}
			System.out.println();
		}
	}
	public void printInfo()
	{
		System.out.println("User info for " + username);
		for(Map.Entry<String, String> e : this.personalInfo.entrySet())
		{
			System.out.println("\t" + e.getKey() + ": " + e.getValue());
		}
	}
}
