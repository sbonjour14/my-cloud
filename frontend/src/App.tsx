import { useState } from 'react'
import { useTheme } from './hooks/useTheme'
import { Input } from './components/ui/input';
import { Label } from './components/ui/label';
import { Button } from './components/ui/button';

export type RegisterFormBody = {
  displayName: string,
  email: string,
  password: string,
}

export default function App() {
  const [form, setForm] = useState<RegisterFormBody>({
    displayName: "",
    email: "",
    password: ""
  });
  const [confirmPassword, setConfirmPassword] = useState("");

  const setUsername = (value: string) => {
    setForm(prev => ({ ...prev, displayName: value }));
  }
  const setEmail = (value: string) => {
    setForm(prev => ({ ...prev, email: value }));
  }
  const setPassword = (value: string) => {
    setForm(prev => ({ ...prev, password: value }));
  }

  const { theme, toggleTheme } = useTheme()

  const handleSubmit = (e: React.SubmitEvent) => {
    e.preventDefault();

    if (form.password !== confirmPassword) {
      alert("Les mots de passe ne correspondent pas");
      return;
    }

    fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    })
  }

  return (
    <>
      <div>
        <button className="bg-blue-500 text-white p-2 rounded" onClick={toggleTheme}>
          {theme === 'dark' ? '☀️ Clair' : '🌙 Sombre'}
        </button>
      </div>

      <div className='mt-10 flex flex-col items-center justify-center'>
        <form onSubmit={handleSubmit}>

          <Label className="block mb-2 text-sm font-medium text-gray-900 dark:text-white" htmlFor="username">
            username
            <Input
              type="text"
              id="username"
              value={form.displayName}
              onChange={(e) => setUsername(e.target.value)}
            />
          </Label>

          <Label className="block mb-2 text-sm font-medium text-gray-900 dark:text-white" htmlFor="email">
            email
            <Input
              type="email"
              id="email"
              value={form.email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </Label>

          <Label className="block mb-2 text-sm font-medium text-gray-900 dark:text-white" htmlFor="password">
            password
            <Input
              type="password"
              id="password"
              value={form.password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Label>

          <Label className="block mb-2 text-sm font-medium text-gray-900 dark:text-white" htmlFor="confirm-password">
            confirm password
            <Input
              type="password"
              id="confirm-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </Label>

          <Button type="submit" className="mt-4 bg-blue-500 text-white p-2 rounded">
            register
          </Button>
        </form>
      </div>
    </>
  )
}