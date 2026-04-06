import { useEffect, useRef } from 'react'

const GOOGLE_CLIENT_ID = '385645622527-ua4rm4umb58oesk09tuamqq5kbdijuqu.apps.googleusercontent.com'

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: any) => void
          renderButton: (element: HTMLElement, config: any) => void
          prompt: () => void
        }
      }
    }
  }
}

interface GoogleSignInButtonProps {
  onSuccess: (idToken: string) => void
  onError?: (error: string) => void
  text?: 'signin_with' | 'signup_with' | 'continue_with'
}

export function GoogleSignInButton({ onSuccess, onError, text = 'continue_with' }: GoogleSignInButtonProps) {
  const buttonRef = useRef<HTMLDivElement>(null)
  const initialized = useRef(false)

  useEffect(() => {
    const initializeGoogle = () => {
      if (!window.google || !buttonRef.current || initialized.current) return
      initialized.current = true

      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: (response: any) => {
          if (response.credential) {
            onSuccess(response.credential)
          } else {
            onError?.('Google sign-in failed')
          }
        },
      })

      window.google.accounts.id.renderButton(buttonRef.current, {
        theme: 'outline',
        size: 'large',
        width: '100%',
        text: text,
        shape: 'rectangular',
        logo_alignment: 'left',
      })
    }

    // Load the Google Identity Services script if not yet loaded
    if (window.google) {
      initializeGoogle()
    } else {
      const existingScript = document.querySelector('script[src*="accounts.google.com/gsi/client"]')
      if (!existingScript) {
        const script = document.createElement('script')
        script.src = 'https://accounts.google.com/gsi/client'
        script.async = true
        script.defer = true
        script.onload = initializeGoogle
        document.head.appendChild(script)
      } else {
        existingScript.addEventListener('load', initializeGoogle)
      }
    }
  }, [onSuccess, onError, text])

  return (
      <div className="w-full">
      <div ref={buttonRef} className="flex justify-center w-full" />
      <div className="auth-divider flex justify-center">
        <span>or</span>
      </div>
    </div>
  )
}
