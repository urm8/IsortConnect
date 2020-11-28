# IsortConnect

![Build](https://github.com/urm8/IsortConnect/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.urm8.isortconnect.svg)](https://plugins.jetbrains.com/plugin/com.github.urm8.isortconnect)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.urm8.isortconnect.svg)](https://plugins.jetbrains.com/plugin/com.github.urm8.isortconnect)

## ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [x] [Publish a plugin manually](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html) for the first time.
- [x] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [x] Use jetbrains default toml engine

<!-- Plugin description -->
This plugin uses [isortd](https://github.com/urm8/isortd) to sort your imports, reusing your project's pyproject.toml config.
Connect to isortd and format your Python code without overhead of starting a new isort process on each file save/commit
by making http call to running isort daemon

Features supported:
- import pyproject.toml
- format on save
- format on action trigger
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "IsortConnect"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/urm8/IsortConnect/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
