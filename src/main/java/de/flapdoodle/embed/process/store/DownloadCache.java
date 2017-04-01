package de.flapdoodle.embed.process.store;

import java.net.URL;
import java.nio.file.Path;

public interface DownloadCache {
	Path getOrDownload(Path artifactPath, URL downloadUrl); 
}
