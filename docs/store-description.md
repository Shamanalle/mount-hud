# Store pages / Страницы на площадках

The Modrinth project page is `docs/modrinth.md`: the whole page, in English, with the Russian translation collapsed in a `<details>` block at the end. It is short and tells players what they get; settings and details belong in the README. The *Modrinth page* workflow uploads the page and the summary below to Modrinth on every change pushed to `main` (it needs the `MODRINTH_TOKEN` secret with the *Write projects* scope). CurseForge cannot be updated this way: paste the same text into the description editor by hand. The gallery pictures are listed in `docs/gallery.json` (file, title, description, featured; the list's order is their order) and the same workflow puts them into the Modrinth gallery, replacing the picture with the same title. The pictures are 1920x1080 screenshots from the in-game client test (Minecraft 26.3). Each picture must stay under 2 MiB (CurseForge's limit). The page shows the first picture through its raw GitHub link, so the text pasted into CurseForge brings it along.

The page describes the release (1.0.0) and is uploaded only once the project exists and its id is in `.github/publish.json`.

Страница проекта на Modrinth — это `docs/modrinth.md`: вся страница на английском, а русский перевод свёрнут в блок `<details>` в конце. Она короткая и рассказывает игрокам, что они получат; настройки и подробности — в README. Workflow *Modrinth page* выкладывает страницу и краткое описание ниже на Modrinth при каждом изменении в `main` (нужен секрет `MODRINTH_TOKEN` с правом *Write projects*). CurseForge так обновить нельзя: вставьте тот же текст в редактор описания вручную. Картинки галереи перечислены в `docs/gallery.json` (файл, заголовок, описание, featured; порядок списка — порядок картинок), тот же workflow кладёт их в галерею Modrinth, заменяя картинку с тем же заголовком. Картинки — скриншоты 1920x1080 из клиентского игрового теста (Minecraft 26.3). Каждая картинка должна быть меньше 2 МиБ (ограничение CurseForge). Первую картинку страница показывает по прямой ссылке на GitHub, так что текст, вставленный в CurseForge, приносит её с собой.

Страница описывает релиз (1.0.0) и выкладывается, только когда проект создан и его id записан в `.github/publish.json`.

---

## English

**Name:** Mount HUD: Hunger, XP & Horse Stats

No loader, Minecraft version or other mods' names in the name: the stores show those as tags, and the name stays right as versions are added.

**Summary:** Keep your hunger bar and experience bar visible while riding a horse, camel, pig or strider: the mount's hearts sit above hunger or on the left, the jump bar shows only while charging a jump, and the horse inventory shows speed and jump height.

246 characters: Modrinth allows 256, CurseForge 250.

**Categories:** Utility, Game Mechanics

**Environment:** client only (Modrinth: *Client: required, Server: unsupported*).

**Dependencies:** Mod Menu (optional on Fabric / Quilt). Fabric API is not required.

**License:** MIT.

**Links:** source `https://github.com/Shamanalle/mount-hud`, issues `https://github.com/Shamanalle/mount-hud/issues`.

**Icon:** `icon.png` (512x512).

### Upload checklist

1. Create the project on Modrinth (type *Mod*) and on CurseForge (*Minecraft → Mods*) with the name, summary, categories, icon, license and links above.
2. Modrinth: set the environment as above. CurseForge: paste the text of `docs/modrinth.md` (English only) into the description, and upload the pictures from `docs/gallery.json` under *Images* with the same titles and descriptions; mark the first one as the featured image.
3. Put both project ids into `.github/publish.json` and add the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` secrets (scopes in `CONTRIBUTING.md`).
4. Run *Actions → Modrinth page* once, then publish the release.

## Русский

**Название:** Mount HUD: Hunger, XP & Horse Stats

В названии нет загрузчика, версии Minecraft и названий других модов: площадки показывают их тегами, а название не придётся менять при добавлении версий.

**Краткое описание** (на площадках только на английском, как выше). Перевод: Сохраняет полоску голода и полоску опыта во время езды на лошади, верблюде, свинье или страйдере: сердца животного располагаются над голодом или слева, полоска прыжка появляется только при зарядке прыжка, а в инвентаре лошади видны скорость и высота прыжка.

**Категории:** Utility, Game Mechanics

**Окружение:** только клиент (Modrinth: *Client: required, Server: unsupported*).

**Зависимости:** Mod Menu (необязателен на Fabric / Quilt). Fabric API не требуется.

**Лицензия:** MIT.

**Ссылки:** исходники `https://github.com/Shamanalle/mount-hud`, issues `https://github.com/Shamanalle/mount-hud/issues`.

**Иконка:** `icon.png` (512x512).

### Чек-лист загрузки

1. Создайте проект на Modrinth (тип *Mod*) и на CurseForge (*Minecraft → Mods*) с названием, кратким описанием, категориями, иконкой, лицензией и ссылками выше.
2. Modrinth: укажите окружение, как выше. CurseForge: вставьте в описание текст `docs/modrinth.md` (только английскую часть) и загрузите картинки из `docs/gallery.json` во вкладку *Images* с теми же заголовками и описаниями; первую отметьте как главную.
3. Впишите id обоих проектов в `.github/publish.json` и добавьте секреты `MODRINTH_TOKEN` и `CURSEFORGE_TOKEN` (права — в `CONTRIBUTING.md`).
4. Один раз запустите *Actions → Modrinth page*, затем опубликуйте релиз.
