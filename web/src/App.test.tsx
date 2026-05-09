import { describe, it, expect } from 'vitest'

describe('BeautyStock App', () => {
  it('should pass basic sanity check', () => {
    expect(true).toBe(true)
  })

  it('should verify app name', () => {
    const appName = 'BeautyStock'
    expect(appName).toBe('BeautyStock')
  })
})