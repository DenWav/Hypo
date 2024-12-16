package scenario04;

// Compiled with JDK 21
public sealed interface TestClass {}

final class TestSubClass implements TestClass {}

sealed class TestSealedSubClass implements TestClass {}

final class TestSubSubClass extends TestSealedSubClass {}
