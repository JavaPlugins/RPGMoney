# Issues [here](https://github.com/PluginBugs/Issues-RPGMoney/issues).

## Building

Use a full **JDK 21** (a JRE alone cannot compile the plugin) and the included
Gradle wrapper:

```sh
bash gradlew build
```

The shaded plugin is written to `build/libs/RPGMoney.jar`. Building no longer
copies files to a developer-specific server directory. To deploy locally, run
`bash gradlew copyToServer -PserverPluginsDir=/path/to/server/plugins` explicitly.

The GitHub Actions `Build` workflow runs the build and tests on pushes and pull
requests and can also be started manually. A successful run publishes the plugin
and test reports as workflow artifacts. This workflow has not yet been run.

## Minecraft compatibility

This update targets Paper 1.21.4 and newer, including Minecraft 26.3. The plugin
is compiled against the public Paper 1.21.4 API, with Java 21 bytecode; it does
not use a server development bundle. Run the server with the Java version
required by that Paper release (Java 25 for Paper 26.3).

NBT API 2.16.1 supplies mappings through 26.3. FastNbt is no longer required.
Adventure is supplied by Paper rather than downloaded separately during startup;
Commons IO is bundled and relocated inside the plugin.
The existing `RPGMoney`, `Money`, `Rnd`, and `CashID` item tags are unchanged,
so this update does not intentionally invalidate existing currency items.
Vault and a compatible economy provider are required. ItemsAdder remains
optional and must itself support your server version. Its compile-time API is
the published `beer.devs:itemsadder-api:4.0.17` artifact from Maven Central.

The bundled configuration references an external legacy resource pack. Its
models are not maintained in this repository and have not been validated on
26.3. Use material names or custom head textures in `item_icon`, or supply an
updated ItemsAdder setup, if legacy models do not render correctly.

### Validation status

`bash gradlew build` completed successfully with JDK 21. All 10 unit tests
passed (none skipped): crop maturity, ordinary blocks, legacy and hex colors,
italic formatting, and MiniMessage compatibility.

A temporary Paper **26.3 build 241** server on **Temurin 25.0.5** with
**Vault 1.7.3** and a test economy service passed 16 runtime checks: plugin
enablement, coin and banknote tags and amounts, item serialization, existing
legacy tag names, obsolete cash ID rejection, ordinary item rejection, head
profiles and their serialization, and immature/mature crops in a live world.
The server was stopped after the checks. Minecraft authentication services were
unavailable from the test environment; no player login was tested.

ItemsAdder, actual player interactions, older server releases, items from a real
pre-upgrade server backup, and the external resource pack have not been tested.

Before deploying to an existing server, test with Vault and your economy
provider: plugin enablement, `/rm`, withdrawing and picking up currency,
existing currency from a backup, `cash_id` checks, head textures, and loot from
entities, fishing, and mature crops. Repeat with ItemsAdder if you use it.
