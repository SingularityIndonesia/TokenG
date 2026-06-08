# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew compileKotlin        # compile only
./gradlew test                 # run all tests
./gradlew test --tests "com.singularity_universe.tokeng.TokenGTest"          # run a single test class
./gradlew test --tests "com.singularity_universe.tokeng.encoder.JwtEncoderTest.signingInput*"  # run a single test
```

## Publishing

Publishing targets **Maven Central** via the Sonatype Central Portal OSSRH Staging API.

**Prerequisites:**
- GPG key configured in `~/.gradle/gradle.properties` (`signing.keyId`, `signing.password`, `signing.secretKeyRingFile`)
- Central Portal User Tokens in `local.properties` (never commit this file — it is gitignored):
  ```
  TOKENG_SONATYPE_TokenG_Project_USERNAME=<token username>
  TOKENG_SONATYPE_TokenG_Project_PASSWORD=<token password>
  ```
  Generate tokens at central.sonatype.com → Account → Generate User Token.

**Steps:**

1. Publish artifacts:
   ```bash
   ./gradlew publishMavenKotlinPublicationToCentralPortalRepository
   ```

2. Trigger visibility in the Central Portal (run from the same machine/IP as the upload):
   ```bash
   curl -X POST \
     -u "<username>:<password>" \
     "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.singularity-universe"
   ```

3. Log in to central.sonatype.com → **Deployments** → review → click **Publish**.

## Architecture

TokenG is a Kotlin/JVM library (Gradle, Kotlin 2.3, `kotlinx-serialization-json`). All source lives under `com.singularity_universe.tokeng`.

### Token lifecycle

Generation follows a strict 3-step type-safe flow enforced at compile time:

```
TokenG.generate(TokenInfo)  →  UnsignedToken
TokenG.sign(UnsignedToken, signature)  →  Token
TokenG.encode(Token, TokenEncoder)  →  String
```

An `UnsignedToken` cannot be passed to `encode()` — the type system prevents it.

### Entity hierarchy (`entity/`)

```
BaseToken (sealed)
├── UnsignedToken(info: TokenInfo)
└── Token(info: TokenInfo, signature: String)
```

`TokenInfo` holds all token metadata. `createdAt` is `internal` and auto-stamped by `TokenG.generate()` — callers set only `issuedAt`.

### Encoders (`encoder/`)

- `Base64JsonEncoder` — object, encodes all `TokenInfo` fields + `signature` into a flat JSON object, then Base64URL-encodes it (no padding)
- `JwtEncoder(algorithm)` — class, produces `base64url(header).base64url(payload).signature`. Exposes `signingInput(UnsignedToken): String` which returns `base64url(header).base64url(payload)` — the caller must sign this string and pass the result to `TokenG.sign()` before encoding

Custom encoders implement `TokenEncoder` (`fun interface { encode(token: Token): String }`).

### Signing contract

TokenG does not sign tokens. The `signature: String` on `Token` is always caller-provided. For `JwtEncoder`, the signature must be computed over `encoder.signingInput(unsignedToken)` to produce a valid JWT.
