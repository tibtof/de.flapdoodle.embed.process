package de.flapdoodle.embed.process.types;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Style(
		typeAbstract = "_*", 
		typeImmutable = "*", 
		visibility = ImplementationVisibility.PUBLIC, 
		defaults = @Value.Immutable(builder = false, copy = false))
public @interface Wrapped {}