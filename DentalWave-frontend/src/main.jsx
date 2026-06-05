// Entry point for the React application.
// Creates the React root and renders the App component.

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import './services/AxiosConfig'

// Render the application into the root element
ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);