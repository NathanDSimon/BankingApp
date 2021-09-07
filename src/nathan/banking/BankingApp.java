package nathan.banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BankingApp {

	private interface SubCommand 
	{
		void run(String ...args);
	}
	public static Scanner globalScanner;
	private static class Command
	{
		private HashMap<String, SubCommand> subCommands = new HashMap<String, SubCommand>();
		private String name;
		private boolean hasSubCommands = true;
		private String usage = "";
		private HashMap<String, String> subUsage = new HashMap<String, String>();
		public Command(String name)
		{
			this.name = name + "[] + <args>";
			this.usage = name;
		}
		public Command(String name, String usage, SubCommand base)
		{
			this.name = name;
			this.hasSubCommands = false;
			this.subCommands.put("base", base);
			this.usage = usage;
		}
		public String getName()
		{
			return name;
		}
		public void addSub(String key,String subUsage, SubCommand c )
		{
			subCommands.put(key, c);
			this.subUsage.put(key, subUsage);
		}
		public void runSub(String name, String ...args)
		{
			subCommands.get(name).run(args);
		}
	}
	private static void doUserRegistration(int type)
	{
		System.out.println("Input username.");
		String username = globalScanner.nextLine();
		boolean pass_set = false;
		String password = "";
		while(!pass_set)
		{
			System.out.println("Input password.");
			password = globalScanner.nextLine();
			System.out.println("Confirm password.");
			if((password.compareTo(globalScanner.nextLine()) != 0))
			{
				System.out.println("Passwords do not match.");
			}
			else {
				pass_set = true;
			}
		}
		System.out.print("Input first and last name. First name: ");
		String firstname = globalScanner.nextLine();
		System.out.print("Last name: ");
		String lastname = globalScanner.nextLine();
		System.out.println("Input date of birth.");
		String dob = globalScanner.nextLine();
		System.out.println("Input address, as one line.");
		String addr = globalScanner.nextLine();
		try {
			DataIO.createUser(username, password, firstname, lastname, lastname, dob, addr, type);
		} catch (PermissionException e1) {
			System.out.println("Cannot create that type of account.");
		}
	}
	private static boolean exit = false;
	private static HashMap<String, Command> cmds = new HashMap<String, Command>();
	private static void printCommandUsage(String commandname)
	{
		System.out.println("Command usage: " + cmds.get(commandname).usage);
	}
	private static void printCommandSubUsage(String commandname, String subname)
	{
		System.out.println("Usage: " + cmds.get(commandname).subUsage.get(subname));
	}
	private static void initCommands()
	{
		
		Command account = new Command("account");
		account.addSub("create", "account create <optional other users for joint>", (String[] args) ->
		{
			if(args.length == 0)
			{
				try {
					DataIO.createNewAccount(false, null);
				} catch (AccountInvalidException e) {
					e.printStackTrace();
				}
			}
			else {
				ArrayList<String> l = new ArrayList<String>();
				for(int i = 0; i < args.length; i++)
				{
					l.add(args[i]);
				}
				try {
					DataIO.createNewAccount(true, l);
				} catch (AccountInvalidException e) {
					System.out.println("A listed user does not exist; please check spelling.");
				}
			}
		});
		account.addSub("approve", "account approve <account number>", (String[] args) -> 
		{
			if(DataIO.selectedAccount == null && args.length == 0)
			{
				System.out.println("Please select an account or provide an account number first.");
			}
			else if (args.length == 0)
			{
				try {
					DataIO.selectedAccount.approve();
				} catch (PermissionException e1) {
					System.out.println("You do not have permission to approve or deny accounts.");
				}
			}
			else 
			{
				int actnum = Integer.parseInt(args[0]);
				try {
					DataIO.getAccountByID(actnum).approve();
				} catch (PermissionException e1) {
					System.out.println("You do not have permission to approve or deny accounts.");
				}
			}
		});
		account.addSub("deny", "account deny <account number>", (String[] args) -> 
		{
			if(DataIO.selectedAccount == null && args.length == 0)
			{
				System.out.println("Please select an account or provide an account number first.");
			}
			else if (args.length == 0)
			{
				try {
					DataIO.selectedAccount.deny();
				} catch (PermissionException e1) {
					System.out.println("You do not have permission to approve or deny accounts.");
				}
			}
			else 
			{
				int actnum = Integer.parseInt(args[0]);
				try {
					DataIO.getAccountByID(actnum).deny();
				} catch (PermissionException e1) {
					System.out.println("You do not have permission to approve or deny accounts.");
				}
			}
		});
		account.addSub("balance", "account balance <account number>", (String[] args) -> 
		{
			Account current = null;
			if(args.length == 0)
			{
				 current = DataIO.selectedAccount;
				 if(current == null)
				 {
					 System.out.println("Please select an account or provide an account number first.");
					 return;
				 }
			}
			else
			{
				current = DataIO.getAccountByID(Integer.parseInt(args[0]));
				if(current == null)
				{
					System.out.println("That account does not exist.");
					return;
				}
			}
			if(current.currentUserCanCheckAccount())
			{
				System.out.println("Current account balance: " + DataIO.selectedAccount.getBalance());
			}
			else {
				System.out.println("You do not have permission to check this account.");
			}
		});
		account.addSub("transfer", "account transfer <target account> <transfer amount>", (String[] args) -> 
		{
			if(DataIO.selectedAccount == null)
			{
				System.out.println("Please select an account first using account select <account number> to transfer funds from.");
				return;
			}
			if(args.length == 0)
			{
				printCommandSubUsage("account", "transfer");
				return;
			}
			try
			{
				int actnum = Integer.parseInt(args[0]);
				double transferAmount = Double.parseDouble(args[1]);
				System.out.println("Confirm you wish to transfer amount $" + transferAmount + "from account number " + DataIO.selectedAccount.getAccountNumber() + " to account number " + actnum + "(y/n)?");
				char res = globalScanner.next().charAt(0);
				if(res == 'n')
				{
					System.out.println("Transfer aborted.");
					return;
				}
				else
				{
					try {
						DataIO.selectedAccount.transfer(DataIO.getAccountByID(actnum), transferAmount);
					} catch(AccountInvalidException e) {
						System.out.println("That account is not approved or does not exist.");
					} catch (TransferDateException e) {
						System.out.println("You have exceeded your transfer limit for one of these accounts.");
					} catch (OverdrawnException e) {
						System.out.println("Your account is overdrawn.");
					}
					System.out.println("Transferred amount $" + transferAmount + " from account number " + DataIO.selectedAccount.getAccountNumber() + " to account number " + actnum + ".");
				}
			} catch (NumberFormatException e)
			{
				printCommandSubUsage("account", "transfer");
			}
			
		});
		account.addSub("cancel", "account cancel <account number> (ADMIN ONLY)", (String[] args) -> 
		{
			Account current = null;
			if(args.length == 0)
			{
				 current = DataIO.selectedAccount;
				 if(current == null)
				 {
					 System.out.println("Please select an account or provide an account number first.");
					 return;
				 }
			}
			else
			{
				current = DataIO.getAccountByID(Integer.parseInt(args[0]));
				if(current == null)
				{
					System.out.println("That account does not exist.");
					return;
				}
			}
			try {
				current.cancel();
			} catch (PermissionException e)
			{
				System.out.println("You do not have that permission.");
			}
		});
		account.addSub("list", "account list <optional user>", (String[] args) -> 
		{
			if(!(DataIO.loggedIn))
			{
				System.out.println("Please login first.");
				return;
			}
			User current = DataIO.currentUser;
			if(args.length != 0)
			{
				current = DataIO.users.get(args[0]);
			}
			if(current == null)
			{
				System.out.println("User does not exist.");
			}
			if(!(DataIO.currentUser.canViewUserData() || DataIO.currentUser.equals(current)))
			{
				System.out.println("You cannot view this information.");
				return;
			}
			if(args.length > 0 && args[0].compareTo("pending") == 0)
			{
				cmds.get("account").runSub("list-pending", args);
			}
			System.out.println("List of accounts for current user: ");
			DataIO.currentUser.listAccounts();
		});
		account.addSub("list-pending", "account list-pending", (String[] args) -> 
		{
			if(!(DataIO.currentUser.canApproveAccounts()))
			{
				System.out.println("You cannot view this information.");
			}
			else
			{
				System.out.println("List of currently pending accounts: ");
				for(Account a : DataIO.accounts)
				{
					if((a.getStatus().compareTo("pending") != 0)) continue;
					System.out.println("\tPending account number " + DataIO.accounts.indexOf(a) + " for users: ");
					a.printConnectedUsers(false);
				}
			}
		});
		account.addSub("select", "account select <account number>", (String [] args) ->
		{
			if(args.length == 0)
			{
				printCommandSubUsage("account", "select");
				return;
			}
			try {
				DataIO.selectAccount(Integer.parseInt(args[0]));
			} catch (NumberFormatException e) {
				System.out.println("Please give an account number; i.e. 'account select <number>'.");
			} catch (PermissionException e) {
				System.out.println("You do not have access to this account.");
			}
		});
		Command user = new Command("user");
		user.addSub("register", "user register", (String[] args) ->
		{
			doUserRegistration(0);
		});
		user.addSub("register-admin", "user register-admin (ADMIN ONLY)", (String[] args) ->
		{
			doUserRegistration(2);
		});
		user.addSub("register-employee", "user register-employee (ADMIN ONLY)", (String[] args) ->
		{
			doUserRegistration(1);
		});
		user.addSub("view", "user view <user name> (restricted)", (String[] args) ->
		{
			if(!(DataIO.currentUser.canViewUserData()))
			{
				System.out.println("Action is not allowed.");
			}
			else 
			{
				if(!(DataIO.users.containsKey(args[0])))
				{
					System.out.println("User does not exist.");
				}
				else 
				{
					DataIO.users.get(args[0]).printInfo();
				}
			}
		});
		user.addSub("change-pass", "user change-pass <old pass> <new pass>", (String[] args) ->
		{
			if(args.length < 2)
			{
				printCommandSubUsage("user", "change-pass");
				return;
			}
			if(DataIO.loggedIn)
			{
				DataIO.currentUser.setPassword(args[0], args[1]);
			}
		});
		Command admin = new Command("admin");
		admin.addSub("set-transfer-limit", "admin set-transfer-limit <new amount> (ADMIN ONLY)", (String[] args) ->
		{
			if(!DataIO.loggedIn || !(DataIO.currentUser.isAdmin()))
			{
				System.out.println("You do not have access to that command.");
				return;
			}
			if(args.length == 0)
			{
				printCommandSubUsage("admin", "set-transfer-limit");
			}
			try {
				Admin.setTransferLimit(Double.parseDouble(args[0]));
			} catch (NumberFormatException e1) {
				printCommandSubUsage("admin", "set-transfer-limit");
			} catch (PermissionException e1) {
				System.out.println("You do not have access to that command.");
			}
		});
		admin.addSub("set-user-password", "admin set-user-password <user> <new password> (ADMIN ONLY)", (String[] args) ->
		{
			if(!DataIO.loggedIn || !(DataIO.currentUser.isAdmin()))
			{
				System.out.println("You do not have access to that command.");
				return;
			}
			if(args.length < 2)
			{
				printCommandSubUsage("admin", "set-user-password");
			}
			DataIO.users.get(args[0]).setPassword("AdminOverride", args[1]);
		});
		Command help = new Command("help");
		cmds.put("login", new Command("login","login <user> <pass>",(String[] args) ->
		{
			if(args[0] == null || args[1] == null)
			{
				printCommandUsage("login");
			}
			else DataIO.login(args[0], args[1]);
		}));
		cmds.put("logout", new Command("logout", "logout", (String[] args) ->
		{
			DataIO.logout();
		}));
		cmds.put("exit", new Command("exit", "exit", (String[] args) ->
		{
			exit = true;
		}));
		cmds.put("account", account);
		cmds.put("user", user);
		cmds.put("admin", admin);
		for(Map.Entry<String, Command> e : cmds.entrySet())
		{
			String key = e.getKey();
			help.addSub(key, "", (String [] args) -> 
			{
				if(args[0] == null)
				{
					printCommandUsage(key);
				}
				else
				{
					printCommandSubUsage(key, args[0]);
				}
			});
		}
		cmds.put("help", help);
	}
	private static void listCmds()
	{
		StringBuilder sb = new StringBuilder("Valid commands are: [");
		for(Map.Entry<String, Command> e : cmds.entrySet())
		{
			sb.append(e.getValue().getName());
			sb.append('|');
		}
		sb.deleteCharAt(sb.lastIndexOf("|"));
		sb.append("]");
		System.out.println(sb);
	}
	private static void getAndParseLine()
	{
		String line = globalScanner.nextLine();
		String[] linesplit = line.split(" ");
		String cmd = linesplit[0];
		String sub = (linesplit.length > 1) ? linesplit[1] : null;
		String[] args = new String[linesplit.length - 1];
		
		if(cmds.containsKey(cmd))
		{
			Command cmdObj = cmds.get(cmd);
			if(cmdObj.hasSubCommands)
			{
				args = new String[linesplit.length - 2];
				for(int i = 2; i < linesplit.length; i++)
				{
					args[i - 2] = linesplit[i]; 
				}
				
			}
			else
			{
				for(int i = 1; i < linesplit.length; i++)
				{
					args[i - 1] = linesplit[i]; 
				}
			}
			if(cmdObj.hasSubCommands && sub == null)
			{
				printCommandUsage(cmd);
			}
			else if (cmdObj.hasSubCommands)
			{
				cmdObj.runSub(sub, args);
			}
			else
			{
				cmdObj.runSub("base", args);
			}
		}
		else
		{
			listCmds();
		}
	}
	@SuppressWarnings("unused")
	@Deprecated
	private static void parseCurrentCommandLine(Scanner s)
	{
		String cmd = s.nextLine();
		String[] cmdsplit = cmd.split(" ");
		int len = cmdsplit.length;
		switch(cmdsplit[0])
		{
			case "login":
				if(len > 1)
				{
					if(len < 3)
					{
						System.out.println("Please enter a password.");
					}
					else {
						DataIO.login(cmdsplit[1], cmdsplit[2]);
					}
				}
				else {
					System.out.println("Command usage: login <user> <pass>");
				}
				break;
			case "account":
			{
				if(!DataIO.loggedIn) {
					System.out.println("Please login to continue.");
				}
				else {
					switch(cmdsplit[1])
					{
					case "create":
						if(len < 2)
						{
							try {
								DataIO.createNewAccount(false, null);
							} catch (AccountInvalidException e) {
								e.printStackTrace();
							}
						}
						else {
							try {
								ArrayList<String> l = new ArrayList<String>();
								for(int i = 2; i < len; i++)
								{
									l.add(cmdsplit[i]);
								}
								DataIO.createNewAccount(true, l);
							} catch (AccountInvalidException e) {
								System.out.println("A listed user does not exist; please check spelling.");
							}
						}
						break;
					case "approve": 
					case "deny":
						if(DataIO.selectedAccount == null)
						{
							System.out.println("Please select an account first.");
							break;
						}
						try {
							if(cmdsplit[1].equals("approve"))
								DataIO.selectedAccount.approve();
							else {
								DataIO.selectedAccount.deny();
							}
						} catch (PermissionException e)
						{
							System.out.println("You do not have permission to approve or deny accounts.");
						}
						break;
					case "balance":
						if(DataIO.selectedAccount.currentUserCanCheckAccount())
						{
							System.out.println("Current account balance: " + DataIO.selectedAccount.getBalance());
						}
						else {
							System.out.println("You do not have permission to check this account.");
						}
						break;
					case "transfer":
						if(DataIO.selectedAccount.currentUserCanCheckAccount())
						{
							try {
								if(DataIO.accounts.size() >= Integer.parseInt(cmdsplit[2]))
								{
									try {
										System.out.println("Confirm you wish to transfer " + cmdsplit[3] + " dollars to account number " + cmdsplit[2] + ": y/n");
										char res = s.next().charAt(0);
										if(res == 'n')
										{
											System.out.println("Transfer aborted.");
										}
										else 
										{
											Account targetAccount = null;
											for(Account a : DataIO.accounts)
											{
												if(a.getAccountNumber() == Integer.parseInt(cmdsplit[2]))
												{
													targetAccount = a;
												}
											}
											if(targetAccount == null)
											{
												System.out.println("That target account doesn't exist.");
												break;
											}
											DataIO.selectedAccount.transfer(targetAccount, Long.parseLong(cmdsplit[3]));
											System.out.println("Transferred.");
										}
									} catch(AccountInvalidException e) {
										System.out.println("That account is not approved or does not exist.");
									} catch (TransferDateException e) {
										System.out.println("You have exceeded your transfer limit for one of these accounts.");
									} catch (OverdrawnException e) {
										System.out.println("Your account is overdrawn.");
									}
								}
							} catch (NumberFormatException e) {
								System.out.println("Command usage: account transfer <target account number> <amount>");
							}
						}
						break;
					case "cancel":
						try {
							DataIO.selectedAccount.cancel();
						} catch (PermissionException e)
						{
							System.out.println("You do not have that permission.");
						}
						break;
					case "list":
						System.out.println("List of accounts for current user: ");
						DataIO.currentUser.listAccounts();
						break;
					case "list-pending":
						if(!(DataIO.currentUser.canApproveAccounts()))
						{
							System.out.println("You cannot view this information.");
						}
						else
						{
							System.out.println("List of currently pending accounts: ");
							for(Account a : DataIO.accounts)
							{
								if((a.getStatus().compareTo("pending") != 0)) continue;
								System.out.println("\tPending account number " + DataIO.accounts.indexOf(a) + " for users: ");
								a.printConnectedUsers(false);
							}
						}
						break;
					case "select":
						try {
							DataIO.selectAccount(Integer.parseInt(cmdsplit[2]));
						} catch (NumberFormatException e) {
							System.out.println("Please give an account number; i.e. 'account select <number>'.");
						} catch (PermissionException e) {
							System.out.println("You do not have access to this account.");
						}
						break;
					default:
						System.out.println("Usage: account [list|list-pending|select|create|approve|deny|balance|transfer|cancel] <arguments>");
					}
				}
			}
			break;
			case "user":
			{
				if(len > 1)
				{
					switch(cmdsplit[1])
					{
					case "register-admin":
						if(!(DataIO.loggedIn && DataIO.currentUser.isAdmin()))
						{
							System.out.println("You cannot create an admin account.");
							break;
						}
						
					case "register-employee":
						if(!(DataIO.loggedIn && DataIO.currentUser.isAdmin()))
						{
							System.out.println("You cannot create an employee account.");
							break;
						}
						
					case "register":
						System.out.println("Input username.");
						String username = s.nextLine();
						boolean pass_set = false;
						String password = "";
						while(!pass_set)
						{
							System.out.println("Input password.");
							password = s.nextLine();
							System.out.println("Confirm password.");
							if((password.compareTo(s.nextLine()) != 0))
							{
								System.out.println("Passwords do not match.");
							}
							else {
								pass_set = true;
							}
						}
						System.out.print("Input first and last name. First name: ");
						String firstname = s.nextLine();
						System.out.print("Last name: ");
						String lastname = s.nextLine();
						System.out.println("Input date of birth.");
						String dob = s.nextLine();
						System.out.println("Input address, as one line.");
						String addr = s.nextLine();
						try {
							int type = 0;
							if(cmdsplit[1].compareTo("register-admin") == 0) type = 2;
							else if (cmdsplit[1].compareTo("register-employee") == 0) type = 1;
							DataIO.createUser(username, password, firstname, lastname, lastname, dob, addr, type);
						} catch (PermissionException e)
						{
							System.out.println("Cannot create that type of account.");
						}
						break;
					case "view":
						if(!(DataIO.currentUser.canViewUserData()))
						{
							System.out.println("Action is not allowed.");
						}
						else 
						{
							if(!(DataIO.users.containsKey(cmdsplit[2])))
							{
								System.out.println("User does not exist.");
							}
							else 
							{
								DataIO.users.get(cmdsplit[2]).printInfo();
							}
						}
						break;
					case "change-pass":
						if(cmdsplit[2] == null || cmdsplit[3] == null)
						{
							System.out.println("Usage: user change-pass <old password> <new password>");
						}
						else 
						{
							DataIO.currentUser.setPassword(cmdsplit[2], cmdsplit[3]);
						}
					default:
						System.out.println("Usage: user [register|register-admin|register-employee|view|change-pass]");
						break;
					}
				}
				else {
					System.out.println("Usage: user [register|register-admin|register-employee|view]");
				}
			}
			break;
			case "admin":
				if(!(DataIO.loggedIn) || !(DataIO.currentUser.isAdmin()))
				{
					System.out.println("You do not have access to that command.");
				}
				else
				{
					try {
						if(cmdsplit[1].compareTo("set-transfer-limit") == 0)
						{
							try {
								Admin.setTransferLimit(Integer.parseInt(cmdsplit[2]));
							} catch (NumberFormatException e){
								System.out.println("Usage: admin [set-transfer-limit|set-user-password] <amount>");
							} catch (PermissionException e) {
								System.out.println("You do not have access to that command.");
							}
						}
						else if(cmdsplit[1].compareTo("set-user-password") == 0)
						{
							if(cmdsplit[2] == null || cmdsplit[3] == null)
							{
								System.out.println("Usage: admin set-user-password <username> <password>");
							}
							DataIO.users.get(cmdsplit[2]).setPassword("AdminOverride", cmdsplit[3]);
						}
					}
					catch (IndexOutOfBoundsException e) {
						System.out.println("Usage: admin [set-transfer-limit|set-user-password] <args>");
					}
				}
				break;
			case "exit":
				exit = true;
				break;
			default:
				System.out.println("Valid commands are: [login|account|user|admin]");
				break;
		}
	}
	
	public static void main(String[] args) {
		globalScanner = new Scanner(System.in);
		initCommands();
		System.out.println("Welcome to the bank. Please enter login <username> <password> to login...");
		while(!exit)
		{
			if(DataIO.loggedIn)
			{
				System.out.print("@" + DataIO.currentUser.username);
			}
			if(DataIO.selectedAccount != null)
			{
				System.out.print("#A" + DataIO.selectedAccount.getAccountNumber());
			}
			System.out.print(">");
			getAndParseLine();
		}
		globalScanner.close();
	}

}
