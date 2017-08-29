package strawman
package collection.immutable

import strawman.collection.mutable.{ArrayBuffer, Builder}
import strawman.collection.{IterableOnce, Iterator, SeqFactory, StrictOptimizedSeqFactory, View}

import scala.{Any, ArrayIndexOutOfBoundsException, Boolean, Int, Nothing, UnsupportedOperationException, throws, Array, AnyRef, `inline`, Serializable, Byte, Short, Long, Double, Unit, Float, Char}
import scala.util.hashing.MurmurHash3
import scala.runtime.ScalaRunTime
import scala.Predef.{???, intWrapper}
import java.util.Arrays

/**
  * An immutable array.
  *
  * Supports efficient indexed access and has a small memory footprint.
  *
  * @define coll immutable array
  * @define Coll `ImmutableArray`
  */
sealed abstract class ImmutableArray[+A]
  extends IndexedSeq[A]
    with IndexedSeqOps[A, ImmutableArray, ImmutableArray[A]]
    with StrictOptimizedSeqOps[A, ImmutableArray, ImmutableArray[A]] {

  def iterableFactory: SeqFactory[ImmutableArray] = ImmutableArray

  protected def elements: AnyRef

  protected[this] def fromSpecificIterable(coll: strawman.collection.Iterable[A]): ImmutableArray[A] = fromIterable(coll)

  protected[this] def newSpecificBuilder(): Builder[A, ImmutableArray[A]] = ImmutableArray.newBuilder[A]()

  override def knownSize: Int = length

  override def updated[B >: A](index: Int, elem: B): ImmutableArray[B] = {
    val dest = scala.Array.ofDim[Any](length)
    scala.Array.copy(elements, 0, dest, 0, length)
    dest(index) = elem
    ImmutableArray.wrapArray(dest)
  }

  override def map[B](f: A => B): ImmutableArray[B] = ImmutableArray.tabulate(length)(i => f(apply(i)))

  override def prepended[B >: A](elem: B): ImmutableArray[B] = {
    val dest = scala.Array.ofDim[Any](length + 1)
    dest(0) = elem
    scala.Array.copy(elements, 0, dest, 1, length)
    ImmutableArray.wrapArray(dest)
  }

  override def appended[B >: A](elem: B): ImmutableArray[B] = {
    val dest = scala.Array.ofDim[Any](length + 1)
    scala.Array.copy(elements, 0, dest, 0, length)
    dest(length) = elem
    ImmutableArray.wrapArray(dest)
  }

  override def appendedAll[B >: A](xs: collection.Iterable[B]): ImmutableArray[B] =
    xs match {
      case bs: ImmutableArray[B] =>
        val dest = scala.Array.ofDim[Any](length + bs.length)
        scala.Array.copy(elements, 0, dest, 0, length)
        scala.Array.copy(bs.elements, 0, dest, length, bs.length)
        ImmutableArray.wrapArray(dest)
      case _ =>
        fromIterable(View.Concat(toIterable, xs))
    }

  override def prependedAll[B >: A](xs: collection.Iterable[B]): ImmutableArray[B] =
    xs match {
      case bs: ImmutableArray[B] =>
        val dest = scala.Array.ofDim[Any](length + bs.length)
        java.lang.System.arraycopy(bs.elements, 0, dest, 0, bs.length)
        java.lang.System.arraycopy(elements, 0, dest, bs.length, length)
        ImmutableArray.wrapArray(dest)
      case _ =>
        fromIterable(View.Concat(xs, toIterable))
    }

  override def zip[B](that: collection.Iterable[B]): ImmutableArray[(A, B)] =
    that match {
      case bs: ImmutableArray[B] =>
        ImmutableArray.tabulate(length min bs.length) { i =>
          (apply(i), bs(i))
        }
      case _ =>
        fromIterable(View.Zip(toIterable, that))
    }

  override def partition(p: A => Boolean): (ImmutableArray[A], ImmutableArray[A]) = {
    val pn = View.Partition(toIterable, p)
    (fromIterable(pn.first), fromIterable(pn.second))
  }

  override def take(n: Int): ImmutableArray[A] = ImmutableArray.tabulate(n)(apply)

  override def takeRight(n: Int): ImmutableArray[A] = ImmutableArray.tabulate(n min length)(i => apply(length - (n min length) + i))

  override def drop(n: Int): ImmutableArray[A] = ImmutableArray.tabulate((length - n) max 0)(i => apply(n + i))

  override def dropRight(n: Int): ImmutableArray[A] = ImmutableArray.tabulate((length - n) max 0)(apply)

  override def tail: ImmutableArray[A] =
    if (length > 0) {
      val dest = scala.Array.ofDim[Any](length - 1)
      java.lang.System.arraycopy(elements, 1, dest, 0, length - 1)
      ImmutableArray.wrapArray(dest)
    } else throw new UnsupportedOperationException("tail of empty array")

  override def reverse: ImmutableArray[A] = ImmutableArray.tabulate(length)(i => apply(length - 1 - i))

}

