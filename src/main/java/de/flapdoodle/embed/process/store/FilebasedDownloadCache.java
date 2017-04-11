/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.process.store;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.embed.process.io.net.UrlStreams;
import de.flapdoodle.embed.process.io.net.UrlStreams.DownloadCopyListener;

public class FilebasedDownloadCache implements DownloadCache {

	private final Path directory;
	private final Function<URL, URLConnection> urlAsConnection;
	private final DownloadCopyListener listener;

	public FilebasedDownloadCache(Path directory, Function<URL, URLConnection> connection, DownloadCopyListener listener) {
		this.directory = directory;
		this.urlAsConnection = connection;
		this.listener = listener;
	}
	
	@Override
	public Path getOrDownload(Path artifactPath, URL downloadUrl) throws IOException {
		Preconditions.checkArgument(!willEscapeDirectory(artifactPath), "invalid artifact path: "+artifactPath);
		Path resolvedArtifactPath = directory.resolve(artifactPath);
		if (!checkValidArtifact(resolvedArtifactPath)) {
			Path tempFile=Files.createTempFile("download", "");
			Files.delete(tempFile);
			try {
				UrlStreams.downloadTo(urlAsConnection.apply(downloadUrl), tempFile, listener);
				Files.copy(tempFile, resolvedArtifactPath);
				Preconditions.checkArgument(checkValidArtifact(resolvedArtifactPath),"download %s to %s failed", downloadUrl, resolvedArtifactPath);
			} finally {
				Files.deleteIfExists(tempFile);
			}
		}
		return resolvedArtifactPath;
	}

	protected static boolean willEscapeDirectory(Path path) {
		if (path.isAbsolute()) return true;
		
		String prefix = UUID.randomUUID().toString();
		Path prefixedAndNormalized = Paths.get(prefix).resolve(path).normalize();
		
		return !prefixedAndNormalized.startsWith(prefix) || hasOnlyOneElement(prefixedAndNormalized);
	}
	
	private static boolean hasOnlyOneElement(Path prefixedAndNormalized) {
		Iterator<Path> iterator = prefixedAndNormalized.iterator();
		if (iterator.hasNext()) {
			iterator.next();
			return !iterator.hasNext();
		}
		return false;
	}

	protected static boolean checkValidArtifact(Path path) {
		File asFile = path.toFile();
		return asFile.exists() && !asFile.isDirectory();
	}
}
