# Iterative roadmap

This order keeps every phase runnable and limits destructive rewrites.

## Phase 1 — working foundation (included)

- Home role and manifest
- App discovery and launch
- Searchable app drawer
- Clock/date, wallpaper, basic dock
- Theme and grid settings
- Package-change refresh

## Phase 1.5 — configurable glass system (included in 0.2)

- Shared `GlassSurface` for clock, dock, search, drawer, and app tiles
- Haze backdrop blur on Android 13+
- Translucent performance fallback on Android 12 and older
- DataStore persistence with migration from the version 0.1 preferences
- Tint picker; transparency, blur, and corner controls
- Light, Dark, and Auto glass styles
- Live preview and press animation

Refraction is intentionally not part of this phase. The optical-effect seam is ready, but the later implementation needs Android 13+ device profiling and a separate user control.

## Phase 2 — persistent home layout

- Room database for pages, cells, favorites, and placement
- Drag from drawer to home
- Drag/reorder and remove
- Screen-size-aware grid migration
- Persistent customizable dock

This is the first major architecture addition because a layout is structured data; Room is safer than saving JSON in preferences.

## Phase 3 — folders and gestures

- Drop app onto app to create folder
- Rename and reorder folders
- Swipe down, double tap, pinch, and configurable actions
- Gesture conflict and accessibility testing

## Phase 4 — widgets

- `AppWidgetHost`
- Widget picker/bind/configure flow
- Resize handles and span calculation
- Widget persistence, deletion, and restore

Widgets use classic Android Views supplied by other apps, so Compose/View interoperability will be isolated in a widget host component.

## Phase 5 — customization

- Icon packs and per-app icon override
- Shape, size, label, font, and color controls
- Multiple pages and transitions
- Hidden apps and app categories
- Import/export backup with schema versions

## Phase 6 — polish and release hardening

- Startup/profile optimization and baseline profiles
- Large app-list benchmarks
- Accessibility and TalkBack audit
- Work/private-profile behavior
- Crash recovery and safe-mode entry
- Automated UI/device tests
- Privacy policy and Play listing review

## Not recommended early

- Notification badges: requires notification-listener access and careful privacy UX.
- Accessibility-service shortcuts: powerful permission with significant trust and Play-policy implications.
- `QUERY_ALL_PACKAGES`: unnecessary for the current launcher list.
- Device-admin or root features: outside normal launcher scope.
