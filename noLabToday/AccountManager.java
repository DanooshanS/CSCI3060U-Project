package noLabToday;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
* Class handles the users accounts and verifying their information
*
* @author Danooshan Sureshkumar, Matthew Wierzbicki, and Daniel Bullock
* (Team No Lab Today)
* @version 0.2
* @since 2019-03-05
*/

// get filenames from main
public class AccountManager extends Main {
  public String username; // user name of current user
  public String type;     // account type of that user (AA, FS, BS, SS)
  public double credit;   // the amount of credit in that users account

  /**
  * Checks that the user that was being created has a unique username
  * @param String username - the user name of the current user
  * @return: True if: User to be created does exist in database
  *          False if: User to be created does NOT exists in database
  */
  public boolean checkUserIntegrity(List<String> userList, String username) {
    // check if username is already in users.ua
    for (String line: userList) {
      if (line.substring(0, 15).trim().equals(username.trim())) {
        System.out.println("User exists in the database.");
  			return true;
      }
    }
    // User does not exist in users.ua
    System.out.println("User is not in database.");
    return false;
  }

  /**
  * Create the new user passed in from the daily trans file
  * @param List<String> userList - a list of all the users in the system
  * @param String trans_line - containing the current line from the daily trans file
  * @return: The List<String> userList with the newly created user
  */
  public List<String> create(List<String> userList, String trans_line) {
    // get trans_line user info in seperate variables
    username = trans_line.substring(3, 19);
    type = trans_line.substring(19, 22);
    String credit_string = trans_line.substring(22, 31);

    // combine the user info
    String combined_user = username + type + credit_string;

    // check if user is in users.ua, if not, add (create)
    if (checkUserIntegrity(userList, username) == false) {
      System.out.println("Creating user: " + username);
      userList.add(combined_user);
    }

    return userList;
  }

  /**
  * Delete a user passed in from the daily trans file
  * @param List<String> userList - a list of all the users in the system
  * @param String trans_line - containing the current line from the daily trans file
  * @return: The List<String> userList without the deleted user
  */
  public List<String> delete(List<String> userList, String trans_line) {
    // finds the username and delete that index
    int i = 0, deletion_index = 0;
    username = trans_line.substring(3, 19);

    // get userinfo in userList
    for (String line : userList) {
			if (line != null) {
        if (line.substring(0, 15).trim().equals(username.trim())) {
          deletion_index = i;
        }
        i++;
      }
		}

    // check if user is in users.ua, if it is, remove (delete)
    if (checkUserIntegrity(userList, username) == true) {
      System.out.println("Deleting user: " + username);
      userList.remove(userList.get(deletion_index));
    }
    return userList;
  }

  /**
  * refund the users passed in from the daily trans file
  * @param List<String> userList - a list of all the users in the system
  * @param String trans_line - containing the current line from the daily trans file
  * @return: The List<String> userList with the user refunded their credits
  */
  public List<String> refund(List<String> userList, String trans_line) {
    // get trans_line user info in seperate variables
    username = trans_line.substring(3, 19);
    String seller_username = trans_line.substring(19, 35);
    // amount to get refunded
    credit = Double.parseDouble(trans_line.substring(35, 44));
    String credit_string = trans_line.substring(35, 44);
    // buyer and seller variables
    Double new_buyer_credit = 0.00, new_seller_credit = 0.00;
    String new_buyer_credit_str = "", new_seller_credit_str = "";
    String seller_type = "";

    // find the users' index in the userList
    int i = 0, buyer_index = 0, seller_index = 0;
    for (String line : userList) {
			if (line != null) {
        if (line.substring(0, 15).trim().equals(username.trim())) { buyer_index = i; }
        if (line.substring(0, 15).trim().equals(seller_username.trim())) { seller_index = i; }
        i++;
      }
    }

    // Check if the user giving the refund has enough credit to give the refund
    if (Double.parseDouble(userList.get(buyer_index).substring(19, 28).trim()) - credit >= 0) {
      new_buyer_credit = Double.parseDouble(userList.get(buyer_index).substring(19, 28).trim()) + credit;
      new_seller_credit =  Double.parseDouble(userList.get(seller_index).substring(19, 28).trim()) - credit;
      type = userList.get(buyer_index).substring(16, 19);
      seller_type = userList.get(seller_index).substring(16, 19);

      // add the leading zeros to the string
      new_buyer_credit_str = ("00000000" + String.valueOf(new_buyer_credit)).substring(String.valueOf(new_buyer_credit).length());
      new_seller_credit_str = ("00000000" + String.valueOf(new_seller_credit)).substring(String.valueOf(new_seller_credit).length());

      // if only one decimal place in string add another "0"
      if (new_buyer_credit_str.length() == 8) { new_buyer_credit_str += "0"; }
      if (new_seller_credit_str.length() == 8) { new_seller_credit_str += "0"; }
    } else {
      System.out.println("ERROR: Credit cannot be refunded fully since seller does not have enough credit");
      return userList;
    }

    // combine the user info
    String combined_buy_user = username + type + new_buyer_credit_str;
    String combined_sell_user = seller_username + seller_type + new_seller_credit_str;

    // if buyer and seller are in users file
    if (checkUserIntegrity(userList, username) == true && checkUserIntegrity(userList, seller_username) == true) {
      System.out.println("Adding " + credit_string + " to user: " + username);
      // check that user is not refunding to themself
      if (!username.equals(seller_username)) {
        // remove the old versions of the buyer/seller from the list and add the new ones
        userList.remove(buyer_index);
        userList.add(buyer_index, combined_buy_user);
        userList.remove(seller_index);
        userList.add(seller_index, combined_sell_user);
      } else {
        System.out.println("ERROR: You cannot refund yourself");
      }
    } else {
      System.out.println("ERROR: One of the users is not in the system");
    }
    return userList;
  }

  /**
  * Addcredit to a user passed in the daily trans file
  * @param List<String> userList - a list of all the users in the system
  * @param String trans_line - containing the current line from the daily trans file
  * @return: The List<String> userList with the user added more credit
  */
  public List<String> addCredit(List<String> userList, String trans_line) {
    // get trans_line user info in seperate variables
    username = trans_line.substring(3, 19);
    type = trans_line.substring(19, 22);
    credit = Double.parseDouble(trans_line.substring(22, 31));
    String credit_string = trans_line.substring(22, 31);

    // find the user who is getting the addCredit
    int i = 0, index = 0;
    for (String line : userList) {
			if (line != null) {
        if (line.substring(0, 15).trim().equals(username.trim())) { index = i; }
        i++;
      }
    }
    System.out.println(userList);
    Double new_credit = Double.parseDouble(userList.get(index).substring(19, 28).trim()) + credit;
    // the credit string of the user getting credit added to them
    // add the leading zeros to the string
    String new_credit_string = ("00000000" + String.valueOf(new_credit)).substring(String.valueOf(new_credit).length());
    // if only one decimal place in string add another "0"
    if (new_credit_string.length() == 8) { new_credit_string += "0"; }
    // combine the user info
    String combined_user = username + type + new_credit_string;
    // check if user is in users.ua, if they are, add (addCredit)
    if (checkUserIntegrity(userList, username) == true) {
      System.out.println("Adding " + credit_string + " to user: " + username);
      // remove from the list
      userList.remove(index);

      // add back the user with credit added
      userList.add(index, combined_user);
    } else {
      System.out.println("User to addcredit to does not exist in database");
    }
    return userList;
  }

}
