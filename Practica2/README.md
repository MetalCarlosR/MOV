# High level components
In this section well have a breve description of the more important components of the game.

![esquema](https://user-images.githubusercontent.com/55288550/146690151-c5aaf523-a0e4-41cf-89dc-59097ccd91d2.png)

## Game Manager
The upmost component in the hierarchy. This component is a singleton with a few public functions relating to the whole application.

*  Clue methods. For checking if there are more clues, for consuming them when the player uses them and for adding them when the player watches a rewarded ad. This clues are a primary resource and a vital part of the game economy that is why the API to interact with them lives this high on the hierarchy.

*  Skin methods. Methods for setting or getting the skin scriptable-object which is a container for colors that change different parts of the UI.

* Level and Scene management. There are a few methods to check if there is a next level in the current package, to switch to next or previous and to switch scenes. This last one will be used for example when going back to the menu from the game itself.

Although not part of the API there are some other notorious private methods that handle application wide operations such as saving the state and restoring it. This methods simply delegate the call to a more specific object that deals with serialization and verification of the data but it is the Game Manager the one in charge of delegating this calls.

## Data Manager
Following up on the last note in the Game Manager, this Data Manager is the one in charge of storing and loading sensible information about the game. Such as: the amount of clues and different level states. Apart from storing and loading this information it will also modify it and access it when request via some methods delegated from the Game Manager such as "UseClue" or "LevelCompleted".

## Ad Manager && Ad Initializer
This ad manager will handle initialization, loading and displaying of all the advertisement int the game.
This are just one level under the Game Manager in hierarchy as they are mostly needed across the whole application and accessed by multiple sources. (When a level is completed we show an ad for example). We also have an script for each type of ad for organization purposes.

## Level Manager
This manager handles the UI that is seen in the game scene and functions as a bridge between the game and the level selector scene. Some buttons call directly the game manager to request a next level or change in scene and there is one that requests the ad manager for advertisement. This class also has some public methods for updating the UI such as: setting the percentage of level completition.

## Board Manager
The board manager is the one that handles the state of a current level. The whole gameplay logic. The flow completition, connection and restoration when the undo button is pressed. It also notifies the level manager when the state changes to update UI and when the level is completed to show the corresponding UI elements to leave the user options and give feedback based on how well the game was finished. It is also worth noting that, thanks to the data driven design that we've followed, levels are defined as a string of text. This string of text gets deconstructed into more useful information in a Puzzle class, with data in a more accessible way,  via a PuzzleParser static class with a Parse method.
