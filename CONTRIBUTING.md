# Contributing

Want to be part of this project but don’t know what you can do to help? You should have a look at the
[low hanging fruit](https://github.com/scala/collection-strawman/issues?q=is%3Aissue+is%3Aopen+label%3A%22low+hanging+fruit%22)
issues!

## Where is the code?

When we talk about “the new collections” we could be referring to the following projects:

- the collections themselves (hosted in the [scala/scala](https://github.com/scala/scala) repository),
- the compatibility library (hosted in the [scala/scala-collection-compat](https://github.com/scala/scala-collection-compat)
  repository), which provide the new APIs to the old collections, so that code that takes
  advantage of the new collections can still cross-compile with 2.12 and older Scala version,
- the scalafix rewrite rules (hosted in the [scala/scala-collection-compat](https://github.com/scala/scala-collection-compat)
  repository), which adapt code that were using the old collections to the new collections,
- the contrib library (hosted in the [scala/scala-collection-contrib](https://github.com/scala/scala-collection-contrib)
  repository), which is an incubator for new operations and new collections.

Please refer to the contributing guidelines of the hosting repository of the project you want to contribute to.
