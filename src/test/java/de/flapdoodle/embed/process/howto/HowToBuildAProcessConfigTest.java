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
package de.flapdoodle.embed.process.howto;

import static de.flapdoodle.transition.NamedType.typeOf;

import org.junit.Test;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.embed.process.types.DownloadPath;
import de.flapdoodle.transition.initlike.InitLike;
import de.flapdoodle.transition.initlike.InitLike.Init;
import de.flapdoodle.transition.initlike.InitRoutes;
import de.flapdoodle.transition.initlike.State;
import de.flapdoodle.transition.routes.Bridge;
import de.flapdoodle.transition.routes.SingleDestination;
import de.flapdoodle.transition.routes.Start;

public class HowToBuildAProcessConfigTest {

	@Test
	public void simpleSample() {
		InitRoutes<SingleDestination<?>> routes = InitRoutes.builder()
			.add(Start.of(typeOf(Version.class)), () -> State.of(Version.of("2.1.1")))
			.add(Start.of(typeOf(DownloadPath.class)), () -> State.of(DownloadPath.of("https://bitbucket.org/ariya/phantomjs/downloads/")))
			.add(Bridge.of(typeOf(Version.class), typeOf(Distribution.class)), (version) -> State.of(Distribution.detectFor(version)))
			.add(Bridge.of(typeOf(Distribution.class), typeOf(DistributionPackage.class)), (distribution) -> packageOf(distribution))
			.build();
		
		try (Init<DistributionPackage> init = InitLike.with(routes).init(typeOf(DistributionPackage.class))) {
			System.out.println("current: "+init.current());
			
		}
	}

	private static State<DistributionPackage> packageOf(Distribution distribution) {
		ArchiveType archiveType = getArchiveType(distribution);
		return State.of(DistributionPackage.of(archiveType, fileSetFor(distribution), getPath(distribution, archiveType)));
	}
	
	private static FileSet fileSetFor(Distribution distribution) {
		String execName;
		switch (distribution.platform()) {
			case Windows:
				execName="phantomjs.exe";
				break;
			default:
				execName="phantomjs";
		}
		
		return FileSet.builder()
				.addEntry(FileType.Executable,execName)
				.build();
	}
	
	private static ArchiveType getArchiveType(Distribution distribution) {
		switch (distribution.platform()) {
			case OS_X:
			case Windows:
				return ArchiveType.ZIP;
		}
		return ArchiveType.TBZ2;
	}

	private static String getPath(Distribution distribution, ArchiveType archiveType) {
		final String packagePrefix;
		String bitVersion="";
		switch (distribution.platform()) {
			case OS_X:
				packagePrefix="macosx";
				break;
			case Windows:
				packagePrefix="windows";
				break;
			default:
				packagePrefix="linux";
				switch (distribution.bitsize()) {
					case B64:
						bitVersion="-x86_64";
						break;
					default:
						bitVersion="-i686";
				}
		}
		
		String packageExtension=".zip";
		if (archiveType==ArchiveType.TBZ2) {
			packageExtension=".tar.bz2";
		}
		return "phantomjs-"+distribution.version().asInDownloadPath()+"-"+packagePrefix+bitVersion+packageExtension;
	}

}
