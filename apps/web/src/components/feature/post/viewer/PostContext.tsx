'use client';

import { ReactNode, createContext, useContext, useMemo, useState } from 'react';

export interface PostTocItem {
  id: string;
  level: number;
  itemIndex: number;
  textContent: string;
  isActive: boolean;
}

interface PostContextValue {
  toc: PostTocItem[];
  setToc: (items: PostTocItem[]) => void;
}

const PostContext = createContext<PostContextValue>({
  toc: [],
  setToc: () => {},
});

export function PostProvider({ children }: { children: ReactNode }) {
  const [toc, setToc] = useState<PostTocItem[]>([]);
  const value = useMemo(() => ({ toc, setToc }), [toc]);

  return <PostContext.Provider value={value}>{children}</PostContext.Provider>;
}

export function usePostContext() {
  return useContext(PostContext);
}
