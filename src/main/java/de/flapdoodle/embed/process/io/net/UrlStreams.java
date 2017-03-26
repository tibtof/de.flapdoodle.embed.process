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
package de.flapdoodle.embed.process.io.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.types.Optionals;
import de.flapdoodle.embed.process.types.ThrowingFunction;
import de.flapdoodle.embed.process.types.ThrowingSupplier;
import de.flapdoodle.transition.Preconditions;

public abstract class UrlStreams {

	static final int BUFFER_LENGTH = 1024 * 8 * 8;
	static final int READ_COUNT_MULTIPLIER = 100;

	public File download(DownloadConfig downloadConfig, Distribution distribution) throws IOException {

		File ret = Files.createTempFile(PropertyOrPlatformTempDir.defaultInstance(), downloadConfig.getFileNaming()
				.nameFor(downloadConfig.getDownloadPrefix(), "." + downloadConfig.getPackageResolver().packageFor(distribution).archiveType()));
		
		String userAgent = downloadConfig.getUserAgent();
		TimeoutConfig timeoutConfig = downloadConfig.getTimeoutConfig();
		URL url = new URL(downloadConfig.getDownloadPath().getPath(distribution) + downloadConfig.getPackageResolver().packageFor(distribution).archivePath());
		Optional<Proxy> proxy = downloadConfig.proxyFactory().map(f -> f.createProxy());
		
		if (ret.canWrite()) {
			
			URLConnection openConnection = urlConnectionOf(url, userAgent, timeoutConfig, proxy);
			downloadAndCopy(openConnection, () -> new BufferedOutputStream(new FileOutputStream(ret)), (readCount, contentLength) -> {
				
			});
			
		} else {
			throw new IOException("Can not write " + ret);
		}
		return ret;
	}
	
	public static <E extends Exception> void downloadTo(URLConnection connection, Path destination, DownloadCopyListener copyListener) throws IOException {
		downloadTo(connection, destination, c -> downloadIntoTempFile(c, copyListener));
	}

	protected static <E extends Exception> void downloadTo(URLConnection connection, Path destination, ThrowingFunction<URLConnection, Path, E> urlToTempFile) throws IOException,E {
		Preconditions.checkArgument(!destination.toFile().exists(), "destination exists");
		Path tempFile = urlToTempFile.apply(connection);
		java.nio.file.Files.copy(tempFile, destination);
		java.nio.file.Files.delete(tempFile);
	}
	
	protected static Path downloadIntoTempFile(URLConnection connection, DownloadCopyListener copyListener) throws IOException, FileNotFoundException {
		Path tempFile = java.nio.file.Files.createTempFile("download", "");
		boolean downloadSucceeded=false; 
		try {
			downloadAndCopy(connection, () -> new BufferedOutputStream(new FileOutputStream(tempFile.toFile())), copyListener);
			downloadSucceeded=true;
			return tempFile;
		} finally {
			if (!downloadSucceeded) {
				java.nio.file.Files.delete(tempFile);
			}
		}
	}

	private static <E extends Exception> void downloadAndCopy(URLConnection connection, ThrowingSupplier<BufferedOutputStream, E> output, DownloadCopyListener copyListener) throws IOException, E {
		long length = connection.getContentLengthLong();
		try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
			try (BufferedOutputStream bos = output.get()) {
				byte[] buf = new byte[BUFFER_LENGTH];
				int read = 0;
				long readCount = 0;
				while ((read = bis.read(buf)) != -1) {
					bos.write(buf, 0, read);
					readCount = readCount + read;
					Preconditions.checkArgument(length==-1 || length>=readCount, "hmm.. readCount bigger than contentLength(more than we want to): %s > %s",readCount, length);
					copyListener.downloaded(readCount, length);
				}
				bos.flush();
				Preconditions.checkArgument(length==-1 || length==readCount, "hmm.. readCount smaller than contentLength(partial download?): %s > %s",readCount, length);
			}
		}
	}

	public static URLConnection urlConnectionOf(URL url, String userAgent, TimeoutConfig timeoutConfig, Optional<Proxy> proxy) throws IOException {
		URLConnection openConnection = Optionals.wrap(proxy)
			.map(p -> url.openConnection(p))
			.orElseGet(() -> url.openConnection());
		
		openConnection.setRequestProperty("User-Agent",userAgent);
		openConnection.setConnectTimeout(timeoutConfig.getConnectionTimeout());
		openConnection.setReadTimeout(timeoutConfig.getReadTimeout());
		return openConnection;
	}
	
	public static interface DownloadCopyListener {
		void downloaded(long bytesCopied, long contentLength);
	}
}
