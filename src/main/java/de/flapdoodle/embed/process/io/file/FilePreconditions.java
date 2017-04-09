package de.flapdoodle.embed.process.io.file;

import java.io.File;
import java.nio.file.Path;

import de.flapdoodle.checks.Preconditions;

public class FilePreconditions {

	public static Path isFile(Path path) {
		File asFile = path.toFile();
		Preconditions.checkArgument(asFile.exists(), "%s does not exist", path);
		Preconditions.checkArgument(asFile.isFile(), "%s is not a file", path);
		return path;
	}
}
