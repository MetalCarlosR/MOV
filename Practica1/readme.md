# 0h Y3s

This android and desktop game is an adaptation of the original [Oh no](https://0hn0.com/) published by Q42 and made by Martin Kool. This version was developed as part of our **Mobile Videogame Development** course.

## Extra features

We have added some extra features:

- Sound
- Desktop stop consuming resources when window is minimized 
- [Serialziation](#android-engine)
- Android Application closing
- Multiplatform link re-direct


## Module Dependencies
![EngineArchitecture](https://user-images.githubusercontent.com/48621751/141697549-3dfd585b-6fee-4546-8798-9ea1c758eb2f.png)

### Engine

This module exposes interfaces that the application logic can use independent of platform and creates the abstract classes that the engine of each platform will use.

#### Technology Interfaces

- IGraphics: Factory methods for Image and Font creation and methods to setup a logical canvas and some methods to change the inner state (actual color, opacity or font to render with).

- TouchEvent: Describes user interaction with the application maps to mouse or tabs on concrete implementations (Android and Desktop)

- IAudio: Similar to graphics abstracts audio resource creation on a factory method and allows reproducing sounds.

- The Engine also defines simple interfaces for the management of resources that are implemented in each platform with the needed resource creation and management (ISound,IFont,IImage)

#### Abstract Classes

- AbstractGraphics: Gives a unify computation for every platform of the translation and scale factor of the screen, it stores a target resolution as well as the scale so when an application is rescaled it will compute the new dimiensions and offsets.

- AbstractEngine: Implements the management of the main loop and the application, it uses active rendering on its own thread so it has full control of the applications flow, allowing us to pause and resume the application when its minimize or needs to be restored on Android. Because the rendering and the closing of the application varies from each platform it leaves those functions to be implemented by the engine of said platform. Because we don't want to waste resources we lock the application main loop to 60 frames per seconds using sleep, we compute the time we need to use the sleep to minimize the amount of frames "lost" with the targeted 60fps.

### Android Engine

- Engine: Because the AbstractEngine already manages the thread it only needs to implement the functions related to the rendering and closing of the Android Platform. It also implements the posibility of restoring the application state, when a Bundle is recieved it checks if its valid with a flag and forwards it to the current app as a Map to restore the state.

- Audio: We need a circular buffer of sounds on our simple audio engine because Android's garbage collector destroys the currently playing sounds if no reference is held to them. But the memory they use is freed on audio completition thanks to a flag the API provides. The user of the engine can Store larger sound objects that won't be freed until they remove the reference as with traditional java programming. (Sound interface supplies simple play and pause methods).

- Input: Creates and manages a list of TouchEvents that the engine will send to the application. In Android we use OnTouchListener given by the platform where we get the type of input (touch,release) and the position on the screen.

### PC Engine

- Engine: Implements the functions for rendering and closing the appliaction required by the AbstractEngine. It also has the posibility to store the current state of the game for later restoring, it is done by using the temp folder of the OS where it serializes the state and stores a txt with the data, later when the app is opened again it checks if it has anything to restore in the temp folder (this is a state save and restore function not a gamesave mechanic, although it could be used that way).

- Audio: Is a really simple implementation with to methods for creating the sound. Because there is a garbage collector we also have the posibility to use a createAndPlay method that for small sounds that will be used once and removed after.

- Input: Creates and manages a list of TouchEvents that the engine will send to the application. Adds a mouseListener to the window so we can when the type of input and the point on screen so we can store it on the list. 

### Launchers

- We need launcher modules to depend on so many other modules because they have the responsability to be the "bridge" that starts the game application, the engine and configures everything to later give up the main loop to the engine. The android one has a little bit more to it as in Android Applications we can't simply have a main function but it ends up been very similar.

## Gameplay Modules Architecture
![OhYesArchitecture](https://user-images.githubusercontent.com/48621751/141690542-47494d9b-eb64-4d27-9349-cd5a81925e8a.png)

### GameStates

- 0hY3s: Manages all the logic in a game, has all the references to the Grid and also the Engine so it can connect the inputs. It updates the cells and renders all the information on screen.

- Menu: Draws and manages the application when is in to main states, the initial window with the game title and credits, and the select size window where the player chooses the size for the game. When a player chooses the size it tells the engine to switch to the 0hYs module with the one selected.

### Grid

- Grid: Central class that stores all the cells. It manages the rendering of the cells as well as their update, when an input is recieved from the game it checks the collision with a cells and changes the state of the one it collides. Store a percentage of the completion for easier win check.

- GridSolver: It has all the methods needed by the game and the GridGenerator related to the Grid. It helps the GridGenerator to check if a generated Grid has a possible solution, also creates the clues using the data on the Grid.

- Clue: Stores all the data about a generated clue wich are the display messages and the cell is centered around, also the solution for the clue (blue or red).

- GridGenerator: Static class that creates a random Grid using the GridSolver as a helper. It also has the possibility to generate the grid from a serialized string created by the engine on when saving a state (theoreticaly you could also load custom grids).

### Buttons

- BaseButton: Abstrac class for buttons to implement, it gives the logic for the management of a click and leaves the classes that extends from it to implement the collision checking and the callback when is clicked.

- RectangleButton: Abstract class that extends from BaseButton, implements the collision checking with an AABB style code. Leaves the implementation of the callback.

- CircleButton: Abstract class that extends from BaseButton, implements the collision comparing the radius and the distance vector module. Leaves the implementation of the callback.

- ImageButton: Abstract class that extends from RectangleButton, adds the possibility to draw an image in the bounding box, also changes the opacity of the image when held. Leaves the implementation of the callback.

- TextBUtton: Abstract class that extends from RectangleButton, adds the possibility to draw a text in the bounding box, also changes the color of the text when pressed. Leaves the implementation of the callback.

- Cell: Extends from CircleButton, it also stores all the data and logic of a cell in the grid. It has it position in the grid as well as it state ( Grey, Blue, Red ). The callback that implements changes it state to the next one, if its locked from the start if will show a padlock for a few seconds as feedback and won't change. Manages all the rendering as well as the animations ( locked, focused...).

### Tweens

- Tween: Simple class to facilitate animations. It takes a duration and an interpolation mode and on update it returns a number between 0 and 1 based on the time that has passed since it's creation. This number between 0 and 1 will follow te easing related to the interpolation mode selected. To facilitate the application of this tweenning system it has the possibility to get an ITweenTarget that applies the value as it sees it fit. 

- ColorModulator: This is an ITweenTarget that a Tween can update with the 0 to 1 value corresponding to the duration. It modulates from initial to target color, component by component.


## Authors

- [Nicolás Pastore](https://github.com/nicopast)
- [Ricardo Sulbarán](https://github.com/drathijin)
- [Carlos Romero](https://github.com/metalcarlosr)
