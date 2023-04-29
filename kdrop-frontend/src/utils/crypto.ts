export async function encryptFile(file: File, key: string) {
  const uint8Array = await readFile(file);
  const encryptedData = await encrypt(uint8Array, key);
  const encryptedFile = new File([encryptedData], file.name, {
    type: file.type,
  });
  return encryptedFile;
}

export async function decryptFile(file: File, key: string) {
  const uint8Array = await readFile(file);
  const decryptedData = await decrypt(uint8Array, key);
  const decryptedFile = new File([decryptedData], file.name, {
    type: file.type,
  });
  return decryptedFile;
}

export function readFile(file: File): Promise<Uint8Array> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      if (reader.result) {
        resolve(new Uint8Array(reader.result as ArrayBuffer))
      }
    };
    reader.onerror = reject;
    reader.readAsArrayBuffer(file);
  });
}

const getImportedKey = async (key: string) => {
  key = key.padEnd(16, "a");
  const encoder = new TextEncoder()
  const keyData = encoder.encode(key);
  const importedKey = await window.crypto.subtle.importKey(
    "raw",
    keyData,
    { name: "AES-CBC" },
    false,
    ["encrypt", "decrypt"]
  );
  return importedKey
}

export async function encrypt(data: Uint8Array, key: string) {
  const importedKey = await getImportedKey(key);
  const iv = new Uint8Array(16).fill(0)
  const encryptedData = await window.crypto.subtle.encrypt(
    {
      name: "AES-CBC",
      iv: iv,
    },
    importedKey,
    data
  );
  return new Uint8Array(encryptedData);
}

export async function decrypt(data: Uint8Array, key: string) {
  const importedKey = await getImportedKey(key);
  const iv = new Uint8Array(16).fill(0)
  const decryptedData = await window.crypto.subtle.decrypt(
    {
      name: "AES-CBC",
      iv: iv,
    },
    importedKey,
    data
  );
  return new Uint8Array(decryptedData);
}
