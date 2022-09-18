# MinecraftStatsPlugin

This is a Bukkit plugin that installs [*MinecraftStats*](https://github.com/pdinklag/MinecraftStats) in a supported plugin's webserver and regularly updates it automatically. This enables you to use *MinecraftStats* on a hoster without the need of any SSH access to the host. 

## Setup Guide

### Requirements

Your server must support Bukkit plugins, e.g., [Spigot](https://www.spigotmc.org/) or [Paper](https://papermc.io/) will do. The minimum supported server version is 1.13.2. Your host must also have *git* and *Python* 3.4 or later installed. Nowadays, this should be true for virtually any system out there.

You require a webserver to serve the *MinecraftStats* frontend. This may be a webserver provided by another plugins. As of now, [dynmap](https://github.com/webbukkit/dynmap) is supported and automatically detected &ndash; in other words, the easiest way to use this plugin is if you are already using dynmap. Otherwise, you will have to specify the webserver yourself in the `target` variable.

### Installation

Simply put the plugin's JAR file into your server's `plugins` directory.

If all [requirements](#requirements) are met, the plugin will automatically detect dynmap and install *MinecraftStats* and reguarly update it &ndash; every 5 minutes by default.

### Configuration

The configuration consists of two files in the plugin's directory that are outlined below.

When you change a configuration, use `/reload` on your server to apply it right away; the stats will then also be updated immediately.

#### Plugin

When you run the plugin for the first time, the file `config.yml` will be created. It has only two entries:

* `target` - This is the path to where *MinecraftStats* will be installed. This should be under your webserver's document root. If you use dynmap, the plugin will automatically fill this in for you.
* `updateInterval` - The stats will be updated every this many minutes.

#### MinecraftStats

Once everything is running, the plugin will also generate a `config.json` file. This is the configuration for *MinecraftStats* itself. Please refer to the [Configuration section](https://github.com/pdinklag/MinecraftStats#configuration) in the *MinecraftStats* documentation for details.

Note that the plugin will initially fill your `server.sources` property automatically. Typically, you will never have to touch it.

## Usage

The plugin will run as a background task and update the stats regularly as configured.

### Accessing via dynmap

If you use dynmap, simply append `/mcstats/index.html` to your dynmap URL.

For example, if your dynmap URL is `http://my-minecraftserver.com:8123`, then you will be able to access *MinecraftStats* via `http://my-minecraftserver.com:8123/mcstats/index.html`. **Note** that leaving out the `/index.html` at the end does not seem to work with dynmap's webserver.

Otherwise, browse to the URL corresponding to your `target` stated in the documentation.

### Troubleshooting

If you are setting up a new server and the *MinecraftStats* frontend remains empty with no players, a common reason for this is the `minPlaytime` being 60 minutes by default. That is, no player will appear in the stats unless they have played for 60 minutes. If

#### Update Logs

Whenever *MinecraftStats* is updated, two files will be created in the plugin's directory: `update.log` and `update-error.log`. These contain the output of `update.py`. Consult them if you need any more details about the last update.

## License and Attribution

*MinecraftStatsPlugin* is released under the [Creative Commons BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) license. This means you can pretty much use and modify it freely, with the only requirements being attribution and not putting it under restrictive (e.g., commercial) licenses if modified.

Concerning the *attribution* part, the only requirement is that you provide a visible link to [the original MinecraftStats repository](https://github.com/pdinklag/MinecraftStats) in your frontend. The easiest way to do this is by not removing it from the `index.html` footer, where you will also find a reminder about this.