# DiscordBotClient

a client for discord bot accounts using the discord application api thing

### [Download](https://github.com/Canary-Prism/DiscordBotClient/releases/)

I'm assuming you know how to use GitHub. If not then here:

### Download Steps

1. Click above link
2. Find latest release
3. Find "Assets" Section
4. Click the "DiscordBotClient-x.y.z.jar" file

### Notice

This program uses Java 21

#### [Here's a handy link](https://adoptium.net/temurin/releases/?version=21)

(it's for Temurin bc nobody likes oracle)


## Historical Changelog

### 1.3.0
- rewrote the markdown parser to be far more robust
- improved rendering of things when in a list so now they dont get stretched out if the list is short
- fixed a bug where you can't delete your own messages sometimes
- some stuff automatically refresh and update now (like member lists and message authors)
- added support for headers, antiheaders (`-# ` in discord), and quotes
- yet more text rendering improvements
- added a `-Dcanaryprism.dbc.debug` property which can be set to output more info to stdout and show bounding boxes on components that support it
- added a save data system that can store persistent data, currently only used to store channel category collapse state
- added a confirm prompt when deleting a message which you can bypass by holding shift like in discord's client
- rewrote all code drawing images to now be anti-aliased

### 1.2.0
- improved spaceng for messages with replies
- improved how reference messages look
- added more markdown support
- made the title bar thing appear in dark mode on macOS if the default dark laf is used
- improved and fixed text wrapping
- improved autoscroll
- fixed message rendering on windows and improved background rendering
- added loading window at the start
- added reconnect and resume support

### 1.1.0
- actually uses Java 21 now
- improved autoscrolling
- added copy and save image to attachments and emojis
- message input now is multiline
- added support for Server Voice Channels' text channels
- improved text spacing
- added ability to edit messages
- deleted messages are highlighted in red
- added ability to copy message content

### 1.0.0
- made the thing