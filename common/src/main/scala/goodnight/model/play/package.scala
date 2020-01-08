
package goodnight.model


package object play {
  type States = Seq[play.State]

  // the current state of a player, along with all info required to show it.
  type PlayerState = (
    Player, // the player herself
    States, // her current acquired qualities
    Activity, // what she last did
    Scene) // the scene of what she last did

  // information about a story, including the PlayerState if this user already
  // has a player.
  //
  // StoryData is also the type of the reply of ApiV1.Story
  type StoryState = (Story, Option[PlayerState])
}
