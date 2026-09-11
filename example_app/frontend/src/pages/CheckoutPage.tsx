import { FormEvent, useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutPreview, createAddress, getAddresses, placeOrder } from '../api'
import { useBasket } from '../context/BasketContext'
import type { CheckoutPreview, SavedAddress, ShippingInfo } from '../types'
import { formatCurrency } from '../utils'

const initialShippingInfo: ShippingInfo = {
  fullName: '',
  phoneNumber: '',
  addressLine: '',
  city: '',
  postalCode: '',
}

type Step = 1 | 2 | 3

export function CheckoutPage() {
  const PROCESSING_STEPS = [
    'Verifying inventory...',
    'Securing connection...',
    'Authorizing payment method...',
    'Finalizing your order...',
    'Preparing confirmation receipt...'
  ]

  const MIN_DELAY_MILLIS= 6000
  const MAX_DELAY_MILLIS= 8000
  const FRAME_RENDER_TIMEOUT_MS=16
  const BURST_DURATION_MS=50
  const READY_STATE_ATTR_NAME= 'readyState'
  const DOC_NOT_READY_STATE = 'loading'
  const READY_STATE_CHANGE_EVENT_NAME = 'readystatechange'

  const navigate = useNavigate()
  const { basket, refreshBasket } = useBasket()
  const [step, setStep] = useState<Step>(1)
  const [shippingInfo, setShippingInfo] = useState<ShippingInfo>(initialShippingInfo)
  const [savedAddresses, setSavedAddresses] = useState<SavedAddress[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState<string>('')
  const [saveAddressOnContinue, setSaveAddressOnContinue] = useState(false)
  const [preview, setPreview] = useState<CheckoutPreview | null>(null)
  const [promoCode, setPromoCode] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [loadingStep, setLoadingStep] = useState(0)
  const stepIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)
  // small utility helper to prevent quick action performing
  const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))
  // helper to generate a random number between a min and max
  const getRandomRange = (min: number, max: number) => {
      return Math.floor(Math.random() * (max - min + 1) + min)
  }

  useEffect(() => {
    getAddresses()
      .then((addresses) => {
        setSavedAddresses(addresses)
        const defaultAddress = addresses.find((address) => address.isDefault) ?? addresses[0]
        if (defaultAddress) {
          setSelectedAddressId(defaultAddress.id)
          setShippingInfo({
            fullName: defaultAddress.fullName,
            phoneNumber: defaultAddress.phoneNumber,
            addressLine: defaultAddress.addressLine,
            city: defaultAddress.city,
            postalCode: defaultAddress.postalCode,
          })
        }
      })
      .catch(() => {
        // Keep manual entry available when addresses cannot be loaded.
      })
  }, [])

  const applySavedAddress = (addressId: string) => {
    setSelectedAddressId(addressId)
    if (!addressId) {
      return
    }
    const selected = savedAddresses.find((address) => address.id === addressId)
    if (!selected) {
      return
    }
    setShippingInfo({
      fullName: selected.fullName,
      phoneNumber: selected.phoneNumber,
      addressLine: selected.addressLine,
      city: selected.city,
      postalCode: selected.postalCode,
    })
  }

  const handleShippingFieldChange = (field: keyof ShippingInfo, value: string) => {
    setSelectedAddressId('')
    setShippingInfo({ ...shippingInfo, [field]: value })
  }

  const handleShippingSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    try {
      if (saveAddressOnContinue && !selectedAddressId) {
        const created = await createAddress(shippingInfo, savedAddresses.length === 0)
        setSavedAddresses((current) => [...current, created])
        setSelectedAddressId(created.id)
      }
      setStep(2)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to save address'
      setError(message)
    }
  }

  const handlePaymentContinue = async () => {
    setError('')
    try {
      const result = await checkoutPreview(
        selectedAddressId ? undefined : shippingInfo,
        promoCode,
        selectedAddressId || undefined,
      )
      setPreview(result)
      setStep(3)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to preview checkout'
      setError(message)
    }
  }

  const handleApplyPromo = async () => {
    setError('')
    try {
      const result = await checkoutPreview(
        selectedAddressId ? undefined : shippingInfo,
        promoCode,
        selectedAddressId || undefined,
      )
      setPreview(result)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to apply promo code'
      setError(message)
    }
  }

  /**
   * Simulates heavy CPU utilization in controlled bursts.
   * This keeps the main thread busy (making the browser calculate constantly)
   * but yields control every 50ms so that CSS animations and timers can still run.
   */
  const simulateHeavyWork = (): (() => void) => {
    let isRunning = true

    const runBurst = () => {
      if (!isRunning) return

      const startTime = performance.now()

      // Active heavy calculation loop
      // Block thread for 50ms chunks
      while (performance.now() - startTime < BURST_DURATION_MS) {
        Math.sqrt(Math.random() * 100000)
      }

      // Yield to let the browser paint/render, then trigger the next burst
      setTimeout(runBurst, FRAME_RENDER_TIMEOUT_MS)
    }

    runBurst()

    // Return a clean ESLint-safe terminator function
    return () => {
      isRunning = false
    }
  }

  // Helper to temporarily override document.readyState
  const simulateLoadingReadyState = () => {
    // Save the original property descriptor from Document.prototype
    const originalDescriptor = Object.getOwnPropertyDescriptor(Document.prototype, READY_STATE_ATTR_NAME)

    // Redefine document.readyState to return 'interactive'
    Object.defineProperty(document, READY_STATE_ATTR_NAME, {
      get() {
        return DOC_NOT_READY_STATE
      },
      configurable: true
    })

    // Return a safe cleanup function
    return () => {
      if (originalDescriptor) {
        // Re-apply original descriptor back to the document instance
        Object.defineProperty(document, READY_STATE_ATTR_NAME, originalDescriptor)
      } else {
        // Clean up our local definition cleanly on the document instance
        Reflect.deleteProperty(document, READY_STATE_ATTR_NAME)
      }

      document.dispatchEvent(new Event(READY_STATE_CHANGE_EVENT_NAME))
    }
  }

  // Helper functions to cycle through steps
  const startLoadingAnimation = (stepDuration: number) => {
    setLoadingStep(0)
    stepIntervalRef.current = setInterval(() => {
      setLoadingStep((prevStep) => {
        if (prevStep < PROCESSING_STEPS.length - 1) {
          return prevStep + 1
        }
        return prevStep
      })
    }, stepDuration)
  }

  const stopLoadingAnimation = () => {
    if (stepIntervalRef.current) {
      clearInterval(stepIntervalRef.current)
      stepIntervalRef.current = null
    }
  }

  // 5. Clean up on component unmount
  useEffect(() => {
    return () => stopLoadingAnimation()
  }, [])

  const handlePlaceOrder = async () => {
    const totalDuration = getRandomRange(MIN_DELAY_MILLIS, MAX_DELAY_MILLIS)
    const stepDuration = totalDuration / PROCESSING_STEPS.length
    setSubmitting(true)
    setError('')

    startLoadingAnimation(stepDuration) // <-- Start simulating here

    // Start the readyState simulation and keep a reference to its cleanup function
    const restoreReadyState = simulateLoadingReadyState()

    // Start the heavy CPU work thread
    const stopHeavyWork = simulateHeavyWork()

    // Dispatch a change event immediately so any third-party scripts detect the change
    document.dispatchEvent(new Event(READY_STATE_CHANGE_EVENT_NAME))

    try {
      // The "await" will block until processing will be finished.
      const [order] = await Promise.all([
          placeOrder(
            selectedAddressId ? undefined : shippingInfo,
            promoCode,
            selectedAddressId || undefined,
          ),
          delay(totalDuration)
      ])
      await refreshBasket()
      // Stop simulation and restore readyState before navigating
      stopHeavyWork()
      restoreReadyState()

      stopLoadingAnimation() // <-- Stop simulating before navigating
      navigate(`/checkout/success/${order.orderId}`)
    } catch (err: unknown) {
      stopLoadingAnimation() // <-- Stop simulating if there's an error
      const message = err instanceof Error ? err.message : 'Failed to place order'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  if (basket.items.length === 0) {
    return (
      <section>
        <h1>Checkout</h1>
        <p>Your basket is empty. Add products before checkout.</p>
      </section>
    )
  }

  return (
    <section>
      <h1>Checkout</h1>
      <p className="muted">Step {step} of 3</p>
      {error && <p className="error">{error}</p>}

      {step === 1 && (
        <form className="card form" onSubmit={(event) => void handleShippingSubmit(event)}>
          <h2>Shipping information</h2>
          {savedAddresses.length > 0 && (
            <label>
              Saved address
              <select
                className="input"
                value={selectedAddressId}
                onChange={(event) => applySavedAddress(event.target.value)}
              >
                <option value="">Enter a new address</option>
                {savedAddresses.map((address) => (
                  <option key={address.id} value={address.id}>
                    {address.fullName} — {address.addressLine}, {address.city}
                    {address.isDefault ? ' (default)' : ''}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label>
            Full name
            <input
              className="input"
              required
              value={shippingInfo.fullName}
              onChange={(event) => handleShippingFieldChange('fullName', event.target.value)}
            />
          </label>
          <label>
            Phone number
            <input
              className="input"
              required
              value={shippingInfo.phoneNumber}
              onChange={(event) => handleShippingFieldChange('phoneNumber', event.target.value)}
            />
          </label>
          <label>
            Address line
            <input
              className="input"
              required
              value={shippingInfo.addressLine}
              onChange={(event) => handleShippingFieldChange('addressLine', event.target.value)}
            />
          </label>
          <label>
            City
            <input
              className="input"
              required
              value={shippingInfo.city}
              onChange={(event) => handleShippingFieldChange('city', event.target.value)}
            />
          </label>
          <label>
            Postal code
            <input
              className="input"
              required
              value={shippingInfo.postalCode}
              onChange={(event) => handleShippingFieldChange('postalCode', event.target.value)}
            />
          </label>
          {!selectedAddressId && (
            <label className="row">
              <input
                type="checkbox"
                checked={saveAddressOnContinue}
                onChange={(event) => setSaveAddressOnContinue(event.target.checked)}
              />
              Save this address for later
            </label>
          )}
          <button className="button" type="submit">
            Continue to payment
          </button>
        </form>
      )}

      {step === 2 && (
        <div className="card">
          <h2>Payment method</h2>
          <p>Only one option is available for this demo:</p>
          <p>
            <strong>Cash on Delivery (COD)</strong>
          </p>
          <div className="row">
            <button className="button secondary" onClick={() => setStep(1)}>
              Back
            </button>
            <button className="button" onClick={() => void handlePaymentContinue()}>
              Continue to review
            </button>
          </div>
        </div>
      )}

      {step === 3 && preview && (
        <div className="card">
          <h2>Review your order</h2>
          <p>
            <strong>Ship to:</strong> {preview.shippingInfo.fullName}, {preview.shippingInfo.addressLine},{' '}
            {preview.shippingInfo.city}, {preview.shippingInfo.postalCode}
          </p>
          <p>
            <strong>Phone:</strong> {preview.shippingInfo.phoneNumber}
          </p>
          <p>
            <strong>Payment:</strong> {preview.paymentMethod}
          </p>

          <div className="list">
            {preview.items.map((item) => (
              <div className="list-row" key={item.productId}>
                <span>
                  {item.productName} x {item.quantity}
                </span>
                <span>{formatCurrency(item.lineTotal)}</span>
              </div>
            ))}
          </div>

          <label>
            Promo code
            <div className="row">
              <input
                className="input"
                value={promoCode}
                placeholder="e.g. SAVE10"
                onChange={(event) => setPromoCode(event.target.value)}
              />
              <button className="button secondary" type="button" onClick={() => void handleApplyPromo()}>
                Apply
              </button>
            </div>
          </label>
          {preview.promoCode && preview.discountAmount != null && preview.discountAmount > 0 && (
            <p className="muted">
              Promo {preview.promoCode}: −{formatCurrency(preview.discountAmount)}
            </p>
          )}

          {preview.deliveryFee != null && (
            <p>
              <strong>Delivery fee:</strong> {formatCurrency(preview.deliveryFee)}
            </p>
          )}

          <p className="price">Total: {formatCurrency(preview.totalAmount)}</p>
          <div className="row">
            <button className="button secondary" disabled={submitting} onClick={() => setStep(2)}>
              Back
            </button>
            <button className="button" disabled={submitting} onClick={() => void handlePlaceOrder()}>
              {submitting ? 'Placing order...' : 'Place order'}
            </button>
              {/*Overlay with statues*/}
            {submitting && (
                <div className="order-loading-overlay">

                  {/* Dynamic Animated Spinner */}
                  <div className="order-loading-spinner" />

                  {/* Step Feedback Description */}
                  <p className="order-loading-text">
                    {PROCESSING_STEPS[loadingStep]}
                  </p>

                  {/* Visual Progress Meter */}
                  <div className="order-progress-track">
                    <div
                        className="order-progress-fill"
                        style={{ width: `${((loadingStep + 1) / PROCESSING_STEPS.length) * 100}%` }}
                    />
                  </div>

                </div>
            )}
          </div>
        </div>
      )}
    </section>
  )
}
