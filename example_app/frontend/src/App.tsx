import { BrowserRouter } from 'react-router-dom'
import { AppRoutes } from './AppRoutes'
import { AuthProvider } from './context/AuthContext'
import { BasketProvider } from './context/BasketContext'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <BasketProvider>
          <AppRoutes />
        </BasketProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
