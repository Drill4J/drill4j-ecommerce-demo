export function setCookie(name: string, value: string, options?: { maxAgeDays?: number }) {
  const maxAgeDays = options?.maxAgeDays ?? 1
  const maxAgeSeconds = Math.floor(maxAgeDays * 24 * 60 * 60)
  document.cookie = `${encodeURIComponent(name)}=${encodeURIComponent(value)}; Path=/; Max-Age=${maxAgeSeconds}; SameSite=Lax`
}

export function getCookie(name: string): string | null {
  const target = `${encodeURIComponent(name)}=`
  const parts = document.cookie.split(';')
  for (const part of parts) {
    const trimmed = part.trim()
    if (trimmed.startsWith(target)) {
      return decodeURIComponent(trimmed.substring(target.length))
    }
  }
  return null
}

export function deleteCookie(name: string) {
  document.cookie = `${encodeURIComponent(name)}=; Path=/; Max-Age=0; SameSite=Lax`
}

