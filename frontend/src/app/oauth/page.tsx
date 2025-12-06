'use client'

import { useEffect } from 'react';

export default function OAuthRedirect() {
    useEffect(() => {
        // Use window.location for full page reload to ensure cookies are properly set
        window.location.href = '/dashboard';
    }, []);

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
                <p className="mt-4 text-gray-600">Completing authentication...</p>
            </div>
        </div>
    );
}