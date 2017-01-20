package strawman.collection.mutable

import scala.{AnyRef, Array, Boolean, Double, Int, Long, Unit, specialized}
import scala.Predef.{classOf, intWrapper, ???}
import scala.annotation.unchecked.uncheckedVariance
import scala.reflect.ClassTag
import strawman.collection.{ArrayView, IndexedView, Iterable, IterableFactory, IterableOnce, Seq, SeqLike, SpecializationUtil, Specialized}

/** Concrete collection type: ArrayBuffer */
class ArrayBuffer[@specialized(Int, Long, Double) A] private[collection] (initElems: Array[A], initLength: Int)
  extends Seq[A]
    with SeqLike[A, ArrayBuffer]
    with Buildable[A, ArrayBuffer[A]]
    with Builder[A, ArrayBuffer[A]] {

  private[collection] var elems: Array[A] = initElems
  private var start = 0
  private var end = initLength

  def apply(n: Int) = elems(start + n)

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

  /* overridden for specialization */
  override def map[B](f: A => B): ArrayBuffer[B] = {
    val length = this.length
    val elems = this.elems
    val start = this.start
    var i = 0
    // An alternative to using getSpecializedReturnType would be to map the first element unspecialized,
    // use the runtime class of the resulting object for specialization, continue on the specialized path
    // catching a possible ClassCastException to unspecialize later (by copying the already mapped values)
    // if required. This would avoid the overhead of getSpecializedReturnType but it would also generate
    // specialized collections from polymorphic mapping functions that happen to produce boxed primitive
    // values.
    val a = SpecializationUtil.getSpecializedReturnType(f).runtimeClass match {
      case java.lang.Integer.TYPE =>
        val ff = f.asInstanceOf[A => Int]
        val a = new Array[Int](length)
        while(i < length) {
          a(i) = ff(elems(start + i))
          i += 1
        }
        a
      case java.lang.Long.TYPE =>
        val ff = f.asInstanceOf[A => Long]
        val a = new Array[Long](length)
        while(i < length) {
          a(i) = ff(elems(start + i))
          i += 1
        }
        a
      case java.lang.Double.TYPE =>
        val ff = f.asInstanceOf[A => Double]
        val a = new Array[Double](length)
        while(i < length) {
          a(i) = ff(elems(start + i))
          i += 1
        }
        a
      case _ =>
        val ff = f.asInstanceOf[A => AnyRef]
        val a = new Array[AnyRef](length)
        while(i < length) {
          // Note: Function1 is not specialized for AnyRef, so even if A is a primitive type
          // we cannot call ff without boxing the argument.
          a(i) = ff(elems(start + i))
          i += 1
        }
        a
    }
    new ArrayBuffer[B](a.asInstanceOf[Array[B]], length)
  }

  /* overridden for specialization */
  override def flatMap[B](f: A => IterableOnce[B]): ArrayBuffer[B] = {
    val length = this.length
    val elems = this.elems
    val start = this.start
    var buf = new Array[IterableOnce[B]](length)
    var i = 0
    var definiteSize = 0
    var hasNonIterable, unspecialized = false
    var bSpec: Specialized[B] = null
    if(length == 0) ArrayBuffer.empty[B]
    else {
      while(i < length) {
        // Note: Function1 is not specialized for AnyRef, so even if A is a primitive type
        // we cannot call f without boxing the argument.
        var it = f(elems(start + i))
        buf(i) = it
        it match {
          case it: Iterable[_] =>
            if(definiteSize != -1) {
              val s = it.knownSize
              if(s == -1) definiteSize = -1
              else definiteSize += s
            }
            if(!unspecialized) {
              val spec = Specialized.forClassTag(it.elementClassTag)
              if(bSpec eq null) bSpec = spec.asInstanceOf[Specialized[B]]
              else if(bSpec != spec) unspecialized = true
            }
          case _ =>
            hasNonIterable = true
            unspecialized = true
            definiteSize = -1
        }
        i += 1
      }
      val sz = if(definiteSize != -1) definiteSize else scala.math.max(16, length)
      i = 0
      if(unspecialized || (bSpec eq null)) {
        val a = new ArrayBuffer[AnyRef](new Array[AnyRef](sz), sz)
        while(i < buf.length) {
          val it = buf(i).iterator()
          while(it.hasNext) a += it.next().asInstanceOf[AnyRef]
          i += 1
        }
        a
      } else bSpec match {
        case bSpec @ Specialized.SpecializedInt =>
          val a = new ArrayBuffer[Int](new Array[Int](sz), sz)
          while(i < buf.length) {
            val it = buf(i).asInstanceOf[Iterable[B]].specializedIterator(bSpec).asInstanceOf[java.util.PrimitiveIterator.OfInt]
            while(it.hasNext) a += it.nextInt()
            i += 1
          }
          a
        case bSpec @ Specialized.SpecializedLong =>
          val a = new ArrayBuffer[Long](new Array[Long](sz), sz)
          while(i < buf.length) {
            val it = buf(i).asInstanceOf[Iterable[B]].specializedIterator(bSpec).asInstanceOf[java.util.PrimitiveIterator.OfLong]
            while(it.hasNext) a += it.nextLong()
            i += 1
          }
          a
        case bSpec @ Specialized.SpecializedDouble =>
          val a = new ArrayBuffer[Double](new Array[Double](sz), sz)
          while(i < buf.length) {
            val it = buf(i).asInstanceOf[Iterable[B]].specializedIterator(bSpec).asInstanceOf[java.util.PrimitiveIterator.OfDouble]
            while(it.hasNext) a += it.nextDouble()
            i += 1
          }
          a
        case _ =>
          ??? // should not happen
      }
    }.asInstanceOf[ArrayBuffer[B]]
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
