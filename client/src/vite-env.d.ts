/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly PROD: boolean;
    readonly DEV: boolean;
    readonly MODE: string;
    readonly BASE_URL: string;
    readonly VITE_API_URL?: string;
    readonly VITE_API_BASE_URL?: string;
    readonly VITE_ZALO_APP_ID?: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}

interface Window {
    ZJSBridge?: unknown;
    APP_ID?: string;
    BASE_PATH?: string;
    APP_CONFIG?: unknown;
}
