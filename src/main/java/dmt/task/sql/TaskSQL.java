package dmt.task.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import dmt.task.Task;

@Repository
public abstract class TaskSQL extends Task {

	private final DataSource dbSource;
	private Connection con = null;

	protected String readSchema = null; // read-only
	protected String userSchema = null; // writable

	@Autowired
	JdbcTemplate jdbcTemplate;

	public TaskSQL(final String repo, final String id, final JsonObject repoData, final JsonObject taskData, DataSource dbSource) throws Exception {

		super(repo, id, repoData, taskData);

		this.dbSource = dbSource; 
		this.readSchema = repoData.getString("schema", "public");
		this.userSchema = "user" + System.currentTimeMillis();

		ArrayList<String> initSQL = new ArrayList<String>();
		for (final JsonArray initSQLJSON : new JsonArray[] { repoData.getJsonArray("init"),
				taskData.getJsonArray("init") }) {
			if (initSQLJSON == null)
				continue;
			for (int i = 0; i < initSQLJSON.size(); i++) {
				initSQL.add(initSQLJSON.getString(i));
			}
		}

		Statement stmt = this.createStatement();
		stmt.execute("SET statement_timeout to 5000"); // timeout after 5sec
		stmt.setMaxRows(10000); // max 10.000 rows
		stmt.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", userSchema));
		stmt.execute(String.format("CREATE SCHEMA %s", userSchema));
		stmt.execute(String.format("SET search_path TO %s, %s", userSchema, this.readSchema));
		for (int i = 0; i < initSQL.size(); i++) {
			stmt.execute(initSQL.get(i));
		}
		// stmt.close();

	}

	protected Statement createStatement() throws SQLException {
		
		if (this.con == null) {
			this.con = this.dbSource.getConnection();
		}
		System.out.println(this.con.isClosed());
		return this.con.createStatement();
	}




	@Override
	public void close() {
		try {
			Statement stmt = this.createStatement();
			stmt.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", userSchema));
			stmt.close();
			this.con.close();
		} catch (final SQLException e) {
			this.setException(e, "Can't disconnect from database.");
		}
	}

	@Override
	public String computeAndGetStatus() {

		String result = "Could not retrieve database information.";
		final String sql = String.format(
				"SELECT upper(table_name) FROM INFORMATION_SCHEMA.TABLES WHERE upper(table_schema) IN ('%s','%s') ORDER BY table_name",
				this.userSchema.toUpperCase(), this.readSchema.toUpperCase());

		try {
			Statement stmt = this.createStatement();

			final ResultSet rs = stmt.executeQuery(sql);
			final List<String> tables = new ArrayList<String>();
			while (rs.next()) {
				tables.add(rs.getString(1));
			}
			result = String.format("Database ready (%d tables %s)", tables.size(), tables.toString());
			rs.close();

		} catch (final SQLException e) {
			this.setException(e, "Can't retrieve schema information.");
		}

		return result;
	}



}
