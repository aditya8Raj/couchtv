# CouchTV Fire TV App

Native Android TV app for Amazon Fire TV Stick.

## Stack

- Kotlin
- Jetpack Compose (TV-first UI in upcoming chunks)
- Media3 ExoPlayer
- Retrofit + OkHttp

## Current chunk status

- Base Android app scaffold
- Leanback launcher manifest setup
- Catalog list rendering from channel feed data
- Remote JSON feed loading with local asset fallback
- Basic click-to-play live playback screen with ExoPlayer

## Next chunks

- Improve D-pad focus behavior for TV-first navigation
- Add Favorites and Recently Watched persistence
- Add stream retry/fallback behavior in player
