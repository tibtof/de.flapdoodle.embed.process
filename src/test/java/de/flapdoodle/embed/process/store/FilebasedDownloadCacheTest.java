package de.flapdoodle.embed.process.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Test;

public class FilebasedDownloadCacheTest {

	@Test
	public void pathCouldEscapeDirectory() {
		assertTrue(FilebasedDownloadCache.willEscapeDirectory(Paths.get("..")));
		assertTrue(FilebasedDownloadCache.willEscapeDirectory(Paths.get("bar", "..")));
	}
	
	@Test
	public void pathDoesNotEscapeDirectory() {
		assertFalse(FilebasedDownloadCache.willEscapeDirectory(Paths.get("foo","bar")));
		assertFalse(FilebasedDownloadCache.willEscapeDirectory(Paths.get("foo","..","bar")));
	}
}
