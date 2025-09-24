# d3d3 - Experimental 3D Engine (WIP)

> A personal journey into 3D engine development with Java, LWJGL, and OpenGL.

---

### 🚧 Status

This is a **very early prototype** of a custom 3D engine, written in Java using LWJGL and raw OpenGL 2.0 APIs. The project is **not production-ready**, nor is it a game — it's an experimental sandbox to learn, test, and architect the foundations of a fully-featured 3D engine.

### 🎯 Goals

* Build a lightweight 3D engine in Java
* Create a hierarchical **scene graph** architecture
* Implement a flexible **event system**
* Handle user **input** (keyboard, mouse)
* Support **resource loading** (textures, OBJ models, atlases)
* Experiment with **ECS**, **shaders**, and **custom tools** (e.g. level generators)

### 🔍 What's Here

So far, the project includes:

* `Engine`, `Application`, `LaunchConfig`: base bootstrapping
* Basic **scene graph** with parent-child node structure
* Camera system (first-person style)
* OBJ model loader (`OBJLoader`)
* Texture loader, atlas builder, asset manager
* Very early lighting system
* Procedural maze/level generator
* Some experimental demo scenes (`DevGame`, `DevGame2`, `LevelShowcase`, etc.)

### 💡 Philosophy

This is not a game engine clone. It is a personal space to learn **how engines work**, not just use them. Code may be messy, naming may change, subsystems may break — it's all part of the process.

> This project helps me stay grounded, keep my engineering mind sharp, and explore low-level graphics programming in a meaningful way.

### 📦 Structure

* `d3d3-engine` - Core engine modules (rendering, input, events, assets)
* `d3d3-dev-game` - Example games, demos, and test scenes
* `src/main/resources/` - Textures, models, shaders, etc.

### 🛠 Tech Stack

* Java 17+
* LWJGL 3
* OpenGL 2.0 (compatibility profile for now)
* STBImage (via LWJGL) for texture loading

### ❗ Warnings

* No GUI yet
* No physics
* No audio
* No networking
* No threading or job system yet
* Many parts are experimental or will be rewritten

### ✅ Next Steps (Todo)

* Finalize event dispatching system
* Implement input abstraction layer
* Improve shader/material system
* Begin ECS experiment
* Add debug GUI (maybe via Nuklear or custom)
* Optimize asset loading

### 🙏 Disclaimer

This is a personal learning project. Expect bugs, weird decisions, and unfinished systems. PRs are not accepted for now — but feedback is welcome!

[me@ancevt.com](mailto:me@ancevt.com)
