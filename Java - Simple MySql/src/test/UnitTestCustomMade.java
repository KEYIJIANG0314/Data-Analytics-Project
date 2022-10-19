package test;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import main.*;

public class UnitTestCustomMade {

	@Test
	public void tableMaker() {
	// The table column
	String[] headers = new String[3];
	headers[0] = "fruits";
	headers[1] = "drinks";
	headers[2] = "quantities<i>";
	
	// The name of the table
	String tableName = "groceryList";
	
	Database db = new Database();
	TableColumns columns = new TableColumns(headers);
	Table testTable = new Table(tableName, columns);
	
	// Test if the "addTable" adds a table to the Database.
	db.addTable(testTable);
	assertEquals(db.getDatabasesList().get(0).getName(), "groceryList");
	assertEquals(db.getDatabasesList().get(0).getSize(), 0);	
	
	
	}
	
	@Test
	public void testUndo() {
		String[] headers = new String[3];
		headers[0] = "fruits";
		headers[1] = "drinks";
		headers[2] = "quantities<i>";
		
		String[] cells = new String[3];
		cells[0] = "apple";
		cells[1] = "gatorade";
		cells[2] =  "10";
		
		// The name of the table
		String tableName = "groceryList";
		
		Database db = new Database();
		TableColumns columns = new TableColumns(headers);
		Table testTable = new Table(tableName, columns);
		
		db.addTable(testTable);
		db.addRowToTable("groceryList", cells);
		assertEquals(db.getDatabasesList().get(0).getSize(), 1);
		db.undo();
		assertEquals(db.getDatabasesList().get(0).getSize(), 0);
		
	}
		
		
		
}
