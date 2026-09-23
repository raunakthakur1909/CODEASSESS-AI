import React from 'react';
import Editor from '@monaco-editor/react';

interface CodeEditorProps {
    language: string;
    value: string;
    onChange: (value: string | undefined) => void;
    readOnly?: boolean;
}

const CodeEditor: React.FC<CodeEditorProps> = ({ language, value, onChange, readOnly = false }) => {
    
    // Map our backend languages to Monaco languages
    const monacoLanguage = language.toLowerCase() === 'cpp' ? 'cpp' :
                           language.toLowerCase() === 'c' ? 'c' :
                           language.toLowerCase() === 'java' ? 'java' :
                           language.toLowerCase() === 'python' ? 'python' : 'plaintext';

    return (
        <div style={{ height: '100%', width: '100%', border: '1px solid #333' }}>
            <Editor
                height="100%"
                language={monacoLanguage}
                theme="vs-dark"
                value={value}
                onChange={onChange}
                options={{
                    readOnly,
                    minimap: { enabled: false },
                    fontSize: 14,
                    wordWrap: 'on',
                    scrollBeyondLastLine: false,
                    automaticLayout: true
                }}
            />
        </div>
    );
};

export default CodeEditor;
