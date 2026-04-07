// web/src/services/ErrorHandler.ts
// Strategy Pattern: Centralized error handling for components

export enum ErrorType {
  NETWORK = 'NETWORK',
  AUTHENTICATION = 'AUTHENTICATION',
  VALIDATION = 'VALIDATION',
  NOT_FOUND = 'NOT_FOUND',
  CONFLICT = 'CONFLICT',
  SERVER = 'SERVER',
  UNKNOWN = 'UNKNOWN',
}

export interface ErrorInfo {
  type: ErrorType
  message: string
  statusCode?: number
  details?: any
}

/**
 * Strategy Pattern for error handling.
 * Classifies and formats errors for consistent UI presentation.
 */
export class ErrorHandler {
  /**
   * Classify error by type and return formatted error info.
   */
  static handle(error: any): ErrorInfo {
    if (!error) {
      return {
        type: ErrorType.UNKNOWN,
        message: 'An unknown error occurred',
      }
    }

    // Axios error response
    if (error.response) {
      const status = error.response.status
      const data = error.response.data

      switch (status) {
        case 400:
          return {
            type: ErrorType.VALIDATION,
            message: data?.message || 'Invalid input',
            statusCode: status,
            details: data,
          }
        case 401:
          return {
            type: ErrorType.AUTHENTICATION,
            message: 'Your session has expired. Please log in again.',
            statusCode: status,
            details: data,
          }
        case 403:
          return {
            type: ErrorType.AUTHENTICATION,
            message: 'You do not have permission to perform this action.',
            statusCode: status,
            details: data,
          }
        case 404:
          return {
            type: ErrorType.NOT_FOUND,
            message: 'The requested resource was not found.',
            statusCode: status,
            details: data,
          }
        case 409:
          return {
            type: ErrorType.CONFLICT,
            message: data?.message || 'This resource already exists.',
            statusCode: status,
            details: data,
          }
        case 500:
          return {
            type: ErrorType.SERVER,
            message: 'Server error. Please try again later.',
            statusCode: status,
            details: data,
          }
        default:
          return {
            type: ErrorType.SERVER,
            message: error.message || 'An error occurred',
            statusCode: status,
            details: data,
          }
      }
    }

    // Network error
    if (error.message === 'Network Error' || !error.response) {
      return {
        type: ErrorType.NETWORK,
        message: 'Network error. Please check your connection.',
      }
    }

    // Generic error
    return {
      type: ErrorType.UNKNOWN,
      message: error.message || 'An unknown error occurred',
    }
  }

  /**
   * Get user-friendly message for display.
   */
  static getMessage(error: any): string {
    const errorInfo = this.handle(error)
    return errorInfo.message
  }

  /**
   * Check if error is authentication-related.
   */
  static isAuthError(error: any): boolean {
    const errorInfo = this.handle(error)
    return errorInfo.type === ErrorType.AUTHENTICATION
  }

  /**
   * Check if error is network-related.
   */
  static isNetworkError(error: any): boolean {
    const errorInfo = this.handle(error)
    return errorInfo.type === ErrorType.NETWORK
  }
}
