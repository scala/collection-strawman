package strawman.collection

import scala.{Int, Long, Double, Any}
import scala.Predef.{classOf, String, Class}
import scala.reflect.ClassTag
import java.util.PrimitiveIterator

sealed trait Specialized[E] {
  type Iterator
}

object Specialized extends SpecializedLowPriority {
  // Using Java's PrimitiveIterator here for simplicity's sake
  implicit case object SpecializedInt extends Specialized[Int] { type Iterator = PrimitiveIterator.OfInt }
  implicit case object SpecializedLong extends Specialized[Long] { type Iterator = PrimitiveIterator.OfLong }
  implicit case object SpecializedDouble extends Specialized[Double] { type Iterator = PrimitiveIterator.OfDouble }
}

trait SpecializedLowPriority {
  private[this] val notSpecializedSingleton = new Specialized[Any] {}

  implicit def notSpecialized[E]: Specialized[E] { type Iterator = strawman.collection.mutable.Iterator[E] } =
    notSpecializedSingleton.asInstanceOf[Specialized[E] { type Iterator = strawman.collection.mutable.Iterator[E] }]
}

private[collection] object SpecializationUtil {
  // Function1[@specialized(scala.Int, scala.Long, scala.Float, scala.Double) -T1, @specialized(scala.Unit, scala.Boolean, scala.Int, scala.Float, scala.Long, scala.Double) +R]
  private val function1 = classOf[_ => _]
  private def getF1(code: String): Class[_ <: (_ => _)] =
    function1.getClassLoader.loadClass("scala.Function1$mc"+code+"$sp").asInstanceOf[Class[_ <: (_ => _)]]
  private val function1DD = getF1("DD")
  private val function1DF = getF1("DF")
  private val function1DI = getF1("DI")
  private val function1DJ = getF1("DJ")
  private val function1FD = getF1("FD")
  private val function1FF = getF1("FF")
  private val function1FI = getF1("FI")
  private val function1FJ = getF1("FJ")
  private val function1ID = getF1("ID")
  private val function1IF = getF1("IF")
  private val function1II = getF1("II")
  private val function1IJ = getF1("IJ")
  private val function1JD = getF1("JD")
  private val function1JF = getF1("JF")
  private val function1JI = getF1("JI")
  private val function1JJ = getF1("JJ")
  private val function1VD = getF1("VD")
  private val function1VF = getF1("VF")
  private val function1VI = getF1("VI")
  private val function1VJ = getF1("VJ")
  private val function1ZD = getF1("ZD")
  private val function1ZF = getF1("ZF")
  private val function1ZI = getF1("ZI")
  private val function1ZJ = getF1("ZJ")

  /** Return ClassTag.(Int, Double, Long, Any) depending on the specialized return type of the function */
  def getSpecializedReturnType[T1, R](f: T1 => R): ClassTag[_] = {
    val cl = f.getClass
    // There is no common abstraction and anon function classes to not use specialized class names,
    // so we have to check all classes individually
    if(function1ID.isAssignableFrom(cl) ||
      function1IF.isAssignableFrom(cl) ||
      function1II.isAssignableFrom(cl) ||
      function1IJ.isAssignableFrom(cl)) ClassTag.Int
    else if(function1JD.isAssignableFrom(cl) ||
      function1JF.isAssignableFrom(cl) ||
      function1JI.isAssignableFrom(cl) ||
      function1JJ.isAssignableFrom(cl)) ClassTag.Long
    else if(function1DD.isAssignableFrom(cl) ||
      function1DF.isAssignableFrom(cl) ||
      function1DI.isAssignableFrom(cl) ||
      function1DJ.isAssignableFrom(cl)) ClassTag.Double
    else ClassTag.Any
  }
}
