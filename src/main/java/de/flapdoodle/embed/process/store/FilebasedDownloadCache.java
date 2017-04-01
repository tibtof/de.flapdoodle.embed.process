package de.flapdoodle.embed.process.store;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import de.flapdoodle.transition.Preconditions;

public class FilebasedDownloadCache implements DownloadCache {

	private final Path directory;
	private final BiConsumer<URL, Path> downloader;

	public FilebasedDownloadCache(Path directory, BiConsumer<URL, Path> downloader) {
		this.directory = directory;
		this.downloader = downloader;
	}
	
	@Override
	public Path getOrDownload(Path artifactPath, URL downloadUrl) {
		Preconditions.checkArgument(!artifactPath.isAbsolute(), "is absolute artifact path: "+artifactPath);
		Path resolvedArtifactPath = directory.relativize(artifactPath);
		if (!checkValidArtifact(resolvedArtifactPath)) {
			downloader.accept(downloadUrl, resolvedArtifactPath);
			Preconditions.checkArgument(checkValidArtifact(resolvedArtifactPath),"download %s to %s failed", downloadUrl, resolvedArtifactPath);
		}
		return resolvedArtifactPath;
	}

	private static boolean checkValidArtifact(Path path) {
		File asFile = path.toFile();
		return asFile.exists() && !asFile.isDirectory();
	}
}
