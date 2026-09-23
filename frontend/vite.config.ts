import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

// Dev server and production build only; the test setup is in vitest.config.ts.
// A `test` block here failed `npm run build` (TS2769): vitest 2 bundles its own
// vite, and its types do not match this project's vite 6.
//
// API_URL points the dev proxy at the backend (default :8080). demo.ps1 sets it.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, ".", "");
  return {
    plugins: [react()],
    server: { port: 5173, proxy: { "/api": env.API_URL || "http://localhost:8080" } },
  };
});
