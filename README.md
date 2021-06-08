
<a href="https://ibb.co/Jj2S8M0"><img src="https://i.ibb.co/YB8VJqY/Bob-The-Discord-Bot.png" alt="Bob-The-Discord-Bot"></a>

# Bob The Discord Bot

> A simple Discord bot made in Java for learning purposes

### Technologies Used
- Java 11
- JDA
- SLF4j
- JDBC
- JUnit
- Maven
- Docker
- Git
- IntelliJ (Development Environment)

### Installation
*The provided source code has been written and tested in Jetbrains IntelliJ. It is not guaranteed to work as-is when imported with other IDEs*

The program is docker-compatible and that is the suggested deployment method.
Navigate over to the program directory and run:
```
docker build -t discord-bot-X.X.X .
```
For raspberry Pi and ARM architecture compatibility use **Dockerfile-arm**
```
docker build -t discord-bot-X.X.X -f Dockerfile-arm .
```

This will compile the project and create an image.
After that you can start the image with:
 ```
 docker run --name discord-bot-X.X.X -e DISCORD_BOT_TOKEN -e DISCORD_BOT_JDBC_URL -d discord-bot-X.X.X
```

*Make sure to you have a bot application token setup as an environment variable with that exact name, otherwise the bot will shutdown immediately.*

*You also need a database connection url that includes the username and password, example is in the* **jdbc.properties file**

###License
This Software is released under an [MIT license](https://opensource.org/licenses/MIT)
