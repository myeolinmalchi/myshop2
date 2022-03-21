package controllers

import javax.inject._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index = Action { implicit request =>
      request.session.get("userId") match {
        case Some(userId) => Ok(views.html.index(userId))
        case None => Ok(views.html.index(null))
      }
  }
}
