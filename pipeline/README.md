# Feed Curation Pipeline

This pipeline builds a curated IPTV feed for the CouchTV Fire TV app.

## What it does

- Pulls public metadata from IPTV-org APIs
- Keeps India channels only
- Prioritizes Hindi channels in output ordering
- Drops NSFW channels
- Writes:
  - `dist/channels.json` (app-first format)
  - `dist/playlist.m3u` (backup compatibility format)

## Run locally

```bash
cd pipeline
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python src/build_feed.py
```

## Notes

- This script does not validate legal rights itself; it applies filtering rules only.
- App-side health checks and stream fallback handling will be added in later chunks.
