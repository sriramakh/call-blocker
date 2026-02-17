# CallBlock – Android spam call blocker

CallBlock (in the `BlockNos` project) is a native Android app that lets you block unwanted calls using your own rules plus a pluggable spam-detection layer.

It is built as a clean, Material-themed call-screening app with a card-based UI and Room-backed persistence.

## Core features

- **Block by exact number** – add any E.164-style number (e.g. `+14085551234`) and every incoming call from it will be rejected.
- **Block by contact** – pick a contact from your phonebook and block all calls from that contact's number.
- **Block by prefix** – define prefixes (e.g. `+140`, `+1877`) to block whole ranges of numbers such as certain country/area codes.
- **Block using public spam information** – a `SpamChecker` abstraction lets you flag numbers as spam using a public spam dataset; the default implementation uses sample prefixes/numbers and can be swapped for a real data source.
- **Block from recent call log** – browse your device call log and block numbers directly from recent calls.
- **View and manage rules** – see all blocked numbers, prefixes, and contacts, and remove any rule with a single tap.
- **View blocked call history** – see when calls were blocked and why (number, time, and rule/spam reason).
- **Modern UI** – teal/green Material design, card-based layout, and tabbed navigation built around the included `style.md` guide.

## Screens & flows

The main activity is organized into four tabs:

1. **Manual Block**
   - Cards to:
     - Block a specific number.
     - Block a prefix.
     - Pick a contact to block.
   - Header shows a live summary like “3 numbers • 1 prefix • 2 contacts blocked”.

2. **Call Log Block**
   - Shows recent device calls (up to 50 entries) via `DeviceCallLogAdapter`.
   - For each call you can:
     - See number, optional contact name, call type (incoming/outgoing/missed/etc.), and timestamp.
     - Tap **Block** to add that number to the blocked list.

3. **Block Rule History**
   - Three lists for:
     - Blocked numbers.
     - Blocked prefixes.
     - Blocked contacts.
   - Uses `BlockedItemAdapter` to render each rule with a delete icon so you can quickly undo rules.

4. **Call Block History**
   - Shows the history of blocked calls using `CallLogAdapter`.
   - Each entry includes:
     - The phone number.
     - The reason (e.g. “Blocked number”, “Blocked prefix”, “Public spam database”).
     - A timestamp (formatted with a friendly date/time pattern).
   - Includes an action to clear the history.

Snackbars are used for quick confirmation messages (e.g. when a number/prefix/contact is blocked or removed).

## Architecture overview

**Modules & packages**

- `app/src/main/java/com/blocknos/data`
  - `BlockedNumberEntity`, `BlockedPrefixEntity`, `BlockedContactEntity` – Room entities for blocking rules.
  - `BlockedCallLogEntity` – Room entity for the history of blocked calls.
  - `BlockedRuleDao` – DAO for inserting/removing rules and reading rules + call history (both as `LiveData` and raw lists).
  - `AppDatabase` – Room database singleton (`blocknos.db`).
  - `SpamChecker` / `DefaultSpamChecker` – abstraction and default implementation for “public spam” classification.
  - `BlockRulesRepository` – core decision engine; evaluates an incoming number against:
    - Exact blocked numbers.
    - Blocked contacts.
    - Blocked prefixes.
    - `SpamChecker`.
    Returns a `BlockDecision` (`shouldBlock` + `reason`).

- `app/src/main/java/com/blocknos/call`
  - `SpamCallScreeningService` – extends `CallScreeningService` and:
    - Extracts the incoming phone number.
    - Asks `BlockRulesRepository` whether the call should be blocked.
    - If yes, responds with `setDisallowCall(true)` and `setRejectCall(true)`.
    - Logs each blocked call into `blocked_call_log` with the resolved reason.

