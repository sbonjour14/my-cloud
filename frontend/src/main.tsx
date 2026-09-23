import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import { Toaster } from './components/ui/toast'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { UploadProvider } from './provider/upload/uploadProvider'

const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <Toaster>
        <UploadProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </UploadProvider>
      </Toaster>
    </QueryClientProvider>
  </StrictMode>,
)
