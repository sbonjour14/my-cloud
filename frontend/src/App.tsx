import { Routes, Route } from "react-router-dom";
import  {ProtectedRoute} from "./components/layout/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import { HomePage } from "./pages/HomePage";
import { useTheme } from "./hooks/useTheme";

export default function App() {
  useTheme();
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<HomePage/>}/>

      <Route element={<ProtectedRoute />}>

      </Route>
    </Routes>
  );
}