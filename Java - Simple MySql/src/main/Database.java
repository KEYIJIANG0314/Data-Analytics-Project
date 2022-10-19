package main;
import java.lang.*;
import java.util.*;

//This class holds and stores numerous Tables.
public class Database{
	
	

	List<Table> databases; // Linked List that holds the Tables.
	DatabaseBackup backUpDb; // The instance of the DatabaseBackup object.
	
	
	/*
	 * The constructor.
	 */
	public Database(){
		databases = new LinkedList<Table>();
		backUpDb = new DatabaseBackup();
		
	}

	/*
	 * takes in a table
	 * returns nothing
	 * adds the table to database
	 * 
	 */
	public void addTable(Table table) {
		
		// The database is backed up every time a new table is added. 
		backUpDb.backUp(this.getDatabase());
		databases.add(table);
		
	}
	
	/*
	 * takes in a table
	 * returns nothing
	 * remove the table in database
	 * 
	 */
	public void removeTable(Table table) {
		if(!databases.contains(table)) {
			throw new NoSuchElementException();
		}
		
		
		// Database is backed up every time a table is removed. 
		backUpDb.backUp(this.getDatabase());
		databases.remove(table);
		
		
	}
	
	/*
	 * takes in a string of tablename and an array of string
	 * returns nothing.
	 */
	public void addRowToTable(String tableName, String[] args) {
		boolean added = false; // To throw the exception if a table doesn't exist. 
		Row row = new Row(args); //place the array of string in the row
		
		// for each loop to iterate over the LinkedList databases. 
		for (Table t : databases) {
			if(t.getName().equals(tableName)) {
				backUpDb.backUp(this.getDatabase()); // Database is backed up. 
				t.addRow(row);
				added = true; 
			}
		}
		
		if (!added) {
		throw new NoSuchElementException();
		}
	}
	
	/*
	 * Takes in a name for the strings, and an array of strings that contains the column names.
	 * returns nothing.
	 * This method will handle the command "Select ... From..."
	 */
	public void printRows(String tableName, String[] columnNameArray) {
		
		// The for loop starts with looping over the tables in the database.
		for (Table t : databases) {
			//The looping continues until the table with the passed in parameter is found.
			if(t.getName().equals(tableName)) {
				//When the requested table is found, the rows in table are looped over. 
				for (Row r : t.tables) {
					// Inside one row, the column names that are passed in are looped over. 
					for (int i = 0; i<columnNameArray.length; i++) {
						
						// The name of the column that the user wants to print.
						String columnName = columnNameArray[i];
						// The "getColumn" method in in "TableColumns" class is accessed so the number of the column
						// in the table can be found.
						int columnNumber = t.column.getColumn(columnName);
						// The columnNumber found above is passed in as a parameter to the getCell method
						// in the Row class, and the cell is printed. 
						System.out.print(r.getCell(columnNumber));
						System.out.print(" ");
					}
					System.out.println();
				}
			}
		}
	}
	
	
	
