/**
 * Generates a UUID v4 using the browser's crypto API (no dependency on a library).
 * Used to create idempotency keys for driver tracking updates.
 */
export function v4(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (crypto.getRandomValues(new Uint8Array(1))[0] & 0x0f) | (c === "y" ? 0x80 : 0);
    return (c === "x" ? r : r & 0x3f | 0x80).toString(16);
  });
}
