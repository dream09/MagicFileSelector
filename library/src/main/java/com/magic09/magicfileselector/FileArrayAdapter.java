package com.magic09.magicfileselector;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.magic09.magicfilechooser.R;

import java.util.List;
import java.util.Locale;



/**
 * FileArrayAdapter provides the layout for each list item displayed
 * in MagicFileSelector.
 * @author dream09
 */
public class FileArrayAdapter extends ArrayAdapter<FileDisplayLine>
{
	
	/* Variables */
	private Context context;
	private int id;
	private List<FileDisplayLine> items;
	
	
	
	/**
	 * Constructor.
	 * @param context The required context.
	 * @param textViewResourceId The view to use to display each item in the list.
	 * @param objects The list to display.
	 */
	public FileArrayAdapter(Context context, int textViewResourceId, List<FileDisplayLine> objects) {
		super(context, textViewResourceId, objects);
		
		this.context = context;
		this.id = textViewResourceId;
		this.items = objects;
	}
	
	
	
	/* Overridden methods */

	@NonNull
    @Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		
		// Get the view and inflate.
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		
		// Get position and setup accordingly.
		final FileDisplayLine o = getItem(position);
		if (o != null) {
			
			// Get text views and setup with appropriate data.
			TextView itemName = (TextView) v.findViewById(R.id.ItemName);
			TextView itemDetails = (TextView) v.findViewById(R.id.ItemDetails);
			TextView itemSize = (TextView) v.findViewById(R.id.ItemSize);
			ImageView icon = (ImageView) v.findViewById(R.id.show_icon);
			if (itemName != null) 
				itemName.setText(o.getName());
			if (itemDetails != null)
				itemDetails.setText(o.getData());
			if (itemSize != null) {
				if (FileDisplayLine.FILETYPE_FILE.equals(o.getType())) {
					itemSize.setText(humanReadableByteCount(o.getSize(), true));
				} else {
					itemSize.setText("");
				}
			}
			
			// Show correct icon (folder or file).
			if (icon != null) {
				if (FileDisplayLine.FILETYPE_FILE.equals(o.getType())) {
					icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_file_24dp));
				} else if (FileDisplayLine.FILETYPE_FOLDER.equals(o.getType()) ||
                        FileDisplayLine.FILETYPE_PARENT.equals(o.getType())) {
					icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_folder_24dp));
				}
			}
		}
		
		return v;
	}
	
	
	
	/* Methods */
	
	/**
	 * Method returns the option specified by the
	 * argument i.
	 */
	public FileDisplayLine getItem(int i)
	{
		return items.get(i);
	}
	
	/**
	 * Method returns a readable file size based on the arguments
	 * bytes and si.
	 * @param bytes The size in bytes.
	 * @param si Use SI units.
	 * @return A string containing readable file size.
	 */
	private String humanReadableByteCount(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		
		return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}
