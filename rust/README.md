# Windows native library

`windowsmediacontrol.dll` is built from this crate. CI and release builds compile it on a
Windows runner and replace the fallback copy in `src/main/resources/natives/windows-x64`
before Gradle packages the JAR.

To build the library locally on Windows:

```shell
cargo build --manifest-path rust/Cargo.toml --release --lib --locked
```

The output is `rust/target/release/windowsmediacontrol.dll`.

The fallback DLL remains in the repository for JitPack and local Java-only builds, which do
not run the native toolchain. Published GitHub release artifacts must always use the DLL
produced by the release workflow from the same commit.
