package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCBookMeta;
import java.util.List;
import org.bukkit.inventory.meta.BookMeta;

public class BukkitMCBookMeta extends BukkitMCItemMeta implements MCBookMeta {

	BookMeta bm;
	public BukkitMCBookMeta(BookMeta im) {
		super(im);
		this.bm = im;
	}

	public BukkitMCBookMeta(AbstractionObject o) {
		super(o);
		this.bm = (BookMeta) o;
	}

	public BookMeta getBookMeta() {
		return bm;
	}

	public boolean hasTitle() {
		return bm.hasTitle();
	}

	public boolean hasAuthor() {
		return bm.hasAuthor();
	}

	public boolean hasPages() {
		return bm.hasPages();
	}

	public String getTitle() {
		return bm.getTitle();
	}

	public String getAuthor() {
		return bm.getAuthor();
	}

	public List<String> getPages() {
		return bm.getPages();
	}

	public int getPageCount() {
		return bm.getPageCount();
	}

	public void addPage(String... pages) {
		bm.addPage(pages);
	}

	public boolean setTitle(String title) {
		return bm.setTitle(title);
	}

	public void setAuthor(String author) {
		bm.setAuthor(author);
	}

	public void setPages(List<String> pages) {
		bm.setPages(pages);
	}

	public void setPages(String... pages) {
		bm.setPages(pages);
	}

}
