function base64UrlToUtf8(base64Url: string): string {
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  return decodeURIComponent(
    atob(padded)
      .split('')
      .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
      .join(''),
  )
}

export type JwtClaims = {
  userId?: string
  email?: string
  exp?: number
  iat?: number
  sub?: string
}

export function decodeJwtClaims(token: string): JwtClaims | null {
  const parts = token.split('.')
  if (parts.length < 2) {
    return null
  }
  try {
    const json = base64UrlToUtf8(parts[1]!)
    return JSON.parse(json) as JwtClaims
  } catch {
    return null
  }
}

