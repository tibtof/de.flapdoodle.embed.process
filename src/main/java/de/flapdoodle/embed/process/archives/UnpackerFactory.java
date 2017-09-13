package de.flapdoodle.embed.process.archives;

import de.flapdoodle.embed.process.distribution.ArchiveType;

public interface UnpackerFactory {
	Unpacker of(ArchiveType type);
}
