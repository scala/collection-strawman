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
repository is only used as an issue tracker.**

Thanks to Git, you can still look up the code before it was deleted from this repository by browsing
the [before-deletion](https://github.com/scala/collection-strawman/tree/before-deletion) branch.

## Current Status and Roadmap

The new collections will be included in the standard library of Scala 2.13.0-M4.

You can currently use a pre-release of 2.13.0-M4:

~~~ scala
scalaVersion := "2.13.0-M4-pre-20d3c21"
~~~

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

## Contributing

We welcome contributions!

For more information, see the [CONTRIBUTING](CONTRIBUTING.md) file.
