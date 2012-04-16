package com.wuntee.oter.view.widgets;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.wuntee.oter.sqlite.SqliteHelper;
import com.wuntee.oter.view.GuiWorkshop;

public class CTabItemWithDatabase {
	private static Logger logger = Logger.getLogger(CTabItemWithDatabase.class);

	private CTabItem cTabItem;
	private CTabFolder parent;
	private String name;
	private SashForm databaseSashForm ;
	private Tree tablesTree;
	private Table contentsTable;
	private SqliteHelper db;

	public CTabItemWithDatabase(CTabFolder parent, String name, File file, int style) throws ClassNotFoundException, SQLException {
		this.parent = parent;
		this.name = name;
    	this.db = new SqliteHelper(file);

		cTabItem = new CTabItem(parent, style);
		cTabItem.setText(name);
		
		databaseSashForm = new SashForm(parent, SWT.NONE);
		cTabItem.setControl(databaseSashForm);
		
		tablesTree = new Tree(databaseSashForm, SWT.BORDER);
		tablesTree.setHeaderVisible(true);
		
		tablesTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				loadTableData(tablesTree.getSelection()[0].getText());
			}
		});

		contentsTable = new Table(databaseSashForm, SWT.BORDER | SWT.FULL_SELECTION);
		contentsTable.setHeaderVisible(true);
		contentsTable.setLinesVisible(true);
		
		databaseSashForm.setWeights(new int[] {1, 3});

		parent.setSelection(cTabItem);
		
		loadTables();
	}
	
	public void loadTableData(final String tableName){
		parent.getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
				try{
					contentsTable.removeAll();
					for(TableColumn c : contentsTable.getColumns()){
						c.dispose();
					}
					
					List<String> columns = db.getTableColumnNames(tableName);
					if(columns.size() > 0){
						// Create columns
						for(String colName : columns){
							TableColumn column = new TableColumn(contentsTable, SWT.NONE);
							column.setText(colName);
							column.pack();
							column.setWidth(column.getWidth() + 20);
						}
					}
					
					// Add data
					List<List<String>> data = db.getTableData(tableName);
					for(List<String> row : data){
		                TableItem tableItem = new TableItem(contentsTable,SWT.NONE);
		                tableItem.setText(row.toArray(new String[row.size()]));
					}
				} catch (Exception e){
					logger.error("Could not load table data: ", e);
				}
			}
		});
	}
	
	public void loadTables(){
		parent.getDisplay().asyncExec(new Runnable(){
			@Override
			public void run() {
				try{
					List<String> tables = db.getTables();
					tablesTree.removeAll();
					for(String table : tables){
						TreeItem item = new TreeItem(tablesTree, SWT.NONE);
						item.setText(table);
					}
				} catch (Exception e){
					logger.error("Could not load tables: ", e);
					cTabItem.dispose();
					GuiWorkshop.messageError(parent.getShell(), "Could not load database: " + e.getMessage());
				}
			}
			
		});
	}
	

	public CTabItem getcTabItem() {
		return cTabItem;
	}


	public void setcTabItem(CTabItem cTabItem) {
		this.cTabItem = cTabItem;
	}


	public CTabFolder getParent() {
		return parent;
	}


	public void setParent(CTabFolder parent) {
		this.parent = parent;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public SashForm getDatabaseSashForm() {
		return databaseSashForm;
	}


	public void setDatabaseSashForm(SashForm databaseSashForm) {
		this.databaseSashForm = databaseSashForm;
	}


	public Tree getTablesTree() {
		return tablesTree;
	}


	public void setTablesTree(Tree tablesTree) {
		this.tablesTree = tablesTree;
	}


	public Table getContentsTable() {
		return contentsTable;
	}


	public void setContentsTable(Table contentsTable) {
		this.contentsTable = contentsTable;
	}
	
}
