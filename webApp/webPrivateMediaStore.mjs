const DB_NAME = "myhub-private-media";
const STORE_NAME = "capture_media";
const resolvedUrlCache = new Map();

// Keep the durable payload in IndexedDB as Blob, but resolve to data URLs.
// KMP Compose image loading on web/wasm is stable with data: URLs and not with blob: URLs.

function openDatabase() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: "key" });
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error || new Error("indexeddb_open_failed"));
  });
}

function requestToPromise(request) {
  return new Promise((resolve, reject) => {
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error || new Error("indexeddb_request_failed"));
  });
}

function transactionDone(transaction) {
  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => resolve();
    transaction.onerror = () => reject(transaction.error || new Error("indexeddb_tx_failed"));
    transaction.onabort = () => reject(transaction.error || new Error("indexeddb_tx_aborted"));
  });
}

function base64ToUint8Array(base64) {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

function revokeCachedUrl(key) {
  const url = resolvedUrlCache.get(key);
  if (!url) return;
  if (url.startsWith("blob:")) {
    URL.revokeObjectURL(url);
  }
  resolvedUrlCache.delete(key);
}

function blobToDataUrl(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ""));
    reader.onerror = () => reject(reader.error || new Error("blob_to_data_url_failed"));
    reader.readAsDataURL(blob);
  });
}

async function getRecord(key) {
  const db = await openDatabase();
  const transaction = db.transaction(STORE_NAME, "readonly");
  const store = transaction.objectStore(STORE_NAME);
  const result = await requestToPromise(store.get(key));
  await transactionDone(transaction);
  return result || null;
}

export async function putWebPrivateMedia(key, base64, mimeType) {
  const db = await openDatabase();
  const transaction = db.transaction(STORE_NAME, "readwrite");
  const store = transaction.objectStore(STORE_NAME);
  const bytes = base64ToUint8Array(base64);
  const blob = new Blob([bytes], { type: mimeType || "application/octet-stream" });
  const record = {
    key,
    blob,
    mimeType: mimeType || "application/octet-stream",
    sizeBytes: blob.size,
    createdAt: Date.now(),
  };
  await requestToPromise(store.put(record));
  await transactionDone(transaction);
  revokeCachedUrl(key);
  return resolveWebPrivateMediaUrl(key);
}

export async function copyWebPrivateMedia(sourceKey, targetKey) {
  const source = await getRecord(sourceKey);
  if (!source) return null;
  const db = await openDatabase();
  const transaction = db.transaction(STORE_NAME, "readwrite");
  const store = transaction.objectStore(STORE_NAME);
  const record = {
    key: targetKey,
    blob: source.blob,
    mimeType: source.mimeType || "application/octet-stream",
    sizeBytes: source.sizeBytes || source.blob?.size || 0,
    createdAt: Date.now(),
  };
  await requestToPromise(store.put(record));
  await transactionDone(transaction);
  revokeCachedUrl(targetKey);
  return resolveWebPrivateMediaUrl(targetKey);
}

export async function resolveWebPrivateMediaUrl(key) {
  const cached = resolvedUrlCache.get(key);
  if (cached) return cached;
  const record = await getRecord(key);
  if (!record || !record.blob) return null;
  const url = await blobToDataUrl(record.blob);
  resolvedUrlCache.set(key, url);
  return url;
}

export async function hasWebPrivateMedia(key) {
  const record = await getRecord(key);
  return !!record;
}

export async function deleteWebPrivateMedia(key) {
  const db = await openDatabase();
  const transaction = db.transaction(STORE_NAME, "readwrite");
  const store = transaction.objectStore(STORE_NAME);
  await requestToPromise(store.delete(key));
  await transactionDone(transaction);
  revokeCachedUrl(key);
}

export function putWebPrivateMediaCallback(key, base64, mimeType, onSuccess, onError) {
  putWebPrivateMedia(key, base64, mimeType).then(onSuccess).catch((error) => onError(String(error?.message || error || "put_failed")));
}

export function copyWebPrivateMediaCallback(sourceKey, targetKey, onSuccess, onError) {
  copyWebPrivateMedia(sourceKey, targetKey).then(onSuccess).catch((error) => onError(String(error?.message || error || "copy_failed")));
}

export function resolveWebPrivateMediaUrlCallback(key, onSuccess, onError) {
  resolveWebPrivateMediaUrl(key).then(onSuccess).catch((error) => onError(String(error?.message || error || "resolve_failed")));
}

export function hasWebPrivateMediaCallback(key, onSuccess, onError) {
  hasWebPrivateMedia(key).then(onSuccess).catch((error) => onError(String(error?.message || error || "exists_failed")));
}

export function deleteWebPrivateMediaCallback(key, onSuccess, onError) {
  deleteWebPrivateMedia(key).then(() => onSuccess()).catch((error) => onError(String(error?.message || error || "delete_failed")));
}
