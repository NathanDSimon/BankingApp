package nathan.banking;

import java.util.ArrayList;
import java.util.HashMap;

public class Customer extends User {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6742085101022354619L;
	public static Customer createNew(String username, String password, HashMap<String, String> personalInfo)
	{
		User u = new Customer();
		u.username = username;
		u.password = password;
		u.personalInfo = new HashMap<String, String>();
		u.personalInfo.putAll(personalInfo);
		u.connectedAccounts = new ArrayList<Account>();
		DataIO.users.put(username, u);
		return (Customer)u;
	}
	@Override
	public boolean canViewUserData() {
		return false;
	}
	@Override
	public boolean canSetUserData() {
		return false;
	}
	@Override
	public boolean canApproveAccounts() {
		return false;
	}
	@Override
	public boolean isAdmin() {
		return false;
	}
}
