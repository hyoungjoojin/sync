import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vitest/config';

const dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(dirname, 'src'),
    },
  },
  test: {
    environment: 'node',
    include: ['src/**/*.test.ts'],
    env: {
      NEXT_PUBLIC_SITE_URL: 'https://sync.example.com',
      NEXT_PUBLIC_BACKEND_URL: 'http://localhost:8080',
    },
  },
});
