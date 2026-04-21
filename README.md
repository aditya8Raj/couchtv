# CouchTV

CouchTV is a Fire TV project focused on legal free-to-air live channels, starting with India and Hindi-priority curation.

## Current status

- Product/technical plan documented in `docs/PROJECT_PLAN.md`
- Legal policy documented in `docs/LEGAL_POLICY.md`
- Curated feed pipeline implemented in `pipeline/src/build_feed.py`
- Android TV Kotlin app scaffold created in `apps/firetv-android`
- App now loads channel catalog from remote feed endpoint with local asset fallback
- GitHub Action added for daily feed publish in `.github/workflows/publish-feed.yml`

## Run the feed pipeline locally

```bash
cd pipeline
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python src/build_feed.py
```

Output files:
- `pipeline/dist/channels.json`
- `pipeline/dist/playlist.m3u`

## Remote feed behavior in app

- App tries remote feed endpoints first, then falls back to local asset data.
- For live remote feed, enable GitHub Pages on the `gh-pages` branch after the workflow runs.
- If endpoints return 404, app will still boot using fallback sample channels.

## Test on Fire TV

- Follow the full APK build and install guide in `docs/TEST_ON_FIRETV.md`.

## Next implementation chunks

- Add ExoPlayer playback screen and channel switching
- Add Favorites + Recently Watched persistence
- Add stream health score and app-side fallback strategy
