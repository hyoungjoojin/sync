declare global {
  interface Window {
    grecaptcha?: {
      ready: (callback: () => void) => void;
      execute: (
        siteKey: string,
        options: { action: string },
      ) => Promise<string>;
    };
  }
}

let scriptLoadingPromise: Promise<void> | null = null;

function loadCaptchaScript(siteKey: string): Promise<void> {
  if (window.grecaptcha) {
    return Promise.resolve();
  }

  scriptLoadingPromise ??= new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = `https://www.google.com/recaptcha/api.js?render=${siteKey}`;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () =>
      reject(new Error('Failed to load the captcha script.'));
    document.head.appendChild(script);
  });

  return scriptLoadingPromise;
}

export async function executeCaptcha(
  siteKey: string,
  action: string,
): Promise<string> {
  await loadCaptchaScript(siteKey);

  return new Promise((resolve, reject) => {
    window.grecaptcha?.ready(() => {
      window.grecaptcha?.execute(siteKey, { action }).then(resolve, reject);
    });
  });
}
