
package goodnight.stories

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.util.{ Try, Success, Failure }

import goodnight.client.pages
import goodnight.common.ApiV1
import goodnight.common.Serialise._
import goodnight.components._
import goodnight.model.play
import goodnight.model
import goodnight.service.Request
import goodnight.service.Conversions._
import goodnight.model.Expression
import goodnight.model.Expression.BinaryOperator


object WithStory {
  val testData: play.StoryState = (play.Story("das-schloss",
    "Das Schloss",
    "fbuether",
    "Newer Looking, But Older Rocket.png"),
    Some((play.Player("fbuether",
      "das-schloss",
      "Sir Archibald"),
      Seq(
        play.State(
          play.Quality("das-schloss",
            "fleissig",
            play.Sort.Bool,
            "Fleißig",
            "I can help you my son, I am Paddle Paul..png"),
          true),
        play.State(
          play.Quality("das-schloss",
            "fleissig TV",
            play.Sort.Integer,
            "Fleißig TV",
            "Plasma TV.png"),
          11),
        play.State(
          play.Quality("das-schloss",
            "gut-situiert",
            play.Sort.Integer,
            "Gut situiert",
            "Chea.png"),
          7)),
      play.Activity("das-schloss",
        "fbuether",
        "abwarten",
        Seq(
          play.State(play.Quality("das-schloss",
            "gut-situiert",
            play.Sort.Integer,
            "Gut situiert",
            "Chea.png"),
            7))),
      play.Scene("das-schloss",
        "abwarten",
        "# Erstmal abwarten.\n\nIrgendetwas wird schon passieren.",
        Seq(
          play.Choice("abwarten-continue",
            "Mal sehen, yes?",
            true,
            Seq(
              play.Test(
                play.Quality("das-schloss",
                  "gierig",
                  play.Sort.Integer,
                  "Gierig",
                  "Bomb.png"),
                true,
                Expression.LessOrEqual, 5),
              play.Test(
                play.Quality("das-schloss",
                  "fleissig",
                  play.Sort.Bool,
                  "Fleißig",
                  "Blue Soap.png"),
                true,
                true)
            )),
          play.Choice("abwarten-hesitate",
            "# Wirklich abwarten?\nBist Du Dir ganz sicher?",
            false,
            Seq(
              play.Test(
                play.Quality("das-schloss",
                  "unerfuellbar",
                  play.Sort.Bool,
                  "Unerfüllbar",
                  "Tree.png"),
                false,
                true)))
            )))))



  case class Props(router: pages.Router,
    storyUrlname: String, full: Boolean, child: play.StoryState => VdomElement)
  case class State(story: Option[play.StoryState])

  class Backend(bs: BackendScope[Props, State]) {
    def setStoryOrFail(result: Try[play.StoryState]): Callback = result match {
      case Success(storyData) => bs.setState(State(Some(storyData)))
      case Failure(e) =>
        Callback.log(s"Error while fetching story: $e") >>
        bs.props.flatMap(_.router.set(pages.Stories))
    }

    def loadIfRequired(storyUrlname: String): Callback =
      Request(ApiV1.Story, storyUrlname).send.
        forStatus(200).
        forJson[play.StoryState].
        body.
        completeWith(//setStoryOrFail)
          _ => setStoryOrFail(
            Success(
              testData
            )))


    def render(props: Props, state: State): VdomElement =
      state.story match {
        case None => <.div(
          Banner.component(props.router, "Alien World.png", "Loading story…"),
          Loading.component(props.router))
        case Some(storyData) => <.div(
          Banner.component(props.router, storyData._1.image, storyData._1.name),
          props.child(storyData))
      }
  }

  // todo: re-use an already loaded story, if this component reloads with the
  // same storyUrlname.
  val component = ScalaComponent.builder[Props]("WithStory").
    initialState(State(None)).
    renderBackend[Backend].
    componentDidMount(bs => bs.backend.loadIfRequired(bs.props.storyUrlname)).
    build

}