- `app/src/main/java/com/blocknos/ui`
  - `MainActivity` – main entry point; sets up tabs, RecyclerViews, click-handlers, permissions, and the call-screening role request.
  - `MainViewModel` – wraps database access, exposes `LiveData` for rules and call logs, and loads the device call log via `CallLog.Calls`.
  - `BlockedItemAdapter` – list adapter for blocked rules (numbers/prefixes/contacts).
  - `CallLogAdapter` – list adapter for the blocked call history.
  - `DeviceCallLogAdapter` & `DeviceCallLogEntry` – list adapter/model for recent device calls with a “Block” action.

**Persistence & decision flow**

1. A user creates rules via the UI (manual input, contact picker, or device call log).
2. `MainViewModel` persists rules through `BlockedRuleDao` into Room tables.
3. On an incoming call, `SpamCallScreeningService`:
   - Normalizes the number.
   - Delegates to `BlockRulesRepository.evaluateNumber`.
   - If `shouldBlock == true`, rejects the call and records `BlockedCallLogEntity` with the reason.
4. The UI observes `LiveData` from Room to update lists and the header summary in real time.

## Permissions & call-screening role

The app requests these permissions (see `AndroidManifest.xml`):

- `READ_CALL_LOG` – to show recent device calls and evaluate history.
- `READ_CONTACTS` – to pick and block contacts from the phonebook.
- `ANSWER_PHONE_CALLS` – required for advanced call handling/rejecting.
- `READ_PHONE_STATE` – used by the call-screening service to inspect incoming calls.

Android also requires the app to hold the **Call Screening** role:

- On Android 10+ (`RoleManager`):
  - `MainActivity` requests `ROLE_CALL_SCREENING` at startup if available and not already granted.
  - You may still need to confirm in system settings that CallBlock is allowed to screen calls.
- On older supported versions (minSdk 26):
  - You may need to manually set this app as the default call screening app in the Phone / Call settings.

## Public spam database integration

The app is designed so that spam classification is pluggable:

- `DefaultSpamChecker` currently uses a small hardcoded list of prefixes and numbers as sample spam data.
- `SpamChecker` is an interface you can implement to:
  - Call a hosted spam API.
  - Query a locally cached spam database that you sync periodically.
  - Use any other public spam dataset.

To integrate a real spam source, implement `SpamChecker` and swap it into `SpamCallScreeningService` instead of `DefaultSpamChecker`.

## Building & running

Requirements:

- Android Studio (Giraffe/Koala+ recommended).
- JDK 17.
- Android device or emulator with API level **26+** (a real device with Android 10+ is strongly recommended to test real call blocking).

Steps:

1. **Clone the repo**
   ```bash
   git clone <your-remote-url> call-blocker
   cd call-blocker
   ```
2. **Open in Android Studio**
   - Choose “Open an existing project” and select this directory.
   - Let Gradle sync and download dependencies.
3. **Run the app**
   - Connect a device (or start an emulator).
   - Run the `app` module.
4. **Grant permissions & role**
   - On first launch, grant requested runtime permissions (contacts, call log, phone state).
   - Accept the system prompt to allow CallBlock to screen calls (or set it in system call settings).
5. **Test blocking**
   - Add a test number/prefix or block from the call log.
   - Call your test device from another phone using that number and verify that it is rejected and logged.

## Customizing & extending

- **UI & theming**
  - Colors, typography, and layout conventions are described in `style.md`.
  - Core color tokens live in `app/src/main/res/values/colors.xml`.
  - Theme configuration is in `app/src/main/res/values/themes.xml` (`Theme.BlockNos`).

- **Blocking rules**
  - Extend Room entities/DAO if you want more rule types (e.g., block by time-of-day, by country code, by unknown callers only).

- **Analytics & logging**
  - Add additional fields to `BlockedCallLogEntity` (e.g., duration, call direction) and display them via `CallLogAdapter`.

- **Platform variations**
  - You can adapt the same architecture for OEM-specific dialing apps or reuse the data layer in a multi-module project.

---

This README reflects the current state of the project after the latest call-blocking, history, and UI improvements. Update it as you evolve the spam data source or add new rule types.

