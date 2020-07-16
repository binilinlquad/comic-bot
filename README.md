# :baby: Comic-Bot : A Telegram Bot (WIP):construction_worker:

Comic-Bot is a simple bot to interact with your favorite web comic.

It has very limited capabilities and only supports **Xkcd** comic.

# How to Start
1. Clone this repo.
2. Provides Telegram Bot API key through enviroment variable TELEGRAM_BOT_TOKEN.
3. Run `clj -m com.gandan.bot` in your favorite shell.
4. When run successfully, no output is printed currently.

Try interacting with bot by send chat message to contact ComicBot in Telegram:
1. `/start` to see welcome message
1. `/latest` to get latest Xkcd comic strip

## Under the hood

Comic-Bot is using [clj-http](https://github.com/dakrone/clj-http) to poll  any new recognized message and respond accordingly. It runs on single thread.

## TODO
- [ ] Print bot status
- [ ] reply message/picture asynchronously
- [ ] improve tests

## Disclaimer

[Xkcd](https://xkcd.com/) is owned by Randall Munroe.
