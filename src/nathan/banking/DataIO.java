package nathan.banking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class DataIO {
	public static HashMap<String, User> users;
	public static ArrayList<Account> accounts;
	public static boolean loggedIn = false;
	public static User currentUser = null;
	public static Account selectedAccount = null;
	public static void selectAccount(int id) throws PermissionException
	{
		if(!(accounts.size() >= id))
		{
			System.out.println("Account does not exist.");
			return;
		}
		if(!(accounts.get(id).currentUserCanCheckAccount()))
		{
			throw new PermissionException();
		}
		selectedAccount = accounts.get(id);
		
	}
	public static void WriteAll()
	{
		try {
			FileOutputStream fout = new FileOutputStream("data/users.cfg");
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(users);
			out.close();
			fout.close();
			fout = new FileOutputStream("data/accounts.cfg");
			out = new ObjectOutputStream(fout);
			out.writeObject(accounts);
			out.close();
			fout.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public static void login(String username, String password)
	{
		if(!(users.containsKey(username)))
		{
			System.out.println("That user does not exist. Please create an account to continue.");
		}
		else {
			User intended = users.get(username);
			if(!(intended.checkPassword(password)))
			{
				System.out.println("Incorrect password. Please try again.");
			}
			else 
			{
				loggedIn = true;
				currentUser = intended;
			}
		}
	}
	public static void logout()
	{
		loggedIn = false;
		currentUser = null;
		selectedAccount = null;
	}
	public static User createUser(String username, String password, String firstName, String lastName, String age, String dob, String addr, int acctType) throws PermissionException
	{
		if(users.containsKey(username))
		{
			System.out.println("User already exists.");
		}
		HashMap<String, String> personalInfo = new HashMap<String, String>();
		personalInfo.put("First Name", firstName);
		personalInfo.put("Last Name", lastName);
		personalInfo.put("Date of Birth", dob);
		personalInfo.put("Address", addr);
		User u;
		switch(acctType)
		{
		case 0:
			u = Customer.createNew(username, password, personalInfo);
			break;
		case 1:
			if(!(loggedIn) || !(currentUser.canSetUserData()))
			{
				throw new PermissionException();
			}
			u = Employee.createNew(username, password, personalInfo);
			break;
		case 2:
			if(!(loggedIn) || !(currentUser.canSetUserData()))
			{
				throw new PermissionException();
			}
			u = Admin.createNew(username, password, personalInfo);
			break;
		default:
			u = null;	
		}
		WriteAll();
		return u;
	}
	public static Account createNewAccount(boolean hasOtherUsers, ArrayList<String> otherUserNames) throws AccountInvalidException
	{
		ArrayList<User> otherUsers = new ArrayList<User>();
		otherUsers.add(currentUser);
		if(!hasOtherUsers)
		{
			return Account.create(otherUsers);
		}
		for(String s : otherUserNames)
		{
			if(!(users.containsKey(s)))
			{
				throw new AccountInvalidException();
			}
			otherUsers.add(users.get(s));
		}
		return Account.create(otherUsers);
	}
	public static void init()
	{
		users = new HashMap<String, User>();
		accounts = new ArrayList<Account>();
		try {
			FileInputStream fileIn = new FileInputStream("data/users.cfg");
			ObjectInputStream oin = new ObjectInputStream(fileIn);
			users = (HashMap<String, User>)oin.readObject();
			oin.close();
			fileIn.close();
			FileInputStream actIn = new FileInputStream("data/accounts.cfg");
			ObjectInputStream ain = new ObjectInputStream(actIn);
			accounts = (ArrayList<Account>)ain.readObject();
			ain.close();
			actIn.close();
		} catch (FileNotFoundException e) {
			try {
				(new File("data/settings.cfg")).createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			System.out.println("Created settings configuration file.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if(!(users.containsKey("root")))
		{
			HashMap<String, String> dummyData = new HashMap<String, String>();
			Admin.createNew("root", "ChangeMe", dummyData);
		}
		WriteAll();
	}
}
