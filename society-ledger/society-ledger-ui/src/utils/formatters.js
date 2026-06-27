/**
 * Format a number as Indian currency (₹)
 * e.g. 25000 → "₹25,000"  |  1250000 → "₹12,50,000"
 */
export function formatINR(value) {
  if (value === null || value === undefined || value === '') return '₹0'
  const num = Number(value)
  if (isNaN(num)) return '₹0'
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(num)
}

/**
 * Format a date string or Date object as "15 Jan 2024"
 */
export function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (isNaN(d.getTime())) return String(value)
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

/**
 * Format month (1-12) and year as "January 2024"
 */
export function formatMonthYear(month, year) {
  if (!month || !year) return '—'
  return new Date(year, month - 1).toLocaleString('default', {
    month: 'long', year: 'numeric'
  })
}

/**
 * Truncate text to maxLen chars with ellipsis
 */
export function truncate(text, maxLen = 80) {
  if (!text) return ''
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text
}

/**
 * Format file size bytes → "2.4 MB"
 */
export function formatFileSize(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024)       return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

/**
 * Debounce a function call
 */
export function debounce(fn, delay) {
  let timer
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
