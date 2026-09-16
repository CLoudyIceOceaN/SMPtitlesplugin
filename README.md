# SMPtitlesplugin

Buy **titles** that float **above your nametag** — made for **BladeSMP**
(Eaglercraft 1.8 / Spigot 1.8.8 servers).

Needs [SMPmoneyplugin](https://github.com/CLoudyIceOceaN/SMPmoneyplugin)
(titles are bought with its money — the installer grabs it automatically).

**Easy download page:** https://cloudyiceocean.github.io/SMPtitlesplugin/

---

## How to install — one command

Terminal → `cd` into your **server folder** → paste:

```
curl -sL https://cloudyiceocean.github.io/SMPtitlesplugin/install.sh | bash
```

Then **restart the server**.

## How it works for players

- `/titles` opens the shop. Every title is an **enchanted book** — hover
  one to see exactly how it will look above your head, and its price.
- Click a book to buy it (with a confirm screen). Buying it puts it on
  right away.
- The **chest at the bottom** opens *Your Titles* — everything you own.
  Click one to equip it, click it again to take it off, or use the
  red barrier button to remove your title.
- Your title floats **above your nametag** for everyone to see, and it
  stays through deaths, teleports, and relogs.

## Adding titles (server owner)

**The plugin ships with NO titles** — you add your own. Open
`plugins/SMPtitlesplugin/config.yml` and copy one of the examples:

```yaml
titles:
  king:
    name: "&6&lKING"
    price: 50000
    font: normal
  shadow:
    name: "&8Shadow"
    price: 10000
    font: smallcaps
```

- The id (`king`) is just for commands — players see the `name`.
- `name` uses `&` color codes: `&6` gold, `&d` pink, `&l` bold...
- `price` is in server money.
- `font` changes the letters themselves:

| font | what it does |
|---|---|
| `normal` | regular letters |
| `smallcaps` | turns letters into small capitals — looks the best |
| `fullwidth` | wide, spaced-out letters |
| `circled` | every letter inside a little circle |

Save the file and type **`/titles reload`** in game — no restart, and
anyone already wearing a renamed title gets the new look instantly.
If a font shows squares in game, that font isn't in the client — use
`normal` or `smallcaps`.

## Admin commands

| Command | What it does |
|---|---|
| `/titles reload` | Reload config.yml after editing (OP only) |
| `/titles give Steve king` | Give a player a title for free (OP only) |

Who owns what is saved in `plugins/SMPtitlesplugin/players.yml`.

## For people who want to change the code

Java source is in `src/`. Rebuild on a Mac with Homebrew OpenJDK 21:

```
./build.sh
```
