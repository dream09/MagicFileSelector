package com.magic09.magicfileselector;

import java.util.Locale;

/**
 * FileLineDisplay provides a class used to display files and folders
 * within MagicFileSelector.
 * @author dream09
 *
 */
public class FileDisplayLine implements Comparable<FileDisplayLine>
{

	/* Constants */
	public static String FILETYPE_PARENT = "fileTypeParent";
	public static String FILETYPE_FOLDER = "fileTypeFolder";
	public static String FILETYPE_FILE = "fileTypeFile";
	
	/* Variables */
	private String name;
	private String data;
	private String path;
	private String type;
	private long size;
	
	
	/**
	 * Constructor.
	 */
	public FileDisplayLine(String name, String data, String path, String type, long size)
	{
		this.name = name;
		this.data = data;
		this.path = path;
		this.type = type;
		this.size = size;
	}
	
	
	
	/* Getter methods */
	
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return data;
	}
	public String getPath()
	{
		return path;
	}
	public String getType()
	{
		return type;
	}
	public long getSize()
	{
		return size;
	}
	
	
	
	/* Overridden method */
	
	@Override
	public int compareTo(FileDisplayLine another) {
		if (this.name != null) {
			return this.name.toLowerCase(Locale.US).compareTo(another.getName().toLowerCase(Locale.US));
		} else {
			throw new IllegalArgumentException();
		}
	}
	
}
