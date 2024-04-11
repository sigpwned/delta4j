# delta4j

delta4j is a lightweight Java library designed to support the development of concurrent and
distributed data processing applications. Focusing on performance and simplicity, delta4j includes a
range of software elements such as Bloom Filters, optimized text processing structures, and various
statistical distribution implementations. It is crafted to for minimal dependencies -- just SLF4J --
to make integrating it into existing projects as simple as possible.

## Features

* Simple: A straightforward API for easy integration and usage.
* Modern: Built with Java 11 and later versions in mind.
* Lightweight: A small library with little overhead and minimal dependencies (just SLF4J).
* Probabilistic Data Structures: High-cardinality probabilistic data structures designed for
  processing larger-than-memory datasets
* Statistical Distributions: Implementations of common statistical distributions with Sketch support
  and tight integrations with Java functional programming constructs, e.g., Stream, Supplier, etc.
* Optimized Text Processing: Handcrafted mutable, immutable, and unmodifiable text processing
  structures for efficient text manipulation.
* Concurrency and Distribution: Concurrency is a primary concern, with all data structures
  supporting divide-and-conquer fitting for vertical scaling and optional serialization for
  horizontal scaling

## Modules

* core: The core module contains the core functionality of delta4j, including probabilistic data
  structures, statistical distributions, and text processing utilities.
* jackson: The jackson module provides support for serializing and deserializing delta4j data
  structures using the Jackson JSON library.

## Quick Start

This section provides a brief introduction to getting delta4j set up in your project.

### BloomFilter

Creating a bloom filter is easy! Simply provide the number of elements you expect to add to it, and
the desired probability of false positives:

    BloomFilter<String> bloomFilter=BloomFilter.of(1000, 0.001);
    bloomFilter.add("hello");
    if(bloomFilter.mightContain("hello")) {
        // Always executes
    }
    if(bloomFilter.mightContain("world")) {
        // 99.9% chance this does not run
    }

If you'd like to create a new BloomFilter and fit it to a large dataset, you can use streams to
perform this operation either sequentially or in parallel. For example, the below code creates new
BloomFilter from the lines of a (potentially very large) file.

    long count;
    try (Stream<String> lines=Files.lines(path)) {
        count = lines.count();
    }
    BloomFilter<String> bloomFilter;
    try (Stream<String> lines=Files.lines(path)) {
        bloomFilter = lines.collect(BloomFilter.toBloomFilter(count, 0.001));
    }

### Distributions

delta4j provides several commonly-used distributions along with methods to create them in parallel
or distribution fashion. Each distribution will support the following features:

* `sample(Random)`: Returns a single sample from the distribution. The result type differs from one
  distribution type to another, but is always categorical (parameteric), continuous (`double`), or
  discrete (`long`).
* `Sketch`: A sketch is a lightweight, mutable, and serializable object that can be used to
  fit a distribution incrementally in a divide-and-conquer fashion. Each distribution type (e.g.,
  `FancyDistribution`) has an inner `Sketch` type (e.g., `FancyDistribution.Sketch`).
* `fit(Stream)`: Fits a distribution to a stream of data. This method is designed to be used in
  parallel or distributed fashion, depending on the stream provided.
* `toXxxDistribution()`: Returns a `Collector` that can be used to fit a distribution to a stream of
  data. This method is designed to be used in parallel or distributed fashion, depending on the
  stream provided. Only defined for categorical distributions because the standard library does not
  define corresponding `Collector` types for `DoubleStream` and `LongStream`.
* `of()`: Returns a distribution with the given parameters. When creating a distribution directly
  from parameters, this method is preferred (as opposed to using an explicit constructor) because
  some distributions may have special or common instances that are precomputed.

For example, here is how to use the `GaussianDistribution` class:

    GaussianDistribution d=GaussianDistribution.of(0.0, 1.0);
    double sample=gaussianDistribution.sample(ThreadLocalRandom.current());

### Text Views

The `StringView` class is a lightweight, mutable, and serializable subclass of `CharSequence` that
can be used to create substring views of a larger string without incurring the cost of copying the
underlying character array. Both mutable (`MutableStringView`) and immutable (`ImmutableStringView`)
version are provided. For example, this code uses `StringView` to create a map of all the unique
one- to four-letter substrings in a stream of strings:

    // Compute the frequency of all one- to four-letter substrings in a file
    Map<CharSequence,Long> bpes=Files.lines(path).flatMap(line -> 
      IntStream.rangeClosed(1, 4)
        .filter(len -> len < line.length())
        .mapToObj(len -> 
          IntStream.range(0, line.length() - len)
            .mapToObj(i -> StringView.mutableOf(line, i, i+len))
            .flatMap(Function.identity())))
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

The `CharArrayView` class is a lightweight, mutable, and serializable subclass of `CharSequence`
that can be used to create substring views of a larger character array without incurring the cost of
copying the underlying character array. Both mutable (`MutableCharArrayView`) and immutable
(`ImmutableCharArrayView`) version are provided. A similar example applies to `CharArrayView`.

### Jackson Serialization

The `delta4j-jackson` module provides support for serializing and deserializing delta4j data
structures using the Jackson JSON library. To use this module, add it to your POM file and register
the module with your `ObjectMapper` instance:

    ObjectMapper mapper=new ObjectMapper();
    mapper.registerModule(new Delta4jModule());

## Installation

To use delta4j in your project, add the following dependency to your `pom.xml` for Maven:

```xml

<dependency>
  <groupId>com.sigpwned</groupId>
  <artifactId>delta4j</artifactId>
  <version>0.0.0-b0</version>
</dependency>
```

## Contributing

We welcome contributions! If you would like to help make delta4j better, please follow our
contributing guidelines. You can submit bug reports, feature requests, and pull requests through our
GitHub issue tracker.

## License

delta4j is open-source software licensed under the Apache License, Version 2.0. See the LICENSE file
for more details

## Colophon

This library was originally designed as a set of `Collector` implementations to assist in parallel
data processing using Java 8 streams. A delta always appears at the end of a large stream. Also, a "
delta" is a small, incremental update to an existing dataset, and this library is designed not only
to represent them efficiently, but also to process them at massive scale.