	/*
	 * Takes in 4 String parameters and 1 array of Strings. The first string indicates the name of the table the local key is on. 
	 * The second string indicates the name of the table the foreign key is on. The array is the columnNames the user wants printed. 
	 * The third string is the name of the local key and the fourth string is the name of the foreign key. 
	 * 
	 * It returns nothing
	 * 
	 * This method will handle the "JOIN ON" command.
	 */
	public Table joinTables(String localKeyTable, String foreignKeyTable, String[] columnNameArray, String localKey, String foreignKey) {
		
		// The values at the requested columns are stored in this arraylist.  
		ArrayList<String> toBePrinted = new ArrayList<String>();
		
		// The Table of the local Key.
		Table localTable = null;
		// The Table of the foreign key. 
		Table foreignTable = null;

		
		// Loop over the databases to find the tables the local and the foreign key are located. 
		for (Table t : databases) {
			if(t.getName().equals(localKeyTable)) {
				localTable = t; // Local table is assigned. 
				
			}else if (t.getName().equals(foreignKeyTable)) {
				foreignTable = t; // Foreign table is assigned. 
				
			}
		}
		
		//tobePrinted is a table create to store the result and to be returned
		TableColumns column = new TableColumns(columnNameArray);
		Table tobePrinted = new Table("Resulting table", column);
		
		// Nested for loop to compare the local and the foreign keys. 
		// The main outer loop loops over the "Row" objects in the "Local Table"
		for (Row rlocal : localTable.tables) {
					// The column Number of the "Local Key"
					int columnNumberLocal = localTable.column.getColumn(localKey);
					// The value at the specific cell located in the column of the "Local Key" at the specific row. 
					String localToBeCompared = rlocal.getCell(columnNumberLocal);
					// This loop Checks to see if the requested columns are in the "Local Table." If they are the values are put into the array that is to be printed "toBePrinted"
					// at the given order. 
					for (int i = 0; i<columnNameArray.length; i++) {
						//System.out.println("outside " + i);
						// The name of the column the user put in. 
						String columnName = columnNameArray[i];
						// The location of the column. 
						int columnNumber = localTable.column.getColumn(columnName);
						// If the column isn't found "columnNumber" will be "-1" which means this column is not in "Local Table" which means it is most likely in the "Foreign Table."
						if (columnNumber >= 0) {
							// The value is added into the arraylist. 
							toBePrinted.add(rlocal.getCell(columnNumber));
						
						}
					}
					// This is the main inner loop.
					// It loops over the "Row" objects in the "Foreign Table."
					for (Row rforeign : foreignTable.tables) {
						// The column Number of the "Foreign Key"
						int columnNumberForeign = foreignTable.column.getColumn(foreignKey);
						// The value a the specific cell located in the column of the "Foreign Key" a the specific row.
						String foreignToBeCompared = rforeign.getCell(columnNumberForeign);
						
						/*
						 * If the "Local Key" is equal to the "Foreign Key" that means there is a match 
						 * and the contents in foreign key will be added to the arraylist after matched local key contents. 
						 */
						if (localToBeCompared.equals(foreignToBeCompared)) {
							// This loop Checks to see if the requested columns are in the "Foreign Table." If they are the values are put into the array that is to be printed "toBePrinted"
							// at the given order. 
							for (int i = 0; i<columnNameArray.length; i++) {
								// The name of the column the user put in. 
								String columnName = columnNameArray[i];
								// The location of the column. 
								int columnNumber = foreignTable.column.getColumn(columnName);
								// If the column isn't found "columnNumber" will be "-1" which means this column is not in "Foreign Table" which means it is most likely in the "Local Table."
								if (columnNumber >= 0) {
									// The value is added into the arraylist. 
									toBePrinted.add((rforeign.getCell(columnNumber)));	
								}
							}
						}
					}
					
					//create an array of string to store the internal string of the arraylist
					String[] addtoRow = toArray(toBePrinted);
					//revert the string list to a row and add it to the table
					Row row = new Row(addtoRow);
					tobePrinted.addRow(row);
					//clear the arraylist for next loop
					toBePrinted.clear();
								
		} // The outer loop concludes here
		
		return tobePrinted;
	}
	
	
	/*
	 * Takes a table and a string alias of a column to sort and 
	 * a string sortStandard to clarify either ascending or descending order
	 * or numerical ordering
	 * returns nothing.
	 * This method will state the order which Rows should be printed to the console.
	 * 
	 */
	public void OrderBy(Table tobesorted, String alias, String sortStandard, int indexOfAlias) {
		
		
		//if requires ascending order
		if(sortStandard.equals("ASC")) { 
			//if it is an integer column
			if(alias.contains("<i>")) {
				
				//create a arraylist of integers to store all the values we want to compare from each row
				ArrayList<Integer> parse = new ArrayList<Integer>();
				for(int j = 0; j < tobesorted.getSize(); j++) {
					parse.add(Integer.parseInt(tobesorted.tables.get(j).getCell(indexOfAlias)));
				}
				
				bubbleSortInteger(parse, tobesorted, sortStandard);		
			}
			else {
				//create an arraylist of strings to store all the values we want to sort by lexicographical ordering
				ArrayList<String> words = new ArrayList<String>();
				for(int j = 0; j < tobesorted.getSize(); j++) {
					words.add(tobesorted.tables.get(j).getCell(indexOfAlias));
				}
				bubbleSortWord(words, tobesorted, sortStandard);
			
			}
		}
		//if requires descending order; mostly same, only the direction of comparison is reversed
		
		else if(sortStandard.equals("DESC")) {
			
			if(alias.contains("<i>")) {
				ArrayList<Integer> parse = new ArrayList<Integer>();
				for(int j = 0; j < tobesorted.getSize(); j++) {
					parse.add(Integer.parseInt(tobesorted.tables.get(j).getCell(indexOfAlias)));
				}
				bubbleSortInteger(parse, tobesorted, sortStandard);
				
			}
			else {
				ArrayList<String> words = new ArrayList<String>();
				for(int j = 0; j < tobesorted.getSize(); j++) {
					words.add(tobesorted.tables.get(j).getCell(indexOfAlias));
				}
				
				bubbleSortWord(words, tobesorted, sortStandard);
			}
		}
		//if not given the order requirement
		else {
			System.out.println("Please be specific about how to sort the order.");
		}
		

	}
	

	
	
