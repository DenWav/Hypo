Hypo
====

[![Maven Central Version 1.2.6](https://img.shields.io/badge/Maven_Central-1.2.6-blue?logo=apache-maven&style=flat)](https://search.maven.org/search?q=g:dev.denwav.hypo)
[![Test](https://github.com/DenWav/Hypo/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/DenWav/Hypo/actions/workflows/test.yml)

Hypo is a model for Java bytecode inspection. The main idea behind Hypo is to separate the process of determining
facts about Java bytecode, and the analysis of those facts.

The logic for determining things from bytecode can sometimes be a little tricky, but once that's worked out, by storing
that data in the model and making it easy to access later, it's much simpler to analyze by just asking the model for
that data which has already been pre-computed, rather than combining the logic to compute the data with your analysis
logic.

> <details>
> <summary>Note about the connection with Lorenz obfuscation mapping</summary>
>
> Hypo is not tied to Java obfuscation mapping analysis, but the primary purpose for Hypo is in the `hypo-mappings` module
> which uses Hypo and Lorenz for Java obfuscation mapping analysis using Hypo's bytecode analytical model. This is the
> only module (except for `hypo-test`) which uses Lorenz and the rest of Hypo can be used independently of that.
> </details>

## Getting Hypo

Releases of Hypo are deployed to Maven Central.

> <details>
> <summary>Using SNAPSHOT versions</summary>
> 
> You can also use the latest SNAPSHOT commit to `main` with Sonatype's snapshot repo:
> 
> ```kotlin
> repositories {
>     maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
> }
> ```
> </details>

The easiest way to use Hypo is to use `hypo-platform` to keep the multiple versions of the artifacts in sync for you.

If you're using Gradle 7.0+ you can also use `hypo-catalog` if you like.

### Gradle Kotlin DSL

> <details open>
> <summary>Click to show build.gradle.kts</summary>
> 
> ```kotlin
> repositories {
>     mavenCentral()
> }
> 
> dependencies {
>     implementation(platform("dev.denwav.hypo:hypo-platform:1.2.6"))
>     // Whichever modules you need:
>     implementation("dev.denwav.hypo:hypo-model")
>     implementation("dev.denwav.hypo:hypo-core")
>     implementation("dev.denwav.hypo:hypo-hydrate")
>     implementation("dev.denwav.hypo:hypo-asm")
>     implementation("dev.denwav.hypo:hypo-asm-hydrate")
>     implementation("dev.denwav.hypo:hypo-mappings")
> }
> ```
> </details>

### Gradle Groovy DSL

> <details>
> <summary>Click to show build.gradle</summary>
> 
> ```groovy
> repositories {
>     mavenCentral()
> }
> 
> dependencies {
>     implementation platform('dev.denwav.hypo:hypo-platform:1.2.6')
>     // Whichever modules you need:
>     implementation 'dev.denwav.hypo:hypo-model'
>     implementation 'dev.denwav.hypo:hypo-core'
>     implementation 'dev.denwav.hypo:hypo-hydrate'
>     implementation 'dev.denwav.hypo:hypo-asm'
>     implementation 'dev.denwav.hypo:hypo-asm-hydrate'
>     implementation 'dev.denwav.hypo:hypo-mappings'
> }
> ```
> </details>

### Maven

> <details>
> <summary>Click to show pom.xml</summary>
> 
> ```xml
> <project>
>     <dependencyManagement>
>         <dependencies>
>             <dependency>
>                 <groupId>dev.denwav.hypo</groupId>
>                 <artifactId>hypo-platform</artifactId>
>                 <version>1.2.6</version>
>             </dependency>
>         </dependencies>
>     </dependencyManagement>
> 
>     <!-- Whichever modules you need -->
>     <dependencies>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-model</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-core</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-hydrate</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-asm</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-asm-hydrate</artifactId>
>         </dependency>
>         <dependency>
>             <groupId>dev.denwav.hypo</groupId>
>             <artifactId>hypo-mappings</artifactId>
>         </dependency>
>     </dependencies>
> </project>
> ```
> </details>

## Using Hypo

### The Hypo Model

[`hypo-model`](hypo-model) is Hypo's foundational module, all other modules depend on it. It contains the Java class
object model interfaces. The default implementation of `hypo-model` is [`hypo-asm`](hypo-asm), which uses the
[ASM](https://asm.ow2.io/) library for parsing Java class files. Theoretically a model implementation could even be
built around Java source files, as long as the model API is implemented, but currently there is no such implementation.

#### How to load data into the model

Define roots you want to use. The default implementation has roots for directories, jars, and a system root for JDK
classes, which come from the currently running JVM.

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.model.ClassProviderRoot;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> 
> public class Example {
>     public static void main(String[] args) {
>         Path dirPath = Paths.get("someDir");
>         Path jarPath = Paths.get("someJar");
> 
>         try (
>             ClassProviderRoot dirRoot = ClassProviderRoot.fromDir(dirPath);
>             ClassProviderRoot jarRoot = ClassProviderRoot.fromJar(jarPath);
>             ClassProviderRoot jdkRoot = ClassProviderRoot.ofJdk()
>         ) {
>             ...
>         }
>     }
> }
> ```
> </details>

Pass these roots to a `ClassDataProvider`, the default implementation is in `hypo-asm` called `AsmClassDataProvider`.

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.asm.AsmClassDataProvider;
> import dev.denwav.hypo.model.ClassDataProvider;
> import dev.denwav.hypo.model.ClassProviderRoot;
> 
> public class Example {
>     public static void main(String[] args) {
>         try (ClassDataProvider provider = AsmClassDataProvider.of(ClassProviderRoot.ofJdk())) {
>             ...
>         }
>     }
> }
> ```
> </details>

`hypo-core` defines a `HypoContext`. A context is an immutable config object which is passed around to most Hypo users,
which defines the "world" of Java class data which will be read.

Create a `HypoContext` using the providers you created:

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.asm.AsmClassDataProvider;
> import dev.denwav.hypo.core.HypoContext;
> import dev.denwav.hypo.model.ClassProviderRoot;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> 
> public class Example {
>     public static void main(String[] args) {
>         Path jarPath = Paths.get("someDir");
>         try (
>             HypoContext context = HypoContext.builder()
>                 .withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(jarPath)))
>                 .withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()))
>                 .build()
>         ) {
>             ...
>         }
>     }
> }
> ```
> </details>

Standard providers are the providers which make up the collection of classes you want to analyze. Context providers
fill out any additional class data on the classpath needed to complete the model.

For example, if you have a class called `CustomList` in your standard provider which implements `java.util.List` then
data about `CustomList`'s super class won't be available unless `ClassProviderRoot.ofJdk()` is given as a context
provider.

Only class data in the standard provider will be iterated over when filling out the model during hydration, running
hydration over your entire classpath could take much longer and use much more memory, so it's a good idea to separate
your standard and context providers.

Now that the providers are set up in a context, you can find `ClassData` objects for specific classes or loop over all
classes available. This data model is not fully complete yet however, as it has not been hydrated yet.

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.asm.AsmClassDataProvider;
> import dev.denwav.hypo.core.HypoContext;
> import dev.denwav.hypo.model.ClassProviderRoot;
> import java.nio.file.Path;
> import java.nio.file.Paths;
> 
> public class Example {
>     public static void main(String[] args) {
>         Path jarPath = Paths.get("someDir");
>         try (
>             HypoContext context = HypoContext.builder()
>                     .withProvider(AsmClassDataProvider.of(ClassProviderRoot.fromJar(jarPath)))
>                     .withContextProvider(AsmClassDataProvider.of(ClassProviderRoot.ofJdk()))
>                     .build()
>         ) {
>             ClassData exampleClassData = context.getProvider().findClass("com.example.ExampleClass");
>             for (ClassData classData : this.context.getProvider().allClasses()) {
>                 System.out.println(classData);
>             }
>         }
>     }
> }
> ```
> </details>

### Hydration

With class data in a `HypoContext`, that context can be passed to a `HydrationManager` to fill out the model with
additional information.

Some data is easy to get directly from Java class files, such as the class's super class, but other data is much more
difficult, or impossible to retrieve directly, such as every class which extend a given class. The only way to get that
data is to read every class on the classpath and check that class's super class value, and build the class hierarchy
directly. That is the core of what hydration does.

Create the default implementation of `HydrationManager` and hydrate your `HypoContext`.

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.core.HypoContext;
> import dev.denwav.hypo.hydrate.HydrationManager;
> 
> public class Example {
>     public static void main(String[] args) {
>         // HypoContext building omitted for brevity
>         try (HypoContext context = HypoContext.buidler().build()) {
>             HydrationManager.createDefault().hydrate(context);
>         }
>     }
> }
> ```
> </details>

The default hydrator fills in extra data in the `ClassData` and `MethodData` classes of the model allowing additional
hydration only methods to be called.

Additional arbitrary data may be included in the model through hydration through custom `HydrationProvider` classes.
These classes read `HypoData` objects (e.g. `ClassData`, `MethodData`, and `FieldData`) and add more information to
these objects using `HypoKey`s.

> <details>
> <summary>Click to show Java snippet</summary>
> 
> ```java
> import dev.denwav.hypo.asm.hydrate.BridgeMethodHydrator;
> import dev.denwav.hypo.core.HypoContext;
> import dev.denwav.hypo.hydrate.generic.HypoHydration;
> import dev.denwav.hypo.hydrate.HydrationManager;
> 
> public class Example {
>     public static void main(String[] args) {
>         // HypoContext building omitted for brevity
>         try (HypoContext context = HypoContext.buidler().build()) {
>             HydrationManager.createDefault()
>                     .register(BridgeMethodHydrator.create())
>                     .hydrate(context);
> 
>             // Get additional data out of the model provided by the BridgeMethodHydrator
>             MethodData syntheticTargetMethod = context.getProvider()
>                     .findClass("com.example.ExampleClass")
>                     .methods("someMethod")
>                     .get(0)
>                     .get(HypoHydration.SYNTHETIC_TARGET);
>         }
>     }
> }
> ```
> </details>

Custom hydration providers are usually going to be specific to a particular model implementation. This is because custom
hydration providers may need to access additional data outside the standard model to build their custom data. For
example, the providers in `hypo-asm-hydrate` use the ASM data nodes to learn what they need to know from the bytecode.

You can run multiple `ClassData` implementations and multiple `HydrationProvider` implementations in the same model at
the same time - the `HydrationManager` will only pass implementations which match what the provider targets to the
model.

## License

[Hypo is licensed under the LGPL version 3.0 only (no later versions).](license.txt)
