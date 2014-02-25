MagicFileSelector
=================
An Android library project that provides a simple file chooser activity.  This project was developed in [Eclipse](http://www.eclipse.org/downloads/) using the [Android ADT plugin for Eclipse](http://developer.android.com/sdk/installing/installing-adt.html) using JDK 7.


Cloning MagicFileSelector source from GitHub
--------------------------------------------
Follow these steps to clone the source if you would like to use this project and/or contribute.

1. Switch to the *Git Repository Exploring* perspective in Eclipse.
2. Copy the URI for this project https://github.com/dream09/MagicFileSelector.git.
3. Click *Clone a Git Repository* and paste the URI from step 2.
4. The *Host* and *Repository* path fields should populate automatically. Click *Next >*.
5. If you wish to use the latest stable version as a library for another project ensure the *master* branch is checked. If you wish to contribute to MagicFileSelector ensure the *develop* branch is checked.
6. Make any changes you wish in the **Local Destination** dialogue (remember - short paths close to root and without spaces are recommended), and click *Finish*.
7. Wait for the repository to be cloned.
8. If you would like to use the latest stable version check out the latest tag, for example v1.0, by:
	- Right-click the repository and select *Switch To → Other...*
	- Select Tags → v1.0 (or whatever is the latest)
	- Click *Checkout*
9. Right-click the repository and select *Import Projects...*.
10. Select the *Use the New Project wizard* option and click *Finish*.
11. Select *Android Project from Existing Code* under the *Android* folder and click *Next >*.
12. Click *Browse* and locate the project directory you cloned to in step 6 then click *Finish*.
13. Switch to the *Java* perspective.
14. Right-click the MagicFileSelector project and click *Properties*.
15. Under *Android* options check the *Is Library* check box and click *OK*.


Using MagicFileSelector for local file system (eg sdcard)
---------------------------------------------------------
* Ensure you have imported the project as described above and that it is set as a library project.

* Ensure your AndroidManifest.xml lists com.magic09.magicfileselector.MagicFileSelector as an activity.

* Start the activity for a result passing any file type filter you require, for example:
```
public static final int FILE_REQUEST = 2

Intent intent = new Intent(getActivity(), MagicFileSelector.class);
intent.putExtra(MagicFileSelector.DATA_KEY_FILTER, "*.csv");
startActivityForResult(intent, FILE_REQUEST);
```

* Handle the return using onActivityResult, for example:
```
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == FILE_REQUEST && resultCode == Activity.RESULT_OK) {
			if (data.hasExtra(MagicFileSelector.DATA_KEY_RETURN)) {
				String filePath = data.getExtras().getString(MagicFileSelector.DATA_KEY_RETURN);
			}
		}
}
```


Using MagicFileSelector for SMB/CIFS file system
------------------------------------------------
* Ensure you have imported the project as described above and that it is set as a library project.

* Ensure your AndroidManifest.xml lists com.magic09.magicfileselector.MagicFileSelector as an activity.

* Start the activity for a result passing any file type filter you require, for example:
```
public static final int FILE_REQUEST = 2

Intent intent = new Intent(getActivity(), MagicFileSelector.class);
intent.putExtra(MagicFileSelector.DATA_KEY_IPADDRESS, ipaddress);
intent.putExtra(MagicFileSelector.DATA_KEY_USERNAME, username);
intent.putExtra(MagicFileSelector.DATA_KEY_PASSWORD, password);
startActivityForResult(intent, FILE_REQUEST);
```

* Handle the return using onActivityResult, for example:
```
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == FILE_REQUEST && resultCode == Activity.RESULT_OK) {
			if (data.hasExtra(MagicFileSelector.DATA_KEY_RETURN)) {
				String filePath = data.getExtras().getString(MagicFileSelector.DATA_KEY_RETURN);
			}
		}
}
```


Contributing to MagicFileSelector
---------------------------------

If you wish to contribute please create a feature branch from the *develop* branch and name *feature-yourfeature*.
