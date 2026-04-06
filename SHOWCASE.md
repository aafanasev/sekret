# The Logger That Leaked Everything

A real-world case for compile-time secret masking in Kotlin.

---

## The Architecture

A mobile application built around a **Finite State Machine (FSM)**. Clean, testable, and easy to reason about. Every user interaction was an `Event`. Every screen state was a `State`. Every side effect — navigation, network call, analytics ping — was an `Effect`.

The interface was deceptively simple:

```kotlin
interface StateMachine<State, Event, Effect> {
    val state: Flow<State>
    val effects: Flow<Effect>
    fun dispatch(event: Event)
}
```

Events in. States and effects out. That is the entire contract.

Because the interface was this clean, decorating it was trivial. You could wrap any `StateMachine` and intercept everything flowing through it without the underlying implementation knowing.

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Application Layer                           │
│                                                                     │
│   User Action                                                       │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────┐               │
│  │              LoggingDecorator                   │               │
│  │                                                 │               │
│  │   dispatch(event) {                             │               │
│  │       log("→ EVENT: $event")    ← prints ALL    │               │
│  │       inner.dispatch(event)        fields       │               │
│  │   }                                             │               │
│  │                                                 │               │
│  │   state.onEach { s ->                           │               │
│  │       log("◆ STATE: $s")        ← prints ALL    │               │
│  │   }                                fields       │               │
│  │                                                 │               │
│  │   effects.onEach { e ->                         │               │
│  │       log("← EFFECT: $e")       ← prints ALL    │               │
│  │   }                                fields       │               │
│  └──────────────────┬──────────────────────────────┘               │
│                     │                                               │
│                     ▼                                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                   Core StateMachine                          │  │
│  │                                                              │  │
│  │  ┌──────────────────────┐   ┌──────────────────────────┐    │  │
│  │  │    AuthSubFSM        │   │    PaymentSubFSM          │    │  │
│  │  │                      │   │                           │    │  │
│  │  │  State:              │   │  State:                   │    │  │
│  │  │   Idle               │   │   Idle                    │    │  │
│  │  │   EnteringCredentials│   │   EnteringCard            │    │  │
│  │  │   Authenticating     │   │   ProcessingPayment       │    │  │
│  │  │   Authenticated      │   │   PaymentComplete         │    │  │
│  │  └──────────────────────┘   └──────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## The Data

Events, states, and effects were Kotlin data classes. Concise, immutable, and — critically — equipped with auto-generated `toString()` methods that faithfully printed every field.

```kotlin
// Events dispatched by the UI
sealed class AuthEvent {
    data class LoginSubmitted(
        val username: String,
        val password: String   // ← plaintext, right here
    ) : AuthEvent()

    data class TwoFactorEntered(
        val code: String
    ) : AuthEvent()
}

// States held in memory and observed by the UI
sealed class AuthState {
    object Idle : AuthState()

    data class EnteringCredentials(
        val username: String,
        val password: String,  // ← updated on every keystroke
        val error: String?
    ) : AuthState()

    data class Authenticated(
        val userId: String,
        val sessionToken: String,  // ← session token in the state tree
        val refreshToken: String   // ← and the refresh token too
    ) : AuthState()
}

// Effects produced by the state machine
sealed class AuthEffect {
    data class NavigateToHome(val sessionToken: String) : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}
```

The payment flow was no different:

```kotlin
sealed class PaymentEvent {
    data class CardDetailsEntered(
        val cardNumber: String,   // ← 16-digit PAN
        val cvv: String,          // ← CVV
        val expiryMonth: Int,
        val expiryYear: Int,
        val cardholderName: String
    ) : PaymentEvent()
}

sealed class PaymentState {
    data class EnteringCard(
        val cardNumber: String,   // ← updated on every keystroke
        val cvv: String,
        val expiryMonth: Int,
        val expiryYear: Int,
        val cardholderName: String,
        val billingAddress: Address
    ) : PaymentState()
}
```

---

## The Loggers

The `LoggingDecorator` was written once and reused everywhere. It was six lines. Everyone loved it.

Different environments, different logger backends — all wired the same way:

```kotlin
class LoggingStateMachine<S, E, Ef>(
    private val inner: StateMachine<S, E, Ef>,
    private val logger: Logger
) : StateMachine<S, E, Ef> by inner {

    override fun dispatch(event: E) {
        logger.log("→ EVENT  | $event")   // data class toString()
        inner.dispatch(event)
    }

    init {
        inner.state.onEach { state ->
            logger.log("◆ STATE  | $state")  // data class toString()
        }.launchIn(scope)

        inner.effects.onEach { effect ->
            logger.log("← EFFECT | $effect") // data class toString()
        }.launchIn(scope)
    }
}
```

The `logger` swapped out per environment:

| Environment | Backend |
|---|---|
| Local development | `println` / file logger |
| Android debug | `Logcat` |
| Staging / QA | Firebase Crashlytics |
| Production | Datadog, Splunk, or a custom ingest pipeline |

The decorator itself never changed. The sensitive data flowed into every one of those sinks.

---

## What the Logs Actually Said

Here is what a typical session looked like in a log aggregator, visible to any engineer with read access:

