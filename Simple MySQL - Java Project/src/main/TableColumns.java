package main;

// This class will hold the TableColumns. 

public class TableColumns {
	
	// An Array is used to store the names of the columns that are passed in. 
	private String[] columnList;
	
	
	/*
	 * The constructor. 
	 * Takes an array of strings, and copies that array to the internal array
	 * returns nothing. 
	 */
	public TableColumns(String[] columnList) {
		// The contents of the array is transferred into the new array.
		this.columnList= columnList;
		
		
	}
	
	/*
	 * Takes no parameters
	 * returns the size of the array.
	 */
	public int getSize() {
		
		// The size of the ArrayList.
		return columnList.length;
	}
	
	
	/*
	 * Takes a String
	 * returns the index at which the given string is located in the array. 
	 * 
	 */
	public int getColumn(String alias) {
		
		// A liner search is conducted to find the column number the given name is at. 
		for (int i=0; i<columnList.length; i++) {
			if (columnList[i].equals(alias)) {
				return i; // The index is returned if it's found. 
			}
		}
		return -1; // If not found. 
		
	}
	
	/*
	 * this method returns the columnList as an array of string
	 */
	public String[] getColumnList() {
		return columnList;
	}
	
	/*
	 * this method print out the column list
	 */
	public void printColumnName() {
		for(int i = 0; i < columnList.length; i++) {
			System.out.print(columnList[i]);
			System.out.print("  ");
		}
		System.out.println();
	}
	/*
	 * Takes in nothing
	 * returns a TableColumns object
	 * 
	 */
	public TableColumns getTableColumns() {
		
		// The array where the copy of the columList will be stored. 
		String[] copyColumnList = new String[columnList.length];
		
		// The copy is made.
		for (int i = 0; i<columnList.length; i++) {
			copyColumnList[i] = columnList[i];
		}
		
		// A new TableColumns object is created with the copied ColumnList.
		TableColumns copyTableColumns = new TableColumns(copyColumnList);
		
		
		// The copyTableColumns is returned. 
		return copyTableColumns;
	}
	
	
}
