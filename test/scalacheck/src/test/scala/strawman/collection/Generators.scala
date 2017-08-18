package strawman.collection

import scala.collection.immutable.{List => ScalaList}
import strawman.collection.immutable.List

import org.scalacheck.Arbitrary

object Generators {

  implicit def arbitraryList[A](implicit arbitraryOldList: Arbitrary[ScalaList[A]]): Arbitrary[List[A]] =
    Arbitrary(arbitraryOldList.arbitrary.map(scalaList => List.fromIterable(scalaList.toStrawman)))

}
