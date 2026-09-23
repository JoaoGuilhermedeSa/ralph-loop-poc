import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

// Kept out of tsconfig's `include`, so the vite/vitest type mismatch cannot
// break `npm run build`. Scoped to the acceptance suite: the v1 verify.py runs
// plain `vitest run` and compares the count with tests/acceptance/expected.json.
// Unit tests run through vitest.unit.config.ts.
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./tests/setup.ts"],
    include: ["tests/acceptance/**/*.test.tsx"],
  },
});
