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

	@Override
	public boolean hasTitle() {
		return bm.hasTitle();
	}

	@Override
	public boolean hasAuthor() {
		return bm.hasAuthor();
	}

	@Override
	public boolean hasPages() {
		return bm.hasPages();
	}

	@Override
	public String getTitle() {
		return bm.getTitle();
	}

	@Override
	public String getAuthor() {
		return bm.getAuthor();
	}

	@Override
	public List<String> getPages() {
		return bm.getPages();
	}

	@Override
	public int getPageCount() {
		return bm.getPageCount();
	}

	@Override
	public void addPage(String... pages) {
		bm.addPage(pages);
	}

	@Override
	public boolean setTitle(String title) {
		return bm.setTitle(title);
	}

	@Override
	public void setAuthor(String author) {
		bm.setAuthor(author);
	}

	@Override
	public void setPages(List<String> pages) {
		bm.setPages(pages);
	}

	@Override
	public void setPages(String... pages) {
		bm.setPages(pages);
	}

}
