---
type: "query"
date: "2026-07-20T13:09:10.936156+00:00"
question: "ddocj prj vaf cho tooi luomg chayj Auth"
contributor: "graphify"
outcome: "useful"
source_nodes: ["SecurityConfig", "LoginController", "StudentDetailsService", "AuthSessionService", "AuthTokenRepository"]
---

# Q: ddocj prj vaf cho tooi luomg chayj Auth

## Answer

Expanded from original query via graph vocab: authentication, logincontroller, securityfilterchain, httpsecurity, handleloginsuccess, authsessionservice, authtoken, authtokenrepository, userdetailsservice, passwordencoder, session, revokecurrentsessiontoken. Runtime auth flow is GET /login -> LoginController -> template -> POST /login -> Spring Security UsernamePasswordAuthenticationFilter -> StudentDetailsService -> UserRepository -> BCrypt password match -> SecurityContext and JSESSIONID -> role redirect. Logout is POST /logout -> Spring Security invalidates session, clears authentication, deletes JSESSIONID -> /login?logout. Important: AuthSessionService exists but is not wired into SecurityConfig, so handleLoginSuccess and revokeCurrentSessionToken do not run and auth_tokens SESSION rows are not created or revoked by the current login/logout flow.

## Outcome

- Signal: useful

## Source Nodes

- SecurityConfig
- LoginController
- StudentDetailsService
- AuthSessionService
- AuthTokenRepository