// Type definitions for Android interface

interface AndroidInterface {
  requestAccessibilityPermission(): Promise<boolean>;
  isAccessibilityServiceEnabled(): Promise<boolean>;
  clickElement(selector: string): Promise<boolean>;
  fillFormField(selector: string, text: string): Promise<boolean>;
  switchToApp(packageName: string): Promise<boolean>;
  getCurrentApp(): Promise<string>;
  longPressElement(selector: string): Promise<boolean>;
  swipe(startX: number, startY: number, endX: number, endY: number, duration: number): Promise<boolean>;
  findElements(selector: string): Promise<any[]>;
  openUrl(url: string): Promise<boolean>;
  storeSecureData(key: string, data: string): Promise<boolean>;
  getSecureData(key: string): Promise<string | null>;
  removeSecureData(key: string): Promise<boolean>;
}

interface Window {
  AndroidInterface?: AndroidInterface;
  androidAutomation?: import('../src/android-automation').default;
  wasmPieces?: import('../src/wasm-pieces').default;
  gmailPiece?: any;
 notionPiece?: any;
  authFlow?: import('../src/auth-flow').default;
}