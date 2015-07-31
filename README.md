MagicFileSelector
=================
An Android library project that provides a simple file chooser activity.  This project was developed in [Android Studio](http://developer.android.com/tools/studio/).

Adding MagicFileSelector to your project
----------------------------------------
**1. Gradle dependency (Android Studio)**

 - 	Add the following to your `build.gradle`:
 ```gradle
repositories {
	    maven { url "https://jitpack.io" }
}

dependencies {
	    compile 'com.github.dream09:MagicFileSelector:3.0'
}
```

**2. Maven**
- Add the following to your `pom.xml`:
 ```xml
<repository>
       	<id>jitpack.io</id>
	    <url>https://jitpack.io</url>
</repository>

<dependency>
	    <groupId>com.github.dream09</groupId>
	    <artifactId>MagicFileSelector</artifactId>
	    <version>3.0</version>
</dependency>
```

**3. Jar file only**
 - Get the [**latest release .jar file**](https://github.com/dream09/MagicEula/releases) from the releases area
 - Copy the **MagicEula-X.X.jar** file into the `libs` folder of your Android project
 - Start using the library

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
String[] filters = {".csv", ".txt"};
intent.putExtra(MagicFileSelector.DATA_FILTERS, filters);
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
