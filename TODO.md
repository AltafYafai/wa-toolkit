# Whatsapp Toolkit TODO

## Phase 1: Core Refactoring & Resiliency
- [x] **Complete Unobfuscator Refactor:** Finish transitioning all hardcoded class and method lookups to the DexKit-based dynamic discovery system.
- [x] **Eliminate Hardcoded Obfuscated Names:** Identify and replace all remaining occurrences of `A00`, `A01`, etc., with dynamic logic in `Unobfuscator.java`.
- [x] **W4B (WhatsApp Business) Compatibility:** Exhaustively test and fix any remaining `ClassNotFoundException` issues specific to the WhatsApp Business variant.

## Phase 2: Feature Enhancements
- [x] **Smart Reply Updates:** Verify and update Smart Reply UI hooks to ensure they work across latest WA/WB versions.
- [x] **Status Enhancements:** Review `StatusDownload` and related features for potential improvements using the new dynamic discovery logic.
- [x] **Separate Group Improvements:** Refine the tab filtering logic to handle edge cases in group/broadcast identification.

## Phase 3: Stability & Testing
- [/] **Unit Tests for DexKit Logic:** Implemented testing infrastructure and base test suite using Mockito. (Local execution blocked by build env issues).
- [ ] **Build Pipeline Optimization:** Investigate and resolve build environment issues (e.g., native class registration errors) to ensure consistent local and CI builds.
- [x] **Performance Audit:** Analyzed and optimized initialization time by eliminating stack trace lookups in `UnobfuscatorCache`.

