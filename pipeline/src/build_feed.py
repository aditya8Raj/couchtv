#!/usr/bin/env python3
"""Build a curated India-first IPTV feed from IPTV-org public APIs.

This script produces:
- dist/channels.json (primary app feed)
- dist/playlist.m3u (backup playlist)
"""

from __future__ import annotations

import json
import pathlib
import sys
from collections import defaultdict
from typing import Any

import requests

CHANNELS_URL = "https://iptv-org.github.io/api/channels.json"
STREAMS_URL = "https://iptv-org.github.io/api/streams.json"
HINDI_M3U_URL = "https://iptv-org.github.io/iptv/languages/hin.m3u"

ROOT = pathlib.Path(__file__).resolve().parents[1]
DIST = ROOT / "dist"
CHANNELS_OUT = DIST / "channels.json"
M3U_OUT = DIST / "playlist.m3u"
TIMEOUT_SECONDS = 20


def fetch_json(url: str) -> list[dict[str, Any]]:
    response = requests.get(url, timeout=TIMEOUT_SECONDS)
    response.raise_for_status()
    data = response.json()
    if not isinstance(data, list):
        raise ValueError(f"Expected list JSON payload from {url}")
    return data


def fetch_text(url: str) -> str:
    response = requests.get(url, timeout=TIMEOUT_SECONDS)
    response.raise_for_status()
    return response.text


def parse_m3u_urls(m3u_content: str) -> set[str]:
    urls = set()
    for raw_line in m3u_content.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        urls.add(line)
    return urls


def is_hindi_channel(
    channel: dict[str, Any],
    channel_streams: list[dict[str, Any]],
    hindi_stream_urls: set[str],
) -> bool:
    if any(str(stream.get("url", "")) in hindi_stream_urls for stream in channel_streams):
        return True

    name = str(channel.get("name", "")).lower()
    return "hindi" in name


def pick_streams_by_channel(streams: list[dict[str, Any]]) -> dict[str, list[dict[str, Any]]]:
    grouped: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for stream in streams:
        channel_id = stream.get("channel")
        url = stream.get("url")
        if not channel_id or not url:
            continue

        grouped[channel_id].append(
            {
                "url": str(url),
                "title": stream.get("title"),
                "label": stream.get("label"),
                "quality": stream.get("quality"),
                "http_referrer": stream.get("referrer"),
                "user_agent": stream.get("user_agent"),
            }
        )
    return grouped


def filter_india_channels(channels: list[dict[str, Any]]) -> list[dict[str, Any]]:
    filtered = []
    for channel in channels:
        if channel.get("country") != "IN":
            continue
        if channel.get("is_nsfw"):
            continue
        filtered.append(channel)
    return filtered


def build_curated_feed(
    channels: list[dict[str, Any]],
    streams_by_channel: dict[str, list[dict[str, Any]]],
    hindi_stream_urls: set[str],
) -> list[dict[str, Any]]:
    curated = []
    for channel in channels:
        channel_id = channel.get("id")
        if not channel_id:
            continue

        channel_streams = streams_by_channel.get(channel_id, [])
        if not channel_streams:
            continue

        hindi_priority = is_hindi_channel(channel, channel_streams, hindi_stream_urls)

        curated.append(
            {
                "id": channel_id,
                "name": channel.get("name"),
                "logo": channel.get("logo"),
                "categories": channel.get("categories") or [],
                "country": channel.get("country"),
                "is_hindi_priority": hindi_priority,
                "streams": channel_streams,
            }
        )

    # Hindi-first sorting, then alphabetical for stable output.
    curated.sort(key=lambda c: (0 if c.get("is_hindi_priority") else 1, str(c.get("name", "")).lower()))
    return curated


def write_channels_json(curated_feed: list[dict[str, Any]]) -> None:
    DIST.mkdir(parents=True, exist_ok=True)
    CHANNELS_OUT.write_text(
        json.dumps({"version": 1, "channels": curated_feed}, indent=2),
        encoding="utf-8",
    )


def write_m3u(curated_feed: list[dict[str, Any]]) -> None:
    lines = ["#EXTM3U"]
    for channel in curated_feed:
        streams = channel.get("streams") or []
        if not streams:
            continue

        first_stream = streams[0]
        logo = channel.get("logo") or ""
        group = ",".join(channel.get("categories") or [])
        title = channel.get("name") or "Unknown"

        lines.append(
            f'#EXTINF:-1 tvg-id="{channel.get("id")}" tvg-name="{title}" tvg-logo="{logo}" group-title="{group}",{title}'
        )
        lines.append(str(first_stream.get("url", "")))

    DIST.mkdir(parents=True, exist_ok=True)
    M3U_OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    try:
        channels = fetch_json(CHANNELS_URL)
        streams = fetch_json(STREAMS_URL)
        hindi_m3u = fetch_text(HINDI_M3U_URL)
        hindi_stream_urls = parse_m3u_urls(hindi_m3u)

        india_channels = filter_india_channels(channels)
        streams_by_channel = pick_streams_by_channel(streams)
        curated_feed = build_curated_feed(india_channels, streams_by_channel, hindi_stream_urls)

        write_channels_json(curated_feed)
        write_m3u(curated_feed)

        hindi_count = sum(1 for c in curated_feed if c.get("is_hindi_priority"))
        print(f"Built curated feed with {len(curated_feed)} channels ({hindi_count} Hindi-priority).")
        print(f"JSON: {CHANNELS_OUT}")
        print(f"M3U:  {M3U_OUT}")
        return 0
    except Exception as exc:  # pylint: disable=broad-except
        print(f"Feed build failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
