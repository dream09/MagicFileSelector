package com.magic09.magicfileselector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.magic09.magicfilechooser.R;
import com.magic09.magicutils.HelpDisplay;

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

/**
 * MagicFileSelector provides a simple file selection activity
 * that returns the path of the file selected.  It allows files
 * to be filtered.
 * @author dream09
 *
 */
public class MagicFileSelector extends AppCompatActivity {

	static final String TAG = "MagicFileSelector";
	
	/* Variables */
	private String myMode;
	private ListView mainList;
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
	private boolean displayHelp;
	private String helpTitle;
	private String helpText;
	private ShowcaseView showcaseView;
	
	public static final int FILE_REQUEST = 102030;
	public static final String MODE_FILE = "file";								// File selection mode (default)
	public static final String MODE_FOLDER = "folder";							// Folder selection mode.
	public static final String DATA_KEY_HELP_DISPLAY = "dataKeyHelpDisplay";	// Display help.
	public static final String DATA_KEY_HELP_TITLE = "dataKeyHelpTitle";		// Custom help title.
	public static final String DATA_KEY_HELP_TEXT = "dataKeyHelpText";			// Custom help text.
	public static final String DATA_KEY_MODE = "dataKeyMode";					// Key used to pass selection mode.
	public static final String DATA_KEY_RETURN = "dataKeyReturn";				// Key used to return data.
	public static final String DATA_KEY_FOLDER = "dataKeyFolder";				// Key used to pass a start folder.
	public static final String DATA_KEY_FILTER = "dataKeyFilter";				// Key used to pass a file filter.
	public static final String DATA_KEY_FILTERS = "dataKeyFilters";				// Key used to pass multiple file filters.
	public static final String DATA_KEY_IPADDRESS = "dataKeyIPAddress";			// Key used to pass an smb address.
	public static final String DATA_KEY_USERNAME = "dataKeyUsername";			// Key used to pass a username for the smb address.
	public static final String DATA_KEY_PASSWORD = "dataKeyPassword";			// Key used to pass a password for the smb address.
	
	
	
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

        // Set the view, get the list and setup the click listener
        setContentView(R.layout.magic_file_selector_layout);
        mainList = (ListView) findViewById(R.id.list);
        mainList.setOnItemClickListener(clickListener);

