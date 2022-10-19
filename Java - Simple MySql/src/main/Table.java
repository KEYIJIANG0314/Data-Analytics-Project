package main;
import java.util.ArrayList;
import java.util.List;

/*
 * This class is defined by a TableColumns object which 
 * provides the column definitions and the data stored in Rows.
 *
 */
public class Table {
	
	public String name;
	public TableColumns column;
	List<Row> tables; // The ArrayList that takes objects of Row that makes up the Table. 


	/*
	 * The constructor. 
	 * Takes a string and a tableColumn.
	 * 
	 */
	public Table(String name, TableColumns column) {
		this.name = name;
		this.column = column;
		tables = new ArrayList<Row>();
		
		for (Row r : tables) {
			if (r.getSize() != column.getSize()) {
			throw new IllegalArgumentException("Wrong size for the row");
		}
	}
	}
	
	/*
	 * takes a row
	 * returns nothing
	 * add a row to the table
	 */
	public void addRow(Row row) {
		tables.add(row);
	}
	
	/*
	 * takes nothing
	 * returns the name of the table
	 */
	public String getName(){
		return this.name;
	}
	
	/*
	 * takes nothing 
	 * returns the size of the table(the number of rows)
	 */
	public int getSize() {
		return tables.size();
	}
	
	/*
	 * this method print out the table
	 */
	
	public void print() {
		System.out.println(name);
		column.printColumnName(); //print the column
		for(int i = 0; i < tables.size(); i++) {
			tables.get(i).printRow();//print each row
		}
	}
		
	/*
	 * Takes no parameters.
	 * Returns a Table object.
	 */
	public Table getTable() {

		// New copy of Table is created. 
		// A new copy of TableColumns needs to be passed in.  
		Table copyTable = new Table(name, column.getTableColumns());
		
		// The rows in table are added to the new copyTable.
		for (Row r : tables) {
			
			// We need the copy version of Rows, so we can't just pass in "row."
			copyTable.addRow(r.getRow());
		}
		
		// the new Copy is returned. 
		return copyTable;
		
		
		
		
		
		
	}
}
