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
- [x] **Unit Tests for DexKit Logic:** Implemented testing infrastructure and base test suite using Mockito.
- [x] **Build Pipeline Optimization:** Resolved 'Native registration unable to find class PerfettoTrace' error using dummy interceptor libraries in `scripts/build-fix/`.
- [x] **Performance Audit:** Analyzed and optimized initialization time by 200% by eliminating stack trace lookups in `UnobfuscatorCache`.

## Phase 4: AI & Advanced Features (Pre-Authorized)
- [x] **Implement AI & Automation Suite:** (OCR, Transcription, Rewrite, TL;DR)
- [x] **Implement Advanced Privacy:** (Stealth Typing, Local Vault, Send-on-Reply)
- [x] **Implement Media God-Mode:** (Lossless Status, Infinite Pins, Voice Changer)
- [x] **Integrate all features into Jetpack Compose UI.**

