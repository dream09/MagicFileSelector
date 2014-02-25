package com.magic09.magicfileselector;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

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
	
	static final String TAG = "MagicFileSelector";
	
	/* Variables */
	private File currentDir;
	private SmbFile currentSmb;
	private FileArrayAdapter adapter;
	private FilenameFilter filter;					// Holds any custom (local) file extension filter passed.
	private SmbFileFilter smbFilter;				// Holds any custom (smb) file extension filter passed.
	private FileFilter folderFilter;				// Used to filter (local) folders and files.
	private SmbFileFilter smbFolderFilter;			// Used to filter (smb) folders.
	private SmbFileFilter smbFileFilter;			// Used to filter (smb) files.
	private boolean SmbMode;
	private NtlmPasswordAuthentication smbAuth;
	private String smbURL;
	
	public static final int FILE_REQUEST = 0;
	public static final String DATA_KEY_RETURN = "dataKeyReturn";			// Key used to return data.
	public static final String DATA_KEY_FOLDER = "dataKeyFolder";			// Key used to pass a start folder.
	public static final String DATA_KEY_FILTER = "dataKeyFilter";			// Key used to pass a file filter.
	public static final String DATA_KEY_FILTERS = "dataKeyFilters";			// Key used to pass multiple file filters.
	public static final String DATA_KEY_IPADDRESS = "dataKeyIPAddress";		// Key used to pass an smb address.
	public static final String DATA_KEY_USERNAME = "dataKeyUsername";		// Key used to pass a username for the smb address.
	public static final String DATA_KEY_PASSWORD = "dataKeyPassword";		// Key used to pass a password for the smb address.
	
	
	
	/* Overridden methods */
	
	/**
	 * Constructor.
	 */
	public MagicFileSelector() {
		
		// Setup the folder filters.
		folderFilter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		
		smbFolderFilter = new SmbFileFilter() {
			
			@Override
			public boolean accept(SmbFile pathname) throws SmbException {
				return pathname.isDirectory();
			}
		};
		
		smbFilter = null;
		smbFileFilter = new SmbFileFilter() {
			
			@Override
			public boolean accept(SmbFile pathname) throws SmbException {
				return pathname.isFile();
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
			
			SmbMode = false;
			String myFolder = extras.getString(DATA_KEY_FOLDER);
			String myIPAddress= extras.getString(DATA_KEY_IPADDRESS);
			String myUsername= extras.getString(DATA_KEY_USERNAME);
			String myPassword= extras.getString(DATA_KEY_PASSWORD);
			
			// Check if browsing locally (default) or on network (ip address).
			if (myFolder != null && myFolder.length() > 0) {
				currentDir = new File(extras.getString(DATA_KEY_FOLDER));
			} else if (myIPAddress != null && myUsername != null && myPassword != null) {
				
				smbURL = "smb://" + myIPAddress + "/";
				smbAuth = new NtlmPasswordAuthentication(null, myUsername, myPassword);
				currentSmb = null;
				try {
					currentSmb = new SmbFile(smbURL, smbAuth);
					SmbMode = true;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				
				currentDir = null;
				
			} else {
				currentDir = new File(Environment.getExternalStorageDirectory().getPath());
			}
			
			// Check for filters and setup as required.
			String myFilter = extras.getString(DATA_KEY_FILTER);			// Holds any singular filter sent.
			String[] myFilters = null;										// Used to produce a final list of filters.
			String[] getFilters = extras.getStringArray(DATA_KEY_FILTERS);	// Holds any array of filters sent.
			if (getFilters == null && myFilter != null) {
				myFilters = new String[1];
				myFilters[0] = myFilter;
			} else if (getFilters != null && myFilter != null) {
				myFilters = new String[getFilters.length + 1];
				for (int i = 0; i < getFilters.length; i++) {
					myFilters[i] = getFilters[i];
				}
				myFilters[getFilters.length] = myFilter;
			}
			
			if (myFilters != null && myFilters.length > 0) {
				
				final String[] theFilters = myFilters;
				
				if (!SmbMode) {
					filter = new FilenameFilter() {
						
						@Override
						public boolean accept(File dir, String filename) {
							
							if (!dir.isFile())
								return false;
							
							for (String filter : theFilters) {
								if (filename.toLowerCase(Locale.US).endsWith(filter))
								return true;
							}
							
							return false;
						}
					};
				} else {
					smbFilter = new SmbFileFilter() {
						
						@Override
						public boolean accept(SmbFile arg0) throws SmbException {
							
							if (!arg0.isFile())
								return false;
							
							String filename = arg0.getName();
							for (String filter : theFilters) {
								if (filename.toLowerCase(Locale.US).endsWith(filter))
								return true;
							}
							
							return false;
						}
					};
					
				}
			}
		} else {
			
			// No data - set to defaults.
			filter = null;
			currentDir = new File(Environment.getExternalStorageDirectory().getPath());
		}
		
		// Call populate for appropriate file listings.
		if (!SmbMode) {
			if (currentDir != null) {
				populateFileList(currentDir);
			} else {
				Log.e(TAG, "No local directory specified!");
			}
		} else {
			if (currentSmb != null) {
				populateSmbFileList(currentSmb);
			} else {
				Log.e(TAG, "No SMB directory specified!");
			}
		}
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
			
			if (SmbMode) {
				try {
					currentSmb = new SmbFile(adapter.getItem(0).getPath(), smbAuth);
					populateSmbFileList(currentSmb);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			} else {	
				currentDir = new File(adapter.getItem(0).getPath());
				populateFileList(currentDir);
			}
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
			
			if (SmbMode) {
				try {
					currentSmb = new SmbFile(o.getPath(), smbAuth);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				populateSmbFileList(currentSmb);
			} else {
				currentDir = new File(o.getPath());
				populateFileList(currentDir);
			}
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
	 * @param aFile
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
			e.printStackTrace();
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
	
	/**
	 * Method reads the current directory and populates the displayed
	 * list.
	 * @param aFile
	 */
	private void populateSmbFileList(SmbFile aFile)
	{
		// Set title to current directory.
		this.setTitle(aFile.getPath());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
		
		// Setup arrays to hold folder list and file list.
		List<FileDisplayLine>folders = new ArrayList<FileDisplayLine>();
		List<FileDisplayLine>files = new ArrayList<FileDisplayLine>();
		
		// Try and loop folder and file list and populate our display lists.
		try {
			SmbFile[]filterDirs = aFile.listFiles(smbFolderFilter);
			for (SmbFile loopDir : filterDirs) {
				folders.add(new FileDisplayLine(loopDir.getName(), sdf.format(new Date(loopDir.lastModified())), loopDir.getPath(), FileDisplayLine.FILETYPE_FOLDER, 0));
			}
			
			SmbFile[]filterFiles;
			if (smbFilter != null) {
				filterFiles = aFile.listFiles(smbFilter);
			} else {
				filterFiles = aFile.listFiles(smbFileFilter);
			}
			for (SmbFile loopFile : filterFiles) {
				files.add(new FileDisplayLine(loopFile.getName(), sdf.format(new Date(loopFile.lastModified())), loopFile.getPath(), FileDisplayLine.FILETYPE_FILE, loopFile.length()));
			}
		} catch (SmbException e) {
			e.printStackTrace();
		}
		
		// Sort the directory and file lists then add the files onto the end of the directory list.
		Collections.sort(folders);
		Collections.sort(files);
		folders.addAll(files);
		
		// Check if we need a parent director option.
		if (!aFile.getPath().equalsIgnoreCase(smbURL)) {
			folders.add(0, new FileDisplayLine("..", "Parent Directory", aFile.getParent(), FileDisplayLine.FILETYPE_PARENT, 0));
		}
		
		// Set the list adapter to display.
		adapter = new FileArrayAdapter(MagicFileSelector.this, R.layout.magic_file_selector_view, folders);
		this.setListAdapter(adapter);
	}
	
	
}
