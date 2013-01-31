package com.laytonsmith.abstraction;

import java.util.List;

/**
 * 
 * @author jb_aero
 */
public interface MCBookMeta extends MCItemMeta {

	public boolean hasTitle();
	public boolean hasAuthor();
	public boolean hasPages();
	
	public String getTitle();
	public String getAuthor();
	public List<String> getPages();
	public int getPageCount();
	
	public void addPage(String... pages);
	
	public boolean setTitle(String title);
	public void setAuthor(String author);
	public void setPages(List<String> pages);
	public void setPages(String... pages);
}
