# CouchTV Fire TV Project Plan

## 1) Technical Stack

### App (Fire TV / Android TV)

- Language: Kotlin
- UI: Jetpack Compose for TV
- Playback: Media3 ExoPlayer
- Local storage: Room
- Networking: Retrofit + OkHttp
- Architecture: MVVM + Repository pattern

### Data Pipeline

- Language: Python
- Source input: IPTV-org public channel and stream metadata
- Output format: `channels.json` (primary), `playlist.m3u` (backup)
- Hosting (zero-cost): GitHub Pages
- Automation (zero-cost): GitHub Actions (daily refresh + health checks)

## 2) Product Scope (Finalized)

- Region focus: India
- Channel categories: All
- Language priority: Hindi first
- Compliance: Legal/free-to-air streams only
- Parental lock: No
- User profiles: No
- Distribution model now: Sideload only
- Release direction later: Eventual public release path (while keeping legal-only policy)

## 3) EPG Decision

- MVP choice: Skip full EPG in v1.0
- Follow-up: Add EPG-lite (Now/Next where available) after playback stability

Reasoning:

- Free EPG data quality is inconsistent and can reduce reliability if introduced too early.

## 4) Phase-by-Phase Plan

### Phase 0: Product and Compliance Freeze (1-2 days)

Deliverables:

- Legal inclusion policy for channels/streams
- Device support matrix (Fire TV Stick variants)
- MVP features and explicit non-goals

Exit criteria:

- Approved scope with no open ambiguities

### Phase 1: Architecture and UX Blueprint (2-3 days)

Deliverables:

- Architecture design (modules, data flow, caching, error handling)
- TV-first navigation design for D-pad focus
- Feed API/data contracts

Exit criteria:

- Implementation-ready technical design

### Phase 2: IPTV Data Pipeline (4-6 days)

Deliverables:

- Ingest IPTV-org metadata
- Filter India channels and prioritize Hindi
- Stream health checks and dead-link pruning
- Publish curated `channels.json` + optional `playlist.m3u`
- Automate daily refresh with GitHub Actions

Exit criteria:

- Stable and continuously updated feed endpoint

### Phase 3: Fire TV App MVP (8-12 days)

Deliverables:

- Home/catalog screen with categories
- Search and filtering
- Channel detail + live playback
- Favorites + recently watched
- Robust D-pad focus handling

Exit criteria:

- Smooth browsing and reliable playback on real Fire TV hardware

### Phase 4: Playback Reliability and Performance (4-6 days)

Deliverables:

- Playback retry and fallback stream strategy
- Buffering/error state UX
- Startup and channel-switch optimizations

Exit criteria:

- Performance and stability targets met on target device

### Phase 5: EPG-lite and Public-readiness (optional v1.1, 3-5 days)

Deliverables:

- Now/Next program info where data exists
- Graceful fallback when EPG is unavailable
- Public docs: legal notice, issue reporting, takedown handling

Exit criteria:

- Public-facing quality for wider use

### Phase 6: Release and Operations (2-3 days)

Deliverables:

- Signed release APK
- Sideload installation and update guide
- Versioning + rollback procedure
- Feed health operations checklist

Exit criteria:

- Repeatable release + maintenance workflow

## 5) Target Metrics

- App cold start: under 4 seconds on Fire TV Stick
- Channel playback start (median): under 6 seconds on stable Wi-Fi
- Curated stream availability: above 90%
- Crash-free sessions: above 99%

## 6) Build Principles

- Work in small, testable chunks
- Prioritize playback reliability over feature breadth
- Keep legal compliance explicit in code, docs, and release artifacts
