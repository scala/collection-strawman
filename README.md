# Collection-Strawman

[![Join the chat at https://gitter.im/scala/collection-strawman](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scala/collection-strawman?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Waffle.io board](https://badge.waffle.io/scala/collection-strawman.svg?label=ready&title=Ready+issues)](http://waffle.io/scala/collection-strawman)

Implementation of the new collections of Scala 2.13.

- [Gitter Discussion](https://gitter.im/scala/collection-strawman)
- [Dotty Issue](https://github.com/lampepfl/dotty/issues/818)
- [Scala Center Proposal](https://github.com/scalacenter/advisoryboard/blob/master/proposals/007-collections.md)

**The implementation of the collections has been merged to the
[scala/scala](https://github.com/scala/scala) repository. Contributions should now target the
[scala/scala](https://github.com/scala/scala) repository (branch `2.13.x`). The current scala/collection-strawman
repository is still used as an issue tracker. It also contains the Scalafix migration rules and the `collections-contrib`
module.**

## Current Status and Roadmap

The new collections will be included in the standard library of Scala 2.13.0-M4.

You can currently use a pre-release of 2.13.0-M4:

~~~ scala
scalaVersion := "2.13.0-M4-pre-20d3c21"
libraryDependencies += "ch.epfl.scala" %% "collections-contrib" % "0.10.0-SNAPSHOT" // optional
~~~

The `collections-contrib` artifact provides additional operations on the collections (see the
[Additional operations](#additional-operations) section).

## Use the last library release in your project

Before 2.13.0-M4 is released you can use the previous releases of the collections (published as an external library).
The collections live in the `strawman.collection` namespace (instead of `scala.collection`).

Add the following dependencies to your project:

~~~ scala
libraryDependencies += "ch.epfl.scala" %% "collection-strawman" % "0.9.0"
libraryDependencies += "ch.epfl.scala" %% "collections-contrib" % "0.9.0" // optional
~~~

The 0.9.0 version is compatible with Scala 2.13-M2 and Dotty 0.6. Scala 2.12 is also supported
but you might encounter type inference issues with it.

### API Documentation

- [`collection-strawman`](https://static.javadoc.io/ch.epfl.scala/collection-strawman_2.12/0.9.0/index.html)
- [`collections-contrib`](https://static.javadoc.io/ch.epfl.scala/collections-contrib_2.12/0.9.0/index.html)

### Migrating from the standard collections to the strawman

There is an [entry in the FAQ](https://github.com/scala/collection-strawman/wiki/FAQ#what-are-the-breaking-changes)
that aims to list all the breaking changes.

A tool is being developed to automatically migrate code that uses the standard
collection to use the strawman.

To use it, add the [scalafix](https://scalacenter.github.io/scalafix/) sbt plugin
to your build, as explained in
[its documentation](https://scalacenter.github.io/scalafix/#Installation).

Two situations are supported: (1) migrating a 2.12 code base to a 2.12 code base that
uses the collection strawman as a library (instead of the standard collections), and
(2) migrating a 2.12 code base to 2.13 code base.

The migration tool is not exhaustive and we will continue to improve
it over time. If you encounter a use case that’s not supported, please
report it as described in the
[contributing documentation](CONTRIBUTING.md#migration-tool).

#### Migrating a 2.12 code base to a 2.12 code base that uses the collection strawman as a library

Run the following sbt task on your project:

~~~
> scalafix https://github.com/scala/collection-strawman/raw/master/scalafix/2.12/rules/src/main/scala/fix/Collectionstrawman_v0.scala
~~~

In essence, the migration tool changes the imports in your source code
so that the strawman definitions are imported. It also rewrites
expressions that use an API that is different in the strawman.

#### Migrating a 2.12 code base to 2.13 code base

Run the following sbt task on your project:

~~~
> scalafix https://github.com/scala/collection-strawman/raw/master/scalafix/2.13/rules/src/main/scala/fix/Collectionstrawman_v0.scala
~~~

## Contributing

We welcome contributions!

For more information, see the [CONTRIBUTING](CONTRIBUTING.md) file.
