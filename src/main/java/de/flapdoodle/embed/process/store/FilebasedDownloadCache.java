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
