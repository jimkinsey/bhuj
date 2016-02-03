package com.github.jimkinsey.mustache

import java.io.{File, PrintWriter}

import org.scalatest.{BeforeAndAfterEach, Suite}

trait TemplateFiles extends BeforeAndAfterEach { self: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    val templateDir = new File(getClass.getResource("/templates").getPath)
    val testDir = new File(templateDir.getPath + s"/$templateDirName")
    if (testDir.exists()) {
      testDir.delete()
    }
    testDir.mkdir()
  }

  lazy val templateDirPath = getClass.getResource(s"/templates/$templateDirName").getPath

  def templateDirName: String

  def templateFile(name: String, content: String): File = templateFile(name, Some(content))

  def templateFile(name: String, content: Option[String] = None): File = {
    val file = new File(s"$templateDirPath/$name.mustache")
    content.foreach { template =>
      val writer = new PrintWriter(file)
      writer.write(template)
      writer.close()
    }
    file
  }

}
