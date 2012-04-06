package com.wuntee.oter.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class SqliteHelper {
	private static Logger logger = Logger.getLogger(SqliteHelper.class);
	
	public static String SQL_TABLES = "select name from sqlite_master where type = 'table'";
	
	private Connection connection;
	private Statement statement;
	
	public SqliteHelper(File f) throws ClassNotFoundException, SQLException{
    	Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
		statement = connection.createStatement();
		statement.setQueryTimeout(30);
	}
	
	public List<String> getTables() throws SQLException{
		ResultSet rs = statement.executeQuery(SQL_TABLES);
		List<String> ret = new LinkedList<String>();
		while(rs.next()){
			ret.add(rs.getString(1));
		}
		return(ret);
	}
	
	public List<String> getTableColumnNames(String tableName) throws SQLException{
		ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")");
		List<String> ret = new LinkedList<String>();
		while(rs.next()){
			ret.add(rs.getString(2));
		}
		return(ret);
	}
	
	public List<List<String>> getTableData(String tableName) throws SQLException{
		List<List<String>> ret = new LinkedList<List<String>>();
		ResultSet rs = statement.executeQuery("select * from " + tableName);
		ResultSetMetaData rsmd = rs.getMetaData();
	    int cols = rsmd.getColumnCount();
	    logger.debug(tableName + " has " + cols + " columns");
	    while(rs.next()){
	    	List<String> col = new LinkedList<String>();
	    	for(int i=1; i<=cols; i++){
	    		col.add(rs.getString(i));
	    	}
	    	ret.add(col);
	    }
	    logger.error(ret);
		return(ret);
	}
}
