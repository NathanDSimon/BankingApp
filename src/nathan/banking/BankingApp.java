package nathan.banking;

import java.util.ArrayList;
import java.util.Scanner;

public class BankingApp {

	private static String getNextCommandLine(Scanner s)
	{
		String cmd = s.nextLine();
		return cmd;
	}
	
	private static void parseCurrentCommandLine(Scanner s, String cmd)
	{
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
								DataIO.selectedAccount.approveBy(DataIO.currentUser);
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
					default:
						System.out.println("Usage: user [register|register-admin|register-employee|view]");
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
						if(cmdsplit[1].equals("set-transfer-limit"))
						{
							try {
								Admin.setTransferLimit(Integer.parseInt(cmdsplit[2]));
							} catch (NumberFormatException e){
								System.out.println("Usage: admin set-transfer-limit <amount>");
							} catch (PermissionException e) {
								System.out.println("You do not have access to that command.");
							}
						}
					}
					catch (IndexOutOfBoundsException e) {
						System.out.println("Usage: admin set-transfer-limit <amount>");
					}
				}
				break;
			case "exit":
				s.close();
				System.exit(0);
			default:
				System.out.println("Valid commands are: [login|account|user|admin]");
				break;
		}
	}
	
	public static void main(String[] args) {
		DataIO.init();
		Scanner s = new Scanner(System.in);
		System.out.println("Welcome to the bank. Please enter a command.");
		String cmd = getNextCommandLine(s);
		while(cmd != "exit")
		{
			parseCurrentCommandLine(s, cmd);
			cmd = getNextCommandLine(s);
		}
		s.close();
	}

}
