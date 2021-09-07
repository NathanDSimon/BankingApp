package nathan.banking;

import java.io.EOFException;
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
public class Admin extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5155591158561999787L;
	private static long transferLimit;
	private static HashMap<String, Object> settings;
	public static long getTransferLimit()
	{
		return (long)settings.get("transferLimit");
	}
	private static void writeSettings()
	{
		try {
			FileOutputStream fout = new FileOutputStream("data/settings.cfg");
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(settings);
			out.close();
			fout.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public static void setTransferLimit(long lim) throws PermissionException
	{
		if(!(DataIO.currentUser.isAdmin()))
		{
			throw new PermissionException();
		}
		settings.replace("transferLimit", transferLimit, lim);
		writeSettings();
	}
	static {
		try {
			FileInputStream fileIn = new FileInputStream("data/settings.cfg");
			ObjectInputStream oin = new ObjectInputStream(fileIn);
			settings = (HashMap<String, Object>)oin.readObject();
			oin.close();
			fileIn.close();
		}catch (EOFException e)
		{
			settings = new HashMap<String, Object>();
			settings.put("transferLimit", 10000L);
			writeSettings();
		}
		catch (FileNotFoundException e) {
			try {
				(new File("data/settings.cfg")).createNewFile();
				settings = new HashMap<String, Object>();
				settings.put("transferLimit", 10000L);
				writeSettings();
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
	}
	public static Admin createNew(String username, String password, HashMap<String, String> personalInfo)
	{
		User u = new Admin();
		u.username = username;
		u.password = password;
		u.personalInfo = new HashMap<String, String>();
		u.personalInfo.putAll(personalInfo);
		u.connectedAccounts = new ArrayList<Account>();
		DataIO.users.put(username, u);
		return (Admin)u;
	}
	@Override
	public boolean canViewUserData() {
		return true;
	}
	@Override
	public boolean canSetUserData() {
		return true;
	}
	@Override
	public boolean canApproveAccounts() {
		return true;
	}
	@Override
	public boolean isAdmin() {
		return true;
	}
}
