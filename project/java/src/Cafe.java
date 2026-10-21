/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         // Host defaults to localhost; in Docker it's set via the DB_HOST env var.
         String host = System.getenv("DB_HOST");
         if (host == null || host.isEmpty()) host = "localhost";
         String url = "jdbc:postgresql://" + host + ":" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql,authorisedUser); break;
                   case 2: UpdateProfile(esql, authorisedUser); break;
                   case 3: PlaceOrder(esql, authorisedUser); break;
                   case 4: UpdateOrder(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
		System.out.print("Login Successful\n");
		return login;
	 }
	 System.out.print("Incorrect Login or Password or No User Exists\n");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void Menu(Cafe esql, String currUser){
   try{
	boolean MenuOn = true;
	while(MenuOn){
	System.out.println("Menu Options");
            System.out.println("---------");
            System.out.println("1. View Full Menu");
            System.out.println("2. Search Menu by type");
	    System.out.println("3. Search Menu by name");
	    System.out.println("4. Mangager Options");
	    System.out.println(".......................");	
            System.out.println("9. < EXIT");
	switch(readChoice()){      
      case 1: 
		String FullMenu = String.format("SELECT itemName, price, type, description FROM Menu");
       		System.out.println("Our Current Full Menu Offerings");
		esql.executeQueryAndPrintResult(FullMenu);
		break;
	case 2: 
		System.out.print("Enter type: ");
		String MenuType = in.readLine();
	        String TypeCheck = String.format("SELECT type FROM MENU WHERE type= '%s'", MenuType);
		int typeCheck =	esql.executeQuery(TypeCheck);
		if(typeCheck>0){
			String TypeMenu = String.format("SELECT itemName, price, description FROM Menu WHERE type= '%s'", MenuType);
	                System.out.printf("Our Current %s Offerings\n", MenuType);
			 esql.executeQueryAndPrintResult(TypeMenu);
			break;
		}	
		System.out.println("Invalid type, available types are: ");
		String AvailableTypes = String.format("SELECT DISTINCT M.type FROM (SELECT M2.type FROM Menu M2) AS M");

		esql.executeQueryAndPrintResult(AvailableTypes);
                break;
	case 3:
		System.out.print("Enter Item Name: ");
                String MenuItemName = in.readLine();
                String ItemCheck = String.format("SELECT itemName FROM MENU WHERE itemName= '%s'", MenuItemName);
                int MenuItemCheck = esql.executeQuery(ItemCheck);
                if(MenuItemCheck>0){
                        String ItemMenu = String.format("SELECT itemName, price, description FROM Menu WHERE itemname= '%s'", MenuItemName);
                        System.out.printf("Our Current %s Offerings\n", MenuItemName);
                         esql.executeQueryAndPrintResult(ItemMenu);
                        break;
                }
                System.out.println("Invalid Item Name, search the full menu with option 1");
               
                break;
	case 4:
		String isManagerQuery = String.format("SELECT * FROM USERS WHERE login = '%s' AND type = 'Manager'", currUser);
		int isManager = esql.executeQuery(isManagerQuery);
		if(isManager<1){
			System.out.println("Unfortunately, you are not a manager, if this is a mistake please call tech support at ucr-ucr-ucrr");
			break;
		}
			System.out.println("hello manager, what would you like to do");
			boolean managerMenu = true;
			while(managerMenu){
			System.out.println("Manager MENU Options");
	                System.out.println("---------");
	                System.out.println("1. Add Menu Items");
	                System.out.println("2. Delete Menu Items");
	                System.out.println("3. Update Menu items");
	                System.out.println(".........................");
	                System.out.println("9. EXIT");
				switch(readChoice()){
					case 1: System.out.println("\tAdd Menu Item");
						System.out.println("\t==================================");
						System.out.print("\tEnter Menu Item Name(required): ");
         					String itemName = in.readLine();
         					System.out.print("\tEnter Menu Item Type(required): ");
         					String itemType = in.readLine();
         					boolean isdouble = false;
						System.out.print("\tEnter Menu Item Price(in $s, required): ");
                                                String itemPrice = in.readLine();

						while(isdouble == false){
							try{
								double doubleCheck = Double.parseDouble(itemPrice);
								isdouble = true;
							}catch(NumberFormatException e){
							System.out.println("\tInput is not a number!!!");
							System.out.print("\tEnter Menu Item Price(in $s, required): ");
                                                        itemPrice = in.readLine();
							}
						}
						System.out.print("\tEnter Menu Item Description (Optional, press enter to skip): ");
         					String itemDesc = in.readLine();
         					System.out.print("\tEnter Menu Item URL (Optional, press enter to skip): ");
         					String URL = in.readLine();
						String addItem = String.format("INSERT INTO MENU (itemName, type, price, description, imageURL) VALUES ('%s','%s','%s','%s','%s')", itemName, itemType, itemPrice, itemDesc, URL);
						esql.executeUpdate(addItem);
						System.out.println("Item added");
						break;
					case 2: System.out.println("\tDelete Menu Item");
						System.out.println("\t=================================");
                                                System.out.print("\tEnter Menu Item Name to delete: ");
                                                String deleteItemName = in.readLine();
						String deleteCheck = String.format("SELECT * FROM Menu WHERE itemName = '%s'", deleteItemName);
						int deleteChecker = esql.executeQuery(deleteCheck);
						if(deleteChecker<1){
							System.out.println("There doesn't seem to be an item with that name, please check spelling and the full menu by entering 9 then 1.");
							break;
						} 
						String deleteQuery = String.format("DELETE FROM Menu WHERE itemName = '%s'", deleteItemName);
						esql.executeUpdate(deleteQuery);
						System.out.printf("%s deleted\n", deleteItemName);
						break;
					case 3: System.out.println("\tEntered Update Menu Items");
                                                System.out.println("\t=================================");
                                                System.out.print("\tEnter Menu Item Name to uptdate: ");
						String updateItemName = in.readLine();
                                                String updateCheck = String.format("SELECT * FROM Menu WHERE itemName = '%s'", updateItemName);
                                                int updateChecker = esql.executeQuery(updateCheck);
                                                if(updateChecker<1){
                                                        System.out.println("There doesn't seem to be an item with that name, please check spelling and the full menu by entering 9 then 1.");
                                                        break;
                                                }
						boolean updateMenu = true;
						while(updateMenu){
						System.out.println("What do you want to update?");
               					System.out.println("---------");
						System.out.println("Please delete and create a new item if you wish to change the name itself");
                				System.out.println("1. type");
                				System.out.println("2. price");
                				System.out.println("3. description");
                				System.out.println("4. imageURL");
                				System.out.println(".........................");
                				System.out.println("9. leave");
						switch(readChoice()){
						case 1: 
							System.out.printf("\tEnter the new type for %s:", updateItemName);
							String newType = in.readLine();
							String updateType = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", newType, updateItemName);
							esql.executeUpdate(updateType);
							System.out.println("type updated");
							break;
                   				case 2: 
							System.out.printf("\tEnter the new price for %s: ", updateItemName);
                                                        String newPrice = in.readLine();
							boolean isPriceDouble = false;
							while(isPriceDouble == false){
                                                        	try{
                                                                	double doubleCheck = Double.parseDouble(newPrice);
                                                                	isPriceDouble = true;
                                                        	}catch(NumberFormatException e){
                                                        	System.out.println("\tInput is not a number!!!");
                                                        	System.out.printf("\tEnter the new price for %s: ", updateItemName);
                                                        	newPrice = in.readLine();
                                                        	}
                                                	}

                                                        String updatePrice = String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'", newPrice, updateItemName);
                                                        esql.executeUpdate(updatePrice);
                                                        System.out.println("Price updated");
                                                        break;

                   				case 3:  
							System.out.printf("\tEnter the description for %s:", updateItemName);
                                                        String newDesc = in.readLine();
                                                        String updateDesc = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", newDesc, updateItemName);
                                                        esql.executeUpdate(updateDesc);
                                                        System.out.println("Description updated");
                                                       
							break;
                   				case 4:  
							System.out.printf("\tEnter the new URL for %s:", updateItemName);
                                                        String newURL = in.readLine();
                                                        String updateURL = String.format("UPDATE Menu SET imageURL = '%s' WHERE itemName = '%s'", newURL, updateItemName);
                                                        esql.executeUpdate(updateURL);
                                                        System.out.println("URL updated");
                                                        
							break;
                   				case 9: updateMenu = false; break;
                   				default : System.out.println("Unrecognized choice!"); break;

						}
						}
                                                break;
					case 9: managerMenu = false;
                                                break;
					default: System.out.println("Invalid choice, try again");
                                                break;

				}
			}

		break;
	case 9: MenuOn = false; break;
	default: System.out.println("Unrecognized choice!"); break;
	}
	
}
   }catch(Exception e){
      System.err.println (e.getMessage ());
      
   }
  }

  public static void UpdateProfile(Cafe esql, String currUser){
try{
	boolean userUpdate = true;
	while(userUpdate) {
		System.out.println("Update Profile Options");
		System.out.println("------------");
		System.out.println("1. Update Phone Number");
		System.out.println("2. Update Password");
		System.out.println("3. Update Favorite Item(s)");
		System.out.println("4. Manager Option");
		System.out.println("...........................");
		System.out.println("9. < EXIT");
		
		switch(readChoice()){
		case 1:
			System.out.printf("Enter new phone number (with no dashes): ");
			String newNum = in.readLine();

			boolean yesNumber = true;
			for (int i = 0; i < newNum.length(); ++i) {
				if(Character.isLetter(newNum.charAt(i))) {
					yesNumber = false;
					System.out.println("This isn't all numbers");
					break;
				}
			}
			if (yesNumber == true && newNum.length() == 10) {
				String updatePhone = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", newNum, currUser);
				esql.executeUpdate(updatePhone);
				System.out.println("Phone Number Updated!");
				String printInfo = String.format("SELECT login, phoneNum FROM Users WHERE login = '%s'", currUser);
				esql.executeQueryAndPrintResult(printInfo);
			}
			else {
				System.out.println("Invalid phone number. Please try again.");
			}
			break;
		case 2:

			System.out.println("Enter current password: ");
			String currPass = in.readLine();
			
			String userChecker = String.format("SELECT password FROM Users WHERE password = '%s' AND login = '%s'", currPass, currUser);
			
			int realUser = esql.executeQuery(userChecker);
			if(realUser>0){
				System.out.println("Enter new password: ");
				String newPass = in.readLine();

				String updatePass = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", newPass, currUser);
				esql.executeUpdate(updatePass);
				System.out.println("Password successfully changed!");
				break;
			}
			else {
				System.out.println("Invalid password. Please try again");
			}
			break;
		case 3:
		boolean updateFav = true;
		while(updateFav) {
			System.out.println("What would you like to do to your Favorite Items list?");
			System.out.println("-------------------------------------------------------");
			System.out.println("1. View Favorite Items");
			System.out.println("2. Update Favorite Item");
			System.out.println("-------------------------------------------------------");
			System.out.println("9. Leave");
					
			switch(readChoice()) {
			case 1:
				String viewFav = String.format("SELECT favItems FROM Users WHERE login = '%s'", currUser);
				esql.executeQueryAndPrintResult(viewFav);
				break;
			case 2: 
				System.out.println("What would you like to change your favorite item to?");
				String fullMenu = String.format("SELECT itemName FROM Menu");
				esql.executeQueryAndPrintResult(fullMenu);
				String readInput = in.readLine();
				String actuallyReal = String.format("SELECT * FROM Menu WHERE itemName = '%s'", readInput);
				int realCount = esql.executeQuery(actuallyReal);
				if (realCount > 0) {
					String updateFavItem = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", readInput, currUser);
					esql.executeQuery(updateFavItem);
					
				}else {
					System.out.println("Menu item does not exist. Please pick an item from the menu: ");
					esql.executeQueryAndPrintResult(fullMenu);
				}
				break;
			case 9:
				updateFav = false;
				break;
			}
		}
		case 4:
			String yesManager = String.format("SELECT * FROM Users WHERE login = '%s' AND type = 'Manager'", currUser);
			int yesIsManager = esql.executeQuery(yesManager);
			if (yesIsManager < 1) {
				System.out.println("Unfortunately, you are not a manager. If this is a mistake, please contact tech support.");
				break;
			}

			System.out.println("Hello Manager! Who would you like to change the user type?");
			String changeUser = in.readLine();
			String checkUserExists = String.format("SELECT login From Users WHERE login = '%s'", changeUser);
				
			int changeUserCheck = esql.executeQuery(checkUserExists);
			if (changeUserCheck > 0) {
				String printUserType = String.format("SELECT login, type FROM Users WHERE login = '%s'", changeUser);
				esql.executeQueryAndPrintResult(printUserType);
				String currUserType = String.format("SELECT type FROM Users WHERE login = '%s'", changeUser);
				System.out.println("What would you like to change " + changeUser +" to?");
				String newType = in.readLine();
					
					
				String change = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", newType, changeUser);
				esql.executeUpdate(change);
				String updateChange = String.format("SELECT login, type FROM Users Where login = '%s'", changeUser);
				System.out.println(changeUser + " User Type has successfully been changed!");
				esql.executeQueryAndPrintResult(updateChange);
			}
			else {
				System.out.println("Sorry " + changeUser + " does not exist. Try again.");
			}
			break;
			
		case 9: 
			userUpdate = false;
			break;
		default:
			System.out.println("Unrecognized choice!");
			break;
		}
	}
}catch(Exception e) {
	System.err.println (e.getMessage());
}
	
}			


  public static void PlaceOrder(Cafe esql, String currUser){
	try{
	System.out.println("What would you like to order?");
	System.out.print("Item name: ");
	String newOrder = in.readLine();
	String checkOrder = String.format("SELECT * FROM Menu WHERE itemName = '%s'", newOrder);
	int OrderChecker = esql.executeQuery(checkOrder);
	while(OrderChecker < 1){
		System.out.println("This doesn't seem to be an item we offer");
		System.out.println("Would you like to bring up the menu to see the choices (Yes)");
		String answer = in.readLine();
		 if(answer.equals("Yes")){
			String bringMenu = String.format("SELECT itemName FROM Menu");
			esql.executeQueryAndPrintResult(bringMenu);

                }
		System.out.print("Item name: ");
		newOrder = in.readLine();
		String isRealItem = String.format("SELECT * FROM Menu Where itemName = '%s'", newOrder);
		int realCount = esql.executeQuery(isRealItem);
		
		if (realCount > 0) {
			OrderChecker = OrderChecker + 1;
		}
	}
	boolean paid = false;
	String OrderQuery = String.format("INSERT INTO Orders (login, paid, timeStampRecieved, total) VALUES ('%s',false, NOW(),(SELECT price FROM Menu WHERE itemName = '%s'))", currUser, newOrder);
	esql.executeUpdate(OrderQuery);
	System.out.println("Order successfully placed!");
	System.out.println("Here is your order confirmation: ");

	String printPlacedOrder = String.format("SELECT * FROM Orders S WHERE S.orderid = (SELECT MAX(S1.orderid) FROM Orders S1)");
	esql.executeQueryAndPrintResult(printPlacedOrder);
	return;
	}catch(Exception e){
		System.err.println(e.getMessage());
}
	
	
	}

  public static void UpdateOrder(Cafe esql, String currUser){
	try{
		String isCustomerQuery = String.format("SELECT * FROM USERS WHERE login = '%s' AND type = 'Customer'", currUser);
		int isCustomer = esql.executeQuery(isCustomerQuery);
	if(isCustomer >0){
	boolean OrderMenuCustomer = true;
	while(OrderMenuCustomer){
	    System.out.println("Update Order Options");
            System.out.println("---------");
            System.out.println("1. Show Order History");
            System.out.println("2. Delete non-paid order");
	   
	    System.out.println(".......................");	
            System.out.println("9. < EXIT");
		switch(readChoice()){
		case 1: 
			String orderHistoryQuery = String.format("SELECT * FROM Orders WHERE login = '%s' ORDER BY timeStampRecieved desc limit 5", currUser);
			esql.executeQueryAndPrintResult(orderHistoryQuery);
			break;
		case 2: 
			System.out.println("Please enter the orderid for the item you would like to delete: ");
			String verifyOrder = in.readLine();
			String checkItExists = String.format("SELECT * FROM Orders WHERE orderid = '%s' AND paid ='f'", verifyOrder);
			int existValue = esql.executeQuery(checkItExists);
			
			if (existValue > 0) {
			String deleteNonPaid = String.format("DELETE FROM Orders WHERE paid = 'f' AND orderid = '%s' AND login = '%s'", verifyOrder, currUser);
			esql. executeUpdate(deleteNonPaid);
			System.out.println("Order " + verifyOrder + " has successfully been cancelled.");
			}
			else {
				System.out.println("Invalid order number. Please check your past orders and try again.");
			}	
			break;

		case 9: OrderMenuCustomer = false; break;
		default: System.out.println("Unrecognized choice!"); break;
		}	
	}
	}else if(isCustomer<1){//is a manager or employee
		 boolean OrderMenuEmpMan = true;
	        while(OrderMenuEmpMan){
        	    System.out.println("Update Order Employee/Manager Options");
	            System.out.println("---------");
	            System.out.println("1. Show Order History");
	            System.out.println("2. Delete non-paid order");
		    System.out.println("3. Browse Pending orders");
		    System.out.println("4. Change an order to paid");
	            System.out.println(".......................");
	            System.out.println("9. < EXIT");
	                switch(readChoice()){
	                case 1:
	                        String orderHistoryQuery = String.format("SELECT * FROM Orders WHERE login = '%s' ORDER BY timeStampRecieved desc limit 5", currUser);
	                        esql.executeQueryAndPrintResult(orderHistoryQuery);
	                        break;

	                case 2: 
		            	System.out.println("Please enter the orderid for the item you would like to delete: ");
            			String verifyOrder = in.readLine();
            			String checkItExists = String.format("SELECT * FROM Orders WHERE orderid = '%s' AND paid = 'f'", verifyOrder);
            			int existValue = esql.executeQuery(checkItExists);

            			if (existValue > 0) {
            				String deleteNonPaid = String.format("DELETE FROM Orders WHERE paid = 'f' AND orderid = '%s'", verifyOrder);
            				esql. executeUpdate(deleteNonPaid);
            				System.out.println("Order " + verifyOrder + " has successfully been cancelled.");
        			}
        			else {
			                System.out.println("Invalid order number. Please check your past orders and try again.");
            			}
				break;
			case 3: 
				String orderPendingQuery = String.format("SELECT * FROM Orders WHERE paid = 'f' AND timeStampRecieved > (now() - interval '1 day')");
				esql.executeQueryAndPrintResult(orderPendingQuery);
				break;
			case 4: 
				System.out.print("Please enter the order id of the order to change to paid: ");
				String stringId = in.readLine();

				String IdChecker = String.format("SELECT * FROM Orders WHERE orderid = %s AND paid = 'f'", stringId);
				int checkIsReal = esql.executeQuery(IdChecker);
				if(checkIsReal < 1){
					System.out.println("This doesn't seem to be a real order or an unpaid order");
		
				}
				String changePaid = String.format("UPDATE Orders SET paid = 't' WHERE orderid = %s", stringId);
				esql.executeUpdate(changePaid);
				break;
	                case 9: OrderMenuEmpMan = false; break;
	                default: System.out.println("Unrecognized choice!"); break;
	                }

		}
	}
	return;
	}catch(Exception e){
		System.err.println(e.getMessage());
	}
}

}//end Cafe

