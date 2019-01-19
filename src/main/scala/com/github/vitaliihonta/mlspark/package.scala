package com.github.vitaliihonta
import scala.util.Try

package object mlspark {
  def withPrettySep[U](title: String)(thunk: => U): U = {
    def sep = println("=" * 100)

    println(title + ":")
    sep
    val x = thunk
    sep
    x
  }

  def using[A <: AutoCloseable, B](acquire: => A)(use: A => B): Try[B] = {
    val a      = acquire
    val result = Try(use(a))
    a.close()
    result
  }
}
