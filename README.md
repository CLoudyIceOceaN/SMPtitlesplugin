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
    name: "&6&l♛ KING ♛"
    price: 50000
    font: normal
    description:
      - "&7For players with WAY too much money."
  rainbow:
    name: "RAINBOW"
    price: 250000
    rainbow: true
    bold: true
  champion:
    name: "&e&l✦ CHAMPION ✦"
    price: 0
    buyable: false
    description:
      - "&7Win a server event to get this!"
```

Everything a title can have:

| option | what it does |
|---|---|
| `name` | the title text — **`&` colors work** (`&6` gold, `&d` pink, `&l` bold...) and so do pasted symbols: ✦ ★ ♛ ❖ ☠ ➤ |
| `price` | cost in server money |
| `font` | `normal` / `smallcaps` (small capitals — looks the best) / `fullwidth` / `circled` |
| `rainbow` | `true` = every letter gets a different color automatically |
| `bold` | `true` = makes rainbow letters bold |
| `buyable` | `false` = **event/giveaway reward** — shows in the shop as "✦ EVENT REWARD, can't be bought", and only `/titles give` can hand it out |
| `description` | extra lore lines shown on the book in the shop |

Save the file and type **`/titles reload`** in game — no restart, and
anyone already wearing a renamed title gets the new look instantly.
If a font shows squares in game, that font isn't in the client — use
`normal` or `smallcaps`.

## Link an NPC (like a villager) to the menu

1. Stand next to the mob and type `/titles npc`
2. Right-click the mob — done!

Now **anyone who right-clicks it opens the titles shop** (a linked
villager won't open its trades). Right-clicking a linked mob while in
`/titles npc` mode unlinks it again. Linked mobs can't be hurt
(turn that off with `protect-npcs: false`).

## Admin commands

| Command | What it does |
|---|---|
| `/titles reload` | Reload config.yml after editing (OP only) |
| `/titles give Steve king` | Give a title for free — perfect for **events and giveaways** (the winner gets a "You got the title!" message) |
| `/titles take Steve king` | Take a title away again |
| `/titles npc` | Link (or unlink) the next mob you right-click to the menu |

Who owns what is saved in `plugins/SMPtitlesplugin/players.yml`.

## For people who want to change the code

Java source is in `src/`. Rebuild on a Mac with Homebrew OpenJDK 21:

```
./build.sh
```
