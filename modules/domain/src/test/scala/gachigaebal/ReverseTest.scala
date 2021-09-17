package gachigaebal

import minitest.SimpleTestSuite
import hedgehog.minitest.HedgehogSupport
import hedgehog.*

object PropertyTest extends SimpleTestSuite with HedgehogSupport:

  property("reverse alphabetic strings") {
    for {
      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
    } yield xs.reverse.reverse ==== xs
  }
  example("reverse hello") {
    "hello".reverse ==== "olleh"
  }
