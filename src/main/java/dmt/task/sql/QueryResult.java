package dmt.task.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletContext;


public class QueryResult {

	public boolean executed = true;
	public String sqlError = "";
	public int columnCount = -1;
	public ArrayList<String> columnTypes;
	public ArrayList<String> columnNames;
	public ArrayList<String[]> rows;
	
	private static final String ROWDELIMITER = "\n";
	private static final String COLDELIMITER = "\t";
	
	
	public QueryResult (ResultSet rs) throws SQLException {

		columnCount = rs.getMetaData().getColumnCount();
		columnTypes = new ArrayList<String>();
		columnNames = new ArrayList<String>();
		for (int i=0; i<columnCount; i++) {
			columnTypes.add(rs.getMetaData().getColumnTypeName(i+1).toUpperCase());
			columnNames.add(rs.getMetaData().getColumnName(i+1).toUpperCase());
		}
		
		int count = 0;	
		rows = new ArrayList<String[]>();
		while (rs.next()) {
			String[] row = new String[columnCount];
			for (int i=0; i<columnCount; i++) row[i] = rs.getString(i+1);
			rows.add(row);
			
			if (++count == 10000) break; // count the number and stop after 10,000 rows to prevent heap space overflow
		}
	}	
	
	
	
	/**
	 * Compute the difference between two query results (this.qr and parameter.qr)
	 * @param qr the Queryresult for comparison
	 * @param sortRelevant is sort order relevant? (true for queries that require ORDER BY)
	 * @param checkDatatypes 
	 * @param checkColumnNames column names are checked; allows for query results having different column orders 
	 * @return 
	 */
	public QueryDiff getDifference (QueryResult qr, boolean sortRelevant, boolean checkDatatypes, boolean checkColumnNames) {

		// check for valid results
		if (!this.executed) return new QueryDiff(String.format("Query1 konnte nicht ausgefuehrt werden. (%s)", this.sqlError));
		if (!qr.executed)   return new QueryDiff(String.format("Query2 konnte nicht ausgefuehrt werden. (%s)", qr.sqlError));

		// check for correct result size
		if (this.rows.size() != qr.rows.size()) return new QueryDiff(String.format ("Unterschiedliche Anzahl an Zeilen (%d; erwartet: %d).", qr.rows.size(), this.rows.size()));
		if (this.columnCount != qr.columnCount) return new QueryDiff(String.format ("Unterschiedliche Anzahl an Spalten (%d; erwartet: %d).", qr.columnCount, this.columnCount));
		
		// determine column mapping
		int[] colMap = new int[this.columnCount];	// colMap[x] = y ... column this.#x = qr.#y
		for (int x=0; x<this.columnCount; x++) {
			colMap[x] = x;
		}
 
		if (checkColumnNames) {
			
			for (int x=0; x<this.columnCount; x++) {
				colMap[x] = -1;	// -1 indicates "no matching column yet"
			}
			
			for (int y=0; y<qr.columnCount; y++) {
				int x = this.columnNames.indexOf(qr.columnNames.get(y));
				
				if (x == -1) {
					return new QueryDiff(String.format ("Unterschiedliche Attributnamen; falsches Attribute %s.", qr.columnNames.get(y)));
				}
				if (colMap[x] != -1) {
					return new QueryDiff(String.format ("Doppelter Attributname %s.", qr.columnNames.get(y)));
				}
				colMap[x] = y;
			}
		}
		
		// check for data types
		if (checkDatatypes) {
			for (int x=0; x<qr.columnCount; x++) {
				if (! this.columnTypes.get(x).equalsIgnoreCase(qr.columnTypes.get(colMap[x]))) {
					return new QueryDiff(String.format ("Unterschiedliche Attribut-Datentypen für Attribut %s (%s; erwartet: %s)", qr.columnNames.get(colMap[x]), qr.columnTypes.get(colMap[x]), this.columnTypes.get(x)));
				}
			}
		}
		
		// generate rows as string concatenation for easy comparison
		ArrayList<String> rowComp = new ArrayList<String>();
		for (String[] row: this.rows) {
			String[] rowMapped = new String[row.length];
			for (int col=0; col<row.length; col++) {
				rowMapped[colMap[col]] = row[col];
			}
			rowComp.add(Arrays.toString(rowMapped).toLowerCase());
		}
		
		// check for equality
		boolean isSorted = true;
		for (int i=0; i<qr.rows.size(); i++) {
			String[] row = qr.rows.get(i);
			int index = rowComp.indexOf(Arrays.toString(row).toLowerCase());
			if (index == -1) return new QueryDiff(String.format ("Unterschiedliche Tupel; falsches Tupel %s.", Arrays.toString(row)));
//			System.out.println("i=" + i + " and index="+ index);
			isSorted &= Arrays.toString(row).toLowerCase().equals(rowComp.get(i));	  // check for "i == index" may fail if there are multiple equal rows  
		}
		
		
		return (isSorted || !sortRelevant) ? new QueryDiff("Korrektes Ergebnis!",true) : new QueryDiff("Identische Tupelmenge, aber unterschiedliche Reihenfolge!");
	}
	
	
	
	
	
	
	public QueryResult(String table) {
		
		columnTypes = new ArrayList<String>();
		columnNames = new ArrayList<String>();
		
		String[] rows_String = table.split(ROWDELIMITER, -1);
		
		// header processing: we ignore empty column names
		String[] header = rows_String[0].split(COLDELIMITER, -1);
		ArrayList<Integer> colIndex = new ArrayList<Integer>();	// mapping table column -> result column 
		for (int i=0; i<header.length; i++) {
			if (!header[i].trim().equals("")) {
				colIndex.add(i);
				columnNames.add (header[i].trim().toUpperCase());
				columnTypes.add ("BIGINT");
			}
		}
		columnCount = columnNames.size();
		 
		// rows: we ignore empty rows
		rows = new ArrayList<String[]>();
		for (int i=1; i<rows_String.length; i++) {
			if (rows_String[i].trim().equals("")) continue;
			String[] tableRow = rows_String[i].split(COLDELIMITER, -1);
			String[] row = new String[columnCount];
			for (int k=0; k<columnCount; k++) {
				row[k] = tableRow[colIndex.get(k)].trim();
			}
			rows.add(row);
		}
	}
	
	

	

