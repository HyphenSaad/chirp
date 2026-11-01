# lib/

Place the Chirp JAR here.

## Where to get it

Download **`chirp-1.0.0.jar`** from the
[latest release](https://github.com/hyphensaad/chirp/releases/latest).

## Expected file structure

```
lib/
└── chirp-1.0.0.jar
```

That is the **fat JAR** — it includes JavaFX for the current platform, so
you have everything needed for both data modules and desktop UI. No
additional downloads.

## Verify

After adding the JAR:

1. Open `src/Main.java` in VS Code.
2. The import `import static chirp.Chirp.*;` should resolve without any
   red squiggles.
3. Press **F5** — you should see the Chirp banner.

## If it doesn't resolve

Check `.vscode/settings.json` contains:

```json
"java.project.referencedLibraries": ["lib/**/*.jar"]
```

Then reload VS Code:

**Ctrl+Shift+P → Developer: Reload Window**

## Updating Chirp

To upgrade to a newer version:

1. Delete the old JAR.
2. Download the new JAR.
3. Place it in this folder.
4. Reload VS Code.

The wildcard classpath pattern picks up any new JAR automatically.
