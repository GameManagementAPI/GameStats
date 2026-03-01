# GMA-Template
This repository contains a template for paper plugins using the GameManagementAPI.<br>
Go read the full documentation [here](https://gma.c4vxl.de/docs/)

## Installation
#### 1. Clone this repository:
```sh
git clone https://github.com/GameManagementAPI/GMA-template
```
and open it in the IDE of your choice.

<br>

#### 2. Download GameManager
Download the [latest release](https://github.com/GameManagementAPI/GameManager/releases/latest) of GameManager and place it into `run/plugins`.<br>
For better testing it is recommended to download [GameLobby](https://github.com/GameManagementAPI/GameLobby/releases/latest) as well.

If you are on linux/macOS you can do this automatically by using the `downloadGMA.sh`-script:
```sh
sh downloadGMA.sh
```

<br>

#### 3. Accept Server EULA
Accept the minecraft server eula in `run/eula.txt`.

<br>

#### 4. Customize Metadata
It is recommended to
- Change the project name in the `settings.gradle.kts`
- Customize the plugin data in the `build.gradle.kts`
- Rename the root path from `de.c4vxl.gamestats` to your own group id

<br>
 
#### 5. Reinitializing the git repo
1. Delete the old repo:<br>
   On Windows:
    ```sh
   rmdir /s /q .git
    ```

   On linux/mac:
    ```sh
   rm -rf .git
    ```

2. Initialize a new one:
    ```sh
   git init
   git add .
    ```

3. Push an initial commit:
    ```sh
   git commit -am "Initial Commit"
   git remote add origin "<your repo url>"
   git checkout -b main
   git push origin main
   ```

### That's it.


## Testing locally
For testing your plugin locally use the `runServer`-gradle task:
```sh
./gradlew runServer
```