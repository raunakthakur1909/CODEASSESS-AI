import React from 'react';

interface LanguageSelectorProps {
    value: string;
    onChange: (language: string) => void;
    disabled?: boolean;
}

/*
 * Must stay in sync with the backend Language enum, which is what maps to a
 * Judge0 language id. Offering anything else lets a candidate pick a language
 * that Run and Submit will reject.
 */
const LANGUAGES = [
    { id: 'JAVA', label: 'Java 25' },
    { id: 'PYTHON', label: 'Python 3' },
    { id: 'CPP', label: 'C++' },
    { id: 'C', label: 'C' }
];

const LanguageSelector: React.FC<LanguageSelectorProps> = ({ value, onChange, disabled = false }) => {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            disabled={disabled}
            style={{
                padding: '6px 12px',
                borderRadius: '4px',
                border: '1px solid #444',
                backgroundColor: '#2d2d2d',
                color: '#fff',
                outline: 'none',
                cursor: disabled ? 'not-allowed' : 'pointer'
            }}
        >
            {LANGUAGES.map(lang => (
                <option key={lang.id} value={lang.id}>{lang.label}</option>
            ))}
        </select>
    );
};

export default LanguageSelector;
