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
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end Cafe

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
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
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */
    
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     Cafe.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      Cafe esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         String authorisedUser = null;
         esql = new Cafe(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorisedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }// end switch
            if (authorisedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. Goto Menu");
                  System.out.println("11. Search Menu Item");
                  System.out.println("2. Update Profile");
                  System.out.println("3. Place a Order");
                  System.out.println("4. Update a Order");
                  System.out.println(".........................");
                  System.out.println("9. Log out");
                  switch (readChoice()) {
                     case 1:
                        Menu(esql);
                        break;
                     case 11:
                        MenuItemType(esql);
                        break;
                     case 2:
                        UpdateProfile(esql);
                        break;
                     case 3:
                        PlaceOrder(esql, authorisedUser);
                        break;
                     case 4:
                        UpdateOrder(esql, authorisedUser);
                        break;
                     case 5:
                        viewOrderHistory(esql, authorisedUser);
                        break;
                     case 9:
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * 
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
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

         String type = "Customer";
         String favItems = "";

         String query = String.format(
               "INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone,
               login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println("User successfully created!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }// end CreateUser

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return login;
         return null;
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }// end

   // Rest of the functions definition go in here

   public static void Menu(Cafe esql) {
      try {
         String query = String.format("SELECT * FROM MENU");
         int menuItems = esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void MenuItemType(Cafe esql) {
      try {
         System.out.println("MENU ITEM TYPE");
         System.out.println("---------------");
         System.out.println("Enter Item Type (Drinks, Sweets, or Soup) or Name: ");
         String item = in.readLine();

         // create sql statment
         String query = String.format("SELECT itemname FROM MENU WHERE type = '%s' OR itemname = '%s'", item, item);

         System.out.println("---------------");
         esql.executeQueryAndPrintResult(query);
         System.out.println("---------------");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void UpdateProfile(Cafe esql) {
      try {
         String login = null;
         String query = null;
         String managerQuery = null;
         List<List<String>> result;
         List<List<String>> managerAuthString;
         login = LogIn(esql);

         if (login != null) {
            query = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            result = esql.executeQueryAndReturnResult(query);
            String resultString = result.get(0).get(0);

            String managerQ = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            managerAuthString = esql.executeQueryAndReturnResult(managerQ);
            String managerAuth = managerAuthString.get(0).get(0);

            if (resultString.equals("Manager ")) {
               System.out.println("Enter login of user to modify: ");
               String userLogin = in.readLine();
               managerQuery = String.format("SELECT * FROM USERS WHERE login = '%s'", userLogin);
               int success = esql.executeQueryAndPrintResult(managerQuery);
               if (success != 0) {
                  System.out.println("Enter which field you would like to edit: ");
                  System.out.println("1. Username");
                  System.out.println("2. Password");
                  System.out.println("3. Phone Number");
                  System.out.println("4. User type");
                  switch(readChoice()) {
                     case 1:
                        System.out.println("Enter new user login: ");
                        String newLogin = in.readLine();
                        String change = String.format("UPDATE USERS SET login = '%s' WHERE login = '%s'", newLogin, userLogin);
                        esql.executeUpdate(change);
                        String changed = String.format("SELECT * FROM USERS WHERE login = '%s'", newLogin);
                        System.out.println("Username changed! Updated information as follows: ");
                        int querySuc2 = esql.executeQueryAndPrintResult(changed);
                        break;
                     case 2:
                     System.out.println("Enter new password: ");
                     String newPassword = in.readLine();
                     String changeP = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", newPassword, userLogin);
                     esql.executeUpdate(changeP);
                     String changedP = String.format("SELECT * FROM USERS WHERE login = '%s'", userLogin);
                     System.out.println("Password changed! Updated information as follows: ");
                     int querySucP = esql.executeQueryAndPrintResult(changedP);
                     break;
                     case 3:
                     System.out.println("Enter new user phone number: ");
                        String newNum = in.readLine();
                        String changeN = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", newNum, userLogin);
                        esql.executeUpdate(changeN);
                        String changedN = String.format("SELECT * FROM USERS WHERE login = '%s'", userLogin);
                        System.out.println("User phone number changed! Updated information as follows: ");
                        int querySucN = esql.executeQueryAndPrintResult(changedN);
                        break;
                        case 4:
                        System.out.println("Enter new user type: ");
                        String newType = in.readLine();
                        String changeT = String.format("UPDATE USERS SET type = '%s' WHERE login = '%s'", newType, userLogin);
                        esql.executeUpdate(changeT);
                        String changedT = String.format("SELECT * FROM USERS WHERE login = '%s'", userLogin);
                        System.out.println("User type changed! Updated information as follows: ");
                        int querySucT = esql.executeQueryAndPrintResult(changedT);
                        break;
                  }
               }
               else {
                  System.out.println("No users found with that login. Exiting...");
                  return;
               }
            }

            System.out.println("Enter which field you would like to edit: ");
                  System.out.println("1. Username");
                  System.out.println("2. Password");
                  System.out.println("3. Phone Number");
                  System.out.println("4. User type");
                  switch(readChoice()) {
                     case 1:
                        System.out.println("Enter new user login: ");
                        String newLogin = in.readLine();
                        String change = String.format("UPDATE USERS SET login = '%s' WHERE login = '%s'", newLogin, login);
                        esql.executeUpdate(change);
                        String changed = String.format("SELECT * FROM USERS WHERE login = '%s'", newLogin);
                        System.out.println("Username changed! Updated information as follows: ");
                        int querySuc2 = esql.executeQueryAndPrintResult(changed);
                        break;
                     case 2:
                     System.out.println("Enter new password: ");
                     String newPassword = in.readLine();
                     String changeP = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", newPassword, login);
                     esql.executeUpdate(changeP);
                     String changedP = String.format("SELECT * FROM USERS WHERE login = '%s'", login);
                     System.out.println("Password changed! Updated information as follows: ");
                     int querySucP = esql.executeQueryAndPrintResult(changedP);
                     break;
                     case 3:
                     System.out.println("Enter new user phone number: ");
                        String newNum = in.readLine();
                        String changeN = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", newNum, login);
                        esql.executeUpdate(changeN);
                        String changedN = String.format("SELECT * FROM USERS WHERE login = '%s'", login);
                        System.out.println("User phone number changed! Updated information as follows: ");
                        int querySucN = esql.executeQueryAndPrintResult(changedN);
                        break;
                        case 4:
                        System.out.println("Enter new user type: ");
                        String newType = in.readLine();
                        String changeT = String.format("UPDATE USERS SET type = '%s' WHERE login = '%s'", newType, login);
                        esql.executeUpdate(changeT);
                        String changedT = String.format("SELECT * FROM USERS WHERE login = '%s'", login);
                        System.out.println("User type changed! Updated information as follows: ");
                        int querySucT = esql.executeQueryAndPrintResult(changedT);
                        break;
                  }

         } else {
            System.out.println("login failed. please try again");
            UpdateProfile(esql);
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }

   }

   public static void PlaceOrder(Cafe esql, String login) {
      try {
         String query = null;
         List<List<String>> result;
         String orderID;
         float price = 0.0f;

         Menu(esql);

         String item = "";
         System.out.println("What would you like to order? (Enter 'q' to complete order)");

         //query = String.format(        "ALTER TABLE ORDERS AUTO_INCREMENT=2"      );
         //2esql.executeUpdate(query);

         //orderID = result.get(0).get(0);
         //int ID = Integer.parseInt(orderID);
         //ID++;

         query = String.format("INSERT INTO ORDERS VALUES (DEFAULT,'%s','%s', CURRENT_TIMESTAMP,'%s' )", login, false, 666.666);
         esql.executeUpdate(query);

         query = String.format("SELECT orderID FROM ORDERS WHERE total = '%s'",666.666);
         result = esql.executeQueryAndReturnResult(query);
         orderID = result.get(0).get(0);

         while (!item.equals("q")) {
            item = in.readLine();
            if (item.equals("q")) {
               break;
            }
            query = String.format("SELECT * FROM MENU WHERE itemName = '%s'", item);
            int valid = esql.executeQuery(query);
            while (valid == 0) {
               System.out.println("Item by that name does not exist in the menu. Please try again.");
               item = in.readLine();
               if (item.equals("q")) {
                  break;
               }
               query = String.format("SELECT * FROM MENU WHERE itemName = '%s'", item);
               valid = esql.executeQuery(query);
            }

            query = String.format("INSERT INTO ITEMSTATUS VALUES ('%s', '%s', CURRENT_TIMESTAMP, '%s')", orderID, item, "Started");
            esql.executeUpdate(query);

            query = String.format("SELECT price FROM MENU WHERE itemName = '%s'", item);
            result = esql.executeQueryAndReturnResult(query);

            price += Float.parseFloat(result.get(0).get(0));

            
         }

         query = String.format("UPDATE ORDERS SET total = '%s' WHERE orderID = '%s'", price, orderID);
         esql.executeUpdate(query);
         System.out.println("Order has been placed with orderID: " + orderID);
         query = String.format("SELECT * FROM ORDERS WHERE orderID = '%s'",orderID);
         esql.executeQueryAndPrintResult(query);
            

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void UpdateOrder(Cafe esql, String login) {
      try {
         //String login = null;
         String query = null;
         String orderID = null;
         //login = LogIn(esql);

         String managerQuery = null;
         List<List<String>> result;
         List<List<String>> managerAuthString;

         if (login != null) {
            query = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            result = esql.executeQueryAndReturnResult(query);
            String resultString = result.get(0).get(0);

            String managerQ = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            managerAuthString = esql.executeQueryAndReturnResult(managerQ);
            String managerAuth = managerAuthString.get(0).get(0);

            if (resultString.equals("Manager ") || resultString.equals("Employee ")) {
               System.out.println("Enter login of user of order to update: ");
               login = in.readLine();
               query = String.format("SELECT * from ORDERS where login = '%s'", login);
               esql.executeQueryAndPrintResult(query);
               System.out.println("Enter orderID of order to change to paid: ");
               orderID = in.readLine();
               query = String.format("UPDATE ORDERS set paid = '%s' where orderID = '%s'", true, orderID);
               esql.executeUpdate(query);
               query = String.format("UPDATE ITEMSTATUS set lastUpdated = CURRENT_TIMESTAMP where orderID = '%s'", orderID);
               esql.executeUpdate(query);
               System.out.println("Updated paid order status.");
               query = String.format("SELECT * FROM ORDERS where orderID = '%s'", orderID);
               esql.executeQueryAndPrintResult(query);
            }
            else {

               query = String.format("SELECT * from ORDERS where login = '%s'", login);
               esql.executeQueryAndPrintResult(query);
               System.out.println("Enter orderID of order to update: ");
               orderID = in.readLine();
               query = String.format("SELECT paid FROM ORDERS WHERE orderID = '%s'", orderID);
               result = esql.executeQueryAndReturnResult(query);
               String isPaid = result.get(0).get(0);
               if (isPaid.equals("f")) {
                  query = String.format("SELECT * FROM ITEMSTATUS WHERE orderID = '%s'", orderID);
                  int numOrders = esql.executeQueryAndPrintResult(query);

                  System.out.println("Would you like to remove or add items to this order? (0 for remove, 1 for add): ");

                  switch(readChoice()) {
                     case 0: 

                        System.out.println("Enter the name of the item you want removed from your order: ");
                        String choice = in.readLine();
                        query = String.format("DELETE FROM ITEMSTATUS WHERE orderid = '%s' AND itemName = '%s'", orderID, choice);
                        esql.executeUpdate(query);
                        query = String.format("SELECT price FROM MENU WHERE itemName = '%s'", choice);
                        result = esql.executeQueryAndReturnResult(query);
                        float itemPrice = Float.parseFloat(result.get(0).get(0));
                        query = String.format("SELECT total FROM ORDERS WHERE orderID = '%s'", orderID);
                        result = esql.executeQueryAndReturnResult(query);
                        float totalPrice = Float.parseFloat(result.get(0).get(0));
                        totalPrice -= itemPrice;
                        query = String.format("UPDATE ORDERS SET total = '%s' WHERE orderID = '%s'", totalPrice, orderID);
                        esql.executeUpdate(query);
                        System.out.println("Removed " + choice + " from orderID " + orderID);
                        System.out.println("New order total: $" + totalPrice);
                        break;

                     case 1:
                     System.out.println("Would you like to remove or add items to this order? (0 for remove, 1 for add): ");
                     System.out.println("Enter the name of the item you want to add to your order: ");
                     String choice1 = in.readLine();
                     //query = String.format("DELETE FROM ITEMSTATUS WHERE itemName = '%s'", choice1);
                     //esql.executeUpdate(query);
                     query = String.format("SELECT price FROM MENU WHERE itemName = '%s'", choice1);
                     result = esql.executeQueryAndReturnResult(query);
                     float itemPrice1 = Float.parseFloat(result.get(0).get(0));
                     query = String.format("SELECT total FROM ORDERS WHERE orderID = '%s'", orderID);
                     result = esql.executeQueryAndReturnResult(query);
                     float totalPrice1 = Float.parseFloat(result.get(0).get(0));
                     totalPrice1 += itemPrice1;
                     query = String.format("UPDATE ORDERS SET total = '%s' WHERE orderID = '%s'", totalPrice1, orderID);
                     esql.executeUpdate(query);
                     System.out.println("Added " + choice1 + " to orderID " + orderID);
                     System.out.println("New order total: $" + totalPrice1);

                     break;
                  }

            }
                  
            
               
               else {
                  query = String.format("SELECT * FROM ORDERS WHERE orderID = '%s'", orderID);
                  esql.executeQueryAndPrintResult(query);
                  System.out.println("Order has been paid, changes cannot be made at this time.");
                  return;
               }
         }}         
         else {
            System.out.println("login failed. please try again");
            UpdateProfile(esql);
         }
      
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

public static void viewOrderHistory(Cafe esql, String login) { 

   try {
      String query = null;
         String orderID = null;
         int orders;
         //login = LogIn(esql);

         String managerQuery = null;
         List<List<String>> result;
         List<List<String>> managerAuthString;

         if (login != null) {
            query = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            result = esql.executeQueryAndReturnResult(query);
            String resultString = result.get(0).get(0);

            String managerQ = String.format("SELECT type FROM USERS WHERE login = '%s'", login);
            managerAuthString = esql.executeQueryAndReturnResult(managerQ);
            String managerAuth = managerAuthString.get(0).get(0);

            if (resultString.equals("Manager ") || resultString.equals("Employee ")) {
               query = String.format("SELECT * FROM ORDERS WHERE paid = '%s' AND timeStampRecieved >= NOW() - '1 day'::INTERVAL ", false);
               orders = esql.executeQueryAndPrintResult(query);
            }
            else {
               query = String.format("SELECT * FROM ORDERS WHERE login = '%s' ORDER BY timeStampRecieved DESC LIMIT 5", login);
               orders = esql.executeQueryAndPrintResult(query);
            }
            if (orders == 0) {
               System.out.println("No orders within 24 hours found.");
            }
         }          else {
            System.out.println("login failed. please try again");
            UpdateProfile(esql);
         }



   } catch (Exception e) {
      System.err.println(e.getMessage());
   }

}




}// end Cafe

