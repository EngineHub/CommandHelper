package com.laytonsmith.abstraction;

import java.util.List;

public interface MCBookMeta extends MCItemMeta {

	boolean hasTitle();

	boolean hasAuthor();

	boolean hasPages();

	String getTitle();

	String getAuthor();

	List<String> getPages();

	int getPageCount();

	void addPage(String... pages);

	boolean setTitle(String title);

	void setAuthor(String author);

	void setPages(List<String> pages);

	void setPages(String... pages);
}
