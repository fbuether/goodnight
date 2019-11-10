
package goodnight.server

import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsonValidationError
import play.api.libs.json.Reads
import play.api.mvc.BaseController
import play.api.mvc.BodyParser
import play.api.mvc.ControllerComponents
import play.api.mvc.Request
import play.api.mvc.Result
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import goodnight.common.Serialise._


class Controller(
  components: ControllerComponents)
    extends BaseController {
  def controllerComponents: ControllerComponents =
    components

  // parse a json body sent with the request to a specified shape.
  // use like:
  // = Action.async(parse.json)(
  //  withJsonAs((request: Request[JsValue], data: Data) => {
  // ...
  def withJsonAs[A,R](innerAction: ((Request[JsValue], A) => Future[Result]))(
    request: Request[JsValue])(implicit reads: Reads[A]): Future[Result] = {
    request.body.validate[A] match {
      case JsSuccess(data, _) => innerAction(request, data)
      case JsError(errors) =>
        val errorMessage = ujson.Obj(
          "success" -> false,
          "errors" -> errors.map({ case (p,ves) =>
            ujson.Obj(
              "path" -> p.toString,
              "errors" -> ves.map({ case JsonValidationError(a) =>
                a}))}))
        Future.successful(
          BadRequest(write(errorMessage)))
    }
  }

  def parseJson[A](body: JsValue, innerAction: A => Future[Result])(
    implicit reads: Reads[A]): Future[Result] = {
    body.validate[A] match {
      case JsSuccess(data, _) => innerAction(data)
      case JsError(errors) =>
        val errorMessage = ujson.Obj(
          "success" -> false,
          "errors" -> errors.map({ case (p,ves) =>
            ujson.Obj(
              "path" -> p.toString,
              "errors" -> ves.map({ case JsonValidationError(a) =>
                a}))}))
        Future.successful(
          BadRequest(write(errorMessage)))
    }
  }


 //  val ofJson[A]: BodyParser[Option[A]] =
 //    BodyParser("upickle-json-body-parser")({ request =>
 //      // check if request is content-type application/json


 // readMaybe


  // def securedJsonAction[A](innerAction: A => Future[Result])(
  //   implicit ev: Serialisable[A]): Future[Result] = {

  //   auth.SecuredAction.async
  // }
}

