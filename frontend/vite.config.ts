import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: { "/api": "http://localhost:8080" } },
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./tests/setup.ts"],
    // Scoped to the acceptance suite only: verify.py runs plain `vitest run`
    // (no path filter) and compares the resulting test count to
    // tests/acceptance/expected.json, so any other *.test.tsx picked up by a
    // wider glob here would trip its TAMPERED guard. Unit tests live under
    // tests/unit/ and run via `vitest.unit.config.ts` instead.
    include: ["tests/acceptance/**/*.test.tsx"],
  },
});
