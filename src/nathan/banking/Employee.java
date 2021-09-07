package nathan.banking;

import java.util.ArrayList;
import java.util.HashMap;

public class Employee extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7227100881024513854L;
	public static Employee createNew(String username, String password, HashMap<String, String> personalInfo)
	{
		User u = new Employee();
		u.username = username;
		u.password = password;
		u.personalInfo = new HashMap<String, String>();
		u.personalInfo.putAll(personalInfo);
		u.connectedAccounts = new ArrayList<Account>();
		DataIO.users.put(username, u);
		return (Employee)u;
	}
	@Override
	public boolean canViewUserData() {
		return true;
	}
	@Override
	public boolean canSetUserData() {
		return false;
	}
	@Override
	public boolean canApproveAccounts() {
		return true;
	}
	@Override
	public boolean isAdmin() {
		return false;
	}
}
