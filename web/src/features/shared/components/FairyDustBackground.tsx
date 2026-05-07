import { useEffect, useRef } from 'react'

const DUST_COUNT = 60

function random(min: number, max: number) {
  return Math.random() * (max - min) + min
}

function matchesPalette(r: number, g: number, b: number): boolean {
  // Overall must be light enough to show black
  if (r < 200 || g < 200 || b < 200) return false
  // Exclude pure/near-white (dust would be invisible)
  if (r > 250 && g > 250 && b > 250) return false
  // Exclude very grey/neutral whites
  const range = Math.max(r, g, b) - Math.min(r, g, b)
  if (range < 10) return false
  // Accept warm pastel tints used in the app (cream, pink, rose, gold, sage)
  return true
}

function getBackgroundColorAt(x: number, y: number): string | null {
  try {
    const el = document.elementFromPoint(x, y)
    if (!el) return null
    const style = getComputedStyle(el)
    let bg = style.backgroundColor
    if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent') {
      return bg
    }
    let parent = el.parentElement
    while (parent) {
      const parentBg = getComputedStyle(parent).backgroundColor
      if (parentBg && parentBg !== 'rgba(0, 0, 0, 0)' && parentBg !== 'transparent') {
        return parentBg
      }
      parent = parent.parentElement
    }
  } catch {
    // ignore
  }
  return null
}

function rgbFromStyle(bg: string): [number, number, number] | null {
  const match = bg.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/)
  if (match) {
    return [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])]
  }
  if (bg.startsWith('#')) {
    const clean = bg.replace('#', '')
    if (clean.length === 6) {
      return [
        parseInt(clean.slice(0, 2), 16),
        parseInt(clean.slice(2, 4), 16),
        parseInt(clean.slice(4, 6), 16)
      ]
    }
  }
  return null
}

export function FairyDustBackground() {
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!containerRef.current) return
    containerRef.current.innerHTML = ''
    Array.from({ length: DUST_COUNT }).forEach(() => {
      const el = document.createElement('div')
      el.className = 'fairy-dust-particle'

      const top = random(0, window.innerHeight)
      const left = random(0, window.innerWidth)
      const bgColor = getBackgroundColorAt(left, top)
      const rgb = bgColor ? rgbFromStyle(bgColor) : null
      const showDust = rgb ? matchesPalette(rgb[0], rgb[1], rgb[2]) : false

      el.style.cssText = `
        position: fixed;
        width: ${random(2, 5)}px;
        height: ${random(2, 5)}px;
        background: black;
        border-radius: 50%;
        pointer-events: none;
        z-index: -1;
        top: ${top}px;
        left: ${left}px;
        opacity: ${showDust ? 0.15 : 0};
        animation: ${showDust ? 'twinkle' : 'none'} ${random(2, 4)}s ease-in-out infinite;
        animation-delay: ${random(0, 3)}s;
        transition: opacity 0.3s ease;
      `
      containerRef.current!.appendChild(el)

      const updateVisibility = () => {
        if (!el.parentElement) return
        const rect = el.getBoundingClientRect()
        const newBg = getBackgroundColorAt(rect.left + 1, rect.top + 1)
        const newRgb = newBg ? rgbFromStyle(newBg) : null
        const shouldShow = newRgb ? matchesPalette(newRgb[0], newRgb[1], newRgb[2]) : false
        el.style.opacity = shouldShow ? '0.15' : '0'
      }

      window.addEventListener('scroll', updateVisibility, { passive: true })
      window.addEventListener('resize', updateVisibility)
      return () => {
        window.removeEventListener('scroll', updateVisibility)
        window.removeEventListener('resize', updateVisibility)
      }
    })
  }, [])

  return (
    <div
      ref={containerRef}
      aria-hidden="true"
      style={{
        position: 'fixed',
        inset: 0,
        overflow: 'hidden',
        pointerEvents: 'none',
        zIndex: -1
      }}
    />
  )
}
