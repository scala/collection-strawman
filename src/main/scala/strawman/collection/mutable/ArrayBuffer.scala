package strawman.collection.mutable

import scala.{AnyRef, Array, Boolean, Double, Int, Long, Unit, specialized}
import scala.Predef.{classOf, intWrapper}
import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.ClassTag
import strawman.collection.{ArrayView, IndexedView, Iterable, IterableFactory, IterableOnce, Seq, SeqLike, Specialized}

/** Concrete collection type: ArrayBuffer */
class ArrayBuffer[@specialized(Int, Long, Double) A] private[collection] (initElems: Array[A], initLength: Int)
  extends Seq[A]
    with SeqLike[A, ArrayBuffer]
    with Buildable[A, ArrayBuffer[A]]
    with Builder[A, ArrayBuffer[A]] {

  private[collection] var elems: Array[A] = initElems
  private var start = 0
  private var end = initLength

  def apply(n: Int) = elems(start + n).asInstanceOf[A]

  def elementClassTag: ClassTag[A] = ClassTag(elems.getClass.getComponentType)

  def length = end - start
  override def knownSize = length

  override def view = new ArrayView(elems, start, end)

  def iterator() = view.iterator()

  override def specializedIterator(implicit spec: Specialized[A @uncheckedVariance]): spec.Iterator =
    view.specializedIterator

  def fromIterable[B](it: Iterable[B]): ArrayBuffer[B] =
    ArrayBuffer.fromIterable(it)

  protected[this] def newBuilder = ArrayBuffer[A]()(elementClassTag)

  def +=(elem: A): this.type = {
    if (end == elems.length) {
      if (start > 0) {
        Array.copy(elems, start, elems, 0, length)
        end -= start
        start = 0
      }
      else {
        val newelems = Array.ofDim[A](end * 2)(elementClassTag)
        Array.copy(elems, 0, newelems, 0, end)
        elems = newelems
      }
    }
    elems(end) = elem
    end += 1
    this
  }

  def result = this

  /** New operation: destructively drop elements at start of buffer. */
  def trimStart(n: Int): Unit = start += (n max 0)

  /** Overridden to use array copying for efficiency where possible. */
  override def ++[B >: A](xs: IterableOnce[B]): ArrayBuffer[B] = xs match {
    case xs: ArrayBuffer[B] =>
      val elems = Array.ofDim[B](length + xs.length)(xs.elementClassTag)
      Array.copy(this.elems, this.start, elems, 0, this.length)
      Array.copy(xs.elems, xs.start, elems, this.length, xs.length)
      new ArrayBuffer[B](elems, elems.length)
    case _ => super.++(xs)
  }

  override def take(n: Int) = {
    val elems = Array.ofDim[A](n min length)(elementClassTag)
    Array.copy(this.elems, this.start, elems, 0, elems.length)
    new ArrayBuffer[A](elems, elems.length)
  }

  override def className = "ArrayBuffer"
}

object ArrayBuffer extends IterableFactory[ArrayBuffer] {

  /** Avoid reallocation of buffer if length is known. */
  def fromIterable[B](coll: Iterable[B]): ArrayBuffer[B] = {
    if (coll.knownSize >= 0) {
      val elems = Array.ofDim[B](coll.knownSize)(coll.elementClassTag.asInstanceOf[ClassTag[B]]) // could use optimisitic respecialization here
      val it = coll.iterator()
      for (i <- 0 until elems.length) elems(i) = it.next()
      ArrayBuffer[B](elems, elems.length)
    }
    else ArrayBuffer[B]()(coll.elementClassTag.asInstanceOf[ClassTag[B]]) ++= coll
  }

  private def apply[@specialized(Int, Long, Double) A](initElems: Array[A], initLength: Int): ArrayBuffer[A] = {
    val cl = initElems.getClass.getComponentType
    if (cl == classOf[Int]) new ArrayBuffer[Int](initElems.asInstanceOf[Array[Int]], initLength).asInstanceOf[ArrayBuffer[A]]
    else if (cl == classOf[Long]) new ArrayBuffer[Long](initElems.asInstanceOf[Array[Long]], initLength).asInstanceOf[ArrayBuffer[A]]
    else if (cl == classOf[Double]) new ArrayBuffer[Double](initElems.asInstanceOf[Array[Double]], initLength).asInstanceOf[ArrayBuffer[A]]
    else new ArrayBuffer(initElems, initLength)
  }

  // This is specialized but `empty` is not
  def apply[@specialized(Int, Long, Double) A]()(implicit ct: ClassTag[A]): ArrayBuffer[A] = apply(Array.ofDim(16), 0)
}
