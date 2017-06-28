package io.github.mkotsur.firebase.rest

sealed trait QueryParam {
  def toStringParam: String
}
trait QueryParams extends QueryParam {
  def params: List[QueryParam]
  override def toStringParam = {
    params.foldLeft("?"){ (one: String, two: QueryParam) =>
      one + two.toStringParam + "&"
    }.dropRight(1)
  }
}
object QueryParams {
  val empty = new QueryParams{
    val params = List.empty
  }
}
case class OrderBy(field: String) extends QueryParam {
  val toStringParam = s"""orderBy="$field""""
}
case object OrderByKey extends QueryParam {
 val toStringParam = s"""orderBy="$$key""""
}
case object OrderByValue extends QueryParam {
  val toStringParam = s"""orderBy="$$value""""
}
case class StartAt(value: String) extends QueryParam {
  val toStringParam = s"""startAt="$value""""
}
case class EndAt(value: String) extends QueryParam {
  val toStringParam = s"""endAt="$value""""
}
case class Range(orderField: String, start: String, end: String) extends QueryParams {
  val params = List(OrderBy(orderField), StartAt(start), EndAt(end))
}
