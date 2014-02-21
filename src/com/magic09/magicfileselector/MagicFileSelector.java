package com.magic09.magicfileselector;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.magic09.magicfilechooser.R;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * MagicFileSelector provides a simple file selection activity
 * that returns the path of the file selected.  It allows files
 * to be filtered.
 * @author dream09
 *
 */
public class MagicFileSelector extends ListActivity {
	
	/* Variables */
	private File currentDir;
	private FileArrayAdapter adapter;
	private FilenameFilter filter;
	private FileFilter folderFilter;
	
	static final String TAG = "MagicFileSelector";
	
	public static final int FILE_REQUEST = 0;
	public static final String DATA_KEY_RETURN = "dataKeyReturn";
	public static final String DATA_KEY_FOLDER = "dataKeyFolder";
	public static final String DATA_KEY_FILTER = "dataKeyFilter";
	
	
	
	/* Overridden methods */
	
	/**
	 * Constructor.
	 */
	public MagicFileSelector() {
		
		// Setup the folder filter.
		folderFilter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
	}
	
	
	/**
	 * Handle creation and setup based on passed data (if any).
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Allow navigating up from the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Get data sent (if any).
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			
			// Setup directory to browse.
			String myFolder = extras.getString(DATA_KEY_FOLDER);
			if (myFolder == null || myFolder.length() < 1) {
				currentDir = new File(Environment.getExternalStorageDirectory().getPath());
			} else {
				currentDir = new File(extras.getString(DATA_KEY_FOLDER));
			}
			
			// Setup the filename filter.
			final String myFilter = extras.getString(DATA_KEY_FILTER);
			if (myFilter == null || myFilter.length() < 1 || myFilter.indexOf(".") == -1) {
				filter = null;
				if (myFilter.length() < 1) {
					Log.d(TAG, "Filter specified as \"" + myFilter + "\", but length <1!");
				} else if (myFilter.indexOf(".") == -1) {
					Log.d(TAG, "Filter specified as \"" + myFilter + "\", but no \".\" could be found!");
				}
			} else {
				filter = new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String filename) {
						return filename.toLowerCase(Locale.US).endsWith(myFilter);
					}
				};
			}
		} else {
			
			// No data - set to defaults.
			filter = null;
			currentDir = new File(Environment.getExternalStorageDirectory().getPath());
		}
		
		// Call populate
		populateFileList(currentDir);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		// Handle a click on the navigation up item in the action bar.
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Handle the back button, jump up a folder level or quit.
	 */
	@Override
	public void onBackPressed() {
		// Check if at top level.
		if (adapter.getCount() > 0 && adapter.getItem(0).getType() == FileDisplayLine.FILETYPE_PARENT) {
			currentDir = new File(adapter.getItem(0).getPath());
			populateFileList(currentDir);
		} else {
			super.onBackPressed();
		}
	}
	
	/**
	 * Handle the selection of an item.  If a folder show its contents.
	 * If a file return its path.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		FileDisplayLine o = adapter.getItem(position);
		if (o.getType() == FileDisplayLine.FILETYPE_FOLDER || o.getType() == FileDisplayLine.FILETYPE_PARENT) {
			currentDir = new File(o.getPath());
			populateFileList(currentDir);
		}
		
		if (o.getType() == FileDisplayLine.FILETYPE_FILE) {
			Intent data = new Intent();
			data.putExtra(MagicFileSelector.DATA_KEY_RETURN, o.getPath());
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	
	
	/* Methods */
	
	/**
	 * Method reads the current directory and populates the displayed
	 * list.
	 * @param f
	 */
	private void populateFileList(File aFile)
	{
		// Set title to current directory.
		this.setTitle(aFile.getPath());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
		
		// Setup arrays to hold folder list and file list.
		List<FileDisplayLine>folders = new ArrayList<FileDisplayLine>();
		List<FileDisplayLine>files = new ArrayList<FileDisplayLine>();
		
		// Try and loop folder and file list and populate our display lists.
		try {
			File[]filterDirs = aFile.listFiles(folderFilter);
			for (File loopDir : filterDirs) {
				folders.add(new FileDisplayLine(loopDir.getName(), sdf.format(new Date(loopDir.lastModified())), loopDir.getAbsolutePath(), FileDisplayLine.FILETYPE_FOLDER, 0));
			}
			
			File[]filterFiles = aFile.listFiles(filter);
			for (File loopFile : filterFiles) {
				files.add(new FileDisplayLine(loopFile.getName(), sdf.format(new Date(loopFile.lastModified())), loopFile.getAbsolutePath(), FileDisplayLine.FILETYPE_FILE, loopFile.length()));
			}
		} catch (Exception e) {
			// TODO: Handle any exceptions.
		}
		
		// Sort the directory and file lists then add the files onto the end of the directory list.
		Collections.sort(folders);
		Collections.sort(files);
		folders.addAll(files);
		
		// Check if we need a parent director option.
		if (!aFile.getName().equalsIgnoreCase(Environment.getExternalStorageDirectory().getName())) {
			folders.add(0, new FileDisplayLine("..", "Parent Directory", aFile.getParent(), FileDisplayLine.FILETYPE_PARENT, 0));
		}
		
		// Set the list adapter to display.
		adapter = new FileArrayAdapter(MagicFileSelector.this, R.layout.magic_file_selector_view, folders);
		this.setListAdapter(adapter);
	}
	
}
