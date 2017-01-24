package strawman.collection

import strawman.collection.immutable.{LazyList, List}

import scala.{Unit, Array, Any, AnyRef, App, Int, Long}
import scala.Predef.{ArrowAssoc, String, println}

import org.openjdk.jol.info.GraphLayout
import strawman.collection.mutable.{ArrayBuffer, ListBuffer}

object MemoryFootprint {

  val sizes = scala.List(8, 64/*, 512, 4096, 32768, 262144, 2097152*/)

  val obj: AnyRef = null

  def benchmark[A <: AnyRef](gen: Int => A): scala.List[Long] =
    for (size <- sizes) yield {
      GraphLayout.parseInstance(gen(size)).totalSize()
    }

  val memories =
    scala.Predef.Map(
      "scala.List"  -> benchmark(scala.List.fill(_)(obj)),
      "List"        -> benchmark(List.fill(_)(obj)),
      "LazyList"    -> benchmark(LazyList.fill(_)(obj)),
      "ArrayBuffer" -> benchmark(ArrayBuffer.fill(_)(obj)),
      "ListBuffer"  -> benchmark(ListBuffer.fill(_)(obj))
    )
  def main(args: Array[String]): Unit = {
    // Print the results as a CSV document
    println("Collection;" + sizes.mkString(";"))
    for ((name, values) <- memories) {
      println(name + ";" + values.mkString(";"))
    }
  }
}
