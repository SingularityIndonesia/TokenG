# TokenG

A Kotlin library for generating structured, signable tokens. TokenG handles token construction and encoding — signing is intentionally left to the caller, so you retain full authority over how tokens are authenticated.

The generation flow is a 3-step procedure: **generate** an unsigned token, **sign** it with your own mechanism, then **encode** it to a string using a built-in or custom encoder.

---

## The Standard Token

A token in TokenG is composed of two parts: **Info** and a **Signature**.

A token starts as an `UnsignedToken` after generation and becomes a `Token` only after signing. This is enforced at the type level — an unsigned token cannot be encoded.

```
UnsignedToken               Token
├── info: TokenInfo   ───►  ├── info: TokenInfo
                            └── signature: String

TokenInfo
├── purpose: String
├── issuer: String
├── subject: String
├── acknowledgements: List<String>
├── issuedAt: Instant
├── createdAt: Instant          (internal — auto-stamped by the system)
├── expiresAt: Instant?
├── scope: List<String>
├── metadata: Map<String, String>
└── nonce: String?
```

### Parts

#### Info (`TokenInfo`)
Carries all the descriptive metadata of the token — who issued it, who it's for, what it's for, and when.

| Field | Description |
|---|---|
| `purpose` | Why the token was issued. e.g. `"payment-authorization"` |
| `issuer` | The service or entity that issued the token. e.g. `"auth-service"` |
| `subject` | Who the token is for. e.g. a user ID or device ID |
| `acknowledgements` | Parties acknowledging this token. Free-form — can carry a signature and timestamp: `"service-a <signature> <timestamp>"` |
| `issuedAt` | When the token **request** was made. Intentionally set by the caller |
| `createdAt` | When the token was **actually generated**. Auto-stamped internally — callers cannot set this |
| `expiresAt` | When the token expires. `null` means it does not expire |
| `scope` | Actions or resources this token grants access to. e.g. `["payment:write"]` |
| `metadata` | Arbitrary key-value pairs for app-specific claims |
| `nonce` | *(Optional)* A unique value that complements the signature. The combination of `nonce` and `signature` is what makes a token truly unique. If your signature is already unique per issuance, `nonce` may not be needed. If your signature is static or expensive to compute, `nonce` can carry the uniqueness and be used for lightweight internal validation instead |

#### Signature (`String`)
The value that authenticates this token. Signing is the **caller's responsibility** — TokenG does not sign tokens itself.

The signature can be anything that makes the token trustworthy and hard to forge. Some approaches:

- **Cryptographic signature** — e.g. HMAC-SHA256 or RSA over the serialized `TokenInfo`. This is the most secure option as it binds the signature to the token content, making tampering detectable.
- **Unique random value** — e.g. a UUID or secure random string stored server-side. Simple and effective if you have a validation store.
- **Content hash** — e.g. SHA-256 of the token info. Deterministic and unique per content, but does not prove authenticity on its own without a secret key.

Regardless of approach, the signature **should be unique per issuance**. Using `nonce` or `issuedAt` in your signing input helps ensure this.

---

## Methods

Token generation follows a 3-step procedure, all via the `TokenG` object.

- `TokenG.generate(tokenInfo: TokenInfo): UnsignedToken` 

  Generate unsigned token, ex:

  ```kotlin
  val token = TokenG.generate(info)
  ```

- `TokenG.sign(token: UnsignedToken, signature: String): Token`

  Attaches a signature to the token. Signing is the caller's responsibility — provide a signature derived from your own mechanism.
  
  ```kotlin
  val signed = TokenG.sign(token, mySignature)
  ```

- `TokenG.encode(token: Token, encoder: TokenEncoder): String`

  Encodes the token into a string using the provided encoder. Returns the final token string ready for use.
  
  ```kotlin
  val result = TokenG.encode(signed, Base64JsonEncoder)
  ```

## Encoders

Built-in encoders and their symmetric decoders are available under `com.singularity_universe.tokeng.encoder`:

| Encoder | Decoder | Output | Description |
|---|---|---|---|
| `JsonEncoder` | `JsonDecoder` | Raw JSON string | Simplest option — no encoding layer. Good for internal use or debugging |
| `Base64JsonEncoder` | `Base64JsonDecoder` | Base64URL string | JSON serialized then Base64URL-encoded. Compact and URL-safe |
| `JwtEncoder(algorithm)` | `JwtDecoder` | JWT (`header.payload.signature`) | Standard JWT format. Use `JwtEncoder.signingInput()` to get the string to sign before calling `TokenG.sign()` |

You can also provide your own encoder by implementing `TokenEncoder`:

```kotlin
val customEncoder = TokenEncoder { token -> "my-format:${token.signature}" }
```