/**
  * $factoryInfo
  * @define coll immutable array
  * @define Coll `ImmutableArray`
  */
object ImmutableArray extends StrictOptimizedSeqFactory[ImmutableArray] {

  private[this] lazy val emptyImpl = new ImmutableArray.ofRef[Nothing](new scala.Array[Nothing](0))

  def empty[A]: ImmutableArray[A] = emptyImpl

  def fromArrayBuffer[A](arr: ArrayBuffer[A]): ImmutableArray[A] =
    wrapArray[A](arr.asInstanceOf[ArrayBuffer[Any]].toArray)

  def from[A](it: strawman.collection.IterableOnce[A]): ImmutableArray[A] =
    if (it.knownSize > -1) {
      val n = it.knownSize
      val elements = scala.Array.ofDim[Any](n)
      val iterator = it.iterator()
      var i = 0
      while (i < n) {
        ScalaRunTime.array_update(elements, i, iterator.next())
        i = i + 1
      }
      wrapArray(elements)
    } else fromArrayBuffer(ArrayBuffer.from(it))

  def newBuilder[A](): Builder[A, ImmutableArray[A]] =
    ArrayBuffer.newBuilder[A]().mapResult(fromArrayBuffer)

  override def fill[A](n: Int)(elem: => A): ImmutableArray[A] = tabulate(n)(_ => elem)

  override def tabulate[A](n: Int)(f: Int => A): ImmutableArray[A] = {
    val elements = scala.Array.ofDim[Any](n)
    var i = 0
    while (i < n) {
      ScalaRunTime.array_update(elements, i, f(i))
      i = i + 1
    }
    ImmutableArray.wrapArray(elements)
  }

  def wrapArray[T](x: AnyRef): ImmutableArray[T] = (x match {
    case null              => null
    case x: Array[AnyRef]  => new ofRef[AnyRef](x)
    case x: Array[Int]     => new ofInt(x)
    case x: Array[Double]  => new ofDouble(x)
    case x: Array[Long]    => new ofLong(x)
    case x: Array[Float]   => new ofFloat(x)
    case x: Array[Char]    => new ofChar(x)
    case x: Array[Byte]    => new ofByte(x)
    case x: Array[Short]   => new ofShort(x)
    case x: Array[Boolean] => new ofBoolean(x)
    case x: Array[Unit]    => new ofUnit(x)
  }).asInstanceOf[ImmutableArray[T]]

  final class ofRef[T <: AnyRef](val array: Array[T]) extends ImmutableArray[T] with Serializable {
    protected def elements: Array[T] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): T = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofRef[_] => Arrays.equals(array.asInstanceOf[Array[AnyRef]], that.array.asInstanceOf[Array[AnyRef]])
      case _ => super.equals(that)
    }
  }

  final class ofByte(val array: Array[Byte]) extends ImmutableArray[Byte] with Serializable {
    protected def elements: Array[Byte] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Byte = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofByte => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofShort(val array: Array[Short]) extends ImmutableArray[Short] with Serializable {
    protected def elements: Array[Short] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Short = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofShort => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofChar(val array: Array[Char]) extends ImmutableArray[Char] with Serializable {
    protected def elements: Array[Char] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Char = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofChar => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofInt(val array: Array[Int]) extends ImmutableArray[Int] with Serializable {
    protected def elements: Array[Int] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Int = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofInt => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofLong(val array: Array[Long]) extends ImmutableArray[Long] with Serializable {
    protected def elements: Array[Long] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Long = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofLong => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofFloat(val array: Array[Float]) extends ImmutableArray[Float] with Serializable {
    protected def elements: Array[Float] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Float = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofFloat => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofDouble(val array: Array[Double]) extends ImmutableArray[Double] with Serializable {
    protected def elements: Array[Double] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Double = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofDouble => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofBoolean(val array: Array[Boolean]) extends ImmutableArray[Boolean] with Serializable {
    protected def elements: Array[Boolean] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Boolean = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofBoolean => Arrays.equals(array, that.array)
      case _ => super.equals(that)
    }
  }

  final class ofUnit(val array: Array[Unit]) extends ImmutableArray[Unit] with Serializable {
    protected def elements: Array[Unit] = array
    def length: Int = array.length
    @throws[ArrayIndexOutOfBoundsException]
    def apply(i: Int): Unit = elements(i)
    override def hashCode = MurmurHash3.arrayHash(array)
    override def equals(that: Any) = that match {
      case that: ofUnit => array.length == that.array.length
      case _ => super.equals(that)
    }
  }
}
