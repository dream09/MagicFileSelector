MagicFileSelector
=================
An Android library project (Eclipse) that provides a simple file chooser activity.


Using MagicFileSelector
-----------------------
* Ensure your project is set to use this library project.

* Ensure your AndroidManifest.xml lists com.magic09.magicfileselector.MagicFileSelector as an activity.

* Start the activity for a result passing any file type filter you require, for example:
```
Intent intent = new Intent(getActivity(), MagicFileSelector.class);
intent.putExtra(MagicFileSelector.DATA_KEY_FILTER, "*.csv");
startActivityForResult(intent, MagicFileSelector.FILE_REQUEST);
```

* Handle the return using onActivityResult, for example:
```
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == MagicFileSelector.FILE_REQUEST && resultCode == Activity.RESULT_OK) {
			if (data.hasExtra(MagicFileSelector.DATA_KEY_RETURN)) {
				String filePath = data.getExtras().getString(MagicFileSelector.DATA_KEY_RETURN);
			}
		}
}
```

