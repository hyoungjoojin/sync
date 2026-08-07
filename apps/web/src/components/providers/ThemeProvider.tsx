import { ThemeProvider as NextThemeProvider } from 'next-themes';

import { getUserPreferences } from '@/api/__generated__/preferences/preferences';

interface ThemeProviderProps {
  children?: React.ReactNode;
}

export default async function ThemeProvider({ children }: ThemeProviderProps) {
  // 비로그인 사용자는 401이므로 조회 실패는 정상 흐름이다.
  const { data } = await getUserPreferences().catch(() => ({
    data: { theme: 'system' },
  }));

  return (
    <NextThemeProvider
      attribute="class"
      defaultTheme={data.theme || 'system'}
      storageKey="sync-theme"
    >
      {children}
    </NextThemeProvider>
  );
}
