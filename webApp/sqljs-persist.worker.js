import initSqlJs from 'sql.js';

const DB_NAME = 'myhub-sqljs';
const STORE_NAME = 'db_files';
const DB_KEY = 'main';

let db = null;
let transactionDepth = 0;
let actionQueue = Promise.resolve();
const DEBUG_TX = true;

function txLog(level, message, extra) {
  if (!DEBUG_TX) return;
  const payload = extra == null ? '' : ` ${JSON.stringify(extra)}`;
  const line = `[sqljs-persist-worker] ${message}${payload}`;
  if (level === 'error') {
    console.error(line);
  } else if (level === 'warn') {
    console.warn(line);
  } else {
    console.info(line);
  }
}

function openIndexedDb() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, 1);
    request.onupgradeneeded = () => {
      const idb = request.result;
      if (!idb.objectStoreNames.contains(STORE_NAME)) {
        idb.createObjectStore(STORE_NAME);
      }
    };
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function loadDatabaseBytes() {
  const idb = await openIndexedDb();
  return new Promise((resolve, reject) => {
    const tx = idb.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const request = store.get(DB_KEY);
    request.onsuccess = () => resolve(request.result ?? null);
    request.onerror = () => reject(request.error);
  });
}

async function saveDatabaseBytes(bytes) {
  const idb = await openIndexedDb();
  return new Promise((resolve, reject) => {
    const tx = idb.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const request = store.put(bytes, DB_KEY);
    request.onsuccess = () => resolve();
    request.onerror = () => reject(request.error);
  });
}

async function createDatabase() {
  const SQL = await initSqlJs({ locateFile: () => '/sql-wasm.wasm' });
  const bytes = await loadDatabaseBytes();
  db = bytes ? new SQL.Database(bytes) : new SQL.Database();
  txLog('info', 'database.ready', { loadedBytes: bytes ? bytes.length : 0 });
}

function isMutatingSql(sql) {
  const head = (sql || '').trim().toLowerCase();
  return /^(insert|update|delete|replace|create|drop|alter|vacuum|attach|detach|pragma)/.test(head);
}

function sqlHead(sql) {
  return (sql || '').trim().toLowerCase();
}

function safeExecTransactionSql(sql, actionId) {
  const head = sqlHead(sql);
  if (head.startsWith('begin')) {
    if (transactionDepth === 0) {
      db.exec('BEGIN TRANSACTION;');
    }
    transactionDepth += 1;
    txLog('info', 'exec.begin', { id: actionId, depth: transactionDepth });
    return [];
  }
  if (head.startsWith('commit') || head.startsWith('end')) {
    if (transactionDepth <= 0) {
      txLog('warn', 'exec.commit.noop', { id: actionId, depth: transactionDepth });
      return [];
    }
    transactionDepth -= 1;
    if (transactionDepth > 0) {
      txLog('info', 'exec.commit.nested', { id: actionId, depth: transactionDepth });
      return [];
    }
    try {
      const results = db.exec('END TRANSACTION;');
      txLog('info', 'exec.commit', { id: actionId, depth: transactionDepth });
      return results;
    } catch (error) {
      const message = String(error?.message ?? error);
      if (message.includes('no transaction is active')) {
        txLog('warn', 'exec.commit.no_active', { id: actionId, depth: transactionDepth });
        return [];
      }
      throw error;
    }
  }
  if (head.startsWith('rollback')) {
    if (transactionDepth <= 0) {
      txLog('warn', 'exec.rollback.noop', { id: actionId, depth: transactionDepth });
      return [];
    }
    transactionDepth = 0;
    try {
      const results = db.exec('ROLLBACK TRANSACTION;');
      txLog('info', 'exec.rollback', { id: actionId, depth: transactionDepth });
      return results;
    } catch (error) {
      const message = String(error?.message ?? error);
      if (message.includes('no transaction is active')) {
        txLog('warn', 'exec.rollback.no_active', { id: actionId, depth: transactionDepth });
        return [];
      }
      throw error;
    }
  }
  return null;
}

async function persistNow() {
  if (db == null) return;
  await saveDatabaseBytes(db.export());
}

