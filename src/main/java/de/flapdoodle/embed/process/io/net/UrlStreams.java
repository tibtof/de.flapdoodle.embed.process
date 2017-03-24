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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.TimeoutConfig;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.types.Optionals;

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

			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ret))) {
			
				downloadAndCopy(url, userAgent, timeoutConfig, proxy, bos);
				bos.flush();
			}
			
		} else {
			throw new IOException("Can not write " + ret);
		}
		return ret;
	}

	private static void downloadAndCopy(URL url, String userAgent, TimeoutConfig timeoutConfig, Optional<Proxy> proxy, BufferedOutputStream bos) throws IOException {
		URLConnection openConnection = Optionals.wrap(proxy)
			.map(p -> url.openConnection(p))
			.orElseGet(() -> url.openConnection());
		
		openConnection.setRequestProperty("User-Agent",userAgent);
		
		
		openConnection.setConnectTimeout(timeoutConfig.getConnectionTimeout());
		openConnection.setReadTimeout(timeoutConfig.getReadTimeout());
		
		long length = openConnection.getContentLength();
		try (InputStream downloadStream = openConnection.getInputStream()) {
				BufferedInputStream bis = new BufferedInputStream(downloadStream);
				byte[] buf = new byte[BUFFER_LENGTH];
				int read = 0;
				long readCount = 0;
				while ((read = bis.read(buf)) != -1) {
					bos.write(buf, 0, read);
					readCount = readCount + read;
					if (readCount > length) length = readCount;
	
				}
		}
	}
}
