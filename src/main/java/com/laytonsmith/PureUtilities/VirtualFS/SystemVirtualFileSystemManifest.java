package com.laytonsmith.PureUtilities.VirtualFS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the underlying manifest file. Generally, it should only be necessary to use this version,
 * but it implements an interface which should be used throughout the code to allow for easier testing.
 */
public final class SystemVirtualFileSystemManifest implements VirtualFileSystemManifest {

	private static final Map<File, VirtualFileSystemManifest> INSTANCE = new HashMap<>();

	/**
	 * There should only be one accessor for the manifest file in the system, but for test purposes, it may be
	 * useful to mock the interface. This method is used to get the instance for each manifest file (of which
	 * there will most likely ever only be one). A file listener will be added to the underlying file so that
	 * changes from other processes can be reflected here as well, but the file can be manually refreshed as well.
	 * @param manifestFile The underlying manifest file.
	 * @return The VirtualFileSystemManifest wrapping the given manifestFile
	 * @throws IOException If there is some error when loading the manifest
	 */
	public static VirtualFileSystemManifest getInstance(File manifestFile) throws IOException {
		if(!INSTANCE.containsKey(manifestFile)) {
			INSTANCE.put(manifestFile, new SystemVirtualFileSystemManifest(manifestFile));
		}
		return INSTANCE.get(manifestFile);
	}

	private final Set<String> manifest;
	private final File manifestFile;

	private final WatchService watcher = FileSystems.getDefault().newWatchService();

	private SystemVirtualFileSystemManifest(File manifestFile) throws FileNotFoundException, IOException {
		this.manifestFile = manifestFile;
		manifest = Collections.synchronizedSet(new TreeSet<>());
		if(!manifestFile.exists()) {
			save();
		} else {
			read(manifestFile, manifest);
		}
		Path p = manifestFile.getParentFile().toPath();
		final WatchKey watchKey = p.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		new Thread(() -> {
			while(true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}
				for(WatchEvent<?> event : key.pollEvents()) {
					if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path path = ev.context();
						if(path.equals(manifestFile.toPath())) {
							try {
								refresh();
							} catch (IOException ex) {
								Logger.getLogger(SystemVirtualFileSystemManifest.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				}
			}
		}, SystemVirtualFileSystemManifest.class.getSimpleName() + " Manifest Watcher").start();
	}

	private void read(File manifestFile, Set<String> manifest) throws IOException {
		try {
			// Read the file in outside of the synchronization block
			Set<String> fileSet = (Set<String>) new ObjectInputStream(new FileInputStream(manifestFile)).readObject();
			synchronized(manifest) {
				manifest.clear();
				manifest.addAll(fileSet);
			}
		} catch (ClassNotFoundException ex) {
			throw new Error(ex);
		}
	}

	private void save() throws IOException {
		new ObjectOutputStream(new FileOutputStream(manifestFile)).writeObject(manifest);
	}

	@Override
	public boolean fileInManifest(VirtualFile file) {
		String p = file.getPath();
		return manifest.contains(p);
	}

	@Override
	public void removeFromManifest(VirtualFile file) throws IOException {
		manifest.remove(file.getPath());
		save();
	}

	@Override
	public void addToManifest(VirtualFile file) throws IOException {
		manifest.add(file.getPath());
		save();
	}

	@Override
	public void refresh() throws IOException {
		System.out.println("Refreshing file");
		read(manifestFile, manifest);
	}
}
