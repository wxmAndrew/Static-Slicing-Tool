package de.uni_passau.fim.se2.slicer.util.cfg;

import java.util.Objects;

/** Represents a local variable of a method as it is in the class file's local variable table. */
public class LocalVariable {

  private final String name;
  private final String descriptor;
  private final String signature;
  private final int index;

  LocalVariable(
      final String pName, final String pDescriptor, final String pSignature, final int pIndex) {
    name = pName;
    descriptor = pDescriptor;
    signature = pSignature;
    index = pIndex;
  }

  public int getIndex() {
    return index;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public String getName() {
    return name;
  }

  public String getSignature() {
    return signature;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object pOther) {
    if (this == pOther) {
      return true;
    }
    if (pOther == null || getClass() != pOther.getClass()) {
      return false;
    }
    final LocalVariable that = (LocalVariable) pOther;
    return index == that.index
        && Objects.equals(name, that.name)
        && Objects.equals(descriptor, that.descriptor)
        && Objects.equals(signature, that.signature);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(name, descriptor, signature, index);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "LocalVariable{"
        + "name='"
        + name
        + '\''
        + ", descriptor='"
        + descriptor
        + '\''
        + ", signature='"
        + signature
        + '\''
        + ", index="
        + index
        + '}';
  }
}
