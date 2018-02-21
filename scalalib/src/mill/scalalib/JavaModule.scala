package mill
package scalalib

import ammonite.ops._
import coursier.{Cache, MavenRepository, Repository}
import mill.define.{Cross, Task}
import mill.define.TaskModule
import mill.eval.{PathRef, Result}
import mill.modules.Jvm
import mill.modules.Jvm.{createAssembly, createJar, interactiveSubprocess, runLocal, subprocess}
import Lib._
import mill.define.Cross.Resolver
import mill.util.Loose.Agg
import mill.util.Strict

trait JavaModule extends mill.Module with TaskModule { outer =>
  def defaultCommandName(): String = "run"

  def mainClass: T[Option[String]] = None

  def ivyDeps = T{ Agg.empty[Dep] }
  def compileIvyDeps = T{ Agg.empty[Dep] }
  def runIvyDeps = T{ Agg.empty[Dep] }

  def javacOps = T{ Seq.empty[String] }
  def moduleDeps = Seq.empty[JavaModule]

  def unmanagedClasspath = T{ Agg.empty[PathRef] }

  def transitiveIvyDeps: T[Agg[Dep]] = T{
    ivyDeps() ++ Task.traverse(moduleDeps)(_.transitiveIvyDeps)().flatten
  }
  def upstreamCompileOutput = T{
    Task.traverse(moduleDeps)(_.compile)
  }

  def upstreamRunClasspath: T[Agg[PathRef]] = T{
    Task.traverse(moduleDeps)(_.runClasspath)().flatten
  }

  def platformSuffix = T{ "" }

  def runClasspath = ???

  def prependShellScript: T[String] = T{ "" }

  def sources = T.sources { millSourcePath / 'src }
  def generatedSources = T{ Seq.empty[PathRef] }
  def allSources = T{ sources() ++ generatedSources() }

  def allSourceFiles = T{
    for {
      root <- allSources()
      if exists(root.path)
      path <- ls.rec(root.path)
      if path.isFile && (path.ext == "java")
    } yield PathRef(path)
  }

  def compile = ???

}
