export interface ZaloMiniAppProfile {
  id: string;
  oaUserId?: string;
  name?: string;
  avatar?: string;
  followedOa?: boolean;
}

export function isZaloMiniAppRuntime() {
  return typeof window !== 'undefined' && Boolean(window.APP_ID || window.ZJSBridge);
}

export async function loadZaloProfile(): Promise<ZaloMiniAppProfile | null> {
  if (!isZaloMiniAppRuntime()) {
    return null;
  }

  const sdk = await import('zmp-sdk/apis');

  let id = '';
  try {
    id = await sdk.getUserID({});
  } catch (error) {
    console.warn('Unable to read Zalo user ID', error);
  }

  try {
    const { userInfo } = await sdk.getUserInfo({
      autoRequestPermission: true,
    });

    return {
      id: userInfo.id || id,
      oaUserId: userInfo.idByOA,
      name: userInfo.name,
      avatar: userInfo.avatar,
      followedOa: userInfo.followedOA,
    };
  } catch (error) {
    if (!id) {
      console.warn('Unable to read Zalo profile', error);
      return null;
    }

    return { id };
  }
}