		// Setup the action bar if available
		// this will be implemented from the launching activity
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);

            int actionBarTitleId = getResources().getIdentifier("action_bar_title", "id", "android");
			TextView titleView = (TextView) findViewById(actionBarTitleId);
			if (titleView != null) {
				titleView.setTextSize(16f);
				titleView.setEllipsize(TextUtils.TruncateAt.START);
			}
		}
		
		// Get data sent (if any).
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			
			SmbMode = false;
			myMode = extras.getString(DATA_KEY_MODE);
			String myFolder = extras.getString(DATA_KEY_FOLDER);
			String myIPAddress= extras.getString(DATA_KEY_IPADDRESS);
			String myUsername= extras.getString(DATA_KEY_USERNAME);
			String myPassword= extras.getString(DATA_KEY_PASSWORD);
			displayHelp = extras.getBoolean(DATA_KEY_HELP_DISPLAY);
			helpTitle = extras.getString(DATA_KEY_HELP_TITLE);
			helpText = extras.getString(DATA_KEY_HELP_TEXT);
			
			// Check if browsing locally (default) or on network (ip address).
			if (myFolder != null && myFolder.length() > 0) {
				currentDir = new File(extras.getString(DATA_KEY_FOLDER));
			} else if (myIPAddress != null && myUsername != null && myPassword != null) {
				
				// Setup access path
				smbURL = "smb://" + myIPAddress + "/";
				
				// Show logging in progress dialog
				ProgressDialog pDialog = new ProgressDialog(this);
				pDialog.setMessage(getString(R.string.smb_accessing) + " " + smbURL + "\n\n" + getString(R.string.smb_please_wait));
				pDialog.setIndeterminate(true);
				pDialog.setCancelable(false);
				pDialog.show();
				
				smbAuth = new NtlmPasswordAuthentication(null, myUsername, myPassword);
				currentSmb = null;
				currentDir = null;
				try {
					currentSmb = new SmbFile(smbURL, smbAuth);
					SmbMode = true;
				} catch (MalformedURLException e) {
					pDialog.dismiss();
					e.printStackTrace();
				}
				
				pDialog.dismiss();
				
			} else {
				currentDir = new File(Environment.getExternalStorageDirectory().getPath());
			}
			
			// Check selection mode - setup folder long click if required.
			if (myMode != null && myMode.equals(MODE_FOLDER)) {
				
				// Setup and show folder select FAB
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(folderSelectButtonListener);
				
				// Setup long click listener for the list.
				mainList.setOnItemLongClickListener(longClickListener);
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
		
		// Call initial populate for appropriate file listings.
		if (!SmbMode) {
			if (currentDir != null) {
				updateFileList(currentDir.getPath());
			} else {
				Log.e(TAG, "No local directory specified!");
			}
		} else {
			if (currentSmb != null) {
				updateFileList(smbURL);
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
		if (adapter != null && adapter.getCount() > 0 && adapter.getItem(0).getType() == FileDisplayLine.FILETYPE_PARENT) {
			updateFileList(adapter.getItem(0).getPath());
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Display help if required
		if (displayHelp && helpTitle != null && helpText != null) {
			// General file help - no target
			if (myMode != null && myMode.equals(MODE_FILE))
				showcaseView = HelpDisplay.displayNoPointHelp(this, helpTitle, helpText);
			// Folder selection - target selection button
			//if (myMode != null && myMode.equals(MODE_FOLDER))
				//showcaseView = HelpDisplay.displayItemHelp(this, R.id.folderSelectButton, helpTitle, helpText);
		}
	}
	
	
	
	/* Listeners */
	
	/**
	 * Handle the click of an item.  If a folder show its contents.
	 * If a file return its path.
	 */
	private OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
			// Remove help
			if (showcaseView != null)
				showcaseView.hide();
			
			FileDisplayLine o = adapter.getItem(position);
			if (o.getType() == FileDisplayLine.FILETYPE_FOLDER || o.getType() == FileDisplayLine.FILETYPE_PARENT) {
				updateFileList(o.getPath());
			}
			if (o.getType() == FileDisplayLine.FILETYPE_FILE) {
				Intent data = new Intent();
				data.putExtra(MagicFileSelector.DATA_KEY_RETURN, o.getPath());
				setResult(RESULT_OK, data);
				finish();
			}
		}
	};
	
	/**
	 * Handle the long click of an item - currently only used in folder selection mode.
	 */
	private OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
		
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
			// Remove help
			if (showcaseView != null)
				showcaseView.hide();
			
			FileDisplayLine selectedItem = (FileDisplayLine) arg0.getItemAtPosition(position);
			final String fullPath = selectedItem.getPath();
			String folderName = foldernameFromPath(fullPath);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
			builder.setTitle(R.string.folder_dialog_title)
				.setMessage(view.getContext().getString(R.string.folder_dialog_text) + " \"" + folderName + "\"")
				.setPositiveButton(R.string.folder_dialog_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent data = new Intent();
						data.putExtra(MagicFileSelector.DATA_KEY_RETURN, fullPath);
						setResult(RESULT_OK, data);
						finish();
					}
				})
				.setNegativeButton(R.string.folder_dialog_no, null);
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
			return true;
		}
	};
	
	/**
	 * Handle a click on the select folder button.
	 */
	private OnClickListener folderSelectButtonListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// Remove help
			if (showcaseView != null)
				showcaseView.hide();
			
			final String fullPath;
			String folderName;
			if (!SmbMode) {
				fullPath = currentDir.getPath();
			} else {
				fullPath = currentSmb.getPath();
			}
			folderName = foldernameFromPath(fullPath);
			AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
			builder.setTitle(R.string.folder_dialog_title)
				.setMessage(getString(R.string.folder_dialog_text) + " \"" + folderName + "\"")
				.setPositiveButton(R.string.folder_dialog_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent data = new Intent();
						data.putExtra(MagicFileSelector.DATA_KEY_RETURN, fullPath);
						setResult(RESULT_OK, data);
						finish();
					}
				})
				.setNegativeButton(R.string.folder_dialog_no, null);
			
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	};
	
	
	
	/* Methods */
	
	/**
	 * Method updates the file list for the the argument path using
	 * the appropriate reader.
	 * @param path
	 */
	private void updateFileList(String path) {
		if (SmbMode) {
			SMBRead smbReader = new SMBRead(this, path);
			smbReader.execute();
		} else {
			LocalRead localReader = new LocalRead(path);
			localReader.execute();
		}
	}
	
	/**
	 * Method reads the current directory and populates the displayed
	 * list.
	 * @param aFile
	 */
	private List<FileDisplayLine> populateFileList(File aFile)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
		
		// Setup arrays to hold folder list and file list.
		List<FileDisplayLine>folders = new ArrayList<FileDisplayLine>();
		List<FileDisplayLine>files = new ArrayList<FileDisplayLine>();
		
		// Try and loop folder and file list and populate our display lists.
		try {
			File[]filterDirs = aFile.listFiles(folderFilter);
			for (File loopDir : filterDirs) {
				folders.add(new FileDisplayLine(foldernameFromPath(loopDir.getPath()), sdf.format(new Date(loopDir.lastModified())), loopDir.getAbsolutePath(), FileDisplayLine.FILETYPE_FOLDER, 0));
			}
			
			File[]filterFiles = aFile.listFiles(filter);
			for (File loopFile : filterFiles) {
				files.add(new FileDisplayLine(loopFile.getName(), sdf.format(new Date(loopFile.lastModified())), loopFile.getAbsolutePath(), FileDisplayLine.FILETYPE_FILE, loopFile.length()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sort the directory and file lists then add the files onto the end of the directory list
        // if required (in file mode)
		Collections.sort(folders);
		Collections.sort(files);
        if (myMode == null || myMode.equals(MODE_FILE)) {
            folders.addAll(files);
        }
		
		// Check if we need a parent director option.
		if (!aFile.getName().equalsIgnoreCase(Environment.getExternalStorageDirectory().getName())) {
			folders.add(0, new FileDisplayLine("..", "Parent Directory", aFile.getParent(), FileDisplayLine.FILETYPE_PARENT, 0));
		}
		
		return folders;
	}
	
	/**
	 * Method reads the current directory and returns a
	 * list of folders and files.
	 * @param aFile
	 */
	private List<FileDisplayLine> populateSmbFileList(SmbFile aFile)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
		
		// Setup arrays to hold folder list and file list.
		List<FileDisplayLine>folders = new ArrayList<FileDisplayLine>();
		List<FileDisplayLine>files = new ArrayList<FileDisplayLine>();
		
		// Try and loop folder and file list and populate our display lists.
		try {
			SmbFile[]filterDirs = aFile.listFiles(smbFolderFilter);
			for (SmbFile loopDir : filterDirs) {
				folders.add(new FileDisplayLine(foldernameFromPath(loopDir.getPath()), sdf.format(new Date(loopDir.lastModified())), loopDir.getPath(), FileDisplayLine.FILETYPE_FOLDER, 0));
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

        // Sort the directory and file lists then add the files onto the end of the directory list
        // if required (in file mode)
        Collections.sort(folders);
        Collections.sort(files);
        if (myMode == null || myMode.equals(MODE_FILE)) {
            folders.addAll(files);
        }
		
		// Check if we need a parent director option.
		if (!aFile.getPath().equalsIgnoreCase(smbURL)) {
			folders.add(0, new FileDisplayLine("..", "Parent Directory", aFile.getParent(), FileDisplayLine.FILETYPE_PARENT, 0));
		}
		
		return folders;
	}
	
	/**
	 * Method returns the last folder name in the argument path.
	 * @param path
	 * @return
	 */
	private String foldernameFromPath(String path) {
		String result = path;
		
		// Remove trailing "/"
		if (result.substring(result.length() - 1, result.length()).equals("/"))
			result = result.substring(0, result.length() - 1);
		
		// Trim name
		result = result.substring(result.lastIndexOf("/") + 1, result.length());
		
		return result;
	}
	
	
	
	/**
	 * LocalRead scans the current local file off the UI thread.
	 * @author dream09
	 *
	 */
	private class LocalRead extends AsyncTask<Void, Void, List<FileDisplayLine>> {
		
		/* Variables */
		private String path;
		
		/**
		 * Constructor.
		 * @param path
		 */
		public LocalRead(String path) {
			this.path = path;
		}
		
		/* Overridden methods */
		
		@Override
		protected List<FileDisplayLine> doInBackground(Void... params) {
			currentDir = new File(path);
			if (currentDir.exists())
				return populateFileList(currentDir);
			
			return null;
		}
		
		@Override
		protected void onPostExecute(List<FileDisplayLine> result) {
			// Set the list adapter to display.
			if (result != null) {
				adapter = new FileArrayAdapter(MagicFileSelector.this, R.layout.magic_file_selector_view, result);
				mainList.setAdapter(adapter);
				setTitle(path);
			}
		}
	}
	
	
	
	/**
	 * SMBRead scans the SMBFile off the UI thread presenting
	 * a progress indication.
	 * @author dream09
	 *
	 */
	private class SMBRead extends AsyncTask<Void, Void, List<FileDisplayLine>> {
		
		/* Variables */
		private Context context;
		private String path;
		private ProgressDialog pDialog = null;
		
		
		
		/**
		 * Constructor.
		 * @param context
		 */
		public SMBRead(Context context, String path) {
			this.context = context;
			this.path = path;
		}
		
		
		
		/* Overridden methods */
		
		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(context);
			pDialog.setMessage(getString(R.string.file_list_updating));
			pDialog.setCancelable(false);
			pDialog.setIndeterminate(true);
			pDialog.show();
		}
		
		@Override
		protected List<FileDisplayLine> doInBackground(Void... params) {
			
			try {
				currentSmb = new SmbFile(path, smbAuth);
				return populateSmbFileList(currentSmb);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(List<FileDisplayLine> result) {
			
			// Set the list adapter to display.
			if (result != null) {
				adapter = new FileArrayAdapter(MagicFileSelector.this, R.layout.magic_file_selector_view, result);
				mainList.setAdapter(adapter);
				setTitle(path);
			}
			
			pDialog.dismiss();
		}
	}
}
