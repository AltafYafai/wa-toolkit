# Whatsapp Toolkit TODO

## Phase 1: Core Refactoring & Resiliency
- [ ] **Complete Unobfuscator Refactor:** Finish transitioning all hardcoded class and method lookups to the DexKit-based dynamic discovery system.
- [ ] **Eliminate Hardcoded Obfuscated Names:** Identify and replace all remaining occurrences of `A00`, `A01`, etc., with dynamic logic in `Unobfuscator.java`.
- [ ] **W4B (WhatsApp Business) Compatibility:** Exhaustively test and fix any remaining `ClassNotFoundException` issues specific to the WhatsApp Business variant.

## Phase 2: Feature Enhancements
- [ ] **Smart Reply Updates:** Verify and update Smart Reply UI hooks to ensure they work across latest WA/WB versions.
- [ ] **Status Enhancements:** Review `StatusDownload` and related features for potential improvements using the new dynamic discovery logic.
- [ ] **Separate Group Improvements:** Refine the tab filtering logic to handle edge cases in group/broadcast identification.

## Phase 3: Stability & Testing
- [ ] **Unit Tests for DexKit Logic:** Implement tests to verify the accuracy of dynamic discovery across different WA/WB DEX files.
- [ ] **Build Pipeline Optimization:** Investigate and resolve build environment issues (e.g., native class registration errors) to ensure consistent local and CI builds.
- [ ] **Performance Audit:** Analyze the impact of dynamic discovery on module initialization time and optimize if necessary.
