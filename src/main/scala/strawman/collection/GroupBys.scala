package strawman.collection

import scala.{None, Some}

object GroupBys {

  def groupByBuilder[A, K, C <: Iterable[A]](
    as: C
  )(
    newBuilder: () => mutable.Builder[A, C]
  )(
    f: A => K
  ): immutable.Map[K, C] = {
    val m = mutable.Map.empty[K, mutable.Builder[A, C]]
    for (elem <- as) {
      val key = f(elem)
      val bldr = m.getOrElseUpdate(key, newBuilder())
      bldr += elem
    }
    var result = immutable.Map.empty[K, C]
    m.foreach { case (k, v) =>
      result = result + ((k, v.result))
    }
    result
  }

  def groupByImmutable[A, K, C <: Iterable[A]](
    as: C
  )(
    empty: C, cons: (A, C) => C
  )(
    f: A => K
  ): immutable.Map[K, C] = {
    var result = immutable.Map.empty[K, C]
    for (elem <- as) {
      val key = f(elem)
      val values = cons(elem, result.getOrElse(key, empty))
      result = result + ((key, values))
    }
    result
  }

  def groupByGrowable[A, K, C <: Iterable[A] with mutable.Growable[A]](
    as: C
  )(
    empty: () => C
  )(
    f: A => K
  ): immutable.Map[K, C] = {
    var result = immutable.Map.empty[K, C]
    for (elem <- as) {
      val key = f(elem)
      result.get(key) match {
        case None =>
          val values = empty()
          values += elem
          result = result + ((key, values))
        case Some(values) =>
          values += elem
      }
    }
    result
  }

}
