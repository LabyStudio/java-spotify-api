# Windows native library

`windowsmediacontrol.dll` is built from this crate. The DLL is generated during CI, release,
and JitPack builds; it is not stored in Git.

To build the library locally on Windows:

```shell
cargo build --manifest-path rust/Cargo.toml --release --lib --locked
```

The output is `rust/target/release/windowsmediacontrol.dll`.

Pass the result to Gradle when packaging locally:

```shell
./gradlew build -PwindowsNativeDll=rust/target/release/windowsmediacontrol.dll
```

Linux/JitPack builds use pinned `cargo-xwin` and place the cross-compiled DLL under
`rust/target/x86_64-pc-windows-msvc/release` instead. Gradle copies the supplied DLL into a
generated resource directory before packaging it, so source-controlled resources never
contain a native binary.
