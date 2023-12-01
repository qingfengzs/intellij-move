# Sui Move Language plugin for the Intellij platform

[中文](./README.zh_CN.md)

## Installation

Open in your IDE, search for _Sui Move Language_ and install the plugin.
`Settings > Plugins > Marketplace`
## Features

* Syntax highlighting
* Code formatting
* Go-to-definition
* Type inference
* `Move.toml` and `move` binary integration

## Dependencies

For git dependencies specified in `Move.toml` file you need to manually run `move package build` to populate `build/` directory. 

## Compatible IDEs

All Intellij-based IDEs starting from version 2022.3. For 2023.3 and below you can use old versions of the plugin.

## Usage

### Set the Sui Cli Path

Open `Settings > Languages&Frameworks > Sui Move Language`,click file select and select the valid sui cli path.
Then the version label will show the version of the sui cli.
![img.png](docs/static/select-sui-path.png)

### Using the ToolWindow

The plugin features a dedicated ToolWindow with the following functionalities:
![img.png](docs/static/init.png)

- Refresh Project:

  ![img.png](img.png) : synchronize the latest state of your project.

- Get Active Address:

  ![img_1.png](img_1.png) : displays the current active address information.

- Switch Account:

  ![img_2.png](img_2.png) : to switch between different accounts.

- Switch Network:

  ![img_3.png](img_3.png) : allows switching between different network environments, such as from a development network
  to a test network.

## Notes

Ensure the project is correctly set up before using the plugin features.
The functionality of the plugin depends on the current state of the project, so keep it up-to-date.

## Getting Help

For any issues encountered while using the plugin, please open an issue
on [GitHub](http://github.com/moveFuns/intellij-move)
