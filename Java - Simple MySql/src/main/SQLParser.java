package main;

import java.util.Arrays;

public class SQLParser {
	public Database db;

	//constructor
	public SQLParser(Database db) {
		this.db = db;
	}
	
	
	public void parse(String sql) {
		
		if (sql.contains("UNDO")) {
			this.db.undo();
		}else {
		//converts the entered string into an array of string
		String[] arr = sql.split(" ");
		//check the first word of the string to determine the funciton
		String first = arr[0].toUpperCase(); 
		String tablename = extractStringFromBrackets(sql); //between[] is a string of the table name
		String[] args = extractStringsFromParens(sql); //between () is a string array
		
		//if the first word is "CREATE", it should constructs and adds a new Table to the Database
		if(first.equals("CREATE")) {
			TableColumns column = new TableColumns(args); //reverse the string array into a TableColumns
			Table table = new Table(tablename, column); //create a table
			this.db.addTable(table); //add this table to our database
		}
		
		//if the first word is "INSERT", it should inserts a Row into a Table
		else if(first.equals("INSERT")) {
			this.db.addRowToTable(tablename, args); //insert the row to the given table
		}
		
		/*
		 * if the first word is "SELECT", it should print out all those Cells 
		 * corresponding to the Columns to the console, as one line per Row
		 */
		else if(first.equals("SELECT")){
			//find alias - the column name we want to sort
			String alias = extractOrderByAlias(sql);
			//create result table to be printed out
			Table result = null;
			int indexOfAlias = 0;
			//find where the alias is in the database
			for (Table t : this.db.databases) {
				for (String s : t.column.getColumnList()) {
					if (s.equals(alias)) {
						result = t;
						indexOfAlias = t.column.getColumn(alias);
					}
				}
			}	
			
			//if it also ask for the join on command
			if(sql.contains("JOIN")) {
				
				//find the index of "JOIN" appears in the given string
				int indexofjoin = sql.indexOf("JOIN");
				//find the name of foreign table that is joined on the local table
				// This will look for the table name located between brackets after the command "JOIN"
				String tablename2 = extractStringFromBrackets(sql.substring(indexofjoin));
				//find the index of string "on" in the array
				int i = 0;
				while(!arr[i].equals("ON")) {
					i++;
				}
				
				//the next index points to the localkey of localtable
				String localkey = arr[i+1];
				//after string "=", the next index points to the foreignkey of foreigntable
				String foreignkey = arr[i+3];
				//the substring after dot is the actual column name, also parameters localkey and foreignkey 
				int indexofdot1 = localkey.indexOf(".");
				// The text after the period is "the Local Key"
				localkey = localkey.substring(++indexofdot1);
				int indexofdot2 = foreignkey.indexOf(".");
				foreignkey = foreignkey.substring(++indexofdot2);
				
				
				//"JOIN ON" command, returns the joint table
				result = this.db.joinTables(tablename, tablename2, args, localkey, foreignkey);
				
			}
			
			// If the command contains "ORDER" the the "ORDER BY" method is called. 
			if (sql.contains("ORDER")) {
					//the last word of the given string
					String sortStandard = arr[arr.length-1];
					
					//find the index of alias appeared in the joint table
					for (String s : result.column.getColumnList()) {
						if (s.equals(alias)) {
							indexOfAlias = result.column.getColumn(alias);
						}
					}
					//sort the given table
					this.db.OrderBy(result, alias, sortStandard,indexOfAlias);
					//print the result table
					result.print();
							
			}
			//if no join on tables
			else {
				//simply print the rows of the table
				this.db.printRows(tablename,args);
			}
			
			
		}
	
		else {
			System.out.println("Function not clear.");
		}
		}
	}
	/**
	 * This method will split a given String into an array of characters 
	 * @param str the input String
	 * @return a String array of each character in str
	 */
	public String[] splitStringIntoChars(String str) {
		return str.split("");
	}
	
	/**
	 * This method will extract a given String from brackets [ ] if it's found inside them 
	 * @param str the String to extract
	 * @return str without its brackets
	 * @throws IllegalArgumentException if str does not have brackets
	 */
	public String extractStringFromBrackets(String str) {
		try {
			return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
		} catch (Exception IndexOutOfBoundsException){
			throw new IllegalArgumentException("expected [...] got " + str);
		}
	}
	
	/**
	 * This method will extract a given String from parens ( ) if it's found inside them 
	 * @param str the String to extract
	 * @return a String array of string arguments extracted from parens
	 * @throws IllegalArgumentException if str does not have parens
	 */
	public static String[] extractStringsFromParens(String str) {
		try {
			return str.substring(str.indexOf("(") + 1, str.indexOf(")")).split(", ");
		} catch (Exception IndexOutOfBoundsException){
			throw new IllegalArgumentException("expected (...) got " + str);
		}
	}
	
	/**
	 * Retrieves the String representation of the "order by" alias from the given String 
	 * @param str the given String 
	 * @return a String representing the order by alias
	 */
	public String extractOrderByAlias(String str) {
		try {
			return extractStringsFromParens(str.substring(str.toUpperCase().indexOf("ORDER BY")))[0];
		} catch (Exception IndexOutOfBoundsException){
			return null;
		}
	}
	

}
