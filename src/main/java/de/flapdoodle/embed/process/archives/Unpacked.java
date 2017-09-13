package de.flapdoodle.embed.process.archives;

import java.nio.file.Path;
import java.util.Map;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(strictBuilder=true)
public interface Unpacked {
	Path basePath();
	Map<Path, FileType> content();
}
