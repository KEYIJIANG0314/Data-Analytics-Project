package main;



// This class will hold the rows of the table.
public class Row {
	
	
	
	// The internal array to store the cells. 
	public String[] cells;
	
	/*
	 * The constructor. 
	 * Takes an array of String.
	 * 
	 */
	public Row(String[] cells) {
	
		// The cell is initilazed
		this.cells = cells;
	}
	
	
		
	/*
	 * Takes an integer as a parameter
	 * returns the data at the cell specified as the index as a String.
	 */
	public String getCell(int index) {
		return cells[index];
		
	}
	

	
	/*
	 * Takes no parameters
	 * returns the internal array. 
	 */
	public String[] getRowArray() {
		
		return cells;
	}
	
	/*
	 * takes in nothing
	 * returns the size of a row
	 */
	public int getSize() {
		return cells.length;
	}
	
	/*
	 * this method print out the row
	 */
	
	public void printRow() {
		for(int i = 0; i < cells.length; i++) {
			System.out.print(cells[i]);
			System.out.print("  ");
		}
		System.out.println();
	}
	
	/*
	 * Takes in nothing
	 * Returns a Row object
	 */

	public Row getRow() {
		
		// The array that will hold the copy of the cells. 
		String[] copyCells = new String[cells.length];
		
		
		// The values are transferred into the new array.
		for (int i= 0; i<cells.length; i++) {
			copyCells[i] = cells[i];
		}
		
		// A new instance of the Row object with the copied Cells is made. 
		Row copyRow = new Row(copyCells);
		
		// The copied instance is returned. 
		return copyRow;
		
	}
}


