import React from 'react';
import { Moon, Sun } from 'lucide-react';
import { useTheme } from '../../context/ThemeContext';

export const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme, isDark } = useTheme();

  return (
    <button
      type="button"
      id="theme-toggle-btn"
      className="icon-button"
      onClick={toggleTheme}
      aria-label={isDark ? 'Switch to Light theme' : 'Switch to Dark theme'}
      title={isDark ? 'Switch to Light theme' : 'Switch to Dark theme'}
    >
      {isDark ? <Sun size={17} style={{ color: '#fbbf24' }} /> : <Moon size={16} />}
    </button>
  );
};