```
→ EVENT  | EnteringCredentials(username=alice@example.com, password=, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=h, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hu, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hun, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hunt, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hunte, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hunter, error=null)
→ EVENT  | EnteringCredentials(username=alice@example.com, password=hunter2, error=null)
◆ STATE  | Authenticated(userId=usr_8821, sessionToken=eyJhbGci...full.jwt.token, refreshToken=rt_xK9m...)
← EFFECT | NavigateToHome(sessionToken=eyJhbGci...full.jwt.token)

→ EVENT  | CardDetailsEntered(cardNumber=4111111111111111, cvv=737, expiryMonth=9, expiryYear=2027, cardholderName=Alice Smith)
◆ STATE  | EnteringCard(cardNumber=4111111111111111, cvv=737, expiryMonth=9, expiryYear=2027, cardholderName=Alice Smith, billingAddress=Address(street=123 Main St, city=...))
```

Every keystroke. Every token. Every card number. All indexed, searchable, retained.

---

## The Blast Radius

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Log Aggregation Pipeline                           │
│                                                                              │
│  App Instance 1 ──┐                                                          │
│  App Instance 2 ──┼──► Ingest API ──► Index ──► Retention (90 days–7 years) │
│  App Instance N ──┘         │                         │                     │
│                             │                         ▼                     │
│                      ┌──────┴───────┐         ┌──────────────┐             │
│                       Who can read?            │  Breach path │             │
│                      └──────┬───────┘         └──────┬───────┘             │
│                             │                        │                      │
│              ┌──────────────┼──────────────┐         │                      │
│              ▼              ▼              ▼         ▼                      │
│         All engineers   QA team       3rd-party   Insider   External        │
│         with log        with staging  vendor      threat    attacker        │
│         access          access        (SaaS)                (if breached)   │
└──────────────────────────────────────────────────────────────────────────────┘
```

The exposure was not a single misconfigured endpoint. It was **structural**:

- **Every engineer** with access to the log aggregator could search for any user's credentials.
- **QA environments** received real production-mirrored traffic in some configurations — including real card numbers entered during testing.
- **Third-party SaaS tools** (Crashlytics, Datadog, Splunk) received the raw log lines as part of their ingestion contracts. That data was now in their infrastructure, subject to their retention and breach policies.
- **The data was indexed and searchable.** This was not noise — it was structured, labeled, and trivial to query.

A single `grep password` across the log store returned years of user credentials.

---

## Why the Obvious Fixes Don't Work

### "Just override `toString()`"

Fragile. The override must be manually written, manually maintained, and manually updated every time a new field is added. One forgotten field in a code review is another breach.

```kotlin
// Added today, forgotten tomorrow
data class AuthState.Authenticated(
    val userId: String,
    val sessionToken: String,
    val refreshToken: String,
    val newField: String  // ← no one updated toString()
)
```

### "Use a wrapper type"

A `Secret<T>` wrapper with a masked `toString()` is better — but it is still opt-in. Every developer must remember to use it. Every code review must catch when it is missing. The wrapper does not prevent a developer from extracting the raw value and logging that instead.

### "Disable logging in production"

This eliminates your observability. When something breaks in production, you have no data. And staging, QA, and development still log — those environments have their own breach surfaces.

### "Audit the data classes periodically"

This requires someone to own the process, run the audit, and have zero misses. It does not scale. The auto-generated `toString()` is invisible in source — there is nothing to grep for. Fields are added across dozens of PRs by dozens of engineers.

---

## The Structural Solution

The only fix that does not rely on human discipline is one enforced by the compiler.

```kotlin
sealed class AuthEvent {
    data class LoginSubmitted(
        val username: String,
        @Secret val password: String
    ) : AuthEvent()
}

sealed class AuthState {
    data class Authenticated(
        val userId: String,
        @Secret val sessionToken: String,
        @Secret val refreshToken: String
    ) : AuthState()
}

data class PaymentEvent.CardDetailsEntered(
    @Secret val cardNumber: String,
    @Secret val cvv: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cardholderName: String
)
```

The log output becomes:

```
→ EVENT  | LoginSubmitted(username=alice@example.com, password=■■■)
◆ STATE  | Authenticated(userId=usr_8821, sessionToken=■■■, refreshToken=■■■)
← EFFECT | NavigateToHome(sessionToken=■■■)

→ EVENT  | CardDetailsEntered(cardNumber=■■■, cvv=■■■, expiryMonth=9, expiryYear=2027, cardholderName=Alice Smith)
```

The `LoggingDecorator` is unchanged. The logger backends are unchanged. The FSM is unchanged. The only change is a single annotation per sensitive field.

**The guarantee is structural, not behavioral.** The `@Secret` annotation rewrites the `toString()` method in the compiled bytecode. There is no runtime path through which the value can surface. No one needs to remember to apply a wrapper. No audit process needs to catch a forgotten override. Adding a new field to a data class without `@Secret` is the only action that could reintroduce exposure — and that is a deliberate, visible, reviewable change.

---

## Summary

| Approach | Relies on human discipline | Survives refactoring | Zero runtime overhead | Works with all loggers |
|---|:---:|:---:|:---:|:---:|
| Manual `toString()` override | Yes | No | Yes | Yes |
| Wrapper type (`Secret<T>`) | Yes | Partial | No | Partial |
| Disable logging | N/A | N/A | N/A | N/A |
| **Sekret compiler plugin** | **No** | **Yes** | **Yes** | **Yes** |

[See the full documentation and installation guide →](README.md)
