package strawman.collection.immutable

import scala.annotation.unchecked.uncheckedVariance
import scala.{Option, None, NoSuchElementException, Nothing, Some, UnsupportedOperationException}
import scala.Predef.???
import strawman.collection.{InhabitedLinearSeqFactory, InhabitedLinearSeqOps, InhabitedSeq, Iterable, IterableFactory, IterableOnce, LinearSeq, LinearSeqLike, View}
import strawman.collection.mutable.{Buildable, ListBuffer}


/** Concrete collection type: List */
sealed trait List[+A]
  extends LinearSeq[A]
    with LinearSeqLike[A, List]
    with Buildable[A, List[A]] {

  def fromIterable[B](c: Iterable[B]): List[B] = List.fromIterable(c)

  protected[this] def newBuilder = new ListBuffer[A].mapResult(_.toList)

  /** Prepend element */
  def :: [B >: A](elem: B): List.NonEmpty[B] =  new ::(elem, this)

  /** Prepend operation that avoids copying this list */
  def ++:[B >: A](prefix: List[B]): List[B] =
    if (prefix.isEmpty) this
    else prefix.unsafeHead :: (prefix.unsafeTail ++: this)

  /** When concatenating with another list `xs`, avoid copying `xs` */
  override def ++[B >: A](xs: IterableOnce[B]): List[B] = xs match {
    case xs: List[B] => this ++: xs
    case _ => super.++(xs)
  }

  override def className = "List"
}

case class :: [+A](x: A, private[collection] var next: List[A @uncheckedVariance]) // sound because `next` is used only locally
  extends List[A]
    with InhabitedSeq[A]
    with InhabitedLinearSeqOps[A, List[A]] {

  override def isEmpty = false
  def head = x
  def tail = next

  def uncons: Option[(A, List[A])] = Some((x, next))
  override protected def unsafeHead: A = x
  override protected def unsafeTail: List[A] = next
}

case object Nil extends List[Nothing] {
  override def isEmpty = true

  def uncons = None
  override protected def unsafeHead: Nothing = throw new NoSuchElementException
  override protected def unsafeTail: Nothing = throw new UnsupportedOperationException
}

object List extends IterableFactory[List] with InhabitedLinearSeqFactory[::] {

  type NonEmpty[A] = ::[A]

  def fromIterable[B](coll: Iterable[B]): List[B] = coll match {
    case coll: List[B] => coll
    case _ => ListBuffer.fromIterable(coll).toList
  }

  def apply[A](a: A, as: A*): List.NonEmpty[A] =
    a :: fromIterable(View.Elems(as: _*))

}