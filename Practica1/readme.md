# 0h Y3s

This android and desktop game is an adaptation of the original [Oh no](https://0hn0.com/) published by Q42 and made by Martin Kool. This version was developed as part of our **Mobile Videogame Development** course.

## Extra features

We have added some extra features:

- Sound
- Desktop stop consuming resources when window is minimized 
- [Serialziation](#)
- Android Application closing
- Multiplatform link re-direct


## Module Dependencies

TODO Abstract Engine y Abstract Graphics

### Engine (Technology Interfaces)

This module exposes interfaces that the application logic can use independent of platform.

- IGraphics: Factory methods for Image and Font creation and methods to setup a logical canvas and some methods to change the inner state (actual color, opacity or font to render with).

- TouchEvent: Describes user interaction with the application maps to mouse or tabs on concrete implementations (Android and Desktop)

- IAudio: Similar to graphics abstracts audio resource creation on a factory method and allows reproducing sounds.

### Android Engine

- Engine: The concrete engine implementation on Android is a little more involved than the one we have on Desktop. Firstly we need a new thread for the main loop.Android Activities can't hold the main loop as they follow an event driven architecture. That architecture doesn't apply very well with serious game development, hence we need active rendering on a second thread to be in full control of our application.  
  Secondly Android Activity Life Cycle more or less forces us to implement onPause and onResume methods and to safely re-launch our application so we need a strong and save thread management to avoid leaving dead threads on the system.

- Audio: We need a circular buffer of sounds on our simple audio engine because Android's garbage collector destroys the currently playing sounds if no reference is held to them. But the memory they use is freed on audio completition thanks to a flag the API provides. The user of the engine can Store larger sound objects that won't be freed until they remove the reference as with traditional java programming. (Sound interface supplies simple play and pause methods).

### Launchers

- We need launcher modules to depend on so many other modules because they have the responsability to be the "bridge" that starts the game application, the engine and configures everything to later give up the main loop to the engine. The android one has a little bit more to it as in Android Applications we can't simply have a main function but it ends up been very similar.

## Gameplay Modules Architecture
![OhYesArchitecture](https://user-images.githubusercontent.com/48621751/141690542-47494d9b-eb64-4d27-9349-cd5a81925e8a.png)
TODO describir un poco esto

## Authors

- [Nicolás Pastore](https://github.com/nicopast)
- [Ricardo Sulbarán](https://github.com/drathijin)
- [Carlos Romero](https://github.com/metalcarlosr)
