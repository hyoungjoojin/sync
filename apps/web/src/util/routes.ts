const ROUTES = {
  HOME: () => '/',
  ABOUT: () => '/about',
  TERMS: () => '/terms',
  PRIVACY: () => '/privacy',
  EXPLORE: () => '/explore',
  LOGIN: () => '/auth/login',
  REGISTER: () => '/auth/register',
  POST: (slug: string) => `/posts/${slug}`,
  NEW_POST: () => `/posts/new`,
  PROJECT: (handle: string) => `/projects/${handle}`,
  PROJECT_POST: (projectHandle: string, postHandle: string) =>
    ROUTES.PROJECT(projectHandle) + `/posts/${postHandle}`,
  NEW_PROJECT_POST: (handle: string) => ROUTES.PROJECT(handle) + '/posts/new',
  PROJECT_MEMBERS: (handle: string) => ROUTES.PROJECT(handle) + '/members',
  PROJECT_SETTINGS: (handle: string) => ROUTES.PROJECT(handle) + '/settings',
};

export default ROUTES;
