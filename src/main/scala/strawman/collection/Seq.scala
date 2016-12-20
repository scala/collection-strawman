package strawman.collection

import scala.{Any, Boolean, Int, IndexOutOfBoundsException, Option, Some, None}
import strawman.collection.mutable.Iterator
import strawman.collection.immutable.{List, Nil}

import scala.annotation.unchecked.uncheckedVariance

/** Base trait for sequence collections */
trait Seq[+A] extends Iterable[A] with SeqLike[A, Seq] with ArrayLike[A]

trait InhabitedSeq[+A]
  extends Seq[A]
    with InhabitedLinearSeqOps[A, Seq[A]]

/** Base trait for linearly accessed sequences that have efficient `head` and
  *  `tail` operations.
  *  Known subclasses: List, LazyList
  */
trait LinearSeq[+A] extends Seq[A] with LinearSeqLike[A, LinearSeq] { self =>

  /** `iterator` is overridden in terms of `head` and `tail` */
  def iterator() = new Iterator[A] {
    private[this] var current: LinearSeq[A] = self
    def hasNext = !current.isEmpty
    def next() = {
      val Some((head, tail)) = current.uncons
      current = tail
      head
    }
  }

  /** `length` is defined in terms of `iterator` */
  def length: Int = iterator().length

  /** `apply` is defined in terms of `drop`, which is in turn defined in
    *  terms of `tail`.
    */
  override def apply(n: Int): A = {
    if (n < 0) throw new IndexOutOfBoundsException(n.toString)
    val skipped = drop(n)
    skipped.uncons.fold(throw new IndexOutOfBoundsException(n.toString))(_._1)
  }
}

/**
  * Collections that have at least one `head` element followed
  * by a `tail`.
  */
trait InhabitedLinearSeqOps[+A, +Repr] extends Any {

  /** The first element of the collection. */
  def head: A

  /** The rest of the collection without its first element. */
  def tail: Repr
}

trait IndexedSeq[+A] extends Seq[A] { self =>
  override def view: IndexedView[A] = new IndexedView[A] {
    def length: Int = self.length
    def apply(i: Int): A = self(i)
  }
}


/** Base trait for Seq operations */
trait SeqLike[+A, +C[X] <: Seq[X]]
  extends IterableLike[A, C]
    with SeqMonoTransforms[A, C[A @uncheckedVariance]] // sound bcs of VarianceNote

trait InhabitedLinearSeqFactory[+C[X] <: InhabitedSeq[X]] {
  def apply[A](a: A, as: A*): C[A]
}

/** Base trait for linear Seq operations */
trait LinearSeqLike[+A, +C[+X] <: LinearSeq[X]] extends SeqLike[A, C] {

  protected def coll: C[A]

  /** Extract the head and tail, if the collection is not empty */
  def uncons: Option[(A, C[A])]

  /** Optimized version of `drop` that avoids copying
    *  Note: `drop` is defined here, rather than in a trait like `LinearSeqMonoTransforms`,
    *  because the `...MonoTransforms` traits make no assumption about the type of `Repr`
    *  whereas we need to assume here that `Repr` is the same as the underlying
    *  collection type.
    */
  override def drop(n: Int): C[A] = {
    def loop(n: Int, s: C[A]): C[A] = {
      // implicit contract to guarantee success of asInstanceOf:
      //   (1) coll is of type C[A]
      //   (2) The tail of a LinearSeq is of the same type as the type of the sequence itself
      // it's surprisingly tricky/ugly to turn this into actual types, so we
      // leave this contract implicit.
      if (n <= 0) s else {
        s.uncons match {
          case None => s
          case Some((_, t)) => loop(n - 1, t.asInstanceOf[C[A]])
        }
      }
    }
    loop(n, coll)
  }
}

/** Type-preserving transforms over sequences. */
trait SeqMonoTransforms[+A, +Repr] extends Any with IterableMonoTransforms[A, Repr] {
  def reverse: Repr = coll.view match {
    case v: IndexedView[A] => fromIterableWithSameElemType(v.reverse)
    case _ =>
      var xs: List[A] = Nil
      val it = coll.iterator()
      while (it.hasNext) xs = it.next() :: xs
      fromIterableWithSameElemType(xs)
  }
}

/** A trait representing indexable collections with finite length */
trait ArrayLike[+A] extends Any {
  def length: Int
  def apply(i: Int): A
}
