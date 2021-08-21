# :baby: Comic-Bot : A Telegram Bot (WIP):construction_worker:

Comic-Bot is a simple bot to interact with your favorite web comic.

It has very limited capabilities and only supports **Xkcd** comic.

# How to Start
There are 2 ways to start:
1. Run `clj -M:main $TELEGRAM_BOT_TOKEN` in shell where TELEGRAM_BOT_TOKEN is Telegram Bot API key.
1. Alternative: set enviroment variable TELEGRAM_BOT_TOKEN with Telegram Bot API key then run `clj -M:main`.

# List of Commands
Try interacting with bot by send chat message to contact ComicBot in Telegram:
1. `/start` to see welcome message
1. `/latest` to get latest Xkcd comic strip

## Under the hood

Comic-Bot is using [clj-http](https://github.com/dakrone/clj-http) to poll  any new recognized message and respond accordingly. It runs on single thread.

## TODO
- [x] Print bot status
- [x] reply message/picture asynchronously
- [ ] improve tests

## Disclaimer

[Xkcd](https://xkcd.com/) is owned by Randall Munroe.
