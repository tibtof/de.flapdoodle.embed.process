package de.flapdoodle.embed.process.types;

import org.immutables.value.Value;

abstract class Wrapper<T> {
  @Value.Parameter
  public abstract T value();
  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + value() + ")";
  }
}