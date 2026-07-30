import { createEnv } from '@t3-oss/env-nextjs';
import z from 'zod';

export const env = createEnv({
  server: {},
  client: {
    NEXT_PUBLIC_BACKEND_URL: z.string().min(1),
    NEXT_PUBLIC_SITE_URL: z.string().url(),
    NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY: z.string().optional(),
  },
  runtimeEnv: {
    NEXT_PUBLIC_BACKEND_URL: process.env.NEXT_PUBLIC_BACKEND_URL,
    NEXT_PUBLIC_SITE_URL: process.env.NEXT_PUBLIC_SITE_URL,
    NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY:
      process.env.NEXT_PUBLIC_CHANNEL_TALK_PLUGIN_KEY,
  },
  skipValidation: process.env.SKIP_ENV_VALIDATION === 'true',
});