	/*
	 * Takes in a list, a Table and a String. 
	 * Returns nothing.
	 * Implements bubble sort for Integer values. 
	 *
	 */
	public void bubbleSortWord(List<String> list, Table t, String sortStandard) {
		
		if (sortStandard.equals("ASC")) {
			
			boolean sorted = false;
			//it will be sorted until no changes are made in the list
			while(!sorted) {
				sorted = true;
				for(int i = 0; i<list.size()-1; i++) {
					if(list.get(i).compareTo(list.get(i+1))>0) {
						//sort the table
						Row temp = t.tables.get(i);
						t.tables.set(i,t.tables.get(i+1));
						t.tables.set(i+1,temp);
						//sort the list
						String temp2 = list.get(i);
						list.set(i, list.get(i+1));
						list.set(i+1, temp2);
						sorted = false;
					}
				}
			}
		} 
		
		else {
			boolean sorted = false;
			while(!sorted) {
				sorted = true;
				for(int i = 0; i<list.size()-1; i++) {
					if(list.get(i).compareTo(list.get(i+1))<0) {
						Row temp = t.tables.get(i);
						t.tables.set(i,t.tables.get(i+1));
						t.tables.set(i+1,temp);
						String temp2 = list.get(i);
						list.set(i, list.get(i+1));
						list.set(i+1, temp2);
						sorted = false;
					}
				}
			}

		}	
	}
	
	/*
	 * Takes in a list, a Table and a String. 
	 * Returns nothing.
	 * Implements bubble sort for Integer values. 
	 *
	 */
	public void bubbleSortInteger(List<Integer> list, Table t, String sortStandard) {
		
		
		if (sortStandard.equals("ASC")) {
			boolean sorted = false;
			while(!sorted) {
				sorted = true;
				for(int i = 0; i<list.size()-1; i++) {
					if(list.get(i) > list.get(i+1)) {
						Row temp = t.tables.get(i);
						t.tables.set(i,t.tables.get(i+1));
						t.tables.set(i+1,temp);
						int temp2 = list.get(i);
						list.set(i, list.get(i+1));
						list.set(i+1, temp2);
						sorted = false;
					}
				}
			}
		} else {
			boolean sorted = false;
			while(!sorted) {
				sorted = true;
				for(int i = 0; i<list.size()-1; i++) {
					if(list.get(i) < list.get(i+1)) {
						Row temp = t.tables.get(i);
						t.tables.set(i,t.tables.get(i+1));
						t.tables.set(i+1,temp);
						int temp2 = list.get(i);
						list.set(i, list.get(i+1));
						list.set(i+1, temp2);
						sorted = false;
					}
				}
			}
		}	
	}
		
	
	
	/*
	 * Takes no parameters
	 * Returns nothing.
	 * Reverts the database to its previous state. 
	 */
	public void undo() {
		
		// A new instance of the latest state of Database is created. 
		Database oldState = backUpDb.getLatestBackUp();
		
		
		// The fields in the actual database are set equal to the fields in the copy database. 
		 this.databases = oldState.databases;
		 this.backUpDb = oldState.backUpDb;
		 	
		
	}
	
	public String[] toArray(ArrayList<String> l) {
		String[] a = new String[l.size()];
		for(int i = 0; i < l.size(); i++) {
			a[i] = l.get(i);
		}
		return a;
	}
	
	/*
	 * Takes in nothing
	 * returns nothing.
	 * This method is needed so that the "Undo" function can work. We use this method to pass a value of the Database and not a reference of it. 
	 * 
	 */	 

	public Database getDatabase() {
		
		// A copy of the database is created. 
		Database copyDB = new Database();
		
		// All the tables in the actual database are copied to the newly created copy of the database. 
		for (Table t : databases) {
			
			// We can't add the actual tables because then there would be no point in creating a new copy, so we need the copies for the tables as well. 
			copyDB.addTable(t.getTable());
		}
		
		
		// The copy database is returned. 
		return copyDB;
		
	}
	
	
	/*
	 * Takes in nothing. 
	 * Returns the the list that contains databases.
	 * It's used for testing. 
	 */
	public List<Table> getDatabasesList(){
		return databases;
	}
}
