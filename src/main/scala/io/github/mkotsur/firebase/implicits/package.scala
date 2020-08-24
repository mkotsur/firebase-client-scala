package io.github.mkotsur.firebase

import com.google.firebase.tasks.Task

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object implicits {

  private [firebase] implicit def task2future[T](task: Task[T]): Future[T] = {
    val resultPromise = Promise[T]()
    task.addOnCompleteListener((task: Task[T]) => Try(task.getResult) match {
      case Success(result) => resultPromise.success(result)
      case Failure(e) => resultPromise.failure(e)
    })
    resultPromise.future
  }

}
