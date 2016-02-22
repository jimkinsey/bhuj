package bhuj.context

import bhuj.context.CaseClassConverter.NotACaseClass

object CaseClassConverter {
  sealed trait Failure
  case class GeneralFailure(message: String) extends Failure
  case class NotACaseClass(obj: Product) extends Failure
}

class CaseClassConverter {
  def map(caseClass: Product): Either[CaseClassConverter.Failure, Map[String, Any]]  = {
    if (isCaseClass(caseClass))
      Right(caseClassMap(caseClass))
    else
      Left(NotACaseClass(caseClass))
  }

  private def caseClassMap(caseClass: Product): Map[String, Any] = {
    publicFields(caseClass).map { f =>
      f.setAccessible(true)
      f.getName -> value(f.get(caseClass))
    }.toMap
  }

  private def value(obj: Any): Any = obj match {
    case product: Product if isCaseClass(product) =>
      caseClassMap(obj.asInstanceOf[Product])
    case map: Map[_,_] =>
      map.map { case (k,v) => k.toString -> value(v) }
    case iterable: Iterable[_] =>
      iterable.map(value)
    case _ =>
      obj
  }

  private def publicFields(obj: Product) = {
    obj.getClass.getDeclaredFields.filterNot(_.getName == "$outer")
  }

  private def isCaseClass(obj: Product) = {
    publicFields(obj).length == obj.productArity
  }
}