async function persistSafely() {
  try {
    const bytes = db?.export();
    txLog('info', 'persist.start', { bytes: bytes?.length ?? 0, depth: transactionDepth });
    await persistNow();
    txLog('info', 'persist.done', { depth: transactionDepth });
  } catch (error) {
    console.error('Failed to persist sql.js database', error);
    throw error;
  }
}

async function onModuleReady(event) {
  const data = event.data;
  txLog('info', 'action.in', {
    id: data?.id,
    action: data?.action,
    depth: transactionDepth,
    sql: data?.sql?.slice?.(0, 80),
    params: data?.params,
  });
  switch (data && data.action) {
    case 'exec': {
      if (!data.sql) {
        throw new Error('exec: Missing query string');
      }
      const txResult = safeExecTransactionSql(data.sql, data.id);
      const result = (txResult ?? db.exec(data.sql, data.params))[0] ?? { values: [] };
      if (isMutatingSql(data.sql)) {
        if (transactionDepth == 0) {
          await persistSafely();
        } else {
          txLog('info', 'persist.deferred.in_transaction', { id: data.id, depth: transactionDepth });
        }
      }
      return postMessage({ id: data.id, results: result });
    }
    case 'begin_transaction':
      if (transactionDepth === 0) {
        db.exec('BEGIN TRANSACTION;');
      }
      transactionDepth += 1;
      txLog('info', 'action.out.begin_transaction', { id: data.id, depth: transactionDepth });
      return postMessage({ id: data.id, results: [] });
    case 'end_transaction':
      if (transactionDepth <= 0) {
        txLog('warn', 'action.out.end_transaction.noop', { id: data.id, depth: transactionDepth });
        return postMessage({ id: data.id, results: [] });
      }
      transactionDepth -= 1;
      if (transactionDepth === 0) {
        let results = [];
        try {
          results = db.exec('END TRANSACTION;');
        } catch (error) {
          const message = String(error?.message ?? error);
          if (message.includes('no transaction is active')) {
            txLog('warn', 'action.out.end_transaction.no_active', { id: data.id, depth: transactionDepth });
            results = [];
          } else {
            txLog('error', 'action.out.end_transaction.error', { id: data.id, depth: transactionDepth, message });
            throw error;
          }
        }
        await persistSafely();
        txLog('info', 'action.out.end_transaction', { id: data.id, depth: transactionDepth });
        return postMessage({ id: data.id, results });
      }
      txLog('info', 'action.out.end_transaction.nested', { id: data.id, depth: transactionDepth });
      return postMessage({ id: data.id, results: [] });
    case 'rollback_transaction':
      if (transactionDepth <= 0) {
        txLog('warn', 'action.out.rollback_transaction.noop', { id: data.id, depth: transactionDepth });
        return postMessage({ id: data.id, results: [] });
      }
      transactionDepth = 0;
      let rollbackResults = [];
      try {
        rollbackResults = db.exec('ROLLBACK TRANSACTION;');
      } catch (error) {
        const message = String(error?.message ?? error);
        if (message.includes('no transaction is active')) {
          txLog('warn', 'action.out.rollback_transaction.no_active', { id: data.id, depth: transactionDepth });
          rollbackResults = [];
        } else {
          txLog('error', 'action.out.rollback_transaction.error', { id: data.id, depth: transactionDepth, message });
          throw error;
        }
      }
      await persistSafely();
      txLog('info', 'action.out.rollback_transaction', { id: data.id, depth: transactionDepth });
      return postMessage({ id: data.id, results: rollbackResults });
    default:
      throw new Error(`Unsupported action: ${data && data.action}`);
  }
}

function onError(error) {
  txLog('error', 'action.error', {
    id: this?.data?.id,
    action: this?.data?.action,
    depth: transactionDepth,
    message: String(error?.message ?? error),
  });
  return postMessage({
    id: this?.data?.id,
    error,
  });
}

if (typeof importScripts === 'function' || typeof self !== 'undefined') {
  const sqlModuleReady = createDatabase();
  self.onmessage = (event) => {
    actionQueue = actionQueue
      .then(() => sqlModuleReady)
      .then(() => onModuleReady(event))
      .catch((error) => onError.call(event, error));
  };
}