	/**
	 * Execute query and store query result (data and metadata) for later comparison
	 * @param conn
	 * @param query
	 * @throws Exception
	 */
	public QueryResult(Connection conn, String schema, String query, ServletContext logContext, Date timestamp) throws Exception {
		
        Statement stmt = conn.createStatement();
        ResultSet selectRS = null;
        try {
        	System.out.println("SET SCHEMA " + schema);
        	stmt.executeUpdate("SET SCHEMA " + schema);
        	// set query timeout to avoid long-ruuning queries that slow down the web server
        	System.out.println("SET QUERY_TIMEOUT 5000");
        	stmt.executeUpdate("SET QUERY_TIMEOUT 5000"); 
        	System.out.println("Execute Query" + query);
			selectRS = stmt.executeQuery(query);
        	System.out.println("Done");
			
			columnCount = selectRS.getMetaData().getColumnCount();
			columnTypes = new ArrayList<String>();
			columnNames = new ArrayList<String>();
			for (int i=0; i<columnCount; i++) {
				columnTypes.add(selectRS.getMetaData().getColumnTypeName(i+1).toUpperCase());
				columnNames.add(selectRS.getMetaData().getColumnName(i+1).toUpperCase());
			}
			
			int count = 0;	
			rows = new ArrayList<String[]>();
			while (selectRS.next()) {
				String[] row = new String[columnCount];
				for (int i=0; i<columnCount; i++) row[i] = selectRS.getString(i+1);
				rows.add(row);
				
				if (++count == 10000) break; // count the number and stop after 10,000 rows to prevent heap space overflow
			}
			
		} catch (SQLException e) {
			executed = false;
			sqlError = e.getMessage();
			logContext.log(timestamp + "@QueryResult: SQLException\n" + sqlError);
//			System.out.println(sqlError);
		} finally {
			if (selectRS != null) selectRS.close();
			if (stmt != null) stmt.close();
			logContext.log(timestamp + "@QueryResult: ResultSet and Statement closed");
		}
	}

	

	public QueryResult distinct() {
		
		// generate rows as string concatenation for easy comparison
		ArrayList<String> rowComp = new ArrayList<String>();
		for (String[] row: this.rows) {
			rowComp.add(Arrays.toString(row).toLowerCase());
		}		
		
		for (int i=this.rows.size()-1; i>=0; i--) {
			
			int k = rowComp.indexOf(Arrays.toString (this.rows.get(i)).toLowerCase());
			if (k<i) {
				this.rows.remove(i);
			}
			
		}
		
		
		return this;
	}

	

	
	
}
